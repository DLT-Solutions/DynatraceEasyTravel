package com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.mobile.MobileDeviceType;
import com.dynatrace.easytravel.misc.CommonUser;


public interface MobileVisit extends Visit{

	public Action[] getActions(MobileDeviceType device, CommonUser user, Location location);

}
