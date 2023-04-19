package com.dynatrace.diagnostics.uemload.mobileopenkit.visit;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileActionType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.parameters.MobileOpenKitParams;

import java.util.List;

public class BounceVisit extends MobileOpenKitVisit {

	public BounceVisit(MobileOpenKitParams params, ExtendedCommonUser user, String apiUrl) {
		super(params, user, apiUrl);
	}

	@Override
	public List<MobileActionType> addActions(List<MobileActionType> list) {
		RandomSet<MobileActionType> actions = new RandomSet<>();
		actions.add(MobileActionType.SEARCH, 2);
		actions.add(MobileActionType.LOGIN, 2);
		actions.add(MobileActionType.LOGIN_FAILED, 2);
		actions.add(MobileActionType.USE_GPS_WITH_ERROR, 1);
		list.add(actions.getRandom());
		return list;
	}
}
