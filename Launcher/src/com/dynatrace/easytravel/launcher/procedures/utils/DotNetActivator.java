package com.dynatrace.easytravel.launcher.procedures.utils;

import java.util.HashSet;
import java.util.Set;

import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.config.ScenarioConfiguration;
import com.dynatrace.easytravel.launcher.engine.ProcedureFactory;
import com.dynatrace.easytravel.launcher.misc.Constants;


class DotNetActivator extends AbstractTechnologyActivator {

	private static Set<String> DOTNET_PLUGINS = new HashSet<String>();
	private static Set<String> DOTNET_PLUGIN_SUBSTITUTES = new HashSet<String>();

	static {
		DOTNET_PLUGINS.add(Constants.Plugin.DotNetPaymentService);
		DOTNET_PLUGIN_SUBSTITUTES.add(Constants.Plugin.DummyPaymentService);
	}
	
	public DotNetActivator() {
		super(Technology.DOTNET_20, DOTNET_PLUGINS, DOTNET_PLUGIN_SUBSTITUTES);
		boolean shouldBeEnabled = isInitiallyEnabled();
		setInitiallyEnabled(shouldBeEnabled);
		setEnabled(shouldBeEnabled);
	}	 
	
	private static boolean isInitiallyEnabled() {
		if(OperatingSystem.IS_WINDOWS) {
			return true;
		}
		
		if(ScenarioConfiguration.userScenarioFileExists()) {
			return true;
		}
		
		if(!ProcedureFactory.getAllRemoteHosts().isEmpty()) {
			return true;
		}
			
		return false;
	}

}
