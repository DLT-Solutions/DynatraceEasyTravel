package com.dynatrace.easytravel.booking;

import java.util.concurrent.atomic.AtomicBoolean;

import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

public class BookingErrorAsHttp500 extends AbstractGenericPlugin  {

	@Override
	public Object doExecute(String location, Object... context) {
		AtomicBoolean showAsHttp500 = (AtomicBoolean) context[0];
		showAsHttp500.set(true);
		return null;
	}
}
