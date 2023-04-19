package com.dynatrace.easytravel;

/**
 * This plugin adds a flash banner to the footer
 *
 * @author cwat-moehler
 */
public class FlashBanner extends FlashElement {

	@Override
	protected String getFlashFileName() {
		return "FlashBanner";
	}

}
