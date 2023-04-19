package com.dynatrace.diagnostics.uemload.dtheader;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.dynatrace.easytravel.constants.BaseConstants;



public class AbstractHeaderEntry implements HeaderEntry {

	private final String name;
	private final Map<String, String> valueMap;

	public AbstractHeaderEntry(String title) {
		this.name = title;
		this.valueMap = new HashMap<String, String>();
	}

	@Override
	public HeaderEntry addHeaderEntry(String key, String value) {
		if (key == null) {
			throw new NullPointerException("The key is not allowed to be null.");
		}
		valueMap.put(key, value);
		return this;
	}

	@Override
	public String getHeaderName() {
		return name;
	}

	@Override
	public String getHeaderValue() {
		StringBuilder headerValue = new StringBuilder();

		for (Entry<String, String> entry : valueMap.entrySet()) {
			headerValue
					.append(entry.getKey())
					.append(BaseConstants.EQUAL)
					.append(entry.getValue())
					.append(BaseConstants.SCOLON);
		}
		return headerValue.toString();
	}

	@Override
	public boolean hasValue() {
		return valueMap.size() > 0;
	}


}
