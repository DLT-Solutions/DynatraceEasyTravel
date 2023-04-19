package com.dynatrace.diagnostics.uemload;


public class Assert {
	
	private Assert() {
		throw new IllegalStateException("Utility class");
	}

	public static void assertEquals(int expected, int actual) {
		if(expected != actual) {
			throw new RuntimeException("Expected: " + expected + ", but was: " + actual);
		}
	}

}
