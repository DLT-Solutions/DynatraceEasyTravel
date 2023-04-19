package com.dynatrace.diagnostics.uemload.mobileopenkit.device;

import com.dynatrace.openkit.AgentTechnologyType;

public enum MobileOS {
	ANDROID("Android", AgentTechnologyType.ANDROID),
	IOS("iOS", AgentTechnologyType.IOS);

	public final String prefix;
	public final AgentTechnologyType agentType;

	MobileOS(String prefix, AgentTechnologyType agentType) {
		this.prefix = prefix;
		this.agentType = agentType;
	}
}
