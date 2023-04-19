package com.dynatrace.easytravel;

import java.util.concurrent.atomic.AtomicBoolean;

import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.PluginConstants;

public class DBSlowdown extends AbstractGenericPlugin  {

	@Override
	public Object doExecute(String location, Object... context) {
		
		if (PluginConstants.BACKEND_LOCATION_MATCHING.equals(location)) {
			// The only thing we need to do is to let the calling code know that we are ON
			((AtomicBoolean)context[0]).set(true);
			return true;
		}
		else if (PluginConstants.BACKEND_BOOKING_STORE_BEFORE.equals(location) || PluginConstants.BACKEND_AUTHENTICATE_GETUSER.equals(location)) {
			// The only thing we need to do is to let the calling code know that we are ON
			((AtomicBoolean)context[1]).set(true);
			return true;
		}
		return null;
	}
}
