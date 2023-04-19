package com.dynatrace.easytravel.launcher.remote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import ch.qos.logback.classic.Logger;

import com.dynatrace.diagnostics.uemload.dcrum.DCRumResponse;
import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.config.*;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.*;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.procedures.utils.CentralTechnologyActivator;
import com.dynatrace.easytravel.launcher.process.Process;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureSetting;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.util.TextUtils;

/**
 * Handler for REST requests to start/stop procedures
 *
 * @author dominik.stadler
 */
@Path("/")
public class RESTProcedureControl {
	private static final Logger LOGGER = LoggerFactory.make(); // this writes to CmdLauncher.log

    private final static List<ProcedureStateListener> list = Collections.emptyList(); // NOPMD

	/*
	 * JLT-65218
	 * Limit constants for log file size.
	 * Note: these are applied to each type of log, i.e. agent bootstrap log, agent log, and application log.
	 */
    private static final int LOG_FILE_CONTENTS_MAX_LINES = 10000; // absolute lines max
	private static final int LOG_FILE_CONTENTS_MAX_CHARS = 80 * LOG_FILE_CONTENTS_MAX_LINES; // lines max * expected chars per line

    private final static Object lock = new Object();

    // static as this class is created for each REST call!
    private static Map<UUID, SingleProcedureBatch> batches = new ConcurrentHashMap<UUID, SingleProcedureBatch>();


    @GET
    @Produces("text/plain")
    @Path(Constants.REST.PREPARE + "/{name}")
    public synchronized String prepare(
            @PathParam("name") final String name,
            @QueryParam("property") final List<String> properties,
            @QueryParam("setting") final List<DefaultProcedureSetting> settings,
            @QueryParam("environment") final List<String> environment,
            @QueryParam("mode") final InstallationType mode) {
        // do a big synch here for now, we don't expect multiple accesses anyway, so rather be safe than sorry
        synchronized (lock) {
            LOGGER.info("Preparing procedure '" + name + "' with properties: " + properties + "\n\nsettings: " + settings + "\n\nenvironment: " + environment);

            // TODO: check for re-use of already running batch/procedure?

            // applly detected installation mode if it was sent
            if(mode != null) {
                DtVersionDetector.enforceInstallationType(mode);
            }

            DefaultProcedureMapping mapping = new DefaultProcedureMapping(name);

            if(settings != null && settings.size() > 0) {
                for(DefaultProcedureSetting setting : settings) {
                    //mapping.addSetting(DefaultProcedureSetting.valueOf(setting));
                    mapping.addSetting(setting);
                }
            }

            /* not done yet, usually we set these values in distributed environments manually currently
            // on Linux override plugin settings for Business Backend to enable the correct IPC mechanism
    		if(!OperatingSystem.IS_WINDOWS &&
    				mapping.getId().equals(Constants.Procedures.BUSINESS_BACKEND_ID)) {
    			LOGGER.info("Adjusting native plugins for non-windows platforms: Disable NamedPipe, enable Socket and Dummy.NET.");
    			mapping.addSetting(SettingBuilder.plugin(Constants.Plugin.NamedPipeNativeApplication).disable().create());
    			mapping.addSetting(SettingBuilder.plugin(Constants.Plugin.NamedPipeNativeApplication_NET).disable().create());
    			mapping.addSetting(SettingBuilder.plugin(Constants.Plugin.DummyNativeApplication_NET).enable().create());
    			mapping.addSetting(SettingBuilder.plugin(Constants.Plugin.SocketNativeApplication).enable().create());
    		}*/

            // put properties in place for procedures
            if(properties != null && properties.size() > 0) {
                EasyTravelConfig config = EasyTravelConfig.read();
                Properties props = new Properties();
                for(String property : properties) {
                    int pos = property.indexOf(Constants.REST.PROPERTY_DELIMITER);
                    if(pos == -1) {
                        props.setProperty(property, BaseConstants.EMPTY_STRING);
                    } else {
                        props.setProperty(property.substring(0, pos), property.substring(pos+1));
                    }
                }
                /* not done yet, usually we set these values in distributed environments manually currently
                // on Linux do not try to find agent in Windows-location
                if(!OperatingSystem.IS_WINDOWS &&
                		props.containsKey("config.agent") && props.getProperty("config.agent").startsWith("C:\\")) {
                	LOGGER.info("Re-setting windows-agent-path '" + props.getProperty("config.agent") + "' on non-Windows system to 'auto'");
                	props.setProperty("config.agent", "none");
                }*/

                // put the new properties in place
                config.enhance(props);

                // write the properties to a file and pass this as setting to the procedures
                try {
                    // get a unique file name
                    File file = File.createTempFile(EasyTravelConfig.PROPERTIES_FILE, ".properties", Directories.getExistingTempDir());

                    // store the properties to the file
                    // ensure that new properties (not yet in local config properties file) will be added
                    config.store(file, props.stringPropertyNames());

                    // set the file name so it is used when starting the procedure
                    config.filePath = file.getAbsolutePath();

                    // remember that we should remove this file when the process stops
                    file.deleteOnExit();
                } catch (IOException e) {
                    LOGGER.warn("Could not create temporary file for custom properties in directory " + Directories.getTempDir(), e);
                }
            }

            if(environment != null && environment.size() > 0) {
            	for(String property : environment) {
                    int pos = property.indexOf(Constants.REST.PROPERTY_DELIMITER);
                    if(pos == -1) {
                        System.clearProperty(property);
                    } else {
                        System.setProperty(property.substring(0, pos), property.substring(pos+1));
                    }
            	}
            }

   
            SingleProcedureBatch batch = new SingleProcedureBatch(mapping, list);

            // if we have a local batch running (i.e. we are not a Commandline Launcher, but a (Web)Launcher that is "re-used" for remote starting as well),
            // then show the additional procedure in the local Launcher as well!
            Batch runningBatch = LaunchEngine.getRunningBatch();
            if(runningBatch != null) {
            	runningBatch.addProcedure(batch.getProcedure());
            }

            UUID uuid = UUID.randomUUID();
            batches.put(uuid, batch);

            // return the UUID as result, "status" let's you query for the state
            return /*"Start: " + name +
            "\nProperties: " + properties.toString() +
            "\nSettings: " + settings.toString() +
            "\nBatch: " + */uuid.toString() + "|" + batch.getPort();
            //} finally {
                // have to reset values that we overwrote
                // TODO: this will revert any changed property file which is set via "-propertyfile" commandline
                // parameter in CommandlineLauncher! Either we allow to set the name of the property file or handle
                // this differently here via "enhance"/"undo" operations on EasyTravelConfig


                /* don't do this any more, this leads to cases where procedures read config-values in the run() part
                 * or asynchronously, thus reading fresh values if the config is reset here
                 *
                 * if(properties != null && properties.size() > 0) {
                    EasyTravelConfig.resetSingleton();
                }*/
            //}
        }
    }

    /**
     * attempts to start the procedure (if it is in one of the states: STOPPED, FAILED, UNKNOWN)
     *
     * @param uuid
     * @return the state of the procedure
     * @author peter.kaiser
     */
    @GET
    @Produces("text/plain")
    @Path(Constants.REST.START + "/{uuid}")
    public String start(
            @PathParam("uuid") final String uuid) {
		// do a big synch here for now, we don't expect multiple accesses anyway, so rather be safe than sorry
		synchronized (lock) {
			LOGGER.info("Starting procedure " + uuid);
		    SingleProcedureBatch batch = batches.get(UUID.fromString(uuid));
		    if (batch == null) {
				LOGGER.warn("(start) Could not find procedure for UUID: " + uuid);
				return "UNKNOWN - UUID not known";
		    }
		    State state = batch.getState();
		    if (state == State.OPERATING || state == State.TIMEOUT || state == State.STARTING || state == State.STOPPING) {
		        LOGGER.warn("(start) Can't start a procedure in state: " + batch.getState());
		        return State.FAILED.toString();
		    } else {
		        batch.start();
		        return batch.getState().toString();
		    }

		}
	}

	@GET
	@Produces("text/plain")
	@Path(Constants.REST.STOP + "/{uuid}")
    public synchronized String stop (
			@PathParam("uuid") final String uuid) {
		// do a big synch here for now, we don't expect multiple accesses anyway, so rather be safe than sorry
		synchronized (lock) {
			SingleProcedureBatch batch = batches.get(UUID.fromString(uuid));
			if(batch == null) {
				return "NOTOK - Unknown Batch-UUID provided: " + uuid;
			}

			if(State.OPERATING != batch.getState() && State.TIMEOUT != batch.getState()) {
				return "NOTOK - Cannot stop procedure, state is not OPERATING or TIMEOUT. Current state is: " + batch.getState();
			}

			batch.stop();

			//don't remove batch to keep log accessible
			//batches.remove(UUID.fromString(uuid));

			// just return ok or notok
			return "OK";
		}
	}

	@GET
	@Produces("text/plain")
	@Path(Constants.REST.STATUS + "/{uuid}")
    public String status(
			@PathParam("uuid") final String uuid) {
		// do a big synch here for now, we don't expect multiple accesses anyway, so rather be safe than sorry
		SingleProcedureBatch batch = batches.get(UUID.fromString(uuid));
		if(batch == null) {
			LOGGER.warn("(status) Could not find procedure for UUID: " + uuid);
			return "UNKNOWN - UUID not known";
		}
		synchronized (batch) {
			return batch.getState().toString();
		}
	}

	@GET
	@Produces("text/plain")
	@Path(Constants.REST.STATUS_ALL)
    public String statusAll() {
		StringBuilder ret = new StringBuilder();
		for(Map.Entry<UUID, SingleProcedureBatch> entry : batches.entrySet()) {
			ret.append(entry.getKey().toString()).append(": ").
			append(entry.getValue().getProcedure().getName()).append(": ").
			append(entry.getValue().getState()).append("/").append(entry.getValue().getProcedure().getStateLabel()).
			append("\n");
		}

		Batch batch = LaunchEngine.getRunningBatch();
		if(batch != null) {
			Scenario scenario = batch.getScenario();
			for(StatefulProcedure proc : batch.getProcedures()) {
				ret.append(scenario.getGroup()).append("-").append(scenario.getTitle()).append(": ").
				append(proc.getName()).append(": ").
				append(proc.getState()).append("/").append(proc.getStateLabel()).
				append("\n");
			}
		}

		return ret.toString();
	}


    /**
	 * Helper to stop all procedures that we started before
	 *
	 * @author dominik.stadler
	 */
	@GET
	@Produces("text/plain")
	@Path(Constants.REST.STOP_ALL)
	public static String stopAll() {
		// do a big synch here for now, we don't expect multiple accesses anyway, so rather be safe than sorry
		synchronized (lock) {
			for(SingleProcedureBatch batch : batches.values()) {
				if(State.STARTING.equals(batch.getState()) || State.OPERATING.equals(batch.getState()) || State.TIMEOUT.equals(batch.getState())) {
					batch.stop();
				}
			}
		}

		return "OK";
	}

	private static final StringBuilder readLogFileTail(File file, StringBuilder buf) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		try {
			return TextUtils.readTail(in, buf, LOG_FILE_CONTENTS_MAX_LINES, LOG_FILE_CONTENTS_MAX_CHARS);
		} finally {
			in.close();
		}
	}

	/**
	 *
	 *
	 * @param uuid
	 * @return
	 * @author peter.kaiser
	 */
	@GET
    @Produces("text/plain")
    @Path(Constants.REST.LOG + "/{uuid}")
    public String log(
            @PathParam("uuid") final String uuid) {
        //don't sync here as it would block the gui waiting for any other action that holds the lock to complete
//        synchronized (lock) {
            SingleProcedureBatch batch = batches.get(UUID.fromString(uuid));
            if (batch == null) {
                LOGGER.warn("(log) Could not find procedure for UUID: " + uuid);
                return "UNKNOWN - UUID not known";
            }
            Procedure procedure = batch.getProcedure();
            if (procedure.hasLogfile()) {
                // TODO: currently we always open the logfile with suffix 0.0.log, this might
                // not be the correct one if multiple Customer Frontends are started as part of a scenario...
	            File logFile = new File(procedure.getLogfile().replace("%u", "0").replace("%g", "0"));
	            if (logFile.exists()) {
	                try {
	                	//JLT-54554 for Java procedures, prepend the Java Agent bootstrap log and agent log
	                	Procedure proc = procedure;
	                	if (proc instanceof StatefulProcedure) {
	                		proc = ((StatefulProcedure) proc).getDelegate();
	                	}
	                	StringBuilder buf = null;
	                	if (proc instanceof AbstractJavaProcedure) {
	                		Process process = ((AbstractJavaProcedure) proc).getProcess();
	                		if (process != null && process.getDtAgentConfig() != null) {
	                			if (LOGGER.isDebugEnabled()) LOGGER.debug("Procedure is Java, prepending logs from agent config: " + process.getDtAgentConfig());
		                		try {
			                		File agentBootstrapLog = process.getDtAgentConfig().getBootstrapLog(Technology.JAVA);
			                		if (LOGGER.isDebugEnabled()) LOGGER.debug("Prepending agent bootstrap log: " + agentBootstrapLog);
			                		if (agentBootstrapLog != null) {
			                			buf = readLogFileTail(agentBootstrapLog, buf);
			                		}
			                		File agentLog = process.getDtAgentConfig().getLog(Technology.JAVA);
			                		if (LOGGER.isDebugEnabled()) LOGGER.debug("Prepending agent log: " + agentLog);
			                		if (agentLog != null) {
			                			buf = readLogFileTail(agentLog, buf);
			                		}
		                		} catch (ConfigurationException e) {
		                			LOGGER.warn("(log) Exception getting agent logs", e);
		                		}
		                		// return concatenated logs (bootstrap, agent, application)
	                			return readLogFileTail(logFile, buf).toString();
	                		}
                			LOGGER.warn("Cannot prepend agent logs, process or agent config null, process=" + process);
	                	}

	                	return readLogFileTail(logFile, buf).toString();
	                } catch (IOException ioe) {
	                    LOGGER.warn("(log) Exception reading logfile", ioe);
	                    return "Exception reading logfile: " + ioe;
	                }
	            } else {
	            	return "Could not read remote logfile: " + logFile;
	            }
            }
            return "Procedure does not provide log";
//        }
    }

	/**
	 * Return the details of a procedure.
	 *
	 * @param uuid
	 * @return
	 * @author cwat-dstadler
	 */
	@GET
   @Produces("text/plain")
   @Path(Constants.REST.DETAILS + "/{uuid}")
   public String details(
           @PathParam("uuid") final String uuid) {
       //don't sync here as it would block the gui waiting for any other action that holds the lock to complete
//       synchronized (lock) {
           SingleProcedureBatch batch = batches.get(UUID.fromString(uuid));
           if (batch == null) {
               LOGGER.warn("(log) Could not find procedure for UUID: " + uuid);
               return "UNKNOWN - UUID not known";
           }
           Procedure procedure = batch.getProcedure();
           return procedure.getDetails();
//       }
   }
	
	/**
	 * Return the details of a procedure.
	 *
	 * @param uuid
	 * @return
	 * @author cwat-dstadler
	 */
	@GET
   @Produces("text/plain")
   @Path(Constants.REST.URI + "/{uuid}")
   public String uri(
           @PathParam("uuid") final String uuid) {
       //don't sync here as it would block the gui waiting for any other action that holds the lock to complete
//       synchronized (lock) {
           SingleProcedureBatch batch = batches.get(UUID.fromString(uuid));
           if (batch == null) {
               LOGGER.warn("(log) Could not find procedure for UUID: " + uuid);
               return "UNKNOWN - UUID not known";
           }
           Procedure procedure = batch.getProcedure();
           return procedure.getURI();
//       }
   }

	/**
	 * @param uuid
	 * @return
	 * @author christoph.neumueller
	 */
	@GET
	@Produces("text/plain")
	@Path(Constants.REST.TECHNOLOGY + "/{uuid}")
	public String technology(@PathParam("uuid")
	final String uuid) {
		SingleProcedureBatch batch = batches.get(UUID.fromString(uuid));
		if (batch == null) {
			LOGGER.warn("(technology) Could not find procedure for UUID: " + uuid);
			return "UNKNOWN - UUID not known";
		}
		if (batch.getProcedure() == null) {
			LOGGER.warn("(technology) Procedure not started: " + uuid);
			return "UNKNOWN - Procedure not started";
		}
		Technology tech = batch.getProcedure().getTechnology();
		return tech != null ? tech.toString() : BaseConstants.EMPTY_STRING;
	}

	/**
	 * @param uuid
	 * @return
	 * @author christoph.neumueller
	 */
	@GET
	@Produces("text/plain")
	@Path(Constants.REST.AGENT_FOUND + "/{uuid}")
	public String agentFound(@PathParam("uuid")
	final String uuid) {
		SingleProcedureBatch batch = batches.get(UUID.fromString(uuid));
		if (batch == null) {
			LOGGER.warn("(agentFound) Could not find procedure for UUID: " + uuid);
			return Boolean.FALSE.toString();
		}
		if (batch.getProcedure() == null) {
			LOGGER.warn("(agentFound) Procedure not started: " + uuid);
			return Boolean.FALSE.toString();
		}
		return Boolean.valueOf(batch.getProcedure().agentFound()).toString();
	}

	/**
	 * @param uuid
	 * @return
	 * @author christoph.neumueller
	 */
	@GET
	@Produces("text/plain")
	@Path(Constants.REST.IS_INSTRUMENTATION_SUPPORTED + "/{uuid}")
	public String isInstrumentationSupported(@PathParam("uuid")
	final String uuid) {
		SingleProcedureBatch batch = batches.get(UUID.fromString(uuid));
		if (batch == null) {
			LOGGER.warn("(isInstrumentationSupported) Could not find procedure for UUID: " + uuid);
			return Boolean.FALSE.toString();
		}
		if (batch.getProcedure() == null) {
			LOGGER.warn("(isInstrumentationSupported) Procedure not started: " + uuid);
			return Boolean.FALSE.toString();
		}
		return Boolean.valueOf(batch.getProcedure().isInstrumentationSupported()).toString();
	}


	/**
	 * Method that verifies whether DC-RUM emulation is running
	 * @return response code 200 if DC-RUM emulation is running and 401 if not
	 * @author stefan.moschinski
	 */
	@GET
	@Produces("text/plain")
	@Path(Constants.REST.DCRUM_AVAILABILITY_SERVLET)
	public Response checkDCRumAvailability(
			@QueryParam("cmd")
			String cmd) {
		return new DCRumResponse().getHeader(cmd);
	}

	/**
	 * Method that returns the emulated data of a DCRum appliance
	 * @return DCRum data as text
	 * @author stefan.moschinski
	 */
	@GET
	@Produces("text/plain")
	@Path(Constants.REST.DCRUM_SERVLET)
	public Response getDCRumData(
			@QueryParam("cmd")
			String cmd) {
		return new DCRumResponse().get(cmd);
	}

	/**
	 * Method that returns the emulated data of a DCRum appliance
	 * @return DCRum data as text
	 * @author stefan.moschinski
	 */
	@GET
	@Produces("text/plain")
	@Path(Constants.REST.VERSION)
	public String getVersion() {
		return Version.read().toString();
	}

	/**
	 * Method that returns the emulated data of a DCRum appliance
	 * @return DCRum data as text
	 * @author stefan.moschinski
	 */
	@GET
	@Produces("text/plain")
	@Path(Constants.REST.DOT_NET_ENABLED)
	public String isDotNetEnabled() {
		return Boolean.toString(CentralTechnologyActivator.getIntance().getActivator(Technology.DOTNET_20).isEnabled());
	}

	/**
	 * Is Procedure running on IIS server
	 * @param uuid
	 * @return
	 */
	@GET
	@Produces("text/plain")
	@Path(Constants.REST.IS_RUNNING_ON_IIS + "/{uuid}")
	public String isRunningOnIIS(@PathParam("uuid") final String uuid) {
		SingleProcedureBatch batch = batches.get(UUID.fromString(uuid));
		if (batch == null) {
			LOGGER.warn("(isRunningOnIIS) Could not find procedure for UUID: " + uuid);
			return Boolean.FALSE.toString();
		}
		return Boolean.toString(batch.getProcedure().isDotNetIISProcedure());
	}

	/**
	 * Mostly used for testing!
	 *
	 * @return
	 * @author cwat-dstadler
	 */
	@TestOnly
	public static Map<UUID, SingleProcedureBatch> getBatches() {
		return batches;
	}
}
