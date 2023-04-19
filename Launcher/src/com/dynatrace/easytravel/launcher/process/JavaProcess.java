package com.dynatrace.easytravel.launcher.process;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.ipc.SocketUtils;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.util.TextUtils;


public class JavaProcess extends AbstractProcess {

    private final static Logger LOGGER = Logger.getLogger(JavaProcess.class.getName());

    private final File moduleJar;
    private String mainClass = null;
    private String[] javaArguments;

    /**
     * Create a new asynchronous java process instance.
     *
     * @param moduleJar the existing JAR file of the module to start
     * @param timeout the timeout of the process in milliseconds or 0 in order to set no timeout
     * @param arguments the arguments for the <code>java -jar</code> command to start the JAR
     * @throws FileNotFoundException if the module JAR file cannot be found
     * @author martin.wurzinger
     */
    public JavaProcess(File moduleJar) throws FileNotFoundException {
        this(moduleJar, null);
    }

    /**
     * Create a new asynchronous java process instance.
     *
     * @param moduleJar the existing JAR file of the module to start
     * @param arguments the arguments for the <code>java -jar</code> command to start the JAR
     * @throws FileNotFoundException if the module JAR file cannot be found
     * @author martin.wurzinger
     */
    public JavaProcess(File moduleJar, DtAgentConfig dtAgentConfig) throws FileNotFoundException {
        super(dtAgentConfig);

        if (moduleJar == null) {
            throw new IllegalArgumentException("JAR module argument must not be null.");
        } else if (!moduleJar.exists()) {
            throw new FileNotFoundException(TextUtils.merge("The JAR file ''{0}'' could not be found.", moduleJar.getAbsolutePath()));
        }

        this.moduleJar = moduleJar;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public void setJavaArguments(String... javaArguments) {
        this.javaArguments = javaArguments;
    }

    @Override
    public CommandLine createCommand() {
        // get the java executable
        String java = findJavaExecutable();

        // now build the commandline out of the pieces created above
        CommandLine commandLine = CommandLine.parse(java);

        // add dtagent string if configured
        DtAgentConfig agentConfig = getDtAgentConfig();
        if (agentConfig != null) {
            try {
                String createAgentString = createAgentString(agentConfig);
                if(createAgentString != null && createAgentString.length() > 0) {
                	commandLine.addArgument(createAgentString, false);
                }
			} catch (ConfigurationException e) {
				if(LOGGER.isLoggable(Level.FINE)){
					LOGGER.log(Level.FINE, "Unable to instrument java application.", e);
				}
                LOGGER.log(Level.WARNING, "Unable to instrument java application: " + e.getMessage());
            }
        }

        if (javaArguments != null && javaArguments.length > 0) {
            for (String arg : javaArguments) {
                if (arg.contains("\"")) {
                    arg = arg.replace("\"", "");
                }
                commandLine.addArgument(arg,false);
            }
        }

        // if JaCoCo coverage is defined, then set the necessary agent to also
        // instrument the sub-processes that we start here
        String coverage = System.getenv("COVERAGE");
		if(coverage != null) {
			try {
				// reserve a port in an arbitrary range to get a unique id
				int port = SocketUtils.reserveNextFreePort(8300, 8400, null);
				coverage = coverage.replace("${id}", moduleJar.getName() + "." + Integer.toString(port));

	    		commandLine.addArgument(coverage, false);
	    		LOGGER.warning("Using JaCoCo code-coverage agent for started Java process with java agent-settings: " + coverage);
			} catch (IOException e) {
	        	LOGGER.log(Level.WARNING, "Could not reserve a port in the range of 8300-8400 for coverage", e);
			}
        }

        if (mainClass == null) {
            commandLine.addArgument(Constants.Misc.JAR_OPTION);
            commandLine.addArgument(moduleJar.getAbsolutePath(), false);
        } else {
            commandLine.addArgument(Constants.Misc.JAR_CLASSPATH);
            commandLine.addArgument(moduleJar.getAbsolutePath(), false);
            commandLine.addArgument(mainClass);
        }

        for (String argument : getApplicationArguments()) {
            commandLine.addArgument(argument, false);
        }

        return commandLine;
    }

	/**
     * Look up the java executable and the path to it by the <code>java.home</code> property.
     *
     * @return the path to the java executable
     */
    private String findJavaExecutable() {
        String javaHomeProperty = System.getProperty(BaseConstants.SystemProperties.JAVA_HOME);
        if (javaHomeProperty == null) {
            String message = "Could not read system propert for java executable ''{0}''. Using ''{1}'' as default executable.";
            LOGGER.warning(TextUtils.merge(message, BaseConstants.SystemProperties.JAVA_HOME, Constants.Misc.JAVA_COMMAND));
            return Constants.Misc.JAVA_COMMAND;
        }

        StringBuilder result = new StringBuilder();

        result.append(BaseConstants.DQUOTE);
        result.append(javaHomeProperty);
        result.append(File.separator);
        result.append(Constants.Misc.JAVA_BIN);
        result.append(File.separator);
        result.append(Constants.Misc.JAVA_COMMAND);
        result.append(BaseConstants.DQUOTE);

        return result.toString();
    }

	@Override
	public Map<String, String> getEnvironment() {
		Map<String, String> environment = new HashMap<String, String>(System.getenv());

		// override with any environment variable provided in the super-class
		Map<String, String> baseEnv = super.getEnvironment();
		if (baseEnv != null)
			environment.putAll(baseEnv);

		DtAgentConfig dtAgentConfig = getDtAgentConfig();

		// add more agent-related environment variables if defined for this process
		if(dtAgentConfig != null) {
			Map<String, String> envArgs = dtAgentConfig.getEnvironmentArgs();
			for (Map.Entry<String, String> envArg : envArgs.entrySet()) {
				environment.put(envArg.getKey(), envArg.getValue());
			}
		}

		return environment;
	}

    /**
     *
     * @return
     * @throws ConfigurationException if agent cannot be found
     * @author martin.wurzinger
     */
    public static String createAgentString(DtAgentConfig agentConfig) throws ConfigurationException {
        String agentPath = agentConfig.getAgentPath(Technology.JAVA);
        if (agentPath == null) {
            return BaseConstants.EMPTY_STRING;
        }
        StringBuilder agentStringBuilder = new StringBuilder();
        agentStringBuilder.append(Constants.Misc.JAVA_AGENT_PATH);
        agentStringBuilder.append(BaseConstants.COLON);
        agentStringBuilder.append(agentPath);
        agentStringBuilder.append(BaseConstants.EQUAL);
        agentStringBuilder.append(BaseConstants.DT_INSTRUMENTATION_SYSTEM_PROFILE);
        agentStringBuilder.append(BaseConstants.EQUAL);
        agentStringBuilder.append(agentConfig.getAgentName());
        agentStringBuilder.append(BaseConstants.COMMA);
        agentStringBuilder.append(BaseConstants.DT_INSTRUMENTATION_SERVER);
        agentStringBuilder.append(BaseConstants.EQUAL);
        agentStringBuilder.append(agentConfig.getServer());

        if (agentConfig.hasServerPort()) {
            agentStringBuilder.append(BaseConstants.COLON);
            agentStringBuilder.append(Integer.toString(agentConfig.getServerPort()));
        }

        for (String furtherOption : agentConfig.getFurtherDtArgs()) {
            agentStringBuilder.append(BaseConstants.COMMA);
            agentStringBuilder.append(furtherOption);
        }

        return agentStringBuilder.toString();
    }

	@Override
	public String getDetails() {
		// can be null for processes which do not use an Agent
		if(getDtAgentConfig() == null) {
			return super.getDetails();
		}

		String agent = "";
		try {
			// we just want to note here if the agent cannot be found
			// the actual location of the agent is already part of the java process anyway
			createAgentString(getDtAgentConfig());
		} catch (ConfigurationException e) {
			agent = "Could not find agent: " + e.getMessage() + "\n";
		}

		return agent + super.getDetails();
	}

	@Override
	protected DefaultExecuteResultHandler getResultHandler(String commandString, FailureListener failureListener) {
		if (Constants.Modules.CASSANDRA.equals(moduleJar.getName())) {
			// cassandra exists with value 1
			return new FailureTolerantResultHandler(commandString, failureListener);
		}
		return super.getResultHandler(commandString, failureListener);
	}
}
