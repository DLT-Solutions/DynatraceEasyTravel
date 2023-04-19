package com.dynatrace.diagnostics.uemload.openkit.action;

import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionRoot;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.root.RootActionDefinition;
import com.dynatrace.openkit.api.Session;

import java.util.Arrays;
import java.util.List;

public class ActionDefinitionSet {
	private final ActionRoot[] actions;

	public ActionDefinitionSet(ActionRoot... actions) {
		this.actions = actions;
	}

	public ActionDefinitionSet(List<ActionRoot> actions) {
		this.actions = actions.toArray(new ActionRoot[0]);
	}

	public void run(Session session) {
		if (actions != null && actions.length > 0)
			Arrays.stream(actions).forEach(action -> action.start(session));
	}
}
