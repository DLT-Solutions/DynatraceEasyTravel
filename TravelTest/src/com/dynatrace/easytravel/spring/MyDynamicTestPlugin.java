package com.dynatrace.easytravel.spring;

import java.util.Date;

public class MyDynamicTestPlugin extends AbstractGenericPlugin {

	long created;
	long lastExecuted;
	boolean beenExecuted;

	public MyDynamicTestPlugin() {
		setName("MyDynamicTestPlugin");
		setDescription("A dynamic GenericPlugin");
		created = System.currentTimeMillis();
	}

	@Override
	public Object doExecute(String location, Object... context) {
		System.out.println("Hello World from: " + location + ", context=" + context + ", created at: " + new Date(created));
		if (lastExecuted > 0) {
			System.out.println("You already stopped by at: " + new Date(lastExecuted));
		}
		beenExecuted = true;
		lastExecuted = System.currentTimeMillis();
		return null;
	}
}