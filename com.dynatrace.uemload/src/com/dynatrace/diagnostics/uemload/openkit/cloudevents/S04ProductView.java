package com.dynatrace.diagnostics.uemload.openkit.cloudevents;

import static com.dynatrace.diagnostics.uemload.openkit.cloudevents.BizEventConstants.*;

import java.util.HashMap;
import java.util.Map;

import com.dynatrace.openkit.util.json.objects.JSONNumberValue;
import com.dynatrace.openkit.util.json.objects.JSONStringValue;
import com.dynatrace.openkit.util.json.objects.JSONValue;

/**
 * 
 * @author tomasz.wieremjewicz
 *
 */
public class S04ProductView {
	public static final String TYPE = "com.easytravel.funnel.product-view";
	
	private String product;
	private int journeyDuration;
	private double startPrice;
	private String reviewScore;
	private int resultPosition;
	
	public Map<String, JSONValue> getMap() {
		Map<String, JSONValue> map = new HashMap<String, JSONValue>();
		map.put(PRODUCT, JSONStringValue.fromString(product));
		map.put(JOURNEY_DURATION, JSONNumberValue.fromLong(journeyDuration));
		map.put(START_PRICE, JSONNumberValue.fromDouble(startPrice));
		map.put(REVIEW_SCORE, JSONStringValue.fromString(reviewScore));
		map.put(RESULT_POSITION, JSONNumberValue.fromLong(resultPosition));
		
		return map;
	}
	
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public int getJourneyDuration() {
		return journeyDuration;
	}
	public void setJourneyDuration(int journeyDuration) {
		this.journeyDuration = journeyDuration;
	}
	public double getStartPrice() {
		return startPrice;
	}
	public void setStartPrice(double startPrice) {
		this.startPrice = startPrice;
	}
	public String getReviewScore() {
		return reviewScore;
	}
	public void setReviewScore(String reviewScore) {
		this.reviewScore = reviewScore;
	}
	public int getResultPosition() {
		return resultPosition;
	}
	public void setResultPosition(int resultPosition) {
		this.resultPosition = resultPosition;
	}
	

}