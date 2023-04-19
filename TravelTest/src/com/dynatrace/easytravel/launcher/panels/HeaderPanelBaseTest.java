package com.dynatrace.easytravel.launcher.panels;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.dynatrace.easytravel.config.CustomerTrafficScenarioEnum;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.procedures.utils.UserSelectionTechListener;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.util.TextUtils;
import com.dynatrace.easytravel.utils.TestHelpers;


public class HeaderPanelBaseTest {
	@Test
	public void test() {
		// can be null or not depending on the current config and installed agents...
		HeaderPanelBase.getAgentPath(EasyTravelConfig.read());

		EasyTravelConfig.read().backendAgent = "Some unexisting path...";
		HeaderPanelBase.getAgentPath(EasyTravelConfig.read());
		EasyTravelConfig.read().backendAgent = "auto";

		assertEquals("somestring", HeaderPanelBase.encode("somestring"));
		assertEquals("somestring%25123", HeaderPanelBase.encode("somestring%123"));

		// just cover some things here
		MyHeaderPanelBase base = new MyHeaderPanelBase(true);
		base.setDCRUMVisible(true);
		assertFalse(base.isInManualMode());
		assertTrue(base.isConfigControllingAllowed());
		base.configControllingAllowed = false;
		assertFalse(base.isConfigControllingAllowed());
		base.configControllingAllowed = true;
		HeaderPanelBase.manualVisitsCreation = true;
		assertFalse(base.isConfigControllingAllowed());
	}

	@Test
	public void testGetSystemProfile() {
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile("some", "4.0.0").getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile("some", "4.1.0").getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile("some", "5.0.0").getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertContains(HeaderPanelBase.getSystemProfile("some", "5.5.0").getName(), "5.5.0");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile("some", "5.6.0").getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile("some", "6.0.0").getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile("some", "6.1.0").getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile("some", "6.2.0").getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");

		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile(null, "4.0.0").getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile(null, "4.1.0").getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile(null, "5.0.0").getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertContains(HeaderPanelBase.getSystemProfile(null, "5.5.0").getName(), "5.5.0");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile(null, "5.6.0").getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile(null, "6.0.0").getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile(null, "6.1.0").getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile(null, "6.2.0").getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");

		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile("4.0.0", "some").getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile("4.1.0", "some").getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile("5.0.0", "some").getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertContains(HeaderPanelBase.getSystemProfile("5.5.0", "some").getName(), "5.5.0");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile("5.6.0", "some").getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile("6.0.0", "some").getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile("6.1.0", "some").getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile("6.2.0", "some").getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");

		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile("4.0.0", null).getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile("4.1.0", null).getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile("5.0.0", null).getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertContains(HeaderPanelBase.getSystemProfile("5.5.0", null).getName(), "5.5.0");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile("5.6.0", null).getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile("6.0.0", null).getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile("6.1.0", null).getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
		TestHelpers.assertNotContains(HeaderPanelBase.getSystemProfile("6.2.0", null).getName(), "4.0", "4.1", "5.0", "5.5", "5.6", "5.7", "6.0", "6.1", "6.2");
	}

	@Test
	public void updateDescription() {
		// no execution if disposed
		MyHeaderPanelBase base = new MyHeaderPanelBase(true);
		base.updateDescription();

		// now it's executed, but N/A as nothing is set
		base = new MyHeaderPanelBase(false);
		base.updateDescription();
		assertEquals("N/A", base.customer);
		assertEquals("N/A", base.b2b);
		assertEquals("N/A", base.mobile);

		// set disposed whenever we do not expect update to actually run...

		base.updateAndroidFrontendUri(TextUtils.merge("{0}apps/EasyTravelAndroid.apk", "http://localhost:8020"));
		base.disposed = true;
		base.updateAndroidFrontendUri(TextUtils.merge("{0}apps/EasyTravelAndroid.apk", "http://localhost:8020"));
		base.disposed = false;
		base.updateB2BFrontendUri(TextUtils.merge(BaseConstants.LINK_HREF, "http://localhost:8020", "name0"));
		base.disposed = true;
		base.updateB2BFrontendUri(TextUtils.merge(BaseConstants.LINK_HREF, "http://localhost:8020", "name0"));
		base.disposed = false;

		// the same two times works
		base.updateCustomerFrontendUri("id1", TextUtils.merge(BaseConstants.LINK_HREF, "http://localhost:8020", "name1"));
		base.disposed = true;
		base.updateCustomerFrontendUri("id1", TextUtils.merge(BaseConstants.LINK_HREF, "http://localhost:8020", "name1"));
		base.disposed = false;

		base.updateCustomerFrontendUri("id2", TextUtils.merge(BaseConstants.LINK_HREF, "http://localhost:8020", "name2"));

		assertEquals("<a href=\"http://localhost:8020\" target=\"_blank\">name1</a> <a href=\"http://localhost:8020\" target=\"_blank\">name2</a> ", base.customer);

		// removing the same two times works
		base.updateCustomerFrontendUri("id2", null);
		base.disposed = true;
		base.updateCustomerFrontendUri("id2", null);
		base.disposed = false;

		base.updateIOSFrontendUri("somestring");
		base.disposed = true;
		base.updateIOSFrontendUri("somestring");
		base.disposed = false;

		assertEquals("<a href=\"http://localhost:8020\" target=\"_blank\">name1</a> ", base.customer);
		assertEquals("<a href=\"http://localhost:8020\" target=\"_blank\">name0</a>", base.b2b);
		assertEquals("http://localhost:8020apps/EasyTravelAndroid.apk somestring", base.mobile);

		// now with webserver
		base.updateWebserverAndroidFrontendUri("234lk23");
		base.updateWebserverB2bFrontendUri("somestags");
		base.updateWebserverCustomerFrontendUri("somemore");
		base.updateWebserverIOSFrontendUri("asdlfj24");

		// again with the same value, no update should run...
		base.disposed = true;
		base.updateWebserverAndroidFrontendUri("234lk23");
		base.updateWebserverB2bFrontendUri("somestags");
		base.updateWebserverCustomerFrontendUri("somemore");
		base.updateWebserverIOSFrontendUri("asdlfj24");
		base.disposed = false;

		base.updateDescription();
		assertEquals("somemore", base.customer);
		assertEquals("somestags", base.b2b);
		assertEquals("234lk23 asdlfj24", base.mobile);

		// back to non-Webserver
		base.updateWebserverAndroidFrontendUri(null);
		base.updateWebserverB2bFrontendUri(null);
		base.updateWebserverCustomerFrontendUri(null);
		base.updateWebserverIOSFrontendUri(null);

		base.updateDescription();
		assertEquals("<a href=\"http://localhost:8020\" target=\"_blank\">name1</a> ", base.customer);
		assertEquals("<a href=\"http://localhost:8020\" target=\"_blank\">name0</a>", base.b2b);
		assertEquals("http://localhost:8020apps/EasyTravelAndroid.apk somestring", base.mobile);

		base.disposed = true;
		base.updateWebserverAndroidFrontendUri(null);
		base.updateWebserverB2bFrontendUri(null);
		base.updateWebserverCustomerFrontendUri(null);
		base.updateWebserverIOSFrontendUri(null);
		base.disposed = false;

		// some border-cases
		base.updateIOSFrontendUri("");
		base.updateAndroidFrontendUri("");
		base.updateIOSFrontendUri(null);
		base.updateWebserverIOSFrontendUri(null);
		base.updateAndroidFrontendUri(null);
		base.updateWebserverAndroidFrontendUri(null);

		base.updateIOSFrontendUri("some");
		base.updateWebserverIOSFrontendUri("someother");
	}

	@Test
	public void updateDescriptionRemoveDuplicates() {
		// no execution if disposed
		MyHeaderPanelBase base = new MyHeaderPanelBase(true);
		base.updateDescription();

		// now it's executed, but N/A as nothing is set
		base = new MyHeaderPanelBase(false);
		base.updateDescription();
		assertEquals("N/A", base.customer);

		// set disposed whenever we do not expect update to actually run...

		// the same two times works
		base.updateCustomerFrontendUri("id1", TextUtils.merge(BaseConstants.LINK_HREF, "http://localhost:8020", "name1"));
		base.updateCustomerFrontendUri("id2", TextUtils.merge(BaseConstants.LINK_HREF, "http://localhost:8020", "name1"));

		assertEquals("<a href=\"http://localhost:8020\" target=\"_blank\">name1</a> ", base.customer);
	}
	@Test
	public void testTechnologyListener() {
		MyHeaderPanelBase base = new MyHeaderPanelBase(false);

		// nothing happens without listeners
		base.notifyUserChangedState(Technology.ADK, true);

		final AtomicInteger called = new AtomicInteger();
		base.registerTechnologyListener(new UserSelectionTechListener() {

			@Override
			public void notifyUserChangedState(Technology technology, boolean enabled) {
				assertEquals(Technology.ADK, technology);
				called.incrementAndGet();
			}
		});

		assertEquals(0, called.get());
		base.notifyUserChangedState(Technology.ADK, true);
		assertEquals(1, called.get());
		base.notifyUserChangedState(Technology.ADK, false);
		assertEquals(2, called.get());
	}

	@Test
	public void testSystemProfileLinkVisible() throws Exception {
		DtVersionDetector.enforceInstallationType(InstallationType.APM);
		Assert.assertFalse(HeaderPanel.isSystemProfileLinkVisible());
		DtVersionDetector.enforceInstallationType(InstallationType.Classic);
		Assert.assertTrue(HeaderPanel.isSystemProfileLinkVisible());
		DtVersionDetector.enforceInstallationType(null);
	}

	private class MyHeaderPanelBase extends HeaderPanelBase {
		boolean disposed;

		protected String customer, b2b, mobile;

		public MyHeaderPanelBase(boolean disposed) {
			this.disposed = disposed;
		}

		@Override
		public void activateUEMLoadPanel() {
		}

		@Override
		public void deactivateUEMLoadPanel() {
		}

		@Override
		public void enableTaggedWebRequest() {
		}

		@Override
		public void disableTaggedWebRequest() {
		}

		@Override
		public void setLoad(int value) {
		}

		@Override
		public void setTrafficLabel(String label) {
		}

		@Override
		public void resetTrafficLabel() {
		}

		@Override
		public void setDCRUMVisible(boolean visible) {
		}

		@Override
		protected void updateLinks(String customer, String b2b, String mobile) {
			this.customer = customer;
			this.b2b = b2b;
			this.mobile = mobile;

			if(disposed) {
				throw new IllegalStateException("Should not have been called, but was with: " + customer + "/" + b2b + "/" + mobile);
			}
		}

		@Override
		protected boolean isDisposed() {
			return disposed;
		}

		@Override
		public void setCustomerTrafficScenario(
				CustomerTrafficScenarioEnum scenario) {
		}

		@Override
		public void setDebugInfo(String info) {
		}
	}
}
