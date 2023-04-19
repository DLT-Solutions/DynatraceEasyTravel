package com.dynatrace.diagnostics.uemload.iot.visit;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.openkit.OpenKitParams;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michal.Bakula
 */
public class RentalCarVisitWithHttpError extends RentalCarVisit {

	public RentalCarVisitWithHttpError(String host, OpenKitParams params, ExtendedCommonUser user) {
		super(host, params, user);
	}

	@Override
	public IotActionType[] getActions() {
		List<IotActionType> list = new ArrayList<>();
		list.add(IotActionType.AUTHENTICATION_FAILURE);
		if (UemLoadUtils.randomInt(2) == 0) {
			Collections.addAll(list, super.getActions());
		}
		return list.toArray(new IotActionType[0]);
	}

}
