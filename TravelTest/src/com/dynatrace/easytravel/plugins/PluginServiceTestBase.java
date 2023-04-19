package com.dynatrace.easytravel.plugins;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.ipc.SocketUtils;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.misc.RESTConstants;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.remote.HttpServiceThread;
import com.dynatrace.easytravel.tomcat.Tomcat7Starter;
import com.dynatrace.easytravel.util.DtVersionDetector;

public class PluginServiceTestBase extends TemplateConfigurationTestBase {
	private static Logger log = LoggerFactory.make();

	// we will overwrite these defaults with the next free port
	protected static int ServicePort = 7654;
	protected static int ServiceShutdownPort = 7655;

	protected static final String HOST = "localhost";

	private static final String PROTOCOL_HTTP = "http";

	private PluginService pluginService = null;
	private HttpServiceThread httpServiceThread = null;

	@BeforeClass
	public static void setUpClass() throws IOException {
		LoggerFactory.initLogging();
	}

	public static void setUpShutdownExecutor() throws Exception {

		Runnable myShutdownExecutor = new Runnable () {
			@Override
			public
			void run () {
				System.out.println("Exiting Tomcat");
			}
		};
		Tomcat7Starter.registerShutdownExecutor(myShutdownExecutor);
	}

	@Before
	public void prepareEasyTravelConfig() throws Exception {
		DtVersionDetector.enforceInstallationType(getInstallationType());

		ServicePort = SocketUtils.reserveNextFreePort(7000, 8000, null);
		ServiceShutdownPort = SocketUtils.reserveNextFreePort(7000, 8000, null);
		System.out.println("service port: <" + ServicePort + ">");
		System.out.println("service shutdown port: <" + ServiceShutdownPort + ">");

		EasyTravelConfig config = EasyTravelConfig.read();
		config.pluginServicePort = ServicePort;
		config.pluginServiceShutdownPort = ServiceShutdownPort;
		config.pluginServiceHost = HOST; config.bootPlugins = new String[0];
		config.apmServerDefault = getInstallationType();
		pluginService = new PluginService();

		setUpShutdownExecutor();
		pluginService.startHttpService();

		pluginService.reset();
	}

	protected InstallationType getInstallationType() {
		return InstallationType.Classic;
	}

	@After
	public void shutdown() throws MalformedURLException, InterruptedException {

		try {

			Socket socket = new Socket("localhost", ServiceShutdownPort);
			try {
				byte[] buffer = BaseConstants.TOMCAT_SHUTDOWN.getBytes();

				OutputStream outStream = socket.getOutputStream();
				try {
					outStream.write(buffer, 0, buffer.length);
					outStream.flush();

				} finally {
					IOUtils.closeQuietly(outStream);
				}
			} finally {
				socket.close();
			}

		} catch (Throwable t) {
			log.warn("unable to shutdown via shutdown http reqeust, trying to shut it down directly", t);
			if(httpServiceThread != null) {
				httpServiceThread.stopService();
			}
		}
		waitForServiceToStop();
		pluginService = null;
		httpServiceThread = null;
		assertTrue("Port has not been released", isPortAvailable(ServicePort));
	}

    private static boolean isPortAvailable(int port) {
    	try {

    	ServerSocket srv = new ServerSocket(port);

    	srv.close();
    	srv = null;
    	return true;

    	} catch (IOException e) {
    		return false;
    	}
    }

	protected void waitForServiceToStop() throws MalformedURLException, InterruptedException {
		for(int i = 0;i < 20;i++) {
			// wait until we cannot query the service any more
			if(!UrlUtils.checkConnect(getURL(RESTConstants.SHUTDOWN).toExternalForm()).isOK()) {
				break;
			}

			Thread.sleep(100);
		}

		assertFalse("REST interface should be gone now",
		UrlUtils.checkConnect(getURL(RESTConstants.SHUTDOWN).toExternalForm()).isOK());
	}

	protected static String retrieveData(String path) throws MalformedURLException, IOException {
		return UrlUtils.retrieveData(getURL(path).toExternalForm());
	}

	protected static String retrieveDataShutdown(String path) throws MalformedURLException, IOException {
		return UrlUtils.retrieveData(getURLShutdown(path).toExternalForm());
	}

	protected static final URL getURL(String path) throws MalformedURLException {
		if (path == null) {
			throw new NullPointerException("path must not be null");
		}

		if (!path.startsWith(BaseConstants.FSLASH)) {
			path = BaseConstants.FSLASH + path;
		}
		System.out.println("URL: "+new URL(PROTOCOL_HTTP, HOST, ServicePort, path).toExternalForm());
		return new URL(PROTOCOL_HTTP, HOST, ServicePort, path);
	}

	protected static final URL getURLShutdown(String path) throws MalformedURLException {
		if (path == null) {
			throw new NullPointerException("path must not be null");
		}

		if (!path.startsWith(BaseConstants.FSLASH)) {
			path = BaseConstants.FSLASH + path;
		}
		return new URL(PROTOCOL_HTTP, HOST, ServiceShutdownPort, path);
	}
}
