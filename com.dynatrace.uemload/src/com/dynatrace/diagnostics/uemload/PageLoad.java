package com.dynatrace.diagnostics.uemload;

import java.io.IOException;
import java.util.Collections;

import com.dynatrace.diagnostics.uemload.thirdpartycontent.ResourceRequestSummary;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.base.MoreObjects;


class PageLoad extends AbstractUemAction {

	PageLoad(JavaScriptAgent agent) {
		super(agent);
	}

	@Override
	protected void sendActionPreviewInternal() throws IOException {
		agent.pageLoadFinished(Collections.<ResourceRequestSummary> emptyList(), NavigationTiming.NONE, null, /* preview */true,
				0);
	}

	@Override
	public ActionType getTye() {
		return ActionType.PAGE_LOAD;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("url", agent.getUrl())
				.add("pageId", agent.getPageId())
				.add("loadStart",
						TextUtils.merge("{0} ({1} millis ago)", agent.getLoadStart(),
								System.currentTimeMillis() - agent.getLoadStart()))
				.toString();
	}


}
