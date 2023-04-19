package com.dynatrace.diagnostics.uemload.headless;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.ExtendedDemoUser;
import com.dynatrace.diagnostics.uemload.HeadlessAngularSimulator;
import com.dynatrace.diagnostics.uemload.HeadlessMobileAngularSimulator;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.UEMLoadScenario;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.scenarios.HeadlessAngularOverloadScenario;
import com.dynatrace.easytravel.TestUtil;
import com.dynatrace.easytravel.util.process.HeadlessProcessKillerFactory;

public class HeadlessVisitTestUtil {
	ExecutorService scheduler;

	static {
		TestUtil.setInstallDirCorrection();
		TestUtil.setupLocalLogging();
    }

	public HeadlessVisitTestUtil() {
		scheduler = Executors.newFixedThreadPool(10);
	}

    public static void setup(boolean isMobile) {
		HeadlessAngularRunnable.startAll();

		if (isMobile) {
			MobileDriverEntryPoolSingleton.getInstance().getPool().start();
		}
		else {
			DriverEntryPoolSingleton.getInstance().getPool().start();
		}

		HeadlessTestUtil.setupChromeAndLogDirectories();
    }

	public void tearDown(boolean isMobile) throws InterruptedException {
		if (isMobile) {
			MobileDriverEntryPoolSingleton.getInstance().getPool().stopAll();
		}
		else {
			DriverEntryPoolSingleton.getInstance().getPool().stopAll();
		}

 		stopScheduler(30);
		HeadlessProcessKillerFactory.stopChromeProcesses(true);
	}

	private void stopScheduler(int timeoutInSeconds) {
		try {
			if(!scheduler.isTerminated()) {
				scheduler.shutdown();
				scheduler.awaitTermination(30, TimeUnit.SECONDS);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static HeadlessDummyVisit createHeadlessBlockingVisit() {
    	HeadlessBlockingAction visit1BlockingAction = new HeadlessBlockingAction("action1");
    	HeadlessDummyAction vist1NotExectutedAction = new HeadlessDummyAction("action2");
    	HeadlessDummyVisit visit1 = new HeadlessDummyVisit(new HeadlessDummyAction[] {visit1BlockingAction, vist1NotExectutedAction});
    	return visit1;
	}

    public Future<?> runHeadlessVisitInThread(Visit visit, boolean isMobile) {
    	return scheduler.submit(() -> {
    		new HeadlessAngularRunnable(visit, ExtendedDemoUser.DEMOUSER, getSimulator(isMobile)).run();
    	});
    }

    public static HeadlessDummyVisit runHeadlessVisit(boolean isMobile) {
    	HeadlessDummyVisit visit = new HeadlessDummyVisit();
    	runHeadlessVisit(visit, isMobile);
    	return visit;
    }

	public static HeadlessDummyMoveVisit runHeadlessDummyMoveVisit(String easytravelUrl, boolean isMobile) {
		HeadlessDummyMoveVisit visit = new HeadlessDummyMoveVisit(easytravelUrl);
		runHeadlessVisit(visit, ExtendedDemoUser.DEMOUSER2, isMobile);
		return visit;
	}

	public static void runHeadlessVisit(Visit visit, boolean isMobile) {
    	new HeadlessAngularRunnable(visit, ExtendedDemoUser.DEMOUSER, getSimulator(isMobile)).run();
    }
    
    public static void runHeadlessVisit(Visit visit, ExtendedCommonUser user, boolean isMobile) {
    	new HeadlessAngularRunnable(visit, user, getSimulator(isMobile)).run();
    }
    
    public static Simulator getSimulator(boolean isMobile) {
    	UEMLoadScenario scenario = new HeadlessAngularOverloadScenario();
    	return isMobile ? new HeadlessMobileAngularSimulator(scenario) : new HeadlessAngularSimulator(scenario);
    }    
}
