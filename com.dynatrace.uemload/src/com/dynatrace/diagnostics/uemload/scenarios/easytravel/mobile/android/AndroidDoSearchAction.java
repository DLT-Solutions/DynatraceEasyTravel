/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: MobileDoLoginAction.java
 * @date: 20.01.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.android;


import static com.dynatrace.diagnostics.uemload.utils.UemLoadHttpUtils.createPair;

import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xmlbeans.XmlException;

import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.http.base.HttpResponse;
import com.dynatrace.diagnostics.uemload.http.callback.ErrorHandlingHttpResonseCallbackAdapter;
import com.dynatrace.diagnostics.uemload.http.exception.PageNotAvailableException;
import com.dynatrace.diagnostics.uemload.mobile.MobileDevice;
import com.dynatrace.diagnostics.uemload.mobile.MobileDeviceType;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PseudoRandomJourneyDestination;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.MobileEasyTravelAction;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.MobileSession;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUrlUtils;
import com.dynatrace.easytravel.business.webservice.FindJourneysResponseDocument;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Action;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Argument;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Response;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Url;
import com.dynatrace.easytravel.jpa.business.xsd.Journey;

/**
 *
 * @author peter.lang
 */
public class AndroidDoSearchAction extends MobileEasyTravelAction {

	private static final Logger LOGGER = Logger.getLogger(AndroidDoSearchAction.class.getName());

	/**
	 *
	 * @param session
	 * @author peter.lang
	 */
	public AndroidDoSearchAction(MobileSession session) {
		super(session);
	}

	@Override
	protected void runOnDevice(final MobileDevice device, final UEMLoadCallback continuation) throws Exception {
		device.startAction(getSession(), Action.MOBILE_SEARCH_ANDROID);
		device.startAction(getSession(), Action.MOBILE_SEARCH_SOAP);

		Calendar fromDate = Calendar.getInstance();
		fromDate.set(Calendar.HOUR, 0);
		fromDate.set(Calendar.MINUTE, 0);
		fromDate.set(Calendar.SECOND, 0);
		fromDate.set(Calendar.MILLISECOND, 0);

		Calendar toDate = (Calendar) fromDate.clone();
		toDate.add(Calendar.YEAR, 1);

		String query = UemLoadUrlUtils.getUrl(getSession().getHost(), Url.MOBILE_SEARCH,
				createPair(Argument.DESTINATION, PseudoRandomJourneyDestination.get()),
				createPair(Argument.FROM_DATE, String.valueOf(fromDate.getTimeInMillis())),
				createPair(Argument.TO_DATE, String.valueOf(toDate.getTimeInMillis())));

		// unset to aid garbage collection
		fromDate = null;
		toDate = null;

		if(device.getDeviceType() != MobileDeviceType.SAMSUNG_GALAXY_TAB	//no tablets
				&& PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.MOBILE_ERRORS)){
			if (Math.random() < 0.4) {
				String failingAdsQuery = UemLoadUrlUtils.getUrl(getSession().getHost(), "services/AdService/",
						createPair("ad", "true"));
				device.performWebRequest(getSession(), failingAdsQuery, 0, new ErrorHandlingHttpResonseCallbackAdapter() {
					@Override
					public void readDone(HttpResponse response) throws IOException {}
					@Override
					public void handleRequestError(PageNotAvailableException exception) throws IOException {}
				});
			}
			device.reportErrorCode(getSession(), "failed to display Ad", -3546);
		}

		device.performWebRequest(getSession(), query, 0, new ErrorHandlingHttpResonseCallbackAdapter() {

			@Override
			public void readDone(HttpResponse response) throws IOException {
				if (response.getStatusCode() != 200) {
					searchRequestFinished(device, null, continuation);
					return;
				}

				String xml = response.getTextResponse();
				if (LOGGER.isLoggable(Level.FINE)) {
					LOGGER.fine("journeySearch response size: " + xml.length());
				}
				Journey[] journeys = null;
				try {
					FindJourneysResponseDocument xmlResponseDoc = FindJourneysResponseDocument.Factory.parse(xml);
					journeys = xmlResponseDoc.getFindJourneysResponse().getReturnArray();
				} catch (XmlException e) {
					LOGGER.log(Level.WARNING, "Unable to parse journey search response.", e);
				}
				searchRequestFinished(device, journeys, continuation);
			}

			@Override
			public void handleRequestError(PageNotAvailableException exception) throws IOException {
				searchRequestFinished(device, null, continuation);
			}

		});

	}

	protected void searchRequestFinished(MobileDevice device, Journey[] journeys, UEMLoadCallback continuation) {
		getSession().setJourneys(journeys);
		int journeysFound = journeys == null ? 0 : journeys.length;
		device.simulateProcessingOnDevice(10, 200);
		device.leaveAction(getSession());
		if (journeysFound > 0) {
			device.reportValue(getSession(), Response.JOURNEY_FOUND, journeysFound);
		} else {
			device.reportEvent(getSession(), Response.NO_JOURNEY_FOUND);
		}
		device.simulateProcessingOnDevice(10, 80);
		device.leaveAllActions(getSession());
		try {
			cont(continuation);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Couldn't finish mobile journey search action.", e);
		}
	}

}
