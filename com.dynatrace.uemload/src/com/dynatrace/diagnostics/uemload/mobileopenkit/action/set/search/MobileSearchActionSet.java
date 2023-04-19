package com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.search;

import com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.MobileActionSet;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileActionType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileEventType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileDevice;
import com.dynatrace.diagnostics.uemload.openkit.action.ActionDefinitionSet;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionParent;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.report.failure.ReportError;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.report.value.ReportValue;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.report.value.ValueReportConfig;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.request.WebRequest;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.root.RootAction;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.sub.SubAction;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.sub.SubActionDefinition;
import com.dynatrace.diagnostics.uemload.openkit.cloudevents.BizEventHelper;
import com.dynatrace.diagnostics.uemload.openkit.cloudevents.S02SearchTriggered;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.request.WebRequestDefinition;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.report.value.ValueReportDefinition;
import com.dynatrace.diagnostics.uemload.openkit.event.EventCallback;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.easytravel.frontend.rest.data.JourneyDTO;
import com.dynatrace.openkit.api.ErrorBuilder;
import com.dynatrace.openkit.api.ErrorReport;
import com.google.gson.JsonParser;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import static com.dynatrace.diagnostics.uemload.openkit.action.definition.report.value.ValueReportConfig.intValue;
import static com.dynatrace.diagnostics.uemload.openkit.action.definition.report.value.ValueReportConfig.textValue;
import static com.dynatrace.diagnostics.uemload.openkit.action.definition.request.WebRequestConfig.from;
import static com.dynatrace.diagnostics.uemload.openkit.event.lifetime.BeginCallbackSet.after;
import static com.dynatrace.diagnostics.uemload.openkit.event.lifetime.LiveCallbackSet.until;
import static com.dynatrace.diagnostics.uemload.utils.UemLoadHttpUtils.createPair;
import static com.dynatrace.easytravel.frontend.rest.Constants.RestCall.Path.JOURNEYS;
import static com.dynatrace.easytravel.frontend.rest.Constants.RestCall.Path.LOCATIONS;
import static com.dynatrace.easytravel.frontend.rest.Constants.RestCall.QueryParam.*;

public class MobileSearchActionSet extends MobileActionSet {
	private final String journeysUrl = device.getApiUrl() + JOURNEYS;
	private final String locationsUrl = device.getApiUrl() + LOCATIONS;
	private final String adServiceUrl = device.getApiUrl() + "AdService";

	private static final double AD_QUERY_FAIL_CHANCE = 0.4;

	private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

	private final String destination = device.getRandomJourneyDestination();
	
	private String dateFrom;
	private String dateTo;

	public MobileSearchActionSet(MobileDevice device) {
		super(device);
		eventNameMapper.register(MobileActionType.SEARCH, "searchJourney", "performSearch");
		eventNameMapper.register(MobileActionType.SEARCH_DESTINATION_PARENT, "", "performSearchForDestinationNamesParent");
		eventNameMapper.register(MobileActionType.SEARCH_WHILE_TYPING, "", "performSearchAsYouTypeForDestinationNames");
		eventNameMapper.register(MobileActionType.SEARCH_FOR_DESTINATION, "SoapCall_findJourneys", "performSearchForDestination");
	}

	@Override
	protected ActionDefinitionSet buildAndroid() {
		RootAction.RootActionBuilder rootAction = RootAction.named(eventNameMapper.get(MobileActionType.SEARCH));
		SubActionDefinition searchAction = SubAction.of(rootAction).named(eventNameMapper.get(MobileActionType.SEARCH_FOR_DESTINATION))
				.begin(after(rootAction::started)).withStartDelay(10, 40);

		addAdQuery(searchAction);
		getJourneysRequest(searchAction);
		return new ActionDefinitionSet(rootAction.live(until(searchAction::ended)).withFinishDelay(40, 60));
	}

	@Override
	protected ActionDefinitionSet buildIOS() {
		RootAction.RootActionBuilder rootAction = RootAction.named(eventNameMapper.get(MobileActionType.SEARCH));
		SubActionDefinition searchParentAction = SubAction.of(rootAction).named(eventNameMapper.get(MobileActionType.SEARCH_DESTINATION_PARENT))
				.begin(after(rootAction::started)).withStartDelay(5, 15).withMinimumDuration(30, 60);

		SubActionDefinition lastAction = searchParentAction;
		for (int i = 0; i < destination.length() && i < 4; i++)
			lastAction = addTypingActionSet(i, destination, rootAction, lastAction);

		SubActionDefinition searchAction = SubAction.of(rootAction).named(eventNameMapper.get(MobileActionType.SEARCH_FOR_DESTINATION)).begin(after(lastAction::ended));
		getJourneysRequest(searchAction);
		addAdQuery(searchAction);

		return new ActionDefinitionSet(rootAction.live(until(searchAction::ended)).withFinishDelay(20, 40));
	}

	private void addAdQuery(SubActionDefinition searchAction) {
		if (device.getUser().getMobileDevice().isTablet() || !PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.MOBILE_ERRORS))
			return;
		if (randomGenerator.nextDouble() < AD_QUERY_FAIL_CHANCE) {
			String requestUrl = WebRequestDefinition.getRequestUrl(adServiceUrl, createPair("ad", "true"));
			WebRequest.sent(from(searchAction).to(requestUrl).using(device.getHttpClient())).begin(after(searchAction::started)).withStartDelay(40, 60);
		}
		ErrorReport errorReport = new ErrorBuilder.ErrorCodeEvent(MobileEventType.AD_FAILED.value, device.isIOS() ? 256 : -3546).build();
		ReportError.from(searchAction).with(errorReport).begin(after(searchAction::started));
		if (device.isIOS()) {
			ErrorReport nsError = new ErrorBuilder.NsErrorEvent(MobileEventType.NS_ERROR.value, "256").withReason("failed to contact advertisements server")
					.withCallStackTrace(Arrays.toString(new String[]{"NSCocoaErrorDomain", "The operation couldn't be completed. (Cocoa error 256.)", "NSError reason", "(null)"})).build();
			ReportError.from(searchAction).with(nsError).begin(after(searchAction::started));
		}
	}

	private SubActionDefinition addTypingActionSet(int setIndex, String destination, ActionParent<com.dynatrace.openkit.api.RootAction> rootAction, SubActionDefinition lastAction) {
		SubActionDefinition typingAction = SubAction.of(rootAction).named(eventNameMapper.get(MobileActionType.SEARCH_WHILE_TYPING))
				.begin(after(lastAction::ended)).withMinimumDuration(120, 300);

		String destinationSubstring = destination.substring(0, setIndex + 1);
		WebRequestDefinition locationsRequest = WebRequest.sent(from(typingAction).to(getLocationsUrl(destinationSubstring)).using(device.getHttpClient()))
				.begin(after(typingAction::started)).withActionErrorReporting();
		typingAction.waitFor(locationsRequest::ended).withExtraDuration(30, 60);

		getValueReportEvent(intValue(setIndex + 1).named(MobileEventType.REQUEST_ID.value), typingAction);
		getValueReportEvent(textValue(destinationSubstring).named(MobileEventType.OPERATION_KEY.value), typingAction);
		locationsRequest.ended(() ->
			typingAction.getAction().reportValue(MobileEventType.RESULT_SIZE.value, locationsRequest.getResponse() != null ? getRequestResponseArraySize(locationsRequest) : 0)
		);
		return typingAction;
	}

	private int getRequestResponseArraySize(WebRequestDefinition request) {
		return new JsonParser().parse(request.getResponse()).getAsJsonArray().size();
	}

	private ValueReportDefinition getValueReportEvent(ValueReportConfig config, SubActionDefinition parent) {
		return ReportValue.from(parent).with(config).begin(after(parent::ended));
	}

	private void getJourneysRequest(SubActionDefinition searchAction) {
		WebRequestDefinition journeysRequest = WebRequest.sent(from(searchAction).to(getJourneysUrl()).using(device.getHttpClient()))
				.begin(after(searchAction::started)).withStartDelay(150, 200).withActionErrorReporting();
		
		BizEventHelper.reportS02Event(device, dateFrom, dateTo, 0, destination);

		EventCallback journeysRequestEndedCallback = searchAction.registerWaitForCallback();
		journeysRequest.ended(() -> {
			int journeysFound = 0;
			if (journeysRequest.getResponse() != null) {
				JourneyDTO[] journeys = WebRequestDefinition.mapResponse(journeysRequest.getResponse(), JourneyDTO[].class);
				device.setCachedJourneys(journeys);
				journeysFound = journeys.length;
			}
			searchAction.getAction().reportValue(MobileEventType.JOURNEYS_FOUND.value, journeysFound);
			BizEventHelper.reportS03Event(device, dateFrom, dateTo, 0, destination, journeysFound);
			journeysRequestEndedCallback.call();
		});
		searchAction.waitFor(journeysRequest::ended).withExtraDuration(40, 80);
	}

	private String getLocationsUrl(String destinationSubstring) {
		return WebRequestDefinition.getRequestUrl(locationsUrl, createPair(MATCH, destinationSubstring));
	}

	private String getJourneysUrl() {
		Calendar fromDate = Calendar.getInstance();
		Calendar toDate = (Calendar) fromDate.clone();
		toDate.add(Calendar.YEAR, 1);
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		dateFrom = format.format(fromDate.getTime());
		dateTo = format.format(toDate.getTime());

		return WebRequestDefinition.getRequestUrl(journeysUrl, createPair(MATCH, destination),
				createPair(FROM, format.format(fromDate.getTime())),
				createPair(TO, format.format(toDate.getTime())));
	}
}
