/**
 *
 */
package com.dynatrace.easytravel.rest.services;

import java.util.Arrays;

import com.dynatrace.easytravel.frontend.beans.InvalidTravellerCostItemException;

/**
 * @author tomasz.wieremjewicz
 * @date 4 kwi 2019
 *
 */
public class JourneyPriceHelper {
	private static final float[] TRAVELLERS_COST_FACTOR = { 0, 1.0f, 1.3f, 1.6f, 2.0f, 2.3f, 2.6f };
	private static final float[] TRAVELLERS_COST_FACTOR_PROMOTION = { 1.0f, 1.33f, 1.667f, 2.0f, 2.33f, 2.667f };

	public double getTotalCosts(double averagePrice, int travellers, String[] enabledPlugins) {
		try {
			return averagePrice * getTravellersCostFactor(enabledPlugins)[travellers];
		} catch (Exception e) {
			throw new InvalidTravellerCostItemException("Had error while accessing item " + travellers + " in list of cost-factors: " + Arrays.toString(getTravellersCostFactor(enabledPlugins)), e);
		}
	}

	private float[] getTravellersCostFactor(String[] plugins) {
		if (plugins != null && Arrays.stream(plugins).anyMatch(s -> s.toLowerCase().contains("angularbookingerror500"))) {
			return TRAVELLERS_COST_FACTOR_PROMOTION;
		}

		return TRAVELLERS_COST_FACTOR;
	}
}
