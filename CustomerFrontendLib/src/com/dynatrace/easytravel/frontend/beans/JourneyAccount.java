package com.dynatrace.easytravel.frontend.beans;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.faces.model.SelectItem;

import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;

/**
 * Asset calculations for one journey in the view.
 * Note: you must not cache JourneyAsset objects, they are user-session specific.
 *
 * @author philipp.grasboeck
 */
public class JourneyAccount implements Serializable {

	private static final long serialVersionUID = 1160965285518269626L;

	private static final GenericPluginList plugins = new GenericPluginList(PluginConstants.FRONTEND_TRAVELLERS_ACCOUNT);

	static final float TAXES_AND_FEES = 29.9f;
    static final float FLIGHT_COST_FACTOR = 0.3f;

	private static final List<SelectItem> TRAVELLERS_SELECT_LIST = Arrays.asList(
			new SelectItem(1, "1 adult"),
			new SelectItem(2, "1 adult+1 kid"),
			new SelectItem(3, "1 adult+2 kids"),
			new SelectItem(4, "2 adults"),
			new SelectItem(5, "2 adults+1 kid"),
			new SelectItem(6, "2 adults+2 kids")
	);

	static final float[] TRAVELLERS_COST_FACTOR = { 0, 1.0f, 1.3f, 1.6f, 2.0f, 2.3f, 2.6f };

	private JourneyDO journey;  // the associated journey
	private int travellers = 4; // i.e. 2 adults

	public double getFlightCosts() {
		return (getTotalCosts() - getTaxesAndFees()) * FLIGHT_COST_FACTOR;
	}

	public double getHotelCosts() {
		return getTotalCosts() - getFlightCosts() - getTaxesAndFees();
	}

	public float getTaxesAndFees() {
		return TAXES_AND_FEES;
	}

	public double getTotalCosts() {
		try {
			return getAvgPerPerson() * getTravellersCostFactor()[travellers];
		} catch (Exception e) {
			// catch java.lang.... exceptions and throw a com.dynatrace.easytravel... exception instead because
			// dynaTrace does not capture exceptions in package java.lang... in the default system profile setup
			throw new InvalidTravellerCostItemException("Had error while accessing item " + travellers + " in list of cost-factors: " + Arrays.toString(getTravellersCostFactor()), e);
		}
	}

	/**
	 * Here we offer a plugin the oppurtunity to provide a different travller cost factor array.
	 * Index is the selected index (1-based) in the select component,
	 * value is the cost factor.
	 *
	 * @return
	 * @author philipp.grasboeck
	 */
	private static float[] getTravellersCostFactor() {
		for (Object value : plugins.execute(PluginConstants.FRONTEND_TRAVELLERS_ACCOUNT)) {
			if (value instanceof float[]) {
				return (float[]) value;
			}
		}
		return TRAVELLERS_COST_FACTOR;
	}

	public double getAvgPerPerson() {
		return journey.getAmount();
	}

	public int getTravellingNights() {
		return journey.getToDate().get(Calendar.DAY_OF_YEAR) - journey.getFromDate().get(Calendar.DAY_OF_YEAR);
	}

	public int getTravellers() {
		return travellers;
	}

	public void setTravellers(int travellers) {
		this.travellers = travellers;
	}

	public JourneyDO getJourney() {
		return journey;
	}

	public void setJourney(JourneyDO journey) {
		this.journey = journey;
	}

	public List<SelectItem> getTravellersSelectList() {
		return TRAVELLERS_SELECT_LIST;
	}
}
