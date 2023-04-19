package com.dynatrace.easytravel;

import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

public class TravellersAccount extends AbstractGenericPlugin  {

	// a slightly different calculation, it simulates a off-by-one error by missing the leading zero-element,
	// i.e. the index is 1-based, but we have a 0-based list here, leading to ArrayIndexOutOfBoundsException in
	// JourneyAccount.getTotalCosts()
	private static final float[] TRAVELLERS_COST_FACTOR = { 1.0f, 1.33f, 1.667f, 2.0f, 2.33f, 2.667f };

	@Override
	public Object doExecute(String location, Object... context) {
		return TRAVELLERS_COST_FACTOR;
	}
}
