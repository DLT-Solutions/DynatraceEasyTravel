package com.dynatrace.easytravel.launcher.plugin;



import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;

/**
 * @author Rafal.Psciuk
 *
 */
public class LoadChangeTest {
	
	MyLoadChange plugin;
	
	@Before
	public void setup() {
		plugin = new MyLoadChange();
	}
	
	@Test
	public void testLoadChangePlugin() {
		EasyTravelConfig config = EasyTravelConfig.read();	
		
		assertFalse(plugin.isPluginEnabled());
		assertEquals(0, plugin.getLastSetLoadValue());
		
		//enable plugin
		plugin.setBaseLoadValue(20);
		enablePlugin();
		assertEquals(config.baseLoadIncreased, plugin.getLastSetLoadValue());
		
		int cnt = plugin.getSetLoadValueCnt();
		assertTrue(cnt != 0);
		enablePlugin();  //this should have no effect
		enablePlugin();
		enablePlugin();
		assertEquals(cnt, plugin.getSetLoadValueCnt());
		
		disablePlugin();
		assertEquals(20, plugin.getLastSetLoadValue());
		assertEquals(++cnt, plugin.getSetLoadValueCnt());
		
		disablePlugin();  //this should have no effect
		disablePlugin();
		disablePlugin();
		assertEquals(cnt, plugin.getSetLoadValueCnt());
		
		//enable plugin and change load value, disabling plugin should not restore previous load
		enablePlugin();
		assertEquals(config.baseLoadIncreased, plugin.getLastSetLoadValue());
		plugin.setBaseLoadValue(40);
		cnt = plugin.getSetLoadValueCnt();
		disablePlugin();
		assertEquals(cnt, plugin.getSetLoadValueCnt());		
	}	
		
	@Test
	public void testScenarioStop() {
		
	}
	
	private void enablePlugin(){
		plugin.setPluginEnabled(true);
		plugin.pluginsChanged();
	}
	
	private void disablePlugin(){
		plugin.setPluginEnabled(false);
		plugin.pluginsChanged();
	}
	
	/**
	 * @author Rafal.Psciuk
	 *   Extend LoadChange class to write some tests. This class uses own implementation of isPluginEnabled, getBaseLoadValue and setLoadValue 
	 * 	 This will allow to write a test without communication with external world.
	 */
	class MyLoadChange extends LoadChange {

		boolean enabled = false; 		 //this will mock the response from PluginChangeMonitor.isPluginEnabled method
		int lastSetLoadValue = 0; 		 //this will hold value passed to the Launcher.setLoadValue method
		int launcherBaseLoadValue = 0;   //this will hold base load value provided by Launcher
		int setLoadValueCnt = 0; 		//keeps number of calsl to setLoadValue method
				
		@Override
		boolean isPluginEnabled() {
			return enabled;
		}
		
		void setPluginEnabled(boolean b) {
			enabled = b;
		}

		@Override
		void setLoadValue(int value) {
			lastSetLoadValue = value;
			launcherBaseLoadValue = value;  
			setLoadValueCnt++;
		}
		
		int getLastSetLoadValue() {
			return lastSetLoadValue;
		}
		
		int getSetLoadValueCnt() {
			return setLoadValueCnt;
		}
		
		@Override
		int getBaseLoadValue() {
			return launcherBaseLoadValue;
		}
		
		void setBaseLoadValue(int value) {
			this.launcherBaseLoadValue = value;
		}
	}
}
