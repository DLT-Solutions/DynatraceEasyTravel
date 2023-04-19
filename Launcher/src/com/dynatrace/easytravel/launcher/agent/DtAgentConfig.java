package com.dynatrace.easytravel.launcher.agent;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.misc.Constants.Misc;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * Class for dynaTrace agent settings.
 *
 * @author martin.wurzinger
 */
public class DtAgentConfig { // NOPMD
    private static final int APM_NG_DEFAULT_AGENT_PORT = 8020;

	private static final Logger LOGGER = Logger.getLogger(DtAgentConfig.class.getName());

    // remember path to only log it once
    private static String lastFoundAgent = null;

	private String dtServer;
	private int dtServerPort;
	private boolean hasDtServerPort;
    private final String agentName;
    private final String dtAgentPath;
    private final List<String> furtherDtArgs;
    private final Map<String, String> envArgs;

	public DtAgentConfig(String dtAgentName, String dtAgentPath, String[] dtFurtherArgs, String[] dtEnvArgs) {
		EasyTravelConfig config = EasyTravelConfig.read();

        this.dtServer = DtVersionDetector.isClassic() ? config.dtServer : config.apmServerHost;
        this.dtServerPort = parseServerPort(DtVersionDetector.isClassic() ? config.dtServerPort : config.apmServerPort);
        this.hasDtServerPort = this.dtServerPort > 0;
        this.agentName = dtAgentName;
        this.dtAgentPath = dtAgentPath;

		dtFurtherArgs = applyCustomServerPropsIfRequired(agentName, BaseConstants.DT_INSTRUMENTATION_SERVER, dtFurtherArgs);
        this.furtherDtArgs = parseFurtherArgs(dtFurtherArgs);

		dtEnvArgs = applyCustomServerPropsIfRequired(agentName, Misc.ENV_VAR_ADK_SERVER, dtEnvArgs);
		dtEnvArgs = applyCustomServerPropsIfRequired(agentName, Misc.ENV_VAR_ADK_RUXIT_CONNECTION_POINT, dtEnvArgs);
        this.envArgs = parseEnvironmentArgs(dtEnvArgs);
    }

	private String[] applyCustomServerPropsIfRequired(String agentName, String serverArgName, String[] args) {
		if (ArrayUtils.isEmpty(args)) {
			return new String[0];
		}

		String toRemove = null;
		for (String arg : args) {
			if (arg != null && arg.startsWith(serverArgName)) {
				if (LOGGER.isLoggable(Level.FINE)) {
					LOGGER.fine(TextUtils.merge("Found custom dynaTrace server settings ''{0}'' for agent ''{1}''", arg,
							agentName));
				}

				List<String> split = extractAddress(arg, serverArgName);
				this.dtServer = split.get(0);
				this.dtServerPort = split.size() == 1 ? 0 : Integer.parseInt(split.get(1));
				this.hasDtServerPort = dtServerPort > 0;
				toRemove = arg;
				break;
			}
		}

		return ArrayUtils.removeElement(args, toRemove);
	}

	private List<String> extractAddress(String dtArg, String serverArgName) {
		String host = dtArg.replaceFirst(serverArgName + BaseConstants.EQUAL,
				BaseConstants.EMPTY_STRING);

		List<String> split = Lists.newArrayList(Splitter.on(BaseConstants.COLON).split(host));
		if (split.size() > 2) {
			throw new IllegalArgumentException(TextUtils.merge(
					"The address of custom dynaTrace server ''{0}'' is not valid",
					host));
		}
		return split;
	}

    /**
     *
     * @param dtServerPort
     * @return the server port or 0 if no server port is configured
     * @author martin.wurzinger
     */
    public static int parseServerPort(String dtServerPort) {
        if (dtServerPort == null || BaseConstants.AUTO.equalsIgnoreCase(dtServerPort)) {
        	// for APM NG use 8020 as default port
        	if(DtVersionDetector.isClassic()) {
        		return 0;
        	} else {
        		return APM_NG_DEFAULT_AGENT_PORT;
        	}
        }

        try {
            return Integer.parseInt(dtServerPort);
        } catch (NumberFormatException nfe) {
            LOGGER.warning("Unable to parse dynaTrace server port '" + dtServerPort + "'. Resume with default settings.");
            return 0;
        }
    }

	protected File getAgentFile(Technology technology) throws ConfigurationException {
        if (BaseConstants.NONE.equalsIgnoreCase(dtAgentPath) || BaseConstants.NONE.equals(agentName)) {
            return null;
        }
		if (DtVersionDetector.isAPM() && BaseConstants.AUTO.equalsIgnoreCase(dtAgentPath)) {
			return null;
		}
        File dtAgent = parseAgentPath(dtAgentPath);

        if (dtAgent == null) {
            dtAgent = detectAgent(technology);

            if (dtAgent == null || !dtAgent.exists()) {
                throw new ConfigurationException("No agent found: " + dtAgent + ", setting: " +
                		dtAgentPath + ", technology: " + technology + ", install-dirs: " + new AgentDetector().detectDtInstallDirs());
            }

            // only log this once
            String agentPath = dtAgent.getAbsolutePath();
            if(!agentPath.equals(lastFoundAgent)) {
            	LOGGER.info("Use automatically detected agent: " + agentPath);
            	lastFoundAgent = agentPath;
            }

        } else if (!dtAgent.exists()) {
            dtAgent = detectAgent(technology);

            if (dtAgent == null || !dtAgent.exists()) {
				throw new ConfigurationException("The configured agent at '" + dtAgentPath +
						"' does not exist and no other agent could be found.");
            }
			LOGGER.warning("The configured agent at '" + dtAgentPath +
					"' does not exist. Resume with automatically detected dynaTrace Agent: " + dtAgent.getAbsolutePath());
        }

        return dtAgent;
    }

	public String getAgentPath(Technology technology) throws ConfigurationException {
		File agent = getAgentFile(technology);
		return agent != null ? agent.getAbsolutePath() : null;
	}

	private File getAgentLogDir(Technology technology) throws ConfigurationException {
		File agent = getAgentFile(technology);
		return agent != null ? new File(agent.getParentFile().getParentFile().getParentFile(), BaseConstants.SubDirectories.LOG)
				: null;
	}

    private static File parseAgentPath(String agentPath) {
        if (agentPath == null || agentPath.isEmpty() || BaseConstants.AUTO.equalsIgnoreCase(agentPath)) {
            return null;
        }

        return new File(agentPath);
    }

    protected File detectAgent(Technology technology) {
    	return new AgentDetector().getAgent(technology);
    }

	public String getServer() {
        return dtServer;
    }

	public int getServerPort() {
        return dtServerPort;
    }

	public String getAgentName() {
        return agentName;
    }

	public boolean hasServerPort() {
        return hasDtServerPort;
    }

	public List<String> getFurtherDtArgs() {
        return furtherDtArgs;
    }

	public Map<String, String> getEnvironmentArgs() {
        return envArgs;
    }

	public void setEnvironmentArgs(String[] value) {
		// Parse into a temporary map, to then use it to write or overwrite values in the target map.
		// This approach allows us to preserve environment settings which are not specified in
		// per-procedure settings, but which are in the configuration.
		Map<String, String> tmpMap = parseEnvironmentArgs(value);
		for (Map.Entry<String, String> entry : tmpMap.entrySet()) {
			this.envArgs.put(entry.getKey(), entry.getValue());
		}
	}

    private static List<String> parseFurtherArgs(String[] furtherOptions) {
        if (furtherOptions == null || furtherOptions.length == 0) {
            return Collections.emptyList();
        }

        return Arrays.asList(furtherOptions);
    }

    public static Map<String, String> parseEnvironmentArgs(String[] environmentArgs) { // NOPMD
        String[] envArgs = (environmentArgs == null) ? new String[0] : environmentArgs;
        Map<String, String> result = new HashMap<String, String>();

        for (String part : envArgs) {
            if (part == null || part.isEmpty()) {
                LOGGER.warning("Bad syntax of configured environment arguments, found empty part.");
                continue;
            }

            int equalsIndex = part.indexOf(BaseConstants.EQUAL);
            if (equalsIndex < 0) {
            	LOGGER.warning("Unable to process configured environment arguments: " + Arrays.toString(envArgs));
            	continue;
            }

            String key = part.substring(0, equalsIndex);
            String value = part.substring(equalsIndex + 1);

            key = key.trim();
            value = value.trim();
            if (key.isEmpty()) {
                LOGGER.warning("Unable to process configured environment arguments: " + Arrays.toString(envArgs));
                continue;
            }

            result.put(key, value);
        }

        return result;
    }

	@Override
	public String toString() {
		return "DtAgentConfig [dtServer=" + dtServer + ", dtServerPort=" + dtServerPort + ", agentName=" + agentName +
				", dtAgentPath=" + dtAgentPath + ", hasDtServerPort=" + hasDtServerPort + ", furtherDtArgs=" + furtherDtArgs +
				", envArgs=" + envArgs + "]";
	}

	/**
	 * Return the agent log that the agent for the given technology is writing to.
	 * Decision is based on latest-timestamp.
	 *
	 * @param technology the technology of the running agent
	 * @return the agent log file for this DtAgentConfig, or null, if not found
	 * @throws ConfigurationException if agent detection fails
	 * @author philipp.grasboeck
	 */
	public File getLog(Technology technology) throws ConfigurationException {
		return getLatestAgentLogFile(getAgentLogDir(technology), getAgentName());
	}

	/**
	 * Return the agent bootstrap log that the agent for the given technology is writing to.
	 * Decision is based on latest-timestamp.
	 *
	 * @param technology the technology of the running agent
	 * @return the agent bootstrap log file for this DtAgentConfig, or null, if not found
	 * @throws ConfigurationException if agent detection fails
	 * @author philipp.grasboeck
	 */
	public File getBootstrapLog(Technology technology) throws ConfigurationException {
		return getLatestAgentLogFile(getAgentLogDir(technology), getAgentName() + "_bootstrap");
	}

	// helper to find the right file based on latest-timestamp.
	private static final File getLatestAgentLogFile(File parent, String agentName) {
		if(parent == null) {
			return null;
		}

		File result = null;
		if (parent.isDirectory()) {
			for (File child : parent.listFiles()) {
				if (child.getName().matches("dt_" + agentName + "_(\\d+).log")) {
					if (result == null || child.lastModified() > result.lastModified()) {
						result = child;
					}
				}
			}
		}

		return result;
	}
}
