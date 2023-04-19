package com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile;


import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.ArrayUtils;

import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.http.base.ResponseHeaders;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.BasicCostumerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CreditCard;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.jpa.business.xsd.Journey;
import com.dynatrace.easytravel.misc.CommonUser;

public class MobileSession extends BasicCostumerSession {

	private static final AtomicInteger visitIdCounter;
	private static final AtomicInteger sessionIdCounter;

	static {
		/* avoid equal start values for visitId and sessionId for different application starts
		 * in order to avoid correlation of a second visit to the first visit. */
		Random rnd = new Random(System.currentTimeMillis());
		visitIdCounter = new AtomicInteger(rnd.nextInt(1000));
		sessionIdCounter = new AtomicInteger(rnd.nextInt(1000));
	}

	private final String applicationId;
	private final String applicationName;
	private final String visitId;
	private final String sessionId;
	private CreditCard creditCard = null;
	private long startTime;
	private String gpsCoordinates;
	private boolean isRooted;

	private Journey[] journeys;
	private boolean loginSuccessful;


	/**
	 *
	 * @param host
	 * @param location
	 * @param applicationid
	 * @param applicationName
	 * @param startTime
	 * @param gpsCoordinates in format "latitudeXlongitude" e.g. "48.32964X14.31968" or null if not available
	 * @param isRooted
	 */
	public MobileSession(String host, CommonUser user, Location location, String applicationid, String applicationName, long startTime, String gpsCoordinates, boolean isRooted) {
		super(host, user, location, false);
		this.applicationId = applicationid;
		this.applicationName = applicationName;
		this.visitId = String.valueOf(visitIdCounter.incrementAndGet());
		this.sessionId = String.valueOf(sessionIdCounter.incrementAndGet());
		this.startTime = startTime;
		this.gpsCoordinates = gpsCoordinates;
		this.isRooted = isRooted;
	}

	@Override
	public void setResponseHtml(String html) {
		/* nothing todo here */
	}

	/* (non-Javadoc)
	 * @see com.dynatrace.diagnostics.uemload.scenarios.easytravel.UEMLoadSession#setResponseHeaders(java.util.Map)
	 */
	@Override
	public void setResponseHeaders(ResponseHeaders responseHeaders) {
		/* nothing todo here */
	}

	/**
	 * @return the visitId
	 */
	public String getVisitId() {
		return visitId;
	}

	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 *
	 * @return start time of session in ms since 1970
	 */
	public long getStartTime() {
		return startTime;
	}


	/**
	 *
	 * @return gpsCoordinates in format "latitudeXlongitude" e.g. "48.32964X14.31968" or null if not available
	 */
	public String getGpsCoordinates() {
		return gpsCoordinates;
	}

	public boolean isRooted() {
		return isRooted;
	}

	/**
	 *
	 * @param journeys
	 * @author peter.lang
	 */
	public void setJourneys(Journey[] journeys) {
		this.journeys = ArrayUtils.clone(journeys);
	}


	/**
	 * @return the journeys
	 */
	public Journey[] getJourneys() {
		return journeys;
	}

	/**
	 *
	 * @param authenticateSuccessful
	 * @author peter.lang
	 */
	public void setLoginSuccessful(boolean authenticateSuccessful) {
		this.loginSuccessful = authenticateSuccessful;
	}


	/**
	 * @return the loginSuccessful
	 */
	public boolean isLoginSuccessful() {
		return loginSuccessful;
	}

	/**
	 *
	 * @return
	 * @author peter.lang
	 */
	public Journey randomJourney() {
		if (journeys!=null && journeys.length>0) {
			return journeys[UemLoadUtils.randomInt(journeys.length)];
		}
		return null;
	}

	/**
	 *
	 * @return
	 * @author peter.lang
	 */
	public String getCreditCardNo() {
		checkCreditCard();
		return String.valueOf(creditCard.getNumber());
	}

	private void checkCreditCard() {
		if (creditCard==null) {
			creditCard = new CreditCard(getUser().getName());
		}
	}


	/**
	 * @return the applicationId
	 */
	public String getApplicationId() {
		return applicationId;
	}


	/**
	 * @return the applicationName
	 */
	public String getApplicationName() {
		return applicationName;
	}

}
