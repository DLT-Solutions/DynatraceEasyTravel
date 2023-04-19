package com.dynatrace.diagnostics.uemload.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;

import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.utils.AngularUTMParams.AngularUTMParamsBuilder;
import com.dynatrace.diagnostics.uemload.utils.UemLoadConstants.UTMParams;

public class AngularUTMParamsDistribution {
	private static final RandomSet<String> utmCampaigns = createUTMCampaigns();
	private static final RandomSet<String> utmMediums = createUTMMediums();
	private static final Map<String, RandomSet<String>> utmSources = createUTMSources();
	private static final RandomSet<String> utmTerms = createUTMTerms();
	private static final RandomSet<String> utmContents = createUTMContent();
	
	private static RandomSet<String> createUTMCampaigns() {
		RandomSet<String> utmCampaigns = new RandomSet<>();
		utmCampaigns.add(UTMParams.CAMPAIGN_SPRING_SALE, 1);
		utmCampaigns.add(UTMParams.CAMPAIGN_SPECIAL_OFFERS, 1);
		return utmCampaigns;
	}
	
	private static RandomSet<String> createUTMMediums() {
		RandomSet<String> utmMediums = new RandomSet<>();
		utmMediums.add(UTMParams.MEDIUM_CPC, 1);
		utmMediums.add(UTMParams.MEDIUM_SOCIAL, 1);
		return utmMediums;
	}
	
	private static Map<String, RandomSet<String>> createUTMSources() {
		Map<String, RandomSet<String>> sourcesMap = new HashMap<>();
		sourcesMap.put(UTMParams.MEDIUM_CPC, createCpcSources());
		sourcesMap.put(UTMParams.MEDIUM_SOCIAL, createSocialSources());
		sourcesMap.put(UTMParams.MEDIUM_EMAIL, createEmailSources());
		return sourcesMap;
	}
	
	private static RandomSet<String> createCpcSources() {
		RandomSet<String> cpc = new RandomSet<>();
		cpc.add(UTMParams.SOURCE_GOOGLE, 1);
		cpc.add(UTMParams.SOURCE_BING, 1);
		return cpc;
	}
	
	private static RandomSet<String> createSocialSources() {
		RandomSet<String> social = new RandomSet<>();
		social.add(UTMParams.SOURCE_INSTAGRAM, 1);
		social.add(UTMParams.SOURCE_FACEBOOK, 1);
		return social;
	}
	
	private static RandomSet<String> createEmailSources() {
		RandomSet<String> social = new RandomSet<>();
		social.add(UTMParams.SOURCE_NEWSLETTER, 1);
		return social;
	}
	
	private static RandomSet<String> createUTMTerms() {
		RandomSet<String> terms = new RandomSet<>();
		terms.add(UTMParams.TERM_EUROPE_FIRST_MINUTE, 1);
		terms.add(UTMParams.TERM_HOLIDAYS_LAST_MINUTE, 1);
		terms.add(UTMParams.TERM_ROMANTIC_HOLIDAYS, 1);
		terms.add("", 1);
		return terms;
	}
	
	private static RandomSet<String> createUTMContent() {
		RandomSet<String> content = new RandomSet<>();
		content.add(UTMParams.CONTENT_PARIS_CITY_OF_LOVE, 1);
		content.add(UTMParams.CONTENT_ROMAN_HOLIDAY, 1);
		content.add(UTMParams.CONTENT_LONDONS_CALLING, 1);
		content.add(UTMParams.CONTENT_ANCIENT_GREECE, 1);
		content.add("", 1);
		return content;
	}
	
	public static AngularUTMParams getRandomParams() {
		String campaign = utmCampaigns.getRandom();
		String medium = utmMediums.getRandom();
		String source = utmSources.get(medium).getRandom();
		String utmTerm = utmTerms.getRandom();
		String utmContent = utmContents.getRandom();
		String gclid = RandomStringUtils.random(AngularUTMParams.GCLID_LENGTH, true, true);
		
		return new AngularUTMParamsBuilder(campaign, medium, source)
				.utmTerm(utmTerm)
				.utmContent(utmContent)
				.gclid(gclid)
				.build();
	}
}
