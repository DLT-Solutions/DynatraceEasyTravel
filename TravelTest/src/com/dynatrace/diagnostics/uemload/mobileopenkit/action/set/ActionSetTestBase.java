package com.dynatrace.diagnostics.uemload.mobileopenkit.action.set;

import com.dynatrace.diagnostics.uemload.NavigationTiming;
import com.dynatrace.diagnostics.uemload.http.base.HttpRequest;
import com.dynatrace.diagnostics.uemload.http.base.HttpResponse;
import com.dynatrace.diagnostics.uemload.http.base.UemLoadHttpClient;
import com.dynatrace.diagnostics.uemload.http.callback.HttpResponseCallback;
import com.dynatrace.diagnostics.uemload.mobileopenkit.MobileCommandFactory;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.EventNameMapper;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.EventInstance.*;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileActionType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileDevice;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileOS;
import com.dynatrace.diagnostics.uemload.openkit.event.Event;
import com.dynatrace.diagnostics.uemload.openkit.time.TimeService;
import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.api.WebRequestTracer;
import com.dynatrace.openkit.api.cloudevents.CloudEvent;
import com.dynatrace.openkit.api.mobile.DisplayAction;
import com.dynatrace.openkit.api.mobile.ViewLifeCycle;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public abstract class ActionSetTestBase {
	private static final int RESPONSE_CODE = 200;
	protected static final String API_URL = "/url";

	private String responseBody = "";
	private HttpResponse requestResponse;
	protected static MobileDevice device;
	protected Session session;

	@BeforeClass
	public static void prepareDevice() {
		device = mock(MobileDevice.class, RETURNS_DEEP_STUBS);
	}

	@Before
	public void prepareSession() {
		session = mock(Session.class);
		when(device.getActiveSession()).thenReturn(session);
	}

	@Test
	public void testAndroidActionSetExecution() {
		testOnPlatform(MobileOS.ANDROID);
	}

	@Test
	public void testIOSActionSetExecution() {
		testOnPlatform(MobileOS.IOS);
	}

	protected void testOnPlatform(MobileOS platform) {
		when(device.isIOS()).thenReturn(platform == MobileOS.IOS);
		MobileActionSet actionSet = MobileCommandFactory.getImplementation(getActionType(), device);
		if(actionSet == null) {
			fail("No action set was matched");
			return;
		}
		List<EventRoot> actionTrees = prepareActionSetExecutionTest(platform, actionSet.eventNameMapper);

		Event.timeServiceSupplier = () -> mock(TimeService.class);
		actionSet.run();
		
		verifyActionSetExecution(actionTrees);
		verifyNoMoreInteractions(session);
	}

	protected void verifyActionSetExecution(List<EventRoot> actionTrees) {
		actionTrees.forEach(eventInstance -> verifyMockTreeExecution((EventInstance) eventInstance));
		actionTrees.forEach(this::verifyNoMoreMockTreeInteractions);
	}

	private List<EventRoot> prepareActionSetExecutionTest(MobileOS platform, EventNameMapper nameMapper) {
		List<EventRoot> actionTrees = buildActionTrees(platform, nameMapper);

		actionTrees.forEach(this::prepareActionTree);
		actionTrees.stream().filter(eventRoot -> eventRoot instanceof ActionInstance).forEach(eventRoot -> mockSubActions((ActionInstance) eventRoot));
		return actionTrees;
	}

	protected abstract MobileActionType getActionType();
	protected abstract List<EventRoot> buildActionTrees(MobileOS platform, EventNameMapper nameMapper);

	public void prepareWebRequestEnvironment() throws IOException {
		UemLoadHttpClient httpClient = mock(UemLoadHttpClient.class);
		requestResponse = mock(HttpResponse.class);
		when(requestResponse.getStatusCode()).thenReturn(RESPONSE_CODE);
		when(requestResponse.getTextResponse()).thenReturn(responseBody);

		doAnswer(invocation -> {
			((HttpResponseCallback) invocation.getArguments()[2]).readDone(requestResponse);
			return null;
		}).when(httpClient).execute(any(HttpRequest.class), any(NavigationTiming.class), any(HttpResponseCallback.class), any(byte[].class), any(HttpResponseCallback.class));

		when(device.getApiUrl()).thenReturn(API_URL);
		when(device.getHttpClient()).thenReturn(httpClient);
	}

	protected void setRequestResponse(String response) throws IOException {
		responseBody = response;
		when(requestResponse.getTextResponse()).thenReturn(response);
	}

	private void verifyMockTreeExecution(EventInstance eventInstance) {
		System.out.println("verifyMockTreeExecution " + eventInstance.value + " " + eventInstance.getClass().getName());
		eventInstance.eventVerification.run();
		if(eventInstance instanceof ActionInstance) ((ActionInstance) eventInstance).subActions.forEach(this::verifyMockTreeExecution);
	}

	private void verifyNoMoreMockTreeInteractions(EventRoot eventRoot) {
		verifyNoMoreInteractions(eventRoot.getAction());
		if(eventRoot instanceof ActionInstance)
			((ActionInstance) eventRoot).subActions.stream().filter(subEvent -> subEvent instanceof ActionInstance)
					.forEach(subAction -> verifyNoMoreInteractions(((ActionInstance) subAction).action));
	}

	private void mockSubActions(ActionInstance rootActionInstance) {
		Map<String, List<ActionInstance>> eventMap = rootActionInstance.subActions.stream().filter(subAction -> subAction instanceof ActionInstance)
				.map(subAction -> (ActionInstance) subAction).collect(Collectors.groupingBy(ActionInstance::getName));
		for (Map.Entry<String, List<ActionInstance>> entry : eventMap.entrySet()) {
			Action[] subEvents = entry.getValue().stream().skip(1).map(instance -> instance.action).toArray(Action[]::new);

			RootAction rootAction = (RootAction) rootActionInstance.action;
			when((rootAction).enterAction(entry.getKey())).thenReturn(entry.getValue().get(0).action, subEvents);

			entry.getValue().forEach(subEvent -> subEvent.eventVerification = () -> {
				verify(rootAction, times(entry.getValue().size())).enterAction(entry.getKey());
				verify(subEvent.action).leaveAction();
			});
		}
	}

	private void prepareActionTree(EventRoot eventRoot) {
		if(eventRoot instanceof ActionInstance) {
			RootAction rootAction = mock(RootAction.class);
			ActionInstance actionRoot = (ActionInstance) eventRoot;
			actionRoot.action = rootAction;
			when(session.enterAction(eventRoot.getName())).thenReturn(rootAction);

			eventRoot.setVerification(() -> {
				InOrder inOrder = inOrder(rootAction, session);
				inOrder.verify(session).enterAction(eventRoot.getName());
				inOrder.verify(rootAction).leaveAction();
			});
			for (EventInstance event : actionRoot.subActions) {
				if(event instanceof ActionInstance) {
					prepareSubAction((ActionInstance) event);
				} else prepareActionChild(event, rootAction);
			}
		} else if(eventRoot instanceof LifeCycleInstance)
			prepareLifeCycleAction((LifeCycleInstance) eventRoot, session);
	}

	private void prepareSubAction(ActionInstance actionInstance) {
		Action action = mock(Action.class);
		actionInstance.action = action;
		actionInstance.subActions.forEach(eventInstance -> prepareActionChild(eventInstance, action));
	}

	private void prepareLifeCycleAction(LifeCycleInstance action, ViewLifeCycle parent) {
		DisplayAction displayAction = mock(DisplayAction.class);
		action.action = displayAction;
		when(parent.enterDisplayAction(action.getName())).thenReturn(displayAction);

		action.setVerification(() -> {
			InOrder inOrder = inOrder(displayAction, parent);
			inOrder.verify(parent).enterDisplayAction(action.getName());
			inOrder.verify(displayAction).reportCreateOrDidLoad();
			inOrder.verify(displayAction).reportStartOrWillAppear();
			inOrder.verify(displayAction).reportResumeOrDidAppear();
			inOrder.verify(displayAction).leaveAction();
		});
	}

	private void prepareActionChild(EventInstance event, Action parentAction) {
		if(event instanceof WebRequestInstance) {
			prepareWebRequest((WebRequestInstance) event, parentAction);
		} else if(event instanceof LifeCycleInstance) {
			prepareLifeCycleAction((LifeCycleInstance) event, parentAction);
		} else if(event instanceof EventReportInstance) {
			EventReportInstance eventReport = (EventReportInstance) event;
			if(event instanceof ValueReportInstance) {
				prepareValueReport((ValueReportInstance) event, parentAction);
			} else if(event instanceof ErrorReportInstance) {
				event.eventVerification = () -> verify(parentAction).reportError(eventReport.getName(), ((ErrorReportInstance) event).errorCode);
			} else {
				event.eventVerification = () -> {
					System.out.println("+++++++ verify parentAction" + parentAction.toString()); 
					verify(parentAction).reportEvent(eventReport.getName());};
			}
		}
	}

	private void prepareValueReport(ValueReportInstance requestInstance, Action parentAction) {
		requestInstance.eventVerification = () -> {
			Object value = requestInstance.getValue();
			if(value instanceof String)
				verify(parentAction).reportValue(requestInstance.getName(), (String) value);
			if(value instanceof Double)
				verify(parentAction).reportValue(requestInstance.getName(), (double) value);
			if(value instanceof Integer)
				verify(parentAction).reportValue(requestInstance.getName(), (int) value);
		};
	}

	private void prepareWebRequest(WebRequestInstance requestInstance, Action parentAction) {
		WebRequestTracer tracer = mock(WebRequestTracer.class);
		final String url = requestInstance.getUrl();
		if(requestInstance.matchUrlEqual)
			when(parentAction.traceWebRequest(url)).thenReturn(tracer);
		else
			when(parentAction.traceWebRequest(startsWith(url))).thenReturn(tracer);

		requestInstance.eventVerification = () -> {
			InOrder inOrder = inOrder(parentAction, tracer);

			if(requestInstance.matchUrlEqual)
				inOrder.verify(parentAction).traceWebRequest(url);
			else
				inOrder.verify(parentAction).traceWebRequest(startsWith(url));
			inOrder.verify(tracer).start();
			inOrder.verify(tracer).getTag();

			verify(tracer).setBytesReceived(responseBody.getBytes().length);

			inOrder.verify(tracer).stop(RESPONSE_CODE);
		};
	}

}
