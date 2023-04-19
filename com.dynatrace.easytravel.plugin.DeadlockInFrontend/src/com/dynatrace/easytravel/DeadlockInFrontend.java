package com.dynatrace.easytravel;

import java.util.concurrent.atomic.AtomicBoolean;

import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

public class DeadlockInFrontend extends AbstractGenericPlugin  {

	@Override
	public Object doExecute(String location, Object... context) {
		// expect an AtomicBoolean for this extension point,
		// simply set this to true as we are only called here if the plugin is enabled
		((AtomicBoolean) context[0]).set(true);
		return null;
	}
}
