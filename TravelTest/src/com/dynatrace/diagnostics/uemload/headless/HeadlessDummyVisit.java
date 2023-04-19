package com.dynatrace.diagnostics.uemload.headless;

import java.util.Arrays;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.easytravel.misc.CommonUser;

public class HeadlessDummyVisit implements Visit {
	HeadlessDummyAction[] actions; 
	
	public HeadlessDummyVisit() {
		actions = new HeadlessDummyAction[] {new HeadlessDummyAction()};
	}
	
	public HeadlessDummyVisit(HeadlessDummyAction[] actions) {
		this.actions = actions;
	}
	
	@Override
	public Action[] getActions(CommonUser user, Location location) {			
		return actions;
	}

	@Override
	public String getVisitName() {
		return "MyVisit";
	}
	
	public int getCallsNumber() {
		return Arrays.stream(actions).reduce(0, (sum, action) -> sum + action.getCallsNumber(), (a, b) -> a + b);
	}

	public int getCallsNumber(int actionNr) {
		return actions[actionNr].getCallsNumber();
	}
	
	public void waitForBlockingAction(int actionNr, int retries) throws InterruptedException {
		HeadlessBlockingAction blockingAction = (HeadlessBlockingAction)actions[actionNr];
		blockingAction.waitForBlockingAction(retries);
	}
	
	public void waitForBlockingAction(int actionNr) throws InterruptedException {
		HeadlessBlockingAction blockingAction = (HeadlessBlockingAction)actions[actionNr];
		blockingAction.waitForBlockingAction();
	}	
	
	public void unblockActionAndWait(int actionNr) throws InterruptedException {
		HeadlessBlockingAction blockingAction = (HeadlessBlockingAction)actions[actionNr];
		blockingAction.unblockActionAndWait();
	}	
	
	public void unblockActionAndWait(int actionNr, int retries) throws InterruptedException {
		HeadlessBlockingAction blockingAction = (HeadlessBlockingAction)actions[actionNr];
		blockingAction.unblockActionAndWait(retries);
	}
}
