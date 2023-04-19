package com.dynatrace.diagnostics.uemload.openkit;

import com.dynatrace.diagnostics.uemload.openkit.action.EventType;

import java.util.HashMap;
import java.util.Map;

public abstract class CommandFactory {
	private final Map<EventType, Command> commands = new HashMap<>();

	public void addCommand(final EventType rca, final Command command) {
		commands.put(rca, command);
	}

	public void executeCommand(EventType rca) {
		if (commands.containsKey(rca)) {
			commands.get(rca).apply();
		}
	}

}
