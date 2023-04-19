package com.dynatrace.diagnostics.uemload.openkit.cloudevents;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.diagnostics.uemload.mobileopenkit.device.MobileDevice;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.frontend.rest.data.JourneyDTO;
import com.dynatrace.easytravel.util.LocalUriProvider;
import com.dynatrace.openkit.util.json.objects.JSONValue;

public class BizEventHelper {	
	private static EasyTravelConfig config = EasyTravelConfig.read();
	
	public static String createUriForJourney(JourneyDTO dto) {
		String host = LocalUriProvider.getLocalUri(config.angularFrontendPortRangeStart, config.angularFrontendContextRoot);
		return host + "easytravel/journeys/" + dto.getId() + "/book";
	}
	
	public static void reportS02Event(MobileDevice device, String fromDate, String toDate, int numberOfTravelers, String searchTerm) {
		S02SearchTriggered event = new S02SearchTriggered();
		
		if (!StringUtils.isEmpty(fromDate)) {
			event.setFromDate(fromDate);
		}
		
		if (!StringUtils.isEmpty(toDate)) {
			event.setToDate(toDate);
		}
		
		event.setNumberOfTravelers(numberOfTravelers);
		
		if (!StringUtils.isEmpty(searchTerm)) {
			event.setSearchTerm(searchTerm);
		}
		
		reportBizEvent(device, S02SearchTriggered.TYPE, event.getMap());
	}
	
	public static void reportS03Event(MobileDevice device, String fromDate, String toDate, int numberOfTravelers, String searchTerm, int numberOfResults) {
		S03SearchResult event = new S03SearchResult();
		
		if (!StringUtils.isEmpty(fromDate)) {
			event.setFromDate(fromDate);
		}
		
		if (!StringUtils.isEmpty(toDate)) {
			event.setToDate(toDate);
		}
		
		event.setNumberOfTravelers(numberOfTravelers);
		
		if (!StringUtils.isEmpty(searchTerm)) {
			event.setSearchTerm(searchTerm);
		}
		
		event.setNumberOfResults(numberOfResults);
		
		reportBizEvent(device, S03SearchResult.TYPE, event.getMap());
	}
	
	public static void reportS04Event(MobileDevice device, String product, int journeyDuration, double startPrice, String reviewScore, int resultPosition) {
		S04ProductView event = new S04ProductView();
		
		event.setJourneyDuration(journeyDuration);
		event.setProduct(product);
		event.setResultPosition(resultPosition);
		event.setReviewScore(reviewScore);
		event.setStartPrice(startPrice);
		
		reportBizEvent(device, S04ProductView.TYPE, event.getMap());
	}
	
	public static void reportS06Event(MobileDevice device, String userID, JourneyDTO dto, int adultTravelers, int childTravelers) {
		S06Login event = new S06Login();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		
		event.setUserID(userID);
		event.setPage(createUriForJourney(dto));
		event.setProduct(dto.getName());
		event.setOrganizer(dto.getTenant());
		event.setAmount(dto.getAmount());
		event.setCurrency("USD");
		event.setReviewScore(dto.getAverageTotal());
		event.setArrivalDate(format.format(dto.getFromDate().getTime()));
		event.setDepartureDate(format.format(dto.getToDate().getTime()));
		event.setJourneyDuration(dto.getToDate() != null && dto.getFromDate() != null ? (dto.getToDate().getTimeInMillis() - dto.getFromDate().getTimeInMillis()) / (24*60*60*1000) : 0);
		event.setAdultTravelers(adultTravelers);
		event.setChildTravelers(childTravelers);
		
		reportBizEvent(device, S06Login.TYPE, event.getMap());
	}
	
	public static void reportS11Event(MobileDevice device, String userID, JourneyDTO dto, 
			int adultTravelers, int childTravelers, String loyaltyStatus) {
		S11BookingFinished event = new S11BookingFinished();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		
		event.setPage(createUriForJourney(dto));
		event.setCcExpirationDate(getCcExpirationDate(format));
		event.setCcProvider(getCcProvider());
		event.setProduct(dto.getName());
		event.setOrganizer(dto.getTenant());
		event.setAmount(dto.getAmount());
		event.setCurrency("USD");
		event.setReviewScore(dto.getAverageTotal());
		event.setArrivalDate(format.format(dto.getFromDate().getTime()));
		event.setDepartureDate(format.format(dto.getToDate().getTime()));
		event.setJourneyDuration(dto.getToDate() != null && dto.getFromDate() != null ? (dto.getToDate().getTimeInMillis() - dto.getFromDate().getTimeInMillis()) / (24*60*60*1000) : 0);
		event.setAdultTravelers(adultTravelers);
		event.setChildTravelers(childTravelers);
		event.setLoyaltyStatus(loyaltyStatus);
		
		reportBizEvent(device, S11BookingFinished.TYPE, event.getMap());
	}
	
	private static String getCcExpirationDate(SimpleDateFormat format) {
		Calendar ccExpiration = Calendar.getInstance();
		ccExpiration.add(Calendar.DATE, UemLoadUtils.randomInt(800, 1200));
		return format.format(ccExpiration.getTime());
	}
	
	private static String getCcProvider() {
		int random = UemLoadUtils.randomInt(1000);
		
		if (random < 400) {
			return "Visa";
		}
		else if (random < 800) {
			return "Mastercard";
		}
		else {
			return "American Express";
		}
	}
	
	private static void reportBizEvent(MobileDevice device, String type, Map<String, JSONValue> attributes) {
		if(PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.ANGULAR_BIZ_EVENTS_PLUGIN)) {
			device.getActiveSession().sendBizEvent(type, attributes);
		}
	}
}
