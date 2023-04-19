package com.dynatrace.easytravel.json;

import org.junit.Assert;
import org.junit.Test;

import com.dynatrace.easytravel.spring.RemotePluginService;

public class JSONTest {
	
	private static final String STR1 = "ABC";
	private static final String STR2 = "DEF";
	private static final String STR3 = "\",\"";

	@Test
	public void testJSON() throws Exception {
		check(new String[] {
			STR1,
			STR2,
			STR3
		});
		check(new String[] {
				JSONObject.valueToString(new String[] {
					STR1,
					STR2,
					STR3
				}),
				STR1,
				JSONObject.valueToString(new String[] {
						STR1,
						STR2,
						STR3
					}),
				STR2,
				JSONObject.valueToString(new String[] {
						STR1,
						STR2,
						STR3
				}),
				STR3
			});
	}
	
	private void check(String[] values) throws Exception {
		String json = JSONObject.valueToString(values);
		String[] result = RemotePluginService.jsonToStringArray(json);
		try {
			Assert.assertArrayEquals(values, result);
			System.out.println(new StringBuilder(arrayToString(values)).append(" --> ").append(json).append(" --> ").append(arrayToString(result)).toString());
		} catch (AssertionError e) {
			System.err.println(new StringBuilder(arrayToString(values)).append(" --> ").append(json).append(" --> ").append(result).toString());
			throw e;
		}
	}
	
	private static String arrayToString(String[] a) {
		if (a == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < a.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			String v = a[i];
			if (v == null) {
				sb.append("null");
			} else {
				sb.append("\"").append(v).append("\"");
			}
		}
		
		return sb.toString();
	}
}
