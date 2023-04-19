package com.dynatrace.easytravel.pluginagentbb;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.spring.pluginagent.PluginAgent;

public class PluginAgentBB extends PluginAgent {
	
	public PluginAgentBB() {
		super("backend", EasyTravelConfig.read().pluginAgentURLBB);
	}
}
