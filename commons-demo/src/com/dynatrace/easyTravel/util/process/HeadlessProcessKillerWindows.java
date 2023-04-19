package com.dynatrace.easytravel.util.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class HeadlessProcessKillerWindows extends HeadlessProcessKiller {
	// System.out.println is used in this class as it can be called on shut down when logging has already stopped
	//Logger is used only in stopOrphanedChromeBrowsers method, which is not called during shutdown
	private static final Logger LOGGER = LoggerFactory.make();

	// in the development environment getLibDir() returns with a Launcher\.. as part of the path but TaskManger may report it with it removed
	private static final String EASYTRAVEL_CHROME_IDENTIFER_0 = HeadlessProcessNames.PATH_TO_CHROMIUM_WINDOWS.toLowerCase();
	private static final String EASYTRAVEL_CHROME_IDENTIFER_1 = FilenameUtils.normalize(EASYTRAVEL_CHROME_IDENTIFER_0).toLowerCase();
	private static final String EASYTRAVEL_CHROME_IDENTIFER_2 = HeadlessProcessNames.PATH_TO_CHROME_DRIVER_WINDOWS.toLowerCase();
	private static final String EASYTRAVEL_CHROME_IDENTIFER_3 = FilenameUtils.normalize(EASYTRAVEL_CHROME_IDENTIFER_2.toLowerCase());

	// commands to get processes in memory
	// /NH no headings, /FI filter
	private static final String GET_CHROME_TASKS_WIN 			= "tasklist /nh /FI \"IMAGENAME eq " + HeadlessProcessNames.CHROME_EXE + "\"";
	private static final String GET_CHROME_DRIVER_TASKS_WIN 	= "tasklist /nh /FI \"IMAGENAME eq " + HeadlessProcessNames.CHROME_DRIVER_EXE + "\"";
	
	private static final Pattern EXTENSION_DIR_REGEX = Pattern.compile("load-extension=\"(.*)internal");

	@Override
	public void stopProcesses() {

		// We only want to kill the ones we started
		// chrome.exe
		// Chromedriver_windows32.exe
		System.out.println(  "Checking Windows headless processes are not left in memory" );
		// now get the command line to see if this is our local chrome or chrome driver
		//
		System.out.println( "\tlooking for \n\t[" + EASYTRAVEL_CHROME_IDENTIFER_0
				+ "] \n\tor [" + EASYTRAVEL_CHROME_IDENTIFER_1 + "]"
				+ "] \n\tor [" + EASYTRAVEL_CHROME_IDENTIFER_2 + "]"
				+ "] \n\tor [" + EASYTRAVEL_CHROME_IDENTIFER_3 + "]"
				);

		removeProcesses();
		System.out.println( "Windows process check complete" );
	}

	@Override
	public List<ChromeProcessDesc> getProcesses( String cmd ) {
		int pos;
		String line, pid, result;
		List<String> pidsAll = new ArrayList<String>();
		List<ChromeProcessDesc> chromeDesc = new ArrayList<>();
		BufferedReader input=null;
		Process p;
		try {
			p = Runtime.getRuntime().exec( cmd );
			input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				if (line.contains(HeadlessProcessNames.CHROME_EXE_SHORT) || line.contains(HeadlessProcessNames.CHROME_DRIVER_EXE_SHORT)) {
					// get the pid - line = "chrome.exe                   27588 Console                    1     98,816 K" we want 27588
					pos = line.indexOf(' ');			// find first space and remove everything before it
					result = line.substring(pos).trim();
					pos = result.indexOf(' ');		// find next space
					pid = result.substring(0, pos).trim();
					pidsAll.add(pid);
				}
			}
			input.close();
						
			// now for each process ID check that it is one for easyTravel
			for ( String thispid : pidsAll ) {
				if(isStopInterrupted()) {
					return Collections.emptyList();
				}
				p = Runtime.getRuntime().exec( "wmic process where processId=" + thispid + " get parentprocessid, commandline");
				input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				line = input.readLine();
				while ((line = input.readLine()) != null) {
					if (line!=null && !line.isEmpty()) {
						line = line.toLowerCase();
						if (line.contains(EASYTRAVEL_CHROME_IDENTIFER_0)
								||  line.contains(EASYTRAVEL_CHROME_IDENTIFER_1)
								||  line.contains(EASYTRAVEL_CHROME_IDENTIFER_2)
								||  line.contains(EASYTRAVEL_CHROME_IDENTIFER_3) ) {
							String chromePluginDir = getChromePluginDir(line);
							String parentProcessId = getParentProcessId(line);
							ChromeProcessDesc chpd = new ChromeProcessDesc(thispid, parentProcessId, chromePluginDir);
							chromeDesc.add(chpd);
						}
					}
				}
			}
			input.close();

		} catch (IOException e) {
			System.out.println("Exception getting list of windows processes [" + e.getMessage() + "]" );
		}
		return chromeDesc;
	}
	
	private String getChromePluginDir(String commandLine) {
		Matcher m = EXTENSION_DIR_REGEX.matcher(commandLine);		
		if(m.find()) {
			return m.group(1);
		}
		return null;
	}
	
	//not private for tests
	String getParentProcessId(String commandLine) {
		return StringUtils.substringAfterLast(commandLine.trim(), " ");
	}

	protected void killProcesses( List<ChromeProcessDesc> chromeProcesses ) {
		try {
			for ( ChromeProcessDesc thisChrome : chromeProcesses ) {
				if(isStopInterrupted()) {
					return;
				}
				System.out.println(  "\tKilling process pid [" + thisChrome.getPid() + "]"  );
				Runtime.getRuntime().exec( "TASKKILL /PID " + thisChrome.getPid() + " /T /F");
			}
		} catch (IOException e) {
			System.out.println("Exception killing windows processes from memory [" + e.getMessage() + "]" );
		}
	}

	@Override
	public String getChromeCmd() {
		return GET_CHROME_TASKS_WIN;
	}

	@Override
	public String getChromeDriverCmd() {
		return GET_CHROME_DRIVER_TASKS_WIN;
	}
	
	@Override
	public void stopOrphanedChromeBrowsers() {
		LOGGER.info("Looking for orphaned chrome processes");
		List<ChromeProcessDesc> chromeProcesses = getChromeProcesses();
		 List<ChromeProcessDesc> notExistingProcesses = chromeProcesses.stream()
		 .filter(chrome -> chrome.getParentPid().isPresent())
		 .filter(chrome -> !processExists(chrome.getParentPid().get()))
		 .collect(Collectors.toList());
		 LOGGER.trace("Found ophaned chrome processes: " + notExistingProcesses.toString());
		 killProcesses(notExistingProcesses);
	}
	
	boolean processExists(String pid) {
		String line = null;
		try (BufferedReader input = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec( "tasklist.exe /nh /fi \"PID eq "+ pid + "\"" ).getInputStream()))) {
			while((line = input.readLine())!= null) {
				if(line.indexOf(pid) != -1) {
					return true;
				}
			}
		} catch (IOException e) {
			LOGGER.error("Error looking for process", e);
		}
		return false;
	}
}
