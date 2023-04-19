package com.dynatrace.diagnostics.uemload.headless;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.ActionExecutor;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;

public class HeadlessDummyAction extends Action {
	int called = 0;
	String name;
	
	public HeadlessDummyAction() {
		name = "MyAction_" + System.currentTimeMillis();
	}
	
	public HeadlessDummyAction(String name) {
		this.name= name;
	}
	
	@Override
	public void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception {
		called++;
	}
	
	public int getCallsNumber() {
		return called;
	}

}
