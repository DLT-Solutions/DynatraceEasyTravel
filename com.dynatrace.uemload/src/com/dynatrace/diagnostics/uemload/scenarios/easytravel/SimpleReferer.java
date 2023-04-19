package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import com.dynatrace.diagnostics.uemload.RandomSet;


/**
 * @author Rafal.Psciuk
 *
 */
public class SimpleReferer implements Referer {
	
	private final RandomSet<String> referer;
	
	public static final Referer EMPTY_REFERER = new SimpleReferer("");
	
	public SimpleReferer(String ref) {
		referer = new RandomSet<String>();
		referer.add(ref, 1);
	}
	
	public SimpleReferer(RandomSet<String> ref) {
		referer = ref;
	}
	
	@Override
	public String getReferer() {
		return referer.getRandom();
	}
	

}
