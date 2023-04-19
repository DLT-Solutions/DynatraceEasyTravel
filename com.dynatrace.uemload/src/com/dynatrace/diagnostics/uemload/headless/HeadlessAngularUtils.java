/**
 *
 */
package com.dynatrace.diagnostics.uemload.headless;

import com.dynatrace.diagnostics.uemload.IterableSet;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelHostManager;

/**
 * @author tomasz.wieremjewicz
 * @date 24 sty 2019
 *
 */
public class HeadlessAngularUtils {
	private HeadlessAngularUtils() {
	    throw new IllegalStateException("Utility class");
	  }

	public static IterableSet<Visit> createVisits(EasyTravelHostManager manager) {
		RandomSet<Visit> set = new RandomSet<>();
		for(String host : manager.getAngularFrontendHosts()) {
			set.add(new HeadlessAngularAlmostConvertedVisit(host), 10);
			set.add(new HeadlessAngularBounceVisit(host), 32);
			set.add(new HeadlessAngularConvertedVisit(host), 70);
			set.add(new HeadlessAngularSearchVisit(host), 31);
			set.add(new HeadlessAngularSelectMenuOptionsVisit(host), 10);
			set.add(new HeadlessAngularPageWandererVisit(host, true, false), 5);
			set.add(new HeadlessAngularPageWandererVisit(host, true, true), 1);
			set.add(new HeadlessAngularSpecialOffersConvertVisit(host), 5);
			set.add(new HeadlessAngularSignUpVisit(host), 5);
		}
		return set;
	}

	public static IterableSet<Visit> createRushHourVisits(EasyTravelHostManager manager) {
		RandomSet<Visit> set = new RandomSet<>();
		for(String host : manager.getAngularFrontendHosts()) {
			set.add(new HeadlessAngularAlmostConvertedVisit(host), 10);
			set.add(new HeadlessAngularBounceVisit(host), 32);
			set.add(new HeadlessAngularConvertedVisit(host), 70);
			set.add(new HeadlessAngularSearchVisit(host), 31);
			set.add(new HeadlessAngularSelectMenuOptionsVisit(host), 10);
			set.add(new HeadlessAngularPageWandererVisit(host, true, false), 3);
			set.add(new HeadlessAngularPageWandererVisit(host, true, true), 1);
			set.add(new HeadlessAngularPageWandererVisit(host, false, false), 3);
			set.add(new HeadlessAngularPageWandererVisit(host, false, true), 1);
			set.add(new HeadlessAngularSpecialOffersConvertVisit(host), 10);
			set.add(new HeadlessAngularSignUpVisit(host), 5);
		}
		return set;
	}

	public static IterableSet<Visit> createAnonymousVisits(EasyTravelHostManager manager) {
		RandomSet<Visit> set = new RandomSet<>();
		for(String host : manager.getAngularFrontendHosts()) {
			set.add(new HeadlessAngularBounceVisit(host), 32);
			set.add(new HeadlessAngularSearchVisit(host), 31);
			set.add(new HeadlessAngularSelectMenuOptionsVisit(host), 10);
			set.add(new HeadlessAngularPageWandererVisit(host, true, false), 3);
			set.add(new HeadlessAngularSignUpVisit(host), 5);
		}
		return set;
	}
}
