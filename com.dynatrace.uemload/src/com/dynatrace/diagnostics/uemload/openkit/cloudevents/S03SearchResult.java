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
public class S03SearchResult {
	public static final String TYPE = "com.easytravel.frontend.search-result";
	
	private String searchTerm;
	private String fromDate;
	private String toDate;
	private int numberOfTravelers;
	private int numberOfResults;
	
	public Map<String, JSONValue> getMap() {
		Map<String, JSONValue> map = new HashMap<String, JSONValue>();
		map.put(SEARCH_TERM, JSONStringValue.fromString(searchTerm));
		map.put(FROM_DATE, JSONStringValue.fromString(fromDate));
		map.put(TO_DATE, JSONStringValue.fromString(toDate));
		map.put(NUMBER_OF_TRAVELERS, JSONNumberValue.fromLong(numberOfTravelers));
		map.put(NUMBER_OF_RESULTS, JSONNumberValue.fromLong(numberOfResults));
		
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
	public int getNumberOfResults() {
		return numberOfResults;
	}
	public void setNumberOfResults(int numberOfResults) {
		this.numberOfResults = numberOfResults;
	}
	
	
}
