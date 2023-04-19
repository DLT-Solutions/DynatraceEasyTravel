/**
 * @author: cwat-pharukst
 */
package com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.ios;


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
 * {@link IOSPerformSearchAction}.
 * @author cwat-pharukst
 */
public class IOSBookJourneyAction extends MobileEasyTravelAction {

	private static final Logger LOGGER = Logger.getLogger(IOSBookJourneyAction.class.getName());

	/**
	 *
	 * @param session
	 * @author cwat-pharukst
	 */
	public IOSBookJourneyAction(MobileSession session) {
		super(session);
	}

	@Override
	protected void runOnDevice(final MobileDevice device, final UEMLoadCallback continuation) throws Exception {
		device.startAction(getSession(), Uem.Action.MOBILE_BOOK);
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
						final String localizedDesc = "Unable to parse authentication response.";
						device.reportNSError(getSession(), "BookingError", e.getClass().getName(), e.getMessage(), Arrays.toString(e.getStackTrace()), localizedDesc);						
						LOGGER.log(Level.WARNING, localizedDesc, e);
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
		device.simulateProcessingOnDevice(20, 200);

		if(device.getDeviceType() != MobileDeviceType.APPLE_IPAD_PRO_2	//no tablets
				&& PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.MOBILE_ERRORS)){
			bookingId = null;
			device.reportNSError(getSession(), "failed to resolve booking status",
					"NSRangeException",
					"*** -[__NSArrayI objectAtIndex:]: index 1 beyond bounds [0 .. 0]",
					Arrays.toString(new String[]{
				"0   CoreFoundation                      0x0285e02e __exceptionPreprocess + 206",
				"1   libobjc.A.dylib                     0x0174fe7e objc_exception_throw + 44",
				"2   CoreFoundation                      0x02813b44 -[__NSArrayI objectAtIndex:] + 196",
				"3   easyTravel                          0x0001a7dd -[DTJourneyDetailsViewController operationFinished:] + 397",
				"4   libobjc.A.dylib                     0x017636b0 -[NSObject performSelector:withObject:] + 70",
				"5   easyTravel                          0x00013e14 -[DTRestUtils connectionDidFinishLoading:] + 964",
				"6   easyTravel                          0x000670b2 -[CPWRURLRequestTiming connectionDidFinishLoading_CPWR:] + 1372"
				}), null);
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
