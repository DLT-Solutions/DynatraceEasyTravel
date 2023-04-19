package com.dynatrace.easytravel;

/**
 * This plugin adds a flash banner to the footer
 *
 * @author cwat-moehler
 */
public class FlashWithError extends FlashElement {

	@Override
	protected String getFlashFileName() {
		return "FlashError";
	}

    protected String getResourceFolder() {
    	return "ErrorInFlashContent";
    }

}
