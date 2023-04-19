package com.dynatrace.diagnostics.uemload.scenarios;

import java.util.HashMap;
import java.util.Map;

import com.dynatrace.diagnostics.uemload.IterableSet;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.SyntheticAndRobotRandomLocation;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.easytravel.util.DtVersionDetector;

public class EasyTravelCustomer extends EasyTravel {
	
	private IterableSet<Visit> anonymousVisits = null;

	public EasyTravelCustomer(boolean highLoadFromAsia) {
	    this(null, null, highLoadFromAsia);
	}

	public EasyTravelCustomer(String customerFrontendHost, String b2bFrontendHost, boolean highLoadFromAsia) {
    	super(customerFrontendHost, b2bFrontendHost, highLoadFromAsia);
    }

	@Override
	protected IterableSet<Visit> createVisits() {
		VisitsModel visits = null;
		if (DtVersionDetector.isAPM()) {
			visits = new VisitsModel.VisitsBuilder()
			.setBounce(32)
			.setSearch(31)
			.setAlmost(60)
			.setConvert(30)
			.setSeo(10)
			.setSpecialOffers(3)
			.setImageGallery(11)
			.setMagentoShort(10)
			.setWandererShort(8)
			.setWandererConvertShort(1)
			.build();
		} else {
			visits = new VisitsModel.VisitsBuilder()
			.setBounce(60)
			.setSearch(31)
			.setAlmost(28)
			.setConvert(3)
			.setSeo(11)
			.setSpecialOffers(3)
			.setImageGallery(11)
			.setMagentoShort(10)
			.setWandererShort(8)
			.setWandererConvertShort(1)
			.build();
		}
		return createVisits(visits);
	}

	@Override
	protected Map<String, IterableSet<Visit>> createVisitsByContinent() {
		Map<String, IterableSet<Visit>> res = new HashMap<String, IterableSet<Visit>>();
		VisitsModel visits = null;
		if (DtVersionDetector.isAPM()) {
			visits = new VisitsModel.VisitsBuilder()
			.setBounce(30)
			.setSearch(60)
			.setAlmost(9)
			.setConvert(10)
			.setSeo(10)
			.setSpecialOffers(3)
			.setImageGallery(10)
			.setMagentoShort(10)
			.setWandererShort(8)
			.setWandererConvertShort(1)
			.build();
		} else {
			visits = new VisitsModel.VisitsBuilder()
			.setBounce(60)
			.setSearch(60)
			.setAlmost(20)
			.setConvert(2)
			.setSeo(10)
			.setSpecialOffers(3)
			.setImageGallery(10)
			.setMagentoShort(10)
			.setWandererShort(8)
			.setWandererConvertShort(1)
			.build();
		}
		res.put(EUROPE, createVisits(visits));
		return res;
	}
	
	@Override
	protected IterableSet<Visit> createRushHourVisits() {
		VisitsModel visits = null;
		if (DtVersionDetector.isAPM()) {
			visits = new VisitsModel.VisitsBuilder()
			.setBounce(32)
			.setSearch(31)
			.setAlmost(60)
			.setConvert(30)
			.setSeo(10)
			.setSpecialOffers(3)
			.setImageGallery(11)
			.setMagentoShort(5)
			.setMagentoLong(5)
			.setWandererShort(3)
			.setWandererConvertShort(1)
			.setWandererLong(6)
			.setWandererConvertLong(1)
			.build();
		} else {
			visits = new VisitsModel.VisitsBuilder()
			.setBounce(60)
			.setSearch(31)
			.setAlmost(28)
			.setConvert(3)
			.setSeo(11)
			.setSpecialOffers(3)
			.setImageGallery(11)
			.setMagentoShort(5)
			.setMagentoLong(5)
			.setWandererShort(3)
			.setWandererConvertShort(1)
			.setWandererLong(6)
			.setWandererConvertLong(1)
			.build();
		}
		return createVisits(visits);
	}

	@Override
	protected IterableSet<Visit> createAnonymousVisits() {
		VisitsModel visits = null;
		if (DtVersionDetector.isAPM()) {
			visits = new VisitsModel.VisitsBuilder()
			.setBounce(32)
			.setSearch(31)
			.setSeo(10)
			.setImageGallery(11)
			.setMagentoShort(10)
			.build();
		} else {
			visits = new VisitsModel.VisitsBuilder()
			.setBounce(60)
			.setSearch(31)
			.setSeo(11)
			.setImageGallery(11)
			.setMagentoShort(10)
			.build();
		}
		return createVisits(visits);
	}

	@Override
	protected String getName() {
		return "Customer Frontend (Java) - EasyTravelCustomer";
	}

	/* (non-Javadoc)
	 * @see com.dynatrace.diagnostics.uemload.scenarios.EasyTravelLauncherScenario#hasHosts()
	 */
	@Override
	public boolean hasHosts() {
		return getHostsManager().getCustomerFrontendHostCount() > 0;
	}

	@Override
	public Location getRandomLocation() {
		if(SyntheticAndRobotRandomLocation.SINGLETON.isNextLocationRobotOrSynthetic()) {
			return SyntheticAndRobotRandomLocation.SINGLETON.get();
		}
		return super.getRandomLocation();
	}

	public Visit getRandomAnonymousVisit(){
		if(anonymousVisits == null){
			anonymousVisits = createAnonymousVisits();
		}
		return anonymousVisits.getNext();
	}
}
