package com.dynatrace.diagnostics.uemload.openkit.action.definition.lifecycyle;

import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionParent;
import com.dynatrace.diagnostics.uemload.openkit.event.lifetime.BeginCallbackSet;

public class LifeCycleAction {
	private final LifeCycleActionDefinition actionDefinition;

	private LifeCycleAction(LifeCycleActionDefinition actionDefinition) {
		this.actionDefinition = actionDefinition;
	}

	public static LifeCycleActionBuilder of(ActionParent actionParent) {
		return new LifeCycleActionBuilder(actionParent);
	}

	public static LifeCycleAction named(String name) {
		return new LifeCycleAction(new LifeCycleActionDefinition(name));
	}

	public LifeCycleActionDefinition report(LifeCycleConfig config) {
		return actionDefinition.configureLifetime(config);
	}

	public static class LifeCycleActionBuilder {
		private final ActionParent parentAction;

		private LifeCycleActionBuilder(ActionParent parentAction) {
			this.parentAction = parentAction;
		}

		public NamedLifeCycleActionBuilder named(String name) {
			return new NamedLifeCycleActionBuilder(name);
		}

		public class NamedLifeCycleActionBuilder {
			public final String name;

			private NamedLifeCycleActionBuilder(String name) {
				this.name = name;
			}

			public LifeCycleAction begin(BeginCallbackSet beginCallbacks) {
				return new LifeCycleAction(new LifeCycleActionDefinition(parentAction, beginCallbacks, name));
			}
		}
	}
}
