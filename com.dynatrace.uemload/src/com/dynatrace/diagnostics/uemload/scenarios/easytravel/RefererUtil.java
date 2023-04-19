package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.UtmReferer.UtmMedium;

/**
 * @author Rafal.Psciuk
 *
 */
public class RefererUtil {

	//cpc sources
	public static final UtmSource GOOGLE = new UtmSource("google", "https://www.google.com");
	public static final UtmSource FACEBOOK = new UtmSource("facebook", "https://www.facebook.com");
	//email sources
	public static final UtmSource HOLIDAY_CHECK = new UtmSource("holiday_check", "http://holidaycheck.com");
	public static final UtmSource TRIP_ADVISOR = new UtmSource("trip_advisor", "http://tripadvisor.com");
	
	public RandomSet<Referer> getReferers() {
		
		UtmReferer cpc = new UtmReferer.Builder()
			.setMedium(UtmMedium.CPC)
			.setSource(getUtmCpcSources())
			.setCampaign(getUtmCampaigns())
			.setTerm(getUtmTerm())
			.setContent(getUtmContent())
			.build();
		
		UtmReferer email = new UtmReferer.Builder()
			.setMedium(UtmMedium.EMAIL)
			.setSource(getUtmEmailSources())
			.setCampaign(getUtmCampaigns())
			.setTerm(getUtmTerm())
			.setContent(getUtmContent())
			.build();

		//main referrer distribution
		//NOTE: update RefererUtilTest if you change values here
		RandomSet<Referer> res = new RandomSet<Referer>();
		res.add(SimpleReferer.EMPTY_REFERER, 50);
		res.add(new SimpleReferer(getSimpleReferUrls()), 35);
		res.add(cpc, 12);
		res.add(email, 3);
		return res;
 	}
	
	private RandomSet<String> getSimpleReferUrls() {
		RandomSet<String> referers = new RandomSet<String>();
		referers.add("http://ask.com", 10);
		referers.add("https://www.yandex.com", 10);
		referers.add("https://www.yahoo.com", 15);
		referers.add("http://www.bing.com", 20);
		referers.add("https://www.google.com", 45);
		return referers;
	}
	
	private RandomSet<UtmSource> getUtmCpcSources() {
		RandomSet<UtmSource> sources = new RandomSet<UtmSource>();
		sources.add(GOOGLE, 3);
		sources.add(FACEBOOK, 1);
		return sources;
	}
	
	private RandomSet<UtmSource> getUtmEmailSources() {
		RandomSet<UtmSource> sources = new RandomSet<UtmSource>();
		sources.add(HOLIDAY_CHECK, 3);
		sources.add(TRIP_ADVISOR, 1);
		return sources;
	}
	
	private RandomSet<String> getUtmCampaigns() {
		RandomSet<String> campaigns = new RandomSet<String>();
		campaigns.add("summer", 1);
		campaigns.add("hot-sale", 2);
		campaigns.add("winter", 1);
		campaigns.add("spring", 1);
		return campaigns;
	}
	
	private RandomSet<String> getUtmTerm() {
		RandomSet<String> terms = new RandomSet<String>();
		terms.add("", 5);
		terms.add("hotel_in_paris", 1);
		terms.add("travel_urope", 1);
		terms.add("flight_to_hawaii", 1);
		return terms;
	}
	
	private RandomSet<String> getUtmContent() {
		RandomSet<String> content = new RandomSet<String>();
		content.add("", 5);
		content.add("promotion_header1.png",1);
		content.add("hawaii3.png",1);
		content.add("paris.png",1);
		return content;
	}	
}
