package com.dynatrace.easytravel;

/**
 * This plugin adds a flash banner to the footer
 *
 * @author cwat-moehler
 */
public class FlashSlowPageAction extends FlashElement {

	@Override
	protected String getFlashFileName() {
		return "FlashSlowPageAction";
	}

	@Override
	public String getHeadInjection() {
		String setManualLoadEndString = "<script>"
				+ "dynaTrace.setLoadEndManually();"
				+ "dynaTrace.signalLoadStart();"
				+ "</script>";

		return super.getHeadInjection() + setManualLoadEndString;
	}

}
