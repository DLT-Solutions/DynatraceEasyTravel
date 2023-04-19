package com.dynatrace.easytravel.plugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.utils.TestHelpers;

public class PluginServiceTest extends PluginServiceTestBase {

	private static final int TIMEOUT = 30000;
	private static final String METHOD_PING = "ping";
	private static final String METHOD_ADD_DYNAMIC_PLUGIN = "addDynamicPlugin";
	private static final String EXPECTED_PING_REPLY = "Plugin Service on embedded Tomcat";

	@Test
	public void testPing() throws Exception {
		assertEquals(EXPECTED_PING_REPLY, UrlUtils.retrieveData(getURL(PluginService.CONTEXT + BaseConstants.FSLASH + METHOD_PING).toExternalForm(), 2000));
	}

	@Test
	public void testAddDynamicPlugin() throws Exception {
		assertEquals(Boolean.FALSE.toString(), UrlUtils.retrieveDataPost(
				getURL(PluginService.CONTEXT + BaseConstants.FSLASH + METHOD_ADD_DYNAMIC_PLUGIN + BaseConstants.FSLASH + "class" + BaseConstants.FSLASH + "bytes").toExternalForm(),
				BaseConstants.UTF8,
				"",
				"text/plain",
				TIMEOUT));
	}

	@Test
	public void testSetPluginHostsPost() throws Exception {
		assertEquals("[]", new PluginService().getEnabledPluginNamesForHost("host1"));

		new PluginService().registerPlugins("[\"DummyPaymentService:Group\"]");
		assertEquals("[\"DummyPaymentService\"]", new PluginService().getAllPluginNames());
		new PluginService().setPluginEnabled("DummyPaymentService", true);
		assertEquals("[\"DummyPaymentService\"]", new PluginService().getEnabledPluginNames());

		String url = getURL(PluginService.CONTEXT + "/setPluginHosts/DummyPaymentService").toExternalForm();
		assertEquals(Boolean.TRUE.toString(), UrlUtils.retrieveDataPost(
				url,
				BaseConstants.UTF8,
				"hosts=[host1,host2]",
				"text/plain",
				TIMEOUT));

		assertEquals("[\"DummyPaymentService\"]", new PluginService().getAllPluginNames());
		assertEquals("[\"DummyPaymentService\"]", new PluginService().getEnabledPluginNamesForHost("host1"));
		assertEquals("[\"DummyPaymentService\"]", new PluginService().getEnabledPluginNamesForHost("host2"));
		assertEquals("[]", new PluginService().getEnabledPluginNamesForHost("host3"));
	}

	@Test
	public void testGetEnabledPluginNames() throws Exception {
		assertEquals("[]", new PluginService().getEnabledPluginNames());

		assertEquals("[]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginNames").toExternalForm(),
				TIMEOUT));

		new PluginService().registerPlugins("[\"DummyPaymentService:Group\"]");
		assertEquals("[\"DummyPaymentService\"]", new PluginService().getAllPluginNames());
		new PluginService().setPluginEnabled("DummyPaymentService", true);
		assertEquals("[\"DummyPaymentService\"]", new PluginService().getEnabledPluginNames());

		assertEquals("[\"DummyPaymentService\"]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginNames").toExternalForm(),
				TIMEOUT));
	}


	@Test
	public void testGetStatus() throws Exception {
		assertEquals("[]", new PluginService().getEnabledPluginNames());

		assertEquals("", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/status").toExternalForm(),
				TIMEOUT));

		new PluginService().registerPlugins("[\"DummyPaymentService:Group\"]");
		assertEquals("[\"DummyPaymentService\"]", new PluginService().getAllPluginNames());
		new PluginService().registerPlugins("[\"DotNetPaymentService:Group\"]");
		assertEquals("[\"DotNetPaymentService\",\"DummyPaymentService\"]", new PluginService().getAllPluginNames());

		new PluginService().setPluginEnabled("DummyPaymentService", true);
		assertEquals("[\"DummyPaymentService\"]", new PluginService().getEnabledPluginNames());

		assertEquals("{\"plugin\":\"DotNetPaymentService\",\"enabled\":false,\"hosts\":null}{\"plugin\":\"DummyPaymentService\",\"enabled\":true,\"hosts\":null}", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/status").toExternalForm(),
				TIMEOUT));

		String url = getURL(PluginService.CONTEXT + "/setPluginHosts/DummyPaymentService").toExternalForm();
		assertEquals(Boolean.TRUE.toString(), UrlUtils.retrieveDataPost(
				url,
				BaseConstants.UTF8,
				"hosts=[host1,host2]",
				"text/plain",
				TIMEOUT));

		assertEquals("{\"plugin\":\"DotNetPaymentService\",\"enabled\":false,\"hosts\":null}{\"plugin\":\"DummyPaymentService\",\"enabled\":true,\"hosts\":[\"host1\",\"host2\"]}", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/status").toExternalForm(),
				TIMEOUT));

		url = getURL(PluginService.CONTEXT + "/setPluginHosts/DotNetPaymentService").toExternalForm();
		assertEquals(Boolean.TRUE.toString(), UrlUtils.retrieveDataPost(
				url,
				BaseConstants.UTF8,
				"hosts=[host3]",
				"text/plain",
				TIMEOUT));

		assertEquals("{\"plugin\":\"DotNetPaymentService\",\"enabled\":false,\"hosts\":[\"host3\"]}{\"plugin\":\"DummyPaymentService\",\"enabled\":true,\"hosts\":[\"host1\",\"host2\"]}", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/status").toExternalForm(),
				TIMEOUT));

		new PluginService().registerPlugins("[\"ThirdPaymentService:Group\"]");
		assertEquals("[\"DotNetPaymentService\",\"DummyPaymentService\",\"ThirdPaymentService\"]", new PluginService().getAllPluginNames());

		assertEquals("{\"plugin\":\"DotNetPaymentService\",\"enabled\":false,\"hosts\":[\"host3\"]}{\"plugin\":\"DummyPaymentService\",\"enabled\":true,\"hosts\":[\"host1\",\"host2\"]}{\"plugin\":\"ThirdPaymentService\",\"enabled\":false,\"hosts\":null}", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/status").toExternalForm(),
				TIMEOUT));
	}

	@Test
	public void testGetEnabledPlugins() throws Exception {
		assertEquals("[]", new PluginService().getAllPluginNames());
		assertEquals("[]", new PluginService().getEnabledPluginNames());

		assertEquals("[]", UrlUtils.retrieveData(getURL(PluginService.CONTEXT + "/getEnabledPlugins").toExternalForm(), TIMEOUT));

		new PluginService().registerPlugins("[\"DummyPaymentService:Group\"]");
		assertEquals("[]", UrlUtils.retrieveData(getURL(PluginService.CONTEXT + "/getEnabledPlugins").toExternalForm(), TIMEOUT));

		new PluginService().registerPlugins("[\"DotNetPaymentService:Group\"]");
		assertEquals("[]", UrlUtils.retrieveData(getURL(PluginService.CONTEXT + "/getEnabledPlugins").toExternalForm(), TIMEOUT));

		new PluginService().setPluginEnabled("DummyPaymentService", true);
		assertEquals("[\"DummyPaymentService:Group:Both\"]", UrlUtils.retrieveData(getURL(PluginService.CONTEXT + "/getEnabledPlugins").toExternalForm(), TIMEOUT));

		new PluginService().registerPlugins("[\"ThirdPaymentService:Group\"]");
		assertEquals("[\"DummyPaymentService:Group:Both\"]", UrlUtils.retrieveData(getURL(PluginService.CONTEXT + "/getEnabledPlugins").toExternalForm(), TIMEOUT));

		new PluginService().setPluginEnabled("ThirdPaymentService", true);
		assertEquals("[\"DummyPaymentService:Group:Both\",\"ThirdPaymentService:Group:Both\"]", UrlUtils.retrieveData(getURL(PluginService.CONTEXT + "/getEnabledPlugins").toExternalForm(), TIMEOUT));
	}

	@Test
	public void testGetAllPlugins() throws Exception {
		assertEquals("[]", new PluginService().getAllPluginNames());
		assertEquals("[]", new PluginService().getEnabledPluginNames());

		assertEquals("[]", UrlUtils.retrieveData(getURL(PluginService.CONTEXT + "/getAllPlugins").toExternalForm(), TIMEOUT));

		new PluginService().registerPlugins("[\"DummyPaymentService:Group\"]");
		assertEquals("[\"DummyPaymentService:Group:Both\"]", UrlUtils.retrieveData(getURL(PluginService.CONTEXT + "/getAllPlugins").toExternalForm(), TIMEOUT));

		new PluginService().registerPlugins("[\"DotNetPaymentService:Group\"]");
		assertEquals("[\"DotNetPaymentService:Group:Both\",\"DummyPaymentService:Group:Both\"]", UrlUtils.retrieveData(getURL(PluginService.CONTEXT + "/getAllPlugins").toExternalForm(), TIMEOUT));

		new PluginService().setPluginEnabled("DummyPaymentService", true);
		assertEquals("[\"DotNetPaymentService:Group:Both\",\"DummyPaymentService:Group:Both\"]", UrlUtils.retrieveData(getURL(PluginService.CONTEXT + "/getAllPlugins").toExternalForm(), TIMEOUT));

		new PluginService().registerPlugins("[\"ThirdPaymentService:Group\"]");
		assertEquals("[\"DotNetPaymentService:Group:Both\",\"DummyPaymentService:Group:Both\",\"ThirdPaymentService:Group:Both\"]", UrlUtils.retrieveData(getURL(PluginService.CONTEXT + "/getAllPlugins").toExternalForm(), TIMEOUT));
	}

	@Test
	public void testGetEnabledPluginsForHost() throws Exception {
		assertEquals("[]", new PluginService().getEnabledPluginNames());

		// no plugins enabled yet
		assertEquals("[]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/host1").toExternalForm(),
				TIMEOUT));

		// register plugins and enable one of them
		new PluginService().registerPlugins("[\"DummyPaymentService:Group\"]");
		assertEquals("[\"DummyPaymentService\"]", new PluginService().getAllPluginNames());
		new PluginService().registerPlugins("[\"DotNetPaymentService:Group\"]");
		assertEquals("[\"DotNetPaymentService\",\"DummyPaymentService\"]", new PluginService().getAllPluginNames());

		new PluginService().setPluginEnabled("DummyPaymentService", true);
		assertEquals("[\"DummyPaymentService\"]", new PluginService().getEnabledPluginNames());

		// now the enabled plugin is enabled on the hosts as no hosts are set at all
		assertEquals("[\"DummyPaymentService:Group:Both\"]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/host1").toExternalForm(),
				TIMEOUT));

		// set some hosts
		String url = getURL(PluginService.CONTEXT + "/setPluginHosts/DummyPaymentService").toExternalForm();
		assertEquals(Boolean.TRUE.toString(), UrlUtils.retrieveDataPost(
				url,
				BaseConstants.UTF8,
				"hosts=[host1,host2]",
				"text/plain",
				TIMEOUT));

		// now the host is enabled on the defined host, but not on others
		assertEquals("[\"DummyPaymentService:Group:Both\"]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/host1").toExternalForm(),
				TIMEOUT));
		assertEquals("[]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/hostN").toExternalForm(),
				TIMEOUT));

		// set plugin host for second plugin (which is not enabled yet)
		url = getURL(PluginService.CONTEXT + "/setPluginHosts/DotNetPaymentService").toExternalForm();
		assertEquals(Boolean.TRUE.toString(), UrlUtils.retrieveDataPost(
				url,
				BaseConstants.UTF8,
				"hosts=[host3]",
				"text/plain",
				TIMEOUT));

		// no plugin is enabled on the third host yet
		assertEquals("[\"DummyPaymentService:Group:Both\"]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/host1").toExternalForm(),
				TIMEOUT));
		assertEquals("[\"DummyPaymentService:Group:Both\"]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/host2").toExternalForm(),
				TIMEOUT));
		assertEquals("[]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/host3").toExternalForm(),
				TIMEOUT));

		// now enable the plugin which is set for the third host
		new PluginService().setPluginEnabled("DotNetPaymentService", true);

		// then we have a plugin on host3 as well
		assertEquals("[\"DummyPaymentService:Group:Both\"]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/host1").toExternalForm(),
				TIMEOUT));
		assertEquals("[\"DummyPaymentService:Group:Both\"]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/host2").toExternalForm(),
				TIMEOUT));
		assertEquals("[\"DotNetPaymentService:Group:Both\"]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/host3").toExternalForm(),
				TIMEOUT));
		assertEquals("[]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/hostN").toExternalForm(),
				TIMEOUT));

		// then change the list of hosts
		url = getURL(PluginService.CONTEXT + "/setPluginHosts/DummyPaymentService").toExternalForm();
		assertEquals(Boolean.TRUE.toString(), UrlUtils.retrieveDataPost(
				url,
				BaseConstants.UTF8,
				"hosts=[host1]",
				"text/plain",
				TIMEOUT));

		// and verify that the plugin is now not enabled any more on host2
		assertEquals("[\"DummyPaymentService:Group:Both\"]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/host1").toExternalForm(),
				TIMEOUT));
		assertEquals("[]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/host2").toExternalForm(),
				TIMEOUT));
		assertEquals("[\"DotNetPaymentService:Group:Both\"]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/host3").toExternalForm(),
				TIMEOUT));
		assertEquals("[]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/hostN").toExternalForm(),
				TIMEOUT));

		// next register hosts for a plugin that is not yet registered
		url = getURL(PluginService.CONTEXT + "/setPluginHosts/FourthPaymentService").toExternalForm();
		assertEquals(Boolean.TRUE.toString(), UrlUtils.retrieveDataPost(
				url,
				BaseConstants.UTF8,
				"hosts=[host4]",
				"text/plain",
				TIMEOUT));
		// still not registered/enabled
		assertEquals("[]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/host4").toExternalForm(),
				TIMEOUT));
		// register the plugin
		new PluginService().registerPlugins("[\"FourthPaymentService:Group\"]");
		// still not enabled
		assertEquals("[]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/host4").toExternalForm(),
				TIMEOUT));

		// finally enable it
		new PluginService().setPluginEnabled("FourthPaymentService", true);
		// now it shows up on that host
		assertEquals("[\"FourthPaymentService:Group:Both\"]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/host4").toExternalForm(),
				TIMEOUT));
		// all others stay the same
		assertEquals("[\"DummyPaymentService:Group:Both\"]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/host1").toExternalForm(),
				TIMEOUT));
		assertEquals("[]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/host2").toExternalForm(),
				TIMEOUT));
		assertEquals("[\"DotNetPaymentService:Group:Both\"]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/host3").toExternalForm(),
				TIMEOUT));
		assertEquals("[]", UrlUtils.retrieveData(
				getURL(PluginService.CONTEXT + "/getEnabledPluginsForHost/hostN").toExternalForm(),
				TIMEOUT));
	}


	@Test
	public void testInvalidJSON() throws Exception {
		String url = getURL(PluginService.CONTEXT + "/setPluginHosts/FourthPaymentService").toExternalForm();
		try {
			UrlUtils.retrieveDataPost(
				url,
				BaseConstants.UTF8,
				"hosts=host1,host2]",
				"text/plain",
				TIMEOUT);
			fail("Should catch exception here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "500");
		}

		try {
			new PluginService().registerPlugins("[\"DummyPayment\\[Service:Group\"]");
			fail("Should catch exception here");
		} catch (WebApplicationException e) {
			TestHelpers.assertContains(e, "JSONException");
		}
	}

	// look at PluginServiceMainTest for tests which cover the main-method of class PluginService
}
