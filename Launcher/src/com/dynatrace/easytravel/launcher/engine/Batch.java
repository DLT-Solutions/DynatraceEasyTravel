package com.dynatrace.easytravel.launcher.engine;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.procedures.DummyProcedure;
import com.dynatrace.easytravel.launcher.procedures.utils.TechnologyActivatorListener;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.launcher.sync.Predicate;
import com.dynatrace.easytravel.launcher.sync.PredicateMatcher;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.util.TextUtils;


/**
 * <p>
 * A class that represents all {@link Procedure}s of a {@link Scenario}. It is able to start and stop multiple
 * <code>Procedure</code>s. Instances are stateful (see {@link State}) and {@link BatchStateListener}s are notified on batch state
 * change events.
 * </p>
 *
 * @author martin.wurzinger
 */
public class Batch implements TechnologyActivatorListener {

	private static final Logger log = Logger.getLogger(Batch.class.getName());
	private static final ProcedureFactory FACTORY = new ProcedureFactory();

	private final CopyOnWriteArrayList<StatefulProcedure> procedures = new CopyOnWriteArrayList<StatefulProcedure>();
	private final CopyOnWriteArrayList<BatchStateListener> batchStateListeners = new CopyOnWriteArrayList<BatchStateListener>();
	private final CopyOnWriteArrayList<ScenarioListener> scenarioListeners = new CopyOnWriteArrayList<ScenarioListener>();
	private final List<ProcedureStateListener> procedureStateListeners = new CopyOnWriteArrayList<ProcedureStateListener>();
	private volatile State state = State.getDefault();
	private boolean isInitialized = false;

	private final Scenario scenario;

	public Batch(Scenario scenario, List<ProcedureStateListener> procedureStateListeners) {
		this.scenario = scenario;
		this.procedureStateListeners.addAll(procedureStateListeners);
	}

	private void registerProcedures() {
		registerAndReuseProcedures(this, Collections.<StatefulProcedure> emptyList());
	}

	private static void registerAndReuseProcedures(Batch targetBatch, List<StatefulProcedure> existingProcedures) {
		if (targetBatch.isInitialized) {
			// procedures already initialized
			return;
		}
		targetBatch.isInitialized = true;

		List<StatefulProcedure> resultingProcedures = new ArrayList<StatefulProcedure>();
//		List<ProcedureMapping> mappings = new ArrayList<ProcedureMapping>(getFilteredProcedureMappings(targetBatch));
		List<ProcedureMapping> mappings = new ArrayList<ProcedureMapping>(targetBatch.scenario.getProcedureMappings(DtVersionDetector.getInstallationType()));

		for (ProcedureMapping mapping : mappings) {

			boolean transferableProcedureFound = false;
			for (StatefulProcedure procedure : existingProcedures) {

				if (procedure.isTransferableTo(mapping)) {
					log.fine("Reusing existing procedure: " + procedure.getName());

					// clear old listeners
					procedure.clearListeners();
					procedure.addListeners(targetBatch.procedureStateListeners);
					procedure.transfer(mapping);

					// re-establish the dependency between procedures if we reuse some of them
					handleDependingProcedures(procedure, existingProcedures);
					handleDependingProcedures(procedure, resultingProcedures);

					// in this case the procedure is transferable
					registerProcedure(procedure, resultingProcedures, targetBatch);

					// remove procedure from list of remaining contemplating procedures
					existingProcedures.remove(procedure);		// NOSONAR - CopyOnWriteArrayList allows to do this and does not allow to remove() on the iterator!

					transferableProcedureFound = true;
					break;
				}
			}
			if (transferableProcedureFound) {
				continue;
			}

			StatefulProcedure statefulProcedure = createStatefulProcedure(mapping);
			if (statefulProcedure == null) {
				log.log(Level.WARNING, TextUtils.merge("Unable to instantiate procedure mapped to ID ''{0}''.",
						mapping == null ? "<null>" : mapping.getId()));
				continue;
			}

			statefulProcedure.addListeners(targetBatch.procedureStateListeners);
			registerProcedure(statefulProcedure, resultingProcedures, targetBatch);
		}
	}

	private static void registerProcedure(StatefulProcedure procedure, List<StatefulProcedure> resultingProcedures,
			Batch targetBatch) {
		resultingProcedures.add(procedure);
		targetBatch.procedures.add(procedure);
	}

	private static StatefulProcedure createStatefulProcedure(ProcedureMapping mapping) {
		Procedure procedure = FACTORY.create(mapping);

		// if we cannot create the procedure, add a dummy procedure which is already in state FAILED
		if (procedure == null) {
			if(mapping == null) {
				return null;
			}
			procedure = new DummyProcedure(mapping, Feedback.Failure, "Could not create procedure, probably some component is not available, check log for details.");
		}

		return StatefulProcedureFactory.newInstance(procedure);
	}

	public Batch transfer(Scenario newScenario, List<ProcedureStateListener> procedureStateListeners) {
		Batch batch = new Batch(newScenario, procedureStateListeners);
		registerAndReuseProcedures(batch, this.procedures);
		return batch;
	}

	/**
	 * Start the batch and wait until all procedures are operating.
	 *
	 * @throws IllegalStateException if the batch has not a runnable state - the batch might has
	 *         already be started
	 * @author martin.wurzinger
	 */
	public void start() throws IllegalStateException {
		if (state != State.STOPPED) {
			throw new IllegalStateException(TextUtils.merge(
					"The batch can only be started if it''s in state {0}. Current State is {1}", State.STOPPED, state));
		}
		notifyScenarioListener(this.scenario);

		long startTime = System.currentTimeMillis();

		registerProcedures();

		setState(State.STARTING);
		log.info(TextUtils.merge("Starting scenario ''{0}'' of group ''{1}''...", scenario.getTitle(), scenario.getGroup()));
		// -------------------------

		boolean areAllProceduresStarted = startProcedures();

		// stop if we were interrupted with a shutdown while still starting up
		if (shouldStop()) {
			log.info(TextUtils.merge("Starting scenario ''{0}'' of group ''{1}'' was stopped", scenario.getTitle(),
					scenario.getGroup()));
			return;
		}

		// if some procedures did not start, still ask all procedures for their state once to show them as "Starting/Operating"
		if (!areAllProceduresStarted) {
			for (StatefulProcedure procedure : procedures) {
				if (!procedure.isEnabled() || !procedure.isOperatingCheckSupported()) {
					continue;
				}

				// Note: this will also adjust state of the procedure
				procedure.isStartingFinished();
			}
		}

		log.info("Finished starting the Batch, now waiting for " + procedures.size() + " procedures to be up and running");

		// only wait for all procedures, if all of them could be started successfully
		boolean areAllProceduresOperating = areAllProceduresStarted && waitUntilOperating(); // ATTENTION: short-circuit AND

		log.info("Done waiting for " + procedures.size() + " procedures");

		// stop if we were interrupted with a shutdown while still starting up
		if (shouldStop()) {
			log.info(TextUtils.merge("Startup of scenario ''{0}'' of group ''{1}'' was stopped", scenario.getTitle(),
					scenario.getGroup()));
			return;
		}

		// -------------------------
		setState(State.OPERATING);

		// check if we have any invalid duplicates
		checkDuplicatePortsAndURIs();

		if (areAllProceduresOperating) {
			log.info("All easyTravel procedures started successfully in " + (System.currentTimeMillis() - startTime) + "ms.");
		} else {
			log.warning("Not all easyTravel procedures could be started in expected time after " +
					(System.currentTimeMillis() - startTime) + "ms.");
		}
	}

	protected void checkDuplicatePortsAndURIs() {
		Set<Integer> ports = new HashSet<Integer>();
		Set<String> urls = new HashSet<String>();
		for(StatefulProcedure proc : procedures) {
			if(proc.isWebProcedure()) {
				int port = ((WebProcedure)proc.getDelegate()).getPort();
				log.info(proc.getName() + ": Having Port: " + port);

				if(!ports.add(port)) {
					log.warning("Port " + port + " is used by more than one procedure, found it in " + proc.getName());
				}
			}

			String uri = proc.getURI();
			log.info(proc.getName() + ": Having URI: " + uri);
			if(uri != null && !urls.add(uri)) {
				log.warning("URI " + uri + " is used by more than one procedure, found it in " + proc.getName());
			}
		}
	}

	private void notifyScenarioListener(Scenario scenario) {
		for (ScenarioListener listener : scenarioListeners) {
			listener.notifyScenarioChanged(scenario);
		}
	}

	private void setState(State newState) {
		State oldState = state;
		state = newState;

		if (oldState != newState) {
			fireBatchStateChanged(oldState, newState);
		}
	}

	private boolean startProcedures() {
		boolean allProceduresStartedSuccessfully = true;

		for (StatefulProcedure procedure : this.procedures) {
			
			if(startScenarioWithStoppedProcedure(procedure)){
				continue;
			}
			
			// stop if we were interrupted with a shutdown while still starting up
			if (shouldStop()) {
				return true;
			}

			if (!procedure.isEnabled()) {
				logStartProcedureDisabled(procedure);
				continue;
			}

			// if there are procedures that we depend on, then connect the listeners correctly
			handleDependingProcedures(procedure, this.procedures);

			logStartProcedure(procedure);

			if (state != State.STARTING) {
				log.warning("Stop starting procedures because starting was canceled.");
				return false;
			}

			allProceduresStartedSuccessfully &= procedure.run().isOk();
		}

		return allProceduresStartedSuccessfully;
	}
	
	private boolean startScenarioWithStoppedProcedure(StatefulProcedure procedure){
		if(procedure.getTechnology() != null && procedure.getTechnology().equals(Technology.VAGRANT)){
			String disableStartupProperty = procedure.getMapping().getSettingValue("procedure_config", "config.disableProcedureStartup");
			
			if(StringUtils.isNotBlank(disableStartupProperty)){
				return true;
			}
		}
		return false;
	}

	private boolean startTechnology(Technology technology) {
		boolean allProceduresStartedSuccessfully = true;

		for (StatefulProcedure procedure : this.procedures) {
			if (procedure.getTechnology() != technology) {
				continue;
			}

			// stop if we were interrupted with a shutdown while still starting up
			if (shouldStop()) {
				return true;
			}

			if (!procedure.isEnabled()) {
				logStartProcedureDisabled(procedure);
				continue;
			}

			// if there are procedures that we depend on, then connect the listeners correctly
			handleDependingProcedures(procedure, this.procedures);

			logStartProcedure(procedure);

			allProceduresStartedSuccessfully &= procedure.run().isOk();
		}

		return allProceduresStartedSuccessfully && waitUntilOperating();
	}

	private static void handleDependingProcedures(StatefulProcedure procedure, List<StatefulProcedure> procedures) {
		List<String> dependingProcedureIDs = procedure.getDependingProcedureIDs();
		if (dependingProcedureIDs != null && dependingProcedureIDs.size() > 0) {
			logStartProcedureDeferred(procedure, dependingProcedureIDs);

			// connect listeners for all depending procedure ids with the current procedure as "listener"
			for (String id : dependingProcedureIDs) {
				// search for depending procedures
				for (StatefulProcedure depending : procedures) {
					if (id.equals(depending.getMapping().getId())) {
						depending.addListener(procedure);

						// notify the depending procedure about the current state
						// procedure.notifyProcedureStateChanged(depending, depending.getState(), depending.getState());
					}
				}
			}
		}
	}

	private boolean shouldStop() {
		return State.STOPPED.equals(state) || State.STOPPING.equals(state);
	}

	public void stop() {
		if (state != State.OPERATING && state != State.TIMEOUT && state != State.STARTING) {
			throw new IllegalStateException(TextUtils.merge(
					"The batch can only be stopped if it is in state {0}, {1} or {2}. Current State is {3}", State.OPERATING,
					State.STARTING, State.TIMEOUT, state));
		}

		// use BatchStopper to shutdown procedures in multiple threads if possible
		setState(State.STOPPING);
		BatchStopper stopper = new BatchStopper(procedures);
		stopper.execute();
		setState(State.STOPPED);

		// inform scenario state listeners that we do not have a scenario running any more
		notifyScenarioListener(null);
	}

	public void stopTechnology(Technology technology) {
		Stopper stopper = new TechnologyStopper(procedures, technology);
		stopper.execute();
	}


	public Scenario getScenario() {
		return scenario;
	}

	/**
	 * Notify all procedures that batch has changed state.
	 *
	 * @param oldState the old state of batch
	 * @param newState the new state of batch
	 * @author martin.wurzinger
	 */
	private void fireBatchStateChanged(State oldState, State newState) {
		for (BatchStateListener listener : batchStateListeners) {
			try {
				listener.notifyBatchStateChanged(scenario, oldState, newState);
			}
			catch (Exception ex) {
				log.log(Level.SEVERE, "There was an exception when calling notifyBatchStateChanged", ex);
			}
		}
	}

	private static void logStartProcedure(Procedure procedure) {
		log.fine(TextUtils.merge("Running {0}...", procedure.getName()));
	}

	private static void logStartProcedureDisabled(Procedure procedure) {
		log.fine(TextUtils.merge("{0} is disabled...", procedure.getName()));
	}

	private static void logStartProcedureDeferred(Procedure procedure, List<String> depending) {
		log.fine(TextUtils.merge("{0} is listening on state changes of procedures {1} ...", procedure.getName(),
				depending.toString()));
	}

	private boolean waitUntilOperating() {
		final EasyTravelConfig config = EasyTravelConfig.read();

		informDepending();

		NoStartingPredicate nonStartingPredicate = new NoStartingPredicate(this.procedures);
		
		// allow one timeout period for each procedure that is not yet started
		int timeout = config.syncProcessTimeoutMs * nonStartingPredicate.getNotOperatingProcedures().size();
		
		for (StatefulProcedure proc : this.procedures){
			if(proc.getTechnology() != null && proc.getTechnology().equals(Technology.VAGRANT)){
				timeout = timeout * 5;
				break;
			}
		}

		PredicateMatcher<Object> matcher = new PredicateMatcher<Object>(null, timeout, config.processOperatingCheckIntervalMs);

		boolean areAllOperating = matcher.waitForMatch(nonStartingPredicate);

		// shortcut if we were interrupted by a shutdown while still waiting for startup
		if (shouldStop()) {
			return false;
		}

		if (!areAllOperating) {
			for (Procedure procedure : nonStartingPredicate.getNotOperatingProcedures()) {
				log.warning(TextUtils.merge("Unable to wait until {0} is operating. Timeout of {1} ms reached.",
						procedure.getName(), timeout));

				// indicate timeout in the UI as well
				((StatefulProcedure)procedure).setState(State.TIMEOUT);
			}
		}

		return areAllOperating;
	}

	public void informDepending() {
		for (StatefulProcedure procedure : procedures) {
			List<String> dependingProcedureIDs = procedure.getDependingProcedureIDs();
			if (dependingProcedureIDs != null && dependingProcedureIDs.size() > 0) {
				// connect listeners for all depending procedure ids with the current procedure as "listener"
				for (String id : dependingProcedureIDs) {
					StatefulProcedure found = null;
					// search for depending procedures
					for (StatefulProcedure depending : procedures) {
						if (id.equals(depending.getMapping().getId()) && depending.hasState(State.OPERATING)) {
							found = depending;
						}
					}

					// notify the depending procedure about the current state
					if (found != null) {
						procedure.notifyProcedureStateChanged(found, found.getState(), found.getState());
					}
				}
			}
		}
	}

	/**
	 * Stateful predicate that hold a collection of procedures that are in state starting. If during a
	 * predicate evaluation a procedure is detected to be operating, stopping or not running, it will be removed from the
	 * collection.
	 *
	 * @author martin.wurzinger
	 */
	private final class NoStartingPredicate implements Predicate<Object> {

		private final Collection<StatefulProcedure> startingProcedures = new ArrayList<StatefulProcedure>();

		public NoStartingPredicate(Collection<StatefulProcedure> procedures) {
			for (StatefulProcedure procedure : procedures) {
				// ignore disabled procedure, procedures where we can not check and procedures that are already Operating (e.g. transfer)
				if (!procedure.isEnabled() ||
						!procedure.isOperatingCheckSupported() ||
						procedure.hasState(State.OPERATING) ||
						procedure.hasState(State.TIMEOUT)) {
					continue;
				}

				this.startingProcedures.add(procedure);
			}
		}

		@Override
		public boolean eval(Object unused) {
			Iterator<StatefulProcedure> iterator = startingProcedures.iterator();

			boolean isNoOneStarting = true;

			while (iterator.hasNext()) {
				StatefulProcedure procedure = iterator.next();

				// Note: this will also adjust state of the procedure
				if (procedure.isStartingFinished()) {
					iterator.remove();
				} else {
					log.info("Procedure " + procedure.getName() + " is still not operating");
					isNoOneStarting = false;
				}
			}
			return isNoOneStarting;
		}

		/**
		 * @return a collection of procedures that have not been detected yet to be not operating.
		 * @author martin.wurzinger
		 */
		public Collection<StatefulProcedure> getNotOperatingProcedures() {
			return new ArrayList<StatefulProcedure>(startingProcedures);
		}

		@Override
		public boolean shouldStop() {
			// indicate waiting predicates should stop waiting if the overall Batch
			// was stopped while we are still starting up
			return Batch.this.shouldStop();
		}
	}

	public void addBatchStateListeners(Collection<BatchStateListener> batchStateListeners) {
		this.batchStateListeners.addAllAbsent(batchStateListeners);
	}

	public void addScenarioListeners(Collection<ScenarioListener> scenarioListeners) {
		this.scenarioListeners.addAllAbsent(scenarioListeners);
	}


	public State getState() {
		return state;
	}


	public List<StatefulProcedure> getProcedures() {
		return procedures;
	}


	public List<BatchStateListener> getBatchStateListeners() {
		return batchStateListeners;
	}


	public List<ProcedureStateListener> getProcedureStateListeners() {
		return procedureStateListeners;
	}

	public void addProcedure(StatefulProcedure procedure) {
		procedures.add(procedure);
		procedure.addListeners(procedureStateListeners);
	}

	@Override
	public void notifyTechnologyStateChanged(Technology technology, boolean enabled, Collection<String> plugins, Collection<String> substitutes) {
		DefaultProcedureMapping businessBackend = null;
		for (Procedure proc : procedures) {
			if (Constants.Procedures.BUSINESS_BACKEND_ID.equalsIgnoreCase(proc.getMapping().getId())) {
				businessBackend = (DefaultProcedureMapping) proc.getMapping();
			}
		}
		if (businessBackend == null) {
			return;
		}

		if (enabled) {
			startTechnology(technology);
		} else {
			stopTechnology(technology);
		}
	}
}
