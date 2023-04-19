/**
 *
 * @author: cwat-pharukst
 */
package com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.ios;

import static com.dynatrace.diagnostics.uemload.utils.UemLoadHttpUtils.createPair;

import java.io.IOException;
import java.util.Arrays;
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
import com.dynatrace.easytravel.business.webservice.FindLocationsResponseDocument;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Action;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Argument;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Response;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Url;
import com.dynatrace.easytravel.jpa.business.xsd.Journey;
import com.dynatrace.easytravel.jpa.business.xsd.Location;

/**
 *
 * @author cwat-pharukst
 */
public class IOSPerformSearchAction extends MobileEasyTravelAction {

	private static final Logger LOGGER = Logger.getLogger(IOSPerformSearchAction.class.getName());

	/**
	 *
	 * @param session
	 * @author cwat-pharukst
	 */
	public IOSPerformSearchAction(MobileSession session) {
		super(session);
	}

	@Override
	protected void runOnDevice(final MobileDevice device, final UEMLoadCallback continuation) throws Exception {
		final MobileSession session = getSession();
		device.startAction(session, Action.MOBILE_SEARCH_IOS);

		Calendar fromDate = Calendar.getInstance();
		fromDate.set(Calendar.HOUR, 0);
		fromDate.set(Calendar.MINUTE, 0);
		fromDate.set(Calendar.SECOND, 0);
		fromDate.set(Calendar.MILLISECOND, 0);

		Calendar toDate = (Calendar) fromDate.clone();
		toDate.add(Calendar.YEAR, 1);

		String destination = PseudoRandomJourneyDestination.get();
		String searchQuery = UemLoadUrlUtils.getUrl(session.getHost(), Url.MOBILE_SEARCH,
				createPair(Argument.DESTINATION, destination),
				createPair(Argument.FROM_DATE, String.valueOf(fromDate.getTimeInMillis())),
				createPair(Argument.TO_DATE, String.valueOf(toDate.getTimeInMillis())));
		String failingAdsQuery = UemLoadUrlUtils.getUrl(session.getHost(), "services/AdService/",
				createPair("ad", "true"));

		// unset to aid garbage collection
		fromDate = null;
		toDate = null;

		//search as you type autocomplete
		device.startAction(session, "performSearchForDestinationNamesParent");
		device.leaveAction(session);
		for (int i = 0; i < destination.length() && i < 4; i++) {	//search first 4 letters for autocomplete search
			device.startAction(session, "performSearchAsYouTypeForDestinationNames");
			final String searchSubstring = destination.substring(0, i+1);
			final int requestId = i+1;
			String searchAsYouTypeQuery = UemLoadUrlUtils.getUrl(session.getHost(), Url.MOBILE_SEARCH_AS_YOU_TYPE,
					createPair(Argument.NAME, searchSubstring),
					createPair(Argument.CHECK_FOR_JOURNEYS, String.valueOf(true)),
					createPair(Argument.MAX_RESULT_SIZE, String.valueOf(20)));
			device.performWebRequest(session, searchAsYouTypeQuery, 0, new ErrorHandlingHttpResonseCallbackAdapter() {
				@Override
				public void readDone(HttpResponse response) throws IOException {
					if (response.getStatusCode() != 200) {
						searchRequestFinished(device, null, continuation);
						return;
					}

					String xml = response.getTextResponse();
					if (LOGGER.isLoggable(Level.FINE)) {
						LOGGER.fine("destinationSearch response size: " + xml.length());
					}
					Location[] locations = null;
					try {
						FindLocationsResponseDocument xmlResponseDoc = FindLocationsResponseDocument.Factory.parse(xml);
						locations = xmlResponseDoc.getFindLocationsResponse().getReturnArray();
					} catch (XmlException e) {
						LOGGER.log(Level.WARNING, "Unable to parse destination search response.", e);
					}
					device.reportValue(session, "requestId", requestId);
					device.reportValue(session, "operationKey", searchSubstring);
					device.reportValue(session, "resultSize", locations != null ? locations.length : 0);
					device.leaveAction(session);
				}
				@Override
				public void handleRequestError(PageNotAvailableException exception) throws IOException {}
			});
		}

		device.startAction(session, "performSearchForDestination");

		if(device.getDeviceType() != MobileDeviceType.APPLE_IPAD_PRO_2	//no tablets
				&& PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.MOBILE_ERRORS)){
			if (Math.random() < 0.4) {
				device.performWebRequest(session, failingAdsQuery, 0, new ErrorHandlingHttpResonseCallbackAdapter() {
					@Override
					public void readDone(HttpResponse response) throws IOException {}
					@Override
					public void handleRequestError(PageNotAvailableException exception) throws IOException {}
				});
			}
			device.reportNSError(getSession(), "NSError", "failed to contact advertisements server",
									"256", Arrays.toString(new String[]{"NSCocoaErrorDomain", "The operation couldn't be completed. (Cocoa error 256.)", "NSError reason", "(null)"}), null);
			device.reportErrorCode(getSession(), "failed to display Ad", 256);
		}

		device.performWebRequest(session, searchQuery, 0, new ErrorHandlingHttpResonseCallbackAdapter() {

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
