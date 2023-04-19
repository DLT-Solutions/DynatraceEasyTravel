/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: MongoDbProcedure.java
 * @date: 08.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.launcher.procedures;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.AbstractNativeProcedure;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.mongodb.MongoDbConnection;
import com.google.common.base.Joiner;
import com.mongodb.MongoException;


/**
 *
 * @author stefan.moschinski
 */
public class MongoDbProcedure extends AbstractNativeProcedure {

	private static final Logger log = Logger.getLogger(MongoDbProcedure.class.getName());

	private int nodeNo = 1;
	private String logfilePath;
	private String address;

	/**
	 *
	 * @param mapping
	 * @throws CorruptInstallationException
	 * @author stefan.moschinski
	 */
	public MongoDbProcedure(ProcedureMapping mapping) throws CorruptInstallationException {
		super(mapping);
		copyMongodbDataFolder();
		configureMongo();
	}

	/**
	 *
	 * @author stefan.moschinski
	 */
	private void configureMongo() {
		address = EasyTravelConfig.read().mongoDbInstances[0]; // just for now
		logfilePath = format("%s%smongo_%d.log", Directories.getLogDir(), File.separator, nodeNo);
		
		// see http://docs.mongodb.org/manual/reference/program/mongod/ for a description of these options
		process.addApplicationArgumentPair("--port", 27017)
				.addApplicationArgument("--dbpath").addApplicationArgument(Directories.getMongodbDataDir().getAbsoluteFile() + "/mongodb")
				.addApplicationArgumentPair("--logpath",
						logfilePath)
				.addApplicationArgument("--logappend")
				.addApplicationArgument("--journal")

				// JLT-77427: add two options which should reduce the size of the mongo-db database files
				.addApplicationArgument("--smallfiles")
				.addApplicationArgument("--noprealloc")
				.addApplicationArgument("--nssize").addApplicationArgument("1")
				.addApplicationArgument("--auth");
	}

	@Override
	public boolean isOperatingCheckSupported() {
		return true;
	}

	@Override
	public boolean isOperating() {
		MongoDbConnection connection = null;
		try {
			connection = MongoDbConnection.openConnection(address);
			return true;
		} catch (MongoException e) {
			log.log(Level.INFO, format("Got an error connecting to the MongoDB '%s'", address), e);
			return false;
		} finally {
			if (connection != null) {
				connection.closeConnection();
			}
		}
	}

	@Override
	public boolean hasLogfile() {
		return true;
	}

	@Override
	public String getLogfile() {
		return logfilePath;
	}

	@Override
	public Technology getTechnology() {
		return Technology.MONGODB;
	}

	@Override
	protected String getExecutable(ProcedureMapping mapping) {
		return Joiner.on(File.separator).join("mongodb", getOsDependentFolder(), "mongod");
	}

	private String getOsDependentFolder() {
		if (OperatingSystem.isCurrent(OperatingSystem.WINDOWS)) {
			return "windows";
		}

		return "linux" + File.separator + getBitness();
	}


	private String getBitness() {
		String bitness = System.getProperty(BaseConstants.SystemProperties.OS_ARCH);
		String folder = bitness == null || !bitness.contains("64") ? "32" : "64";
		return folder;
	}

	@Override
	protected String getWorkingDir() {
		return null;
	}

	@Override
	protected DtAgentConfig getAgentConfig() {
		return null;
	}

	@Override
	public Feedback stop() {
		log.warning("Stopping procedures. Stopping MongoDb");
		MongoDbConnection connection = null;
		try {
            /* shutdown montodb running only on localhost */
            connection = MongoDbConnection.openConnection("localhost:27017");
			connection.shutdownDatabase();
			log.warning("Stopping procedures. MongoDb stopped");
			return Feedback.Success;
		} catch (MongoException e) {
			log.log(Level.INFO, format("Got an error connecting to the MongoDB '%s'", address), e);
			return Feedback.Failure;
		} finally {
			if (connection != null) {
				connection.closeConnection();
			}
		}
	}

	@Override
	public boolean isInstrumentationSupported() {
		return false;
	}
	/**
	 * copy data/mongodb
	 * to EasyTravel configuration folder
	 */

	private void copyMongodbDataFolder() {
		File mongodbDataFolder = new File(Directories.getMongodbDataDir() .getAbsoluteFile() + "/");
		File mongodbDataDir = new File(Directories.getMongodbDir().getAbsolutePath() + "/mongodb");

		if (!mongodbDataFolder.exists()) {
			try {
				FileUtils.copyDirectoryToDirectory(mongodbDataDir, mongodbDataFolder);
			} catch (IOException e) {
				log.log(Level.WARNING,
						"Could not copy data/mongodb directory", e);
			}
		}
	}
}
