package com.dynatrace.diagnostics.uemload;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DetectedFrameworks {

	enum Framework {
		JQUERY('j'),
		PROTOTYPE('o'),
		ICEFACES('i'),
		ANGULARJS('g');
		
		private char featureHash;
		
		private Framework(char featureHash) {
			this.featureHash = featureHash;
		}
		
		public char getFeatureHash() {
			return featureHash;
		}
		
	}
	
	private Map<Framework, String> detectedFrameworks = new HashMap<>();
	
	public DetectedFrameworks() {
		
	}
	
	public void addFramework(Framework framework, String version) {
		detectedFrameworks.put(framework, version);
	}
	
	public String getQueryValue() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(Entry<Framework, String> entry : detectedFrameworks.entrySet()) {
			if (first) {
				first = false;
			} else {
				sb.append(";");
			}
			sb.append(entry.getKey().getFeatureHash());
			sb.append(entry.getValue());
		}
		
		return sb.toString();
	}

	public boolean isEmpty() {
		return detectedFrameworks.isEmpty();
	}
	
	
}
