package com.dynatrace.easytravel.launcher.httpd;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Test;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.UrlType;
import com.dynatrace.easytravel.launcher.engine.ProcedureFactory;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;


public class VirtualHostTest {
	private ByteArrayOutputStream out = new ByteArrayOutputStream();
	private PrintWriter writer = new PrintWriter(out);
	private static final String CONFIG_DIRECTORY = Directories.getConfigDir().getAbsolutePath() + File.separator;

	@After
	public void tearDown() {
		// we modify the config for some tests, ensure that we reset it at the end of the test
		EasyTravelConfig.resetSingleton();
	}


	@Test
	public void testWrite() throws IOException {
		VirtualHost.writeCustomerAndB2bVirtualHostEntries(writer, false);
		writer.close();
		out.close();

		String result = out.toString();
		EasyTravelConfig config = EasyTravelConfig.read();

		assertEquals(PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.apacheWebServerPort + ">\n" +
                    REWRITE_ENGINE_BLOCK +
					PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				"<VirtualHost *:" + config.apacheWebServerB2bPort + ">\n" +
				"	ProxyPass / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"	ProxyPassReverse / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"</VirtualHost>\n" +
				"<VirtualHost *:" + config.apacheWebServerProxyPort + ">\n" +
					PROXY_BLOCK +
				"</VirtualHost>\n" +
				ANGULAR_PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.angularFrontendApachePort + ">\n" +
				ANGULAR_PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				getApacheWebserverSslConfig(), result.replace("\r", ""));
	}
	
	@Test
	public void testWriteSkipProxyBlock() throws IOException {
		EasyTravelConfig config = EasyTravelConfig.read();
		config.apacheWebServerProxyPort = 0;
		
		VirtualHost.writeCustomerAndB2bVirtualHostEntries(writer, false);
		writer.close();
		out.close();

		String result = out.toString();

		assertEquals(PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.apacheWebServerPort + ">\n" +
                    REWRITE_ENGINE_BLOCK +
					PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				"<VirtualHost *:" + config.apacheWebServerB2bPort + ">\n" +
				"	ProxyPass / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"	ProxyPassReverse / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"</VirtualHost>\n" +
				ANGULAR_PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.angularFrontendApachePort + ">\n" +
				ANGULAR_PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				getApacheWebserverSslConfig(), result.replace("\r", ""));
		
		EasyTravelConfig.resetSingleton();
	}

	@Test
	public void testWritePHP() throws IOException {
		VirtualHost.writeCustomerAndB2bVirtualHostEntries(writer, true);
		writer.close();
		out.close();

		String result = out.toString();
		EasyTravelConfig config = EasyTravelConfig.read();

		assertEquals(PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.apacheWebServerPort + ">\n" +
				"	ProxyPass /rating !\n" +
				"	ProxyPass /blog !\n" +
                    REWRITE_ENGINE_BLOCK +
					PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				"<VirtualHost *:" + config.apacheWebServerB2bPort + ">\n" +
				"	ProxyPass / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"	ProxyPassReverse / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"</VirtualHost>\n" +
				"<VirtualHost *:" + config.apacheWebServerProxyPort + ">\n" +
					PROXY_BLOCK +
				"</VirtualHost>\n" +
				ANGULAR_PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.angularFrontendApachePort + ">\n" +
				ANGULAR_PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				getApacheWebserverSslConfig(), result.replace("\r", ""));
	}


	@Test
	public void testWriteRemoteB2B() throws IOException {
		System.setProperty("com.dynatrace.easytravel.host.b2b_frontend", "myhost123");
		try {
			// verify that this has the expected result
			String hostOrLocal = ProcedureFactory.getHostOrLocal(Constants.Procedures.B2B_FRONTEND_ID);
			assertEquals("had: " + hostOrLocal, "myhost123", hostOrLocal);

			VirtualHost.writeCustomerAndB2bVirtualHostEntries(writer, false);
		} finally {
			System.clearProperty("com.dynatrace.easytravel.host.b2b_frontend");
		}
		writer.close();
		out.close();

		String result = out.toString();
		EasyTravelConfig config = EasyTravelConfig.read();

		assertEquals(EXTENDED_PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.apacheWebServerPort + ">\n" +
                    REWRITE_ENGINE_BLOCK +
					PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				"<VirtualHost *:" + config.apacheWebServerB2bPort + ">\n" +
				"	ProxyPass / http://myhost123:" + config.b2bFrontendPortRangeStart + "/\n" +
				"	ProxyPassReverse / http://myhost123:" + config.b2bFrontendPortRangeStart + "/\n" +
				"</VirtualHost>\n" +
				"<VirtualHost *:" + config.apacheWebServerProxyPort + ">\n" +
					PROXY_BLOCK +
				"</VirtualHost>\n" +
				EXTENDED_ANGULAR_PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.angularFrontendApachePort + ">\n" +
				ANGULAR_PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				getApacheWebserverSslConfig(), result.replace("\r", ""));
	}

	@Test
	public void testWriteRemoteCustomerFrontend() throws IOException {
		System.setProperty("com.dynatrace.easytravel.host.customer_frontend", "myhost123");
		try {
			// verify that this has the expected result
			String hostOrLocal = ProcedureFactory.getHostOrLocal(Constants.Procedures.CUSTOMER_FRONTEND_ID);
			assertEquals("had: " + hostOrLocal, "myhost123", hostOrLocal);

			VirtualHost.writeCustomerAndB2bVirtualHostEntries(writer, false);
		} finally {
			System.clearProperty("com.dynatrace.easytravel.host.customer_frontend");
		}
		writer.close();
		out.close();

		String result = out.toString();
		EasyTravelConfig config = EasyTravelConfig.read();

		assertEquals(EXTENDED_PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.apacheWebServerPort + ">\n" +
                    REWRITE_ENGINE_BLOCK +
					PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				"<VirtualHost *:" + config.apacheWebServerB2bPort + ">\n" +
				"	ProxyPass / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"	ProxyPassReverse / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"</VirtualHost>\n" +
				"<VirtualHost *:" + config.apacheWebServerProxyPort + ">\n" +
					PROXY_BLOCK +
				"</VirtualHost>\n" +
				EXTENDED_ANGULAR_PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.angularFrontendApachePort + ">\n" +
				ANGULAR_PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				getApacheWebserverSslConfig(), result.replace("\r", ""));
	}

	@Test
	public void testWriteRemoteBusinessBackend() throws IOException {
		ApacheConf apacheConf = new ApacheConf();
		apacheConf.setRelevantUrlTypes(new UrlType[] {null, UrlType.APACHE_BUSINESS_BACKEND});
		VirtualHost.writeCustomerAndB2bVirtualHostEntries(writer, apacheConf);
		writer.close();
		out.close();

		String result = out.toString();
		EasyTravelConfig config = EasyTravelConfig.read();

		assertEquals("<VirtualHost *:" + config.backendPort + ">\n" +
					BACKEND_CLUSTER_BLOCK +
				"</VirtualHost>\n" + getApacheWebserverSslConfig(), result.replace("\r", ""));
	}

	@Test
	public void testWriteRemoteBusinessBackendInternet() throws IOException {
		EasyTravelConfig.read().apacheWebServerHost = "host1.domain.com";
		EasyTravelConfig.read().apacheWebServerEnableVirtualIp = true;

		ApacheConf apacheConf = new ApacheConf();
		apacheConf.setRelevantUrlTypes(new UrlType[] {null, UrlType.APACHE_BUSINESS_BACKEND});
		VirtualHost.writeCustomerAndB2bVirtualHostEntries(writer, apacheConf);
		writer.close();
		out.close();

		String result = out.toString();

		assertEquals("<VirtualHost 127.0.0.2>\n" +
					INTERNET_URL_PROXY +
					BACKEND_CLUSTER_BLOCK +
				"</VirtualHost>\n" + getApacheWebserverSslConfig(), result.replace("\r", ""));
	}


	@Test
	public void testWriteRemoteBusinessBackendInternetNoIp() throws IOException {
		EasyTravelConfig.read().apacheWebServerHost = "host1.domain.com";

		ApacheConf apacheConf = new ApacheConf();
		apacheConf.setRelevantUrlTypes(new UrlType[] {null, UrlType.APACHE_BUSINESS_BACKEND});
		VirtualHost.writeCustomerAndB2bVirtualHostEntries(writer, apacheConf);
		writer.close();
		out.close();

		String result = out.toString();

		assertEquals("<VirtualHost *:8091>\n" +
					BACKEND_CLUSTER_BLOCK +
				"</VirtualHost>\n" + getApacheWebserverSslConfig(), result.replace("\r", ""));
	}


	@Test
	public void testWriteFirewall() throws IOException {
		EasyTravelConfig.read().apacheWebServerSimulatesFirewall = true;

		VirtualHost.writeCustomerAndB2bVirtualHostEntries(writer, false);
		writer.close();
		out.close();

		String result = out.toString();
		EasyTravelConfig config = EasyTravelConfig.read();

		assertEquals(PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.apacheWebServerPort + ">\n" +
                    REWRITE_ENGINE_BLOCK +
					PROXY_CLUSTER_BLOCK +
					FIREWALL_BLOCK +
				"</VirtualHost>\n" +
				"<VirtualHost *:" + config.apacheWebServerB2bPort + ">\n" +
				"	ProxyPass / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"	ProxyPassReverse / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
					FIREWALL_BLOCK +
				"</VirtualHost>\n" +
				"<VirtualHost *:" + config.apacheWebServerProxyPort + ">\n" +
					PROXY_BLOCK +
					FIREWALL_BLOCK +
				"</VirtualHost>\n" +
				ANGULAR_PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.angularFrontendApachePort + ">\n" +
				ANGULAR_PROXY_CLUSTER_BLOCK +
				FIREWALL_BLOCK +
				"</VirtualHost>\n" +
				getApacheWebserverSslConfig(), result.replace("\r", ""));
	}

	@Test
	public void testWriteNonInternetUrl() throws IOException {
		EasyTravelConfig.read().apacheWebServerHost = "host1.dynatrace.local";
		EasyTravelConfig.read().apacheWebServerB2bHost = "host2.dynatrace.vmta";

		VirtualHost.writeCustomerAndB2bVirtualHostEntries(writer, false);
		writer.close();
		out.close();

		String result = out.toString();
		EasyTravelConfig config = EasyTravelConfig.read();

		assertEquals(PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.apacheWebServerPort + ">\n" +
                    REWRITE_ENGINE_BLOCK +
					PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				"<VirtualHost *:" + config.apacheWebServerB2bPort + ">\n" +
				"	ProxyPass / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"	ProxyPassReverse / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"</VirtualHost>\n" +
				"<VirtualHost *:" + config.apacheWebServerProxyPort + ">\n" +
					PROXY_BLOCK +
				"</VirtualHost>\n" +
				ANGULAR_PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.angularFrontendApachePort + ">\n" +
				ANGULAR_PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				getApacheWebserverSslConfig(), result.replace("\r", ""));
	}

	@Test
	public void testWriteNonInternetUrl2() throws IOException {
		EasyTravelConfig.read().apacheWebServerHost = "host1.example";
		EasyTravelConfig.read().apacheWebServerB2bHost = "dynatrace.com";

		VirtualHost.writeCustomerAndB2bVirtualHostEntries(writer, false);
		writer.close();
		out.close();

		String result = out.toString();
		EasyTravelConfig config = EasyTravelConfig.read();

		assertEquals(PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.apacheWebServerPort + ">\n" +
                    REWRITE_ENGINE_BLOCK +
					PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				"<VirtualHost *:" + config.apacheWebServerB2bPort + ">\n" +
				"	ProxyPass / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"	ProxyPassReverse / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"</VirtualHost>\n" +
				"<VirtualHost *:" + config.apacheWebServerProxyPort + ">\n" +
					PROXY_BLOCK +
				"</VirtualHost>\n" +
				ANGULAR_PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.angularFrontendApachePort + ">\n" +
				ANGULAR_PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				getApacheWebserverSslConfig(), result.replace("\r", ""));
	}

	@Test
	public void testWriteInternetUrl() throws IOException {
		EasyTravelConfig.read().apacheWebServerHost = "host1.domain.com";
		EasyTravelConfig.read().apacheWebServerB2bHost = "host2.domain.com";
		EasyTravelConfig.read().apacheWebServerEnableVirtualIp = true;

		VirtualHost.writeCustomerAndB2bVirtualHostEntries(writer, false);
		writer.close();
		out.close();

		String result = out.toString();
		EasyTravelConfig config = EasyTravelConfig.read();

		assertEquals(PROXY_BALANCER_BLOCK +
				"<VirtualHost 127.0.0.2>\n" +
					INTERNET_URL_PROXY +
                    REWRITE_ENGINE_BLOCK +
					PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				"<VirtualHost 127.0.0.3>\n" +
					INTERNET_URL_PROXY +
				"	ProxyPass / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"	ProxyPassReverse / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"</VirtualHost>\n" +
				"<VirtualHost *:" + config.apacheWebServerProxyPort + ">\n" +
					PROXY_BLOCK +
				"</VirtualHost>\n" +
				ANGULAR_PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.angularFrontendApachePort + ">\n" +
				ANGULAR_PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				getApacheWebserverSslConfig(), result.replace("\r", ""));
	}

	@Test
	public void testWriteInternetUrl2() throws IOException {
		EasyTravelConfig.read().apacheWebServerHost = "host1.domain.com";
		EasyTravelConfig.read().apacheWebServerB2bHost = "193.213.23.12";
		EasyTravelConfig.read().apacheWebServerEnableVirtualIp = true;

		VirtualHost.writeCustomerAndB2bVirtualHostEntries(writer, false);
		writer.close();
		out.close();

		String result = out.toString();
		EasyTravelConfig config = EasyTravelConfig.read();

		assertEquals(PROXY_BALANCER_BLOCK +
				"<VirtualHost 127.0.0.2>\n" +
					INTERNET_URL_PROXY +
                    REWRITE_ENGINE_BLOCK +
					PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				"<VirtualHost 127.0.0.3>\n" +
					INTERNET_URL_PROXY +
				"	ProxyPass / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"	ProxyPassReverse / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"</VirtualHost>\n" +
				"<VirtualHost *:" + config.apacheWebServerProxyPort + ">\n" +
					PROXY_BLOCK +
				"</VirtualHost>\n" +
				ANGULAR_PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.angularFrontendApachePort + ">\n" +
				ANGULAR_PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				getApacheWebserverSslConfig(), result.replace("\r", ""));
	}

	@Test
	public void testWriteVirtualIp() throws IOException {
		EasyTravelConfig.read().apacheWebServerVirtualIp = "host1.domain.com";
		EasyTravelConfig.read().apacheWebServerB2bVirtualIp = "193.213.23.12";
		EasyTravelConfig.read().apacheWebServerHost = "host1.domain.com";
		EasyTravelConfig.read().apacheWebServerB2bHost = "193.213.23.12";
		EasyTravelConfig.read().apacheWebServerEnableVirtualIp = true;

		VirtualHost.writeCustomerAndB2bVirtualHostEntries(writer, false);
		writer.close();
		out.close();

		String result = out.toString();
		EasyTravelConfig config = EasyTravelConfig.read();

		assertEquals(PROXY_BALANCER_BLOCK +
				"<VirtualHost host1.domain.com>\n" +
					INTERNET_URL_PROXY +
                    REWRITE_ENGINE_BLOCK +
					PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				"<VirtualHost 193.213.23.12>\n" +
					INTERNET_URL_PROXY +
				"	ProxyPass / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"	ProxyPassReverse / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"</VirtualHost>\n" +
				"<VirtualHost *:" + config.apacheWebServerProxyPort + ">\n" +
					PROXY_BLOCK +
				"</VirtualHost>\n" +
				ANGULAR_PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.angularFrontendApachePort + ">\n" +
				ANGULAR_PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				getApacheWebserverSslConfig(), result.replace("\r", ""));
	}

	@Test
	public void testWriteVirtualIpStar() throws IOException {
		EasyTravelConfig.read().apacheWebServerVirtualIp = "*:8080";
		EasyTravelConfig.read().apacheWebServerB2bVirtualIp = "*:8999";
		EasyTravelConfig.read().apacheWebServerHost = "host1.domain.com";
		EasyTravelConfig.read().apacheWebServerB2bHost = "193.213.23.12";
		EasyTravelConfig.read().apacheWebServerEnableVirtualIp = true;

		VirtualHost.writeCustomerAndB2bVirtualHostEntries(writer, false);
		writer.close();
		out.close();

		String result = out.toString();
		EasyTravelConfig config = EasyTravelConfig.read();

		assertEquals(PROXY_BALANCER_BLOCK +
				"<VirtualHost *:8080>\n" +
					INTERNET_URL_PROXY +
                    REWRITE_ENGINE_BLOCK +
					PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				"<VirtualHost *:8999>\n" +
					INTERNET_URL_PROXY +
				"	ProxyPass / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"	ProxyPassReverse / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"</VirtualHost>\n" +
				"<VirtualHost *:" + config.apacheWebServerProxyPort + ">\n" +
					PROXY_BLOCK +
				"</VirtualHost>\n" +
				ANGULAR_PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.angularFrontendApachePort + ">\n" +
				ANGULAR_PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				getApacheWebserverSslConfig(), result.replace("\r", ""));
	}

	@Test
	public void testWriteVirtualIp2() throws IOException {
		EasyTravelConfig.read().apacheWebServerVirtualIp = "host1.domain.com";
		EasyTravelConfig.read().apacheWebServerB2bVirtualIp = "193.213.23.12";
		EasyTravelConfig.read().apacheWebServerHost = "host1.domain.com";
		EasyTravelConfig.read().apacheWebServerB2bHost = "193.213.23.12";
		EasyTravelConfig.read().frontendContextRoot = ".com";
		EasyTravelConfig.read().apacheWebServerEnableVirtualIp = true;

		VirtualHost.writeCustomerAndB2bVirtualHostEntries(writer, false);
		writer.close();
		out.close();

		String result = out.toString();
		EasyTravelConfig config = EasyTravelConfig.read();

		assertEquals(PROXY_BALANCER_BLOCK +
				"<VirtualHost host1.domain>\n" +
					REWRITE_BLOCK +
					INTERNET_URL_PROXY +
                    REWRITE_ENGINE_BLOCK +
					PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				"<VirtualHost 193.213.23.12>\n" +
					INTERNET_URL_PROXY +
				"	ProxyPass / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"	ProxyPassReverse / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"</VirtualHost>\n" +
				"<VirtualHost *:" + config.apacheWebServerProxyPort + ">\n" +
					PROXY_BLOCK +
				"</VirtualHost>\n" +
				ANGULAR_PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.angularFrontendApachePort + ">\n" +
				ANGULAR_PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				getApacheWebserverSslConfig(), result.replace("\r", ""));
	}

	@Test
	public void testWriteVirtualIp3() throws IOException {
		EasyTravelConfig.read().apacheWebServerVirtualIp = "localhost";
		EasyTravelConfig.read().apacheWebServerB2bVirtualIp = "www.example.com";
		EasyTravelConfig.read().apacheWebServerHost = "localhost";
		EasyTravelConfig.read().apacheWebServerB2bHost = "www.example.com";
		EasyTravelConfig.read().apacheWebServerEnableVirtualIp = true;

		// TODO: here we set "frontendContextRoot", but it has effect on the backend-config!
		EasyTravelConfig.read().frontendContextRoot = ".com";

		VirtualHost.writeCustomerAndB2bVirtualHostEntries(writer, false);
		writer.close();
		out.close();

		String result = out.toString();
		EasyTravelConfig config = EasyTravelConfig.read();

		assertEquals(PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.apacheWebServerPort + ">\n" +
					REWRITE_BLOCK +		// TODO: is this expected here?!?
                    REWRITE_ENGINE_BLOCK +
					PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				"<VirtualHost www.example>\n" +
					// TODO: would this be expected here? REWRITE_BLOCK +
					INTERNET_URL_PROXY +
				"	ProxyPass / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"	ProxyPassReverse / http://localhost:" + config.b2bFrontendPortRangeStart + "/\n" +
				"</VirtualHost>\n" +
				"<VirtualHost *:" + config.apacheWebServerProxyPort + ">\n" +
					PROXY_BLOCK +
				"</VirtualHost>\n" +
				ANGULAR_PROXY_BALANCER_BLOCK +
				"<VirtualHost *:" + config.angularFrontendApachePort + ">\n" +
				ANGULAR_PROXY_CLUSTER_BLOCK +
				"</VirtualHost>\n" +
				getApacheWebserverSslConfig(), result.replace("\r", ""));
	}

	@Test
	public void testWriteCustomerSslHostEntryDisabled() throws Exception {
		EasyTravelConfig.read().apacheWebServerSslPort = 0;
		VirtualHost.writeCustomerSslHostEntry(writer);
		writer.close();
		out.close();

		String result = out.toString();
		assertEquals("", result);
	}

	@Test
	public void testWriteCustomerSslHostEntryEnabled() throws Exception {
		EasyTravelConfig.read().apacheWebServerSslPort = 9443;
		EasyTravelConfig.read().apacheWebServerSslHost = "ssl-host";
		EasyTravelConfig.read().apacheWebServerPort = 8079;
		EasyTravelConfig.read().apacheWebServerHost = "localhost";
		VirtualHost.writeCustomerSslHostEntry(writer);
		File certificateFile = new File(CONFIG_DIRECTORY, "easytravel-san.crt");
		File certificateKey = new File(CONFIG_DIRECTORY, "easytravel-san.key");
		String certificateDirectory = CONFIG_DIRECTORY;
		writer.close();
		out.close();

		String result = out.toString();
		
		/*
		 * If there is no certificate in user area Apache will not be running
		 */
		if(!(certificateFile.exists() && certificateKey.exists())){
			certificateDirectory="conf/ssl/";
		}
	
		String expectedResult = "<VirtualHost *:9443>\n" +
				"    ServerName \"ssl-host\"\n" +
				"    SSLEngine on\n" +
				"    SSLProtocol -all +TLSv1.2\n" +
				"    SSLCertificateFile \""+certificateDirectory+"easytravel-san.crt\"\n"+
				"    SSLCertificateKeyFile \""+certificateDirectory+"easytravel-san.key\"\n"+
				"    ProxyPass / http://localhost:8079/\n" +
				"    ProxyPassReverse / http://localhost:8079/\n" +
				"</VirtualHost>\n";

		assertEquals(expectedResult, result.replace("\r", ""));
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(VirtualHost.class);
	}

	/**
	 * returns the apache vhost configuration for ssl connections (if enabled)
	 *
	 * @return
	 */
	private String getApacheWebserverSslConfig() {
		EasyTravelConfig config = EasyTravelConfig.read();
		File certificateFile = new File(CONFIG_DIRECTORY, "easytravel-san.crt");
		File certificateKey = new File(CONFIG_DIRECTORY, "easytravel-san.key");
		String certificateDirectory = CONFIG_DIRECTORY;
		String apacheWebserverSslConfig = "";
		if (config.apacheWebServerSslPort > 0) {
			String hostName = config.apacheWebServerSslHost;

			if (hostName == null || hostName.trim().equals(BaseConstants.EMPTY_STRING)) {
				try {
				    InetAddress addr = InetAddress.getLocalHost();
				    hostName = addr.getHostName();
				} catch (UnknownHostException e) {
					hostName = "localhost";
				}
			}
			
			/*
			 * If there is no certificate in user area Apache will not be running
			 */
			if(!(certificateFile.exists() && certificateKey.exists())){
				certificateDirectory="conf/ssl/";
			}
			
			String uriApacheWebServer = "http://" + config.apacheWebServerHost + ":" + config.apacheWebServerPort + "/";

			apacheWebserverSslConfig =
					"<VirtualHost *:" + config.apacheWebServerSslPort + ">\n" +
					"    ServerName \"" + hostName + "\"\n" +
					"    SSLEngine on\n" +
					"    SSLProtocol -all +TLSv1.2\n" +
					"    SSLCertificateFile \""+certificateDirectory+"easytravel-san.crt\"\n"+
					"    SSLCertificateKeyFile \""+certificateDirectory+"easytravel-san.key\"\n"+
					"    ProxyPass / " + uriApacheWebServer + "\n" + //FIXME
					"    ProxyPassReverse / " + uriApacheWebServer + "\n" + //FIXME
					"</VirtualHost>\n";
		}
		return apacheWebserverSslConfig;
	}

	private final static String FIREWALL_BLOCK =
			"	<LocationMatch \"^/dtagent_.*\\.js\">\n" +
			"		Order allow,deny\n" +
			"		Deny from all\n" +
			"	</LocationMatch>\n" +
			"	<LocationMatch \"^/dynaTraceMonitor.*\">\n" +
			"		Order allow,deny\n" +
			"		Deny from all\n" +
			"	</LocationMatch>\n";

	private final static String PROXY_CLUSTER_BLOCK =
			"	ProxyPass / balancer://mycluster/\n" +
					"	ProxyPassReverse / balancer://mycluster/\n";

	private final static String ANGULAR_PROXY_CLUSTER_BLOCK =
			"	ProxyPass / balancer://angcluster/\n" +
					"	ProxyPassReverse / balancer://angcluster/\n";

    private final static String REWRITE_ENGINE_BLOCK =
            "	RewriteEngine On\n" +
            "	RewriteOptions Inherit\n";

	private final static String BACKEND_CLUSTER_BLOCK =
			"	ProxyPass / balancer://backendcluster/\n" +
			"	ProxyPassReverse / balancer://backendcluster/\n";


	private final static String INTERNET_URL_PROXY =
			"	ProxyRequests Off\n" +
			"	ProxyPreserveHost On\n";

	private final static String PROXY_BLOCK =
			"	RequestHeader unset Accept-Encoding\n" +
			"	ProxyRequests On\n";

	private final static String REWRITE_BLOCK =
			"	RewriteEngine On\n" +
			"	RewriteRule   ^/$  .com/ [R]\n";

	private final static String PROXY_BALANCER_BLOCK =
			"<Proxy balancer://mycluster>\n" +
					"    ProxySet stickysession=JSESSIONID\n" +
					"    BalancerMember ajp://localhost:8280 route=jvmRoute-8280 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://localhost:8281 route=jvmRoute-8281 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://localhost:8282 route=jvmRoute-8282 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://localhost:8283 route=jvmRoute-8283 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://localhost:8284 route=jvmRoute-8284 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://localhost:8285 route=jvmRoute-8285 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://localhost:8286 route=jvmRoute-8286 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://localhost:8287 route=jvmRoute-8287 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://localhost:8288 route=jvmRoute-8288 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://localhost:8289 route=jvmRoute-8289 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://localhost:8290 route=jvmRoute-8290 connectiontimeout=10 retry=120\n" +
					"</Proxy>\n";

	private final static String EXTENDED_PROXY_BALANCER_BLOCK =
			"<Proxy balancer://mycluster>\n" +
			"    ProxySet stickysession=JSESSIONID\n" +
			"    BalancerMember ajp://myhost123:8280 route=jvmRoute-8280 connectiontimeout=10 retry=120\n" +
			"    BalancerMember ajp://myhost123:8281 route=jvmRoute-8281 connectiontimeout=10 retry=120\n" +
			"    BalancerMember ajp://myhost123:8282 route=jvmRoute-8282 connectiontimeout=10 retry=120\n" +
			"    BalancerMember ajp://myhost123:8283 route=jvmRoute-8283 connectiontimeout=10 retry=120\n" +
			"    BalancerMember ajp://myhost123:8284 route=jvmRoute-8284 connectiontimeout=10 retry=120\n" +
			"    BalancerMember ajp://myhost123:8285 route=jvmRoute-8285 connectiontimeout=10 retry=120\n" +
			"    BalancerMember ajp://myhost123:8286 route=jvmRoute-8286 connectiontimeout=10 retry=120\n" +
			"    BalancerMember ajp://myhost123:8287 route=jvmRoute-8287 connectiontimeout=10 retry=120\n" +
			"    BalancerMember ajp://myhost123:8288 route=jvmRoute-8288 connectiontimeout=10 retry=120\n" +
			"    BalancerMember ajp://myhost123:8289 route=jvmRoute-8289 connectiontimeout=10 retry=120\n" +
			"    BalancerMember ajp://myhost123:8290 route=jvmRoute-8290 connectiontimeout=10 retry=120\n" +
			"    BalancerMember ajp://localhost:8280 route=jvmRoute-8280 connectiontimeout=10 retry=120\n" +
			"    BalancerMember ajp://localhost:8281 route=jvmRoute-8281 connectiontimeout=10 retry=120\n" +
			"    BalancerMember ajp://localhost:8282 route=jvmRoute-8282 connectiontimeout=10 retry=120\n" +
			"    BalancerMember ajp://localhost:8283 route=jvmRoute-8283 connectiontimeout=10 retry=120\n" +
			"    BalancerMember ajp://localhost:8284 route=jvmRoute-8284 connectiontimeout=10 retry=120\n" +
			"    BalancerMember ajp://localhost:8285 route=jvmRoute-8285 connectiontimeout=10 retry=120\n" +
			"    BalancerMember ajp://localhost:8286 route=jvmRoute-8286 connectiontimeout=10 retry=120\n" +
			"    BalancerMember ajp://localhost:8287 route=jvmRoute-8287 connectiontimeout=10 retry=120\n" +
			"    BalancerMember ajp://localhost:8288 route=jvmRoute-8288 connectiontimeout=10 retry=120\n" +
			"    BalancerMember ajp://localhost:8289 route=jvmRoute-8289 connectiontimeout=10 retry=120\n" +
			"    BalancerMember ajp://localhost:8290 route=jvmRoute-8290 connectiontimeout=10 retry=120\n" +
			"</Proxy>\n";

	private final static String ANGULAR_PROXY_LOCALHOST_BLOCK =
			"    BalancerMember ajp://localhost:9280 route=jvmRoute-9280 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://localhost:9281 route=jvmRoute-9281 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://localhost:9282 route=jvmRoute-9282 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://localhost:9283 route=jvmRoute-9283 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://localhost:9284 route=jvmRoute-9284 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://localhost:9285 route=jvmRoute-9285 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://localhost:9286 route=jvmRoute-9286 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://localhost:9287 route=jvmRoute-9287 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://localhost:9288 route=jvmRoute-9288 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://localhost:9289 route=jvmRoute-9289 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://localhost:9290 route=jvmRoute-9290 connectiontimeout=10 retry=120\n";

	private final static String ANGULAR_PROXY_MYHOST_BLOCK =
			"    BalancerMember ajp://myhost123:9280 route=jvmRoute-9280 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://myhost123:9281 route=jvmRoute-9281 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://myhost123:9282 route=jvmRoute-9282 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://myhost123:9283 route=jvmRoute-9283 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://myhost123:9284 route=jvmRoute-9284 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://myhost123:9285 route=jvmRoute-9285 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://myhost123:9286 route=jvmRoute-9286 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://myhost123:9287 route=jvmRoute-9287 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://myhost123:9288 route=jvmRoute-9288 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://myhost123:9289 route=jvmRoute-9289 connectiontimeout=10 retry=120\n" +
					"    BalancerMember ajp://myhost123:9290 route=jvmRoute-9290 connectiontimeout=10 retry=120\n";

	private final static String ANGULAR_PROXY_BALANCER_BLOCK =
			"<Proxy balancer://angcluster>\n" +
					"    ProxySet stickysession=JSESSIONID\n" +
					ANGULAR_PROXY_LOCALHOST_BLOCK +
					"</Proxy>\n";

	private final static String EXTENDED_ANGULAR_PROXY_BALANCER_BLOCK =
			"<Proxy balancer://angcluster>\n" +
					"    ProxySet stickysession=JSESSIONID\n" +
					ANGULAR_PROXY_MYHOST_BLOCK +
					ANGULAR_PROXY_LOCALHOST_BLOCK +
					"</Proxy>\n";
}
