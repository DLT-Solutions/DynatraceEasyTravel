package com.dynatrace.easytravel.components;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ComponentManagerTest {
	final String expectedTypeFrontend = "frontend";
	final String vagrantTechnologyString = "vagrant";
	final String expectedIP = "172.18.12.12";
	
	@Test
	public void testAddingComponent(){		
		ComponentManager cm = new ComponentManager();
		
		cm.setComponent(expectedIP, new String[]{vagrantTechnologyString,expectedTypeFrontend, "http://", ""});	
		
		String[] ipArray = cm.getComponentsIPList(expectedTypeFrontend);	
		
		assertTrue(ipArray[0].contains(expectedIP));
	}
	
	@Test
	public void testRemovingComponent(){		
		ComponentManager cm = new ComponentManager();
		
		cm.setComponent(expectedIP, new String[]{vagrantTechnologyString,expectedTypeFrontend, "http://", ""});	
		
		cm.removeComponent(expectedIP);
		
		String[] ipArray = cm.getComponentsIPList(expectedTypeFrontend);	
		
		assertTrue(ipArray.length == 0);
	}

}
