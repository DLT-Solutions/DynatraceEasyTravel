package com.dynatrace.easytravel.util.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.google.common.base.Splitter;

import ch.qos.logback.classic.Logger;

public class HeadlessProcessKillerLinux extends HeadlessProcessKiller {	
	// System.out.println is used in this class as it can be called on shut down when logging has already stopped
	//Logger is used only in stopOrphanedChromeBrowsers method, which is not called during shutdown 
	private static final Logger LOGGER = LoggerFactory.make();

	// in the development environment getLibDir() returns with a Launcher\.. as part of the path but TaskManger may report it with it removed
	private static final String EASYTRAVEL_CHROME_ID0_LINUX = HeadlessProcessNames.PATH_TO_CHROMIUM_LINUX.toLowerCase();
	private static final String EASYTRAVEL_CHROME_ID1_LINUX = FilenameUtils.normalize(HeadlessProcessNames.PATH_TO_CHROMIUM_LINUX).toLowerCase();
	private static final String EASYTRAVEL_CHROME_ID2_LINUX = HeadlessProcessNames.PATH_TO_CHROME_DRIVER_LINUX.toLowerCase();
	private static final String EASYTRAVEL_CHROME_ID3_LINUX = FilenameUtils.normalize(EASYTRAVEL_CHROME_ID2_LINUX).toLowerCase();

	// commands to get processes in memory
	// /NH no headings, /FI filter
	private static final String GET_CHROME_TASKS_LINUX 			= "ps -C chromium-browse -o pid,ppid,command";   // include pid and command for all chrome processes
	private static final String GET_CHROME_DRIVER_TASKS_LINUX 	= "ps -C chromedriver_linux64 -o pid,ppid,command" ;
	
	private static final Pattern EXTENSION_DIR_REGEX = Pattern.compile("load-extension=(.*)internal");
	private static final Splitter spliter = Splitter.on(" ").omitEmptyStrings().trimResults();

	@Override
	public void stopProcesses() {

		// kill all
		// chrome-c
		// chromedriver_linux64
		System.out.println(  "Checking Linux headless processes are not left in memory" );
		System.out.println( "\tlooking for \n\t[" + EASYTRAVEL_CHROME_ID0_LINUX
				+ "] \n\tor [" + EASYTRAVEL_CHROME_ID1_LINUX + "]"
				+ "] \n\tor [" + EASYTRAVEL_CHROME_ID2_LINUX + "]"
				+ "] \n\tor [" + EASYTRAVEL_CHROME_ID3_LINUX + "]"
				);

		removeProcesses();
		System.out.println( "Linux Process check complete" );
	}

	@Override
	public List<ChromeProcessDesc> getProcesses( String cmd ) {
		String line;
		List<ChromeProcessDesc> chromeProcesses = new ArrayList<>();
		try (BufferedReader input = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec( cmd ).getInputStream()))) {			
			while ((line = input.readLine()) != null) {				
				if (line!=null && !line.isEmpty()) {
					line = line.toLowerCase();
					if (line.contains(EASYTRAVEL_CHROME_ID0_LINUX)
							||  line.contains(EASYTRAVEL_CHROME_ID1_LINUX)
							||  line.contains(EASYTRAVEL_CHROME_ID2_LINUX)
							||  line.contains(EASYTRAVEL_CHROME_ID3_LINUX) ) {
						// example line " 1281 /home/paul/easytravel/et/chrome/chrome --type=zygote --enable-logging --log-level=0 etc etc"
						try {
							ChromeProcessDesc chromeDesc = parseLine(line);
							chromeProcesses.add(chromeDesc);
						} catch (Exception e) {
							System.out.println("Cannot parse chrome from line: " + line);
							e.printStackTrace();
						}
					}
				}
			}			
		} catch (IOException e) {
			System.out.println("Exception getting list of Linux processes [" + e.getMessage() + "]" );
		} 
		return chromeProcesses;
	}
	
	ChromeProcessDesc parseLine(String line) {
		List<String> tokens = spliter.splitToList(line);
		String pid = tokens.get(0);
		String parentPid = tokens.get(1);
		String pluginDir = getChromePluginDir(line);
		return new ChromeProcessDesc(pid, parentPid, pluginDir);
	}
	
	private String getChromePluginDir(String commandLine) {
		Matcher m = EXTENSION_DIR_REGEX.matcher(commandLine);		
		if(m.find()) {
			return m.group(1);
		}
		return null;
	}

	@Override
	protected void killProcesses( List<ChromeProcessDesc> chromeProcesses ) {
		for ( ChromeProcessDesc thisChrome : chromeProcesses ) {
			if(isStopInterrupted()) {
				return;
			}
			killProcess(thisChrome.getPid());
		}
	}
	
	protected void killProcess(String pid) {
		try {
			System.out.println(  "\tKilling process pid [" + pid+ "]"    );
			Runtime.getRuntime().exec( "kill " + pid );
		} catch (IOException e) {
			System.out.println("Exception killing Linux processes from memory [" + e.getMessage() + "]" );
		}
	}

	@Override
	public String getChromeCmd() {
		return GET_CHROME_TASKS_LINUX;
	}

	@Override
	public String getChromeDriverCmd() {
		return GET_CHROME_DRIVER_TASKS_LINUX ;
	}


	@Override
	public void stopOrphanedChromeBrowsers() {
		LOGGER.info("Looking for orphaned chrome processes");
		List<ChromeProcessDesc> processes = getProcesses(GET_CHROME_TASKS_LINUX);
		List<ChromeProcessDesc> orphaned = processes.stream()
				.filter( chrome -> chrome.getParentPid().isPresent())
				.filter( chrome -> chrome.getParentPid().get().equals("1"))
				.collect(Collectors.toList());
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("Found ophaned chrome processes: " + orphaned.toString()); 
		}
		orphaned.stream().forEach( chrome -> killProcess(chrome.getPid()));
		LOGGER.info("Orphaned chrome processes killed");
	}
}
