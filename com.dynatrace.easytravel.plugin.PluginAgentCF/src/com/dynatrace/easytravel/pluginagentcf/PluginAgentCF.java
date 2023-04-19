package com.dynatrace.easytravel.pluginagentcf;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.spring.pluginagent.PluginAgent;

public class PluginAgentCF extends PluginAgent {

	public PluginAgentCF() {
		super("frontend", EasyTravelConfig.read().pluginAgentURLCF);
	}
}