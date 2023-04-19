package com.dynatrace.easytravel.launcher.remote;

import static org.junit.Assert.*;

import org.easymock.EasyMock;
import org.junit.Test;

import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.baseload.BaseLoadManager;

/**
 * @author Rafal.Psciuk
 *
 */
public class SyntheticRequestHandlerTest extends UiRestHandlerTestBase {

	@Test
	public void test() {
		SyntheticRequestHandler handler = new SyntheticRequestHandler();

		headerPanel.enableTaggedWebRequest();
		headerPanel.disableTaggedWebRequest();
		EasyMock.replay(headerPanel);

		String res = handler.setSyntheticRequests(true);
		assertEquals("synthetic requests set to true", res);
		assertTrue("synthetic web request should be enabled", BaseLoadManager.getInstance().getCustomerLoadController().isTaggedWebRequest());
		assertTrue("synthetic web request should be enabled", BaseLoadManager.getInstance().getB2bLoadController().isTaggedWebRequest());
		assertTrue("synthetic web request should be enabled", Launcher.isTaggedWebRequest());

		res = handler.setSyntheticRequests(false);
		assertEquals("synthetic requests set to false", res);
		assertFalse("synthetic web request should be disabled", BaseLoadManager.getInstance().getCustomerLoadController().isTaggedWebRequest());
		assertFalse("synthetic web request should be disabled", BaseLoadManager.getInstance().getB2bLoadController().isTaggedWebRequest());
		assertFalse("synthetic web request should be disabled", Launcher.isTaggedWebRequest());

		EasyMock.verify(headerPanel);
	}
}

