package com.dynatrace.easytravel.plugins;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.startup.Tomcat;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.WarDeploymentPlugin;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.spring.TomcatHolderBean;
import com.dynatrace.easytravel.util.TextUtils;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.NanoHTTPD;

/**
 * Test for WarDeploymentPlugin
 * @author cwpl-rpsciuk
 *
 */
public class WarDeploymentPluginTest {

	private static final Logger log = Logger.getLogger(WarDeploymentPluginTest.class.getName());

	private Tomcat tomcat;
	private Host host;
	private Context context;
	private final TomcatHolderBean tomcatHolder = new TomcatHolderBean();

	private File file = new File(Directories.getTempDir(), WarDeploymentPlugin.DST_APP_NAME);

	private WarDeploymentPlugin plugin = new WarDeploymentPlugin();


	/**
	 * Initialize all mocks
	 */
	@Before
	public void setup() {
		deleteWarFile();

		EasyTravelConfig config = EasyTravelConfig.read();
		tomcat = EasyMock.createNiceMock(Tomcat.class);
		host = EasyMock.createNiceMock(Host.class);
		context = EasyMock.createNiceMock(Context.class);
		tomcatHolder.setTomcat(tomcat);
		tomcatHolder.setReservation(null);


		expect(tomcat.getHost()).andReturn(host);
		expect(host.findChild(config.backendEasyTravelMonitorContextRoot)).andReturn(context);
		expect(context.getDocBase()).andReturn(file.getAbsolutePath());
		context.reload();

		replay(tomcat, host, context);
	}

	@After
	public void tearDown() {
		deleteWarFile();
	}

	/**
	 * Remove war file from temporary directory
	 */
	private void deleteWarFile() {
		// make sure the file does not exist before
		File file = new File(Directories.getTempDir(), WarDeploymentPlugin.DST_APP_NAME);
		assertTrue(!file.exists() || file.delete());
	}

	/**
	 * Calculate size of the resource
	 * @param resourceName
	 * @return
	 * @throws IOException
	 */
	private int getResourceSize(String resourceName) throws IOException {
		URL url = plugin.getClass().getClassLoader().getResource(resourceName);
		InputStream in = url.openStream();
		try {
			return getStreamSize(in);
		} finally {
			in.close();
		}
	}

	/**
	 * Get number of bytes in the stream, used to calculate size of the resuource
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private int getStreamSize(InputStream in) throws IOException {
		int size = 0;
		int i = 0;
		byte[] buf = new byte[4*1024*1024];
		while((i = in.read(buf)) != -1) {
			size+=i;
		}
		return size;
	}

	/**
	 * Upgrade application from version 1 to version 2
	 * @throws IOException
	 */
	@Test
	public void testUpgradeV1() throws IOException {
		upgradeApp(WarDeploymentPlugin.SRC_APP_NAME_V1);
	}

	/**
	 * Upgrade application from version 2 to version 1
	 * @throws IOException
	 */
	@Test
	public void testUpgradeV2() throws IOException {
		upgradeApp(WarDeploymentPlugin.SRC_APP_NAME_V2);
	}

	/**
	 * Upgrade application
	 * @param srcApp source version of application
	 * @throws IOException
	 */
	private void upgradeApp(String srcApp) throws IOException {
		//run mock HTTP server, answering with proper version of application
		String version = (srcApp == WarDeploymentPlugin.SRC_APP_NAME_V1 ? "1.0" : "1.0.1");
		String response = TextUtils.merge("<h1>version: {0}</h1>", version);
		MockRESTServer mockRESTServer = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, response);

		try {
			int port = mockRESTServer.getPort();
			EasyTravelConfig config = EasyTravelConfig.read();
			config.backendPort = port;

			//execute plugin
			plugin.doExecute(PluginConstants.LIFECYCLE_PLUGIN_ENABLE, new Object[] {tomcatHolder});

			//check if war file was created
			File file = new File(Directories.getTempDir(), WarDeploymentPlugin.DST_APP_NAME);
			assertTrue("New war file does not exists: " + file.getAbsolutePath() ,file.exists());

			//compare size of the expected resource and generated war file; they should be equal
			String deployedApp = (srcApp == WarDeploymentPlugin.SRC_APP_NAME_V1 ? WarDeploymentPlugin.SRC_APP_NAME_V2 : WarDeploymentPlugin.SRC_APP_NAME_V1);
			int resourceSzie = getResourceSize(deployedApp);
			long warFileSize = file.length();
			assertEquals("Size of deployed app " + deployedApp + " should be the same as war file", resourceSzie, warFileSize);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Error comparing file sizes", e);
			fail();
		} finally {
			mockRESTServer.stop();
			EasyTravelConfig.resetSingleton();
		}
	}
}