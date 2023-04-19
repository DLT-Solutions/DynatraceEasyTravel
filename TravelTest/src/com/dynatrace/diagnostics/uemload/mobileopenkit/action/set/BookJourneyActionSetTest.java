package com.dynatrace.diagnostics.uemload.mobileopenkit.action.set;

import com.dynatrace.diagnostics.uemload.mobileopenkit.action.EventNameMapper;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.EventInstance.ActionInstance;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.EventInstance.ValueReportInstance;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.EventInstance.WebRequestInstance;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileActionType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileEventType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileOS;
import org.junit.Before;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.dynatrace.easytravel.frontend.rest.Constants.RestCall.Path.BOOKINGS_MOBILE;
import static org.mockito.Mockito.when;

public class BookJourneyActionSetTest extends ActionSetTestBase {
	private static final double JOURNEY_AMOUNT = 1;
	private static final String JOURNEY_DESTINATION = "nowhere";

	@Before
	public void prepareEnvironment() throws IOException {
		prepareWebRequestEnvironment();

		when(device.getRandomJourney().getAmount()).thenReturn(JOURNEY_AMOUNT);
		when(device.getSelectedJourney().getAmount()).thenReturn(JOURNEY_AMOUNT);
		when(device.getRandomJourney().getDestination()).thenReturn(JOURNEY_DESTINATION);
		when(device.getSelectedJourney().getDestination()).thenReturn(JOURNEY_DESTINATION);
		when(device.getUser().getMobileDevice().isTablet()).thenReturn(false);
	}

	@Override
	protected MobileActionType getActionType() {
		return MobileActionType.BOOK_JOURNEY;
	}

	@Override
	protected List<EventRoot> buildActionTrees(MobileOS platform, EventNameMapper nameMapper) {
		ActionInstance rootAction = new ActionInstance(BookJourneyActionSet.BOOK_ACTION_NAME);
		ActionInstance parent = rootAction;
		if(platform == MobileOS.ANDROID) {
			parent = new ActionInstance(BookJourneyActionSet.SOAP_ACTION_NAME);
			rootAction.add(parent);
		}
		WebRequestInstance bookingRequest = new WebRequestInstance(API_URL + BOOKINGS_MOBILE, true);
		ValueReportInstance bookingEvent = new ValueReportInstance(MobileEventType.BOOKING_JOURNEY_AMOUNT.value, JOURNEY_AMOUNT);
		ValueReportInstance bookingDestinationEvent = new ValueReportInstance(MobileEventType.BOOKING_JOURNEY_DESTINATION.value, JOURNEY_DESTINATION);
		parent.addAll(bookingRequest, bookingEvent, bookingDestinationEvent);
		return Collections.singletonList(rootAction);
	}
}
