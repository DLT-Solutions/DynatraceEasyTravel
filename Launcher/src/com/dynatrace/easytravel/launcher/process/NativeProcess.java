package com.dynatrace.easytravel.launcher.process;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.httpd.ApacheHttpdUtils;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.mysqld.MysqlUtils;
import com.dynatrace.easytravel.util.TextUtils;


public class NativeProcess extends AbstractProcess {

	private static final Logger LOGGER = Logger.getLogger(NativeProcess.class.getName());

	private final File executable;

	private final Technology technology;

	public NativeProcess(File executable, DtAgentConfig dtAgentConfig, Technology technology) throws FileNotFoundException {
		super(dtAgentConfig);
		this.technology = technology;
		if (executable == null) {
			throw new IllegalArgumentException("Executable file must not be null.");
		} else if (!executable.exists()) {
			throw new FileNotFoundException(TextUtils.merge("The executable file ''{0}'' could not be found.",
					executable.getAbsolutePath()));
		}
		this.executable = executable;

		if (LOGGER.isLoggable(Level.INFO)) {
			StringBuilder builder = new StringBuilder();
			String agentPath = null;
			switch (technology) {
				case DOTNET_20:
					builder.append(Constants.Misc.ENV_VAR_DOTNET_20_AGENT_NAME).append(BaseConstants.EQUAL).append(getDtAgentConfig().getAgentName()).append(BaseConstants.COMMA_WS);
					builder.append(Constants.Misc.ENV_VAR_DOTNET_20_RUXIT_AGENT_NAME).append(BaseConstants.EQUAL).append(getDtAgentConfig().getAgentName()).append(BaseConstants.COMMA_WS);
					builder.append(Constants.Misc.ENV_VAR_DOTNET_20_AGENT_ACTIVE).append(BaseConstants.EQUAL).append(Boolean.TRUE.toString()).append(BaseConstants.COMMA_WS);
					builder.append(Constants.Misc.ENV_VAR_DOTNET_20_RUXIT_AGENT_ACTIVE).append(BaseConstants.EQUAL).append(Boolean.TRUE.toString()).append(BaseConstants.COMMA_WS);
					builder.append(Constants.Misc.ENV_VAR_DOTNET_20_SERVER).append(BaseConstants.EQUAL).append(getServer());
					builder.append(Constants.Misc.ENV_VAR_DOTNET_20_RUXIT_CONNECTION_POINT).append(BaseConstants.EQUAL).append(getServer());
					break;
				case WEBSERVER:
				case WEBPHPSERVER:
					if (!OperatingSystem.WINDOWS.equals(OperatingSystem.pickUp())) {
						builder.append(Constants.Misc.ENV_VAR_WEBSERVER_LIBRARY_PATH).append(BaseConstants.EQUAL)
								.append(Directories.getInstallDir().getAbsolutePath()).append("/")
								.append(ApacheHttpdUtils.APACHE_VERSION).append("/Linux/lib").append(":")
								.append(System.getenv(Constants.Misc.ENV_VAR_WEBSERVER_LIBRARY_PATH));
					}

					try {
						agentPath = getDtAgentConfig().getAgentPath(technology);
					} catch (ConfigurationException e) {
						agentPath = "Can not read agent config: " + e.getMessage();
					}
					break;
                case NGINX:
                    if (!OperatingSystem.WINDOWS.equals(OperatingSystem.pickUp())) {
                        try {
                            agentPath = getDtAgentConfig().getAgentPath(technology);
                        } catch (ConfigurationException e) {
                            agentPath = "Can not read agent config: " + e.getMessage();
                        }

                        builder.append(Constants.Misc.ENV_VAR_NGINX_LIBRARY_PATH).
                                append(BaseConstants.EQUAL).
                                append(agentPath)
                                .append(BaseConstants.COMMA_WS);
                    }
                    break;
				// procedures without agent
				case MYSQL:
				case MONGODB:
				case COUCHDB:
				case VAGRANT:
					break;
				default:
					if (agentPath == null) {
						try {
							agentPath = getDtAgentConfig().getAgentPath(technology);
						} catch (ConfigurationException e) {
							agentPath = "Can not read agent config: " + e.getMessage();
						}
					}
					if (agentPath != null) {
						builder.append(Constants.Misc.ENV_VAR_ADK_AGENT_LIB).append(BaseConstants.EQUAL).append(agentPath).append(BaseConstants.COMMA_WS);
						builder.append(Constants.Misc.ENV_VAR_ADK_AGENT_NAME).append(BaseConstants.EQUAL).append(getDtAgentConfig().getAgentName()).append(BaseConstants.COMMA_WS);
						builder.append(Constants.Misc.ENV_VAR_ADK_SERVER).append(BaseConstants.EQUAL).append(getServer());
						builder.append(Constants.Misc.ENV_VAR_ADK_RUXIT_AGENT_LIB).append(BaseConstants.EQUAL).append(agentPath).append(BaseConstants.COMMA_WS);
						builder.append(Constants.Misc.ENV_VAR_ADK_RUXIT_AGENT_NAME).append(BaseConstants.EQUAL).append(getDtAgentConfig().getAgentName()).append(BaseConstants.COMMA_WS);
						builder.append(Constants.Misc.ENV_VAR_ADK_RUXIT_CONNECTION_POINT).append(BaseConstants.EQUAL).append(getServer());
					}
			}

			LOGGER.info("Setting environment variables: " + builder.toString());
		}
	}


	/** {@inheritDoc} */
	@Override
	public CommandLine createCommand() {
		CommandLine commandLine = new CommandLine(executable);

		for (String argument : getApplicationArguments()) {
			commandLine.addArgument(argument, false);
		}

// currently there is a bug trying to run with dt agent arguments, so we are using environment variables now
//        try {
//            commandLine.addArgument(getAgentLibArg());
//            commandLine.addArgument(getSystemProfileArg());
//            commandLine.addArgument(getServerArg());
//
//            for (String furtherOption : getDtAgentConfig().getFurtherOptions()) {
//                commandLine.addArgument(furtherOption);
//            }
//        } catch (ConfigurationException e) {
//            LOGGER.log(Level.WARNING, "Unable to instrument executable.", e);
//        }

		return commandLine;
	}

	@Override
	public Map<String, String> getEnvironment() {
		Map<String, String> environment = new HashMap<String, String>(System.getenv());

		// override with any environment variable provided in the super-class
		Map<String, String> baseEnv = super.getEnvironment();
		if (baseEnv != null)
			environment.putAll(baseEnv);

		DtAgentConfig dtAgentConfig = getDtAgentConfig();

		switch (technology) {
			case DOTNET_20:
				environment.put(Constants.Misc.ENV_VAR_DOTNET_20_AGENT_NAME, dtAgentConfig.getAgentName());
				environment.put(Constants.Misc.ENV_VAR_DOTNET_20_AGENT_ACTIVE, Boolean.TRUE.toString());
				environment.put(Constants.Misc.ENV_VAR_DOTNET_20_SERVER, getServer());
				environment.put(Constants.Misc.ENV_VAR_DOTNET_20_RUXIT_AGENT_NAME,dtAgentConfig.getAgentName());
				environment.put(Constants.Misc.ENV_VAR_DOTNET_20_RUXIT_AGENT_ACTIVE, Boolean.TRUE.toString());
				environment.put(Constants.Misc.ENV_VAR_DOTNET_20_RUXIT_CONNECTION_POINT, getServer());
				break;
			case WEBSERVER:
			case WEBPHPSERVER :
				if (!OperatingSystem.isCurrent(OperatingSystem.WINDOWS)){
					environment.put(Constants.Misc.ENV_VAR_WEBSERVER_LIBRARY_PATH, ApacheHttpdUtils.getLibraryPath());
				}
				break;
			case ADK:
				String agentPath = getAgentPath(dtAgentConfig);
				if (agentPath == null) {
					break;
				}
				environment.put(Constants.Misc.ENV_VAR_ADK_AGENT_LIB, agentPath);
				environment.put(Constants.Misc.ENV_VAR_ADK_AGENT_NAME, dtAgentConfig.getAgentName());
				environment.put(Constants.Misc.ENV_VAR_ADK_SERVER, getServer());
				environment.put(Constants.Misc.ENV_VAR_ADK_RUXIT_AGENT_LIB, agentPath);
				environment.put(Constants.Misc.ENV_VAR_ADK_RUXIT_AGENT_NAME, dtAgentConfig.getAgentName());
				environment.put(Constants.Misc.ENV_VAR_ADK_RUXIT_CONNECTION_POINT, getServer());
				break;
            case NGINX:
                String nginxAgentPath = getAgentPath(dtAgentConfig);
                if (nginxAgentPath == null) {
                    break;
                }
                environment.put(Constants.Misc.ENV_VAR_NGINX_LIBRARY_PATH, nginxAgentPath);
                break;
                // procedures without agent
			case MYSQL:
				if (!OperatingSystem.isCurrent(OperatingSystem.WINDOWS)){
					environment.put(Constants.Misc.ENV_VAR_MYSQL_LIBRARY_PATH, MysqlUtils.getLibraryPath());
				}
				break;
			case COUCHDB:
			case MONGODB:
			case HBASE:
			case VAGRANT:
				break;
			default:
				throw new IllegalArgumentException("Unexpected Technology encountered while creating the environment for a native process: "  + technology);
		}

		// add more agent-related environment variables if defined for this process
		if(dtAgentConfig != null) {
			Map<String, String> envArgs = dtAgentConfig.getEnvironmentArgs();
			for (Map.Entry<String, String> envArg : envArgs.entrySet()) {
				environment.put(envArg.getKey(), envArg.getValue());
			}
		}

		return environment;
	}

	private String getAgentPath(DtAgentConfig dtAgentConfig) {
		if (dtAgentConfig != null) {
			try {
				return dtAgentConfig.getAgentPath(technology);
			} catch (ConfigurationException e) {
				LOGGER.log(Level.WARNING, "Unable to instrument executable '" + executable + "', arguments: '" +
						getApplicationArguments(), e);
			}
		}
		return null;
	}

	private String getServer() {
		DtAgentConfig agentConfig = getDtAgentConfig();

		if (agentConfig.hasServerPort()) {
			return agentConfig.getServer() + BaseConstants.COLON + Integer.toString(agentConfig.getServerPort());
		} else {
			return agentConfig.getServer();
		}
	}

	protected Technology getTechnology() {
		return technology;
	}


	@Override
	protected DefaultExecuteResultHandler getResultHandler(String commandString, FailureListener failureListener) {
		// TODO@(stefan.moschinski): keep this code as long as CCC app returns exit code 1 ...
		return new FailureTolerantResultHandler(commandString, failureListener);
	}


//    currently there is a bug trying to run with dt agent arguments, so we are using environment variables now
//    /**
//     *
//     * @return
//     * @throws ConfigurationException if no valid agent path is configured
//     * @author martin.wurzinger
//     */
//    private String getAgentLibArg() throws ConfigurationException {
//        StringBuilder result = new StringBuilder();
//
//        result.append(Constants.Misc.CMD_PARAM_ADK_AGENT_LIB);
//        result.append(Constants.Misc.CMD_PARAM_ADK_RUXIT_AGENT_LIB);
//        result.append(BaseConstants.EQUAL);
//        result.append(getDtAgentConfig().getAgentPath(Technology.ADK));
//
//        return result.toString();
//    }
//
//    private String getSystemProfileArg() {
//        StringBuilder result = new StringBuilder();
//
//        result.append(Constants.Misc.CMD_PARAM_ADK_AGENT_NAME);
//        result.append(Constants.Misc.CMD_PARAM_ADK_RUXIT_AGENT_NAME);
//        result.append(BaseConstants.EQUAL);
//        result.append(getDtAgentConfig().getSystemProfile());
//
//        return result.toString();
//    }
//
//    private String getServerArg() {
//        DtAgentConfig agentConfig = getDtAgentConfig();
//
//        StringBuilder result = new StringBuilder();
//
//        result.append(Constants.Misc.CMPD_PARAM_ADK_SERVER);
//        result.append(Constants.Misc.CMPD_PARAM_ADK_RUXIT_SERVER);
//        result.append(BaseConstants.EQUAL);
//        result.append(agentConfig.getServer());
//        if (agentConfig.hasServerPort()) {
//            result.append(BaseConstants.COLON);
//            result.append(Integer.toString(agentConfig.getServerPort()));
//        }
//
//        return result.toString();
//    }

}
