package com.dynatrace.diagnostics.uemload.iot.visit;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.openkit.OpenKitParams;

import java.util.List;

/**
 * @author Michal.Bakula
 */
public class RentalCarVisitWithCrash extends RentalCarVisit {

	public RentalCarVisitWithCrash(String host, OpenKitParams params, ExtendedCommonUser user) {
		super(host, params, user);
	}

	@Override
	public IotActionType[] getActions() {
		List<IotActionType> list = getBeginningActions();
		list.add(IotActionType.CRASH);
		list.add(IotActionType.EMERGENCY);
		list.add(IotActionType.REPORT);
		return list.toArray(new IotActionType[0]);
	}

}
