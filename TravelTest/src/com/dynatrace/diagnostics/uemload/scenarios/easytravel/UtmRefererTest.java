package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import org.junit.Test;
import static org.junit.Assert.*;

import com.dynatrace.diagnostics.uemload.RandomSet;

/**
 * @author Rafal.Psciuk
 *
 */
public class UtmRefererTest {
	@Test
	public void test() {
		RandomSet<UtmSource> source = new RandomSet<UtmSource>();
		source.add(new UtmSource("google", "http://www.google.com"),1);
		RandomSet<String> utm_campaign = new RandomSet<String>();
		utm_campaign.add("campaign",1);
		RandomSet<String> utm_term = new RandomSet<String>();
		utm_term.add("term",1);
		RandomSet<String> utm_content = new RandomSet<String>();
		utm_content.add("content",1);
		
		Referer ref = new UtmReferer.Builder()
		.setMedium(UtmReferer.UtmMedium.CPC)
		.setSource(source)
		.setCampaign(utm_campaign)
		.setTerm(utm_term)
		.setContent(utm_content)
		.build();
		
		assertEquals("http://www.google.com?utm_source=google&utm_medium=cpc&utm_campaign=campaign&utm_term=term&utm_content=content", ref.getReferer());
		
		ref = new UtmReferer.Builder()
		.setMedium(UtmReferer.UtmMedium.CPC)
		.setSource(source)
		.setCampaign(utm_campaign)
		.build();
		assertEquals("http://www.google.com?utm_source=google&utm_medium=cpc&utm_campaign=campaign", ref.getReferer());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreationExecetion() {
		new UtmReferer.Builder().build();
	}
}
