package com.dynatrace.easytravel.util.process;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.SystemUtils;

import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class HeadlessProcessKillerFactory {
	private static final Logger log = LoggerFactory.make();
	
	private static AtomicBoolean stopInProgress = new AtomicBoolean(false);
	
	private HeadlessProcessKillerFactory() {
	}

	public static HeadlessProcessKiller createProcessKiller( ) {
		HeadlessProcessKiller hpk = null;
		if (SystemUtils.IS_OS_WINDOWS){
			hpk = new HeadlessProcessKillerWindows();
		}

		if (SystemUtils.IS_OS_LINUX){
			hpk = new HeadlessProcessKillerLinux();
		}
		return hpk;
	}

	public static void stopChromeProcesses( boolean launcherShutdown ) {
		if (launcherShutdown) {
			HeadlessProcessKiller.startLauncherShutdown();
		}
		
		if (stopInProgress.getAndSet(true) && !launcherShutdown) {
			System.out.println("Stopping Chrome processes already in progress");
			return;
		}

		try {
			HeadlessProcessKiller hpk = createProcessKiller( );
			if (hpk!=null) {
				hpk.stopProcesses();
			} else {
				System.out.println( "No Chrome HeadlessProcessKiller defined for current Operating system" );
			}
		} finally {
			HeadlessProcessKiller.setNormalState();
			stopInProgress.set(false);
			System.out.println("Killing Chrome processes finished");
		}
	}
	
	public static void removeOrphanedChromeBrowsers() {
		if (stopInProgress.getAndSet(true)) {
			System.out.println("Stopping Chrome processes already in progress");
			return;
		}
		
		try {
			HeadlessProcessKiller hpk = createProcessKiller( );
			if (hpk!=null) {
				hpk.stopOrphanedChromeBrowsers();
			} else {
				System.out.println( "No Chrome HeadlessProcessKiller defined for current Operating system" );
			}
		} finally {
			HeadlessProcessKiller.setNormalState();
			stopInProgress.set(false);
			System.out.println("Killing Chrome processes finished");
		}
	}
	
	public static boolean interruptStopAndWait() {
		HeadlessProcessKiller.interruptStop();
		return waitForStop();
	}
	
	private static boolean waitForStop() {
		int cnt = 0;
		int retries = 30;
		try {
			while(stopInProgress.get() && cnt++ < retries) {
				Thread.sleep(500);
			}
		} catch (InterruptedException e) {
			log.warn("Waiting for HeadlessProcessKiller.isStopInterrupted was interrupted");
		}
		if (stopInProgress.get()) {
			log.warn("stopInProgress is still " + stopInProgress.get() + " after " + retries + " retries");
		}
		
		return !stopInProgress.get();
	}		
		
	public static boolean isStopInProgress() {
		return stopInProgress.get();
	}
		
	public static List<ChromeProcessDesc> getChromeAndDriverProcesses() {
		HeadlessProcessKiller hpk = createProcessKiller();
		if (hpk!=null) {
			List<ChromeProcessDesc> list = hpk.getChromeProcesses();
			list.addAll(hpk.getChromeDriverProcesses());
			return list;
		} else {
			System.out.println( "No Chrome HeadlessProcessKiller defined for current Operating system" );
			return null;
		}
	}
}
