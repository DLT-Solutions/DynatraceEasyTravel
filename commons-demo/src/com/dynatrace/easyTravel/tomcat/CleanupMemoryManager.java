package com.dynatrace.easytravel.tomcat;

import org.apache.catalina.session.StandardManager;

/**
 * Behaves like {@link StandardManager}, but performs a memory cleanup when ending Tomcat if necessary.
 *
 * @author stefan.moschinski
 */
public class CleanupMemoryManager extends AutomaticMemoryManager {

	public CleanupMemoryManager(String serverName) {
		super(serverName);
	}

	@Override
	protected void clearAttributes(double clearRate) {
	}

	@Override
	protected void clearSessions() {
	}
}
