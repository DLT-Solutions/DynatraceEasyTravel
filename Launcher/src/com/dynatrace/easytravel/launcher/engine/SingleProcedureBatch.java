package com.dynatrace.easytravel.launcher.engine;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.Setting;
import com.dynatrace.easytravel.util.TextUtils;


/**
 * A class that can start and stop a {@link Procedure}. Instances are stateful (see {@link State}) and
 * {@link BatchStateListener}s are notified on batch state change events.
 *
 * Note that this procedure will not start remote procedures, even if the system properties are set up
 * in order to avoid endless loops when we try to start a remote procedure in the same process as the
 * master launcher.
 *
 * @author dominik.stadler
 */
public class SingleProcedureBatch {
    private static final Logger LOGGER = Logger.getLogger(SingleProcedureBatch.class.getName());
    private static final ProcedureFactory FACTORY = new ProcedureFactory();

    private final StatefulProcedure procedure;
    private final CopyOnWriteArrayList<BatchStateListener> batchStateListeners = new CopyOnWriteArrayList<BatchStateListener>();
    private final List<ProcedureStateListener> procedureStateListeners = new ArrayList<ProcedureStateListener>();

    private long start = 0;

    public SingleProcedureBatch(ProcedureMapping mapping, List<ProcedureStateListener> procedureStateListeners) {
        this.procedureStateListeners.addAll(procedureStateListeners);
        this.procedure = createStatefulProcedure(mapping);
    }

    private static StatefulProcedure createStatefulProcedure(ProcedureMapping mapping) {
        // use custom settings of the scenario if any exists
    	// do this before we even create the procedure as some procedures do stuff in the constructor already
    	// e.g. Customer Frontend
    	if(mapping != null) {
    		// check for size here to not apply new properties when not needed
    		if(mapping.hasCustomSettings()) {
				Map<String, String> settings = new HashMap<String, String>();
				for (Setting setting : mapping.getCustomSettings()) {
					settings.put(setting.getName(), setting.getValue());
				}
		        EasyTravelConfig.applyCustomSettings(settings);
    		}
    	}

    	// never start remote procedures here, otherwise we create endless loops if hostname is set to the hostname of the master-launcher
        Procedure procedure = FACTORY.create(mapping, false);
        if (procedure == null) {
            throw new IllegalStateException("Could not create procedure with mapping id: " + (mapping == null ? mapping : mapping.getId()));
        }

        return StatefulProcedureFactory.newInstance(procedure);
    }

    /**
     * Start the batch. Does not wait until all procedures are operating!
     *
     * @throws IllegalStateException if the batch has not a runnable state - the batch might has
     *         already be started
     * @author martin.wurzinger
     */
    public void start() throws IllegalStateException {
        if (procedure.getState() != State.STOPPED) {
            throw new IllegalStateException(TextUtils.merge("The batch can only be started if it is in state {0}. Current state is {1}", State.STOPPED, procedure.getState()));
        }

        //registerProcedures();

        LOGGER.info(TextUtils.merge("Starting procedure ''{0}''...", procedure.getName()));
        // -------------------------

        logStartProcedure(procedure);

        boolean started = procedure.run().isOk();
        start = System.currentTimeMillis();

        if (started) {
            LOGGER.info("easyTravel procedure " + procedure.getName() + " started successfully.");
        } else {
            LOGGER.warning("easyTravel procedures " + procedure.getName() + " could not be started.");
        }
    }

    public void stop() {
		if (procedure.getState() != State.OPERATING && procedure.getState() != State.TIMEOUT && procedure.getState() != State.STARTING) {
			throw new IllegalStateException(TextUtils.merge(
					"The batch can only be stopped if it is in state {0}, {1} or {2}. Current state is {3}", State.OPERATING,
					State.STARTING, State.TIMEOUT, procedure.getState()));
		}

        LOGGER.info("Stopping easyTravel procedures...");
        // -------------------------

        boolean stopped = true;

        if (procedure.getStopMode() != StopMode.NONE) {
            logStopProcedure(procedure);
            stopped = procedure.stop().isOk();
        }

        // -------------------------
        if (stopped) {
            LOGGER.info("Procedure stopped successfully.");
        } else {
            LOGGER.warning("Procedure could not be stopped in expected time.");
        }
    }

    private static void logStartProcedure(Procedure procedure) {
        LOGGER.info(TextUtils.merge("Running {0}...", procedure.getName()));
    }

    private static void logStopProcedure(Procedure procedure) {
        LOGGER.info(TextUtils.merge("Stopping {0}...", procedure.getName()));
    }

    public void addBatchStateListeners(Collection<BatchStateListener> batchStateListeners) {
        this.batchStateListeners.addAllAbsent(batchStateListeners);
    }

	public State getState() {
		if(procedure.getState().equals(State.STARTING)) {
			if((System.currentTimeMillis() - start) >=  EasyTravelConfig.read().syncProcessTimeoutMs) {
				procedure.setState(State.TIMEOUT);
				LOGGER.warning("easyTravel procedures " + procedure.getName() + " could not be started in the expected time, timeout: " + EasyTravelConfig.read().syncProcessTimeoutMs*2 + "ms.");
			}
		}

		// refresh the state periodically until we have OPERATING
		if(procedure.isOperatingCheckSupported() && procedure.getState().equals(State.STARTING)) {
			procedure.isStartingFinished();
		}

		return procedure.getState();
	}

	public StatefulProcedure getProcedure() {
		return procedure;
	}

	public List<BatchStateListener> getBatchStateListeners() {
		return batchStateListeners;
	}

	public List<ProcedureStateListener> getProcedureStateListeners() {
		return procedureStateListeners;
	}

	public int getPort() {
	    return procedure.getPort();
	}

}
