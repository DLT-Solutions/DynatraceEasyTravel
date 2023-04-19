package com.dynatrace.diagnostics.uemload.iot.visit;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.iot.IotDevice;
import com.dynatrace.diagnostics.uemload.iot.car.DynatraceRentalCar;
import com.dynatrace.diagnostics.uemload.iot.car.RentalCarCommandFactory;
import com.dynatrace.diagnostics.uemload.openkit.OpenKitParams;
import com.dynatrace.diagnostics.uemload.openkit.visit.OpenKitVisit;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal.Bakula
 */
public class RentalCarVisit extends OpenKitVisit<IotActionType> {

	public RentalCarVisit(String host, OpenKitParams params, ExtendedCommonUser user) {
		this.device = new DynatraceRentalCar(params, user);
		this.commandFactory = RentalCarCommandFactory.init(host, (IotDevice) device);
	}

	@Override
	public IotActionType[] getActions() {
		List<IotActionType> list = getBeginningActions();
		list.add(IotActionType.PARK);
		list.add(IotActionType.STOP);
		list.add(IotActionType.LOCK);
		return list.toArray(new IotActionType[0]);
	}

	protected List<IotActionType> getBeginningActions() {
		List<IotActionType> list = new ArrayList<>();
		list.add(IotActionType.AUTHENTICATION);
		list.add(IotActionType.UNLOCK);
		list.add(IotActionType.START);
		for (int i = 0; i <= UemLoadUtils.randomInt(15, 30); i++) {
			list.add(IotActionType.TRACKING_POINT);
			if (UemLoadUtils.randomInt(10) < 1) {
				list.add(IotActionType.GPS_ERROR);
			}
		}
		return list;
	}
}
