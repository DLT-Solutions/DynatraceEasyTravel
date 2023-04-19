package com.dynatrace.easytravel;

import java.util.concurrent.atomic.AtomicBoolean;

import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.PluginConstants;


public class EnableCaching extends AbstractGenericPlugin {

	@Override
	protected Object doExecute(String location, Object... context) {
		if(PluginConstants.FRONTEND_RESOURCE_CACHING.equals(location)) {
			setCachingTrue(context);
		}
		return null;
	}

	private void setCachingTrue(Object[] context) {
		if(isAtomicBoolean(context)) 
			((AtomicBoolean) context[0]).set(true);
	}
	
	private boolean isAtomicBoolean(Object[] context) {
		return context != null && 
				context.length > 0 &&
				context[0] instanceof AtomicBoolean;
	}
}
