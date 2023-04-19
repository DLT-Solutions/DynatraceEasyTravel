package com.dynatrace.easytravel.plugins;

import static org.junit.Assert.*;

import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.ipc.SocketUtils;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.utils.ThreadTestHelper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class PluginServiceMainTest {
    private static final Logger LOGGER = LoggerFactory.make();
    
	// we will overwrite these defaults with the next free port
	private int ServicePort = 7654;
	private int ServiceShutdownPort = 7655;
    
	@Before
	public void setUpShutdownExecutor() throws Exception {
		PluginServiceTestBase.setUpShutdownExecutor();
	}
	
	@Before
	public void prepareEasyTravelConfig() throws Exception {
		DtVersionDetector.enforceInstallationType(InstallationType.Classic);
		
		ServicePort = SocketUtils.reserveNextFreePort(7000, 8000, null);
		ServiceShutdownPort = SocketUtils.reserveNextFreePort(7000, 8000, null);
		System.out.println("service port: <" + ServicePort + ">");
		System.out.println("service shutdown port: <" + ServiceShutdownPort + ">");
		
		EasyTravelConfig config = EasyTravelConfig.read();
		config.pluginServicePort = ServicePort;
		config.pluginServiceShutdownPort = ServiceShutdownPort;
	}	
	
	@After
	public void shutdown() throws Exception {
		// ensure that the REST service is shut down again
		if(!SocketUtils.isPortAvailable(PluginServiceTestBase.ServiceShutdownPort, PluginServiceTestBase.HOST)  ||
				!SocketUtils.isPortAvailable(PluginServiceTestBase.ServiceShutdownPort, null)) {
			new PluginServiceTestBase().shutdown();
		}

		System.clearProperty("com.dynatrace.easytravel.propertiesfile");
	}

	@Test
	public void testMain() throws Exception {
		
		PluginService.main(new String[] {"-installationmode", "Both"});
		assertEquals(InstallationType.Both, DtVersionDetector.getInstallationType());

		// sleep a bit to have the thread running to properly shut it down later
		Thread.sleep(200);
	}
	
	@Test
	public void testMainArgumentFails() throws Exception {
		assertNull(System.getProperty("com.dynatrace.easytravel.propertiesfile"));
		PluginService.main(new String[] {});
		assertNull("Not fully started if no commandline options are specified",
				System.getProperty("com.dynatrace.easytravel.propertiesfile"));
	}

	// temporarily disabled because of Tomcat now providing the plugin service
	@Ignore
	@Test
	public void testMainWithPropertyFile() throws Exception {
		EasyTravelConfig.read().agent = "12345asdf";
		File propertyFile = EasyTravelConfig.read().storeInTempFile();
		assertNotNull(propertyFile);

		// now reset the setting to something else
		EasyTravelConfig.read().agent = "someother";

		PluginService.main(new String[] {"-installationmode", "Both", BaseConstants.MINUS + Constants.CmdArguments.PROPERTY_FILE, propertyFile.getAbsolutePath()});

		// now the setting from the property-file should be applied
		assertEquals("12345asdf", EasyTravelConfig.read().agent);

		// sleep a bit to have the thread running to properly shut it down later
		Thread.sleep(200);
	}

	@Test
	public void testMainWithInvalidPropertyFile() throws Exception {
		EasyTravelConfig.read().agent = "12345asdf";

		// now reset the setting to something else
		EasyTravelConfig.read().agent = "someother";

		PluginService.main(new String[] {"-installationmode", "Both", BaseConstants.MINUS + Constants.CmdArguments.PROPERTY_FILE, "someinvalidfile"});

		// now the setting from the property-file should be applied
		assertEquals("someother", EasyTravelConfig.read().agent);

		// sleep a bit to have the thread running to properly shut it down later
		Thread.sleep(200);
	}

	@Test
	public void testRESTInterfaces() throws Exception {
		PluginService.main(new String[] {"-installationmode", "Both"});
		assertEquals(InstallationType.Both, DtVersionDetector.getInstallationType());

		// sleep a bit to have the thread running to properly shut it down later
		Thread.sleep(200);

		assertEquals("Plugin Service on embedded Tomcat", UrlUtils.retrieveData("http://localhost:" + ServicePort + PluginService.CONTEXT + "/ping", 20000));
	}

	@Ignore("Only works with a started instance")
	@Test
	public void testTimes() throws IOException {
		for(int i = 0;i < 10000;i++) {
			long start = System.currentTimeMillis();
			IOUtils.toString(new URL("http://localhost:" + ServicePort + "/PluginService/getEnabledPluginNames"));
			LOGGER.info("Took: " + (System.currentTimeMillis() - start));
		}
	}

	private static final int NUMBER_OF_THREADS = 20;
	private static final int NUMBER_OF_TESTS = 500;

	@Ignore("Only works with a started instance")
	@Test
	public void testTimesThreaded() throws Throwable {
		final AtomicReference<Long> max = new AtomicReference<Long>(0l), min = new AtomicReference<Long>(0l);
		final long[][] times = new long[NUMBER_OF_THREADS][NUMBER_OF_TESTS];
		final Multimap<Long, Long> temp = ArrayListMultimap.create();
		final Multimap<Long, Long> timeDis = Multimaps.synchronizedMultimap(temp);

		ThreadTestHelper helper =
				new ThreadTestHelper(NUMBER_OF_THREADS, NUMBER_OF_TESTS);

		helper.executeTest(new ThreadTestHelper.TestRunnable() {

			@Override
			public void doEnd(int threadnum) throws Exception {
				// do stuff at the end ...
			}

			@Override
			public void run(int threadnum, int iter) throws Exception {
				long start = System.currentTimeMillis();
				IOUtils.toString(new URL("http://gdn-rx-co6-acc-etstg03v.emea.cpwr.corp:" + ServicePort + "/PluginService/getEnabledPluginNames"));
				long end = System.currentTimeMillis();
				// LOGGER.info("" + threadnum + "/" + iter + ": Took: " + (System.currentTimeMillis() - start));
				long duration = end-start;
				max.set(Math.max(max.get(), duration));
				min.set(Math.min(min.get(), duration));

				times[threadnum][iter] = duration;
				timeDis.put(start, duration);
				Thread.sleep(50);
			}
		});

		LOGGER.info("Max: " + max.get() + ", Min: " + min.get());
		for(int i = 0;i < NUMBER_OF_THREADS;i++) {
			System.out.print("Thread " + i + ": ");
			for(int j = 0;j < NUMBER_OF_TESTS;j++) {
				System.out.print(times[i][j] + ", ");
			}
			System.out.println();
		}

		// [161.2, 51.6],
		Writer writer = new BufferedWriter(new FileWriter(new File("C:\\temp\\data.txt")));
		try {
			for(Map.Entry<Long,Long> entry : timeDis.entries()) {
				//System.out.print("[" + entry.getKey() + "," + entry.getValue() + "],");
				if(entry.getValue() > 150) {
					writer.append("[" + entry.getKey() + "," + entry.getValue() + "],\n");
				}
			}
		} finally {
			writer.close();
		}
	}
}
