package com.dynatrace.diagnostics.uemload.iot.car;

import com.dynatrace.diagnostics.uemload.iot.IotActionPatterns;
import com.dynatrace.diagnostics.uemload.iot.IotDevice;
import com.dynatrace.diagnostics.uemload.iot.IotUtils;
import com.dynatrace.diagnostics.uemload.iot.visit.IotActionType;
import com.dynatrace.diagnostics.uemload.openkit.CommandFactory;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;

/**
 * @author Michal.Bakula
 */
public class RentalCarCommandFactory extends CommandFactory {

	public static RentalCarCommandFactory init(String host, IotDevice device) {
		final RentalCarCommandFactory cf = new RentalCarCommandFactory();

		IotActionPatterns patterns = new IotActionPatterns(device.getActiveSession());

		cf.addCommand(IotActionType.AUTHENTICATION, () -> patterns.userAuthenticationCall(IotActionType.AUTHENTICATION.getValue(), host, device.getUser().getName(), device.getUser().getPassword(), device.getUser().getFullName()));
		cf.addCommand(IotActionType.AUTHENTICATION_FAILURE, () -> patterns.userAuthenticationCall(IotActionType.AUTHENTICATION_FAILURE.getValue(), host, IotUtils.getRandomString(), IotUtils.getRandomString(), IotUtils.getRandomString()));
		cf.addCommand(IotActionType.START, () -> patterns.simpleAction(IotActionType.START.getValue(), UemLoadUtils.randomInt(100, 250)));
		cf.addCommand(IotActionType.STOP, () -> patterns.simpleAction(IotActionType.STOP.getValue(), UemLoadUtils.randomInt(100, 250)));
		cf.addCommand(IotActionType.LOCK, () -> patterns.simpleAction(IotActionType.LOCK.getValue(), UemLoadUtils.randomInt(100, 250)));
		cf.addCommand(IotActionType.UNLOCK, () -> patterns.simpleAction(IotActionType.UNLOCK.getValue(), UemLoadUtils.randomInt(100, 250)));
		cf.addCommand(IotActionType.TRACKING_POINT, () -> patterns.simpleAction(IotActionType.TRACKING_POINT.getValue(), UemLoadUtils.randomInt(20, 100)));
		cf.addCommand(IotActionType.GPS_ERROR, () -> patterns.actionWithError(IotActionType.GPS_ERROR.getValue(), UemLoadUtils.randomInt(500, 1000), "GPS_SIGNAL_LOST", 4));
		cf.addCommand(IotActionType.PARK, () -> patterns.simpleAction(IotActionType.PARK.getValue(), UemLoadUtils.randomInt(100, 250)));
		cf.addCommand(IotActionType.CRASH, () -> patterns.reportCrash(device));
		cf.addCommand(IotActionType.EMERGENCY, () -> patterns.simpleAction(IotActionType.EMERGENCY.getValue(), UemLoadUtils.randomInt(100, 250)));
		cf.addCommand(IotActionType.REPORT, () -> patterns.actionWithServerCall(IotActionType.REPORT.getValue(), host, "/RentalCarCrashReport"));

		return cf;
	}
}
