package com.dynatrace.easytravel.launcher.baseload;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.UEMLoadConfig;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.util.TextUtils;


/**
 * Instances of this class read in a defined interval the {@link EasyTravelConfig} and check whether values referring
 * to UEM load had been changed. If so, it changes the actual UEM load behavior is accordingly adapted. Moreover, the
 * instance also adjusts the GUI.
 *
 * @author stefan.moschinski
 */
public class UEMLoadConfigWatcher implements UEMLoadModificator {

	private static final Logger logger = Logger.getLogger(UEMLoadConfigWatcher.class.getName());

	private UEMLoadConfig lastAppliedCfg = null;

	private LoadController loadController;

	UEMLoadConfigWatcher(LoadController loadController) {
		this.loadController = loadController;
		//set lastAppliedCfg to some initial value
		this.lastAppliedCfg = EasyTravelConfig.read();
	}

	@Override
	public void notifyBatchStateChanged(Scenario scenario, State oldState, State newState) {
	}

	@Override
	public void notifyConfigLoaded(EasyTravelConfig oldCfg, EasyTravelConfig newCfg) {
		boolean changed = false;
		if (newCfg.hasDifferentCustomerLoad(lastAppliedCfg)) {
			BaseLoadManager.getInstance().updateCustomerBaseLoadScenario(newCfg.getCustomerTrafficScenario());
			changed = true;
		}

		if (newCfg.hasDifferentLoadSettingsThan(lastAppliedCfg)) {
			if (!loadController.getB2bLoadController().isSchedulingBlocked()) {
				loadController.getB2bLoadController().setValue(newCfg.baseLoadDefault, newCfg.baseLoadB2BRatio);
			}
			if (!loadController.getCustomerLoadController().isSchedulingBlocked()) {
				loadController.getCustomerLoadController().setValue(newCfg.baseLoadDefault, newCfg.baseLoadCustomerRatio);
			}
			if (!loadController.getMobileNativeLoadController().isSchedulingBlocked()) {
				loadController.getMobileNativeLoadController().setValue(newCfg.baseLoadDefault, newCfg.baseLoadMobileNativeRatio);
			}
			if (!loadController.getMobileBrowserLoadController().isSchedulingBlocked()) {
				loadController.getMobileBrowserLoadController().setValue(newCfg.baseLoadDefault, newCfg.baseLoadMobileBrowserRatio);
			}
			if (!loadController.getHotDealLoadController().isSchedulingBlocked()) {
				loadController.getHotDealLoadController().setValue(newCfg.baseLoadDefault, newCfg.baseLoadHotDealServiceRatio);
			}
			if (!loadController.getHeadlessCustomerLoadController().isSchedulingBlocked()) {
				loadController.getHeadlessCustomerLoadController().setValue(newCfg.baseLoadDefault, newCfg.baseLoadHeadlessCustomerRatio);
			}
			if (!loadController.getHeadlessAngularLoadController().isSchedulingBlocked()) {
				loadController.getHeadlessAngularLoadController().setValue(newCfg.baseLoadDefault, newCfg.baseLoadHeadlessAngularRatio);
			}
			if (!loadController.getHeadlessMobileAngularLoadController().isSchedulingBlocked()) {
				loadController.getHeadlessMobileAngularLoadController().setValue(newCfg.baseLoadDefault, newCfg.baseLoadHeadlessMobileAngularRatio);
			}

			changed = true;
		}

		if (changed) {
			lastAppliedCfg = newCfg;
			logChangedProperties(oldCfg, newCfg);
		}
	}

	private void logChangedProperties(EasyTravelConfig oldCfg, EasyTravelConfig newCfg) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine(
					TextUtils.merge("Changing the load settings to following values: {0} - they were: {1}",
							newCfg.getUemLoadConfigAsString(),
							oldCfg != null ? oldCfg.getUemLoadConfigAsString() : "<null>"));
		}
	}

	@Override
	public void enable() {
		// nothing to do here
	}

	@Override
	public void disable() {
		// nothing to do here
	}

	@Override
	public UEMLoadModificatorType getType() {
		return UEMLoadModificatorType.CONFIG_WATCHER;
	}

}
