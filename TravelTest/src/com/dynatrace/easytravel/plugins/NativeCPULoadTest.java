package com.dynatrace.easytravel.plugins;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.*;

import org.junit.*;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.NativeCPULoad;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.util.NetstatUtil;
import com.dynatrace.easytravel.util.TextUtils;
import com.dynatrace.easytravel.utils.ThreadTestHelper;

public class NativeCPULoadTest {
	private static Logger log = LoggerFactory.make();

	private static final int SLEEP_TIME = 2000;
	private static final String PROPERTY_PORT = new StringBuilder().append(NativeCPULoad.class.getName()).append(BaseConstants.DOT).append("port").toString();
	private static final String PROPERTY_FILE = new StringBuilder().append(NativeCPULoad.class.getName()).append(BaseConstants.DOT).append("file").toString();
	private static final int WAIT_FOR_SHUTDONW_PORT = 30000;
	private static final int WAIT_FOR_PROCESS = 30000;

	@BeforeClass
	public static void setUpClass() throws IOException {
		LoggerFactory.initLogging();
	}

	@Before
	public void before() {
		deleteFile();
	}

	@After
	public void after() throws InterruptedException {
		System.clearProperty(PROPERTY_PORT);
		System.clearProperty(PROPERTY_FILE);

		// ensure that the stopper-thread is actually done so we can overwrite the file
		ThreadTestHelper.waitForThreadToFinish("NativeCPULoad stopper");

		deleteFile();
		
		// we modify the config for some tests, ensure that we reset it at the end of the test
		EasyTravelConfig.resetSingleton();
	}
	
	protected void deleteFile() {
		NativeCPULoad nativeCPULoad = new NativeCPULoad();
		String executableName = nativeCPULoad.getExecutableName();

		// make sure the file does not exist before
		File file = new File(executableName);
		assertTrue("File should be deletable, i.e. not locked any more: " + file, !file.exists() || file.delete());
	}

	@Test
	public void testDefault() throws Exception {
		NativeCPULoad nativeCPULoad = new NativeCPULoad();
		String executableName = nativeCPULoad.getExecutableName();

		nativeCPULoad.doExecute(PluginConstants.LIFECYCLE_PLUGIN_ENABLE, (Object[])null);
		assertTrue("File " + executableName + " not found.", new File(executableName).exists());
		assertTrue("Did not find process in list of running processes", checkForProcessInTasklistRetry(executableName));

		//wait for open shutdown port
		//do not fail here - try disable the plugin even if port is not open
		boolean portWasOpen = waitForShutdownPort(nativeCPULoad.getPort());

		//disable plugin
		nativeCPULoad.doExecute(PluginConstants.LIFECYCLE_PLUGIN_DISABLE, (Object[])null);

		//check if shutdown port was open
		assertTrue("Shutdown port was not open. Port: " + nativeCPULoad.getPort(), portWasOpen);

		assertFalse("File should not exist any more, but found: " + executableName,
				new File(executableName).exists());
		assertFalse("Expected process to be not running any more, but was still running", checkForProcessInTasklist(executableName));
	}
	
	@Test
	public void testTimeout() throws Exception {
		
		EasyTravelConfig config = EasyTravelConfig.read();
		// Create CPUload that will run for 15 seconds and then exit itself.
		// Supply a number of other options too to check that they are accepted by the executable
		config.NativeCPULoadTotalTime=15;
		config.NativeCPULoadActiveTime=5;
		config.NativeCPULoadQuietTime=1;
		
		NativeCPULoad nativeCPULoad = new NativeCPULoad();
		String executableName = nativeCPULoad.getExecutableName();
		
		nativeCPULoad.doExecute(PluginConstants.LIFECYCLE_PLUGIN_ENABLE, (Object[])null);
		assertTrue("File " + executableName + " not found.", new File(executableName).exists());
		
		Thread.sleep(5000);
		// The process should still be running at this stage
		assertTrue("Did not find process in list of running processes", checkForProcessInTasklistRetry(executableName));
		
		Thread.sleep(10000); // now the process should have ended by itself
		Thread.sleep(1000); // add an extra second to be sure
		// The process should not be running by now as it should have exited itself.
		assertFalse("Expected process not to be running any more, but was still running", checkForProcessInTasklist(executableName));
		
		// no need to wait to open shutdown port as the process should have exited
		
		//disable plugin
		nativeCPULoad.doExecute(PluginConstants.LIFECYCLE_PLUGIN_DISABLE, (Object[])null);

		// no need to check if shutdown port was open

		assertFalse("File should not exist any more, but found: " + executableName,
				new File(executableName).exists());
	}

	@Test
	public void testIllegalOptions() throws Exception {
		
		EasyTravelConfig config = EasyTravelConfig.read();
		// Specify an illegal combination of command line options: active time and quiet time cannot both be zero
		config.NativeCPULoadTotalTime=15;
		config.NativeCPULoadActiveTime=0;
		config.NativeCPULoadQuietTime=0;
		
		NativeCPULoad nativeCPULoad = new NativeCPULoad();
		String executableName = nativeCPULoad.getExecutableName();
		
		nativeCPULoad.doExecute(PluginConstants.LIFECYCLE_PLUGIN_ENABLE, (Object[])null);
		assertTrue("File " + executableName + " not found.", new File(executableName).exists());
		
		Thread.sleep(5000);
		// The process should not have started at all
		assertFalse("Expected process not to be running, but was found to be running", checkForProcessInTasklist(executableName));
		
		//disable plugin
		nativeCPULoad.doExecute(PluginConstants.LIFECYCLE_PLUGIN_DISABLE, (Object[])null);

		assertFalse("File should not exist any more, but found: " + executableName,
				new File(executableName).exists());
	}

	@Test
	public void testDifferentPort() throws Exception {
		System.setProperty(PROPERTY_PORT, String.valueOf(45322));
		testDefault();
		System.clearProperty(PROPERTY_PORT);

	}

	@Test
	public void testDifferentFileName() throws Exception {
		System.setProperty(PROPERTY_FILE, "foo-bar");
		testDefault();
		System.clearProperty(PROPERTY_FILE);
	}

	@Test
	public void testMultiInstancesPrevented() throws Exception {
		System.setProperty(PROPERTY_FILE, "foo-bar");
		NativeCPULoad nativeCPULoadA = new NativeCPULoad();

		String executableNameA = nativeCPULoadA.getExecutableName();
		nativeCPULoadA.doExecute(PluginConstants.LIFECYCLE_PLUGIN_ENABLE, (Object[])null);
		assertTrue(new File(executableNameA).exists());
		assertTrue("Did not find process in list of running processes", checkForProcessInTasklistRetry(executableNameA));

		System.setProperty(PROPERTY_FILE, "bar-foo");
		NativeCPULoad nativeCPULoadB = new NativeCPULoad();
		String executableNameB = nativeCPULoadB.getExecutableName();
		nativeCPULoadB.doExecute(PluginConstants.LIFECYCLE_PLUGIN_ENABLE, (Object[])null);
		assertTrue(new File(executableNameB).exists());
		assertFalse("Expected process to be not running any more, but was still running", checkForProcessInTasklist(executableNameB));

		//wait for open shutdown port
		//do not fail here - try disable the plugin even if port is not open
		boolean portWasOpen = waitForShutdownPort(nativeCPULoadA.getPort());


		nativeCPULoadA.doExecute(PluginConstants.LIFECYCLE_PLUGIN_DISABLE, (Object[])null);
		nativeCPULoadB.doExecute(PluginConstants.LIFECYCLE_PLUGIN_DISABLE, (Object[])null);

		//check if shutdown port was open
		assertTrue("Shutdown port was not open " + nativeCPULoadA.getPort(), portWasOpen);

		assertFalse("File " + executableNameA + " should not exist, but was found", new File(executableNameA).exists());
		assertFalse("File " + executableNameB + " should not exist, but was found", new File(executableNameB).exists());
		assertFalse("Expected process to be not running any more, but was still running", checkForProcessInTasklist(executableNameA));
		assertFalse("Expected process to be not running any more, but was still running", checkForProcessInTasklist(executableNameB));

		assertFalse(new File(executableNameA).exists());
		assertFalse(new File(executableNameB).exists());

		System.clearProperty(PROPERTY_PORT);
		System.clearProperty(PROPERTY_FILE);
	}

	@Test
	public void testMultiInstances() throws Exception {
		System.setProperty(PROPERTY_FILE, "foo-bar");
		System.setProperty(PROPERTY_PORT, String.valueOf(45322));
		NativeCPULoad nativeCPULoadA = new NativeCPULoad();

		String executableNameA = nativeCPULoadA.getExecutableName();
		nativeCPULoadA.doExecute(PluginConstants.LIFECYCLE_PLUGIN_ENABLE, (Object[])null);
		assertTrue(new File(executableNameA).exists());
		assertTrue("Did not find process in list of running processes", checkForProcessInTasklistRetry(executableNameA));

		System.setProperty(PROPERTY_FILE, "bar-foo");
		System.setProperty(PROPERTY_PORT, String.valueOf(45323));
		NativeCPULoad nativeCPULoadB = new NativeCPULoad();
		String executableNameB = nativeCPULoadB.getExecutableName();
		nativeCPULoadB.doExecute(PluginConstants.LIFECYCLE_PLUGIN_ENABLE, (Object[])null);
		assertTrue(new File(executableNameB).exists());
		assertTrue("Did not find process in list of running processes", checkForProcessInTasklistRetry(executableNameB));

		//wait for open shutdown port
		//do not fail here - try disable the plugin even if port is not open
		boolean portAWasOpen = waitForShutdownPort(nativeCPULoadA.getPort());
		boolean portBWasOpen = waitForShutdownPort(nativeCPULoadB.getPort());


		nativeCPULoadA.doExecute(PluginConstants.LIFECYCLE_PLUGIN_DISABLE, (Object[])null);
		nativeCPULoadB.doExecute(PluginConstants.LIFECYCLE_PLUGIN_DISABLE, (Object[])null);

		//check if shutdown port was open
		assertTrue("Shutdown port was not open for first process " + nativeCPULoadA.getPort(), portAWasOpen);
		assertTrue("Shutdown port was not open for second process " + nativeCPULoadB.getPort(), portBWasOpen);


		assertFalse("File " + executableNameA + " should not exist, but was found", new File(executableNameA).exists());
		assertFalse("File " + executableNameA + " should not exist, but was found", new File(executableNameB).exists());
		assertFalse("Expected process to be not running any more, but was still running", checkForProcessInTasklist(executableNameA));
		assertFalse("Expected process to be not running any more, but was still running", checkForProcessInTasklist(executableNameB));

		System.clearProperty(PROPERTY_PORT);
		System.clearProperty(PROPERTY_FILE);
	}

	private boolean checkForProcessInTasklistRetry(String name) throws Exception {
		long tStart = System.currentTimeMillis();
		long timeout = tStart + WAIT_FOR_PROCESS;

		boolean processFound = checkForProcessInTasklist(name);

		while (!processFound && System.currentTimeMillis() < timeout){
			Thread.sleep(SLEEP_TIME);
			processFound = checkForProcessInTasklist(name);
		}

		long waitTime = System.currentTimeMillis() - tStart;
		log.info("Waited for process in task list for " + waitTime);

		return processFound;
	}

	private boolean checkForProcessInTasklist(String name) throws Exception {
		final String cmd;
		if (OperatingSystem.IS_WINDOWS) {
			cmd = "tasklist.exe";
		} else {
			cmd = "ps -ef";
		}
		Process proc = Runtime.getRuntime().exec(cmd);
		InputStream in = proc.getInputStream();
		InputStreamReader irs = new InputStreamReader(in);
		BufferedReader br = new BufferedReader(irs);
		String line = br.readLine();
		while (line != null) {
			if (line.contains(name)) {
				log.info(line);
				return true;
			}
			line = br.readLine();
		}
		proc.waitFor();
		return false;
	}

	/**
	 * Wait for open shutdown port of CPUProcess. Use netstat for this.
	 * @param port
	 * @return
	 * @throws Exception
	 */
	private boolean waitForShutdownPort(int port) throws Exception {
		log.info("Waiting for open port: " + port);

		NetstatUtil netstatUtil = new NetstatUtil(Runtime.getRuntime());
		String proces = netstatUtil.findProcessIdForPort(port);
		boolean portOpen = (proces != null);

		long tStart = System.currentTimeMillis();
		long timeout = tStart + WAIT_FOR_SHUTDONW_PORT;

		while (!portOpen && System.currentTimeMillis() < timeout) {
			Thread.sleep(SLEEP_TIME);

			proces = netstatUtil.findProcessIdForPort(port);
			portOpen = (proces != null);
		}

		long waitTime = System.currentTimeMillis() - tStart;
		log.info("found proces id " + proces);
		log.info(TextUtils.merge("Waited for open port for {0}ms with result {1}", waitTime, portOpen));

		return portOpen;
	}
}
