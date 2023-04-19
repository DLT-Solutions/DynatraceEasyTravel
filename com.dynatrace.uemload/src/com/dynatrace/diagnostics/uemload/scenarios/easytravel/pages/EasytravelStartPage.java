package com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages;

import static java.lang.String.format;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codahale.metrics.Timer.Context;
import com.dynatrace.diagnostics.uemload.*;
import com.dynatrace.diagnostics.uemload.dtheader.DynaTraceHeader;
import com.dynatrace.diagnostics.uemload.http.base.HttpRequest;
import com.dynatrace.diagnostics.uemload.http.base.HttpRequest.Type;
import com.dynatrace.diagnostics.uemload.http.base.HttpResponse;
import com.dynatrace.diagnostics.uemload.http.base.UemLoadFormBuilder;
import com.dynatrace.diagnostics.uemload.http.callback.HttpResponseCallback;
import com.dynatrace.diagnostics.uemload.http.exception.SessionExpiredException;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.*;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType.PageAction;
import com.dynatrace.diagnostics.uemload.thirdpartycontent.ThirdPartyContentUtils;
import com.dynatrace.diagnostics.uemload.utils.Journeys;
import com.dynatrace.diagnostics.uemload.utils.UemLoadCalendarUtils;
import com.dynatrace.diagnostics.uemload.utils.UemLoadHttpUtils;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUrlUtils;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Action;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Title;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Url;
import com.dynatrace.easytravel.metrics.Metrics;
import com.dynatrace.easytravel.misc.CommonUser;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.base.Joiner;

public class EasytravelStartPage extends CustomerPage {

	private static final Pattern JOURNEY_ID_PATTERN = Pattern.compile("journeyId=(-?\\d+)");
	private static final Logger LOGGER = Logger.getLogger(EasytravelStartPage.class.getName());
	private static final int MAX_SEARCH_TRIES = PseudoRandomJourneyDestination.LOCATION_COUNT;
	private static final int MAX_WAIT_TIME_MS = (int) TimeUnit.SECONDS.toMillis(60);

	private static final RandomSet<Referer> referes = new RefererUtil().getReferers();

	public EasytravelStartPage(CustomerSession session, State state) {
		super(EtPageType.START, session, getPageReferer(state));
		this.state = state;
		if(state == State.INIT) {
			setPageLoadReferer(referes.getRandom().getReferer());
		}
		if(state == State.CALENDAR_DAY || state == State.CALENDAR_MONTH || state == State.CALENDAR_YEAR) {
			calendar = new UemLoadCalendarUtils();
		} else {
			calendar = null;
		}
	}
	
	public EasytravelStartPage(CustomerSession session, State state, UemLoadCalendarUtils calendar) {
		super(EtPageType.START, session, getPageReferer(state));
		this.state = state;
		this.calendar = calendar;
		if(state == State.INIT) {
			setPageLoadReferer(referes.getRandom().getReferer());
		}
	}
	
	private static String getPageReferer(State state) {
		if(state == State.INIT) {
			return referes.getRandom().getReferer();
		} 
		return null;
	}
	
	public enum State {
		INIT,
		LOGIN,
		CALENDAR_YEAR,
		CALENDAR_MONTH,
		CALENDAR_DAY,
		SEARCH,
		NEW_SEARCH,
		CLEAR;
	}

	private final State state;
	private final UemLoadCalendarUtils calendar;

	@Override
	public void runInBrowser(final Browser browser, UEMLoadCallback continuation) throws IOException {
		long timeStart = 0L;
		long timeEnd = 0L;
		long diff = 0L;
		if(LOGGER.getLevel() == Level.FINEST) {
			timeStart = System.currentTimeMillis();
		}
		final Context context = Metrics.getTimerContext(this, "EasytravelStartPage-runInBrowser-metrics");
		if (State.INIT.equals(state)) {
			visit(browser, continuation, new UEMOnLoadCallback() {
				
				@Override
				public void run() throws IOException {}
				
				@Override
				public void run(final UEMLoadCallback callback) throws IOException {
					String calcRecUrl = UemLoadUrlUtils.getUrl(getSession().getHost(), Url.MOBILE_CALCULATE_RECOMMENDATIONS);

					startCustomAction("ONLOAD_XHR", "C", "j1.8.1", browser, calcRecUrl, 3);

					String referrerUrl = browser.getCurrentPage();
					HttpRequest request = new HttpRequest(calcRecUrl).setMethod(Type.GET).setReferer(referrerUrl);
					browser.send(request, new HttpResponseCallback() {
						@Override
						public void readDone(HttpResponse response) throws IOException {
							finishCustomAction(browser, getProcessingTime(PageAction.ONLOAD_XHR), false, true);
							callback.run();
						}
					});
				}
			});
		} else if (State.LOGIN.equals(state)) {
			CommonUser user = getSession().getUser();
			login(browser, user.name, user.password, getProcessingTime(PageAction.LOGIN), continuation);
		} else if (State.SEARCH.equals(state)) {
			search(browser, getSession().getDestination(), getProcessingTime(PageAction.SEARCH), continuation);
		} else if (State.CALENDAR_YEAR.equals(state)) {
			calendarYear(browser, getSession().getDestination(), getProcessingTime(PageAction.CALENDAR), continuation);
		} else if (State.CALENDAR_MONTH.equals(state)) {
			calendarMonth(browser, getSession().getDestination(), getProcessingTime(PageAction.CALENDAR), continuation);
		} else if (State.CALENDAR_DAY.equals(state)) {
			calendarDay(browser, getSession().getDestination(), getProcessingTime(PageAction.CALENDAR), continuation);
		} else if (State.CLEAR.equals(state)) {
			clear(browser, getProcessingTime(PageAction.CLEAR), continuation);
		} else if(State.NEW_SEARCH.equals(state)) {
			newSearch(browser, getProcessingTime(PageAction.NEW_SEARCH), continuation);
		} else {
		    cont(continuation);
		}
		context.stop();
		context.close();
		if(LOGGER.getLevel() == Level.FINEST) {
			timeEnd = System.currentTimeMillis();
			diff = timeEnd - timeStart;
			String numberAsString = String.format("%010d", diff);
			LOGGER.finest("runInBrowser: time spent was <" + numberAsString + ">");
		}
	}

	public void visit(Browser browser, UEMLoadCallback continuation) throws IOException {
		loadPage(browser, continuation);
	}

	public void visit(Browser browser, UEMLoadCallback continuation, UEMOnLoadCallback pageOnLoad) throws IOException {
		loadPage(browser, continuation, pageOnLoad);
	}

	public void login(final Browser browser, final String user, String password, final int processingTime, final UEMLoadCallback continuation) throws IOException {
		if (browser == null) {
			throw new IllegalStateException("First, you have to call the visit method.");
		}

		startCustomAction(Action.LOGIN, "C", "icefaces.ajax", browser, getXhrUrl());

		DynaTraceHeader header = getSession().getHeader().setMetaData(getPage().getMetaData());

		final HttpRequest req = getXhr(browser, header);

		UemLoadFormBuilder form = getForm(true);
		form.add("_spring_security_remember_me", "on").
				add("ice.event.captured", "loginForm:loginSubmit").
				add("ice.event.target", "loginForm:loginSubmit").
				add("ice.focus", "").
				add("javax.faces.partial.event", "click").
				add("javax.faces.source", "loginForm:loginSubmit").
				add("loginForm", "loginForm").
				add("loginForm:loginSubmit", "Check In to easyTravel").
				add("spring-security-redirect", "/signup").
				add("loginForm:password", password).
				add("loginForm:username", user);

		sendForm(browser, form, req, new HttpResponseCallback() {

            @Override
            public void readDone(HttpResponse response) throws IOException {

            	String action;
            	String key = "dt_visittag";
            	int depth = 2;
            	int actionId = 5;
            	//in ruxit we expect the visit tag to be a root action
            	if (DtVersionDetector.isAPM()) {
            		key = "rx_visittag";
            		depth = 1;
            		actionId = 6;
            	}
            	if (JavaScriptAgent.shouldSendActionWithActionInfo()) {
            		action = Joiner.on(BaseConstants.PIPE).join(depth, actionId, key + "=" + user, "_rs_", BaseConstants.MINUS, "<endtime>", "<endtime>", 19);
            	}
            	else if (JavaScriptAgent.shouldSendActionWithActionId()) {
            		action = Joiner.on(BaseConstants.PIPE).join(depth, actionId, key + "=" + user, "_rs_", "<endtime>", "<endtime>", 19);
            	} else {
            		action = Joiner.on(BaseConstants.PIPE).join(depth ,key + "=" + user, BaseConstants.MINUS, "_rs_", "<endtime>", "<endtime>", 19);
            	}

            	Collection<String> additionalActions = new ArrayList<String>();
            	additionalActions.add(action);

            	if (PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.JAVASCRIPT_APP_VERSION_SPECIFICERROR)) {
        			JavaScriptErrorAction errorAction = JavaScriptErrorActionHelper.generateVersionSpecificErrorAction(JavaScriptAgent.getAppVersion(), browser.getCurrentPage(), browser.getType());
        			if (errorAction != null) {
        				browser.sendJavaScriptErrors(Arrays.asList(errorAction));
        			}
        		}

                final int xhrActionId = finishCustomAction(browser, processingTime, false, additionalActions);
                if (partialResponseLogging) {
                    LOGGER.info(response.getTextResponse());
                }

                //create XHR third party resources
				ThirdPartyContentUtils.createXhrThirdPartyResources(req.getUrl(), response, browser, xhrActionId, getSession());

                cont(continuation);
            }
        });
	}
	
	public void calendarYear(final Browser browser, String query, final int processingTime, final UEMLoadCallback continuation) throws IOException {
		calendarAction(browser, query, processingTime, continuation, calendar.getYearXhrActionName());
	}
	
	public void calendarMonth(final Browser browser, String query, final int processingTime, final UEMLoadCallback continuation) throws IOException {
		calendarAction(browser, query, processingTime, continuation, calendar.getMonthXhrActionName());
	}
	
	public void calendarDay(final Browser browser, String query, final int processingTime, final UEMLoadCallback continuation) throws IOException {
		calendarAction(browser, query, processingTime, continuation, calendar.getDayXhrActionName());
	}

	private void calendarAction(final Browser browser, String query, final int processingTime, final UEMLoadCallback continuation, final String xhrActionName) throws IOException {

		final String calcRecUrl = UemLoadUrlUtils.getUrl(getSession().getHost(), Url.START);
		startCustomAction(xhrActionName, "C", "j1.8.1", browser, calcRecUrl);

		// Based on the real click on sear action, the GET request for
		// calculate recommendations was added
		String referrerUrl = browser.getCurrentPage();
		HttpRequest request = new HttpRequest(calcRecUrl).setMethod(Type.GET).setReferer(referrerUrl);
		browser.send(request, new HttpResponseCallback() {
			@Override
			public void readDone(HttpResponse response) throws IOException {
				// intentionally left empty
			}
		});

		// Create search request
		DynaTraceHeader header = getSession().getHeader().setMetaData(getPage().getMetaData());
		final HttpRequest req = getXhr(browser, header);

		waitUntilSessionIsCompletlyLoaded();

		UemLoadFormBuilder form = getForm(false);
		form.add("ice.event.captured", "iceform:toDate_nm").add("ice.focus", "iceform:toDate_nm")
				.add("iceform:destination_idx", "").add("iceform:j_idcl", "iceform:toDate_nm")
				.add("iceform:destination", "").add("iceform:toDate", "").add("javax.faces.partial.event", "click")
				.add("iceform:travellers", "2").add("javax.faces.source", "iceform:toDate_nm");

		sendForm(browser, form, req, false, new HttpResponseCallback() {

			@Override
			public void readDone(HttpResponse response) throws IOException {
				final int xhrActionId = finishCustomAction(browser, processingTime, false);
				ThirdPartyContentUtils.createXhrThirdPartyResources(req.getUrl(), response, browser, xhrActionId,
						getSession());
				cont(continuation);
			}
		});
	}

	public void search(final Browser browser, String query, final int processingTime, final UEMLoadCallback continuation) throws IOException {
		search(browser, query, processingTime, 1, continuation);
	}


	void search(final Browser browser, String destination, final int processingTime, final int tryCnt, final UEMLoadCallback continuation) throws IOException {
		if (tryCnt == MAX_SEARCH_TRIES) {
			throw new IllegalStateException(format(
					"Performed %d searches without any results, some component of easyTravel seems to be broken",
					MAX_SEARCH_TRIES));
		}
				
		// Based on the real click on search action, this is the xhr url
		String calcRecUrl = UemLoadUrlUtils.getUrl(getSession().getHost(), Url.MOBILE_CALCULATE_RECOMMENDATIONS);
		
		// Start the custom action
		startCustomAction(Action.SEARCH, "C", "j1.8.1", browser, calcRecUrl);
		
		// Based on the real click on sear action, the GET request for calculate recommendations was added
		String referrerUrl = browser.getCurrentPage();
		HttpRequest request = new HttpRequest(calcRecUrl)
				.setMethod(Type.GET)
				.setReferer(referrerUrl);
		browser.send(request, new HttpResponseCallback() {
			@Override
			public void readDone(HttpResponse response) throws IOException {
				// intentionally left empty
			}
		});

		// Create search request
		DynaTraceHeader header = getSession().getHeader().setMetaData(getPage().getMetaData());
		final HttpRequest req = getXhr(browser, header);

		waitUntilSessionIsCompletlyLoaded();

		UemLoadFormBuilder form = getForm(false);
		form.add("ice.event.captured", "iceform:search").
				add("ice.event.target", "iceform:search").
				add("ice.focus", "iceform:search").
				add("iceform:destination_idx", "").
				add("iceform:j_idcl", "").
				add("iceform:destination", destination).
				add("iceform:search", "Search").
				add("iceform:toDate", "").
				add("javax.faces.partial.event", "click").
				add("iceform:travellers", "2").
				add("javax.faces.source", "iceform:search");

		boolean loadIFrames = isWeatherAvailable();

		sendForm(browser, form, req, loadIFrames, new HttpResponseCallback() {

			@Override
			public void readDone(HttpResponse response) throws IOException {
				int journeyId = getRandomJourneyId(response);
				getSession().setJourneyId(journeyId);

				if (partialResponseLogging) {
					LOGGER.info(response.getTextResponse());
				}
				int xhrActionId = finishCustomAction(browser, processingTime, false);

				if (isJourneyIdInvalid()) {
					search(browser, getSession().getNewDestination(), processingTime, tryCnt + 1, continuation);
				} else {

					//create XHR third party resources
					ThirdPartyContentUtils.createXhrThirdPartyResources(req.getUrl(), response, browser, xhrActionId,
							getSession());

					if (PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.JAVASCRIPT_ERROR_ONLABEL_CLICK) && JavaScriptErrorActionHelper.getNextRandom(0, 10) < 5) {
						JavaScriptErrorAction errorAction = JavaScriptErrorActionHelper.generateErrorOnLabelClick(browser, browser.getType());
						browser.sendJavaScriptErrors(Arrays.asList(errorAction));
					}

					if (isWeatherAvailable(getSession().getAttribute("forecast-link"))) {
						loadNodeJSWeather(browser, continuation);
					} else {
						checkInvalidWeatherLink(response, journeyId);
						cont(continuation);
					}					
				}
			}
		});
	}
	
	/**
	 * @author Michal.Bakula
	 */
	public void clear(final Browser browser, final int processingTime, final UEMLoadCallback continuation) throws IOException {
		final String startUrl = UemLoadUrlUtils.getUrl(getSession().getHost(), Url.START);		
		startCustomAction(Action.CLEAR, "C", "icefaces.ajax", browser, startUrl);

		// Create search request
		getSession().setJourneyId(Journeys.NO_JOURNEY_FOUND);
		DynaTraceHeader header = getSession().getHeader().setMetaData(getPage().getMetaData());
		final HttpRequest req = getXhr(browser, header);

		waitUntilSessionIsCompletlyLoaded();

		UemLoadFormBuilder form = getForm(false);
		form.add("ice.event.captured", "iceform:clear").
		add("ice.event.target", "iceform:clear").
		add("ice.focus", "iceform:clear").
		add("iceform:destination_idx", "").
		add("iceform:j_idcl", "iceform:clear").
		add("iceform:destination", getSession().getDestination()).
		add("iceform:toDate", "").
		add("javax.faces.partial.event", "click").
		add("iceform:travellers", "2").
		add("iceform:clear", "iceform:clear").
		add("javax.faces.source", "iceform:clear");
		

		sendForm(browser, form, req, false, new HttpResponseCallback() {

			@Override
			public void readDone(HttpResponse response) throws IOException {
				if (partialResponseLogging) {
					LOGGER.info(response.getTextResponse());
				}
				finishCustomAction(browser, processingTime, false);
				cont(continuation);
			}					
		});
	}
	
	/**
	 * @author Michal.Bakula
	 */
	public void newSearch(final Browser browser, final int processingTime, final UEMLoadCallback continuation)
			throws IOException {

		final String recUrl = UemLoadUrlUtils.getUrl(getSession().getHost(), Url.START);
		startCustomAction(Action.NEW_SEARCH, "C", "", browser, "");

		String referrerUrl = browser.getCurrentPage();
		final HttpRequest request = new HttpRequest(recUrl).setMethod(Type.GET).setReferer(referrerUrl);
		browser.send(request, new HttpResponseCallback() {
			@Override
			public void readDone(HttpResponse response) throws IOException {
				final int xhrActionId = finishCustomAction(browser, processingTime, true);
				ThirdPartyContentUtils.createXhrThirdPartyResources(request.getUrl(), response, browser, xhrActionId,
						getSession());
				
				loadPage(browser, continuation);
			}
		});
	}
	
	private boolean isWeatherAvailable() {
		return isWeatherAvailable(EasyTravelConfig.read().nodejsURL);
	}
	
	private boolean isWeatherAvailable(String url) {
		if (!PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.NODEJS_WEATHER_APPLICATION)) {
			return false;
		}

		if (url == null) {
			LOGGER.warning("Weather url is not set. Weather will not be loaded");
			return false; 
		}
		
		if (!UemLoadHttpUtils.isConnectable(url)) { 
			LOGGER.warning(TextUtils.merge("Weather host: {0} is not avaliable. Weather will not be loaded", url));
			return false;
		}
		
		return true;
	}

	/**
	 * @param browser
	 * @param continuation
	 * @param response
	 * @param journeyId
	 * @throws IOException
	 */
	void loadNodeJSWeather(final Browser browser, final UEMLoadCallback continuation) throws IOException {
		final String url = getSession().getAttribute("forecast-link");

		if (url != null) {
			LOGGER.finest("Weather forecast: forecast-link url is not null");

			final Context context = Metrics.getTimerContext(this, "forecast-link");
			try {
				browser.loadSubPage(url, Title.WEATHERFORECAST, getSession(), new UEMLoadCallback() {

					@Override
					public void run() throws IOException {
						String subPageHost = url.substring(0, url.indexOf('/', 8));
						Collection<String> subPageResourceUrls = getHtmlResourceParser().listResourceReferences(subPageHost, browser.getSubPageHtml().get(url));
						browser.loadResources(subPageResourceUrls, getSession(), url, false, new UEMLoadCallback() {

							@Override
							public void run() throws IOException {
								if (browser.getSubPageAgents().containsKey(url)) {
									browser.getSubPageAgents().get(url).subPageLoadFinished(
											browser.getSubPageResources().get(url),
											browser.getSubPageNavigationTimings().get(url),
											browser.getBrowserWindowSize(),
											false, url, browser.getViewDuration());
									cont(continuation);
								}
							}
						}, browser.getSubPageResources().get(url), url);
					}
				});
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, TextUtils.merge("Error loading weather url: {0}. Error: {1}", url, e.getMessage()));
			}
			context.stop();
			context.close();
		} 
	}

	/**
	 * @param response
	 * @param journeyId
	 * @throws IOException
	 */
	void checkInvalidWeatherLink(HttpResponse response, int journeyId)
			throws IOException {
		final String url = getSession().getAttribute("forecast-link");
		if (PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.NODEJS_WEATHER_APPLICATION) && url == null) {
			LOGGER.finest("Weather forecast: forecast-link URL is null for journey ID <" + journeyId + ">");
			// URL is null - this can happen at the very start of the application
			// and also if the journey destination is not found,
			// as we cannot get weather for a non-existent destination.
			if (LOGGER.isLoggable(Level.FINEST)) {
				String pgContent = response.getTextResponse();
				int tmpIdx = pgContent.indexOf("No journeys found");
				if (tmpIdx != -1) {

					// This presumably can happen: if the destination does not exist, we have no weather for it.
					LOGGER.finest("Weather forecast for an non-existent journey not found.");

				} else {

					//
					// This is more serious - the trip was found, but there is no weather for it.
					//

					LOGGER.finest("Weather forecast not found, even though the journey has been found.");
					// extract the destination
					tmpIdx = pgContent.indexOf("Trip Destination");

					if (tmpIdx != -1) {
						String partialPgContent = pgContent.substring(tmpIdx);
						// we need to extract the first value= after this point

						String[] split = partialPgContent.split("value=");
						String valueSubString = split[1].substring(0, 30); // 30 should be enough to catch the name of any destination.
						LOGGER.finest("Weather forecast for destination <" + valueSubString + "> not found!");
					} else {
						LOGGER.finest("Weather forecast: Trip Destination not found on page.");
					}

				}
			}
		}
	}


	private boolean isJourneyIdInvalid() {
		int journeyId = getSession().getJourneyId();
		return !Journeys.isValidJourneyId(journeyId);
	}

	int getRandomJourneyId(HttpResponse res) throws IOException {

		if (UemLoadHttpUtils.isSessionExpired(res)) {
			throw new SessionExpiredException(getSession().getHost());
		}

		String html = res.getTextResponse();

		int journeyId = Journeys.NO_JOURNEY_FOUND;
		Matcher matcher = JOURNEY_ID_PATTERN.matcher(res.getTextResponse());

		while (matcher.find()) {
			journeyId = Integer.parseInt(matcher.group(1));
			LOGGER.fine("Found journeyId: " + journeyId);
		}

		if (PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.NODEJS_WEATHER_APPLICATION)) {
			// parse weather forecast link (only available if NodeJSWeatherApplication problem pattern active)
			// similar to the journey Id we take the last index that we can find assuming that this will match
			// the journey id
			int indexOfWeatherForecastLink = html.indexOf("forecast-link");
			while (indexOfWeatherForecastLink > 0) {
				int startIndex = html.indexOf("href=\"", indexOfWeatherForecastLink) + "href=\"".length();
				int endIndex = html.indexOf('\"', startIndex + 1);
				String url = html.substring(startIndex, endIndex);

				getSession().setAttribute("forecast-link", url);
				indexOfWeatherForecastLink = html.indexOf("forecast-link", indexOfWeatherForecastLink + 1);
			}

		}

		if (!Journeys.isValidJourneyId(journeyId)) {
			LOGGER.info(format("No journey found for destination '%s'", getSession().getDestination()));
		}

		return journeyId;
	}

	private void waitUntilSessionIsCompletlyLoaded() {
		int waitTime = 0;

		while(getSession().getView() == null ||
				getSession().getViewState() == null  ||
				getSession().getWindow() == null) {
			if (waitTime > MAX_WAIT_TIME_MS) {
				throw new IllegalStateException(format("Waited %d ms but session view state is still not set: " +
						getSession().getView() + "/" + getSession().getViewState() + "/" + getSession().getWindow(),
						waitTime));
			}

			try {
				Thread.sleep(150);
				waitTime += 150;
			} catch (InterruptedException e) {
				LOGGER.info("Interruption while waiting for completion of the session data.");
				Thread.currentThread().interrupt();
			}
		}
	}
}
