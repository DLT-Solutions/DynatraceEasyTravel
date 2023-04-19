package com.dynatrace.easytravel.launcher.engine;

import java.io.File;
import java.util.List;

import com.dynatrace.easytravel.constants.BaseConstants.UrlType;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;

/**
 * <p>
 * A <code>Procedure</code> is a runnable entity in context of the easyTravel launcher. A
 * <code>Procedure</code> has to provide some general information like its name, its running state
 * or if it is actually enabled.
 * </p>
 * <p>
 * The core methods of a <code>Procedure</code> is {@link #run()} and {@link #stop()}. Because some
 * procedures are doing its work differently a <code>Procedure</code> has to specify the behavior
 * more precise by implementing {@link #isSynchronous()}, {@link #getStopMode()} and
 * {@link #isOperatingCheckSupported()} appropriately. So different combinations of
 * {@link #isSynchronous()} and {@link #getStopMode()} has different meanings:
 * <ul>
 * <li>
 * if the procedure is <em>synchronous</em> and <em>stoppable</em> then the procedure will be
 * operating after {@link #run()} completes</li>
 * <li>
 * if the procedure is <em>synchronous</em> and <em>not stoppable</em> then the procedure will
 * terminate after {@link #run()} completes</li>
 * <li>
 * if the procedure is <em>not synchronous</em> and <em>stoppable</em> then the procedure is will
 * continue to starting or will be operating after {@link #run()} completes</li>
 * <li>
 * if the procedure is <em>not synchronous</em> and <em>not stoppable</em> then the procedure will
 * continue its work after {@link #run()} completes - the procedure will stop automatically after it
 * finishes its work</li>
 * </ul>
 * </p>
 *
 * @author martin.wurzinger
 */
public interface Procedure extends ProcedureStateListener, RunningSubject {

    /**
     * Get the procedure name.
     *
     * @return the name of the procedure
     * @author martin.wurzinger
     */
    String getName();

    /**
     * Run or start the procedure.
     *
     * @return
     * @author martin.wurzinger
     */
    Feedback run();

    /**
     * <p>
     * Characterize if the {@link #run()} method is blocking or not.
     * </p>
     *
     * @return <code>true</code> if the {@link #run()} method blocks until the procedure is
     *         operating or returns <code>false</code> otherwise
     * @author martin.wurzinger
     */
    boolean isSynchronous();

    /**
     * Characterize if the procedure can be stopped by calling the {@link #stop()} method. If it is
     * not stoppable the procedure terminates automatically after {@link #run()} has been completed.
     *
     * @return <code>true</code> if the procedure has to be stopped by calling {@link #stop()} or
     *         returns <code>false</code> if it stops automatically.
     * @author martin.wurzinger
     */
    boolean isStoppable();

    /**
     * Indicates in which mode the procedure can be stopped.
     *
     * @return an enum values of {@link StopMode}
     * @author stefan.moschinski
     */
    StopMode getStopMode();

    /**
     * Stop the procedure.
     *
     * @return feedback if the stop command could be executed successfully
     * @author martin.wurzinger
     */
    Feedback stop();

    /**
     * <p>
     * Check if this Procedure is currently running.
     * </p>
     * <p>
     * A process is <em>running</em> if operating normally or if it is currently starting or
     * stopping.
     * </p>
     * <p>
     * A process is <em>not running</em> if it has not been started yet or if it was already
     * stopped.
     * </p>
     *
     * @return <code>true</code> if the procedure is currently running
     * @author martin.wurzinger
     */
    boolean isRunning();

    /**
     * Characterize if the procedure supports the {@link #isOperating()} method.
     *
     * @return <code>true</code> if {@link #isOperating()} is supported or returns
     *         <code>false</code> otherwise
     * @author martin.wurzinger
     */
    boolean isOperatingCheckSupported();

    /**
     * <p>
     * Check if this procedure is operating at the moment.
     * </p>
     * <p>
     * A procedure is <em>operating</em> if startup process has been completed and it is actually
     * servicing.
     * </p>
     * <p>
     * A procedure is <em>not operating</em> if it has not been started yet, if it was already
     * stopped or if it is currently starting or stopping. stopped.
     * </p>
     *
     * @return <code>true</code> if the procedure is currently running
     * @author martin.wurzinger
     */
    boolean isOperating();

    /**
     * Characterize if the procedure is enabled.
     *
     * @return <code>true</code> if the procedure is enabled or returns <code>false</code> otherwise
     * @author martin.wurzinger
     */
    boolean isEnabled();

    /**
     * Get the configuration mapping for this <code>Procedure</code>.
     *
     * @return the configuration mapping for this <code>Procedure</code> which must not be
     *         <code>null</code>
     * @author martin.wurzinger
     */
    ProcedureMapping getMapping();

    /**
     * Check if the procedure is able to be transfered to another {@link Batch} in place of the
     * specified {@link ProcedureMapping}.
     *
     * @param otherMapping the mapping to be used for
     * @return <code>true</code> if the procedure is transferable
     * @author martin.wurzinger
     */
    boolean isTransferableTo(ProcedureMapping otherMapping);

    /**
     * Callback that is executed when a procedure is transfered to another {@link Batch}.
     *
     * @param mapping the mapping for which the procedure is used for
     * @param state the current state of the procedure
     * @author martin.wurzinger
     */
    void transfer(ProcedureMapping mapping, State state);

    /**
     * If the procedure exposes some service, it can return an URI to it.
     *
     * Typically this is used by the frontend procedures to provide the location
     * where the web pages can be accessed.
     *
     * @return An URI where this procedure is accessible or null if none is available.
     */
    String getURI();

    /**
     * Provides some details about the started procedure, e.g. agent details,
     * actual commandline, ...
     *
     * @return
     * @author dominik.stadler
     */
    String getDetails();

    /**
     * Return a list of procedure-ids which we have dependencies on. This
     * means that this procedure should get notified if the depending procedure
     * changes state.
     *
     * @return
     * @author dominik.stadler
     */
    List<String> getDependingProcedureIDs();

    /**
     *
     *
     * @return true if this procedure does have a logfile.
     * @author peter.kaiser
     */
    boolean hasLogfile();

    /**
     * Returns the path of the logfile that this procedure creates.
     *
     * @return A full path to the logfile.
     * @author dominik.stadler
     */
    String getLogfile();

    /**
     * If the procedure exposes some service, it can return an URI to it.
     *
     * Typically this is used by the frontend procedures to provide the location
     * where the web pages can be accessed.
     *
     * If a procedure may return more than one url, you can use this method to determine the return value.
     *
     * @return An URI where this procedure is accessible or null if none is available.
     */
	String getURI(UrlType urlType);


    /**
     * @return true if this Procedure there exists an agent which supports the technology used in this procedure.
     * @author christoph.neumueller
     */
    boolean isInstrumentationSupported();

    /**
     * @return true if the corresponding agent has been found on the current machine.
     * @author christoph.neumueller
     */
	boolean agentFound();

    /**
     * @return The technology used by this Procedure.     *
     * @author christoph.neumueller
     */
	Technology getTechnology();

	/**
	 * @return A properties file that is passed to the external procedure or null if none is available
	 */
	File getPropertyFile();
	
	/**
	 * Like getURI(), but returns a fully qualified domain name.
	 * (Perhaps not in all cases, but at least for local host.)
	 */
	String getURIDNS();
	
	/**
	 * Like getURI(UrlType urlType), but returns a fully qualified domain name.
	 * (Perhaps not in all cases, but at least for local host.)
	 */
	String getURIDNS(UrlType urlType);
	
    /**
     * @return The timeout of procedure.
     * @author kasper.kulikowski
     */
	int getTimeout();
}
