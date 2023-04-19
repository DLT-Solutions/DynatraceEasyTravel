package com.dynatrace.diagnostics.uemload.openkit.action.definition;

import com.dynatrace.diagnostics.uemload.openkit.action.definition.sub.SubActionDefinition;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.sub.TestSubAction;
import com.dynatrace.diagnostics.uemload.openkit.event.EventCallback;
import com.dynatrace.diagnostics.uemload.openkit.time.TimeService;
import com.dynatrace.openkit.api.Action;
import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.InOrder;

import java.util.List;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.dynatrace.diagnostics.uemload.openkit.action.definition.BetweenMatcher.isBetween;
import static org.mockito.Mockito.*;

public class ActionDefinitionTest extends EventTestBase {
	private static final String ACTION_NAME = "action";

	private TestSubAction subAction;

	@Before
	public void prepareEnvironment() {
		prepareRootAction();
		when(rootAction.enterAction(anyString())).thenAnswer(invocation -> mock(Action.class));

		subAction = prepareEvent(new TestSubAction(rootActionDef, ACTION_NAME));
		subAction.beginAfterCallback(rootActionDef::started);
		rootActionDef.waitFor(subAction::ended);
		rootActionDef.assignSubTask(subAction::getTask);
	}

	@Test
	public void testActionFlow() {
		// Callbacks
		EventCallback rootStartCalled = subAction.registerBeginCallback();
		EventCallback subEndCalled = rootActionDef.registerWaitForCallback();
		EventCallback rootStart = mockCallback(rootStartCalled);
		EventCallback rootEnd = mockCallback(() -> {});
		EventCallback subStart = mockCallback(() -> {});
		EventCallback subEnd = mockCallback(subEndCalled);
		registerActionCallbacks(rootStart, rootEnd, subStart, subEnd);
		// Periods
		List<DurationBounds> subBounds = getDurationBounds(subAction);
		List<DurationBounds> rootBounds = getDurationBounds(rootActionDef);
		rootBounds.get(1).min = 0; // (minimum duration) root action is waiting for sub action to end (which is nondeterministic)

		rootActionDef.start(session);

		verifyMainFlow(rootStart, subStart, subEnd, subBounds);
		// Concurrently
		verifySubActionFlow(subEnd, subBounds);
		verifyRootActionFlow(rootEnd, subEnd, rootBounds);
	}

	private void verifyRootActionFlow(EventCallback rootEnd, EventCallback subEnd, List<DurationBounds> rootBounds) {
		InOrder rootOrder = inOrder(subEnd, rootActionDef.getTimeService(), rootActionDef.getAction(), rootEnd);
		verifyCallback(rootOrder, subEnd, 1);
		verifyTimeServiceCalls(rootActionDef.getTimeService(), rootOrder, rootBounds.subList(1, 2)); //0 index (start delay) ignored
		verifyCallback(rootOrder, rootEnd, 1);
		verifyTimeServiceCall(rootActionDef.getTimeService(), rootOrder, rootBounds.get(3));
		verify(rootActionDef.getAction()).leaveAction();
	}

	private void verifySubActionFlow(EventCallback subEnd, List<DurationBounds> subBounds) {
		InOrder subOrder = inOrder(subEnd, subAction.getAction(), subAction.getTimeService());
		verifyCallback(subOrder, subEnd, 1);
		verifyTimeServiceCall(subAction.getTimeService(), subOrder, subBounds.get(3));
		subOrder.verify(subAction.getAction()).leaveAction();
	}

	private void verifyMainFlow(EventCallback rootStart, EventCallback subStart, EventCallback subEnd, List<DurationBounds> subBounds) {
		InOrder mainFlowOrder = inOrder(session, rootActionDef.getAction(), subAction.getTimeService(), rootStart, subStart, subEnd);
		mainFlowOrder.verify(session).enterAction(ROOT_ACTION_NAME);
		verifyCallback(mainFlowOrder, rootStart, 1); // Sub action start
		verifyTimeServiceCall(subAction.getTimeService(), mainFlowOrder, subBounds.get(0));
		verify(rootActionDef.getAction()).enterAction(ACTION_NAME);
		verifyCallback(mainFlowOrder, subStart, 1);
		verifyTimeServiceCalls(subAction.getTimeService(), mainFlowOrder, subBounds.subList(1, 2));
		verifyCallback(mainFlowOrder, subEnd, 1);
	}

	private void registerActionCallbacks(EventCallback rootStart, EventCallback rootEnd, EventCallback subStart, EventCallback subEnd) {
		rootActionDef.started(rootStart);
		rootActionDef.ended(rootEnd);
		subAction.started(subStart);
		subAction.ended(subEnd);
	}

	private List<DurationBounds> getDurationBounds(ActionDefinition actionDefinition) {
		List<DurationBounds> durationBounds = IntStream.rangeClosed(1, 4).mapToObj(i -> new DurationBounds(i * 100, i * 100 + 100)).collect(Collectors.toList());
		if(actionDefinition instanceof SubActionDefinition)
			addDuration(((SubActionDefinition) actionDefinition)::withStartDelay, durationBounds.get(0));
		addDuration(actionDefinition::withMinimumDuration, durationBounds.get(1));
		addDuration(actionDefinition::withExtraDuration, durationBounds.get(2));
		addDuration(actionDefinition::withFinishDelay, durationBounds.get(3));
		return durationBounds;
	}

	private EventCallback mockCallback(EventCallback callback) {
		return mock(EventCallback.class, AdditionalAnswers.delegatesTo(callback));
	}

	private void verifyCallback(InOrder inOrder, EventCallback callback, int times) {
		inOrder.verify(callback, times(times)).call();
	}

	private void verifyTimeServiceCalls(TimeService timeService, InOrder inOrder, List<DurationBounds> boundsList) {
		boundsList.forEach(bounds -> verifyTimeServiceCall(timeService, inOrder, bounds));
	}

	private void verifyTimeServiceCall(TimeService timeService, InOrder inOrder, DurationBounds bounds) {
		inOrder.verify(timeService).waitForDuration(longThat(isBetween(bounds)));
	}

	private void addDuration(ObjIntConsumer<Integer> to, DurationBounds bounds) {
		to.accept(bounds.min, bounds.max);
	}
}
