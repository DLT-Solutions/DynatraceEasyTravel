package com.dynatrace.easytravel.launcher.baseload;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dynatrace.diagnostics.uemload.RandomWalk;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.UEMLoadScenario;
import com.dynatrace.diagnostics.uemload.UemLoadScheduler;
import com.dynatrace.diagnostics.uemload.http.base.HostAvailability;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravel;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelHostManager;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelLauncherScenario;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeListener;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.easytravel.constants.BaseConstants.UrlType;
import com.dynatrace.easytravel.launcher.engine.ProcedureStateListener;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.engine.StatefulProcedure;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.collect.Sets;

import ch.qos.logback.classic.Logger;


public abstract class BaseLoad implements ProcedureStateListener, PluginChangeListener {
	private static final Logger LOGGER = LoggerFactory.make();

	private final Object lock = new Object();

	private final String mainProcedureId;

	private final Simulator simulator;

	private final Set<String> hosts = Sets.newHashSet();

	private double ratio;

	private int value;

	private boolean running;

	private ScheduledFuture<?> changeTask;

	private String loadBalancer;

	private boolean taggedWebRequest;

	private boolean simulatorLogging = true;
	private boolean manual = false;

	private final AtomicBoolean blockScheduling = new AtomicBoolean(false);

	private volatile boolean loadBalancerRunning = false;


	/**
	 *
	 *
	 * @param scenario
	 * @param procedureId
	 * @param ratio
	 * @param taggedWebRequest
	 * @author peter.kaiser
	 */
	protected BaseLoad(EasyTravelLauncherScenario scenario, String procedureId, int value, double ratio, boolean taggedWebRequest) {
		this.simulator = scenario.createSimulator();
		this.mainProcedureId = procedureId;
		this.ratio = ratio;
		this.value = BaseLoadUtil.getLoadNumberPerMinute(value, ratio);
		this.taggedWebRequest = taggedWebRequest;

		PluginChangeMonitor.registerForPluginChanges(this);
	}


	private final Runnable changeRunnable = new Runnable() {

		@Override
		public void run() {
			synchronized (lock)
			{
				if (running)
				{
					if (value <= 0)
					{
						simulator.stop();
					}
					else
					{
						setHostsAvailable();
						getScenario().setLoad(value);
						if (manual)
						{
							simulator.runNow(new RandomWalk(value));
						}
						else
						{
							simulator.run(new RandomWalk(value), /* performWakeup */false, simulatorLogging);
						}
					}
				}
			}
		}
	};

	private void setHostsAvailable() {
		if (loadBalancer != null) {
			HostAvailability.INSTANCE.setAvailable(loadBalancer);
		}
		for (String host : hosts) {
			HostAvailability.INSTANCE.setAvailable(host);
		}
	}

	private void setHostsUnavailable() {
		for (String host : hosts) {
			HostAvailability.INSTANCE.setExpectedUnavailable(host);
		}
		if (loadBalancer != null) {
			HostAvailability.INSTANCE.setExpectedUnavailable(loadBalancer);
		}
	}

	public void setValue(int value) {
		setValue(value, ratio);
	}

	public int getValue() {
		synchronized (lock) {
			return value;
		}
	}

	/**
	 * This method allows you to set a value without considering the set ratio
	 *
	 * @param value
	 * @param ratio
	 * @author stefan.moschinski
	 */
	public void setValue(int value, double ratio) {
		synchronized (lock) {
			manual = false;
			this.value = BaseLoadUtil.getLoadNumberPerMinute(value, ratio);
			if (changeTask != null) {
				changeTask.cancel(false);
			}
			changeTask = UemLoadScheduler.schedule(changeRunnable, 1, TimeUnit.SECONDS);
		}
	}


	public void setManualVisits(int value) {
		synchronized (lock) {
			manual = true;
			this.value = value;
			if (changeTask != null) {
				changeTask.cancel(false);
			}
			changeTask = UemLoadScheduler.schedule(changeRunnable, 1, TimeUnit.SECONDS);
		}
	}

	@Override
	public void notifyProcedureStateChanged(StatefulProcedure subject, State oldState, State newState) {
		String procedureId = subject.getMapping().getId();
		try {
			if (Constants.Procedures.APACHE_HTTPD_ID.equals(procedureId)
					|| Constants.Procedures.APACHE_HTTPD_PHP_ID.equals(procedureId)
					|| Constants.Procedures.NGINX_WEBSERVER_ID.equals(procedureId)) {
				handleWebserver(subject, newState);
			} else if (mainProcedureId.equals(procedureId)) {
				handleMainProcedure(subject, newState);
			} else if (Constants.Procedures.THIRDPARTY_SERVER_ID.equals(procedureId)) {
				handleThirdPartyServer(subject, newState);
			}

			LOGGER.trace(getClass().getSimpleName() + " - Procedure " + procedureId + " state changed to " + newState.toString() + ". Scenario hosts after change: " + getScenario().getHostsManager().getAllHostsAsString());
		} catch (Exception e) {
			LOGGER.warn("Exception in BaseLoad: " + mainProcedureId, e);
		}
	}
	
	protected boolean useAngularFrontend() {
		return false;
	}

	/**
	 *  Mark third party server URL as unavailable when third party server procedure is stopped
	 * @param subject
	 * @param newState
	 */
	private void handleThirdPartyServer(StatefulProcedure subject, State newState) {
		synchronized (lock) {
			String uri = subject.getURI(); // NOPMD
			if (uri == null) {
				if (newState == State.OPERATING || newState == State.TIMEOUT || newState == State.STOPPING) {
					LOGGER.warn("Process " + subject.getName() + " in state " + newState + " has no uri set!");
				}
				return;
			}
			switch (newState) {
				case OPERATING:
				case TIMEOUT:
					HostAvailability.INSTANCE.setAvailable(uri);
					break;
				case STOPPING:
				case STOPPED:
					HostAvailability.INSTANCE.setExpectedUnavailable(uri);
				default:		// nothing to do
			}
		}
	}

	private void handleMainProcedure(StatefulProcedure subject, State newState) {
		synchronized (lock) {

			String uri = subject.getURIDNS(); // NOPMD

			if (uri == null) {
				if (newState == State.OPERATING || newState == State.TIMEOUT || newState == State.STOPPING) {
					LOGGER.warn("Process " + subject.getName() + " in state " + newState + " has no uri set!");
				}
				return;
			}
			switch (newState) {
			case OPERATING:
			case TIMEOUT:
				hosts.add(uri);
				if (!running && loadBalancer == null) {
					HostAvailability.INSTANCE.setAvailable(uri);
					startSimulator(uri);
				} else if (loadBalancer == null) {
					HostAvailability.INSTANCE.setAvailable(uri);
					addHost2Scenario(uri);
					getScenario().init(taggedWebRequest);


					// if the load balancer started, but it did not cause the load simulator to start
					// we have to start it here see JLT-54432
				} else if (loadBalancerRunning && !running) {
					HostAvailability.INSTANCE.setAvailable(uri);
					startSimulator(loadBalancer);
				}
				break;
			case STOPPING:
			case STOPPED:
				removeHost(uri, newState);
			default:		// nothing to do
			}
		}
	}


	private void handleWebserver(StatefulProcedure subject, State newState) {
		synchronized (lock) {
			if (!mainProcedureUsesLoadBalancer(subject)) {
				return;
			}

			String uri = getLoadBalancerURI(subject);
			
            //cwpl-rpsciuk
			//check for null URI. This will happen if the apache is on different host than load generator and following config variables are not set:
			//config.apacheFrontendPublicUrl, config.apacheB2BFrontendPublicUrl
			if (uri == null) {
				if (newState == State.OPERATING || newState == State.TIMEOUT || newState == State.STOPPING) {
					LOGGER.warn("Process " + subject.getName() + " in state " + newState + " has no uri set!");
				}
				return;
			}

			switch (newState) {
				case OPERATING:
				case TIMEOUT:
					HostAvailability.INSTANCE.setAvailable(uri);
					if (running) {
						for (String host : hosts) {
							HostAvailability.INSTANCE.setExpectedUnavailable(host);
							removeHostFromScenario(host);
						}
						addHost2Scenario(uri);
						getScenario().init(taggedWebRequest);
					} else if (!hosts.isEmpty()) {
						startSimulator(uri);
					}
					// we need to keep track whether load balancer is running,
					loadBalancerRunning = true;
				case STARTING: // fall through
					loadBalancer = uri;
					break;
				case STOPPING:
				case STOPPED:
					loadBalancerRunning = false;
					loadBalancer = null;
					for (String host : hosts) {
						addHost2Scenario(host);
						HostAvailability.INSTANCE.setAvailable(host);
					}
					removeHost(uri, newState);
				default:	// nothing to do
			}

			LOGGER.trace("Webserver state changed. New state: " + newState.toString());
		}
	}
	
	private boolean mainProcedureUsesLoadBalancer(StatefulProcedure subject) {
		return !mainProcedureId.equals(Constants.Procedures.BUSINESS_BACKEND_ID);
	}
	
	private String getLoadBalancerURI(StatefulProcedure subject) {
		String uri = null;
		if (mainProcedureId.equals(Constants.Procedures.CUSTOMER_FRONTEND_ID)) {
            if (subject.isNginxWebserver()) {
                uri = subject.getURIDNS(UrlType.NGINX_JAVA_FRONTEND); // NOPMD
            } else {
                uri = subject.getURIDNS(UrlType.APACHE_JAVA_FRONTEND); // NOPMD
            }
        } else if(mainProcedureId.equals(Constants.Procedures.ANGULAR_FRONTEND_ID)) {
        	if (subject.isNginxWebserver()) {
        		uri = subject.getURIDNS(UrlType.NGINX_ANGULAR_FRONTEND); // NOPMD
        	} else {
            	uri = subject.getURIDNS(UrlType.APACHE_ANGULAR_FRONTEND); // NOPMD
            }
        }
        else {
            if (subject.isNginxWebserver()) {
                uri = subject.getURIDNS(UrlType.NGINX_B2B_FRONTEND); // NOPMD
            } else {
            	uri = subject.getURIDNS(UrlType.APACHE_B2B_FRONTEND); // NOPMD
            }
        }
		return uri;
	}
		
	private void removeHost(String host, State newState) {
		HostAvailability.INSTANCE.setExpectedUnavailable(host);
		hosts.remove(host);
		//cwpl-rpsciuk, APM-8129: stop simulator if there are no real hosts running
		//even if loadbalancer is still running		 
		if (hosts.isEmpty() && running) {
			String hostString = loadBalancerRunning ? loadBalancer : host;
			LOGGER.warn("stopping simulator with host: " + hostString);
			simulator.stop();
			running = false;
		}
		removeHostFromScenario(host);
		getScenario().init(taggedWebRequest);
	}

	private void startSimulator(String host) {
		PluginChangeMonitor.registerForPluginChanges(this); //register it since it could be deregistered in stop method
		addHost2Scenario(host);
		getScenario().init(taggedWebRequest);
		running = true; // else later switches from manual to automatic mode would not work
		if (blockScheduling.get()) {
			return;
		}
		LOGGER.info(TextUtils.merge("{0} - Starting simulator with host: {1}", getClass().getSimpleName(), host));
		if (value > 0) {
			simulator.run(new RandomWalk(value, value), false, simulatorLogging);
		}
	}

	/**
	 * Start simulator on all hosts.
	 */
	protected void startSimulator() {
		synchronized (lock) {
			if (running || manualVisitsEnabled() || blockScheduling.get()) { //simulator is running or manual visits enabled or scheduling blocked

				return;
			}

			if (!getScenario().hasHosts() || value<=0) { //no hosts or traffic disabled
				return;
			}

			LOGGER.info("Starting simulator on hosts: " + hosts);
			disableBlocking();
			setHostsAvailable();
			simulator.run(new RandomWalk(value, value), false, simulatorLogging);
		}
	}

	/**
	 * @return true if manual visits are enabled
	 */
	protected boolean manualVisitsEnabled() {
		return manual;
	}

	public void setTaggeWebRequest(boolean taggedWebRequest) {
		this.taggedWebRequest = taggedWebRequest;
		EasyTravel.setTaggedWebRequest(taggedWebRequest);
	}


	protected abstract void addHost2Scenario(String host);


	protected abstract void removeHostFromScenario(String host);


	protected abstract boolean hasHost();

	//made public for tests
	public boolean isTaggedWebRequest() {
		return taggedWebRequest;
	}

	protected boolean isSimulatorLogging() {
		return simulatorLogging;
	}

	protected void setSimulatorLoggingOn() {
		this.simulatorLogging = true;
	}

	protected void setSimulatorLoggingOff() {
		this.simulatorLogging = false;
	}

	protected double getRatio() {
		return ratio;
	}

	protected void setRatio(double ratio) {
		this.ratio = ratio;
	}

	/**
	 * Stop simulator. This is used by b2b load generator to stop simulator when b2b frontend runs on IIS and cannot be stopped. See APM-8129
	 */
	public void stopSimulator() {
		stop(blockScheduling.get());
		running = false;
	}

	public boolean stop(boolean blockFutureSchedules) {
		// The hosts are set to unavailable as well as default
		return stop(blockFutureSchedules, true);
	}

	public boolean stop(boolean blockFutureSchedules, boolean setHostsUnavailable) {
		PluginChangeMonitor.unregisterFromPluginChanges(this);

		if (setHostsUnavailable) {
			setHostsUnavailable();
		}
		blockScheduling.set(blockFutureSchedules);
		return this.simulator.stop(false);
	}

	public void disableBlocking() {
		blockScheduling.set(false);
	}

	public boolean isSchedulingBlocked() {
		return blockScheduling.get();
	}

	@Override
	public void pluginsChanged() {
		getScenario().init(taggedWebRequest);
	}

	public synchronized EasyTravelLauncherScenario getScenario() {
		return (EasyTravelLauncherScenario) simulator.getScenario();
	}


	public synchronized void setScenario(EasyTravelLauncherScenario newScenario) {
		synchronized (lock) {
			if (newScenario instanceof UEMLoadScenario) {
				//add all known hosts to the scenario
				updateScenarioHosts(newScenario);
				newScenario.init(isTaggedWebRequest());
				simulator.updateScenario((UEMLoadScenario) newScenario);
			}
		}
	}

	private void updateScenarioHosts(EasyTravelLauncherScenario newScenario) {
		EasyTravelHostManager manager = getScenario().getHostsManager();
		EasyTravelHostManager newManager = newScenario.getHostsManager();

		for (String host : manager.getB2bFrontendHosts()) {
			newManager.addB2BFrontendHost(host);
		}

		for (String host : manager.getCustomerFrontendHosts()) {
			newManager.addCustomerFrontendHost(host);
		}
	}
}
