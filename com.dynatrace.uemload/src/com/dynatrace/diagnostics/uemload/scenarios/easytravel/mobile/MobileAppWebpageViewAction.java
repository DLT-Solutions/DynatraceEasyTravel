package com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile;


import java.net.URLEncoder;

import com.dynatrace.diagnostics.uemload.Bandwidth;
import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.diagnostics.uemload.BrowserWindowSize;
import com.dynatrace.diagnostics.uemload.RandomSet;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.mobile.MobileDevice;
import com.dynatrace.diagnostics.uemload.mobile.MobileDeviceType;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EasyTravelPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUrlUtils;


/**
 *
 * @author daniel.kaneider
 */
public class MobileAppWebpageViewAction extends MobileEasyTravelAction {

	private static RandomSet<EtPageType> pages = new RandomSet<EtPageType>();

	static {
		pages.add(EtPageType.MOBILE_CONTACT, 3);
		pages.add(EtPageType.MOBILE_PRIVACY, 2);
		pages.add(EtPageType.MOBILE_TERMS, 2);
		pages.add(null, 3); // no web visit
	}


	public MobileAppWebpageViewAction(MobileSession session) {
		super(session);
	}

	@Override
	protected void runOnDevice(final MobileDevice device, final UEMLoadCallback continuation) throws Exception {
		final EtPageType newRandom = pages.getRandom();

		if (newRandom == null) {
			cont(continuation);
			return;
		}

		EasyTravelPage easyTravelPage = new EasyTravelPage(newRandom, getSession(), true) {

			/** @see com.dynatrace.diagnostics.uemload.scenarios.easytravel.EasyTravelPage#runInBrowser(com.dynatrace.diagnostics.uemload.Browser, com.dynatrace.diagnostics.uemload.UEMLoadCallback) */
			@Override
			public void runInBrowser(Browser browser, UEMLoadCallback continuation) throws Exception {
				loadPage(browser, UemLoadUrlUtils.getUrl(getHost(), newRandom), continuation);
			}
		};

		MobileDeviceType deviceType = device.getDeviceType();
		BrowserType browserType = deviceType.getBrowserType();

		String adkCookie = URLEncoder.encode(getDynaTraceSessionId(getSession().getVisitId(), getSession().getSessionId(), getSession().getApplicationId()), "UTF-8"); //$NON-NLS-1$

		BrowserWindowSize browserWindowSize = BrowserWindowSize.getWindowSize(deviceType.getScreenWidth(), deviceType.getScreenHeight());
		Browser browser = new Browser(browserType, device.getLocation(), device.getLatency(),
			Bandwidth.BROADBAND, browserWindowSize, device.getVisitorId());
		browser.setDtAdkCookie(adkCookie);

		easyTravelPage.run(browser, continuation);
	}

    public static String getDynaTraceSessionId(String visitorId, String mobileSessionId, String mobileAppName) {
    	return visitorId + "_" + mobileSessionId + "_" + mobileAppName + "_m"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
