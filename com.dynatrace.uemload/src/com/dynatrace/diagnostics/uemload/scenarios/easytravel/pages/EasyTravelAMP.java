package com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.BrowserWindowSize;
import com.dynatrace.diagnostics.uemload.NavigationTiming;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.http.base.HttpRequest;
import com.dynatrace.diagnostics.uemload.http.base.HttpRequest.Type;
import com.dynatrace.diagnostics.uemload.http.callback.HttpResponseCallback;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUrlUtils;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.DtCookieName;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.DynatraceUrlUtils;
import com.google.common.base.Strings;

import ch.qos.logback.classic.Logger;

import com.dynatrace.diagnostics.uemload.http.base.HttpResponse;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class EasyTravelAMP extends CustomerPage {
	
	private static final Logger LOGGER = LoggerFactory.make();
	
	private static final String TITLE = "easyTravel AMP website";
	
	private static final String[] PATHS = new String[]{"index.jsp", "gallery.jsp", "offices.jsp", "info.jsp", "hazel_hurst/", "latrobe/", "italy/", "imbler/", "hot_springs/"};
	private static final String aPattern = "1|1|_load_|_load_|-|%d|%d|0,2|2|_onload_|_load_|-|%d|%d|0";
	
	private static String protocol = StringUtils.trim(EasyTravelConfig.read().ampBfProtocol);
	private static String tenant = StringUtils.trim(EasyTravelConfig.read().ampBfTenant);
	private static String host = StringUtils.trim(EasyTravelConfig.read().ampBfEnvironment);
	private static String port = StringUtils.trim(EasyTravelConfig.read().ampBfPort);
	private static String app = StringUtils.trim(EasyTravelConfig.read().ampApplicationID);
	
	public enum State {
		Init, Random, Finish
	}
	
	private final State state;
	
	
	private String CONTENT = "OK(BF)|";
	
	public EasyTravelAMP(final CustomerSession session, final State state) {
		super(EtPageType.AMP, session);
		this.state = state;
	}
	
	@Override
	public void runInBrowser(final Browser browser, final UEMLoadCallback continuation) throws IOException, URISyntaxException {
		if(State.Init.equals(state)) {
			String url = UemLoadUrlUtils.getUrl(getHost(), "amp/");
			sendRequest(browser, continuation, url);
		} else if(State.Random.equals(state)) {
			String url = UemLoadUrlUtils.getUrl(getHost(), "amp/"+PATHS[UemLoadUtils.randomInt(PATHS.length)]);
			sendRequest(browser, continuation, url);
		} else {
			cont(continuation);
		}
	}
	
	private void sendRequest(final Browser browser, final UEMLoadCallback continuation, final String referer) throws IOException, URISyntaxException {		
		loadPage(browser, referer, continuation);
		HttpRequest request = new HttpRequest(getRequestUrl(browser, referer)).setMethod(Type.GET).setReferer(browser.getCurrentPage())
				.setHeader("Accept", "image/webp,image/apng,image/*,*/*;q=0.8")
				.setHeader("Accept-Encoding", "gzip, deflate, br")
				.setHeader("Accept-Language", "en-US,en;q=0.8,pl;q=0.6")
				.setHeader("Connection", "keep-alive")
				.setHeader("User-Agent", browser.getType().getUserAgent());
		if(DynatraceUrlUtils.isManagedOrLocalTenant(tenant)) {
			request.setHeader("Host", String.format("%s:%s", host, port));
		} else {
			request.setHeader("Host", String.format("%s.%s", tenant, host));
		}
	
		//TODO: Workaround for APM-108574. Should be removed when bug is fixed.
		browser.getUemLoadHttpClient().removeCookie(DtCookieName.DT_COOKIE);
		browser.send(request, new HttpResponseCallback() {

			@Override
			public void readDone(HttpResponse response) throws IOException {
				if (response.getStatusCode() != 200) {
					LOGGER.error("Wrong response code for AMP traffic GET request, was {0}, expected 200.",
							response.getStatusCode());
				} else {
					if(!response.getTextResponse().equals(CONTENT))
						LOGGER.error("Wrong text response for AMP traffic GET request, was {0}, expected " + CONTENT, response.getTextResponse());
				}
			}
		});
	}
	
	private String getRequestUrl(final Browser browser, final String referer) throws URISyntaxException {
		NavigationTiming nt = browser.getNavigationTiming();
		long navStart = nt.getNavigationStartTime();
		long loadStart = nt.getDomContentLoadedEventStart();
		long loadEnd = nt.getDomContentLoadedEventEnd();
		long time = nt.getLoadEventEnd();
		BrowserWindowSize bws = browser.getBrowserWindowSize();
		String fId = Integer.toString(UemLoadUtils.randomInt(1000, 9999));
		
		return new URIBuilder(DynatraceUrlUtils.getDynatraceAMPBeaconUrl())
			.addParameter("type", "js")
			.addParameter("flavor", "amp")
			.addParameter("v", "1")
			.addParameter("a", String.format(aPattern, navStart, loadEnd, loadStart, loadEnd))
			.addParameter("fId", fId)
			.addParameter("vID", browser.getVisitorId().getVisitorId())
			.addParameter("referer", referer)
			.addParameter("title", TITLE)
			.addParameter("sw", Integer.toString(bws.getScreenWidth()))
			.addParameter("sh", Integer.toString(bws.getScreenHeight()))
			.addParameter("w", Integer.toString(bws.getWidth()))
			.addParameter("h", Integer.toString(bws.getHeight()))
			.addParameter("nt", nt.createSignal())
			.addParameter("app", app)
			.addParameter("time", Long.toString(time))
			.addParameter("url", referer)
			.build().toString();
	}

	@Override
	protected void loadPage(Browser browser, String url, UEMLoadCallback pageLoadCallback) throws IOException {
		super.loadPage(browser, url, pageLoadCallback);
		browser.getNavigationTiming().createNavigationTimingDataForOnload();
	}
	
	public static boolean isConfigSet() {
		boolean isEmpty = Strings.isNullOrEmpty(protocol) || Strings.isNullOrEmpty(tenant)
				|| Strings.isNullOrEmpty(host) || Strings.isNullOrEmpty(port) || Strings.isNullOrEmpty(app);
		return !isEmpty;
	}
}
