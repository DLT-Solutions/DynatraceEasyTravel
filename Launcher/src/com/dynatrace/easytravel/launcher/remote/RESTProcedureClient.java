package com.dynatrace.easytravel.launcher.remote;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.Version;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.ProcedureFactory;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureSetting;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.ConfigurationProvider;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.util.TextUtils;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * A class which encapsulates accessing the REST interfaces of a remote
 * launcher instance. It allows to remotely start/stop procedures and to
 * query the current state.
 *
 * @author dominik.stadler
 */
public class RESTProcedureClient {
    private static final Logger LOGGER = LoggerFactory.make();

    private static final String URL_PREPARE = "http://{0}:{1}/" + Constants.REST.PREPARE + "/{2}";
    private static final String URL_START = "http://{0}:{1}/" + Constants.REST.START + "/{2}";
    private static final String URL_STOP = "http://{0}:{1}/" + Constants.REST.STOP + "/{2}";
    private static final String URL_STATUS = "http://{0}:{1}/" + Constants.REST.STATUS + "/{2}";
    private static final String URL_LOGFILE = "http://{0}:{1}/" + Constants.REST.LOG + "/{2}";
    private static final String URL_DETAILS = "http://{0}:{1}/" + Constants.REST.DETAILS + "/{2}";
    private static final String URL_URI = "http://{0}:{1}/" + Constants.REST.URI + "/{2}";
	private static final String URL_TECHNOLOGY = "http://{0}:{1}/" + Constants.REST.TECHNOLOGY + "/{2}";
	private static final String URL_VERSION = "http://{0}:{1}/" + Constants.REST.VERSION;
	private static final String URL_AGENT_FOUND = "http://{0}:{1}/" + Constants.REST.AGENT_FOUND + "/{2}";
	private static final String URL_IS_INSTRUMENTATION_SUPPORTED = "http://{0}:{1}/" + Constants.REST.IS_INSTRUMENTATION_SUPPORTED + "/{2}";
	private static final String URL_IS_RUNNING_ON_IIS = "http://{0}:{1}/" + Constants.REST.IS_RUNNING_ON_IIS + "/{2}";

	// make it publicly accessible for test purposes
   	public static int MAX_CONNECTION_ERROR_COUNT = 90; // to give 900 seconds = 15 min.

   	private int consecutiveConnectionErrorCount = 0;
   	private State lastState = null;

	// we tried to make these two static to avoid the costly create() call
	// but it seems to have caused problems in large distributed environments
	// although we could not reproduce this via unit-tests
	// however we reverted this for now as the gain is not that big to invest more time
	private final ClientConfig clientConfig = new DefaultClientConfig();
    private final Client client = Client.create(clientConfig);

	private final ProcedureMapping mapping;
	private final String host;

    // Note: uuid is not State!
	private String uuid;
	private int port = -1;

	/**
	 *
	 * @param mapping contains the settings for the procedure that should be started remotely.
	 * @param host The remote host where a Launcher is listening for commands.
	 */
	public RESTProcedureClient(ProcedureMapping mapping, String host) {
		super();
		this.mapping = mapping;
		this.host = host;

	    setClientTimeouts(1800000);
	}

	public RESTProcedureClient() {
		this(null, null);
	}

	/**
	 * Allow to set lower timeout for tests
	 *
	 * @param timeout
	 */
	protected void setClientTimeouts(int timeout) {
		// set high timeout, documentation states that it is "infinite" by default, but we see timeouts after aprox 30s
		client.setConnectTimeout(timeout);
		client.setReadTimeout(timeout);
	}


	/**
	 * Start the procedure on the remote host, returing a UUID that identifies the running procedure.
	 *
	 * @return A UUID which identifies the running procedure or null if an error occurred.
	 * @author dominik.stadler
	 */
	public String prepare() {
		if(host == null || host.isEmpty()) {
			throw new IllegalArgumentException("Trying to prepare a procedure via REST, but host is empty: " + host);
		}
		if(uuid != null) {
			LOGGER.warn("Cannot prepare a procedure more than once.");
			return null;
		}

		final EasyTravelConfig config = EasyTravelConfig.read();
	    WebResource r = client.resource(TextUtils.merge(URL_PREPARE, host, Integer.toString(config.launcherHttpPort), mapping.getId().replace(" ", "%20")));

	    if(LOGGER.isDebugEnabled()) {
	    	LOGGER.debug("Requesting URL: " + r.getURI() + ", query params: " + getQueryParamsForPrepare());
	    }

	    try {
	    	String tmp = r
		    	.queryParams(getQueryParamsForPrepare())
		    	.accept(MediaType.TEXT_PLAIN_TYPE)
		    	.get(String.class);

	    	// parse result, should be in form of <uuid>|<port>
		    int separatorIdx = tmp.indexOf('|');
		    uuid = tmp.substring(0, separatorIdx);
		    String portStr = tmp.substring(separatorIdx + 1);
		    try {
		        port = Integer.parseInt(portStr);
		    } catch (NumberFormatException nfe) {
		        LOGGER.warn("port information faulty: " + portStr, nfe);
		        port = -1;
		    }
		    LOGGER.info("Result from remotely preparing '" + mapping.getId() + "': " + uuid);

		    return uuid;
	    } catch (ClientHandlerException e) {
	    	LOGGER.error("Exception while trying to execute remote command, cannot prepare remote procedure: " + r.getURI(), e);
	    	return null;
	    } catch (UniformInterfaceException e) {
	    	LOGGER.error("Exception while trying to execute remote command, cannot prepare remote procedure\n" +
	    			"Headers: " + e.getResponse().getHeaders() + "\n" +
	    			"URL:" + r.getURI(), e);
	    	return null;
	    }
    }

	/**
     * Start the procedure on the remote host, returning a UUID that identifies the running procedure.
     *
     * @return A UUID which identifies the running procedure or null if an error occurred.
     * @author dominik.stadler
     */
    public State start() {

   		consecutiveConnectionErrorCount = 0;

        if(uuid == null) {
            LOGGER.warn("Cannot start procedure that has not been prepared.");
            lastState = null;
            return lastState;
        }
		final EasyTravelConfig config = EasyTravelConfig.read();
		
        WebResource r = client.resource(TextUtils.merge(URL_START, host, Integer.toString(config.launcherHttpPort), uuid));

        LOGGER.debug("Requesting URL: " + r.getURI());

        try {
        	
            String result = r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
            
            LOGGER.info("Result from remotely starting: " + result);

            if(result.startsWith("UNKNOWN")) {
            	lastState = State.UNKNOWN;
            	return lastState;
            }

            lastState = State.valueOf(result);
            return lastState;

        } catch (ClientHandlerException e) {
            LOGGER.error("Exception while trying to start remote command, cannot start remote procedure: " + r.getURI(), e);
            lastState = State.FAILED;
            return lastState;
        } catch (UniformInterfaceException e) {
            LOGGER.error("Exception while trying to start remote command, cannot start remote procedure: " + r.getURI(), e);
            lastState = State.FAILED;
            return lastState;
        }
    }

	/**
	 * Stop the procedure that was started before on the remote host.
	 *
	 * @return The string "OK" if stopping was successfull, the string "NOTOK - ...." if some
	 * 		problem occurred.
	 * @author dominik.stadler
	 */
    public String stop() {
		if(uuid == null) {
			LOGGER.warn("Cannot stop procedure that has not been prepared.");
			return "NOTOK - Cannot stop procedure that hsa not been prepared.";
		}

		final EasyTravelConfig config = EasyTravelConfig.read();
	    WebResource r = client.resource(TextUtils.merge(URL_STOP, host, Integer.toString(config.launcherHttpPort), uuid));

	    LOGGER.debug("Requesting URL: " + r.getURI());

	    try {
		    String result = r
		    	.accept(MediaType.TEXT_PLAIN_TYPE)
		    	.get(String.class);
		    LOGGER.info("Result from remotely stopping '" + mapping.getId() + "' (" + uuid + "): " + result);

		    // do not unset uuid here as we keep it across start/stop cycles!

		    return result;
	    } catch (ClientHandlerException e) {
	    	LOGGER.error("Exception while trying to start remote command, cannot stop remote procedure: " + r.getURI(), e);
	    	return "NOTOK - Failed to connect remote host with URL: " + r.getURI();
	    } catch (UniformInterfaceException e) {
	    	LOGGER.error("Exception while trying to start remote command, cannot stop remote procedure: " + r.getURI(), e);
	    	return "NOTOK - Failed to connect remote host with URL: " + r.getURI();
	    }
    }

    /**
     * Query the current state
     *
     * @return One of the State values, UNKNOWN if the procedure was not started or was stopped.
     *
     * @author dominik.stadler
     */
	public State currentState() {

		if(uuid == null) {
			LOGGER.warn("Cannot query state of procedure that is not started.");
			consecutiveConnectionErrorCount = 0;
			lastState = State.UNKNOWN;
			return lastState;
		}

		final EasyTravelConfig config = EasyTravelConfig.read();
		WebResource r = client.resource(TextUtils.merge(URL_STATUS, host, Integer.toString(config.launcherHttpPort), uuid));

	    LOGGER.debug("Requesting URL: " + r.getURI());

	    try {
		    String result = r
		    	.accept(MediaType.TEXT_PLAIN_TYPE)
		    	.get(String.class);
		    LOGGER.debug("Result from querying state of '" + mapping.getId() + "' (" + uuid + "): " + result);

		    // special handling, we also transmit some error information in this case


		    if(result.startsWith("UNKNOWN")) {
		    	LOGGER.warn("Client state unknown");
		    	consecutiveConnectionErrorCount = 0;
		    	lastState = State.UNKNOWN;
		    	return lastState;
		    }

	    	LOGGER.debug("Client state result <" + result + ">");
	    	consecutiveConnectionErrorCount = 0;
	    	lastState = State.valueOf(result);
		    return lastState;
	    } catch (ClientHandlerException e) {

	    	LOGGER.error("Exception while trying to start remote command, cannot get state of remote procedure: " + r.getURI(), e);
            if (lastState != null  && consecutiveConnectionErrorCount <= MAX_CONNECTION_ERROR_COUNT) {
            	// Ignore the error for now - it might be an intermittent  problem
        		consecutiveConnectionErrorCount++;
                return lastState;
            } else {
	    		LOGGER.warn("Max consecutive connection error count exceeded: returing FAILED");
        		consecutiveConnectionErrorCount = 0;
        		lastState = State.FAILED;
	    		return lastState;
            }

	    } catch (UniformInterfaceException e) {

	    	LOGGER.error("Exception while trying to start remote command, cannot get state of remote procedure: " + r.getURI(), e);
            if (lastState != null  && consecutiveConnectionErrorCount <= MAX_CONNECTION_ERROR_COUNT) {
            	// Ignore the error for now - it might be an intermittent  problem
        		consecutiveConnectionErrorCount++;
                return lastState;
            } else {
	    		LOGGER.warn("Max consecutive connection error count exceeded: returing FAILED");
        		consecutiveConnectionErrorCount = 0;
        		lastState = State.FAILED;
	    		return lastState;
            }

	    }
	}

	/**
	 * Create a map which contains all query parameters, i.e. properties and settings
	 *
	 * @return
	 * @author dominik.stadler
	 */
	private MultivaluedMap<String, String> getQueryParamsForPrepare() {
		MultivaluedMap<String, String> queryMap = new MultivaluedMapImpl();

	    // add Settings
	    for(ProcedureSetting setting : mapping.getSettings()) {
	    	queryMap.add("setting", setting.toREST());
	    }

	    // add Properties
	    try {
	    	File file = EasyTravelConfig.read().storeInTempFile();
	    	try {
				Properties properties = ConfigurationProvider.readPropertyFile(file.getAbsolutePath());
				for(String property : properties.stringPropertyNames()) {
					queryMap.add("property", property + Constants.REST.PROPERTY_DELIMITER + properties.getProperty(property));
				}
				// let every remote procedure know on what host it is running
				// the master launcher determines that - not the remote launchers
				queryMap.add("property", "config.officialHost" + Constants.REST.PROPERTY_DELIMITER + host);
	    	} finally {
	    		if(!file.delete()) {
	    			LOGGER.warn("Could not remove temporary file: " + file);
	    		}
	    	}
		} catch (IOException e) {
			LOGGER.error("Could not read property file '" + EasyTravelConfig.PROPERTIES_FILE + "'", e);
		}

	    // additionally add some environment variables
	    for(String mappingId : Constants.Procedures.ALL_REMOTE) {
	    	if(ProcedureFactory.isRemote(mappingId)) {
	        	String propertyName = ProcedureFactory.getSystemProperty(mappingId);
	        	String property = System.getProperty(propertyName);
	        	if(property != null) {
	        		queryMap.add("environment", propertyName + Constants.REST.PROPERTY_DELIMITER + property);
	        	}
	    	}
	    }

		// allow to specify other hosts via special system property
		String property = System.getProperty("com.dynatrace.easytravel.host.additional");
		if (property != null) {
        	queryMap.add("environment", "com.dynatrace.easytravel.host.additional" + Constants.REST.PROPERTY_DELIMITER + property);
		}

        // also send the type of installation detected on the master to the slave to not have the slave detect a different installation mode
        queryMap.add("mode", DtVersionDetector.getInstallationType().name());

		return queryMap;
	}

	public String getHost() {
        if (host != null) {
            return host.toLowerCase();
        }
        return host;
    }


	public ProcedureMapping getMapping() {
		return mapping;
	}


	/**
     * Retrieve the current log output.
     *
     * @return The current log as string.
     *
     * @author peter.kaiser
     */
    public String getLog() {
        if(uuid == null) {
            LOGGER.warn("Cannot retrieve log of procedure that is not started.");
            return BaseConstants.EMPTY_STRING;
        }

		final EasyTravelConfig config = EasyTravelConfig.read();
        WebResource r = client.resource(TextUtils.merge(URL_LOGFILE, host, Integer.toString(config.launcherHttpPort), uuid));

        LOGGER.debug("Requesting URL: " + r.getURI());

        try {
            //LOGGER.fine("Result from querying state of '" + mapping.getId() + "' (" + uuid + "): " + result);
            return r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
        } catch (ClientHandlerException e) {
            LOGGER.error("Exception while trying to start remote command, cannot get log from remote procedure: " + r.getURI(), e);
            return BaseConstants.EMPTY_STRING;
        } catch (UniformInterfaceException e) {
            LOGGER.error("Exception while trying to start remote command, cannot get log from remote procedure: " + r.getURI(), e);
            return BaseConstants.EMPTY_STRING;
        }
    }


	public String getDetails() {
        if(uuid == null) {
            LOGGER.warn("Cannot retrieve details of procedure that is not started.");
            return BaseConstants.EMPTY_STRING;
        }

		final EasyTravelConfig config = EasyTravelConfig.read();
        WebResource r = client.resource(TextUtils.merge(URL_DETAILS, host, Integer.toString(config.launcherHttpPort), uuid));

        LOGGER.debug("Requesting URL: " + r.getURI());

        try {
            //LOGGER.fine("Result from querying state of '" + mapping.getId() + "' (" + uuid + "): " + result);
            return r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
        } catch (ClientHandlerException e) {
            LOGGER.error("Exception while trying to start remote command, cannot get details from remote procedure: " + r.getURI(), e);
            return BaseConstants.EMPTY_STRING;
        } catch (UniformInterfaceException e) {
            LOGGER.error("Exception while trying to start remote command, cannot get details from remote procedure: " + r.getURI(), e);
            return BaseConstants.EMPTY_STRING;
        }
	}
	
	public String getURI() {

		final EasyTravelConfig config = EasyTravelConfig.read();
        WebResource r = client.resource(TextUtils.merge(URL_URI, host, Integer.toString(config.launcherHttpPort), uuid));

        LOGGER.debug("Requesting URL: " + r.getURI());

        try {
            //LOGGER.fine("Result from querying state of '" + mapping.getId() + "' (" + uuid + "): " + result);
            return r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
        } catch (ClientHandlerException e) {
            LOGGER.error("Exception while trying to start remote command, cannot get uri from remote procedure: " + r.getURI(), e);
            return BaseConstants.EMPTY_STRING;
        } catch (UniformInterfaceException e) {
            LOGGER.error("Exception while trying to start remote command, cannot get uri from remote procedure: " + r.getURI(), e);
            return BaseConstants.EMPTY_STRING;
        }
	}

    public String getUUID() {
        return uuid;
    }

    public int getPort() {
        return port;
    }

	public Technology getTechnology() {
		if (uuid == null) {
			if (LOGGER.isDebugEnabled()) LOGGER.debug("Cannot retrieve technology of procedure that is not started.");
			return null;
		}

		final EasyTravelConfig config = EasyTravelConfig.read();
		WebResource r = client.resource(TextUtils.merge(URL_TECHNOLOGY, host, Integer.toString(config.launcherHttpPort), uuid));

		LOGGER.debug("Requesting URL: " + r.getURI());

		try {
			String returnValue = r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
			if(returnValue == null || returnValue.isEmpty()) {
				return null;
			}

			if(returnValue.startsWith("UNKNOWN")) {
				LOGGER.warn("Technology for procedure unknown at remote host: " + returnValue);
				return null;
			}

			return Technology.valueOf(returnValue);
		} catch (ClientHandlerException e) {
			LOGGER.error("Exception while trying to start remote command, cannot get technology from remote procedure: " + r.getURI(), e);
			return null;
		} catch (UniformInterfaceException e) {
			LOGGER.error("Exception while trying to start remote command, cannot get technology from remote procedure: " + r.getURI(), e);
			return null;
		}
	}

	public Version getLauncherVersion(String host) {
		final EasyTravelConfig config = EasyTravelConfig.read();
		LOGGER.info("getLauncherVersion() for host: " + host + " - begin");
		WebResource r = client.resource(TextUtils.merge(URL_VERSION, host, Integer.toString(config.launcherHttpPort)));
		LOGGER.info("getLauncherVersion() for host: " + host + ", URI: " + r.getURI() + " - end");

		LOGGER.debug("Requesting URL: " + r.getURI());

		try {
			String remoteLauncherVersion = r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
			return Version.read(remoteLauncherVersion);
		} catch (ClientHandlerException e) {
			LOGGER.error("Exception while trying to retrieve version from remote Launcher: " + r.getURI(), e);
			return null;
		} catch (UniformInterfaceException e) {
			LOGGER.error("Exception while trying to retrieve version from remote Launcher: " + r.getURI(), e);
			return null;
		}
	}


	public boolean agentFound() {
		if (uuid == null) {
			LOGGER.warn("Cannot retrieve agentFound of procedure that is not started.");
			return false;
		}

		final EasyTravelConfig config = EasyTravelConfig.read();
		WebResource r = client.resource(TextUtils.merge(URL_AGENT_FOUND, host, Integer.toString(config.launcherHttpPort), uuid));

		LOGGER.debug("Requesting URL: " + r.getURI());

		try {
			return Boolean.valueOf(r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class));
		} catch (ClientHandlerException e) {
			LOGGER.error("Exception while trying to start remote command, cannot get agentFound from remote procedure: " + r.getURI(), e);
			return false;
		} catch (UniformInterfaceException e) {
			LOGGER.error("Exception while trying to start remote command, cannot get agentFound from remote procedure: " + r.getURI(), e);
			return false;
		}
	}

	public boolean isInstrumentationSupported() {
		if (uuid == null) {
			LOGGER.info("Cannot retrieve isInstrumentationSupported of procedure that is not started.");
			return false;
		}

		final EasyTravelConfig config = EasyTravelConfig.read();
		WebResource r = client.resource(TextUtils.merge(URL_IS_INSTRUMENTATION_SUPPORTED, host, Integer.toString(config.launcherHttpPort), uuid));

		LOGGER.debug("Requesting URL: " + r.getURI());

		try {
			return Boolean.valueOf(r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class));
		} catch (ClientHandlerException e) {
			LOGGER.error("Exception while trying to start remote command, cannot get isInstrumentationSupported from remote procedure: " + r.getURI(), e);
			return false;
		} catch (UniformInterfaceException e) {
			LOGGER.error("Exception while trying to start remote command, cannot get isInstrumentationSupported from remote procedure: " + r.getURI(), e);
			return false;
		}
	}

	/**
	 * Check if remote procedure is running on IIS Server
	 * @return
	 */
	public boolean isRunningOnIIS() {
		if (uuid == null) {
			LOGGER.info("Cannot retrieve isRunningOnIIS of procedure that is not started.");
			return false;
		}

		final EasyTravelConfig config = EasyTravelConfig.read();
		WebResource r = client.resource(TextUtils.merge(URL_IS_RUNNING_ON_IIS, host, Integer.toString(config.launcherHttpPort), uuid));

		try {
			return Boolean.valueOf(r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class));
		} catch (ClientHandlerException e) {
			LOGGER.error("Exception while trying to start remote command, cannot get agentFound from remote procedure: " + r.getURI(), e);
			return false;
		} catch (UniformInterfaceException e) {
			LOGGER.error("Exception while trying to start remote command, cannot get agentFound from remote procedure: " + r.getURI(), e);
			return false;
		}

	}

	/**
	 * Reset values so that we can start a fresh prepare/start/stop state transition.
	 *
	 * @author cwat-dstadler
	 */
	public void reset() {
		uuid = null;
		port = -1;
	}
}
