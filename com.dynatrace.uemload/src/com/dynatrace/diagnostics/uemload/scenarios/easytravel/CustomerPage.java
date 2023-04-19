package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import org.apache.http.message.BasicNameValuePair;

import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.JavaScriptAgent;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.UEMOnLoadCallback;
import com.dynatrace.diagnostics.uemload.dtheader.HeaderEntry;
import com.dynatrace.diagnostics.uemload.http.base.HttpRequest;
import com.dynatrace.diagnostics.uemload.http.base.UemLoadFormBuilder;
import com.dynatrace.diagnostics.uemload.http.callback.HttpResponseCallback;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType.PageAction;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUrlUtils;


public abstract class CustomerPage extends EasyTravelPage {
	private static final boolean DO_LOAD_DYNATRACE_RESOURCES = true;
    private String destination;
    private String pageLoadReferer;

    private final CustomerSession session;

	public CustomerPage(EtPageType page, CustomerSession session) {
		super(page, session, DO_LOAD_DYNATRACE_RESOURCES);
		this.session = session;
	}
	
	public CustomerPage(EtPageType page, CustomerSession session, String referer) {
		super(page, session, DO_LOAD_DYNATRACE_RESOURCES);
		this.session = session;
		this.pageLoadReferer = referer;
	}

	@Override
	public CustomerSession getSession() {
		return this.session;
	}

	@Override
	public void runInBrowser(Browser browser, UEMLoadCallback continuation) throws Exception {
		cont(continuation);
	}

	protected void loadPage(Browser browser, UEMLoadCallback pageLoadCallback) throws IOException {
		loadPage(browser, session.getJourneyId(), pageLoadCallback);
	}

	protected void loadPage(Browser browser, UEMLoadCallback pageLoadCallback, UEMOnLoadCallback pageOnLoadCallback) throws IOException {
		super.loadPage(browser, UemLoadUrlUtils.getUrlForJourney(session.getHost(), getPage().getPath(), session.getJourneyId()), null,
				pageLoadReferer, pageLoadCallback, pageOnLoadCallback, false);
	}

	protected void loadPage(Browser browser, int journeyId, UEMLoadCallback pageLoadCallback) throws IOException {
		loadPage(browser, journeyId, null, pageLoadCallback);
	}

	protected void loadPage(final Browser browser, int journeyId, final PageAction pageAction, final UEMLoadCallback pageLoadCallback) throws IOException{
		super.loadPage(browser, UemLoadUrlUtils.getUrlForJourney(session.getHost(), getPage(), journeyId), pageAction, pageLoadReferer,
				pageLoadCallback);
	}

	protected void loadPage(final Browser browser, int journeyId, final PageAction pageAction, final UEMLoadCallback pageLoadCallback, BasicNameValuePair additionalParameter) throws IOException{
		super.loadPage(browser, UemLoadUrlUtils.getUrlForJourney(session.getHost(), getPage().getPath(), journeyId, additionalParameter), pageAction,
				pageLoadReferer, pageLoadCallback);
	}

	protected void loadResources(Browser browser, String html, UEMLoadCallback loadResourcesCallback) throws IOException {
		super.loadResources(browser, html, session.getHost(),
				UemLoadUrlUtils.getUrlForJourney(session.getHost(), getPage(), session.getJourneyId()), loadResourcesCallback);
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getPageLoadReferer() {
		return pageLoadReferer;
	}

	public void setPageLoadReferer(String pageLoadReferer) {
		this.pageLoadReferer = pageLoadReferer;
	}

	protected void startCustomAction(String actionTitle, String actionType, String actionInfo, Browser browser) {
		if (JavaScriptAgent.shouldSendActionWithActionInfo()) {
			browser.startCustomAction(actionTitle, actionType, actionInfo);
		}
		else {
			browser.startCustomAction("click on \"" + actionTitle + "\"", "icefaces.ajax", "-");
		}
	}

	protected void startCustomAction(String actionTitle, String actionType, String actionInfo, Browser browser, String xhrUrl) {
		if (JavaScriptAgent.shouldSendActionWithActionInfo()) {
			browser.startCustomAction(actionTitle, actionType, actionInfo, xhrUrl);
		}
		else {
			browser.startCustomAction("click on \"" + actionTitle + "\"", "icefaces.ajax", "-", xhrUrl);
		}
	}

	protected void startCustomAction(String actionTitle, String actionType, String actionInfo, Browser browser, String xhrUrl, int hierarchy) {
		if (JavaScriptAgent.shouldSendActionWithActionInfo()) {
			browser.startCustomAction(actionTitle, actionType, actionInfo, xhrUrl, hierarchy);
		}
		else {
			browser.startCustomAction("click on \"" + actionTitle + "\"", "icefaces.ajax", "-", xhrUrl, hierarchy);
		}
	}

	protected HttpRequest getXhr(Browser browser) {
		return getXhr(browser, UemLoadUrlUtils.getUrlForJourney(session.getHost(), getPage(), session.getJourneyId()));
	}

	protected String getXhrUrl() {
		return UemLoadUrlUtils.getUrlForJourney(session.getHost(), getPage(), session.getJourneyId());
	}

	protected HttpRequest getXhr(Browser browser, HeaderEntry headerEntry) {
		return super.getPostRequest(browser, getXhrUrl(), headerEntry);
	}

	protected String getXhrUrl(EtPageType page) {
		return UemLoadUrlUtils.getUrlForJourney(session.getHost(), page, session.getJourneyId());
	}

	protected HttpRequest getXhr(Browser browser, EtPageType page) {
		return getXhr(browser, getXhrUrl(page));
	}

	private HttpRequest getXhr(Browser browser, String url) {
		return super.getXhr(browser, url, getPage());
	}

	protected void sendForm(final Browser browser, UemLoadFormBuilder form, HttpRequest request, boolean loadIframe, final HttpResponseCallback callback) throws UnsupportedEncodingException,
		IOException {
		super.sendForm(browser, form, UemLoadUrlUtils.getUrlForJourney(session.getHost(), getPage(), session.getJourneyId()), request, loadIframe, callback);
	}

	protected void sendForm(final Browser browser, UemLoadFormBuilder form, HttpRequest request, final HttpResponseCallback callback) throws UnsupportedEncodingException,
			IOException {
		super.sendForm(browser, form, UemLoadUrlUtils.getUrlForJourney(session.getHost(), getPage(), session.getJourneyId()), request,
				callback);
	}

	protected void sendForm(final Browser browser, UemLoadFormBuilder form, EtPageType page, HttpRequest request, final HttpResponseCallback callback) throws UnsupportedEncodingException,
	IOException {
		super.sendForm(browser, form, UemLoadUrlUtils.getUrlForJourney(session.getHost(), page, session.getJourneyId()), request,
				callback);
	}

	protected UemLoadFormBuilder getForm(boolean isIceform) {
		return new CustomerFormBuilder(session, false);
	}

	/**
	 * stops a custom action, returns the custom action id
	 * @param browser
	 * @param processingTime
	 * @param isIncomplete
	 * @return
	 * @throws IOException
	 */
	protected int finishCustomAction(Browser browser, int processingTime, boolean isIncomplete) throws IOException {
		return browser.stopCustomAction(isIncomplete);
	}

	protected int finishCustomAction(Browser browser, int processingTime, boolean isIncomplete, boolean xhrInOnLoad) throws IOException {
		return browser.stopCustomAction(isIncomplete, xhrInOnLoad);
	}

	/**
	 * stops a custom action, returns the custom action id
	 * @param browser
	 * @param processingTime
	 * @param isIncomplete
	 * @param additionalActions
	 * @return
	 * @throws IOException
	 */
	protected int finishCustomAction(Browser browser, int processingTime, boolean isIncomplete, Collection<String> additionalActions) throws IOException {
		return browser.stopCustomAction(isIncomplete, additionalActions);
	}
}
