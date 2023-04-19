package com.dynatrace.diagnostics.uemload.openkit.cloudevents;

import java.util.HashMap;
import java.util.Map;

import com.dynatrace.openkit.util.json.objects.JSONNumberValue;
import com.dynatrace.openkit.util.json.objects.JSONStringValue;
import com.dynatrace.openkit.util.json.objects.JSONValue;

import static com.dynatrace.diagnostics.uemload.openkit.cloudevents.BizEventConstants.*;

/**
 * 
 * @author tomasz.wieremjewicz
 *
 */
public class S11BookingFinished {
	public static final String TYPE = "com.easytravel.funnel.booking-finished";
	
	private String page;
	private String ccExpirationDate;
	private String ccProvider;
	private String product;
	private String organizer;
	private double amount;
	private String currency;
	private String reviewScore;
	private String arrivalDate;
	private String departureDate;
	private long journeyDuration;
	private int adultTravelers;
	private int childTravelers;
	private String loyaltyStatus;
	
	public Map<String, JSONValue> getMap() {
		Map<String, JSONValue> map = new HashMap<String, JSONValue>();
		map.put(PAGE, JSONStringValue.fromString(page));
		map.put(CC_EXPIRATION_DATE, JSONStringValue.fromString(ccExpirationDate));
		map.put(CC_PROVIDER, JSONStringValue.fromString(ccProvider));
		map.put(PRODUCT, JSONStringValue.fromString(product));
		map.put(ORGANIZER, JSONStringValue.fromString(organizer));
		map.put(AMOUNT, JSONNumberValue.fromDouble(amount));
		map.put(CURRENCY, JSONStringValue.fromString(currency));
		map.put(REVIEW_SCORE, JSONStringValue.fromString(reviewScore));
		map.put(ARRIVAL_DATE, JSONStringValue.fromString(arrivalDate));
		map.put(DEPARTURE_DATE, JSONStringValue.fromString(departureDate));
		map.put(JOURNEY_DURATION, JSONNumberValue.fromLong(journeyDuration));
		map.put(ADULT_TRAVELERS, JSONNumberValue.fromLong(adultTravelers));
		map.put(CHILD_TRAVELERS, JSONNumberValue.fromLong(childTravelers));
		map.put(LOYALTY_STATUS, JSONStringValue.fromString(loyaltyStatus));
		return map;
	}
	
	public String getPage() {
		return page;
	}
	public void setPage(String page) {
		this.page = page;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public String getOrganizer() {
		return organizer;
	}
	public void setOrganizer(String organizer) {
		this.organizer = organizer;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getReviewScore() {
		return reviewScore;
	}
	public void setReviewScore(String reviewScore) {
		this.reviewScore = reviewScore;
	}
	public String getArrivalDate() {
		return arrivalDate;
	}
	public void setArrivalDate(String arrivalDate) {
		this.arrivalDate = arrivalDate;
	}
	public String getDepartureDate() {
		return departureDate;
	}
	public void setDepartureDate(String departureDate) {
		this.departureDate = departureDate;
	}
	public long getJourneyDuration() {
		return journeyDuration;
	}
	public void setJourneyDuration(long journeyDuration) {
		this.journeyDuration = journeyDuration;
	}
	public int getAdultTravelers() {
		return adultTravelers;
	}
	public void setAdultTravelers(int adultTravelers) {
		this.adultTravelers = adultTravelers;
	}
	public int getChildTravelers() {
		return childTravelers;
	}
	public void setChildTravelers(int childTravelers) {
		this.childTravelers = childTravelers;
	}
	public String getCcExpirationDate() {
		return ccExpirationDate;
	}
	public void setCcExpirationDate(String ccExpirationDate) {
		this.ccExpirationDate = ccExpirationDate;
	}
	public String getCcProvider() {
		return ccProvider;
	}
	public void setCcProvider(String ccProvider) {
		this.ccProvider = ccProvider;
	}
	public String getLoyaltyStatus() {
		return loyaltyStatus;
	}
	public void setLoyaltyStatus(String loyaltyStatus) {
		this.loyaltyStatus = loyaltyStatus;
	}
}
