package com.dynatrace.easytravel.launcher.baseload;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.diagnostics.uemload.LoadTimeWatcher;
import com.dynatrace.diagnostics.uemload.UemLoadScheduler;
import com.dynatrace.diagnostics.uemload.headless.DriverEntryPoolSingleton;
import com.dynatrace.diagnostics.uemload.headless.MobileDriverEntryPoolSingleton;
import com.dynatrace.diagnostics.uemload.iot.car.DynatraceRentalCar;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelCustomer;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelLauncherScenario;
import com.dynatrace.easytravel.config.ConfigChangeListener;
import com.dynatrace.easytravel.config.CustomerTrafficScenarioEnum;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.HeadlessTrafficScenarioEnum;
import com.dynatrace.easytravel.launcher.baseload.UEMLoadModificator.UEMLoadModificatorType;
import com.dynatrace.easytravel.launcher.engine.BatchStateListener;
import com.dynatrace.easytravel.launcher.engine.ProcedureStateListener;
import com.dynatrace.easytravel.launcher.engine.ScenarioListener;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.engine.StatefulProcedure;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.panels.HeaderPanelInterface;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.util.process.HeadlessProcessKillerFactory;

public class BaseLoadManager implements LoadController, ScenarioListener, ConfigChangeListener, BatchStateListener, ProcedureStateListener {
	private static final Logger log = Logger.getLogger(BaseLoadManager.class.getName());

	private static final BaseLoadManager INSTANCE = new BaseLoadManager();

	private B2BBaseLoad b2bLoadController;
	private BaseLoad customerLoadController;
	private MobileBaseLoad mobileNativeLoadController;
	private MobileBrowserBaseLoad mobileBrowserLoadController;
	private HotDealBaseLoad hotDealLoadController;
	private IotDevicesBaseLoad iotDevicesLoadController;
	private HeadlessCustomerBaseLoad headlessCustomerLoadController;
	private HeadlessAngularBaseLoad headlessAngularLoadController;
	private HeadlessMobileAngularBaseLoad headlessMobileAngularLoadController;
	private HeadlessB2BBaseLoad headlessB2BLoadController;

	private HeaderPanelInterface headerPanel;

	private volatile UEMLoadModificator loadModificator = new UEMLoadConfigWatcher(this);

	static {
		EasyTravelConfig.addConfigChangeListener(INSTANCE);
	}

	private BaseLoadManager() {
	}

	public static BaseLoadManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Depending on the config.enableFixedBaseLoad property in easyTravelConfig.property file, method changes
	 * EasyTravelCustomer default load scenario to EasyTravelFixedCustomer load scenario
	 *
	 * @param value
	 * @param ratio
	 * @param taggedWebRequest
	 * @return CustomerBaseLoad instance when the fixedBaseLoad=false
	 * @return CustomerFixedBaseLoad instance when the fixedBaseLoad=true
	 */
	public BaseLoad getCustomerBaseLoadInstance(CustomerTrafficScenarioEnum customerTrafficScenario, int value, double ratio,
			boolean taggedWebRequest) {
		if (customerLoadController == null) {
			customerLoadController = new CustomerBaseLoad(customerTrafficScenario, value, ratio, taggedWebRequest);
		}
		return customerLoadController;
	}

	public boolean isDefaultCustomerLoadEnabled() {
		return getCustomerLoadController().getScenario() instanceof EasyTravelCustomer;
	}

	public void updateCustomerBaseLoadScenario(CustomerTrafficScenarioEnum customerTrafficScenario) {
		if (customerLoadController != null) {
			EasyTravelLauncherScenario newScenario = CustomerBaseLoad.createScenario(customerTrafficScenario, customerLoadController.getValue(), customerLoadController.getRatio());
			customerLoadController.setScenario(newScenario);
		}
	}

	public B2BBaseLoad getB2BBaseLoadInstance(int value, double ratio, boolean taggedWebRequest) {
		if (b2bLoadController == null) {
			b2bLoadController = new B2BBaseLoad(value, ratio, taggedWebRequest);
		}
		return b2bLoadController;
	}

	public MobileBaseLoad getMobileNativeBaseLoadInstance(int value, double ratio, boolean taggedWebRequest) {
		if (mobileNativeLoadController == null) {
			mobileNativeLoadController = new MobileBaseLoad(value, ratio, taggedWebRequest);
		}
		return mobileNativeLoadController;
	}

	public MobileBrowserBaseLoad getMobileBrowserBaseLoadInstance(int value, double ratio, boolean taggedWebRequest) {
		if (mobileBrowserLoadController == null) {
			mobileBrowserLoadController = new MobileBrowserBaseLoad(value, ratio, taggedWebRequest);
		}
		return mobileBrowserLoadController;
	}

	public HotDealBaseLoad getHotDealBaseLoadInstance(int value, double ratio, boolean taggedWebRequest) {
		if (hotDealLoadController == null) {
			hotDealLoadController = new HotDealBaseLoad(value, ratio, taggedWebRequest);
		}
		return hotDealLoadController;
	}

	public IotDevicesBaseLoad getIotDevicesBaseLoadInstance(int value, double ratio, boolean taggedWebRequest) {
		if(iotDevicesLoadController == null && DynatraceRentalCar.isConfigSet()) {
			iotDevicesLoadController = new IotDevicesBaseLoad(value, ratio, taggedWebRequest);
		}
		return iotDevicesLoadController;
	}

	public HeadlessCustomerBaseLoad getHeadlessCustomerBaseLoadInstance(HeadlessTrafficScenarioEnum headlessTrafficScenario, int value, double ratio) {
		if (headlessCustomerLoadController == null) {
			headlessCustomerLoadController = new HeadlessCustomerBaseLoad(headlessTrafficScenario, value, ratio);
		}
		return headlessCustomerLoadController;
	}

	public HeadlessAngularBaseLoad getHeadlessAngularBaseLoadInstance(HeadlessTrafficScenarioEnum headlessTrafficScenario, int value, double ratio) {
		if(headlessAngularLoadController == null) {
			headlessAngularLoadController = new HeadlessAngularBaseLoad(headlessTrafficScenario, value, ratio);
		}
		return headlessAngularLoadController;
	}

	public HeadlessMobileAngularBaseLoad getHeadlessMobileAngularBaseLoadInstance(int value, double ratio) {
		if(headlessMobileAngularLoadController == null) {
			headlessMobileAngularLoadController = new HeadlessMobileAngularBaseLoad(value, ratio);
		}
		return headlessMobileAngularLoadController;
	}
	
	public HeadlessB2BBaseLoad getHeadlessB2BBaseLoadInstance(int value, double ratio, boolean taggedWebRequest) {
		if(headlessB2BLoadController == null) {
			headlessB2BLoadController = new HeadlessB2BBaseLoad(value, ratio, taggedWebRequest);
		}
		return headlessB2BLoadController;
	}

	public void setCustomerBaseLoad(int value) {
		if (customerLoadController != null) {
			customerLoadController.setValue(value);
		}
	}

	public void setB2bBaseLoad(int value) {
		if (b2bLoadController != null) {
			b2bLoadController.setValue(value);
		}
	}

	public void setMobileNativeBaseLoad(int value) {
		if (mobileNativeLoadController != null) {
			mobileNativeLoadController.setValue(value);
		}
	}

	public void setMobileBrowserBaseLoad(int value) {
		if (mobileBrowserLoadController != null) {
			mobileBrowserLoadController.setValue(value);
		}
	}

	public void setIotDevicesBaseLoad(int value) {
		if (iotDevicesLoadController != null) {
			iotDevicesLoadController.setValue(value);
		}
	}

	public void setHeadlessCustomerBaseLoad(int value) {
		if (headlessCustomerLoadController != null) {
			headlessCustomerLoadController.setValue(value);
		}
	}

	public void setHeadlessAngularBaseLoad(int value) {
		if (headlessAngularLoadController != null) {
			headlessAngularLoadController.setValue(value);
		}
	}

	public void setHeadlessMobileAngularBaseLoad(int value) {
		if (headlessMobileAngularLoadController != null) {
			headlessMobileAngularLoadController.setValue(value);
		}
	}
	
	public void setHeadlessB2BBaseLoad(int value) {
		if (headlessB2BLoadController != null) {
			headlessB2BLoadController.setValue(value);
		}
	}

	public void setCustomerVisits(int visits) {
		if (customerLoadController != null) {
			customerLoadController.setManualVisits(visits);
		}
	}

	public void setB2bVisits(int visits) {
		if (b2bLoadController != null) {
			b2bLoadController.setManualVisits(visits);
		}
	}

	public void setHeadlessCustomerVisits(int visits) {
		if(headlessCustomerLoadController != null) {
			headlessCustomerLoadController.setManualVisits(visits);
		}
	}

	public void setHeadlessAngularVisits(int visits) {
		if(headlessAngularLoadController != null) {
			headlessAngularLoadController.setManualVisits(visits);
		}
	}

	public void setHeadlessMobileAngularVisits(int visits) {
		if(headlessMobileAngularLoadController != null) {
			headlessMobileAngularLoadController.setManualVisits(visits);
		}
	}

	public void setMobileNativeVisits(int visits) {
		if (mobileNativeLoadController != null) {
			mobileNativeLoadController.setManualVisits(visits);
		}
	}

	public void setMobileBrowserVisit(int visits) {
		if (mobileBrowserLoadController != null) {
			mobileBrowserLoadController.setManualVisits(visits);
		}
	}
	
	public void setHeadlessB2BVisits(int visits) {
		if(headlessB2BLoadController != null) {
			headlessB2BLoadController.setManualVisits(visits);
		}
	}

	public void setHeaderPanelInterface(HeaderPanelInterface headerListener) {
		this.headerPanel = headerListener;
	}

	public boolean stopCustomerLoad() {
		return stop(customerLoadController, false);
	}

	public boolean stopB2bLoad() {
		return stop(b2bLoadController, false);
	}

	public boolean stopMobileNativeLoad() {
		return stop(mobileNativeLoadController, false);
	}

	public boolean stopMobileBrowserLoad() {
		return stop(mobileBrowserLoadController, false);
	}

	public boolean stopIotDevicesLoad() {
		return stop(iotDevicesLoadController, false);
	}

	public boolean stopHeadlessCustomerLoad() {
		return stop(headlessCustomerLoadController, false);
	}

	public boolean stopHeadlessAngularLoad() {
		return stop(headlessAngularLoadController, false);
	}

	public boolean stopHeadlessMobileAngularLoad() {
		return stop(headlessMobileAngularLoadController, false);
	}
	
	public boolean stopHeadlessB2BLoad() {
		return stop(headlessB2BLoadController, false);
	}

	public boolean stopCustomerLoadAndBlock() {
		return stop(customerLoadController, true);
	}

	public boolean stopB2bLoadAndBlock() {
		return stop(b2bLoadController, true);
	}

	public boolean stopMobileNativeLoadAndBlock() {
		return stop(mobileNativeLoadController, true);
	}

	public boolean stopMobileBrowserLoadAndBlock() {
		return stop(mobileBrowserLoadController, true);
	}

	public boolean stopIotDevicesLoadAndBlock() {
		if(iotDevicesLoadController != null) {
			return stop(iotDevicesLoadController, true);
		}
		return true;
	}

	public boolean stopHeadlessCustomerLoadAndBlock() {
		return stop(headlessCustomerLoadController, true);
	}

	public boolean stopHeadlessAngularLoadAndBlock() {
		return stop(headlessAngularLoadController, true);
	}

	public boolean stopHeadlessMobileAngularLoadAndBlock() {
		return stop(headlessMobileAngularLoadController, true);
	}
	
	public boolean stopHeadlessB2BLoadAndBlock() {
		return stop(headlessB2BLoadController, true);
	}

	private boolean stop(BaseLoad inst, boolean blockFutureSchedules) {
		if (inst == null) {
			return true;
		}
		return inst.stop(blockFutureSchedules, false);
	}

	public boolean stopAll() {
		// one & is intentionally to prevent short circuit evaluation so that all methods get executed
		return stopCustomerLoad() & stopB2bLoad() & stopMobileNativeLoad() & stopMobileBrowserLoad() & stopIotDevicesLoad() & stopHeadlessCustomerLoad() & stopHeadlessAngularLoad() & stopHeadlessMobileAngularLoad() & stopHeadlessB2BLoad();
	}

	public void removeScheduleBlocking() {
		if (customerLoadController != null) {
			customerLoadController.disableBlocking();
		}
		if (b2bLoadController != null) {
			b2bLoadController.disableBlocking();
		}
		if (mobileNativeLoadController != null) {
			mobileNativeLoadController.disableBlocking();
		}
		if (mobileBrowserLoadController != null) {
			mobileBrowserLoadController.disableBlocking();
		}
		if (iotDevicesLoadController != null) {
			iotDevicesLoadController.disableBlocking();
		}
		if (headlessCustomerLoadController != null) {
			headlessCustomerLoadController.disableBlocking();
		}
		if (headlessAngularLoadController != null) {
			headlessAngularLoadController.disableBlocking();
		}
		if (headlessMobileAngularLoadController != null) {
			headlessMobileAngularLoadController.disableBlocking();
		}
		if (headlessB2BLoadController != null) {
			headlessB2BLoadController.disableBlocking();
		}
	}

	@Override
	public void notifyScenarioChanged(Scenario scenario) {
		// we need to switch to the UEMLoadIncreaser if the increasing load scenario is selected
		// else we use the UEMLoadConfigWatcher
		if (modificatorChangeRequired(scenario)) {
			UEMLoadModificator newLoadModificator = isLoadIncreasingScenario(scenario) ? new UEMLoadIncreaser(this)
					: new UEMLoadConfigWatcher(this);
			loadModificator.disable();
			newLoadModificator.enable();
			loadModificator = newLoadModificator;
		}
	}

	private boolean modificatorChangeRequired(Scenario scenario) {
		return scenario == null ||
				(isLoadIncreasingScenario(scenario) && this.loadModificator.getType() != UEMLoadModificatorType.LOAD_INCREASER) ||
				(!isLoadIncreasingScenario(scenario) && this.loadModificator.getType() != UEMLoadModificatorType.CONFIG_WATCHER);
	}

	public static boolean isLoadIncreasingScenario(Scenario scenario) {
		if(scenario == null) {
			return false;
		}
		return MessageConstants.TESTCENTER_SCENARIO_INCREASING_LOAD_TITLE.equalsIgnoreCase(scenario.getTitle()) ||
				MessageConstants.DEVTEAM_SCENARIO_LOADTESTING_TITLE.equalsIgnoreCase(scenario.getTitle());
	}

	@Override
	public void notifyConfigLoaded(EasyTravelConfig oldCfg, EasyTravelConfig newCfg) {
		loadModificator.notifyConfigLoaded(oldCfg, newCfg);
	}

	@Override
	public BaseLoad getB2bLoadController() {
		return b2bLoadController == null ? new NullBaseLoad() : b2bLoadController;
	}

	@Override
	public BaseLoad getCustomerLoadController() {
		return customerLoadController == null ? new NullBaseLoad() : customerLoadController;
	}

	@Override
	public BaseLoad getMobileNativeLoadController() {
		return mobileNativeLoadController == null ? new NullBaseLoad() : mobileNativeLoadController;
	}

	@Override
	public BaseLoad getMobileBrowserLoadController() {
		return mobileBrowserLoadController == null ? new NullBaseLoad() : mobileBrowserLoadController;
	}

	@Override
	public BaseLoad getHotDealLoadController() {
		return hotDealLoadController == null ? new NullBaseLoad() : hotDealLoadController;
	}

	@Override
	public BaseLoad getIotDevicesLoadController() {
		return iotDevicesLoadController == null ? new NullBaseLoad() : iotDevicesLoadController;
	}

	@Override
	public BaseLoad getHeadlessCustomerLoadController() {
		return headlessCustomerLoadController == null ? new NullBaseLoad() : headlessCustomerLoadController;
	}

	@Override
	public BaseLoad getHeadlessAngularLoadController() {
		return headlessAngularLoadController == null ? new NullBaseLoad() : headlessAngularLoadController;
	}

	@Override
	public BaseLoad getHeadlessMobileAngularLoadController() {
		return headlessMobileAngularLoadController == null ? new NullBaseLoad() : headlessMobileAngularLoadController;
	}
	
	@Override
	public BaseLoad getHeadlessB2BLoadController() {
		return headlessB2BLoadController == null ? new NullBaseLoad() : headlessB2BLoadController;
	}

	@Override
	public HeaderPanelInterface getHeaderPanel() {
		return headerPanel;
	}

	@Override
	public void notifyBatchStateChanged(Scenario scenario, State oldState, State newState) {
		if (log.isLoggable(Level.FINE)) {
			log.info("notifyBatchStateChanged " + scenario.getTitle() + " " + oldState + " " + newState);
		}
		loadModificator.notifyBatchStateChanged(scenario, oldState, newState);
		//APM-8129: stop b2b load when scenario is stopped
		if (b2bLoadController != null) {
			b2bLoadController.notifyBatchStateChanged(scenario, oldState, newState);
		}
		if (headlessB2BLoadController != null) {
			headlessB2BLoadController.notifyBatchStateChanged(scenario, oldState, newState);
		}
		//APM-8129: clear all actions in LoadTimeWatcher when scenario is stopped
		if (State.STOPPED == newState || State.OPERATING == newState) {
			LoadTimeWatcher.notifyBatchStateChanged(State.OPERATING == newState);
		}

		notifyDriverEntryPoolOnBatchStateChange(newState);
	}

	private void notifyDriverEntryPoolOnBatchStateChange(State newState) {
		switch(newState) {
		case STOPPED:
		case STOPPING:
			UemLoadScheduler.schedule(() -> {
				DriverEntryPoolSingleton.getInstance().getPool().stopAll();
				MobileDriverEntryPoolSingleton.getInstance().getPool().stopAll();
				HeadlessProcessKillerFactory.stopChromeProcesses(false);
			}, 0, TimeUnit.SECONDS);
			break;
		case OPERATING:
		case STARTING:
		case TIMEOUT:
			HeadlessProcessKillerFactory.interruptStopAndWait();
			DriverEntryPoolSingleton.getInstance().getPool().start();
			MobileDriverEntryPoolSingleton.getInstance().getPool().start();
			break;
		default:
			break;
		}
	}

	@Override
	public void notifyProcedureStateChanged(StatefulProcedure subject,
			State oldState, State newState) {
		getCustomerLoadController().notifyProcedureStateChanged(subject, oldState, newState);
		getB2bLoadController().notifyProcedureStateChanged(subject, oldState, newState);
		getMobileNativeLoadController().notifyProcedureStateChanged(subject, oldState, newState);
		getMobileBrowserLoadController().notifyProcedureStateChanged(subject, oldState, newState);
		getHeadlessMobileAngularLoadController().notifyProcedureStateChanged(subject, oldState, newState);
		getHeadlessB2BLoadController().notifyProcedureStateChanged(subject, oldState, newState);
	}

	/**
	 * For tests, reset state
	 */
	public void reset() {
		customerLoadController = null;
		b2bLoadController = null;
		mobileNativeLoadController = null;
		mobileBrowserLoadController = null;
		headlessCustomerLoadController = null;
		headlessAngularLoadController = null;
		headlessMobileAngularLoadController = null;
		headlessB2BLoadController = null;
		headerPanel = null;
		loadModificator = new UEMLoadConfigWatcher(this);
	}

	private class NullBaseLoad extends BaseLoad {

		NullBaseLoad() {
			super(new EasyTravelCustomer(false), null, 0, 0.0, false);
		}

		@Override
		protected void addHost2Scenario(String host) {
		}

		@Override
		protected void removeHostFromScenario(String host) {
		}

		@Override
		protected boolean hasHost() {
			return false;
		}

	}
}
