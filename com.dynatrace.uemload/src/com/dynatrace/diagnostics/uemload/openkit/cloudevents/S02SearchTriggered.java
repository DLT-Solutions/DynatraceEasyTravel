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
public class S02SearchTriggered {
	public static final String TYPE = "com.easytravel.frontend.search-triggered";
	
	private String searchTerm;
	private String fromDate;
	private String toDate;
	private int numberOfTravelers;
	
	public Map<String, JSONValue> getMap() {
		Map<String, JSONValue> map = new HashMap<String, JSONValue>();
		map.put(SEARCH_TERM, JSONStringValue.fromString(searchTerm));
		map.put(FROM_DATE, JSONStringValue.fromString(fromDate));
		map.put(TO_DATE, JSONStringValue.fromString(toDate));
		map.put(NUMBER_OF_TRAVELERS, JSONNumberValue.fromLong(numberOfTravelers));
		
		return map;
	}
	
	public String getSearchTerm() {
		return searchTerm;
	}
	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}
	public String getFromDate() {
		return fromDate;
	}
	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}
	public String getToDate() {
		return toDate;
	}
	public void setToDate(String toDate) {
		this.toDate = toDate;
	}
	public int getNumberOfTravelers() {
		return numberOfTravelers;
	}
	public void setNumberOfTravelers(int numberOfTravelers) {
		this.numberOfTravelers = numberOfTravelers;
	}
	
	
}
