package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import java.util.Map;
import java.util.Set;

import org.junit.Test;
import ch.qos.logback.classic.Logger;

import static org.junit.Assert.*;

import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


/**
 * @author Rafal.Psciuk
 *
 */
public class RefererUtilTest {
	private static final int TEST_COUNT = 1000000;
	private static final Logger log = LoggerFactory.make();
	
	@Test
	public void test() {
		RandomSet<Referer> referers = new RefererUtil().getReferers();
		
		int emptyRefrerCnt = 0;
		int simpleRefrerCnt = 0;
		int utmCpcRefrerCnt = 0;
		int utmEmailRefrerCnt = 0;
		int utmReferersWithoutContent = 0;
		int utmReferersWithoutTerm = 0;
		int utmReferersWithoutContentAndTerm = 0;
		Set<String> campaign = Sets.newHashSet();
		Set<String> source = Sets.newHashSet();
		Set<String> content = Sets.newHashSet();
		Set<String> terms = Sets.newHashSet();		
		
		for(int i=0; i<TEST_COUNT; i++) {
			String referer = referers.getRandom().getReferer();

			Map<String, String> params = getUtmParams(referer);
			if(Strings.isNullOrEmpty(referer)) {
				emptyRefrerCnt ++;
			} else if(params.isEmpty()) {			
				simpleRefrerCnt ++;
			} else {
				String medium = params.get("utm_medium");
				if( "cpc".equals(medium)) {
					utmCpcRefrerCnt ++;
				} else if ("email".equals(medium)) {
					utmEmailRefrerCnt ++;
				}
				
				campaign.add(params.get("utm_campaign"));
				source.add(params.get("utm_source"));
				
				String contentS = params.get("utm_content");
				String term = params.get("utm_term");
				if(contentS == null && term == null) {
					utmReferersWithoutContentAndTerm++;
				} else if (contentS == null) {
					terms.add(term);
					utmReferersWithoutContent ++;
				} else if (term == null) {
					content.add(contentS);
					utmReferersWithoutTerm ++;
				}
				content.add(contentS);
			}										
		}
		
		log.info(TextUtils.merge("emtpy: {0} simple: {1} cpc: {2} email: {3} ", emptyRefrerCnt, simpleRefrerCnt, utmCpcRefrerCnt, utmEmailRefrerCnt) );
		log.info(campaign.toString());
		log.info(source.toString());
		log.info(content.toString());
		log.info(terms.toString());
		
		//we expect following percentages: 
		//empty referer: 50%
		//simple referer: 35%
		//cpc referer: 12%
		//email referer: 3%
		
		assertEquals(TEST_COUNT * 0.5, emptyRefrerCnt, 2000);
		assertEquals(TEST_COUNT * 0.35, simpleRefrerCnt, 2000);
		assertEquals(TEST_COUNT * 0.12, utmCpcRefrerCnt, 2000);
		assertEquals(TEST_COUNT * 0.03, utmEmailRefrerCnt, 2000);
		
		assertFalse(campaign.isEmpty());
		assertFalse(source.isEmpty());
		assertFalse(content.isEmpty());
		assertFalse(terms.isEmpty());
		
		assertTrue(utmReferersWithoutContent > 0);
		assertTrue(utmReferersWithoutTerm > 0);
		assertTrue(utmReferersWithoutContentAndTerm > 0);
	}
			
	private Map<String,String> getUtmParams( String referer ) {
		Map<String,String> res = Maps.newHashMap();
		
		if( Strings.isNullOrEmpty(referer) ) {
			return res;
		}
		
		String[] url = referer.split("\\?");
		if( url.length > 1) {
			String[] params = url[1].split("&");
			for( String param : params) {
				String[] tab = param.split("=");
				res.put(tab[0], tab[1]);
			}
		} 
		return res;
	}		
}
