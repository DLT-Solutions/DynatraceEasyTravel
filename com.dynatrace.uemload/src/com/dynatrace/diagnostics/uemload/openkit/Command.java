package com.dynatrace.diagnostics.uemload.openkit;

/**
 * @author Michal.Bakula
 */
@FunctionalInterface
public interface Command {
	void apply();
}
