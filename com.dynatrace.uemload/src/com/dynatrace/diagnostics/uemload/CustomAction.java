package com.dynatrace.diagnostics.uemload;

import java.io.IOException;
import java.util.Collections;

import com.dynatrace.diagnostics.uemload.thirdpartycontent.ResourceRequestSummary;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.base.MoreObjects;


class CustomAction extends AbstractUemAction {

	public CustomAction(JavaScriptAgent agent) {
		super(agent);
	}

	@Override
	protected void sendActionPreviewInternal() throws IOException {
		agent.stopCustomAction(false, Collections.<ResourceRequestSummary> emptyList(), NavigationTiming.NONE, null, true, 0);
	}

	@Override
	public ActionType getTye() {
		return ActionType.CUSTOM_ACTION;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("customActionActionId", agent.getCustomActionId())
				.add("customActionName", agent.getCustomActionName())
				.add("customActionType", agent.getCustomActionType())
				.add("customActionInfo", agent.getCustomActionInfo())
				.add("customActionStart",
						TextUtils.merge("{0} ({1} millis ago)", agent.getCustomActionStart(),
								System.currentTimeMillis() - agent.getCustomActionStart()))
				.toString();
	}


}
