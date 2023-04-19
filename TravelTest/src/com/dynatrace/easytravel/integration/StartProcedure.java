package com.dynatrace.easytravel.integration;

import static com.dynatrace.easytravel.launcher.scenarios.builder.SettingBuilder.antProperty;
import static com.dynatrace.easytravel.launcher.scenarios.builder.SettingBuilder.property;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.Procedure;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.procedures.AntProcedure;
import com.dynatrace.easytravel.launcher.procedures.CassandraProcedure;
import com.dynatrace.easytravel.launcher.procedures.CreditCardAuthorizationProcedure;
import com.dynatrace.easytravel.launcher.procedures.DatabaseContentCreationProcedure;
import com.dynatrace.easytravel.launcher.procedures.DbmsProcedure;
import com.dynatrace.easytravel.launcher.procedures.MongoDbProcedure;
import com.dynatrace.easytravel.launcher.procedures.PaymentBackendProcedure;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureSetting;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.builder.ProcedureBuilder;
import com.google.common.collect.Lists;

/**
 * Small test application which starts a single procedure and allows to quickly test something without
 * starting full Launcher/Scenarios.
 *
 * @author dominik.stadler
 */
public class StartProcedure {

	private static final Logger log = Logger.getLogger(Launcher.class.getName());

	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, args[0]);
		}
//		runMongoDb();
		runCassandraNodes(3);
//		runInternalDatabase();
		//runDotNet();
		//runCCAS();
		//runCCA();
//		runSeleniumTests();

		//final Process exec = Runtime.getRuntime().exec("C:\\data\\easyTravel\\Distribution\\dist\\CreditCardAuthorization64.exe");
		/*final Process exec = Runtime.getRuntime().exec(new String[] {
				"C:\\data\\easyTravel\\Distribution\\dist\\CreditCardAuthorization64.exe"
		});*/
		/*new Thread() {

			@Override
			public void run() {
				try {
					while (true) {
						System.err.write(exec.getErrorStream().read());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}.start();

		new Thread() {

			@Override
			public void run() {
				try {
					while (true) {
						System.out.write(exec.getInputStream().read());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}.start();*/
	}

	public static void runDotNet() throws IOException, CorruptInstallationException, InterruptedException {
		log.info("Starting .NET procedure with install-dir: " + Directories.getInstallDir());
		PaymentBackendProcedure proc = new PaymentBackendProcedure(new DefaultProcedureMapping(
				Constants.Procedures.PAYMENT_BACKEND_ID));
		runProcedure(proc);
	}

	public static void runMongoDb() throws IOException, CorruptInstallationException, InterruptedException {
		MongoDbProcedure proc = newMongoDbProcedure();
		runProcedure(proc);
	}

	public static void runCassandraNodes(int instanceNo) throws IOException, CorruptInstallationException, InterruptedException {
		Procedure[] instances = new Procedure[instanceNo];
		for (int i = 0; i < instanceNo; i++) {
			instances[i] = newCassandraProcedure();
		}
		runProcedures(instances);
	}

	public static void runInternalDatabase() throws InterruptedException, IOException {
		DbmsProcedure dbms = new DbmsProcedure(new DefaultProcedureMapping(
				Constants.Procedures.INPROCESS_DBMS_ID));
		runProcedure(dbms);
	}

	public static MongoDbProcedure newMongoDbProcedure() throws CorruptInstallationException {
		log.info("Starting MongoDb procedure with install-dir: " + Directories.getInstallDir());
		return new MongoDbProcedure(new DefaultProcedureMapping(
				Constants.Procedures.MONGO_DB_ID));
	}

	public static CassandraProcedure newCassandraProcedure() throws CorruptInstallationException {
		log.info("Starting Cassandra procedure with install-dir: " + Directories.getInstallDir());
		return new CassandraProcedure(new DefaultProcedureMapping(
				Constants.Procedures.CASSANDRA_ID));
	}

	public static DatabaseContentCreationProcedure newCreateDatabaseContentProcedure(String persistenceMode)
			throws CorruptInstallationException {
		log.info("Starting MongoDb procedure with install-dir: " + Directories.getInstallDir());
		DefaultProcedureMapping mapping = new DefaultProcedureMapping(
				Constants.Procedures.DATABASE_CONTENT_CREATOR_ID);
		mapping.addSetting(new DefaultProcedureSetting(Constants.Misc.SETTING_PERSISTENCE_MODE, persistenceMode));
		return new DatabaseContentCreationProcedure(mapping);
	}

	public static void runCCA() throws IOException, CorruptInstallationException, InterruptedException {
		log.info("Starting CreditCardAuthorization procedure with install-dir: " + Directories.getInstallDir());

		CreditCardAuthorizationProcedure proc = new CreditCardAuthorizationProcedure(new DefaultProcedureMapping(
				Constants.Procedures.CREDIT_CARD_AUTH_UNIT_ID));
		runProcedure(proc);
	}

	public static void runCCAS() throws IOException, CorruptInstallationException, InterruptedException {
		log.info("Starting CreditCardAuthorization procedure with install-dir: " + Directories.getInstallDir());

		DefaultProcedureMapping mapping = new DefaultProcedureMapping(
				Constants.Procedures.CREDIT_CARD_AUTH_UNIT_ID);
		mapping.addSetting(new DefaultProcedureSetting(CreditCardAuthorizationProcedure.SETTING_IPC_MODE, CreditCardAuthorizationProcedure.IpcMode.Socket.name()));
		CreditCardAuthorizationProcedure proc = new CreditCardAuthorizationProcedure(mapping);
		runProcedure(proc);
	}

	public static void runSeleniumTests() throws IOException, CorruptInstallationException, InterruptedException {
		log.info("Starting Selenium procedure with install-dir: " + Directories.getInstallDir());

		assertTrue("File: " + new File(Directories.getInstallDir(), BaseConstants.SubDirectories.SELENIUM + "/runtest.xml"),
				new File(Directories.getInstallDir(), BaseConstants.SubDirectories.SELENIUM + "/runtest.xml").exists());

		// don't use ie on linux
		String browser = OperatingSystem.WINDOWS == OperatingSystem.pickUp() ? "ie" : "ff";

		ProcedureMapping mapping = ProcedureBuilder.ant().
				set(property(AntProcedure.TITLE).value(MessageConstants.MODULE_JUNIT_TESTS)).
				set(property(AntProcedure.FILE).value(BaseConstants.SubDirectories.SELENIUM + "/runtest.xml")).
				// requires dynaTrace Server: set(property(AntProcedure.TARGET).value("runAllTestsOnce")).
				set(property(AntProcedure.TARGET).value("runTestsWithDefaultBrowser")).
				set(property(AntProcedure.RECURRENCE).value("1")).
				set(property(AntProcedure.INSTRUMENTATION).value(AntProcedure.SETTING_VALUE_ON)).
				set(property(AntProcedure.FORK).value(Boolean.TRUE.toString())).
				set(antProperty(AntProcedure.TEST_REPORT_DIR).value(new File(Directories.getExistingTestsDir(), BaseConstants.SubDirectories.JUNIT).getAbsolutePath())).
				set(antProperty(AntProcedure.WEBDRIVER_BROWSER).value(browser)).
				create();

		AntProcedure proc = new AntProcedure(mapping);
		runProcedure(proc);
	}

	private static void runProcedure(Procedure proc) throws InterruptedException, IOException {
		Feedback feed = proc.run();
		log.info("Result from starting procedure: " + feed);

		if (feed.equals(Feedback.Success) || feed.equals(Feedback.Neutral)) {
			int count = 0;
			for(;!proc.isOperating() && count < 10;count ++) {
				log.info("Waiting for procedure to become operating: " + count);
				Thread.sleep(5000);
			}
			if(count == 10) {
				log.warning("Stopping because of timeout during startup");
				proc.stop();
			}

			System.out.println("Press a key to stop procedure!");
			System.in.read();
		}

		log.info("Stopping procedure.");
		proc.stop();
	}

	private static void runProcedures(Procedure... procs) throws InterruptedException, IOException {
		ExecutorService executor = Executors.newCachedThreadPool();
		ArrayList<Future<Boolean>> futures = Lists.newArrayList();
		for (final Procedure proc : procs) {
			futures.add(executor.submit(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {
					Feedback feed = proc.run();
					log.info("Result from starting procedure: " + feed);

					if (feed.equals(Feedback.Success) || feed.equals(Feedback.Neutral)) {
						int count = 0;
						for (; !proc.isOperating() && count < 20; count++) { // all little more tries for cassandra
							log.info("Waiting for procedure to become operating: " + count);
							TimeUnit.SECONDS.sleep(5);
						}
						if (count == 20) {
							log.warning("Stopping because of timeout during startup");
							proc.stop();
							throw new TimeoutException("Procedure " + proc.getName() + " did not start in time");
						}

						return true;
					}
					return false;
				}

			}));
		}

		for (Future<Boolean> future : futures) {
			Boolean result;
			try {
				result = future.get();
				if (!result) {
					stopAllProcedures(procs);
				}
			} catch (ExecutionException e) {
				System.out.println(e.getCause().getMessage());
				stopAllProcedures(procs);

				System.exit(1);
			}

		}
		System.out.println("Press a key to stop procedure!");
		System.in.read();

		log.info("Stopping procedure.");
		stopAllProcedures(procs);
	}

	private static void stopAllProcedures(Procedure... procs) {
		ExecutorService executor = Executors.newCachedThreadPool();
		Collection<Future<Void>> futures = Lists.newArrayList();
		for (final Procedure proc : procs) {
			executor.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					proc.stop();
					return null;
				}
			});
		}
		for (Future<Void> future : futures) {
			try {
				future.get();
			} catch (InterruptedException e) {
				System.out.println("Had following exception stopping: " + e.getMessage());
			} catch (ExecutionException e) {
				System.out.println("Had following exception stopping: " + e.getCause().getMessage());
			}
		}

	}
}
