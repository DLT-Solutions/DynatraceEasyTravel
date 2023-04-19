package com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.login;

import com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.MobileActionSet;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileActionType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileEventType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileDevice;
import com.dynatrace.diagnostics.uemload.openkit.action.ActionDefinitionSet;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.report.event.ReportEvent;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.report.value.ReportValue;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.request.WebRequest;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.request.WebRequestDefinition;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.root.RootAction;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.sub.SubAction;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.sub.SubActionDefinition;
import com.dynatrace.diagnostics.uemload.openkit.cloudevents.BizEventHelper;
import com.dynatrace.easytravel.frontend.rest.data.JourneyDTO;
import com.dynatrace.easytravel.frontend.rest.data.LoginUserDTO;

import static com.dynatrace.diagnostics.uemload.openkit.action.definition.report.value.ValueReportConfig.textValue;
import static com.dynatrace.diagnostics.uemload.openkit.action.definition.request.WebRequestConfig.from;
import static com.dynatrace.diagnostics.uemload.openkit.event.lifetime.BeginCallbackSet.after;
import static com.dynatrace.diagnostics.uemload.openkit.event.lifetime.LiveCallbackSet.until;
import static com.dynatrace.easytravel.frontend.rest.Constants.RestCall.Path.LOGIN;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MobileLoginActionSet extends MobileActionSet {
	private final String loginUrl = device.getApiUrl() + LOGIN;

	public MobileLoginActionSet(MobileDevice device) {
		super(device);
		eventNameMapper.register(MobileActionType.LOGIN, "DoLogin", "performLogin");
		eventNameMapper.register(MobileActionType.LOGIN_SUB_ACTION, "SoapCall_authenticate", "animateLogin");
	}

	protected String getUserPassword() {
		return device.getUser().getPassword();
	}

	@Override
	protected ActionDefinitionSet buildAndroid() {
		return buildCommon();
	}

	@Override
	protected ActionDefinitionSet buildIOS() {
		return buildCommon();
	}

	private ActionDefinitionSet buildCommon() {
		JourneyDTO dto = device.getSelectedJourney();
		if (dto != null) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			BizEventHelper.reportS06Event(device, device.getUser().getName(), dto, 1, 1);
		}
		
		String username = device.getUser().getName();
		LoginUserDTO body = new LoginUserDTO(username, getUserPassword());

		RootAction.RootActionBuilder rootAction = RootAction.named(eventNameMapper.get(MobileActionType.LOGIN));

		SubActionDefinition loginSubAction = SubAction.of(rootAction).named(eventNameMapper.get(MobileActionType.LOGIN_SUB_ACTION))
				.begin(after(rootAction::started)).withStartDelay(20, 50).withExtraDuration(100, 300);

		WebRequestDefinition loginRequest = WebRequest.sent(from(rootAction).to(loginUrl).using(device.getHttpClient())).begin(after(loginSubAction::started))
				.ofPostType(WebRequestDefinition.getRequestBody(body)).withStartDelay(80, 120).withActionErrorReporting();

		WebRequestDefinition fbRequest =  WebRequest.sent(from(rootAction).to("https://www.facebook.com/dynatrace")
				.using(device.getHttpClient())).begin(after(loginRequest::ended)).withStartDelay(200, 400);
		loginSubAction.waitFor(fbRequest::started);

		addReportEvent(loginRequest, loginSubAction, username);
		return new ActionDefinitionSet(rootAction.live(until(loginSubAction::ended)).withFinishDelay(50, 120));
	}

	private void addReportEvent(WebRequestDefinition loginRequest, SubActionDefinition action, String username) {
		loginRequest.ended(() -> {
			if (device.isIOS())
				ReportValue.from(action).with(textValue(username).named(getReportEventName(loginRequest.getResponseStatus()))).begin(after(action::ended));
			else
				ReportEvent.from(action).named(getReportEventName(loginRequest.getResponseStatus())).begin(after(action::ended));
		});
	}

	private String getReportEventName(int loginResponseStatus) {
		return (loginResponseStatus == 200 ? MobileEventType.LOGIN_SUCCESSFUL : MobileEventType.LOGIN_FAILED).value;
	}
}
