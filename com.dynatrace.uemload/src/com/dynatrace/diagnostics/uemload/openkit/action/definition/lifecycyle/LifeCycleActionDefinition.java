package com.dynatrace.diagnostics.uemload.openkit.action.definition.lifecycyle;

import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionParent;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionRoot;
import com.dynatrace.diagnostics.uemload.openkit.event.ControlledEvent;
import com.dynatrace.diagnostics.uemload.openkit.event.lifetime.EventCallbackSet;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.api.mobile.DisplayAction;
import com.dynatrace.openkit.api.mobile.ViewLifeCycle;

import java.util.Collections;
import java.util.function.Supplier;

public class LifeCycleActionDefinition extends ControlledEvent<LifeCycleActionDefinition> implements ActionRoot {
	private Supplier<ViewLifeCycle> parentSupplier;

	private final String name;
	private DisplayAction displayAction;

	LifeCycleActionDefinition(ActionParent actionParent, EventCallbackSet startCallbacks, String name) {
		this(name);
		this.parentSupplier = actionParent::getAction;

		withParentAction(actionParent);
		addBeginCallbacks(startCallbacks);
	}

	LifeCycleActionDefinition(String name) {
		this.name = name;
	}

	@Override
	public LifeCycleActionDefinition withStartDelay(int minStartDelay, int maxStartDelay) {
		return super.withStartDelay(minStartDelay, maxStartDelay);
	}

	protected LifeCycleActionDefinition configureLifetime(LifeCycleConfig config) {
		return withMinimumDuration(config.minAddedDuration, config.maxAddedDuration)
				.withExtraDuration(config.minExtraDuration, config.maxExtraDuration)
				.withFinishDelay(config.minEndDelay, config.maxEndDelay);
	}

	@Override
	protected void start() {
		super.start();
		displayAction = parentSupplier.get().enterDisplayAction(name);
		ended(() -> displayAction.reportStartOrWillAppear());
	}

	@Override
	public void start(Session session) {
		parentSupplier = () -> session;
		startExecution();
		waitForTasksCompletion(Collections.singleton(() -> task));
	}

	@Override
	protected void midDuration() {
		displayAction.reportCreateOrDidLoad();
	}

	@Override
	protected void run() {
		super.run();
		displayAction.reportResumeOrDidAppear();
		displayAction.leaveAction();
	}

	@Override
	protected LifeCycleActionDefinition getThis() {
		return this;
	}
}
