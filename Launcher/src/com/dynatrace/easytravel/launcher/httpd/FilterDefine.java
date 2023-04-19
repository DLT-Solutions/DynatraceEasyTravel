package com.dynatrace.easytravel.launcher.httpd;

import com.dynatrace.easytravel.constants.BaseConstants;

import java.io.PrintWriter;

public final class FilterDefine {

	private FilterDefine() {
	}

	private static final String LOAD_MODULE = "LoadModule ext_filter_module modules/mod_ext_filter.so";

	/**
	 * Loading module mod_ext_filter.so to Apache httpd.conf
	 *
	 * @param writer
	 */
	public static void writeModule(PrintWriter writer) {
		writer.println(LOAD_MODULE);
	}

	public static void writeExtFilterDefine(PrintWriter writer) {
		writer.print(createExtFilterDefine());
	}

	public static void writeFilterLocation(PrintWriter writer) {
		writer.println(createFilterLocation());
	}


	private static String createExtFilterDefine() {
		String extFilterDefine = new StringBuilder().append(BaseConstants.CRLF)
				.append(ExtFilterDefineConfig.DIRECTIVE)
				.append(ExtFilterDefineConfig.FILTER_NAME)
				.append(ExtFilterDefineConfig.MODE_INPUT)
				.append(ExtFilterDefineConfig.CMD)
				.toString();

		return extFilterDefine;
	}

	private static String createFilterLocation() {
		return new StringBuilder().append(BaseConstants.CRLF)
				.append(ExtFilterDefineConfig.REQUEST_LOCATION_SLOWDOWN).append(BaseConstants.CRLF)
				.append(ExtFilterDefineConfig.SET_INPUT_FILTER).append(ExtFilterDefineConfig.FILTER_NAME).append(BaseConstants.CRLF)
				.append(ExtFilterDefineConfig.END_LOCATION_XML_TAG).append(BaseConstants.CRLF)
				.toString();
	}

}

