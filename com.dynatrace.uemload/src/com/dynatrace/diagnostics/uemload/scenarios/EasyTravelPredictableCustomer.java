package com.dynatrace.diagnostics.uemload.scenarios;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.diagnostics.uemload.IterableSet;
import com.dynatrace.diagnostics.uemload.SimpleIterableSet;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.util.TextUtils;

/**
 * @author Rafal Psciuk
 *
 *         Predictable load scenario.
 *         In this scenario each type of Visit (bounce, search, almost convert and convert) is executed given number of times.
 *         Following configuration settings are used:
 *         predictableCustomerLoadBounce - defines ratio for bounce visits
 *         predictableCustomerLoadSearch - defines ratio for search visits
 *         predictableCustomerLoadAlmost - defines ratio for search visits
 *         predictableCustomerLoadConvert - defines ratio for search visits
 *
 *         Actual number of vistis is calculated in following way:
 *         requests/min * ratio
 *         where requests/min is an amount of customer frontend traffic
 */
public class EasyTravelPredictableCustomer extends EasyTravelFixedCustomer {

	private static final Logger logger = Logger.getLogger(EasyTravelPredictableCustomer.class.getName());

	private SimpleIterableSet<Visit> myVisits;
	private int baseLoad = 0;

	private enum VISIT_TYPE { BOUNCE, SEARCH, ALMOST_CONVERT, CONVERT };

	public EasyTravelPredictableCustomer(String customerFrontendHost, String b2bFrontendHost,
			boolean highLoadFromAsia, int baseLoad) {
		super(customerFrontendHost, b2bFrontendHost, highLoadFromAsia);
		this.baseLoad = baseLoad;
	}

	public EasyTravelPredictableCustomer(boolean highLoadFromAsia, int baseLoad) {
		this(null, null, highLoadFromAsia, baseLoad);

	}

	/*
	 * Create vistis. Use current baseLoad value to calculate number visitis of each type.
	 * (non-Javadoc)
	 * 
	 * @see com.dynatrace.diagnostics.uemload.scenarios.EasyTravelFixedCustomer#createVisits()
	 */
	@Override
	protected IterableSet<Visit> createVisits() {
		//calculate quantities
		EnumMap<VISIT_TYPE, Integer> quantities = getVistQuantities();

		SimpleIterableSet<Visit> res = new SimpleIterableSet<Visit>();
		for (String customerFrontendHost : getHostsManager().getCustomerFrontendHosts()) {

    		res.add(new Bounce(customerFrontendHost), quantities.get(VISIT_TYPE.BOUNCE));
    		res.add(new Search(customerFrontendHost), quantities.get(VISIT_TYPE.SEARCH));
    		res.add(new AlmostConvert(customerFrontendHost), quantities.get(VISIT_TYPE.ALMOST_CONVERT));
    		res.add(new Convert(customerFrontendHost, useRandomFirstPage()), quantities.get(VISIT_TYPE.CONVERT));
		}

		this.myVisits = res;
		return res;
	}
	
	@Override
	protected Map<String, IterableSet<Visit>> createVisitsByContinent() {
		return Collections.emptyMap();
	}
	
	@Override
	protected Map<String, IterableSet<Visit>> createVisitsByCountry() {
		return Collections.emptyMap();
	}
	
	@Override
	protected IterableSet<Visit> createRushHourVisits() {
		return createVisits();
	}

	/**
	 * Calculate visit quantities based on baseLoad value and ratios from easyTravelConfig.properties file
	 *
	 * @return
	 */
	private EnumMap<VISIT_TYPE, Integer> getVistQuantities(){
		EasyTravelConfig config = EasyTravelConfig.read();

		//read ratios from easyTravelConfig.properties
		double bounceRatio = config.predictableCustomerLoadBounce;
		double searchRatio = config.predictableCustomerLoadSearch;
		double almostRatio = config.predictableCustomerLoadAlmostConvert;
		double convertRatio = config.predictableCustomerLoadConvert;

		EnumMap<VISIT_TYPE, Integer> res = new EnumMap<VISIT_TYPE, Integer>(VISIT_TYPE.class);

		res.put(VISIT_TYPE.BOUNCE, (int)Math.round(bounceRatio * baseLoad));
		res.put(VISIT_TYPE.SEARCH, (int)Math.round(searchRatio * baseLoad));
		res.put(VISIT_TYPE.ALMOST_CONVERT, (int)Math.round(almostRatio * baseLoad));
		res.put(VISIT_TYPE.CONVERT, (int)Math.round(convertRatio * baseLoad));

		if(logger.getLevel() == Level.FINEST) {
			logger.finest(TextUtils.merge("getVistQuantities: baseLoad {0}, BOUNCE {1}, SEARCH {2}, ALMOST {3}, CONVERT {4}",
					baseLoad, res.get(VISIT_TYPE.BOUNCE), res.get(VISIT_TYPE.SEARCH), res.get(VISIT_TYPE.ALMOST_CONVERT), res.get(VISIT_TYPE.CONVERT)));
		}

		return res;
	}

	@Override
	protected String getName() {
		return "Customer Frontend (Java) - EasyTravelPredictableCustomer";
	}

	/* (non-Javadoc)
	 * @see com.dynatrace.diagnostics.uemload.scenarios.EasyTravelLauncherScenario#hasHosts()
	 */
	@Override
	public boolean hasHosts() {
		return getHostsManager().getCustomerFrontendHostCount() > 0;
	}

	/*
	 * When this method is called visit's quantities are recalculated
	 * (non-Javadoc)
	 * 
	 * @see com.dynatrace.diagnostics.uemload.scenarios.EasyTravel#setLoad(int)
	 */
	@Override
	public void setLoad(int load) {
		//check if load has been changed
		if(this.baseLoad == load){
			if(logger.getLevel() == Level.FINEST) {
				logger.finest(TextUtils.merge("setLoad: load level not changed {0}", load));
			}
			return;
		}

		this.baseLoad = load;

		if(myVisits != null) {
			//recalculate quantities for visits
			EnumMap<VISIT_TYPE, Integer> vistQuantities = getVistQuantities();
			int[] tab = { vistQuantities.get(VISIT_TYPE.BOUNCE),
					vistQuantities.get(VISIT_TYPE.SEARCH),
					vistQuantities.get(VISIT_TYPE.ALMOST_CONVERT),
					vistQuantities.get(VISIT_TYPE.CONVERT),
			};
			myVisits.updateQuantities(tab);
		}
	}
}
