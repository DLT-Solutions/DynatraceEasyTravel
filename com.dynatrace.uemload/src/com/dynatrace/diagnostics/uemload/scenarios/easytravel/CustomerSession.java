package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.utils.Journeys;
import com.dynatrace.easytravel.misc.CommonUser;

public class CustomerSession extends BasicCostumerSession {

	private int journeyId = Journeys.NO_JOURNEY_FOUND;
	private boolean paymentSuccessful = false;
	private boolean bookingConfirmed = false;
	private boolean confirmationPageDisplayed = false;

	private static final Pattern windowPattern = Pattern.compile("input name=\"ice.window\" type=\"hidden\" value=\"([^\"]+)\"");
	private static final Pattern viewStatePattern = Pattern.compile("id=\"javax.faces.ViewState\" value=\"([^\"]+)\"");
	private static final Pattern viewPattern = Pattern.compile("input name=\"ice.view\" type=\"hidden\" value=\"([^\"]+)\"");

	private String window;
	private String view;
	private String viewState;

	private Map<String, String> attributes = new HashMap<String, String>();

	public CustomerSession(String host, CommonUser user, Location location, boolean taggedWebRequest) {
		super(host, user, location, taggedWebRequest);
	}

	public void setJourneyId(int journeyId) {
		this.journeyId = journeyId;
	}

	public int getJourneyId() {
		return journeyId;
	}

	public void setPayementSuccessful(boolean paymentSuccessful) {
		this.paymentSuccessful  = paymentSuccessful;

	}

	public boolean isPaymentSuccessful() {
		return paymentSuccessful;
	}

	public void setBookingConfirmed(boolean bookingConfirmed) {
		this.bookingConfirmed = bookingConfirmed;
	}

	public void setConfirmationPageDisplayed(boolean confirmationPageDisplayed) {
		this.confirmationPageDisplayed = confirmationPageDisplayed;
	}

	public boolean isConfirmationPageDisplayed() {
		return confirmationPageDisplayed;
	}

	public boolean isBookingConfirmed() {
		return bookingConfirmed;
	}

	public String getView() {
		return view;
	}

	public String getWindow() {
		return window;
	}

	public String getViewState() {
		return viewState;
	}

	@Override
	public void setResponseHtml(String html) {
		if (html != null) {
			Matcher matcher;
			matcher = windowPattern.matcher(html);
			if (matcher.find()) {
				window = matcher.group(1);
			}
			matcher = viewPattern.matcher(html);
			if (matcher.find()) {
				view = matcher.group(1);
			}
			matcher = viewStatePattern.matcher(html);
			if (matcher.find()) {
				viewState = matcher.group(1);
			}
		}
	}

	public void setAttribute(String key, String value) {
		this.attributes.put(key, value);
	}

	public String getAttribute(String key) {
		return this.attributes.get(key);
	}


}
