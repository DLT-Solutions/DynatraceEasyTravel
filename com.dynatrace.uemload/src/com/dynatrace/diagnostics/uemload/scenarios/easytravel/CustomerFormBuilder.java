package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.diagnostics.uemload.http.base.UemLoadFormBuilder;
import com.dynatrace.easytravel.constants.BaseConstants;


public class CustomerFormBuilder extends UemLoadFormBuilder {

	private CustomerSession session;
	private String view;
	private String window;
	private String viewState;

	private static final Logger LOGGER = Logger.getLogger(CustomerFormBuilder.class.getName());

	public CustomerFormBuilder(CustomerSession session2, boolean isIceForm) {
		try {
			this.session = session2;
			initializeWindowProperties();
			addStandardParams(isIceForm);
		} catch (IOException e) {
			logWarning(e);
		}
	}

	private void logWarning(Exception e) {
		LOGGER.log(Level.WARNING, "EasyTravelFormBuilder could not complete successfully.", e);
	}

	private void addStandardParams(boolean isIceForm) {
		add("iceform", "iceform");
		add("ice.view", view);
		add("ice.window", window);
		add("javax.faces.ViewState", viewState);
		add("javax.faces.partial.ajax", "true");
		add("javax.faces.partial.execute", "@all");
		add("javax.faces.partial.render", "@all");
		add("ice.submit.serialization", "sb");
		add("ice.submit.type", "ice.s");
		add("icefacesCssUpdates", BaseConstants.EMPTY_STRING);
		add("ice.event.type", "onclick");

		if (isIceForm) {
			addStandardIceFormParams();
		}
	}

	private void initializeWindowProperties() throws IOException {
		loadWindowProperties();
		throwExceptionIfWindowPropertiesNotLoaded();
	}


	private void throwExceptionIfWindowPropertiesNotLoaded() throws IOException {
		if (areWindowPropertiesLoaded()) {
			return;
		}
		throw new IOException();
	}

	private void loadWindowProperties() {
		view = session.getView();
		viewState = session.getViewState();
		window = session.getWindow();
	}

	private boolean areWindowPropertiesLoaded() {
		return view != null &&
				window != null &&
				viewState != null;

	}

	private void addStandardIceFormParams() {
		add("iceform:j_idiceform:fromDatesp", "");
		add("iceform:j_idiceform:j_idt85dropID", "");
		add("iceform:j_idiceform:j_idt85status", "");
		add("iceform:j_idiceform:toDatesp", "");
		add("iceform:toDate", "");
		add("iceform:toDate_cc", "");
		add("iceform:fromDate", "");
		add("iceform:fromDate_cc", "");
		add("iceform:iceDND", "");
	}
}
