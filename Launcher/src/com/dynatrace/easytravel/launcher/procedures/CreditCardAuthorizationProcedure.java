package com.dynatrace.easytravel.launcher.procedures;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Map;

import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.Architecture;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.AbstractNativeProcedure;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.DatabaseEnvArgs;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.LogFileStream;
import com.dynatrace.easytravel.launcher.process.AbstractProcess;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureSetting;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureSetting;
import com.dynatrace.easytravel.logging.BasicLoggerConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Procedure which starts the native application which we use to show ADK features.
 *
 * It can either communication via Socket or Named Pipes, the "IpcMode" setting
 * in the ProcedureMapping defines which one is used, default is NamedPipe.
 *
 * @author dominik.stadler
 */
public class CreditCardAuthorizationProcedure extends AbstractNativeProcedure {
    private static final Logger LOGGER = LoggerFactory.make();

	public static final String SETTING_IPC_MODE = "IpcMode";

    public static enum IpcMode {
    	NamedPipe,
    	Socket;

    	public static IpcMode pickUp() {
    		return SystemUtils.IS_OS_WINDOWS ? NamedPipe : Socket;
    	}
    }

    private static final String SEND_CHANNEL = BaseConstants.PIPE_PREFIX + BaseConstants.CREDITCARD_PIPE_CHANNEL;
    private static final String READ_CHANNEL = BaseConstants.PIPE_PREFIX + BaseConstants.CREDITCARD_PIPE_CHANNEL + "Back";

    private final IpcOperatingChecker operatingChecker;

    private IpcMode ipcMode;

	/**
	 * Returns true, if the current JVM is 64-bit architecture.
	 *
	 * Note: a 32-bit JVM can still run on a 64-bit os, but not vice-versa,
	 * this method will return false in this case as it only looks at the JVM,
	 * not the OS-capabilities.
	 *
	 * @return
	 * @author dominik.stadler
	 */
	private static boolean is64BitJVM() {
		/*
		 * see http://stackoverflow.com/questions/807263/how-do-i-detect-which-kind-of-jre-is-installed-32bit-vs-64bit
		 * Sun VM:
		 * 32bit VM: os.arch=x86
		 * 64bit VM: os.arch=amd64
		 *
		 * GNU VM:
		 * 64bit: os.arch=x86_64
		 */
        return Architecture.pickUp() == Architecture.BIT64;
	}

    public CreditCardAuthorizationProcedure(ProcedureMapping mapping) throws CorruptInstallationException {
    	super(mapping);

		this.ipcMode = getIpcMode(mapping);
		LOGGER.info("Running CreditCardAuthorization application with '" + ipcMode + "' interfaces");

		// we probably auto-detected the ipc mode setting, remember the outcome as setting in the mapping
		this.getMapping().addSetting(new DefaultProcedureSetting(SETTING_IPC_MODE, ipcMode.name()));

		operatingChecker = ipcMode == IpcMode.NamedPipe ? new NamedPipeOperatingChecker() : new SocketOperatingChecker();
    }

	/**
     * Determine the correct name of the executeable depending on
     * 64-bit JVM and if we are running in a 3.5 environment
     */
	@Override
	protected String getExecutable(ProcedureMapping mapping) {
    	final IpcMode localMode = getIpcMode(mapping);
		if (localMode.equals(IpcMode.NamedPipe)) {
			return (is64BitJVM() ? Constants.Modules.CREDITCARD_AUTHORIZATION_64BIT : Constants.Modules.CREDITCARD_AUTHORIZATION);
		} else {
			return (is64BitJVM() ? Constants.Modules.CREDITCARD_AUTHORIZATION_64BIT_S : Constants.Modules.CREDITCARD_AUTHORIZATION_S);
		}
	}

	private static IpcMode getIpcMode(ProcedureMapping mapping) {
    	String ipcModeSetting = mapping.getSettingValue(SETTING_IPC_MODE);

    	// APM-2614: use platform default if not set
    	if(StringUtils.isEmpty(ipcModeSetting)) {
    		IpcMode mode = IpcMode.pickUp();
    		LOGGER.info("Automatically detected IPC-mode: " + mode.name());
    		return mode;
    	}

    	return (IpcMode.Socket.name().equals(ipcModeSetting)) ? IpcMode.Socket : IpcMode.NamedPipe;
	}

    @Override
	protected DtAgentConfig getAgentConfig() {
        final EasyTravelConfig CONFIG = EasyTravelConfig.read();
    	return new DtAgentConfig(CONFIG.creditCardAuthorizationSystemProfile, CONFIG.creditCardAuthorizationAgent, CONFIG.creditCardAuthorizationAgentOptions, CONFIG.creditCardAuthorizationEnvArgs);
    }

    @Override
    public Technology getTechnology() {
        return Technology.ADK;
    }

	@Override
	protected String getWorkingDir() {
		return null;
	}

    @Override
    public boolean isOperatingCheckSupported() {
        return true;
    }

    @Override
    public boolean isOperating() {
        if (!isRunning()) {
            return false;
        }

        return operatingChecker.isOperating();
    }

    @Override
	public Feedback stop() {
    	LOGGER.debug("Stopping procedures. Stopping CreditCardAuthorization");
    	// ask the process to stop gracefully
    	operatingChecker.shutdown();

    	// let process stop gracefully, wait 5 seconds max, with checks each 500ms
    	for(int i = 0;i < 10;i++) {
    		// check if the process is still running
    		if(!isRunning()) {
    			break;
    		}

    		// wait a short while to give the process time to stop
    		try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				LOGGER.warn("Interrupted while sleeping", e);
			}
    	}
    	
    	if (!isRunning()) {
    		LOGGER.debug("Stopping procedures. CreditCardAuthorization stopped");
    	}

    	return super.stop();
	}

	private abstract class IpcOperatingChecker {
		// use an invalid message on purpose to not having to wait 5 seconds for validation during startup
        private static final String TEST_MESSAGE = "IS OPERATING?";

        // special message which causes the process to stop gracefully
    	protected static final String EXIT_MESSAGE = "Exit";

		public IpcOperatingChecker() {
			super();
		}

        /* Done by the C++ app on linux itself, Windows is not a problem
        public IpcOperatingChecker() {
			super();

			// on Linux make sure that the temp files are not there
	        if (!OperatingSystem.isCurrent(OperatingSystem.WINDOWS)) {
	            if(new File(READ_CHANNEL).exists()) {
	            	if(!new File(READ_CHANNEL).delete()) {
	            		LOGGER.warning("Could not clean read-channel for named pipe: " + READ_CHANNEL);
	            	}
	            }
	            if(new File(SEND_CHANNEL).exists()) {
	            	if(!new File(SEND_CHANNEL).delete()) {
	            		LOGGER.warning("Could not clean send-channel for named pipe: " + SEND_CHANNEL);
	            	}
	            }
	        }
		}*/


        public boolean isOperating() {
        	final StringBuilder builder = new StringBuilder();

            try {
                final EasyTravelConfig CONFIG = EasyTravelConfig.read();
            	// give the process time to start up, otherwise we get I/O Exceptions in the thread because the app is not yet started up fully                
                Thread.sleep(2000);
                Thread operatingCheckThread = new Thread( () -> {                		
                		try {
                			String response = sendMessage(TEST_MESSAGE);
                			if(response != null) {
                				builder.append(response);
                			}
                		} catch (IOException e) {
                			LOGGER.warn("Had I/O error when trying to send message to native application: " + e.getMessage());
                		}
                	}
                );
                operatingCheckThread.run();
                operatingCheckThread.join(CONFIG.processOperatingCheckIntervalMs);
                
                if(operatingCheckThread.isAlive()) {
                	LOGGER.warn("Waiting for native application to respond did not finish, trying to clean up by closing streams.");
                	close();

                	// give the thread another chance to stop
                	operatingCheckThread.join(CONFIG.processOperatingCheckIntervalMs);
                }
               
            	return !builder.toString().isEmpty();
            } catch (InterruptedException e) {
				LOGGER.warn("Waiting for sending message to native application was interrupted.", e);
            	return false;
			}
        }

		abstract String sendMessage(String testMessage) throws IOException;
		abstract void close();

		/**
		 * Send a special message to the external process to ask it to shut down.
		 */
		public boolean shutdown() {
			LOGGER.info("Sending Exit-Messasge to native application");
			try {
				String response = sendMessage(EXIT_MESSAGE);

				return response != null;
			} catch (IOException e) {
				LOGGER.warn("Had I/O error when trying to send exit-message to native application: " + e.getMessage());
				return false;
			}
		}
    }

    private final class NamedPipeOperatingChecker extends IpcOperatingChecker {
        private RandomAccessFile writepipe;
        private RandomAccessFile readpipe;

		@Override
		public String sendMessage(String message) throws IOException {
            // Connect to the pipe
            writepipe = new RandomAccessFile(SEND_CHANNEL, "rw");

            try {
                if (OperatingSystem.isCurrent(OperatingSystem.WINDOWS)) {
                    readpipe = writepipe;
                } else {
                    readpipe = new RandomAccessFile(READ_CHANNEL, "r");
                }

                try {
                    // TODO: should we use UTF-16 and convert correctly on the other side?
                    byte[] messageBuffer = message.getBytes(BaseConstants.UTF8);
                    byte[] sendBuffer = new byte[messageBuffer.length];

                    System.arraycopy(messageBuffer, 0, sendBuffer, 0, messageBuffer.length);

                    // write to pipe
                    writepipe.write(sendBuffer);

                    // stop short if this is an exit message
                    if(EXIT_MESSAGE.equals(message)) {
                    	return "";	// just needs to be non-null
                    }

                    // read response
                    //String echoResponse = readLineUnicode(pipe);
                    String echoResponse = readpipe.readLine();
                    LOGGER.info("Expected invalid Response: " + echoResponse);

                    return echoResponse;
                } finally {
                    readpipe.close();
                }
            } finally {
                writepipe.close();
            }
        }

		@Override
		void close() {
			IOUtils.closeQuietly(writepipe);
			IOUtils.closeQuietly(readpipe);
		}


    }

    private final class SocketOperatingChecker extends IpcOperatingChecker {
        private Socket socket;

        @Override
		public String sendMessage(String message) throws IOException {
            final EasyTravelConfig CONFIG = EasyTravelConfig.read();
            final int SOCKET_PORT = CONFIG.creditCardAuthorizationSocketPort;

            // Connect to the socket

        	socket = new Socket("localhost", SOCKET_PORT);
            try {
                    // TODO: should we use UTF-16 and convert correctly on the other side?
                    byte[] messageBuffer = message.getBytes(BaseConstants.UTF8);
                    byte[] sendBuffer = new byte[messageBuffer.length];

                    System.arraycopy(messageBuffer, 0, sendBuffer, 0, messageBuffer.length);

                    // write to socket
                    IOUtils.write(sendBuffer, socket.getOutputStream());

                    // stop short if this is an exit message
                    if(EXIT_MESSAGE.equals(message)) {
                    	return "";	// just needs to be non-null
                    }

                    // read response
                    //String echoResponse = readLineUnicode(pipe);
                    byte[] recBuffer = new byte[512];
                    int count = socket.getInputStream().read(recBuffer);
                    String echoResponse = new String(recBuffer);
                    LOGGER.info("Response(" + count + "): " + echoResponse.replace("\0", "<0>"));

                    return echoResponse;
            } finally {
                socket.close();
            }
        }

		@Override
		void close() {
			IOUtils.closeQuietly(socket);
		}
    }

    public boolean isExecutable() {
        File file = new File(Directories.getInstallDir(), createOsSpecificExecutable(getExecutable(getMapping())));
        return file.exists() && file.canExecute();
    }


    @Override
    public Feedback run() {
        if (!isExecutable()) {
            LOGGER.error("Can't start executable '" + new File(Directories.getInstallDir(), createOsSpecificExecutable(getExecutable(getMapping()))) + "' due to missing execute permissions!");
            return Feedback.Failure;
        }

        // create log streams to try to capture log of the native application
	    AbstractProcess abstractProcess = (AbstractProcess)process;
	    abstractProcess.setTimeout(ExecuteWatchdog.INFINITE_TIMEOUT);
	    String logfile = getLogfile();
		final OutputStream loggingStream = logfile != null ? new LogFileStream(logfile) : null;
	    if (loggingStream != null) {
    	    abstractProcess.setOut(new PrintStream(new TeeOutputStream(System.out, loggingStream)));
    	    abstractProcess.setErr(new PrintStream(new TeeOutputStream(System.err, loggingStream)));
	    }

	    loadDatabaseEnvArgs(abstractProcess);

	    return super.run();
    }

	public void loadDatabaseEnvArgs(AbstractProcess abstractProcess) {
		Map<String,String> actualEnv = abstractProcess.getEnvironment();
		final EasyTravelConfig CONFIG = EasyTravelConfig.read();
		DatabaseEnvArgs env = new DatabaseEnvArgs(CONFIG.databaseUrl);
		if (!env.isEmptyOrNull()) {
			if (actualEnv.get(DatabaseEnvArgs.ET_CCA_DB_NAME) == null) {
				abstractProcess.setEnvironmentVariable(DatabaseEnvArgs.ET_CCA_DB_NAME, env.name);
			}
			if (actualEnv.get(DatabaseEnvArgs.ET_CCA_DB_VENDOR) == null) {
				abstractProcess.setEnvironmentVariable(DatabaseEnvArgs.ET_CCA_DB_VENDOR, env.vendor);
			}
			if (actualEnv.get(DatabaseEnvArgs.ET_CCA_DB_CHANNEL_TYPE) == null) {
				abstractProcess.setEnvironmentVariable(DatabaseEnvArgs.ET_CCA_DB_CHANNEL_TYPE, env.channelType);
			}
			if (actualEnv.get(DatabaseEnvArgs.ET_CCA_DB_CHANNEL_ENDPOINT) == null) {
				abstractProcess.setEnvironmentVariable(DatabaseEnvArgs.ET_CCA_DB_CHANNEL_ENDPOINT, env.channelEndpoint);
			}
		}
	}

	@Override
    public StopMode getStopMode() {
    	return StopMode.PARALLEL;
    }

	@Override
	public String getLogfile() {
		return BasicLoggerConfig.getLogFilePath(BaseConstants.LoggerNames.CREDIT_CARD_AUTHORIZATION);
	}

	@Override
	public boolean hasLogfile() {
	    return true;
	}

    @Override
    protected boolean isTransferable(ProcedureSetting setting) {
    	// we can "transfer" the IpcMode-Setting as we expect it to be equal
        if (SETTING_IPC_MODE.equalsIgnoreCase(setting.getName())) {
            return true;
        }

        return super.isTransferable(setting);
    }

    @Override
    public void transfer(ProcedureMapping mapping, State state) {
    	if (getIpcMode(mapping) != ipcMode) {
    		LOGGER.warn("Need to restart CreditCardAuthorization procedure as the IPC Mode is different in the new Scenario");

    		// ignore failure, we start the app with a new ipc mode anyway
    		stop();

    		ipcMode = getIpcMode(mapping);

    		// log output on failure is done in there
    		run();
    	}
    }

	@Override
	public boolean agentFound() {
        // also report as found if we have a environment variables that activate the Agent
		// this is used for Training Mode, where we set envArgs directly

		// this only should be done for Procedures that support agents
        if (isInstrumentationSupported()) {
        	DtAgentConfig agentConfig = getAgentConfig();
        	Map<String, String> environmentArgs = agentConfig.getEnvironmentArgs();
        	if (environmentArgs.containsKey("DT_AGENTLIBRARY") &&
        			environmentArgs.containsKey("DT_AGENTNAME") &&
        			environmentArgs.containsKey("DT_SERVER") ||
							(environmentArgs.containsKey("RUXIT_AGENTLIBRARY") &&
							environmentArgs.containsKey("RUXIT_AGENTNAME") &&
							environmentArgs.containsKey("RUXIT_CONNECTION_POINT"))) {
        		// we found environment variables which enable the Agent, e.g. in Training Mode
        		return true;
        	}
		}

        // look at normal agent config if not found in javaOpts
		return super.agentFound();
	}
}
