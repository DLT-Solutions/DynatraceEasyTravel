package com.dynatrace.easytravel.launcher.engine;


public class FailureAwareStatefulProcedure extends StatefulProcedure {


	public FailureAwareStatefulProcedure(Procedure delegate) {
		super(delegate);
	}
	
	
	@Override
	protected void addDefaultStopListener() {
		getDelegate().addStopListener(new AbstractStopListener() {
			@Override
			public void notifyProcessFailed() {
				setState(State.ACCESS_DENIED);
			}
			
			@Override
			public void notifyProcessStopped() {
				setState(State.STOPPED);
			}
		});
	}
	




}
