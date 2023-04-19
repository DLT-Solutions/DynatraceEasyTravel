package com.dynatrace.easytravel.util.process;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 *
 * Class to make sure that all the headless processes are removed from memory
 * They should be removed through normal running.  HeadlessVisitRunnable.stopDriver( ) will release Chromium and the ChromeDriver from memory
 * However if there was a problem or a crash or easyTravel was shut down through task manager
 * its possible there could be some chrome.exe or chromedriver_windows32.exe processes still in memory
 * This class makes sure they are removed from memory when easytravel is subsequently closed down
 *
 * @author Paul.Johnson
 *
 */
public abstract class HeadlessProcessKiller {

	public abstract void stopProcesses() ;
	protected abstract List<ChromeProcessDesc> getProcesses( String cmd );
	protected abstract void killProcesses( List<ChromeProcessDesc> pids );

	public abstract String getChromeCmd();
	public abstract String getChromeDriverCmd();
			
	private enum HeadlessProcessKillerState {
		NORMAL,
		STOPPING,
		STOP_INTERRUPTED,
		LAUNCHER_SHUTDOWN		
	}
	private static HeadlessProcessKillerState state = HeadlessProcessKillerState.NORMAL;
		
	public synchronized static void interruptStop() {
		if (state != HeadlessProcessKillerState.LAUNCHER_SHUTDOWN) {
			state = HeadlessProcessKillerState.STOP_INTERRUPTED;
		}
	}
	
	public synchronized static void startLauncherShutdown() {
		state = HeadlessProcessKillerState.LAUNCHER_SHUTDOWN;
	}
	
	private synchronized static void setStoppingState() {
		if (state != HeadlessProcessKillerState.LAUNCHER_SHUTDOWN) {
			state = HeadlessProcessKillerState.STOPPING;
		}
	}
	
	public synchronized static void setNormalState() {
		if (state != HeadlessProcessKillerState.LAUNCHER_SHUTDOWN) {
			state = HeadlessProcessKillerState.NORMAL;
		}
	}
	
	public synchronized static boolean isStopInterrupted() {
		return state == HeadlessProcessKillerState.STOP_INTERRUPTED;
	}
		
	//for tests only
	public synchronized static boolean isStopInProgress() {
		return state == HeadlessProcessKillerState.STOPPING || state ==  HeadlessProcessKillerState.LAUNCHER_SHUTDOWN;
	}
	
	//for tests only
	public synchronized static void resetState() {
		state = HeadlessProcessKillerState.NORMAL;
	}
			
	protected void removeProcesses() {
		setStoppingState();
		List<ChromeProcessDesc> pids;
		pids = getProcesses( getChromeCmd() );
		killProcesses( pids );
		deleteChromePluginDirs(pids);
		pids = getProcesses( getChromeDriverCmd()  );
		killProcesses( pids );
		deleteChromeDirectories();
	}

	public List<ChromeProcessDesc> getChromeProcesses() {
		return getProcesses(getChromeCmd());
	}
	
	public List<ChromeProcessDesc> getChromeDriverProcesses() {
		return getProcesses(getChromeDriverCmd());
	}
	
	private void deleteChromeDirectories() {
		try {
			FileUtils.deleteDirectory(new File(HeadlessProcessNames.PATH_TO_CHROME_USER_DIR));
		} catch (IOException e) {
			System.out.println("Error deleting chrome user directories");
			e.printStackTrace();
		}
	}
	
	private void deleteChromePluginDirs(List<ChromeProcessDesc> chromeProcesses) {
		for(ChromeProcessDesc chrome: chromeProcesses) {
			try {
				if(chrome.getExtensionDir().isPresent()) {
					FileUtils.deleteDirectory(new File(chrome.getExtensionDir().get()));
				}
			} catch (IOException e) {
				System.out.println("Error deleting chrome plugin directory " + chrome.getExtensionDir().get());
				e.printStackTrace();
			}	
		}
	}
	
	public void stopOrphanedChromeBrowsers() {
	}

}
