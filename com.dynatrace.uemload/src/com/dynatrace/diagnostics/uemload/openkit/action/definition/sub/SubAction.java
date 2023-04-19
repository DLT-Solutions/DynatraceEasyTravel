package com.dynatrace.diagnostics.uemload.openkit.action.definition.sub;

import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionParent;
import com.dynatrace.diagnostics.uemload.openkit.event.lifetime.BeginCallbackSet;
import com.dynatrace.openkit.api.RootAction;

public class SubAction {
	private SubAction() {}

	public static SubActionBuilder of(ActionParent<RootAction> parentAction) {
		return new SubActionBuilder(parentAction);
	}

	public static class SubActionBuilder {
		private final ActionParent<RootAction> parentAction;

		private SubActionBuilder(ActionParent<RootAction> parentAction) {
			this.parentAction = parentAction;
		}

		public NamedSubActionBuilder named(String name) {
			return new NamedSubActionBuilder(name);
		}

		public class NamedSubActionBuilder {
			public final String name;

			private NamedSubActionBuilder(String name) {
				this.name = name;
			}

			public SubActionDefinition begin(BeginCallbackSet beginCallbacks) {
				return new SubActionDefinition(parentAction, beginCallbacks, name);
			}
		}
	}
}
