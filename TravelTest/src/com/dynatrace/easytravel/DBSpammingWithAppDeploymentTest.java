package com.dynatrace.easytravel;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.dynatrace.easytravel.spring.PluginConstants;


/**
 * @author cwpl-rpsciuk
 *
 */
public class DBSpammingWithAppDeploymentTest {
	
	@Test
	public void test() throws InterruptedException {
		WarDeploymentPlugin webAppDeploymentMock = mock(WarDeploymentPlugin.class);
		DBSpammingWithAppDeployment plugin = new DBSpammingWithAppDeployment();
		plugin.setWebAppDeploymentdPlugin(webAppDeploymentMock);
		plugin.setSpammingDelay(TimeUnit.SECONDS.toMillis(3));
		plugin.setMode("mode");

		AtomicBoolean isDBSpammingEnabled = new AtomicBoolean(false);
		Object[] context = new Object[] {isDBSpammingEnabled};
		
		plugin.doExecute(PluginConstants.LIFECYCLE_PLUGIN_ENABLE, context);
		verify(webAppDeploymentMock, times(1)).doExecute(PluginConstants.LIFECYCLE_PLUGIN_ENABLE, context);
		assertFalse("DBSpamming flag should not be set", isDBSpammingEnabled.get());
		
		
		plugin.doExecute("backend.authenticationservice.authenticate.getuser", context);
		plugin.doExecute("someloc", context);
		verify(webAppDeploymentMock, times(1)).doExecute(PluginConstants.LIFECYCLE_PLUGIN_ENABLE, context);
		assertFalse("DBSpamming flag should not be set", isDBSpammingEnabled.get());
		
		Thread.sleep(TimeUnit.SECONDS.toMillis(3));
		
		plugin.doExecute("backend.authenticationservice.authenticate.getuser", context);
		verify(webAppDeploymentMock, times(1)).doExecute(PluginConstants.LIFECYCLE_PLUGIN_ENABLE, context);
		assertTrue("DBSpamming flag should be set", isDBSpammingEnabled.get());
		
		isDBSpammingEnabled.set(false); //reset
		plugin.doExecute("someloc", context);
		assertFalse("DBSpamming flag should not be set", isDBSpammingEnabled.get());
		
		isDBSpammingEnabled.set(false); //reset
		plugin.doExecute(PluginConstants.LIFECYCLE_PLUGIN_DISABLE, context);
		assertFalse("DBSpamming flag should not be set", isDBSpammingEnabled.get());
	}
	
	
}
