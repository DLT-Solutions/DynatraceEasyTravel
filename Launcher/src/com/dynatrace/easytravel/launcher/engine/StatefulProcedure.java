package com.dynatrace.easytravel.launcher.engine;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.UrlType;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.procedures.AntProcedure;
import com.dynatrace.easytravel.launcher.procedures.B2BFrontendProcedure;
import com.dynatrace.easytravel.launcher.procedures.PaymentBackendProcedure;
import com.dynatrace.easytravel.launcher.procedures.RemoteProcedure;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;


/**
 * A {@link Procedure} that always has one of the states defined in {@link State}. Listeners can
 * register for state change events.
 *
 * @author martin.wurzinger
 * @author Michal.Bakula
 */
public class StatefulProcedure implements Procedure {

    private static final Logger LOGGER = Logger.getLogger(StatefulProcedure.class.getName());

	private static final ScheduledExecutorService automaticProcedureStopExecutor = new ScheduledThreadPoolExecutor(1,
			new ThreadFactoryBuilder().setDaemon(true)
					.setNameFormat(BaseConstants.AUTOMATIC_PROCEDURE_SHUTDOWN_THREAD + "-%d")
					.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
						@Override
						public void uncaughtException(Thread t, Throwable e) {
							LOGGER.log(Level.SEVERE, TextUtils
									.merge("An uncaught exception happened in the thread ''{0}''", t.getName()), e);
						}
					}).build());

    private final CopyOnWriteArrayList<ProcedureStateListener> listeners = new CopyOnWriteArrayList<ProcedureStateListener>();
    private final Procedure delegate;
    private State state = State.getDefault();
    private boolean isRunningWithTimeout;
    private long timeout;

    public StatefulProcedure(Procedure delegate) {
        this.delegate = delegate;
        this.isRunningWithTimeout = false;

        // delegate has to inform us if it stops because of external instructions (kill process)
        addDefaultStopListener(); // NOPMD
    }

	protected void addDefaultStopListener() {
		delegate.addStopListener(new AbstractStopListener() {
            @Override
            public void notifyProcessStopped() {
                setState(State.STOPPED);
            }
        });
	}

    public void setState(State newState) {
        State oldState = state;
        state = newState;

        if (oldState != newState) {
            fireStateChanged(oldState, newState);
        }
    }

    /**
     * @author Michal.Bakula
     * @param oldState
     * @param newState
     */
	private void fireStateChanged(State oldState, State newState) {
		// delegates always listen to state changes of itself
		delegate.notifyProcedureStateChanged(this, oldState, newState);

		if (this.isRunningWithTimeout()) {
			if (oldState == State.STARTING && newState == State.OPERATING) {
				automaticStopTask();
			}

			if (newState == State.STOPPED || newState == State.FAILED || newState == State.TIMEOUT) {
				resetRunWithTimeout();
			}
		}

		fireStateChanged(oldState, newState, listeners);
	}

	/**
	 * @author Michal.Bakula
	 */
	private void automaticStopTask(){
		LOGGER.log(Level.INFO, TextUtils.merge("Scheduling stop of procedure {0}.", delegate.getName()));
		automaticProcedureStopExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				LOGGER.log(Level.INFO,
						TextUtils.merge("Attempting to stop procedure {0}.", delegate.getName()));
				delegate.stop();
			}
		}, this.getRunTimeout(), TimeUnit.MILLISECONDS);
	}

    private void fireStateChanged(State oldState, State newState, Collection<ProcedureStateListener> listeners) {
        for (ProcedureStateListener listener : listeners) {
        	if (listener != null) {
        		listener.notifyProcedureStateChanged(this, oldState, newState);
        	}
        }
    }

    /**
     * Add lister if it hasn't been registered yet. The listener will be immediately notified about
     * the initial state.
     *
     * @param listener
     * @author martin.wurzinger
     */
    public void addListener(ProcedureStateListener listener) {
		listeners.addIfAbsent(listener);

		// initial state notification
		listener.notifyProcedureStateChanged(this, state, state);
    }

    /**
     * Add listeners which have not been registered already. The listeners will be immediately
     * notified about the initial state.
     *
     * @param listeners
     * @author martin.wurzinger
     */
    public void addListeners(Collection<ProcedureStateListener> listeners) {
		this.listeners.addAllAbsent(listeners);

        // initial state notification
        fireStateChanged(state, state, listeners);
    }

    public void removeListener(ProcedureStateListener listener) {
		listeners.remove(listener);
    }

    public void clearListeners() {
		listeners.clear();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return delegate.getName();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRunning() {
        if (delegate.isRunning()) {
            /*
             * if the operating check is not supported than we assume that the procedure is
             * operating when it's running
             */
            if (!isOperatingCheckSupported()) {
                setState(State.OPERATING);
            }
            return true;
        } else {
            setState(State.STOPPED);
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Feedback run() {
        switch (state) {
            case STOPPED:
            case UNKNOWN:
            case FAILED:
            case ACCESS_DENIED:
                setState(State.STARTING);

                try {
					Feedback feedback = delegate.run();
					// if starting did not succeed, set state to "FAILED"
					if(!feedback.isOk()) {
						setState(State.FAILED);
					} else if (isSynchronous()) {
						// if the Procedure is still running now, we set the State to
						// OPERATING as we expect it to keep running "endlessly", otherwise
						// we set it to STOPPED as it did finish as part of "run()"
						setState(isRunning() ? State.OPERATING : State.STOPPED);
					}
					return feedback;

                } catch (Exception e) {
    	            LOGGER.log(Level.SEVERE, "Exception caught when running procedure '" + delegate.getName() + "'", e);
                	setState(State.FAILED);
                }
            case TIMEOUT:
            case STARTING:
            case OPERATING:
                LOGGER.info(TextUtils.merge("Trying to start a procedure that is in state ''{0}'': {1}.", state, delegate.getName()));
                return Feedback.Neutral;

            case STOPPING:
                LOGGER.warning(TextUtils.merge("Trying to start a procedure that is stopping at the moment: {0}.", delegate.getName()));
                return Feedback.Failure;

            default:
                LOGGER.warning(TextUtils.merge("Unable to start a procedure that has an undefined state ''{0}''.", state));
                return Feedback.Failure;
        }
    }

    /** {@inheritDoc} */
    @Override
    public StopMode getStopMode() {
        return delegate.getStopMode();
    }

    /** {@inheritDoc} */
	@Override
	public boolean isStoppable() {
		return delegate.isStoppable();
	}

    /** {@inheritDoc} */
    @Override
    public Feedback stop() {
    	LOGGER.warning("Stopping procedures. Stopping Stateful - " + this.getDetails());
        if (getStopMode() == StopMode.NONE) {
            throw new IllegalStateException("The procedure is not stoppable.");
        }

        if (hasState(State.STOPPED)) {
            LOGGER.info(TextUtils.merge("Trying to stop a procedure that is in state ''{0}''.", state));
            return Feedback.Neutral;
        }

        setState(State.STOPPING);
        Feedback feedback = delegate.stop();
        if(feedback.isOk()) {
        	LOGGER.warning("Stopping procedures. Stateful stopped");
        	setState(State.STOPPED);
        } else {
        	LOGGER.warning("Stopping procedures. Stateful stop failed");
        	setState(State.FAILED);
        }

        return feedback;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isOperatingCheckSupported() {
        return delegate.isOperatingCheckSupported();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isOperating() {
    	try {
    		return delegate.isOperating();
    	} catch (Exception e) {
    		LOGGER.log(Level.WARNING, "Checking for state of procedure '" + delegate.getName() + "' failed with an exception.", e);
    		return false;
    	}
    }

    public boolean isStartingFinished() {
        if (!isOperatingCheckSupported()) {
            throw new IllegalStateException("The operating check for this procedure is not supported.");
        }

        if (!hasState(State.STARTING)) { // starting might have been canceled
            return true;
        } else if (isOperating()) { // may take some time; network communication
            setState(State.OPERATING);
        }

        return !hasState(State.STARTING); // check again; state might have changed in mean time
    }

    public boolean hasState(State state) {
        return this.state == state;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSynchronous() {
        return delegate.isSynchronous();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEnabled() {
        return delegate.isEnabled();
    }

    @Override
    public void notifyProcedureStateChanged(StatefulProcedure subject, State oldState, State newState) {
        delegate.notifyProcedureStateChanged(subject, oldState, newState);
    }

    @Override
    public void transfer(ProcedureMapping mapping, State state) {
        delegate.transfer(mapping, state);
    }

    public void transfer(ProcedureMapping mapping) {
        transfer(mapping, this.state);
    }

    public String getStateLabel() {
        switch (state) {
            case STOPPED:
                if (!isSynchronous()) {
                    return MessageConstants.STATE_NOT_RUNNING;
                } else {
                    return MessageConstants.STATE_FINISHED;
                }
            case STARTING:
                if (!isSynchronous()) {
                    return MessageConstants.STATE_STARTING;
                } else {
                    return MessageConstants.STATE_RUNNING;
                }
            case OPERATING:
                return MessageConstants.STATE_OPERATING;
            case STOPPING:
                return MessageConstants.STATE_STOPPING;
            case FAILED:
                return MessageConstants.STATE_FAILED;
            case ACCESS_DENIED:
            	return MessageConstants.STATE_ACCESS_DENIED;
            case TIMEOUT:
                return MessageConstants.STATE_TIMEOUT;
            default:
                return MessageConstants.STATE_UNKNOWN;
        }
    }

    public State getState() {
        return state;
    }

    @Override
    public void addStopListener(StopListener stopListener) {
        delegate.addStopListener(stopListener);
    }

    @Override
    public void removeStopListener(StopListener stopListener) {
        delegate.removeStopListener(stopListener);
    }

    @Override
    public void clearStopListeners() {
        delegate.clearStopListeners();
    }

    @Override
    public ProcedureMapping getMapping() {
        return delegate.getMapping();
    }

    @Override
    public boolean isTransferableTo(ProcedureMapping otherMapping) {
    	// re-try procedures which timed out before
    	if(State.TIMEOUT.equals(state)) {
    		return false;
    	}

        return delegate.isTransferableTo(otherMapping);
    }

    @Override
	public String getURI() {
        String uri = delegate.getURI();
        if (uri != null) {
            return uri.toLowerCase();
        }
        return uri;
    }

	// Like getURI(), but it attempts to return a fully qualified domain name.
	@Override
	public String getURIDNS() {
        String uri = delegate.getURIDNS();
        if (uri != null) {
            return uri.toLowerCase();
        }
        return uri;
    }

	@Override
	public String getDetails() {
		return delegate.getDetails();
	}


	@Override
	public List<String> getDependingProcedureIDs() {
		return delegate.getDependingProcedureIDs();
	}

	@Override
	public String getLogfile() {
        return delegate.getLogfile();
	}

	@Override
	public String getURI(UrlType urlType) {
        String uri = delegate.getURI(urlType);
        if (uri != null) {
            return uri.toLowerCase();
        }
        return uri;
    }

	// Like getURI(UrlType urlType), but it attempts to return.
	@Override
	public String getURIDNS(UrlType urlType) {
        String uri = delegate.getURIDNS(urlType);
        if (uri != null) {
            return uri.toLowerCase();
        }
        return uri;
    }

	@Override
    public boolean hasLogfile() {
	    return delegate.hasLogfile();
    }

	public boolean isWebProcedure() {
	    return delegate instanceof WebProcedure;
	}

	public boolean isDotNetIISProcedure() {
	    if(delegate instanceof B2BFrontendProcedure){
	    	return ((B2BFrontendProcedure)delegate).isRunningOnLocalIIS();
	    }
	    if(delegate instanceof PaymentBackendProcedure){
	    	return ((PaymentBackendProcedure)delegate).isRunningOnLocalIIS();
	    }
		if (delegate instanceof RemoteProcedure) {
			return ((RemoteProcedure) delegate).isRunningOnIIS();
	    }
		return false;
	}

    public boolean isNginxWebserver() {
        if (delegate instanceof NginxWebserverProcedure) {
            return true;
        }
        if (delegate instanceof RemoteProcedure) {
            if (Constants.Procedures.NGINX_WEBSERVER_ID.equals(delegate.getMapping().getId())) {
                return true;
            }
        }
        return false;
    }

	public int getPort() {
	    return isWebProcedure() ? ((WebProcedure) delegate).getPort() : -1;
	}

	public String getPortPropertyName() {
	    return isWebProcedure() ? ((WebProcedure) delegate).getPortPropertyName() : null;
	}

	public Procedure getDelegate() {
		return delegate;
	}

    @Override
    public Technology getTechnology() {
        return delegate.getTechnology();
    }

    @Override
    public boolean isInstrumentationSupported() {
        return delegate.isInstrumentationSupported();
    }

    @Override
    public boolean agentFound() {
        return delegate.agentFound();
    }

	public boolean setContinuously(boolean continuously) {
		// TODO: make this work for Remote Ant Procedures as well!
	    if (delegate instanceof AntProcedure) {
	        return ((AntProcedure)delegate).setContinuously(continuously);
	    } else {
	        throw new IllegalStateException("must not set continuously for a non ant Procedure");
	    }
	}

	@Override
	public File getPropertyFile() {
		return delegate.getPropertyFile();
	}

	@Override
	public int getTimeout(){
		return EasyTravelConfig.read().syncProcessTimeoutMs;
	}

	public boolean isRunningWithTimeout() {
		return isRunningWithTimeout;
	}

	/**
	 * Returns time in milliseconds after which procedure should stop.
	 */
	public long getRunTimeout() {
		return timeout;
	}

	/**
	 * Used to setup procedure with automatic stop after provided time in milliseconds.
	 */
	public void setupRunWithTimeout(long timeout) {
		this.isRunningWithTimeout = true;
		this.timeout = timeout;
	}

	/**
	 * Resets settings used for automatic stop of procedure.
	 */
	public void resetRunWithTimeout() {
		LOGGER.log(Level.INFO, TextUtils.merge("Reseting automatic stop settings for procedure {0}.", delegate.getName()));
		this.isRunningWithTimeout = false;
		this.timeout = 0;
	}
}
