package com.dynatrace.diagnostics.uemload.headless;

import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

import com.dynatrace.diagnostics.uemload.ActionExecutor;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.easytravel.TestUtil;

public class HeadlessBlockingAction extends HeadlessDummyAction {

	private static final int RETRIES = 10;
	private Semaphore actionSemaphore = new Semaphore(0);
	
	public HeadlessBlockingAction() {
		super();
	}
	
	public HeadlessBlockingAction(String name) {
		super(name);
	}
	
	@Override
	public void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception {
		super.run(browser, continuation);
		actionSemaphore.acquire();
	}   	
	
	public void waitForBlockingAction(int retries) throws InterruptedException {
		Supplier<Boolean> s = () -> (actionSemaphore.getQueueLength() < 1);
		TestUtil.waitWhileTrue(s, retries);
	}
	
	public void waitForBlockingAction() throws InterruptedException {
		waitForBlockingAction(RETRIES);
	}
	
	public void unblockActionAndWait() throws InterruptedException {
		unblockActionAndWait(RETRIES);
	}
	
	public void unblockActionAndWait(int retries) throws InterruptedException {
		unblockAction();
		waitForUnblockingAction(retries);
	}
	
	private void unblockAction() {
		actionSemaphore.release();
	}
	    	    	
	private void waitForUnblockingAction(int retries) throws InterruptedException {
		Supplier<Boolean> s = () -> actionSemaphore.getQueueLength() > 0;
		TestUtil.waitWhileTrue(s, retries);
	}

}
