package com.dynatrace.easytravel.util;

import static com.dynatrace.easytravel.util.LocalUriProvider.getThirdPartyWebServiceUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;
import com.google.common.net.InetAddresses;

public class LocalUriProviderTest {
	private static final String LOCALHOST = "localhost";
	private static final String HOST127 = "127.0.0.1";

	@After
	public void tearDown() {
		// reset config to avoid dependencies between test-methods as we change some value in some of the tests
		EasyTravelConfig.resetSingleton();
		
        // clear cached values
        DtVersionDetector.clearCache();
        DtVersionDetector.enforceInstallationType(null);
	}

	@Test
	public void testDefaultUris()
	{
		EasyTravelConfig CONFIG = EasyTravelConfig.read();

		int frontendPort = 8080;
		int backendPort = 8091;
		String frontendContextRoot = "/";
		CONFIG.backendContextRoot = "/";
		String frontendUri = LocalUriProvider.getLocalUri(frontendPort, frontendContextRoot);
		String backendUri = LocalUriProvider.getLocalUri(backendPort, CONFIG.backendContextRoot);
		String wsUri = LocalUriProvider.getBackendWebServiceUri(BaseConstants.CONFIGURATION_SERVICE);

		Assert.assertEquals("http://localhost:8080/", frontendUri);
		Assert.assertEquals("http://localhost:8091/", backendUri);
		Assert.assertEquals("http://localhost:8091/services/" + BaseConstants.CONFIGURATION_SERVICE + "/", wsUri);
	}

	@Test
	public void testOtherContextRootUris()
	{
		EasyTravelConfig CONFIG = EasyTravelConfig.read();

		int frontendPort = 8080;
		int backendPort = 8091;
		String frontendContextRoot = "/easy";
		CONFIG.backendContextRoot = "/backend";
		CONFIG.webServiceBaseDir = "http://localhost:8091/backend/services/";
		String frontendUri = LocalUriProvider.getLocalUri(frontendPort, frontendContextRoot);
		String backendUri = LocalUriProvider.getLocalUri(backendPort, CONFIG.backendContextRoot);
		String wsUri = LocalUriProvider.getBackendWebServiceUri(BaseConstants.CONFIGURATION_SERVICE);

		Assert.assertEquals("http://localhost:8080/easy/", frontendUri);
		Assert.assertEquals("http://localhost:8091/backend/", backendUri);
		Assert.assertEquals("http://localhost:8091/backend/services/" + BaseConstants.CONFIGURATION_SERVICE + "/", wsUri);
	}

	@Test
	public void testOtherContextRootPortUris()
	{
		EasyTravelConfig CONFIG = EasyTravelConfig.read();

		int frontendPort = 9000;
		int backendPort = 8000;
		String frontendContextRoot = "/easy";
		CONFIG.backendContextRoot = "/backend";
		CONFIG.webServiceBaseDir = "http://localhost:8000/backend/services/";
		String frontendUri = LocalUriProvider.getLocalUri(frontendPort, frontendContextRoot);
		String backendUri = LocalUriProvider.getLocalUri(backendPort, CONFIG.backendContextRoot);
		String wsUri = LocalUriProvider.getBackendWebServiceUri("JourneyService");

		Assert.assertEquals("http://localhost:9000/easy/", frontendUri);
		Assert.assertEquals("http://localhost:8000/backend/", backendUri);
		Assert.assertEquals("http://localhost:8000/backend/services/JourneyService/", wsUri);
	}

	@Test
	public void testThirdPartyWebserviceUriPresent()
	{
		EasyTravelConfig CONFIG = EasyTravelConfig.read();
		CONFIG.thirdPartyWebserviceUri = "http://localhost:8000/thirdparty/services/";

		assertThat(getThirdPartyWebServiceUri("JourneyService"), is("http://localhost:8000/thirdparty/services/JourneyService/"));

		CONFIG.thirdPartyWebserviceUri = "http://localhost:8000/thirdparty/services";
		assertThat(getThirdPartyWebServiceUri("JourneyService"), is("http://localhost:8000/thirdparty/services/JourneyService/"));
	}

	@Test
	public void testIfThirdPartyWebserviceUriNotPresentUseWebServiceBaseDir()
	{
		EasyTravelConfig CONFIG = EasyTravelConfig.read();

		CONFIG.webServiceBaseDir = "http://localhost:8000/backend/services/";

		CONFIG.thirdPartyWebserviceUri = "";
		assertThat(getThirdPartyWebServiceUri("JourneyService"), is("http://localhost:8000/backend/services/JourneyService/"));

		CONFIG.thirdPartyWebserviceUri = " ";
		assertThat(getThirdPartyWebServiceUri("JourneyService"), is("http://localhost:8000/backend/services/JourneyService/"));

		CONFIG.thirdPartyWebserviceUri = null;
		assertThat(getThirdPartyWebServiceUri("JourneyService"), is("http://localhost:8000/backend/services/JourneyService/"));
	}


    @Test
    public void testSetPluginEnableSpaceBreak() {
        String name = "Dummy - Payment Service";
        String spaceChar = "%20";
        String sUri = LocalUriProvider.getBackendWebServiceUri(BaseConstants.CONFIGURATION_SERVICE, "setPluginEnabled?name={0}&enabled={1}", name, Boolean.toString(true));
        Assert.assertTrue(sUri.contains(spaceChar));
        Assert.assertEquals(3, StringUtils.countMatches(sUri, spaceChar));
    }

    @Test
    public void testRegisterPluginsSpaceBreak() {
        String name = "Dummy - Payment  Service";
        String spaceChar = "%20";
        String sUri = LocalUriProvider.getBackendWebServiceUri(BaseConstants.CONFIGURATION_SERVICE, "registerPlugins?pluginData={0}", name);
        System.out.println(sUri);
        Assert.assertTrue(sUri.contains(spaceChar));
        Assert.assertEquals(4, StringUtils.countMatches(sUri, spaceChar));
    }

    // helper method to get coverage of the unused constructor
    @Test
    public void testPrivateConstructor() throws Exception {

        PrivateConstructorCoverage.executePrivateConstructor(LocalUriProvider.class);
    }

	//========================================
	// A helper method to call LocalUriProvider.getFQDN()
	// on the passed string, and then confirm that the result (which will
	// be different on every system) is of appropriate format.
	//========================================

	private void testForHost(String host) {

		String fqdn = LocalUriProvider.getFQDN(host);

		assertFalse("Host <" + host + "> incorrectly converted (to <" + fqdn + ">", fqdn.equals(LOCALHOST));
		assertFalse("Host <" + host + "> incorrectly converted (to <" + fqdn + ">", fqdn.equals(HOST127));
		assertFalse("Host <" + host + "> incorrectly converted (to <" + fqdn + ">", InetAddresses.isInetAddress(fqdn));
		assertTrue(isFQDN(fqdn));
	}

	//========================================
	// A helper method to confirm that the passed string
	// looks like a fully qualified domain name or
	// at least like its first component (a host name).
	//========================================

	private boolean isFQDN(String fqdn) {

		// An FQDN should contain:
		// - not a period, repeated 0 or more times, followed by
		// - at least one not a digit nor a period, followed by
		// - not a period, repeated 0 or more times
		// - optionally followed by something...
		Pattern p = Pattern.compile("[^.]*[^.0-9]+[^.]*.*");

  		Matcher m = p.matcher(fqdn);
  		return m.find();
	}


	//========================================
	// Tests
	//========================================

	@Test
	public void testUri1() {
        DtVersionDetector.enforceInstallationType(InstallationType.APM);
		testForHost(LOCALHOST);
	}

	@Test
	public void testUri2() {
        DtVersionDetector.enforceInstallationType(InstallationType.APM);
		testForHost(HOST127);
	}

	@Test
	public void testUri3() {
        DtVersionDetector.enforceInstallationType(InstallationType.Classic);
		String fqdn = LocalUriProvider.getFQDN(LOCALHOST);
		// there should be NO conversion at all
		assertTrue("Host <" + LOCALHOST + "> incorrectly converted (to <" + fqdn + ">", fqdn.equals(LOCALHOST));
	}

	@Test
	public void testUri4() {
        DtVersionDetector.enforceInstallationType(InstallationType.Classic);
		String fqdn = LocalUriProvider.getFQDN(HOST127);
		// there should be NO conversion at all
		assertTrue("Host <" + HOST127 + "> incorrectly converted (to <" + fqdn + ">", fqdn.equals(HOST127));
	}


    @Test
    public void testNginxJavaFrontend() {
        EasyTravelConfig config = EasyTravelConfig.read();
		config.apmServerDefault = InstallationType.Classic;
		DtVersionDetector.enforceInstallationType(null);

		try {
			Properties prop = new Properties();
			prop.setProperty("config.nginxFrontendPublicUrl", "");

			config.enhance(prop);

			String url = LocalUriProvider.getURL(BaseConstants.UrlType.NGINX_JAVA_FRONTEND, true);
			Assert.assertThat(url, is("http://localhost:8079/"));

			url = LocalUriProvider.getURL(BaseConstants.UrlType.NGINX_JAVA_FRONTEND, false);
			Assert.assertThat(url, is("http://localhost:8079/"));

			prop.setProperty("config.nginxFrontendPublicUrl", "http://xxx:1234/");

			config.enhance(prop);

			url = LocalUriProvider.getURL(BaseConstants.UrlType.NGINX_JAVA_FRONTEND, true);
			Assert.assertThat(url, is("http://xxx:1234/"));

			url = LocalUriProvider.getURL(BaseConstants.UrlType.NGINX_JAVA_FRONTEND, false);
			Assert.assertThat(url, is("http://xxx:1234/"));
		} finally {
			EasyTravelConfig.resetSingleton();	
			DtVersionDetector.enforceInstallationType(null);
		}

        
    }

    @Test
    public void testApacheJavaFrontend() {
        EasyTravelConfig config = EasyTravelConfig.read();

        Properties prop = new Properties();
        prop.setProperty("config.apmServerDefault", "APM");
        prop.setProperty("config.disableFQDN", "false");
        prop.setProperty("config.apacheFrontendPublicUrl", "");

        config.enhance(prop);

        String url = LocalUriProvider.getURL(BaseConstants.UrlType.APACHE_JAVA_FRONTEND, true);
        Assert.assertThat("Domain name is expected", url,  not(containsString("localhost:8079")));

        url = LocalUriProvider.getURL(BaseConstants.UrlType.APACHE_JAVA_FRONTEND, false);
        Assert.assertThat("No domain name is expected", url, is("http://localhost:8079/"));

        prop.setProperty("config.apacheFrontendPublicUrl", "http://xxx:1234/");
        config.enhance(prop);

        url = LocalUriProvider.getURL(BaseConstants.UrlType.APACHE_JAVA_FRONTEND, true);
        Assert.assertThat("No FQDN check for apacheFrontendPublicUrl", url, is("http://xxx:1234/"));

        url = LocalUriProvider.getURL(BaseConstants.UrlType.APACHE_JAVA_FRONTEND, false);
        Assert.assertThat(url, is("http://xxx:1234/"));

        prop.setProperty("config.apacheFrontendPublicUrl", "http://frontend-1-hostname:8079/");
        prop.setProperty("config.apacheWebServerHost", "frontend-2-hostname");
        prop.setProperty("config.apacheWebServerPort", "8079");
        prop.setProperty("config.frontendContextRoot", "/");

        config.enhance(prop);

        // if the configuration contains config.apacheFrontendPublicUrl use this property as it is with no FQDN check
        // also config.apacheWebServerHost, config.apacheWebServerProt, config.frontendContextRoot are not transformed
        // for apacheFrontendPublicUrl puproses
        url = LocalUriProvider.getApacheFrontendPublicUrl();
        Assert.assertThat("", url, is("http://frontend-1-hostname:8079/"));

        EasyTravelConfig.resetSingleton();
    }

    @Test
    public void testApacheB2BFrontend() {
        EasyTravelConfig config = EasyTravelConfig.read();

        Properties prop = new Properties();
        prop.setProperty("config.apacheB2BFrontendPublicUrl", "");
        prop.setProperty("config.apmServerDefault", "APM");
        prop.setProperty("config.disableFQDN", "false");

        config.enhance(prop);

        String url = LocalUriProvider.getURL(BaseConstants.UrlType.APACHE_B2B_FRONTEND, true);
        Assert.assertThat("Domain name is expected", url,  not(containsString("localhost:8999")));

        url = LocalUriProvider.getURL(BaseConstants.UrlType.APACHE_B2B_FRONTEND, false);
        Assert.assertThat(url, is("http://localhost:8999/"));

        prop.setProperty("config.apacheB2BFrontendPublicUrl", "http://xxx:1234/");
        config.enhance(prop);

        url = LocalUriProvider.getURL(BaseConstants.UrlType.APACHE_B2B_FRONTEND, true);
        Assert.assertThat(url, is("http://xxx:1234/"));

        url = LocalUriProvider.getURL(BaseConstants.UrlType.APACHE_B2B_FRONTEND, false);
        Assert.assertThat(url, is("http://xxx:1234/"));

        prop.setProperty("config.apacheB2BFrontendPublicUrl", "http://frontendb2b-1-hostname:8999/");
        prop.setProperty("config.apacheWebServerB2bHost", "frontendb2b-2-hostname");
        prop.setProperty("config.apacheWebServerB2bPort", "8999");

        config.enhance(prop);

        // if the configuration contains config.apacheB2BFrontendPublicUrl use this property as it is with no FQDN check
        // also config.apacheWebServerB2bHost, config.apacheWebServerB2bPort, are not transformed
        // for apacheB2BFrontendPublicUrl puproses
        url = LocalUriProvider.getApacheB2BFrontendPublicUrl();
        Assert.assertThat("", url, is("http://frontendb2b-1-hostname:8999/"));

        EasyTravelConfig.resetSingleton();
    }

    @Test
    public void testNginxFrontendPublicUrl() {
        EasyTravelConfig config = EasyTravelConfig.read();

        Properties prop = new Properties();
        prop.setProperty("config.nginxFrontendPublicUrl", "");
        prop.setProperty("config.apmServerDefault", "APM");
        prop.setProperty("config.disableFQDN", "false");

        config.enhance(prop);

        String url = LocalUriProvider.getURL(BaseConstants.UrlType.NGINX_JAVA_FRONTEND, true);
        Assert.assertThat("Domain name is expected", url,  not(containsString("localhost:8079")));

        url = LocalUriProvider.getURL(BaseConstants.UrlType.NGINX_JAVA_FRONTEND, false);
        Assert.assertThat(url, is("http://localhost:8079/"));

        prop.setProperty("config.nginxFrontendPublicUrl", "http://xxx:1234/");
        config.enhance(prop);

        url = LocalUriProvider.getURL(BaseConstants.UrlType.NGINX_JAVA_FRONTEND, true);
        Assert.assertThat(url, is("http://xxx:1234/"));

        url = LocalUriProvider.getURL(BaseConstants.UrlType.NGINX_JAVA_FRONTEND, false);
        Assert.assertThat(url, is("http://xxx:1234/"));

        prop.setProperty("config.nginxFrontendPublicUrl", "http://nginxfrontend-1-hostname:8079/");
        prop.setProperty("config.nginxWebServerHost", "nginxfrontend-2-hostname");
        prop.setProperty("config.nginxWebServerPort", "8079");

        config.enhance(prop);

        // if the configuration contains config.nginxFrontendPublicUrl use this property as it is with no FQDN check
        // also config.nginxWebServerHost, config.nginxWebServerPort, are not transformed
        // for nginxFrontendPublicUrl puproses
        url = LocalUriProvider.getNginxFrontendPublicUrl();
        Assert.assertThat("", url, is("http://nginxfrontend-1-hostname:8079/"));

        EasyTravelConfig.resetSingleton();
    }


    @Test
    public void testNginxB2BFrontendPublicUrl() {
        EasyTravelConfig config = EasyTravelConfig.read();

        Properties prop = new Properties();
        prop.setProperty("config.nginxB2BFrontendPublicUrl", "");
        prop.setProperty("config.apmServerDefault", "APM");
        prop.setProperty("config.disableFQDN", "false");

        config.enhance(prop);

        String url = LocalUriProvider.getURL(BaseConstants.UrlType.NGINX_B2B_FRONTEND, true);
        Assert.assertThat("Domain name is expected", url,  not(containsString("localhost:8999")));

        url = LocalUriProvider.getURL(BaseConstants.UrlType.NGINX_B2B_FRONTEND, false);
        Assert.assertThat(url, is("http://localhost:8999/"));

        prop.setProperty("config.nginxB2BFrontendPublicUrl", "http://xxx:1234/");
        config.enhance(prop);

        url = LocalUriProvider.getURL(BaseConstants.UrlType.NGINX_B2B_FRONTEND, true);
        Assert.assertThat(url, is("http://xxx:1234/"));

        url = LocalUriProvider.getURL(BaseConstants.UrlType.NGINX_B2B_FRONTEND, false);
        Assert.assertThat(url, is("http://xxx:1234/"));

        prop.setProperty("config.nginxB2BFrontendPublicUrl", "http://nginxb2bfrontend-1-hostname:8999/");
        prop.setProperty("config.nginxWebServerB2bHost", "nginxb2bfrontend-2-hostname");
        prop.setProperty("config.nginxWebServerB2bPort", "8999");

        config.enhance(prop);

        // if the configuration contains config.nginxB2BFrontendPublicUrl use this property as it is with no FQDN check
        // also config.nginxWebServerB2bHost, config.nginxWebServerB2bPort, are not transformed
        // for nginxB2BFrontendPublicUrl puproses
        url = LocalUriProvider.getNginxB2BFrontendPublicUrl();
        Assert.assertThat("", url, is("http://nginxb2bfrontend-1-hostname:8999/"));

        EasyTravelConfig.resetSingleton();
    }

    @Test
    public void apacheProxyUrlType() {
        Throwable e = null;
        try {
            LocalUriProvider.getURL(BaseConstants.UrlType.APACHE_PROXY, false);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("The UrlType was initialy unspecified", e instanceof IllegalArgumentException);
    }

    @Test
    public void apacheBusinessBackendUrlType() {
        Throwable e = null;
        try {
            LocalUriProvider.getURL(BaseConstants.UrlType.APACHE_BUSINESS_BACKEND, false);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("The UrlType was initialy unspecified", e instanceof IllegalArgumentException);
    }

}
