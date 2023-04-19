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
import java.util.Arrays;
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
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.MobileEasyTravelAction;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.MobileSession;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUrlUtils;
import com.dynatrace.easytravel.business.webservice.StoreBookingResponseDocument;
import com.dynatrace.easytravel.constants.BaseConstants.Uem;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Argument;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Response;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Url;
import com.dynatrace.easytravel.jpa.business.xsd.Journey;

/**
 * Mobile native action which stores a booking for any journey previously found by
 * {@link AndroidDoSearchAction}.
 * @author peter.lang
 */
public class AndroidBookJourneyAction extends MobileEasyTravelAction {

	private static final Logger LOGGER = Logger.getLogger(AndroidBookJourneyAction.class.getName());

	/**
	 *
	 * @param session
	 * @author peter.lang
	 */
	public AndroidBookJourneyAction(MobileSession session) {
		super(session);
	}

	@Override
	protected void runOnDevice(final MobileDevice device, final UEMLoadCallback continuation) throws Exception {
		device.startAction(getSession(), Uem.Action.MOBILE_BOOK);
		device.startAction(getSession(), Uem.Action.MOBILE_BOOK_SOAP);
		final Journey journey = getSession().randomJourney();
		if (getSession().isLoginSuccessful() && journey != null) {
			String url = UemLoadUrlUtils.getUrl(getSession().getHost(), Url.MOBILE_BOOKING,
					createPair(Argument.JOURNEY_ID, String.valueOf(journey.getId())),
					createPair(Argument.USER_NAME, getSession().getUser().getName()),
					createPair(Argument.CREDIT_CARD, getSession().getCreditCardNo()),
					createPair(Argument.AMOUNT, String.valueOf(journey.getAmount()))
					);

			device.performWebRequest(getSession(), url, 0, new ErrorHandlingHttpResonseCallbackAdapter() {

				@Override
				public void readDone(HttpResponse response) throws IOException {
					if (response.getStatusCode() != 200) {
						if(response.getStatusCode() >= 400){
							device.reportErrorCode(getSession(), "HTTP Error", response.getStatusCode());
						}
						storeBookingFinished(device, null, 0, journey.getDestination().getName(), continuation);
						return;
					}

					String xml = response.getTextResponse();
					String bookingId = null;
					try {
						StoreBookingResponseDocument responseDocument = StoreBookingResponseDocument.Factory.parse(xml);
						bookingId = responseDocument.getStoreBookingResponse().getReturn();

					} catch (XmlException e) {						
						device.reportException(getSession(), "BookingError", e.getMessage(), e.getClass().getName(), Arrays.toString(e.getStackTrace()));
						LOGGER.log(Level.WARNING, "Unable to parse authentication response.", e);
					}
					storeBookingFinished(device, bookingId, (long) journey.getAmount(), journey.getDestination().getName(), continuation);
				}

				@Override
				public void handleRequestError(PageNotAvailableException exception) throws IOException {
					if (exception.getHttpResponseCode()>=400) {
						device.reportErrorCode(getSession(), "HTTP Error", exception.getHttpResponseCode());
						storeBookingFinished(device, null, 0, journey.getDestination().getName(), continuation);
					} else {
						device.reportErrorCode(getSession(), "Could not connect", -1);
						storeBookingFinished(device, null, 0, journey.getDestination().getName(), continuation);
					}
				}
			});
		/* Not done currently, it will leave socket-connections open until garbage collection, but
		 * otherwise we would try to continue the Action whereas we had a problem here...
		} else {
			cont(continuation);*/
		}
	}

	protected void storeBookingFinished(MobileDevice device, String bookingId, long amount, String destination, UEMLoadCallback continuation) {
		device.leaveAction(getSession());
		device.simulateProcessingOnDevice(20, 200);

		if(device.getDeviceType() != MobileDeviceType.SAMSUNG_GALAXY_TAB	//no tablets
				&& PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.MOBILE_ERRORS)){
			bookingId = null;
			device.reportException(getSession(), "BookingStatusError", "failed to resolve booking status",
					"java.lang.ArrayIndexOutOfBoundsException",
					Arrays.toString(new String[]{"failed to resolve booking status: java.lang.ArrayIndexOutOfBoundsException: length=1; index=1 near/at com.dynatrace.easytravel.android.activities.DisplayJourneyListActivity$5.run:233",
							"at com.dynatrace.easytravel.android.activities.DisplayJourneyListActivity$5.run(DisplayJourneyListActivity.java:233)", "at java.lang.Thread.run(Thread.java:856)"}));
		}

		if (bookingId != null) {
			device.reportValue(getSession(), Response.BOOK_JOURNEY_AMOUNT, amount);
			device.reportValue(getSession(), Response.BOOK_JOURNEY_DESTINATION, destination);
		} else {
			device.reportEvent(getSession(), Response.BOOKING_FAILED);
			device.reportValue(getSession(), Response.BOOK_JOURNEY_DESTINATION, destination);
		}

		device.leaveAllActions(getSession());
		try {
			cont(continuation);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Couldn't finish mobile storeBooking action.", e);
		}
	}
}
