package com.dynatrace.easytravel.launcher.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import com.dynatrace.diagnostics.uemload.UemLoadScheduler;
import com.dynatrace.diagnostics.uemload.iot.car.DynatraceRentalCar;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.baseload.BaseLoadManager;
import com.dynatrace.easytravel.launcher.baseload.HeadlessB2BBaseLoad;
import com.dynatrace.easytravel.launcher.config.ScenarioConfiguration;
import com.dynatrace.easytravel.launcher.plugin.restore.DefaultRestorePoint;
import com.dynatrace.easytravel.launcher.plugin.restore.ScenarioStateRestore;
import com.dynatrace.easytravel.launcher.pluginscheduler.PluginSchedulerManager;
import com.dynatrace.easytravel.launcher.procedures.utils.CentralTechnologyActivator;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.launcher.scenarios.ScenarioGroup;
import com.dynatrace.easytravel.pluginscheduler.Quartz;
import com.dynatrace.easytravel.util.TextUtils;


/**
 * The launch engine is the core unit to start and stop scenarios. It also controls the number of
 * {@link Batch} instances - only one running scenario is allowed.
 *
 * @author martin.wurzinger
 */
public final class LaunchEngine {

    private static final Logger LOGGER = Logger.getLogger(LaunchEngine.class.getName());

	private static boolean pluginSchedulerEnabled = EasyTravelConfig.read().pluginSchedulerEnabled;

    // singleton
    private static Batch runningBatch;

    // members
	private final List<BatchStateListener> batchStateListeners = new ArrayList<BatchStateListener>();
    private final List<ProcedureStateListener> procedureStateListeners = new ArrayList<ProcedureStateListener>();
    private final List<ScenarioListener> scenarioListeners = new ArrayList<ScenarioListener>();

    private final CentralTechnologyActivator techActivator = CentralTechnologyActivator.getIntance();


    private LaunchEngine() {
        procedureStateListeners.add(Launcher.getCustomerBaseLoad());
        procedureStateListeners.add(Launcher.getB2BBaseLoad());
        procedureStateListeners.add(Launcher.getMobileNativeBaseLoad());
		procedureStateListeners.add(Launcher.getMobileBrowserBaseLoad());
		procedureStateListeners.add(Launcher.getHotDealBaseLoad());
		if(DynatraceRentalCar.isConfigSet()) {
			procedureStateListeners.add(Launcher.getIotDevicesBaseLoad());
		}
		procedureStateListeners.add(Launcher.getHeadlessCustomerBaseLoad());
		procedureStateListeners.add(Launcher.getHeadlessAngularBaseLoad());
		procedureStateListeners.add(Launcher.getHeadlessMobileAngularBaseLoad());
		procedureStateListeners.add(Launcher.getHeadlessB2BBaseLoad());

		procedureStateListeners.add(CommandProcedureStateListener.getInstance());

		batchStateListeners.add(BaseLoadManager.getInstance());
		
		techActivator.registerBackendListener((HeadlessB2BBaseLoad)Launcher.getHeadlessB2BBaseLoad());

		if (pluginSchedulerEnabled) {
			//Launcher.initPluginScheduler must be called before to make scheduler work properly
			Scheduler scheduler = Quartz.getScheduler();
			if (scheduler != null) {
				batchStateListeners.add(new PluginSchedulerManager(scheduler));
			} else { //this may happen in tests
				LOGGER.warning("Scheduler is not initialized correctly. batchStateListenter not added");
			}
		}
        scenarioListeners.add(BaseLoadManager.getInstance());

        if (Launcher.isWeblauncher() && EasyTravelConfig.read().isWebLauncherAuthEnabled) {
            batchStateListeners.add(new DefaultRestorePoint());
            scenarioListeners.add(new ScenarioStateRestore());
            LOGGER.log(Level.INFO, "Automatic scenario restore has been initialized");
        }

        scenarioListeners.add(new ScenarioListener() {
			@Override
			public void notifyScenarioChanged(Scenario scenario) {
				if(scenario == null) {
					// nothing to do on Batch-stop
					return;
				}

				// try to read setting and set the thread-count which is configured in the scenario
				String threads = scenario.getCustomSettings().get("UEMLoadThreadCount");
				if(threads != null) {
					try {
						int threadCount = Integer.parseInt(threads);
						UemLoadScheduler.setMaxThreads(threadCount);
					} catch (NumberFormatException e) {
						LOGGER.warning("Could not parse setting for UEMLoadThreadCount: " + threads + ": " + e);
					}
				}
			}
		});
    }

    public static LaunchEngine getNewInstance() {
    	return new LaunchEngine();
    }

    public static Batch getRunningBatch() {
    	return runningBatch;
    }

    /**
     * Restore a previous state by invoking the listeners again.
     */
    public void restore() {
    	Batch runningBatch = getRunningBatch();
    	if (runningBatch == null) {
    		return;
    	}

    	State batchState = runningBatch.getState();
    	State initial = State.getDefault();
    	List<BatchStateListener> batchStateListeners = this.batchStateListeners;
    	List<ProcedureStateListener> procedureStateListeners = this.procedureStateListeners;
    	List<ScenarioListener> scenarioListeners = this.scenarioListeners;

    	runningBatch.getBatchStateListeners().clear();
    	runningBatch.getBatchStateListeners().addAll(batchStateListeners);
    	runningBatch.getProcedureStateListeners().clear();
    	runningBatch.getProcedureStateListeners().addAll(procedureStateListeners);

    	for (BatchStateListener listener : batchStateListeners) {
    		listener.notifyBatchStateChanged(runningBatch.getScenario(), initial, batchState);
    	}

    	for (StatefulProcedure procedure : runningBatch.getProcedures()) {
    		procedure.clearListeners();
    		procedure.addListeners(procedureStateListeners);
    		for (ProcedureStateListener listener : procedureStateListeners) {
    			listener.notifyProcedureStateChanged(procedure, initial, procedure.getState());
    		}
    	}

    	for(ScenarioListener scenarioListener : scenarioListeners) {
    		scenarioListener.notifyScenarioChanged(runningBatch.getScenario());
    	}
    }

    /**
     * Start the specified scenario or transfer the current running scenario to a specified one.
     * This method is blocking until all procedures are operating (or timeout is reached).
     *
     * @param scenario the scenario to start or transfer to
     * @author martin.wurzinger
     */
    public void run(Scenario scenario) {
        synchronized (LaunchEngine.class) {
            // use custom settings of the scenario if any exists
            EasyTravelConfig.applyCustomSettings(scenario.getCustomSettings());

            final Batch batch;
            if (runningBatch == null) {
                batch = new Batch(scenario, procedureStateListeners);
            } else if (runningBatch.getScenario().equals(scenario)) {
                LOGGER.info(TextUtils.merge("Unable to start new scenario ''{0}'' because it is already running.", scenario.getTitle()));
                return;
            } else {
                batch = runningBatch.transfer(scenario, procedureStateListeners);
            }
            techActivator.registerBackendListener(batch);
            batch.addBatchStateListeners(batchStateListeners);
            batch.addScenarioListeners(scenarioListeners);

            if (runningBatch != null) {
                try {
                	doStop();
                } catch (IllegalStateException e) {
                    LOGGER.log(Level.WARNING, TextUtils.merge("Unable to stop scenario ''{0}'' because it has an illegal state.", scenario.getTitle()), e);
                }
            }

            try {
            	doStart(batch);
            } catch (IllegalStateException e) {
                LOGGER.log(Level.SEVERE, TextUtils.merge("Unable to run scenario ''{0}'' because it has an illegal state.", scenario.getTitle()), e);
            }
        }
    }

    public void runAsync(final Scenario scenario) throws IllegalStateException {
    	ThreadEngine.createBackgroundThread("Scenario Runner " + scenario.getTitle(), new Runnable() {
            @Override
            public void run() {
            	LaunchEngine.this.run(scenario);
            }
        }, Display.getCurrent()).start();
    }

    public static void stop(final Scenario scenario) throws IllegalStateException {
        synchronized (LaunchEngine.class) {
            if (runningBatch == null) {
                LOGGER.warning(TextUtils.merge("Trying to stop scenario ''{0}''. But currently no scenario is running.", scenario.getTitle()));
                return;
            }

            if (!runningBatch.getScenario().equals(scenario)) {
                LOGGER.warning(TextUtils.merge("Trying to stop scenario ''{0}'' which is not running.", scenario.getTitle()));
                return;
            }

            try {
            	doStop();
            } catch (IllegalStateException e) {
                LOGGER.info(TextUtils.merge("Unable to stop scenario ''{0}''. It is currently not running.", scenario.getTitle()));
            }
        }
    }

    public static void stopAsync(final Scenario scenario) throws IllegalStateException {
    	ThreadEngine.createBackgroundThread("Scenario Stopper " + scenario.getTitle(), new Runnable() {
            @Override
            public void run() {
            	LaunchEngine.stop(scenario);
            }
        }, Display.getCurrent()).start();
    }

    public static void stopAsync() throws IllegalStateException {
    	ThreadEngine.createBackgroundThread("Scenario Stopper", new Runnable() {
            @Override
            public void run() {
            	LaunchEngine.stop();
            }
        }, Display.getCurrent()).start();
    }

    public static void stop() {
		// do not synchronize here as this is a request to stop
		// which can happen while the Batch is still starting up
		// The Batch is built to handle synchronization itself in this case!
		//synchronized (LaunchEngine.class) {
		if (pluginSchedulerEnabled) {
			shutdownQuartz();
		}
		doStop();
		//}
	}

    public void addBatchStateListener(BatchStateListener listener) {
    	batchStateListeners.add(listener);
    }

    public void addProcedureStateListener(ProcedureStateListener listener) {
    	procedureStateListeners.add(listener);
    }

    public void addScenarioListener(ScenarioListener listener) {
    	scenarioListeners.add(listener);
    }

    private static void doStop() {
        if (runningBatch != null) {
            Batch toStop = runningBatch;
            runningBatch = null;
            toStop.stop();
        }
    }

    private static void doStart(Batch toStart) {
    	runningBatch = toStart;
    	toStart.start();
    }

    public static void addProcedure(StatefulProcedure procedure) {
    	if(runningBatch == null) {
    		LOGGER.warning("Cannot add Procedure " + procedure.getName() + " without a running Batch");
    		return;
    	}
    	runningBatch.addProcedure(procedure);
    }

	private static void shutdownQuartz() {
		try {
			Quartz.shutDown();
			LOGGER.log(Level.INFO, TextUtils.merge("Shutdown scheduler {0}", Quartz.getSchedulerInstanceName()));
		} catch (SchedulerException e) {
			LOGGER.log(Level.SEVERE, "Cannot shutdown Quartz Scheduler", e);
		}
	}

	public static Scenario findScenario(String scenarioGroupTitle, String scenarioTitle) {
        ScenarioConfiguration scenarioConfig = new ScenarioConfiguration();
        scenarioConfig.loadOrCreate();

        for (ScenarioGroup group : scenarioConfig.getScenarioGroups()) {
        	if (group.getTitle().equals(scenarioGroupTitle)) {
        		for (Scenario scenario : group.getScenarios()) {
        			if (scenario.getTitle().equals(scenarioTitle)) {
        				return scenario;
        			}
        		}
        	}
        }

        return null;
	}
}
