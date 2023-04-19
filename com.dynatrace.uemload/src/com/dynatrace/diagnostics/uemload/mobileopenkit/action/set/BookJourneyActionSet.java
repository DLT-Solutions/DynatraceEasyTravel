package com.dynatrace.diagnostics.uemload.mobileopenkit.action.set;

import static com.dynatrace.diagnostics.uemload.openkit.action.definition.request.WebRequestConfig.from;
import static com.dynatrace.diagnostics.uemload.openkit.event.lifetime.BeginCallbackSet.after;
import static com.dynatrace.diagnostics.uemload.openkit.event.lifetime.LiveCallbackSet.until;
import static com.dynatrace.easytravel.frontend.rest.Constants.RestCall.Path.BOOKINGS_MOBILE;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileEventType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.crash.Crash;
import com.dynatrace.diagnostics.uemload.mobileopenkit.crash.CrashLoader;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileDevice;
import com.dynatrace.diagnostics.uemload.openkit.action.ActionDefinitionSet;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionParent;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.request.WebRequest;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.request.WebRequestDefinition;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.root.RootAction;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.root.RootAction.RootActionBuilder;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.sub.SubAction;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.sub.SubActionDefinition;
import com.dynatrace.diagnostics.uemload.openkit.cloudevents.BizEventHelper;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CreditCard;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.frontend.rest.data.JourneyDTO;
import com.dynatrace.easytravel.frontend.rest.data.StoreBookingDTO;
import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.CrashBuilder;
import com.dynatrace.openkit.api.CrashReport;

public class BookJourneyActionSet extends MobileActionSet {
	private final String bookingUrl = device.getApiUrl() + BOOKINGS_MOBILE;
	static final String BOOK_ACTION_NAME = "bookJourney";
	static final String SOAP_ACTION_NAME = "SoapCall_storeBooking";

	private CreditCard creditCard = new CreditCard(device.getUser().getName());
	private JourneyDTO journey;

	public BookJourneyActionSet(MobileDevice device) {
		super(device);
		eventNameMapper.register(MobileEventType.BOOKING_FAILED_ERROR, "BookingStatusError", "NSRangeException");
	}

	@Override
	protected ActionDefinitionSet buildAndroid() {
		reportEvent();
		if (journey == null)
			return new ActionDefinitionSet();
		RootActionBuilder rootAction = RootAction.named(BOOK_ACTION_NAME);
		SubActionDefinition soapBook = SubAction.of(rootAction).named(SOAP_ACTION_NAME).begin(after(rootAction::started)).withStartDelay(10, 40);
		WebRequestDefinition bookingRequest = getBookingRequest(soapBook);
		getBookingEvents(soapBook, bookingRequest);
		return new ActionDefinitionSet(rootAction.live(until(soapBook::ended)).withFinishDelay(100, 150));
	}

	@Override
	protected ActionDefinitionSet buildIOS() {
		reportEvent();
		if (journey == null)
			return new ActionDefinitionSet();
		RootActionBuilder rootAction = RootAction.named(BOOK_ACTION_NAME);
		WebRequestDefinition bookingRequest = getBookingRequest(rootAction);
		getBookingEvents(rootAction, bookingRequest);
		return new ActionDefinitionSet(rootAction.live(until(bookingRequest::ended)).withFinishDelay(50, 100));
	}

	private WebRequestDefinition getBookingRequest(ActionParent<? extends Action> actionParent) {
		StoreBookingDTO body = new StoreBookingDTO(journey.getId(), device.getUser().name,
				String.valueOf(creditCard.getNumber()), journey.getAmount(), String.valueOf(UemLoadUtils.randomInt(1, 30)));
		WebRequestDefinition bookingRequest = WebRequest.sent(from(actionParent).to(bookingUrl).using(device.getHttpClient())).begin(after(actionParent::started));
		bookingRequest.ended(() -> {
			if(bookingRequest.getResponseStatus() == 500) {
				Crash crash = CrashLoader.loadCrashReport(device);
				CrashReport crashReport = new CrashBuilder(crash.getName())
						.withReason(bookingRequest.getResponse())
						.withStackTrace(crash.getStackTrace())
						.withSignalNumber(crash.signalNumber)
						.build();
				device.setCrashReport(crashReport);
			}
		});
		return bookingRequest.ofPostType(WebRequestDefinition.getRequestBody(body)).withStartDelay(50, 150).withActionErrorReporting();
	}
	
	private void reportEvent() {
		journey = device.getSelectedJourney();
		if (journey != null) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			BizEventHelper.reportS11Event(device, device.getUser().getName(), journey, 1, 1, device.getUser().getLoyaltyStatus());
		}
	}

	private void getBookingEvents(ActionParent<? extends Action> actionParent, WebRequestDefinition bookingRequest) {
		bookingRequest.ended(() -> {
			Action action = actionParent.getAction();
			if(bookingRequest.getResponseStatus() == 200)
				action.reportValue(MobileEventType.BOOKING_JOURNEY_AMOUNT.value, journey.getAmount());
			else
				action.reportEvent(MobileEventType.BOOKING_FAILED.value);
			action.reportValue(MobileEventType.BOOKING_JOURNEY_DESTINATION.value, journey.getDestination());
		});
	}
}
