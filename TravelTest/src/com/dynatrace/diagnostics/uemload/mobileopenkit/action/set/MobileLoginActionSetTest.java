package com.dynatrace.diagnostics.uemload.mobileopenkit.action.set;

import com.dynatrace.diagnostics.uemload.mobileopenkit.action.EventNameMapper;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.EventInstance.*;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileActionType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileEventType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileOS;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.dynatrace.easytravel.frontend.rest.Constants.RestCall.Path.LOGIN;
import static org.mockito.Mockito.when;

public class MobileLoginActionSetTest extends ActionSetTestBase {
	private static final String FB_URL = "https://www.facebook.com/dynatrace";
	private static final String USER_NAME = "user";

	@Before
	public void prepareEnvironment() throws IOException {
		prepareWebRequestEnvironment();

		when(device.getUser().getName()).thenReturn(USER_NAME);
	}

	@Override
	protected MobileActionType getActionType() {
		return MobileActionType.LOGIN;
	}
		
	@Ignore
	@Override
	@Test
	public void testAndroidActionSetExecution() {
		testOnPlatform(MobileOS.ANDROID);
	}

	@Ignore
	@Override
	@Test
	public void testIOSActionSetExecution() {
		testOnPlatform(MobileOS.IOS);
	}


	@Override
	protected List<EventRoot> buildActionTrees(MobileOS platform, EventNameMapper nameMapper) {
		ActionInstance rootAction = new ActionInstance(nameMapper.get(MobileActionType.LOGIN));
		ActionInstance loginSubAction = new ActionInstance(nameMapper.get(MobileActionType.LOGIN_SUB_ACTION));
		WebRequestInstance loginRequest = new WebRequestInstance(API_URL + LOGIN, true);
		WebRequestInstance fbRequest = new WebRequestInstance(FB_URL, true);
		rootAction.addAll(loginSubAction, loginRequest, fbRequest);
		if(platform == MobileOS.ANDROID)
			loginSubAction.add(new EventReportInstance(MobileEventType.LOGIN_SUCCESSFUL.value));
		else
			loginSubAction.add(new ValueReportInstance(MobileEventType.LOGIN_SUCCESSFUL.value, USER_NAME));

		return Collections.singletonList(rootAction);
	}
}
