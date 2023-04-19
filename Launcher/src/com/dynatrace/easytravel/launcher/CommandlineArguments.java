package com.dynatrace.easytravel.launcher;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.util.TextUtils;

public class CommandlineArguments {

    private final File propertyFile;
    private final File scenarioFile;
    private final String startScenario;
    private final String startGroup;
    private final boolean noAutostart;
    private final boolean help;

    /**
     * @param arguments the command line arguments to parse
     * @throws ParseException if the command line arguments are invalid
     * @throws FileNotFoundException if the scenarios.xml or the easyTravelConfig.properties file
     *         cannot be found
     * @author martin.wurzinger
     */
    public CommandlineArguments(String[] arguments) throws ParseException, FileNotFoundException, IllegalArgumentException {
        String[] tempArguments = (arguments == null) ? new String[0] : arguments;

        Parser parser = new BasicParser();
        Options options = createOptions();
		CommandLine commandLine = parser.parse(options, tempArguments);

        // automatically generate the help statement
		if( commandLine.hasOption( "help" ) ) {
	        HelpFormatter formatter = new HelpFormatter();
	        formatter.printHelp( "CommandlineLauncher", options );

	        help = true;

	        // don't try to parse the other options
	        propertyFile = null;
	        scenarioFile = null;
	        startScenario = null;
	        startGroup = null;
	        noAutostart = false;

	        return;
	    }

		// report if the commandline is stated incorrectly
        if(!commandLine.getArgList().isEmpty()) {
        	throw new IllegalArgumentException("Trailing arguments are not supported by CommandlineLauncher, but had: " + commandLine.getArgList());
        }

        String propertyFilePath = commandLine.getOptionValue(Constants.CmdArguments.PROPERTY_FILE);
        propertyFile = (propertyFilePath == null) ? null : parsePath(propertyFilePath);

        String scenarioFilePath = commandLine.getOptionValue(Constants.CmdArguments.SCENARIO_FILE);
        scenarioFile = (scenarioFilePath == null) ? null : parsePath(scenarioFilePath);

        startScenario = commandLine.getOptionValue(Constants.CmdArguments.START_SCENARIO);
        startGroup = commandLine.getOptionValue(Constants.CmdArguments.START_GROUP);

        noAutostart = commandLine.hasOption(Constants.CmdArguments.NO_AUTOSTART);

        help = false;
    }

    /**
     * Parse in a path, create the {@link File} instance and check if the specified file can be
     * found.
     *
     * @param filePath the absolute or relative (to working directory) path to a file that has
     *        to exist
     * @return the resolved and existing file
     * @throws FileNotFoundException if the specified file could not be found
     * @author martin.wurzinger
     */
    private File parsePath(String filePath) throws FileNotFoundException {
        if (filePath == null) {
            throw new IllegalArgumentException("Path argument must not be null.");
        }

        File absoluteFile = new File(filePath);
        if (absoluteFile.isAbsolute() && absoluteFile.exists()) {
            return absoluteFile;
        }

        File relativeFile = new File(Directories.getWorkingDir(), filePath);
        if (relativeFile.exists()) {
            return relativeFile;
        }

        relativeFile = new File(filePath);
        if (relativeFile.exists()) {
            return relativeFile;
        }

        throw new FileNotFoundException(TextUtils.merge("Unable to find file ''{0}''.", filePath));
    }

    public static Options createOptions() {
        Options options = new Options();

        Option help = new Option( "h", "help", false, "print this message" );
        help.setRequired(false);
        options.addOption(help);

        Option propertiesFile = new Option(Constants.CmdArguments.PROPERTY_FILE, true, "the basic configuration properties file");
        propertiesFile.setType(String.class);
        propertiesFile.setRequired(false);
        options.addOption(propertiesFile);

        Option scenarioFile = new Option(Constants.CmdArguments.SCENARIO_FILE, true, "the scenario configuration file");
        scenarioFile.setType(String.class);
        options.addOption(scenarioFile);

        Option startScenario = new Option(Constants.CmdArguments.START_SCENARIO, true, "the scenario to start");
        startScenario.setType(String.class);
        options.addOption(startScenario);

        Option startGroup = new Option(Constants.CmdArguments.START_GROUP, true, "the group to look for the scenario to start (necessary if scenarios with same title exists in different grups)");
        startGroup.setType(String.class);
        options.addOption(startGroup);

        Option noAutostart = new Option(Constants.CmdArguments.NO_AUTOSTART, false, "specifies that no scenario should be started upon startup of the application");
        options.addOption(noAutostart);

        return options;
    }

    public File getPropertyFile() {
        return propertyFile;
    }

    public File getScenarioFile() {
        return scenarioFile;
    }

    public String getStartScenario() {
        return startScenario;
    }

    public String getStartGroup() {
        return startGroup;
    }

	public boolean isNoAutostart() {
		return noAutostart;
	}

	public boolean isHelp() {
		return help;
	}
}