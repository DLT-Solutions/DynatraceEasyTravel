package com.dynatrace.easytravel.util.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.headless.DriverEntryPoolSingleton;
import com.dynatrace.diagnostics.uemload.headless.HeadlessVisitTestUtil;
import com.dynatrace.easytravel.ipc.SocketUtils;

public class HeadlessProcessKillerTest {

	@Before
	public void setup() {
		HeadlessVisitTestUtil.setup(false);
	}

	@After
	public void tearDown() throws InterruptedException {
		DriverEntryPoolSingleton.getInstance().getPool().stopAll();
		HeadlessProcessKillerFactory.stopChromeProcesses(true);
	}

	@Test
	public void testProcessShutDown( ) {
		Assume.assumeThat("Test can only run on windows", SystemUtils.IS_OS_WINDOWS, Is.is(true));

		// windows processes in memory
		List<ChromeProcessDesc> processList = new ArrayList<>();
		HeadlessProcessKiller pk = HeadlessProcessKillerFactory.createProcessKiller();

		if (pk==null) {
			// check that the operating system we are running agains is supported
			// should be windows or linux
			fail( "failed to get the HeadlessProcessKiller class");
			return;
		}

		// make sure all easyTravel associated chrome processes are killed (Shouldnt be any)
		HeadlessProcessKillerFactory.stopChromeProcesses(false);

		// These two tests should always pass - if they doen't then it looks like
		// the chrome processes have been left in memory from a previous test
		// if that is the case the fix would be to kill them here before running the test
		processList = pk.getProcesses( pk.getChromeCmd() );
		assertTrue( "Chromium processes already in memory (stopChromeProcess failed to remove them)", processList.size()==0);
		processList = pk.getProcesses( pk.getChromeDriverCmd() );
		assertTrue( "Chromium driver process already in memory (stopChromeProcess failed to remove them)", processList.size()==0);

		// create a couple of visits
		HeadlessVisitTestUtil.runHeadlessVisit(false);
		HeadlessVisitTestUtil.runHeadlessVisit(false);

		// you could get one or two of each process depending on the speed of completion of one visit before the next starts
		// in tests the first is reused so you only get one

		processList = pk.getProcesses( pk.getChromeCmd() );
		assertTrue( "Chromium processes was not left in memory after creating a visit", processList.size()>0);
		List<ChromeProcessDesc> chromeProcesses = processList;
		assertTrue( "Not all chromium processes have plugin dirs", allPluginDirsExist(chromeProcesses));
		processList = pk.getProcesses( pk.getChromeDriverCmd() );
		assertTrue( "Chromium driver processes was not in in memory after creating a visit", processList.size()>0);

		// Now the test, run the process killer
		// this is the line executed during the easyTravel shut down process
		HeadlessProcessKillerFactory.stopChromeProcesses(true);

		// test that nothing is in memory
		processList = pk.getProcesses( pk.getChromeCmd() );
		assertTrue( "Chromium processes was left in memory after creating a visit [" + processList.size() + "]", processList.size()==0);
		processList = pk.getProcesses( pk.getChromeDriverCmd() );
		assertTrue( "Chromium driver processes was left in memory after creating a visit[" + processList.size() + "]", processList.size()==0);
		assertFalse("Chromium user data directory was not deleted", Files.exists(Paths.get(HeadlessProcessNames.PATH_TO_CHROME_USER_DIR)));
		assertTrue("Some chromium plugin dirs are left", nonePluginDirsExist(chromeProcesses));
	}
	
	private boolean allPluginDirsExist(List<ChromeProcessDesc> processList) {
		return processList.stream()
				.filter(p -> p.getExtensionDir().isPresent())
				.allMatch(p -> Files.exists( Paths.get(p.getExtensionDir().get())));
	}
	
	private boolean nonePluginDirsExist(List<ChromeProcessDesc> processList) {
		return processList.stream()
				.filter(p -> p.getExtensionDir().isPresent())
				.noneMatch(p -> Files.exists( Paths.get(p.getExtensionDir().get())));
	}
	
	@Test
	public void testCaseSensitive() throws Exception{
		Assume.assumeThat("Test can only run on windows", SystemUtils.IS_OS_WINDOWS, Is.is(true));
		HeadlessProcessKillerFactory.stopChromeProcesses(false);
		HeadlessProcessKiller pk = HeadlessProcessKillerFactory.createProcessKiller();
		
		List<ChromeProcessDesc> pids = pk.getProcesses( pk.getChromeCmd() );
		assertTrue( pids.isEmpty() );
		
		int port = SocketUtils.getNextFreePort(9200, 9300, "localhost");
		try {
			Runtime.getRuntime().exec( HeadlessProcessNames.PATH_TO_CHROMIUM_WINDOWS.toUpperCase() + " --headless --disable-gpu --remote-debugging-port=" + port);
		}
		finally {
			SocketUtils.freePort(port);
		}
		
		pids = pk.getProcesses( pk.getChromeCmd() );
		assertFalse( pids.isEmpty() );
		
		HeadlessProcessKillerFactory.stopChromeProcesses(false);
		pids = pk.getProcesses( pk.getChromeCmd() );
		assertTrue( pids.isEmpty() );
	}
		
	@Test
	public void testParseLineLinux() {
		String line = " 6448  3032 /home/labuser/easytravel-2.0.0-x64/chrome/chrome --type=renderer --enable-automation --enable-logging --log-level=0 --remote-debugging-port=0 --test-type=webdriver --allow";
		ChromeProcessDesc chromeDesc =  new HeadlessProcessKillerLinux().parseLine(line);
		assertEquals("6448", chromeDesc.getPid());
		assertEquals("3032", chromeDesc.getParentPid().get());
		assertFalse(chromeDesc.getExtensionDir().isPresent());
		
		String line2 = " 6448  3032 /home/labuser/easytravel-2.0.0-x64/chrome/chrome --type=renderer --enable-automation --enable-logging --log-level=0 --remote-debugging-port=0 --test-type=webdriver --allow --load-extension=somePathinternal";
		chromeDesc =  new HeadlessProcessKillerLinux().parseLine(line2);
		assertEquals("6448", chromeDesc.getPid());
		assertEquals("3032", chromeDesc.getParentPid().get());
		assertEquals("somePath", chromeDesc.getExtensionDir().get());
	}
	
	@Test (expected = IndexOutOfBoundsException.class) 
	public void testParseLineLinuxExpcetion() {
		String line = "chrome";
		new HeadlessProcessKillerLinux().parseLine(line);
	}
	
	@Test
	public void testGetParentProcessIdWindows() {
		String line = "\"C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe\" --type=renderer --display-capture-permissions-policy-allowed --lang=en-US --device-scale-factor=1 --num-raster-threads=4 --enable-main-frame-before-activation --renderer-client-id=157 --launch-time-ticks=15875055922 --mojo-platform-channel-handle=10424 --field-trial-handle=1716,i,10242618570037254357,18413004861557581795,131072 /prefetch:1  6844";
		assertEquals("6844", new HeadlessProcessKillerWindows().getParentProcessId(line));
	}
	
	@Test
	@Ignore
	public void testIfProcessExists() {
		assertTrue(new HeadlessProcessKillerWindows().processExists("22196"));
		assertFalse(new HeadlessProcessKillerWindows().processExists("22197"));
	}
}