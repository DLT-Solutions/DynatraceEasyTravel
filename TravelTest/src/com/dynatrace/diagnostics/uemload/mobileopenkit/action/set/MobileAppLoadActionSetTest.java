package com.dynatrace.diagnostics.uemload.mobileopenkit.action.set;

import com.dynatrace.diagnostics.uemload.mobileopenkit.action.EventNameMapper;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.type.MobileActionType;
import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileOS;
import com.dynatrace.diagnostics.uemload.mobileopenkit.action.set.EventInstance.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MobileAppLoadActionSetTest extends ActionSetTestBase {
	@Override
	protected MobileActionType getActionType() {
		return MobileActionType.LOAD_APP;
	}

	@Override
	protected List<EventRoot> buildActionTrees(MobileOS platform, EventNameMapper nameMapper) {
		return platform == MobileOS.ANDROID ? buildAndroidActionTree(nameMapper) : buildIOSActionTree(nameMapper);
	}

	private List<EventRoot> buildAndroidActionTree(EventNameMapper nameMapper) {
		ActionInstance appStart = new ActionInstance(nameMapper.get(MobileActionType.APP_START));
		LifeCycleInstance displayView = new LifeCycleInstance(nameMapper.get(MobileActionType.DISPLAY));
		return Arrays.asList(appStart, displayView);
	}

	private List<EventRoot> buildIOSActionTree(EventNameMapper nameMapper) {
		ActionInstance rootAction = new ActionInstance(MobileAppLoadActionSet.LOADING_ACTION_NAME);
		ActionInstance appStart = new ActionInstance(nameMapper.get(MobileActionType.APP_START));
		LifeCycleInstance displayView = new LifeCycleInstance(nameMapper.get(MobileActionType.DISPLAY));
		rootAction.addAll(appStart, displayView);
		return Collections.singletonList(rootAction);
	}
}
