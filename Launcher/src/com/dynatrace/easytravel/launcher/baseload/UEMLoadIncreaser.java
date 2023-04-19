package com.dynatrace.easytravel.launcher.baseload;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.UEMLoadConfig;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

class UEMLoadIncreaser implements UEMLoadModificator {

	private static final Logger logger = Logger.getLogger(UEMLoadIncreaser.class.getName());

	private final EasyTravelConfig CONFIG = EasyTravelConfig.read();

	private int CUSTOMER_LOAD_INCREASE_PER_MINUTE = CONFIG.customerFrontendIncreasePerMinute;
	private int B2B_LOAD_INCREASE_PER_MINUTE = CONFIG.b2bFrontendIncreasePerMinute;
	private int MOBILE_NATIVE_LOAD_INCREASE_PER_MINUTE = CONFIG.mobileNativeIncreasePerMinute;
	private int MOBILE_BROWSER_LOAD_INCREASE_PER_MINUTE = CONFIG.mobileBrowserIncreasePerMinute;

	private int MIN_CUSTOMER_LOAD_PER_MINUTE = CONFIG.customerFrontendStartLoad;
	private int MIN_B2B_LOAD_PER_MINUTE = CONFIG.b2bFrontendStartLoad;
	private int MIN_MOBILE_NATIVE_LOAD_PER_MINUTE = CONFIG.mobileNativeStartLoad;
	private int MIN_MOBILE_BROWSER_LOAD_PER_MINUTE = CONFIG.mobileBrowserStartLoad;

	private int MAX_CUSTOMER_LOAD_PER_MINUTE = CONFIG.customerFrontendMaximumLoad;
	private int MAX_B2B_LOAD_PER_MINUTE = CONFIG.b2bFrontendMaximumLoad;
	private int MAX_MOBILE_NATIVE_LOAD_PER_MINUTE = CONFIG.mobileNativeMaximumLoad;
	private int MAX_MOBILE_BROWSER_LOAD_PER_MINUTE = CONFIG.mobileBrowserMaximumLoad;

	private int INITIAL_DELAY_IN_SECONDS = 15;
	private int PERIOD_BETWEEN_RUNS_IN_SECONDS = 60;
	private final double ONE = 1.0;

	private int customerLoadPerMinute;
	private int b2bLoadPerMinute;
	private int mobileNativeLoadPerMinute;
	private int mobileBrowserLoadPerMinute;

	private ScheduledExecutorService scheduler;
	private boolean isRunning;


	private boolean originalValueTaggedWebRequest;

	private SliderController slider;

	private LoadController loadController;

	private UEMLoadConfig lastAppliedCfg = null;

	UEMLoadIncreaser(LoadController loadController) {
		this.loadController = loadController;
		this.customerLoadPerMinute = MIN_CUSTOMER_LOAD_PER_MINUTE;

		this.originalValueTaggedWebRequest = loadController.getCustomerLoadController().isTaggedWebRequest();
		this.b2bLoadPerMinute = MIN_B2B_LOAD_PER_MINUTE;
		this.mobileNativeLoadPerMinute = MIN_MOBILE_NATIVE_LOAD_PER_MINUTE;
		this.mobileBrowserLoadPerMinute = MIN_MOBILE_BROWSER_LOAD_PER_MINUTE;

	}

	@Override
	public void enable() {
		BaseLoad customerLoad = loadController.getCustomerLoadController();
		BaseLoad b2bLoad = loadController.getB2bLoadController();
		BaseLoad mobileNativeLoad = loadController.getMobileNativeLoadController();
		BaseLoad mobileBrowserLoad = loadController.getMobileBrowserLoadController();

		// additional logging within the simulator is quite disturbing, so we switch it off
		customerLoad.setSimulatorLoggingOff();
		b2bLoad.setSimulatorLoggingOff();
		mobileNativeLoad.setSimulatorLoggingOff();
		mobileBrowserLoad.setSimulatorLoggingOff();

		customerLoad.setValue(0);
		b2bLoad.setValue(0);
		mobileNativeLoad.setValue(0);
		mobileBrowserLoad.setValue(0);

		b2bLoad.setTaggeWebRequest(true);
		customerLoad.setTaggeWebRequest(true);
		b2bLoad.setRatio(ONE);
		customerLoad.setRatio(ONE);
		mobileNativeLoad.setRatio(ONE);
		mobileBrowserLoad.setRatio(ONE);

		slider  = new SliderController();
		slider.initialize();
	}

	@Override
	public void disable() {
		logger.info("Increasing load feature disabled.");
		if(scheduler != null) {
			scheduler.shutdownNow();
			scheduler = null;
		}
		isRunning = false;


		this.customerLoadPerMinute = MIN_CUSTOMER_LOAD_PER_MINUTE;
		this.b2bLoadPerMinute = MIN_B2B_LOAD_PER_MINUTE;
		this.mobileNativeLoadPerMinute = MIN_MOBILE_NATIVE_LOAD_PER_MINUTE;
		this.mobileBrowserLoadPerMinute = MIN_MOBILE_BROWSER_LOAD_PER_MINUTE;

		if(slider != null) {
			slider.restoreOldValues();
		}
		restoreInitialValues();
	}


	private void restoreInitialValues() {
		loadController.getCustomerLoadController().setSimulatorLoggingOn();
		loadController.getB2bLoadController().setSimulatorLoggingOn();
		loadController.getMobileNativeLoadController().setSimulatorLoggingOn();
		loadController.getMobileBrowserLoadController().setSimulatorLoggingOn();

		loadController.getCustomerLoadController().setTaggeWebRequest(originalValueTaggedWebRequest);
		loadController.getMobileNativeLoadController().setTaggeWebRequest(originalValueTaggedWebRequest);
		loadController.getMobileBrowserLoadController().setTaggeWebRequest(originalValueTaggedWebRequest);
	}

	protected ScheduledFuture<?> start() {
		if (canBeStarted()) {
			logger.info("Increasing load feature started");
			isRunning = true;
			scheduler = Executors.newScheduledThreadPool(1,
					new ThreadFactoryBuilder()
					.setDaemon(true)
					.setNameFormat("Uem-Load-Increaser-Thread-%d")
					.build());
			return scheduler.scheduleAtFixedRate(new LoadIncreaser(), INITIAL_DELAY_IN_SECONDS, PERIOD_BETWEEN_RUNS_IN_SECONDS, TimeUnit.SECONDS);
		} else {
			logger.warning("Cannot start increasing load feature: Running: " + isRunning +
					", Customer: " + (loadController.getCustomerLoadController() == null ? "null" : loadController.getCustomerLoadController().hasHost()) +
					", B2B: " + (loadController.getB2bLoadController() == null ? "null" : loadController.getB2bLoadController().hasHost()));
			return null;
		}
	}

	private boolean canBeStarted() {
		BaseLoad customerLoad = loadController.getCustomerLoadController();
		BaseLoad b2bLoad = loadController.getB2bLoadController();

		return !isRunning && ((customerLoad != null && customerLoad.hasHost()) || (b2bLoad != null && b2bLoad.hasHost()));
	}

	private class LoadIncreaser implements Runnable {

		@Override
		public void run() {
			int customerLoadPerMinute = getCustomerLoadPerMinute();
			int b2bLoadPerMinute = getB2bLoadPerMinute();
			int mobileNativeLoadPerMinute = getMobileNativeLoadPerMinute();
			int mobileBrowserLoadPerMinute = getMobileBrowserLoadPerMinute();
			int totalLoadPerMin = 0;
			String loadIncreaseMsg = "Set %s load to %d visits per minute.";

			if (isCustomerLoadAvailable()) {
				loadController.getCustomerLoadController().setValue(customerLoadPerMinute);
				logger.info(String.format(loadIncreaseMsg, "CustomerFrontend", customerLoadPerMinute));
				totalLoadPerMin += customerLoadPerMinute;
			}
			if (isB2BLoadAvailable()) {
				loadController.getB2bLoadController().setValue(b2bLoadPerMinute);
				logger.info(String.format(loadIncreaseMsg, "B2BFrontend", b2bLoadPerMinute));
				totalLoadPerMin += b2bLoadPerMinute;
			}

			if (isMobileNativeLoadAvailable()) {
				loadController.getMobileNativeLoadController().setValue(mobileNativeLoadPerMinute);
				logger.info(String.format(loadIncreaseMsg, "Mobile Native", mobileNativeLoadPerMinute));
				totalLoadPerMin += mobileNativeLoadPerMinute;
			}

			if (isMobileBrowserLoadAvailable()) {
				loadController.getMobileBrowserLoadController().setValue(mobileBrowserLoadPerMinute);
				logger.info(String.format(loadIncreaseMsg, "Mobile Browser", mobileBrowserLoadPerMinute));
				totalLoadPerMin += mobileBrowserLoadPerMinute;
			}

			slider.setLoad(totalLoadPerMin);
		}

		private int getB2bLoadPerMinute() {
			int oldB2bLoadPerMinute = b2bLoadPerMinute;
			if (oldB2bLoadPerMinute >= MAX_B2B_LOAD_PER_MINUTE) {
				return MAX_B2B_LOAD_PER_MINUTE;
			}
			b2bLoadPerMinute = B2B_LOAD_INCREASE_PER_MINUTE + oldB2bLoadPerMinute;
			return oldB2bLoadPerMinute;
		}

		private int getCustomerLoadPerMinute() {
			int oldCustomerLoadPerMinute = customerLoadPerMinute;
			if (oldCustomerLoadPerMinute >= MAX_CUSTOMER_LOAD_PER_MINUTE) {
				return MAX_CUSTOMER_LOAD_PER_MINUTE;
			}
			customerLoadPerMinute = CUSTOMER_LOAD_INCREASE_PER_MINUTE + oldCustomerLoadPerMinute;
			return oldCustomerLoadPerMinute;
		}

		private int getMobileNativeLoadPerMinute() {
			int oldMobileLoadPerMinute = mobileNativeLoadPerMinute;
			if (oldMobileLoadPerMinute >= MAX_MOBILE_NATIVE_LOAD_PER_MINUTE) {
				return MAX_MOBILE_NATIVE_LOAD_PER_MINUTE;
			}
			mobileNativeLoadPerMinute = MOBILE_NATIVE_LOAD_INCREASE_PER_MINUTE + oldMobileLoadPerMinute;
			return oldMobileLoadPerMinute;
		}

		private int getMobileBrowserLoadPerMinute() {
			int oldMobileBrowserLoadPerMinute = mobileBrowserLoadPerMinute;
			if (oldMobileBrowserLoadPerMinute >= MAX_MOBILE_BROWSER_LOAD_PER_MINUTE) {
				return MAX_MOBILE_BROWSER_LOAD_PER_MINUTE;
			}
			mobileBrowserLoadPerMinute = MOBILE_BROWSER_LOAD_INCREASE_PER_MINUTE + oldMobileBrowserLoadPerMinute;
			return oldMobileBrowserLoadPerMinute;
		}

	}

	private boolean isCustomerLoadAvailable() {
		return loadController.getCustomerLoadController().hasHost();
	}

	private boolean isB2BLoadAvailable() {
		return loadController.getB2bLoadController().hasHost();
	}

	private boolean isMobileNativeLoadAvailable() {
		return loadController.getMobileNativeLoadController().hasHost();
	}

	private boolean isMobileBrowserLoadAvailable() {
		return loadController.getMobileBrowserLoadController().hasHost();
	}

	// currently only for testing

	@Override
	public void notifyBatchStateChanged(Scenario scenario, State oldState, State newState) {
		switch(newState) {
			case OPERATING:
			case TIMEOUT:
				start();
				break;
			case STOPPING:
			case STOPPED:
			case FAILED:
				disable();
				break;
			default:
				break;
		}
	}

	void setInitialDelay(int initialDelayInSeconds) {
		INITIAL_DELAY_IN_SECONDS = initialDelayInSeconds;
	}

	void setPeriodBetweenRuns(int periodBetweenRunsInSeconds) {
		PERIOD_BETWEEN_RUNS_IN_SECONDS = periodBetweenRunsInSeconds;
	}

	void setValues(int customerLoadIncreasePerMinute, int b2bLoadIncreasePerMinute, int mobileNativeLoadIncreasePerMinute,
			int mobileBrowserLoadIncreasePerMinute,
			int minCustomerLoadPerMinute, int minB2bLoadPerMinute, int minMobileNativeLoadPerMinute,
			int minMobileBrowserLoadPerMinute,
			int maxCustomerLoadPerMinute, int maxB2bLoadPerMinute, int maxMobileNativeLoadPerMinute,
			int maxMobileBrowserLoadPerMinute) {
		CUSTOMER_LOAD_INCREASE_PER_MINUTE = customerLoadIncreasePerMinute;
		B2B_LOAD_INCREASE_PER_MINUTE = b2bLoadIncreasePerMinute;
		MOBILE_NATIVE_LOAD_INCREASE_PER_MINUTE = mobileNativeLoadIncreasePerMinute;
		MOBILE_BROWSER_LOAD_INCREASE_PER_MINUTE = mobileBrowserLoadIncreasePerMinute;

		MIN_CUSTOMER_LOAD_PER_MINUTE = minCustomerLoadPerMinute;
		MIN_B2B_LOAD_PER_MINUTE = minB2bLoadPerMinute;
		MIN_MOBILE_NATIVE_LOAD_PER_MINUTE = minMobileNativeLoadPerMinute;
		MIN_MOBILE_BROWSER_LOAD_PER_MINUTE = minMobileBrowserLoadPerMinute;

		MAX_CUSTOMER_LOAD_PER_MINUTE = maxCustomerLoadPerMinute;
		MAX_B2B_LOAD_PER_MINUTE = maxB2bLoadPerMinute;
		MAX_MOBILE_NATIVE_LOAD_PER_MINUTE = maxMobileNativeLoadPerMinute;
		MAX_MOBILE_BROWSER_LOAD_PER_MINUTE = maxMobileBrowserLoadPerMinute;
	}

	private class SliderController {
		private int initialLoad = MIN_CUSTOMER_LOAD_PER_MINUTE + MIN_B2B_LOAD_PER_MINUTE;
		private int targetLoad = MAX_CUSTOMER_LOAD_PER_MINUTE + MAX_B2B_LOAD_PER_MINUTE;
		private double factor = 100.0 / (targetLoad - initialLoad);

		public void initialize() {
			if(loadController.getHeaderPanel() == null) {
				return;
			}
			loadController.getHeaderPanel().deactivateUEMLoadPanel();
			loadController.getHeaderPanel().enableTaggedWebRequest();
			setLoadToNull();
		}

		void setLoadToNull() {
			if(loadController.getHeaderPanel() == null) {
				return;
			}
			loadController.getHeaderPanel().setLoad(0);
		}

		void setLoad(int load) {
			if(loadController.getHeaderPanel() == null) {
				return;
			}
			int nettoLoad = (load - initialLoad > 0) ? load - initialLoad : 0;
			int value = (int) Math.round(factor * nettoLoad);
			value = value > 100 ? 100 : value;
			loadController.getHeaderPanel().setLoad(load);
			loadController.getHeaderPanel().setTrafficLabel(TextUtils.merge("{0} visits/min", load));
		}

		void restoreOldValues() {
			if(loadController.getHeaderPanel() == null) {
				return;
			}
			loadController.getHeaderPanel().activateUEMLoadPanel();
			if(originalValueTaggedWebRequest) {
				loadController.getHeaderPanel().enableTaggedWebRequest();
			} else {
				loadController.getHeaderPanel().disableTaggedWebRequest();
			}
			loadController.getHeaderPanel().resetTrafficLabel();
		}

	}

	@Override
	public void notifyConfigLoaded(EasyTravelConfig oldCfg, EasyTravelConfig newCfg) {
		//update customer load scenario
		if (newCfg != null && newCfg.hasDifferentCustomerLoad(lastAppliedCfg)) {
			BaseLoadManager.getInstance().updateCustomerBaseLoadScenario(newCfg.getCustomerTrafficScenario());
			lastAppliedCfg = newCfg;
		}

		// nothing to do more, the UEMLoadIncreaser should ignore other configuration settings
	}

	@Override
	public UEMLoadModificatorType getType() {
		return UEMLoadModificatorType.LOAD_INCREASER;
	}

	/**
	 * mainly for testing!
	 *
	 * @return
	 * @author cwat-dstadler
	 */
	protected boolean isRunning() {
		return isRunning;
	}
}
