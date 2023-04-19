package com.dynatrace.easytravel.launcher.fancy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.junit.Test;


public class VisibilityAwareTest {

	@Test
	public void testVisibilityAware() {
		MenuVisibilityCallback visibility = EasyMock.createMock(MenuVisibilityCallback.class);
		MenuVisibilityCallback visibility2 = EasyMock.createMock(MenuVisibilityCallback.class);
		VisibilityAware aware = new VisibilityAware(visibility);
		
		EasyMock.expect(visibility.isEnabled()).andReturn(false);
		EasyMock.expect(visibility.isVisible()).andReturn(true);
		
		EasyMock.expect(visibility2.isVisible()).andReturn(false);
		
		EasyMock.replay(visibility, visibility2);

		assertFalse(aware.checkEnabled());
		assertTrue(aware.checkVisible());
		
		aware.setVisibility(visibility2);
		assertFalse(aware.checkVisible());
		
		EasyMock.verify(visibility, visibility2);
	}
}
