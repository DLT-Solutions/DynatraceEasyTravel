package com.dynatrace.diagnostics.uemload.scenarios;

import java.util.List;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.SyntheticEndVisitAction;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravel.VisitLength;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.EasyTravelFinishPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.EasyTravelPaymentPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.EasyTravelReviewPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.EasyTravelTripDetailsPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.EasytravelStartPage;
import com.dynatrace.diagnostics.uemload.utils.UemLoadCalendarUtils;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.constants.BaseConstants.VisitNames;
import com.dynatrace.easytravel.misc.CommonUser;
import com.dynatrace.easytravel.util.PHPEnablementCheck;
import com.google.common.collect.Lists;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class PageWanderer implements Visit {

	protected final String host;
	private final boolean useRandomPage;
	private final VisitLength length;

	public PageWanderer(String host, boolean useRandomPage, VisitLength length) {
		this.host = host;
		this.useRandomPage = useRandomPage;
		this.length = length;
	}

	@Override
	public Action[] getActions(CommonUser user, Location location) {
		
		CustomerSession session = EasyTravel.createCustomerSession(host, user, location);
		List<Action> actions = Lists.newArrayList();
		
		addSetupActions(session, actions);
		addMultipleSearch(session, actions);
		addEndOfSession(session, actions, location);
		
		return actions.toArray(new Action[actions.size()]);
	}
	
	protected void addMultipleSearch(CustomerSession session, List<Action> actions) {
		int noOfTries;
		if(VisitLength.SHORT.equals(length)) {
			noOfTries = UemLoadUtils.randomInt(2) + 1;
		} else {
			noOfTries = UemLoadUtils.randomInt(3) + 3;
		}
		for(int i=0;i<noOfTries;i++) {
			if(Math.random() > 0.3) {
				addSearchWithReview(session, actions);
			} else {
				addSearchWithReviewAndPaymentPage(session, actions);
			}
		}
	}
	
	protected void addSetupActions(CustomerSession session, List<Action> actions) {
		actions.add(useRandomPage ? EasyTravel.createRandomPage(session) : new EasytravelStartPage(session, EasytravelStartPage.State.INIT));
		if(EasyTravel.isPHPEnabled()){
			actions.add(EasyTravel.createBlogDetailsPage(session));
		}
		actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.INIT));
		actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.LOGIN));
	}
	
	private void addSearchWithReview(CustomerSession session, List<Action> actions) {
		addSearch(session, actions);
		actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.NEW_SEARCH));
		actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.CLEAR));
	}
	
	private void addSearchWithReviewAndPaymentPage(CustomerSession session, List<Action> actions) {
		addSearch(session, actions);
		actions.add(new EasyTravelPaymentPage(session));
		actions.add(new EasyTravelReviewPage(session, EasyTravelReviewPage.State.BACK));
		actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.NEW_SEARCH));
		actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.CLEAR));
	}
	
	protected void addSearchWithConversion(CustomerSession session, List<Action> actions) {
		addSearch(session, actions);
		actions.add(new EasyTravelPaymentPage(session));
		actions.add(new EasyTravelFinishPage(session));
	}
	
	private void addSearch(CustomerSession session, List<Action> actions) {
		UemLoadCalendarUtils calendar = new UemLoadCalendarUtils();
		if(!calendar.isTripThisYear()) {
			actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.CALENDAR_YEAR, calendar));
		}
		if(!calendar.isTripThisMonth()) {
			actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.CALENDAR_MONTH, calendar));
		}
		actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.CALENDAR_DAY, calendar));
		actions.add(new EasytravelStartPage(session, EasytravelStartPage.State.SEARCH));
		if (PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.PHP_ENABLEMENT_PLUGIN)){
			actions.add(new EasyTravelTripDetailsPage(session));
		}
		actions.add(new EasyTravelReviewPage(session));
	}
	
	protected void addEndOfSession(CustomerSession session, List<Action> actions, Location location) {
		actions.add(EasyTravel.createLogoutPage(session));
		if(location.isRuxitSynthetic()) {
			actions.add(new SyntheticEndVisitAction());
		}
	}

	@Override
	public String getVisitName() {
		return VisitNames.EASYTRAVEL_PAGE_WANDERER;
	}
}
