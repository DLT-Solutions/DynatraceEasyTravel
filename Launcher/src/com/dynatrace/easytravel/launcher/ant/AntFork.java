package com.dynatrace.easytravel.launcher.ant;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.*;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.AbstractLauncher;
import com.dynatrace.easytravel.launcher.engine.AbstractStopListener;
import com.dynatrace.easytravel.launcher.process.JavaProcess;
import com.dynatrace.easytravel.launcher.remote.ContinuouslyRequestHandler;
import com.dynatrace.easytravel.logging.RootLogger;
import com.dynatrace.easytravel.util.TextUtils;


/**
 * Run multiple ant threads in separate process.
 *
 * This is the main application that is started by the {@link OutOfProcessAntController},
 * so this code is running in a separate process to actually run the Ant build.
 *
 * Internally it re-uses the InPRocessAntController for execution.
 *
 * @author martin.wurzinger
 */
public class AntFork extends AbstractLauncher {

    private static final Logger LOGGER = Logger.getLogger(AntFork.class.getName());

    /** Exit value to indicate a NORMAL process termination. */
    public static final int EXIT_STATUS_DEFAULT = 0;

    /** Exit value to indicate a general ABNORMAL process termination. */
    public static final int EXIT_STATUS_ABNORMAL = 1;

    /** Exit value to indicate that the process wants to be restarted. */
    public static final int EXIT_STATUS_RESTART = 2;

    private final AtomicInteger exitStatus = new AtomicInteger(EXIT_STATUS_DEFAULT);

    /**
     *
     * @param args
     * @author martin.wurzinger
     */
    public static void main(String[] args) {
        RootLogger.setup(BaseConstants.LoggerNames.ANT);

        InProcessAntController controller;
        try {
            controller = read(args);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to parse arguments for Ant process fork", e);
            return;
        }

        new AntFork().run(controller);
    }

    public void run(final InProcessAntController controller) {
        setRunning(true);

        try {
            ContinuouslyRequestHandler.setAntController(controller);
            startHttpService(EasyTravelConfig.read().antForkHttpService);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to run Ant fork because the required HTTP Service could not be started", e);
            return;
        }

        controller.addStopListener(new AbstractStopListener() {

            @Override
            public void notifyProcessStopped() {
                if (controller.isRestartRequired()) {
                    setExitStatus(EXIT_STATUS_RESTART);
                }

                exit();
            }
        });

        controller.start();
		EasyTravelConfig config = EasyTravelConfig.read();

        try {
            while (isRunning()) {
				Thread.sleep(config.isRunningCheckIntervalMs);
            }
            controller.stopSoft();
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Ant fork was instructed to stop immediately", e);
            controller.stopHard();
        }

        shutdown();

        int exitValue = exitStatus.get();
        LOGGER.log(Level.INFO, TextUtils.merge("Ant fork terminates with exit value ''{0}''", Integer.toString(exitValue)));
        System.exit(exitValue);
    }

    private void setExitStatus(int exitStatus) {
        this.exitStatus.compareAndSet(EXIT_STATUS_DEFAULT, exitStatus);
    }

    protected static final class Arguments {

        public static final String BUILD_FILE = "buildfile";
        public static final String BUILD_TARGET = "buildtarget";
        public static final String RECURRENCE = "recurrence";
        public static final String RECURRENCE_INTERVAL_MS = "recurrenceintervalms";
        public static final String INSTANCES = "instances";
        public static final String START_INTERVAL_MS = "startintervalms";
    }

    public static void addArguments(JavaProcess javaProcess, AntController controller) {
        File buildFile = controller.getBuildFile();
        if (buildFile != null) {
            javaProcess.addApplicationArgument(BaseConstants.MINUS + Arguments.BUILD_FILE);
            javaProcess.addApplicationArgument(buildFile.getAbsolutePath());
        }

        String buildTarget = controller.getBuildTarget();
        if (buildTarget != null) {
            javaProcess.addApplicationArgument(BaseConstants.MINUS + Arguments.BUILD_TARGET);
            javaProcess.addApplicationArgument(buildTarget);
        }

        javaProcess.addApplicationArgument(BaseConstants.MINUS + Arguments.RECURRENCE);
        javaProcess.addApplicationArgument(Integer.toString(controller.getRecurrence()));

        javaProcess.addApplicationArgument(BaseConstants.MINUS + Arguments.RECURRENCE_INTERVAL_MS);
        javaProcess.addApplicationArgument(Long.toString(controller.getRecurrenceIntervalMs()));

        javaProcess.addApplicationArgument(BaseConstants.MINUS + Arguments.INSTANCES);
        javaProcess.addApplicationArgument(Integer.toString(controller.getInstances()));

        javaProcess.addApplicationArgument(BaseConstants.MINUS + Arguments.START_INTERVAL_MS);
        javaProcess.addApplicationArgument(Long.toString(controller.getStartIntervalMs()));

        // tell application about changed location of property file (e.g. in commandline launcher)
        javaProcess.setPropertyFile();

        Map<String, String> properties = controller.getProperties();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            javaProcess.addApplicationArgument(entry.getKey() + BaseConstants.EQUAL + entry.getValue());
        }
    }

    public static Options createOptions() {
        Options options = new Options();

        Option buildFile = new Option(Arguments.BUILD_FILE, true, "ant build file");
        buildFile.setType(String.class);
        buildFile.setRequired(true);
        options.addOption(buildFile);

        Option buildTarget = new Option(Arguments.BUILD_TARGET, true, "ant build target");
        buildTarget.setType(String.class);
        buildTarget.setRequired(true);
        options.addOption(buildTarget);

        Option recurrence = new Option(Arguments.RECURRENCE, true, "number of repetitions executing the ant target");
        recurrence.setType(Integer.class);
        options.addOption(recurrence);

        Option recurrenceIntervalMs = new Option(Arguments.RECURRENCE_INTERVAL_MS, true, "pause before restarting an ant instance");
        recurrenceIntervalMs.setType(Long.class);
        options.addOption(recurrenceIntervalMs);

        Option instances = new Option(Arguments.INSTANCES, true, "number of ant instances");
        instances.setType(Integer.class);
        options.addOption(instances);

        Option startIntervalMs = new Option(Arguments.START_INTERVAL_MS, true, "interval between starting multiple ant instances");
        startIntervalMs.setType(Long.class);
        options.addOption(startIntervalMs);

        Option propertiesFilePath = new Option(BaseConstants.CmdArguments.PROPERTY_FILE, true, "the path to the configuration file");
        propertiesFilePath.setType(String.class);
        options.addOption(propertiesFilePath);

        return options;
    }

    /**
     *
     * @param arguments
     * @return a valid {@link InProcessAntController} that must not be <code>null</code<
     * @throws ParseException if command line arguments could not be parsed in general
     * @throws NumberFormatException if number arguments could not be parsed
     * @author martin.wurzinger
     */
    protected static InProcessAntController read(String[] arguments) throws ParseException, NumberFormatException {
        Parser parser = new BasicParser();
        CommandLine commandLine = parser.parse(createOptions(), arguments);

        String propertiesFilePath = commandLine.getOptionValue(BaseConstants.CmdArguments.PROPERTY_FILE);
        if (propertiesFilePath != null && new File(propertiesFilePath).exists()) {
            EasyTravelConfig.createSingleton(propertiesFilePath);
        } else {
            LOGGER.warning("Run with default configuration because custom configuration file not available");
        }

        String buildFilePath = commandLine.getOptionValue(Arguments.BUILD_FILE);
        File buildFile = (buildFilePath == null) ? null : new File(buildFilePath);

        String buildTarget = commandLine.getOptionValue(Arguments.BUILD_TARGET);

        InProcessAntController controller = new InProcessAntController(buildFile, buildTarget);

        String recurrenceValue = commandLine.getOptionValue(Arguments.RECURRENCE);
        if (recurrenceValue != null) {
            controller.setRecurrence(Integer.parseInt(recurrenceValue));
        }

        String recurrenceIntervalValue = commandLine.getOptionValue(Arguments.RECURRENCE_INTERVAL_MS);
        if (recurrenceIntervalValue != null) {
            controller.setRecurrenceIntervalMs(Long.parseLong(recurrenceIntervalValue));
        }

        String instancesValue = commandLine.getOptionValue(Arguments.INSTANCES);
        if (instancesValue != null) {
            controller.setInstances(Integer.parseInt(instancesValue));
        }

        String startIntervalValue = commandLine.getOptionValue(Arguments.START_INTERVAL_MS);
        if (startIntervalValue != null) {
            controller.setStartIntervalMs(Long.parseLong(startIntervalValue));
        }

        for (String argument : commandLine.getArgs()) {
            // remove heading and trailing double quotes
            if (argument.startsWith(BaseConstants.DQUOTE) && argument.endsWith(BaseConstants.DQUOTE) && argument.length() > 2) {
                argument = argument.substring(1, argument.length() - 1);
            }

            int equalIndex = argument.indexOf(BaseConstants.EQUAL);
            if (equalIndex < 0) {
                throw new IllegalArgumentException(TextUtils.merge("String unable to evaluate argument ''{0}''", argument));
            }

            String name = argument.substring(0, equalIndex);
            String value = argument.substring(equalIndex + 1, argument.length());

            controller.addProperty(name, value);
        }

        return controller;
    }

}
