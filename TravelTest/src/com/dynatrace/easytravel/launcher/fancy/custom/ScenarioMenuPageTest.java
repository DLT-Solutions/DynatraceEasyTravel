package com.dynatrace.easytravel.launcher.fancy.custom;

import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.junit.Test;

import com.dynatrace.easytravel.launcher.fancy.MenuVisibilityCallback;


public class ScenarioMenuPageTest {

	@Test
	public void testScenarioMenuPageStringMenuActionCallbackMenuVisibilityCallback() {
		MenuVisibilityCallback visibility = EasyMock.createMock(MenuVisibilityCallback.class);
		EasyMock.expect(visibility.isVisible()).andReturn(true);
		EasyMock.replay(visibility);
		ScenarioMenuPage page = new ScenarioMenuPage("title", null, visibility);
		assertTrue(page.checkVisible());
		EasyMock.verify(visibility);
	}

	@Test
	public void testScenarioMenuPageStringMenuActionCallbackMenuVisibilityCallbackString() {
		MenuVisibilityCallback visibility = EasyMock.createMock(MenuVisibilityCallback.class);
		EasyMock.expect(visibility.isVisible()).andReturn(true);
		EasyMock.replay(visibility);
		ScenarioMenuPage page = new ScenarioMenuPage("title", null, visibility, "image");
		assertTrue(page.checkVisible());
		EasyMock.verify(visibility);
	}

}
