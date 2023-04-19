package com.dynatrace.diagnostics.uemload.mobileopenkit.action.set;

import com.dynatrace.diagnostics.uemload.mobileopenkit.action.EventNameMapper;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileActionType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileEventType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileOS;
import com.dynatrace.openkit.api.cloudevents.CloudEvent;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.EventInstance.*;
import org.junit.Before;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.dynatrace.easytravel.frontend.rest.Constants.RestCall.Path.JOURNEYS;
import static com.dynatrace.easytravel.frontend.rest.Constants.RestCall.Path.LOCATIONS;
import static com.dynatrace.easytravel.frontend.rest.Constants.RestCall.QueryParam.MATCH;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MobileSearchActionSetTest extends ActionSetTestBase {
	private static final String JOURNEY_DESTINATION = "Testopia";

	@Before
	public void prepareEnvironment() throws IOException {
		prepareWebRequestEnvironment();

		when(device.getUser().getMobileDevice().isTablet()).thenReturn(false);
		setRequestResponse("[{}]");
		when(device.getRandomJourneyDestination()).thenReturn(JOURNEY_DESTINATION);
	}

	@Override
	protected MobileActionType getActionType() {
		return MobileActionType.SEARCH;
	}

	@Override
	protected List<EventRoot> buildActionTrees(MobileOS platform, EventNameMapper nameMapper) {
		ActionInstance rootAction = new ActionInstance(nameMapper.get(MobileActionType.SEARCH));
		if(platform == MobileOS.ANDROID)
			buildAndroidActionTree(rootAction, nameMapper);
		else
			buildIOSActionTree(rootAction, nameMapper);
		return Collections.singletonList(rootAction);
	}

	private void buildAndroidActionTree(ActionInstance rootAction, EventNameMapper nameMapper) {
		ActionInstance searchAction = new ActionInstance(nameMapper.get(MobileActionType.SEARCH_FOR_DESTINATION));
		rootAction.add(searchAction);
		buildJourneysRequest(searchAction);
	}

	private void buildIOSActionTree(ActionInstance rootAction, EventNameMapper nameMapper) {
		rootAction.add(new ActionInstance(nameMapper.get(MobileActionType.SEARCH_DESTINATION_PARENT)));
		for (int i = 0; i < JOURNEY_DESTINATION.length() && i < 4; i++)
			buildTypingActionSet(i, rootAction, nameMapper);

		ActionInstance searchAction = new ActionInstance(nameMapper.get(MobileActionType.SEARCH_FOR_DESTINATION));
		rootAction.add(searchAction);
		buildJourneysRequest(searchAction);
	}

	private void buildTypingActionSet(int setIndex, ActionInstance parentAction, EventNameMapper nameMapper) {
		ActionInstance typingAction = new ActionInstance(nameMapper.get(MobileActionType.SEARCH_WHILE_TYPING));
		String destinationSubstring = JOURNEY_DESTINATION.substring(0, setIndex + 1);

		WebRequestInstance locationsRequest = new WebRequestInstance(String.format("%s%s?%s=%s", API_URL, LOCATIONS, MATCH, destinationSubstring), true);
		ValueReportInstance requestIdReport = new ValueReportInstance(MobileEventType.REQUEST_ID.value, setIndex + 1);
		ValueReportInstance operationKeyReport = new ValueReportInstance(MobileEventType.OPERATION_KEY.value, destinationSubstring);
		ValueReportInstance resultSizeReport = new ValueReportInstance(MobileEventType.RESULT_SIZE.value, 1);

		typingAction.addAll(locationsRequest, requestIdReport, operationKeyReport, resultSizeReport);
		parentAction.add(typingAction);
	}

	private void buildJourneysRequest(ActionInstance parentAction) {
		WebRequestInstance journeysRequest = new WebRequestInstance(API_URL + JOURNEYS, false);
		ValueReportInstance journeysFoundEvent = new ValueReportInstance(MobileEventType.JOURNEYS_FOUND.value, 1);
		parentAction.addAll(journeysRequest, journeysFoundEvent);
	}
}
