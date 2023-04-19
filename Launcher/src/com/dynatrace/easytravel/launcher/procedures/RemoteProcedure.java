package com.dynatrace.easytravel.launcher.procedures;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants.UrlType;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.*;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.remote.RESTProcedureClient;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.LocalUriProvider;

/**
 * Start a procedure on a different instance of launcher via REST commands.
 *
 * @author dominik.stadler
 */
public class RemoteProcedure extends AbstractProcedure {
    private static final Logger LOGGER = LoggerFactory.make();

	public static int WATCHER_THREAD_INTERVALL_MS = 10000; // made public for test purposes
	
	// these two are called quite often and never change their content after initial procedure creation...
	private enum CacheKey {
		InstrumentationSupported,
		AgentFound,
		IsRunningOnIIS
	}
    private Map<CacheKey,Boolean> cachedValues = new HashMap<CacheKey, Boolean>();

	@TestOnly
	public static void setWATCHER_THREAD_INTERVALL_MS(int wATCHER_THREAD_INTERVALL_MS) {
		WATCHER_THREAD_INTERVALL_MS = wATCHER_THREAD_INTERVALL_MS;
	}

	private final List<StopListener> stopListeners = new ArrayList<StopListener>();
    private final RESTProcedureClient client;
    private Thread watcherThread = null;
    private volatile boolean shouldStop; // NOPMD

	public RemoteProcedure(final ProcedureMapping mapping, final String host) throws IllegalArgumentException {
		super(mapping);

		// override the name to indicate that this is started remotely
		String displayHost = host;
		if (displayHost != null) {
			EasyTravelConfig config = EasyTravelConfig.read();
			if (config.shortHostDisplay) {
				int idx = displayHost.indexOf('.');
				if (idx > 0) {
					displayHost = displayHost.substring(0, idx);
				}
			}
		}
		this.name = ProcedureFactory.getNameOfProcedure(mapping) + " (" + displayHost + ")";
		updateNameForCommandProcedure();
		client = new RESTProcedureClient(mapping, host);
	}
	
	private void updateNameForCommandProcedure(){
		String procedureId = this.getMapping().getId();
		
		if(Constants.Procedures.VAGRANT_ID.equals(procedureId)){
			String procedureName = this.getMapping().getSettingValue("procedure_config", "config.vagrantProcedureName");
			if(StringUtils.isNotBlank(procedureName)){
				this.name = name + " ("+procedureName+")";
			}		
		}
	}
	
	@Override
	public Feedback run() {
		if (isRunningOnIIS()) {
			return Feedback.Neutral;
		}
		// check for UNKNOWN state as the remote CmdLauncher could have been restarted and thus
		// not know the current UUID any more
		if (client.getUUID() == null || State.UNKNOWN.equals(client.currentState())) {
			client.reset();
	        client.prepare();

	        // also clean the cache here as previously we were not able to call the remote side
	        cachedValues.clear();
	    }
		State result = client.start();
		if(result == null || result == State.FAILED || result == State.ACCESS_DENIED) {
			// have to let listeners know that we failed to start completely
			notifyStopListeners();
			return Feedback.Failure;
		}

		shouldStop = false;
		watcherThread = new RemoteWatcherThread("Remote procedure watcher - " + getMapping().getId());
		watcherThread.start();

	    return Feedback.Success;
	}

	@Override
	public StopMode getStopMode() {
		if (isRunningOnIIS()) {
			return StopMode.NONE;
		}
		return StopMode.PARALLEL;
	}

    @Override
    public boolean isStoppable() {
        return true;
    }

	@Override
	public Feedback stop() {
		LOGGER.debug("Stopping procedures. Stopping Remote");
		if (isRunningOnIIS()) {
			LOGGER.debug("Stopping procedures. Remote was not running");
			return Feedback.Success;
		}

		if(watcherThread != null && watcherThread.isAlive()) {
			shouldStop = true;

			// ask thread to stop
			watcherThread.interrupt();

			try {
				watcherThread.join();
			} catch (InterruptedException e) {
				LOGGER.warn("Join on RemoteWatcherThread was interrupted", e);
			}
		}

	    if(!client.stop().equals("OK")) {
	    	LOGGER.debug("Stopping procedures. Remote stop failed");
	    	return Feedback.Failure;
	    }

	    // notify any listener that we have now stopped
	    notifyStopListeners();

	    LOGGER.debug("Stopping procedures. Remote stopped");
	    return Feedback.Success;
	}

	@Override
	public boolean isRunning() {
	    switch (client.currentState()) {
			case OPERATING:
			case STARTING:
			case TIMEOUT:
				return true;

			default:
				return false;
		}
	}

	@Override
	public boolean isOperatingCheckSupported() {
		return true;
	}

	@Override
	public boolean isOperating() {
        switch (client.currentState()) { // NOPMD
			case OPERATING:
			case TIMEOUT:
				return true;

			default:
				return false;
		}
	}

	@Override
	public String getDetails() {
		// do a remote call here to retrieve the actual startup parameters...
		String remote = client.getDetails();

		return "Remote procedure on host '" + client.getHost() + "': " + ProcedureFactory.getNameOfProcedure(getMapping()) + "\n" + remote;
	}

	@Override
	public boolean hasLogfile() {
        String id = getMapping().getId();
        if (id.equals(Constants.Procedures.NGINX_WEBSERVER_ID) || id.equals(Constants.Procedures.VAGRANT_ID)) {
            return false;
        }
        return true;
	}

	@Override
	public String getLogfile() {
	    String log = client.getLog();
	    StringBuilder sb = new StringBuilder(log.length() + 150);
	    sb.append("log retrieved remotely from ").append(client.getHost()).append(" on ");
	    sb.append(new Date().toString());
	    sb.append('\n');
	    sb.append(log);

		File logFile;
		if (client.getPort() < 0) {
		    logFile = new File(Directories.getLogDir(), name + ".log");
		} else {
		    logFile = new File(Directories.getLogDir(), name + "_" + client.getPort() + ".log");
		}

		try {
            FileUtils.writeStringToFile(logFile, sb.toString());
            return logFile.getAbsolutePath();
        } catch (IOException ioe) {
            LOGGER.warn("Exception writing log file", ioe);
        }
        return null;
	}

	@Override
	public String getURI() {
        String uri = getURI(false);
        if (uri != null) {
            return uri.toLowerCase();
        }
        return uri;
    }

	@Override
	public String getURIDNS() {
        String uri = getURI(true);
        if (uri != null) {
            return uri.toLowerCase();
        }
        return uri;
    }

	public String getComponentUri(){
		if(Constants.Procedures.VAGRANT_ID.equals(getMapping().getId())){
	    	return client.getURI();
	    }
		return null;
	}
	
	private String getURI(boolean useFQDN) {
	    if (client.getPort() < 0) {
	        return null;
	    }
	    EasyTravelConfig config = EasyTravelConfig.read(); //NOPMD
        if(Constants.Procedures.CUSTOMER_FRONTEND_ID.equals(getMapping().getId())) {
            if (StringUtils.isNotEmpty(config.frontendPublicUrl)) {
                return config.frontendPublicUrl;
            }
            return getProperUri(useFQDN, config.frontendContextRoot);
		} else if(Constants.Procedures.ANGULAR_FRONTEND_ID.equals(getMapping().getId())) {
            if (StringUtils.isNotEmpty(config.angularFrontendPublicUrl)) {
                return config.angularFrontendPublicUrl;
            }
            return getProperUri(useFQDN, config.angularFrontendContextRoot);
		} else if (Constants.Procedures.B2B_FRONTEND_ID.equals(getMapping().getId())) {
		    if (StringUtils.isNotEmpty(config.b2bFrontendPublicUrl)) {
                return config.b2bFrontendPublicUrl;
            }
		    return getProperUri(useFQDN, "/");
		} else if (Constants.Procedures.APACHE_HTTPD_ID.equals(getMapping().getId()) ||
				Constants.Procedures.APACHE_HTTPD_PHP_ID.equals(getMapping().getId())) {
		    if (StringUtils.isNotEmpty(config.apacheFrontendPublicUrl)) {
		        return config.apacheFrontendPublicUrl;
		    }
		    return getProperUri(useFQDN, "/");
		} else if (Constants.Procedures.NGINX_WEBSERVER_ID.equals(getMapping().getId())) {
            if (LocalUriProvider.isNginxFrontendPublicUrl()) {
                return config.nginxFrontendPublicUrl;
            }
            return getProperUri(useFQDN, "/");
        } 

		return null;
	}
	
	private String getProperUri(boolean useFQDN, String contextRoot) {
		if (useFQDN) {
            return LocalUriProvider.getUriDNS(client.getHost(), client.getPort(), contextRoot);
        } else {
            return LocalUriProvider.getUri(client.getHost(), client.getPort(), contextRoot);
        }
	}

	@Override
	public String getURI(UrlType urlType) {
        String uri = getURI(urlType, false);
        if (uri != null) {
            return uri.toLowerCase();
        }
        return uri;
    }

	@Override
	public String getURIDNS(UrlType urlType) {
        String uri = getURI(urlType, true);
        if (uri != null) {
            return uri.toLowerCase();
        }
        return uri;
    }

	private String getURI(UrlType urlType, boolean useFQDN) {
	    if (Constants.Procedures.APACHE_HTTPD_ID.equals(getMapping().getId()) ||
	    		Constants.Procedures.APACHE_HTTPD_PHP_ID.equals(getMapping().getId()) ||
                Constants.Procedures.NGINX_WEBSERVER_ID.equals(getMapping().getId())) {
	        EasyTravelConfig config = EasyTravelConfig.read();  //NOPMD
	        switch(urlType) {
	            case APACHE_JAVA_FRONTEND:
	                if (config.apacheFrontendPublicUrl != null) {
	                    return config.apacheFrontendPublicUrl;
	                } else {
					    if (useFQDN) {
		                	return LocalUriProvider.getUriDNS(client.getHost(), config.apacheWebServerPort, "/");
					    } else {
		                	return LocalUriProvider.getUri(client.getHost(), config.apacheWebServerPort, "/");
					    }
	                }

	            case APACHE_B2B_FRONTEND:
	                if (config.apacheB2BFrontendPublicUrl != null) {
                        return config.apacheB2BFrontendPublicUrl;
                    } else {
					    if (useFQDN) {
	                    	return LocalUriProvider.getUriDNS(client.getHost(), config.apacheWebServerB2bPort, "/");
					    } else {
	                    	return LocalUriProvider.getUri(client.getHost(), config.apacheWebServerB2bPort, "/");
					    }
                    }

                case NGINX_JAVA_FRONTEND:
                    if (LocalUriProvider.isNginxFrontendPublicUrl()) {
                        return config.nginxFrontendPublicUrl;
                    } else {
                        if (useFQDN) {
                            return LocalUriProvider.getUriDNS(client.getHost(), config.nginxWebServerPort, "/");
                        } else {
                            return LocalUriProvider.getUri(client.getHost(), config.nginxWebServerPort, "/");
                        }
                    }

                case NGINX_B2B_FRONTEND:
                    if (LocalUriProvider.isNginxB2BFrontendPublicUrl()) {
                        return config.nginxB2BFrontendPublicUrl;
                    } else {
                        if (useFQDN) {
                            return LocalUriProvider.getUriDNS(client.getHost(), config.nginxWebServerB2bPort, "/");
                        } else {
                            return LocalUriProvider.getUri(client.getHost(), config.nginxWebServerB2bPort, "/");
                        }
                    }

	             // nothing to do for the others...
	            case APACHE_BUSINESS_BACKEND:
	            case APACHE_PROXY:
	        }
	    }
	    
		return null;
	}
	
	@Override
	public void addStopListener(StopListener stopListener) {
        stopListeners.add(stopListener);
	}

	@Override
	public void removeStopListener(StopListener stopListener) {
        stopListeners.remove(stopListener);
	}

	@Override
	public void clearStopListeners() {
        stopListeners.clear();
	}

	private void notifyStopListeners() {
		for(StopListener listener : stopListeners) {
			listener.notifyProcessStopped();
		}
	}

	private class RemoteWatcherThread extends Thread {
		public RemoteWatcherThread(String name) {
			super(name);

			setDaemon(true);
		}

		@Override
		public void run() {
			
			while(!shouldStop) {
				try {
					Thread.sleep(WATCHER_THREAD_INTERVALL_MS);
					com.dynatrace.easytravel.launcher.engine.State currentState = client.currentState();
					if(!currentState.equals(com.dynatrace.easytravel.launcher.engine.State.OPERATING) &&
							!currentState.equals(com.dynatrace.easytravel.launcher.engine.State.STARTING) &&
							!currentState.equals(com.dynatrace.easytravel.launcher.engine.State.TIMEOUT)
							) {
					    LOGGER.warn("Remote Procedure '" + getMapping().getId() + "' is not operating any more, having state: " + currentState + ", notifying stop listeners");
					    notifyStopListeners();

					    // make sure it is stopped in the REST Client
					    client.stop();
					    
					    // stop listening if it stopped
					    break;
					} else {
						LOGGER.debug("Watcher: No problem with client.");
					}
				} catch (InterruptedException e) {
					LOGGER.warn("RemoteWatcherThread was interrupted", e);
				}
			}
		}
	}

    @Override
    public Technology getTechnology() {
		Technology technology = client.getTechnology();

		// if we could not get it remotely, we use a "best guess" to make the "isEnabled()" call in AbstractProcedure work!
		if(technology == null) {
			String id = getMapping().getId();
			if(id.equals(Constants.Procedures.PAYMENT_BACKEND_ID) || id.equals(Constants.Procedures.B2B_FRONTEND_ID)) {
				return Technology.DOTNET_20;
			} else if(id.equals(Constants.Procedures.BUSINESS_BACKEND_ID) || id.equals(Constants.Procedures.CUSTOMER_FRONTEND_ID) ||
					id.equals(Constants.Procedures.ANT_ID) || id.equals(Constants.Procedures.CASSANDRA_ID) ||
					id.equals(Constants.Procedures.PLUGIN_SERVICE)) {
				return Technology.JAVA;
			} else if(id.equals(Constants.Procedures.CREDIT_CARD_AUTH_UNIT_ID)) {
				return Technology.ADK;
			} else if(id.equals(Constants.Procedures.MONGO_DB_ID)) {
				return Technology.MONGODB;
			} else if(id.equals(Constants.Procedures.INPROCESS_MYSQL_ID)) {
				return Technology.MYSQL;
			} else if(id.equals(Constants.Procedures.APACHE_HTTPD_ID)) {
				return Technology.WEBSERVER;
            } else if (id.equals(Constants.Procedures.NGINX_WEBSERVER_ID)) {
                return Technology.NGINX;
			} else if(id.equals(Constants.Procedures.APACHE_HTTPD_PHP_ID)) {
				return Technology.WEBPHPSERVER;
			} else if(id.equals(Constants.Procedures.VAGRANT_ID)){
				return Technology.VAGRANT;
			}
		}

		return technology;
    }

    @Override
    public boolean agentFound() {
		// cache value to avoid repeated costly network-call
		if(cachedValues.containsKey(CacheKey.AgentFound)) {
			return cachedValues.get(CacheKey.AgentFound).booleanValue();
		}

		boolean ret = client.agentFound();
		cachedValues.put(CacheKey.AgentFound, Boolean.valueOf(ret));

		return ret;
	}

	@Override
	public boolean isInstrumentationSupported() {
		// cache value to avoid repeated costly network-call
		if(cachedValues.containsKey(CacheKey.InstrumentationSupported)) {
			return cachedValues.get(CacheKey.InstrumentationSupported).booleanValue();
		}

		boolean ret = client.isInstrumentationSupported();
		cachedValues.put(CacheKey.InstrumentationSupported, Boolean.valueOf(ret));

		return ret;
	}

	public boolean isRunningOnIIS() {
		// cache value to avoid repeated costly network-call
		if(cachedValues.containsKey(CacheKey.IsRunningOnIIS)) {
			return cachedValues.get(CacheKey.IsRunningOnIIS).booleanValue();
		}

		boolean ret = client.isRunningOnIIS();
		cachedValues.put(CacheKey.IsRunningOnIIS, Boolean.valueOf(ret));

		return ret;
	}
}
