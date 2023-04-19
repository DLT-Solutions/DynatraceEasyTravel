package com.dynatrace.diagnostics.uemload.dtheader;



public interface HeaderEntry {

	HeaderEntry addHeaderEntry(String key, String value);

	String getHeaderName();

	String getHeaderValue();

	boolean hasValue();
}