package com.dynatrace.easytravel;

import java.util.concurrent.atomic.AtomicBoolean;

import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

public class UseLocationCache extends AbstractGenericPlugin  {

	@Override
	public Object doExecute(String location, Object... context) {
		((AtomicBoolean)context[0]).set(true);
		return null;
	}
}
