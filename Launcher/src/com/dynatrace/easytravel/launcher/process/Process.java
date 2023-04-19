package com.dynatrace.easytravel.launcher.process;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.RunningSubject;

/**
 * Interface for an external process that is started from Launcher.
 *
 * It allows to start and stop the process and usually internally monitors the
 * process to get notified when it stops unexpectedely.
 *
 * @author martin.wurzinger
 *
 */
public interface Process extends RunningSubject {

    /**
     * Start the process.
     *
     * @author martin.wurzinger
     */
    Feedback start();

    /**
     * The given runnable will be executed when the process is destroyed.
     *
     * @author peter.kaiser
     */
    Feedback start(Runnable stopAction);

    /**
     * Stop the process
     *
     * @author martin.wurzinger
     */
    Feedback stop();

    /**
     * Check if the process is currently running.
     *
     * @return <code>true</code> if the java process is currently running
     * @author martin.wurzinger
     */
    boolean isRunning();

    /**
     * Set command line application arguments.
     *
     * @param appArgument a command line application argument
     * @author martin.wurzinger
     */
	Process addApplicationArgument(String appArgument);

	/**
	 * Set command line application arguments.
	 *
	 * @param appArgument a command line application argument
	 * @param value separated by a <b>whitespace</b>, may not be <code>null</code>
	 * @return
	 */
	Process addApplicationArgumentPair(String appArgument, Object value);

    /**
     * Remove all additional application arguments.
     */
    void clearApplicationArguments();

    /**
     * Define an environment variable which should be set when the process is started.
     *
     * This overrides any environment variable already defined in the parent environment.
     *
     * Note: Implementations of this interface need to ensure to add the result of
     * 		super.getEnvironment() into account in overridden getEnvironment() implementations.
     *
     * @param key environment variable name
     * @param value environment variable value
     */
	void setEnvironmentVariable(String key, String value);

    /**
     * set a custom output stream for the application
     *
     * @param out
     * @author peter.kaiser
     */
    public void setOut(OutputStream out);

    /**
     * set a custom error stream for the application
     *
     * @param err
     * @author peter.kaiser
     */
    public void setErr(OutputStream err);

    /**
     * set a custom input stream for the application
     *
     * @param in
     * @author peter.kaiser
     */
    public void setIn(InputStream in);

    /**
     * Provides some details about the started procedure, e.g. agent details,
     * actual commandline, ...
     *
     * @return
     * @author dominik.stadler
     */
    String getDetails();

    /**
     * Passes the current config-properties via a comamndline parameter
     * "-propertyfile".
     *
     * @author dominik.stadler
     */
	void setPropertyFile();

	/**
	 * @return the custom property file that is set or null if none is set.
	 */
	File getPropertyFile();

    /**
     * @return the agent config object which was created in this implementation.
     */
    DtAgentConfig getDtAgentConfig();
    
    /**
     * @return enviroment variables for this process
     * @author rafal.psciuk
     */
    Map<String, String> getEnvironment();
}
