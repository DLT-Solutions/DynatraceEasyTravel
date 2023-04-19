package com.dynatrace.diagnostics.uemload.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.dynatrace.diagnostics.uemload.utils.AngularUTMParams.AngularUTMParamsBuilder;

public class AngularUTMParamsTest {
	
	@Test
	public void createAngularUTMParamsString() {
		String campaign = "spring_sale";
		String medium = "social";
		String source = "facebook";
		String term = "paris";
		String content = "city_of_love";
		String gclid = "EAIaIQobChMIpJHIrrPE6AIVSrDtCh0wRQYZEAAYAiAAEgLBkPD_BwE";
		
		AngularUTMParams params = new AngularUTMParamsBuilder(campaign, medium, source).build();
		
		AngularUTMParams extraParams = new AngularUTMParamsBuilder(campaign, medium, source)
				.utmTerm(term)
				.utmContent(content)
				.build();
		
		AngularUTMParams paramsGclid = new AngularUTMParamsBuilder(campaign, medium, source)
				.utmTerm(term)
				.utmContent(content)
				.gclid(gclid)
				.build();

		assertEquals(params.toString(), "?utm_source=facebook&utm_medium=social&utm_campaign=spring_sale");
		assertEquals(extraParams.toString(), "?utm_source=facebook&utm_medium=social&utm_campaign=spring_sale&utm_term=paris&utm_content=city_of_love");
		assertEquals(paramsGclid.toString(), "?utm_source=facebook&utm_medium=social&utm_campaign=spring_sale&utm_term=paris&utm_content=city_of_love&gclid=EAIaIQobChMIpJHIrrPE6AIVSrDtCh0wRQYZEAAYAiAAEgLBkPD_BwE");
	}
}
