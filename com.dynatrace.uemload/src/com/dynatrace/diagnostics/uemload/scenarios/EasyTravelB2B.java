package com.dynatrace.diagnostics.uemload.scenarios;

import java.util.HashMap;
import java.util.Map;

import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.IterableSet;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.VisitorInfo;

public class EasyTravelB2B extends EasyTravel {
	
	private RandomSet<ExtendedCommonUser> b2bUsers;

	public EasyTravelB2B(String b2bFrontendHost, boolean highLoadFromAsia) {
		super(null, b2bFrontendHost, highLoadFromAsia);
		b2bUsers = createB2BUsers();
	}

	public EasyTravelB2B(boolean highLoadFromAsia) {
		super(null, null, highLoadFromAsia);
		b2bUsers = createB2BUsers();
	}

	@Override
	protected IterableSet<Visit> createVisits() {
		VisitsModel visits = new VisitsModel.VisitsBuilder().setB2b(100).build();
		return createVisits(visits);
	}

	@Override
	protected Map<String, IterableSet<Visit>> createVisitsByContinent() {
		Map<String, IterableSet<Visit>> res = new HashMap<String, IterableSet<Visit>>();
		VisitsModel visits = new VisitsModel.VisitsBuilder().setB2b(100).build();
		res.put(EUROPE, createVisits(visits));
		return res;
	}
	
	@Override
	protected IterableSet<Visit> createRushHourVisits() {
		return createVisits();
	}

	@Override
	protected String getName() {
		return "B2B Frontend (.NET)";
	}

	/* (non-Javadoc)
	 * @see com.dynatrace.diagnostics.uemload.scenarios.EasyTravelLauncherScenario#hasHosts()
	 */
	@Override
	public boolean hasHosts() {
		return getHostsManager().getB2bFrontendHostCount()>0;
	}
	
	private RandomSet<ExtendedCommonUser> createB2BUsers() {
		RandomSet<ExtendedCommonUser> users = new RandomSet<>();
		users.add(new ExtendedCommonUser.ExtendedCommonUserBuilder("Speed Travel Agency", null, null, "sta", 1).setVisitorInfo(new VisitorInfo(true)).build(), 1);
		users.add(new ExtendedCommonUser.ExtendedCommonUserBuilder("Personal Travel Inc.", null, null, "pti", 1).setVisitorInfo(new VisitorInfo(true)).build(), 1);
		users.add(new ExtendedCommonUser.ExtendedCommonUserBuilder("Thomas Chef", null, null, "tch", 1).setVisitorInfo(new VisitorInfo(true)).build(), 1);
		users.add(new ExtendedCommonUser.ExtendedCommonUserBuilder("TravelNiche Ltd.", null, null, "tni", 1).setVisitorInfo(new VisitorInfo(true)).build(), 1);
		return users;
	}
	
	@Override
	public ExtendedCommonUser getRandomUser(String country){
		return b2bUsers.getNext();
	}
}
