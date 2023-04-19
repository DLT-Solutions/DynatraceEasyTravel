package com.dynatrace.easytravel.launcher.panels;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.easymock.internal.matchers.Any;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dynatrace.easytravel.config.CustomerTrafficScenarioEnum;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.LauncherUI;
import com.dynatrace.easytravel.launcher.engine.AbstractStopListener;
import com.dynatrace.easytravel.launcher.engine.Procedure;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.engine.StatefulProcedure;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.PluginInfoList;
import com.dynatrace.easytravel.weblauncher.LauncherUIRAP;

import ch.qos.logback.classic.Logger;

@RunWith(Parameterized.class)
public class HeaderPanelListenerTest {

	private static final Logger log = LoggerFactory.make();

	HeaderPanelInterface header = createStrictMock(HeaderPanelInterface.class);
	HeaderPanelListener listener = new HeaderPanelListener(header);

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] { { true }, { false } };
		return Arrays.asList(data);
	}

	private boolean standalone;

	public HeaderPanelListenerTest(boolean standalone) {
		this.standalone = standalone;
	}

	@Before
	public void setUp() {
		log.info("Running with standalone set to: " + standalone);

		if (standalone) {
			Launcher.addLauncherUI(new LauncherUI());
			Launcher.setIsWeblauncher(false);
		}
		else {
			Launcher.addLauncherUI(new LauncherUIRAP());
			Launcher.setIsWeblauncher(true);
		}
	}

	@Test
	public void testNotifyProcedureStateChangedB2BNull() {
		header.updateB2BFrontendUri(null);

		Procedure procedure = createStrictMock(Procedure.class);
		procedure.addStopListener(anyStopListener());
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.B2B_FRONTEND_ID));
		expect(procedure.getURI()).andReturn("uri1");

		replay(procedure, header);

		listener.notifyProcedureStateChanged(new StatefulProcedure(procedure), State.UNKNOWN, State.STOPPED);

		verify(procedure, header);
	}

	@Test
	public void testNotifyProcedureStateChangedB2BNotNull() {
		header.updateB2BFrontendUri("uri1");

		Procedure procedure = createStrictMock(Procedure.class);
		procedure.addStopListener(anyStopListener());
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.B2B_FRONTEND_ID));
		expect(procedure.getURI()).andReturn("uri1");
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.B2B_FRONTEND_ID));

		replay(procedure, header);

		listener.notifyProcedureStateChanged(new StatefulProcedure(procedure), State.UNKNOWN, State.STARTING);

		verify(procedure, header);
	}

	@Test
	public void testNotifyProcedureStateChangedB2BNotNullWithLink() {
		header.updateB2BFrontendUri("<a href=\"uri1\" target=\"_blank\">uri1</a>");

		Procedure procedure = createStrictMock(Procedure.class);
		procedure.addStopListener(anyStopListener());
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.B2B_FRONTEND_ID));
		expect(procedure.getURI()).andReturn("uri1").times(2);
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.B2B_FRONTEND_ID));

		replay(procedure, header);

		listener.notifyProcedureStateChanged(new StatefulProcedure(procedure), State.UNKNOWN, State.OPERATING);

		verify(procedure, header);
	}


	@Test
	public void testNotifyProcedureStateChangedCustomerNull() {
		Procedure procedure = createStrictMock(Procedure.class);
		procedure.addStopListener(anyStopListener());
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
		expect(procedure.getURI()).andReturn("uri1");
		if (!standalone) {
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1");
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1");
		}

		replay(procedure);

		StatefulProcedure subject = new StatefulProcedure(procedure);
		header.updateCustomerFrontendUri(subject.toString(), null);
		if (!standalone) {
			header.updateAndroidFrontendUri(null);
			header.updateIOSFrontendUri(null);
		}

		replay(header);

		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.STOPPED);

		verify(procedure, header);
	}

	@Test
	public void testNotifyProcedureStateChangedCustomerNotNull() {
		Procedure procedure = createStrictMock(Procedure.class);
		procedure.addStopListener(anyStopListener());
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
		expect(procedure.getURI()).andReturn("uri1");
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
		if (!standalone) {
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1");
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1");
		}

		replay(procedure);

		StatefulProcedure subject = new StatefulProcedure(procedure);
		header.updateCustomerFrontendUri(subject.toString(), "uri1");
		if (!standalone) {
			header.updateAndroidFrontendUri("Android");
			header.updateIOSFrontendUri("iOS");
		}

		replay(header);

		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.STARTING);

		verify(procedure, header);
	}

	@Test
	public void testNotifyProcedureStateChangedApacheHTTPDNotNull() {
		Procedure procedure = createStrictMock(Procedure.class);
		procedure.addStopListener(anyStopListener());
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.APACHE_HTTPD_ID));
		expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1");
		expect(procedure.getURI(BaseConstants.UrlType.APACHE_B2B_FRONTEND)).andReturn("uri2");

		if (!standalone) {
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1");
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1");
		}

		replay(procedure);
		StatefulProcedure subject = new StatefulProcedure(procedure);
		header.updateWebserverCustomerFrontendUri("uri1");
		header.updateWebserverB2bFrontendUri("uri2");
		if (!standalone) {
			header.updateWebserverAndroidFrontendUri("Android");
			header.updateWebserverIOSFrontendUri("iOS");
		}

		replay(header);

		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.STARTING);

		verify(procedure, header);
	}


	@Test
	public void testNotifyProcedureStateChangedCustomerNotNullStateFailed() {
		Procedure procedure = createStrictMock(Procedure.class);
		procedure.addStopListener(anyStopListener());
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
		expect(procedure.getURI()).andReturn("uri1");
		if (!standalone) {
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1");
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1");
		}

		replay(procedure);

		StatefulProcedure subject = new StatefulProcedure(procedure);
		header.updateCustomerFrontendUri(subject.toString(), null);
		if (!standalone) {
			header.updateAndroidFrontendUri(null);
			header.updateIOSFrontendUri(null);
		}

		replay(header);

		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.FAILED);

		verify(procedure, header);
	}

	@Test
	public void testNotifyProcedureStateChangedApacheHTTPDNotNullStateFailed() {
		Procedure procedure = createStrictMock(Procedure.class);
		procedure.addStopListener(anyStopListener());
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.APACHE_HTTPD_ID));
		expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1");
		expect(procedure.getURI(BaseConstants.UrlType.APACHE_B2B_FRONTEND)).andReturn("uri2");
		if (!standalone) {
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1");
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1");
		}

		replay(procedure);

		StatefulProcedure subject = new StatefulProcedure(procedure);
		header.updateWebserverCustomerFrontendUri(null);
		header.updateWebserverB2bFrontendUri(null);
		if (!standalone) {
			header.updateWebserverAndroidFrontendUri(null);
			header.updateWebserverIOSFrontendUri(null);
		}

		replay(header);

		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.FAILED);

		verify(procedure, header);
	}

	@Test
	public void testNotifyProcedureStateChangedCustomerNotNullWithLink() {
		Procedure procedure = createStrictMock(Procedure.class);
		procedure.addStopListener(anyStopListener());
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
		expect(procedure.getURI()).andReturn("uri1").times(2);
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
		if (!standalone) {
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1");
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1");
		}

		replay(procedure);

		StatefulProcedure subject = new StatefulProcedure(procedure);
		header.updateCustomerFrontendUri(subject.toString(), "<a href=\"uri1\" target=\"_blank\">uri1</a>");
		if (!standalone) {
			header.updateAndroidFrontendUri("<a href=\"uri1apps/EasyTravelAndroid.apk\" target=\"_blank\">Android</a>");
			header.updateIOSFrontendUri("<a href=\"itms-services://?action=download-manifest&url=uri1apps/easyTravel.plist\" target=\"_blank\">iOS</a>");
		}

		replay(header);

		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.OPERATING);

		verify(procedure, header);
	}

	@Test
	public void testNotifyProcedureStateChangedCustomerNullURI() {
		Procedure procedure = createStrictMock(Procedure.class);
		procedure.addStopListener(anyStopListener());
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
		expect(procedure.getURI()).andReturn("uri1").times(2);
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
		if (!standalone) {
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn(null);
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn(null);
		}

		replay(procedure);

		StatefulProcedure subject = new StatefulProcedure(procedure);
		header.updateCustomerFrontendUri(subject.toString(), "<a href=\"uri1\" target=\"_blank\">uri1</a>");
		if (!standalone) {
			header.updateAndroidFrontendUri(null);
			header.updateIOSFrontendUri(null);
		}

		replay(header);

		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.OPERATING);

		verify(procedure, header);
	}

	@Test
	public void testNotifyProcedureStateChangedCustomerNotNullWithLinkStateTimeout() {
		Procedure procedure = createStrictMock(Procedure.class);
		procedure.addStopListener(anyStopListener());
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
		expect(procedure.getURI()).andReturn("uri1").times(2);
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
		if (!standalone) {
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1");
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1");
		}

		replay(procedure);

		StatefulProcedure subject = new StatefulProcedure(procedure);
		header.updateCustomerFrontendUri(subject.toString(), "<a href=\"uri1\" target=\"_blank\">uri1</a>");
		if (!standalone) {
			header.updateAndroidFrontendUri("<a href=\"uri1apps/EasyTravelAndroid.apk\" target=\"_blank\">Android</a>");
			header.updateIOSFrontendUri("<a href=\"itms-services://?action=download-manifest&url=uri1apps/easyTravel.plist\" target=\"_blank\">iOS</a>");
		}

		replay(header);

		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.TIMEOUT);

		verify(procedure, header);
	}

	@Test
	public void testNotifyProcedureStateChangedApacheHTTPDNotNullWithLinkStateTimeout() {
		Procedure procedure = createStrictMock(Procedure.class);
		procedure.addStopListener(anyStopListener());
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.APACHE_HTTPD_ID));
		expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1").times(2);
		expect(procedure.getURI(BaseConstants.UrlType.APACHE_B2B_FRONTEND)).andReturn("uri2").times(2);
		if (!standalone) {
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1");
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1");
		}

		replay(procedure);

		StatefulProcedure subject = new StatefulProcedure(procedure);
		header.updateWebserverCustomerFrontendUri("<a href=\"uri1\" target=\"_blank\">uri1</a>");
		header.updateWebserverB2bFrontendUri("<a href=\"uri2\" target=\"_blank\">uri2</a>");
		if (!standalone) {
			header.updateWebserverAndroidFrontendUri("<a href=\"uri1apps/EasyTravelAndroid.apk\" target=\"_blank\">Android</a>");
			header.updateWebserverIOSFrontendUri("<a href=\"itms-services://?action=download-manifest&url=uri1apps/easyTravel.plist\" target=\"_blank\">iOS</a>");
		}

		replay(header);

		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.TIMEOUT);

		verify(procedure, header);
	}

	@Test
	public void testNotifyProcedureStateChangedApacheHTTPDNullURI() {
		Procedure procedure = createStrictMock(Procedure.class);
		procedure.addStopListener(anyStopListener());
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.APACHE_HTTPD_ID));
		expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn(null).times(2);
		expect(procedure.getURI(BaseConstants.UrlType.APACHE_B2B_FRONTEND)).andReturn(null).times(2);
		if (!standalone) {
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn(null);
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn(null);
		}

		replay(procedure);

		StatefulProcedure subject = new StatefulProcedure(procedure);
		header.updateWebserverCustomerFrontendUri(null);
		header.updateWebserverB2bFrontendUri(null);
		if (!standalone) {
			header.updateWebserverAndroidFrontendUri(null);
			header.updateWebserverIOSFrontendUri(null);
		}

		replay(header);

		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.TIMEOUT);

		verify(procedure, header);
	}

	@Test
	public void testNotifyProcedureStateChangedApachePHPHTTPDNotNullWithLinkStateTimeout() {
		Procedure procedure = createStrictMock(Procedure.class);
		procedure.addStopListener(anyStopListener());
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.APACHE_HTTPD_PHP_ID));
		expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1").times(2);
		expect(procedure.getURI(BaseConstants.UrlType.APACHE_B2B_FRONTEND)).andReturn("uri2").times(2);
		if (!standalone) {
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1");
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn("uri1");
		}

		replay(procedure);

		StatefulProcedure subject = new StatefulProcedure(procedure);
		header.updateWebserverCustomerFrontendUri("<a href=\"uri1\" target=\"_blank\">uri1</a>");
		header.updateWebserverB2bFrontendUri("<a href=\"uri2\" target=\"_blank\">uri2</a>");
		if (!standalone) {
			header.updateWebserverAndroidFrontendUri("<a href=\"uri1apps/EasyTravelAndroid.apk\" target=\"_blank\">Android</a>");
			header.updateWebserverIOSFrontendUri("<a href=\"itms-services://?action=download-manifest&url=uri1apps/easyTravel.plist\" target=\"_blank\">iOS</a>");
		}

		replay(header);

		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.TIMEOUT);

		verify(procedure, header);
	}

	@Test
	public void testNotifyProcedureStateChangedOtherProc() {
		Procedure procedure = createStrictMock(Procedure.class);
		procedure.addStopListener(anyStopListener());
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.ANT_ID));

		replay(procedure, header);

		StatefulProcedure subject = new StatefulProcedure(procedure);

		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.OPERATING);

		verify(procedure, header);
	}

	private AbstractStopListener anyStopListener() {
		reportMatcher(Any.ANY);
		return null;
	}

	@Test
	public void testNotifyEnabledPluginsEmptyList() {
		// false when DC-RUM is not in list of plugins
		header.setDCRUMVisible(false);

		replay(header);

		listener.notifyEnabledPlugins(new PluginInfoList());

		verify(header);
	}

	@Test
	public void testNotifyEnabledPluginsSomeEntries() {
		// false when DC-RUM is not in list of plugins
		header.setDCRUMVisible(false);

		replay(header);

		listener.notifyEnabledPlugins(new PluginInfoList(new String[] { "plugin1", "plugin2" }));

		verify(header);
	}

	@Test
	public void testNotifyEnabledPluginsWithDCRUM() {
		// false when DC-RUM is not in list of plugins
		header.setDCRUMVisible(true);

		replay(header);

		listener.notifyEnabledPlugins(new PluginInfoList(new String[] { "plugin1", "plugin2",
				BaseConstants.Plugins.DC_RUM_EMULATOR }));

		verify(header);
	}

	@Test
	public void testNotifyConfigLoaded() {
		EasyTravelConfig.read().baseLoadDefault = 20; // make test work even if we set different load in properties
		EasyTravelConfig.read().customerLoadScenario = CustomerTrafficScenarioEnum.EasyTravelFixed;

		//1 update
		// once false, then true a few times
		expect(header.isConfigControllingAllowed()).andReturn(false);

		//2 update
		header.setCustomerTrafficScenario(CustomerTrafficScenarioEnum.EasyTravelFixed);
		expect(header.isConfigControllingAllowed()).andReturn(true);
		header.setLoad(20);

		//3 update
		header.setCustomerTrafficScenario(CustomerTrafficScenarioEnum.EasyTravel);
		expect(header.isConfigControllingAllowed()).andReturn(true);
		header.setLoad(0);

		//4 update
		header.setCustomerTrafficScenario(CustomerTrafficScenarioEnum.EasyTravelFixed);
		expect(header.isConfigControllingAllowed()).andReturn(true);
		header.setLoad(20);

		//5 update
		expect(header.isConfigControllingAllowed()).andReturn(true);

		//6 update
		expect(header.isConfigControllingAllowed()).andReturn(true);

		//7 update
		expect(header.isConfigControllingAllowed()).andReturn(true);

		//8 update
		header.setCustomerTrafficScenario(CustomerTrafficScenarioEnum.EasyTravelPredictable);
		expect(header.isConfigControllingAllowed()).andReturn(true);
		header.setLoad(anyInt());
		expectLastCall().times(0, 1);

		replay(header);

		listener.notifyConfigLoaded(null, EasyTravelConfig.read()); //1 update
		listener.notifyConfigLoaded(null, EasyTravelConfig.read()); //2 update

		EasyTravelConfig newCfg = new EasyTravelConfig();
		newCfg.baseLoadB2BRatio = 30;
		listener.notifyConfigLoaded(null, newCfg); 					//3 update

		listener.notifyConfigLoaded(null, EasyTravelConfig.read()); //4 update
		listener.notifyConfigLoaded(null, EasyTravelConfig.read()); //5 update
		listener.notifyConfigLoaded(null, EasyTravelConfig.read()); //6 update
		listener.notifyConfigLoaded(null, EasyTravelConfig.read()); //7 update

		//8 update test changing CustomerLoadScenario
		EasyTravelConfig.applyCustomSettings(Collections.singletonMap("config.customerLoadScenario", "EasyTravelPredictable"));
		listener.notifyConfigLoaded(null, EasyTravelConfig.read());

		verify(header);
	}

	@Test
	public void testShortenName() {
		// returns name if compare or name is null
		assertEquals(null, HeaderPanelListener.shortenName(null, null));
		assertEquals("", HeaderPanelListener.shortenName("", null));
		assertEquals(null, HeaderPanelListener.shortenName(null, ""));
		assertEquals(null, HeaderPanelListener.shortenName(null, "sometext"));

		// returns name of nothing in common
		assertEquals("name1", HeaderPanelListener.shortenName("name1", ""));
		assertEquals("name1234567890", HeaderPanelListener.shortenName("name1234567890", ""));
		assertEquals("name1234567890", HeaderPanelListener.shortenName("name1234567890", "othername"));
		assertEquals("name1234567890", HeaderPanelListener.shortenName("name1234567890", "name1234567891"));
		assertEquals("name1234567890", HeaderPanelListener.shortenName("name1234567890", "name123456789"));

		// returns shorter name if suffix matches, but at least 7 chars are returned
		assertEquals("name123", HeaderPanelListener.shortenName("name1234567890", "name1234567890"));
		assertEquals("http://uri2", HeaderPanelListener.shortenName("http://uri2.dynatrace.com", "http://uri1.dynatrace.com"));

		// real-world-example
		assertEquals("http://apmng-centos-1", HeaderPanelListener.shortenName("http://apmng-centos-1.emea.cpwr.corp:8080", "http://apmng-centos-2.emea.cpwr.corp:8080"));
	}

	@Test
	public void testNotifyProcedureStateChangedCustomerShorting() {
		Procedure procedure = createStrictMock(Procedure.class);
		procedure.addStopListener(anyStopListener());
		prepareProcedure(procedure, "http://uri1.dynatrace.com");

		replay(procedure);

		Procedure procedure2 = createStrictMock(Procedure.class);
		procedure2.addStopListener(anyStopListener());
		prepareProcedure(procedure2, "http://uri2.dynatrace.com");

		replay(procedure2);

		StatefulProcedure subject = new StatefulProcedure(procedure);
		expectHeaderCall(subject, true, "http://uri1.dynatrace.com", "http://uri1.dynatrace.com");

		StatefulProcedure subject2 = new StatefulProcedure(procedure2);
		expectHeaderCall(subject2, true, "http://uri2.dynatrace.com", "http://uri2");

		replay(header);

		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.OPERATING);

		listener.notifyProcedureStateChanged(subject2, State.UNKNOWN, State.OPERATING);

		verify(procedure, header);
	}

	@Test
	public void testNotifyProcedureStateChangedStartingCustomerShorting() {
		Procedure procedure = createStrictMock(Procedure.class);
		procedure.addStopListener(anyStopListener());
		prepareProcedure(procedure, "http://uri1.dynatrace.com");
		prepareProcedure(procedure, "http://uri1.dynatrace.com");
		prepareProcedure(procedure, "http://uri1.dynatrace.com");
		prepareProcedure(procedure, "http://uri1.dynatrace.com");

		replay(procedure);

		Procedure procedure2 = createStrictMock(Procedure.class);
		procedure2.addStopListener(anyStopListener());
		prepareProcedure(procedure2, "http://uri2.dynatrace.com");
		prepareProcedure(procedure2, "http://uri2.dynatrace.com");
		prepareProcedure(procedure2, "http://uri2.dynatrace.com");
		prepareProcedure(procedure2, "http://uri2.dynatrace.com");

		replay(procedure2);

		StatefulProcedure subject = new StatefulProcedure(procedure);
		expectHeaderCall(subject, false, "http://uri1.dynatrace.com", "http://uri1.dynatrace.com");
		expectHeaderCall(subject, false, "http://uri1.dynatrace.com", "http://uri1.dynatrace.com");
		expectHeaderCall(subject, true, "http://uri1.dynatrace.com", "http://uri1.dynatrace.com");
		expectHeaderCall(subject, true, "http://uri1.dynatrace.com", "http://uri1.dynatrace.com");

		StatefulProcedure subject2 = new StatefulProcedure(procedure2);
		expectHeaderCall(subject2, false, "http://uri2.dynatrace.com", "http://uri2");
		expectHeaderCall(subject2, false, "http://uri2.dynatrace.com", "http://uri2");
		expectHeaderCall(subject2, true, "http://uri2.dynatrace.com", "http://uri2");
		expectHeaderCall(subject2, true, "http://uri2.dynatrace.com", "http://uri2");

		replay(header);

		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.STARTING);
		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.STARTING);
		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.OPERATING);
		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.TIMEOUT);

		listener.notifyProcedureStateChanged(subject2, State.UNKNOWN, State.STARTING);
		listener.notifyProcedureStateChanged(subject2, State.UNKNOWN, State.STARTING);
		listener.notifyProcedureStateChanged(subject2, State.UNKNOWN, State.OPERATING);
		listener.notifyProcedureStateChanged(subject2, State.UNKNOWN, State.TIMEOUT);

		verify(procedure, header);
	}

	private void expectHeaderCall(StatefulProcedure subject, boolean withLink, String uri, String name) {
		header.updateCustomerFrontendUri(subject.toString(), withLink ? ("<a href=\"" + uri + "\" target=\"_blank\">" + name + "</a>") : name);
		if (!standalone) {
			header.updateAndroidFrontendUri(withLink ? "<a href=\"" + uri + "apps/EasyTravelAndroid.apk\" target=\"_blank\">Android</a>" : "Android");
			header.updateIOSFrontendUri(withLink ? "<a href=\"itms-services://?action=download-manifest&url=" + uri + "apps/easyTravel.plist\" target=\"_blank\">iOS</a>" : "iOS");
		}
	}

	private void prepareProcedure(Procedure procedure, String uri) {
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
		expect(procedure.getURI()).andReturn(uri).times(1,2);
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
		if (!standalone) {
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn(uri);
			expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
			expect(procedure.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)).andReturn(uri);
		}
	}

	@Test
	public void testNotifyProcedureStateChangedStartingCustomerActualCallOrder() {
		Procedure procedure = createStrictMock(Procedure.class);
		procedure.addStopListener(anyStopListener());
		prepareProcedure(procedure, "http://uri1.dynatrace.com");
		prepareProcedure(procedure, "http://uri1.dynatrace.com");
		prepareProcedure(procedure, "http://uri1.dynatrace.com");
		prepareProcedure(procedure, "http://uri1.dynatrace.com");

		replay(procedure);

		Procedure procedure2 = createStrictMock(Procedure.class);
		procedure2.addStopListener(anyStopListener());
		prepareProcedure(procedure2, "http://uri2.dynatrace.com");
		prepareProcedure(procedure2, "http://uri2.dynatrace.com");
		prepareProcedure(procedure2, "http://uri2.dynatrace.com");
		prepareProcedure(procedure2, "http://uri2.dynatrace.com");

		replay(procedure2);

		StatefulProcedure subject = new StatefulProcedure(procedure);
		StatefulProcedure subject2 = new StatefulProcedure(procedure2);
		// both STARTING
		expectHeaderCall(subject, false, "http://uri1.dynatrace.com", "http://uri1.dynatrace.com");
		expectHeaderCall(subject2, false, "http://uri2.dynatrace.com", "http://uri2");

		// then OPERATING
		expectHeaderCall(subject, true, "http://uri1.dynatrace.com", "http://uri1.dynatrace.com");
		expectHeaderCall(subject2, true, "http://uri2.dynatrace.com", "http://uri2");

		// once again OPERATING, with 2nd first
		expectHeaderCall(subject2, true, "http://uri2.dynatrace.com", "http://uri2");
		expectHeaderCall(subject, true, "http://uri1.dynatrace.com", "http://uri1.dynatrace.com");

		// then both STOPPING, 2nd first
		expectHeaderCall(subject2, false, "http://uri2.dynatrace.com", "http://uri2");
		expectHeaderCall(subject, false, "http://uri1.dynatrace.com", "http://uri1.dynatrace.com");

		replay(header);

		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.STARTING);
		listener.notifyProcedureStateChanged(subject2, State.UNKNOWN, State.STARTING);
		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.OPERATING);
		listener.notifyProcedureStateChanged(subject2, State.UNKNOWN, State.OPERATING);
		listener.notifyProcedureStateChanged(subject2, State.UNKNOWN, State.OPERATING);
		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.OPERATING);
		listener.notifyProcedureStateChanged(subject2, State.UNKNOWN, State.STOPPING);
		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.STOPPING);

		verify(procedure, header);
	}


	@Test
	public void testCustomerURIShortenBatchState() {
		Procedure procedure = createStrictMock(Procedure.class);
		procedure.addStopListener(anyStopListener());
		prepareProcedure(procedure, "http://uri1.dynatrace.com");
		prepareProcedure(procedure, "http://uri1.dynatrace.com");
		prepareProcedure(procedure, "http://uri1.dynatrace.com");

		replay(procedure);

		Procedure procedure2 = createStrictMock(Procedure.class);
		procedure2.addStopListener(anyStopListener());
		prepareProcedure(procedure2, "http://uri2.dynatrace.com");
		prepareProcedure(procedure2, "http://uri2.dynatrace.com");
		prepareProcedure(procedure2, "http://uri2.dynatrace.com");

		replay(procedure2);

		StatefulProcedure subject = new StatefulProcedure(procedure);
		StatefulProcedure subject2 = new StatefulProcedure(procedure2);

		// both OPERATING
		expectHeaderCall(subject, true, "http://uri1.dynatrace.com", "http://uri1.dynatrace.com");
		expectHeaderCall(subject2, true, "http://uri2.dynatrace.com", "http://uri2");

		// once again OPERATING, with 2nd first, after Batch state update, the other uri is shortened
		expectHeaderCall(subject2, true, "http://uri2.dynatrace.com", "http://uri2.dynatrace.com");
		expectHeaderCall(subject, true, "http://uri1.dynatrace.com", "http://uri1");

		// once again OPERATING, with 2nd first, after Batch state update did not report STARTING
		expectHeaderCall(subject2, true, "http://uri2.dynatrace.com", "http://uri2.dynatrace.com");
		expectHeaderCall(subject, true, "http://uri1.dynatrace.com", "http://uri1");

		replay(header);

		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.OPERATING);
		listener.notifyProcedureStateChanged(subject2, State.UNKNOWN, State.OPERATING);
		listener.notifyBatchStateChanged(null, null, State.STARTING);
		listener.notifyProcedureStateChanged(subject2, State.UNKNOWN, State.OPERATING);
		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.OPERATING);
		listener.notifyBatchStateChanged(null, null, State.OPERATING);
		listener.notifyProcedureStateChanged(subject2, State.UNKNOWN, State.OPERATING);
		listener.notifyProcedureStateChanged(subject, State.UNKNOWN, State.OPERATING);

		verify(procedure, header);
	}
}
