package com.dynatrace.diagnostics.uemload.iot.car;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.iot.IotDevice;
import com.dynatrace.diagnostics.uemload.openkit.EtOpenKitBuilder;
import com.dynatrace.diagnostics.uemload.openkit.OpenKitLoggerProxy;
import com.dynatrace.diagnostics.uemload.openkit.OpenKitParams;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants.OpenkitLogs;
import com.dynatrace.easytravel.logging.BasicLoggerConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.openkit.PlatformType;
import com.dynatrace.openkit.api.OpenKit;
import com.google.common.base.Strings;

import ch.qos.logback.classic.Logger;

/**
 * @author Michal.Bakula
 */
public class DynatraceRentalCar extends IotDevice {
	private static final Logger logger = LoggerFactory.make();
	private static final String[] CRASH_REASON = {"Engine overheated", "Dangerously low power level", "Engine control system failed"};

	private static final String BEACON = StringUtils.trim(EasyTravelConfig.read().iotBeaconUrl);
	private static final String APP_ID = StringUtils.trim(EasyTravelConfig.read().iotRentalCarsAppId);
	private static final OpenKit OPEN_KIT = new EtOpenKitBuilder(BEACON, APP_ID, 0).withInstanceName("DynatraceRentalCar")
			.withPlatformType(PlatformType.CUSTOM).withApplicationVersion(getRandomAppVersion())
			.withLogger(new OpenKitLoggerProxy(logger, new BasicLoggerConfig(OpenkitLogs.FILE_PREFIX, DynatraceRentalCar.class.getSimpleName()))).build(); // device ID set on a session level

	private Engine engine;

	public DynatraceRentalCar(OpenKitParams params, ExtendedCommonUser user) {
		super(OPEN_KIT, params, user);
		this.engine = new Engine();
	}

	@Override
	public void crash() throws EngineFailure {
		engine.fail();
	}

	public static void shutdownOpenKit() {
		OPEN_KIT.shutdown();
	}

	@Override
	public String getCrashName() {
		return "Engine failure";
	}

	@Override
	public String getCrashReason() {
		return CRASH_REASON[UemLoadUtils.randomInt(CRASH_REASON.length)];
	}

	public static boolean isConfigSet() {
		return !(Strings.isNullOrEmpty(BEACON) || Strings.isNullOrEmpty(APP_ID)) && DtVersionDetector.isAPM();
	}

}
