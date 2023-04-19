package com.dynatrace.diagnostics.uemload.openkit.action.definition.root;

import java.util.concurrent.Future;
import java.util.function.Supplier;

public class TestRootAction extends RootActionDefinition {
	public TestRootAction(String name) {
		super(name);
	}

	public void assignSubTask(Supplier<Future> task) {
		subTasks.clear();
		subTasks.add(task);
	}
}
