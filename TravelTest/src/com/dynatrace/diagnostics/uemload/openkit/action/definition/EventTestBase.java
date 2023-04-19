package com.dynatrace.diagnostics.uemload.openkit.action.definition;

import com.dynatrace.diagnostics.uemload.openkit.action.definition.root.TestRootAction;
import com.dynatrace.diagnostics.uemload.openkit.time.TimeService;
import com.dynatrace.openkit.api.Session;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public abstract class EventTestBase {
	protected static final String ROOT_ACTION_NAME = "root";

	protected TestRootAction rootActionDef;
	protected com.dynatrace.openkit.api.RootAction rootAction;
	protected Session session;

	protected void prepareRootAction() {
		rootActionDef = prepareEvent(new TestRootAction(ROOT_ACTION_NAME));
		session = mock(Session.class, RETURNS_DEEP_STUBS);
		rootAction = mock(com.dynatrace.openkit.api.RootAction.class);
		when(session.enterAction(anyString())).thenReturn(rootAction);
	}

	protected <A extends ActionParent> A prepareEvent(A event) {
		A spiedEvent = spy(event);
		spiedEvent.setTimeService(mock(TimeService.class));
		return spiedEvent;
	}
}
