package com.dynatrace.diagnostics.uemload.headless;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.HeadlessBySelectors;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.easytravel.misc.CommonUser;

import java.util.ArrayList;
import java.util.List;

public class HeadlessBlogVisit extends HeadlessVisit {
	public HeadlessBlogVisit(String host) {
		super(host);
	}

	@Override
	public Action[] getActions(CommonUser user, Location location) {
		List<Action> actions = new ArrayList<>();
		actions.add(new HeadlessGetAction(host));
		//TODO: assuming PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.PHP_ENABLEMENT_PLUGIN)) {
		actions.add(new HeadlessClickAction(HeadlessBySelectors.BlogLink.get(), true));
		actions.add(new HeadlessClickAction(HeadlessBySelectors.BlogLatrobePost.get(), true));
		actions.add(new HeadlessClickAction(HeadlessBySelectors.BlogArchive2013Link.get(), true));
		actions.add(new HeadlessClickAction(HeadlessBySelectors.BlogItalyPost.get(), true));
		actions.add(new HeadlessGetAction(host));
		return actions.toArray(new Action[actions.size()]);
	}

	@Override
	public String getVisitName() {
		return "HeadlessBlogVisit";
	}
}
