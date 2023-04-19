/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: ThirdPartyContentSummary.java
 * @date: 11.01.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload.thirdpartycontent;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;

import com.dynatrace.diagnostics.uemload.NavigationTiming;
import com.dynatrace.diagnostics.uemload.URLUtil;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;

/**
 * Utility class to collect information of loaded resources for a page. The
 * information is used to generate the monitoring parameter for the javascript
 * agent third party content summary detection.
 *
 * @author peter.lang
 */
public class ThirdPartyContentSummary {

	private static final Logger log = Logger.getLogger(ThirdPartyContentSummary.class.getName());

	private String pageUrl;
	private long pageloadStart;
	private long pageloadFinished;
	private List<ResourceRequestSummary> loadedResources = Collections.emptyList();

	private int xhrActionId = -1;

	// private static final int BITVALUE_RESOURCE_TIMING = 1;
	private static final int BITVALUE_IMAGES = 2;
	private static final int BITVALUE_SCRIPTS = 4;
	private static final int BITVALUE_STYLESHEETS = 8;
	private static final int BITVALUE_OTHER = 16;
	private static final int BITVALUE_CUSTOM = 32;

	/**
	 * @return the pageUrl
	 */
	public String getPageUrl() {
		return pageUrl;
	}

	/**
	 * @param pageUrl
	 *            the pageUrl to set
	 */
	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}

	/**
	 * @return the pageloadStart
	 */
	public long getPageloadStart() {
		return pageloadStart;
	}

	/**
	 * @param pageloadStart
	 *            the pageloadStart to set
	 */
	public void setPageloadStart(long pageloadStart) {
		this.pageloadStart = pageloadStart;
	}

	/**
	 * @return the pageloadFinished
	 */
	public long getPageloadFinished() {
		return pageloadFinished;
	}

	/**
	 * @param pageloadFinished
	 *            the pageloadFinished to set
	 */
	public void setPageloadFinished(long pageloadFinished) {
		this.pageloadFinished = pageloadFinished;
	}

	public int getXhrActionId() {
		return xhrActionId;
	}

	public void setXhrActionId(int xhrActionId) {
		this.xhrActionId = xhrActionId;
	}

	/**
	 * @return the loadedResources
	 */
	public List<ResourceRequestSummary> getLoadedResources() {
		return loadedResources;
	}

	/**
	 * @param loadedResources
	 *            the loadedResources to set
	 */
	public void setLoadedResources(List<ResourceRequestSummary> loadedResources) {
		this.loadedResources = loadedResources;
	}

	/**
	 *
	 * @return
	 * @author peter.lang
	 */
	public String getSummaryDetails() {
		if (loadedResources.isEmpty()) {
			return "no resources loaded";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("num.resources=").append(loadedResources.size());
		sb.append(", url=").append(pageUrl);
		sb.append(", pageloadstart=").append(pageloadStart);
		sb.append(", pageloadfinished=").append(pageloadFinished);

		return sb.toString();
	}

	/**
	 *
	 * @return
	 * @author peter.lang
	 */
	public List<ResourceRequestSummary> getLoadedThirdPartyResources() {
		if (loadedResources.isEmpty())
			return loadedResources;
		List<ResourceRequestSummary> result = new ArrayList<ResourceRequestSummary>();

		for (ResourceRequestSummary resource : loadedResources) {
			if (resource.isThirdPartyResource(pageUrl)) {
				result.add(resource);
			}
		}
		return result;
	}

	private List<ThirdPartyHostSummary> buildThirdPartySummary(boolean includeOwnResources) {
		// key==hostname, value==summary for third party host
		Map<String, ThirdPartyHostSummary> mapThirdPartyHosts = new HashMap<String, ThirdPartyHostSummary>();
		List<ResourceRequestSummary> listofThirdPartyResources = loadedResources;
		if (!includeOwnResources) {
			listofThirdPartyResources = getLoadedThirdPartyResources();
		}

		for (ResourceRequestSummary resource : listofThirdPartyResources) {
			String hostname = resource.getHostname();
			ThirdPartyHostSummary summary;
			if (mapThirdPartyHosts.containsKey(hostname)) {
				summary = mapThirdPartyHosts.get(hostname);
			} else {
				summary = new ThirdPartyHostSummary();
				summary.setHostname(hostname);
				mapThirdPartyHosts.put(hostname, summary);
			}
			addStatisticsForResource(summary, resource);
		}

		if (mapThirdPartyHosts.isEmpty()) {
			return Collections.emptyList();
		}

		return new ArrayList<ThirdPartyHostSummary>(mapThirdPartyHosts.values());
	}

	private void addStatisticsForResource(ThirdPartyHostSummary summary, ResourceRequestSummary resource) {
		String mimeType = resource.getMimeType();
		if (resource.isLoadedFromCache()) {
			mimeType = URLUtil.guessMimeType(resource.getResourceUrl());
		}

		if (mimeType == null) {
			summary.addCustomSample(resource.getLoadstart() - pageloadStart,
					resource.getLoadfinished() - pageloadStart);
		} else if (mimeType.contains("javascript")) {
			summary.addScriptSample(resource.getLoadstart() - pageloadStart,
					resource.getLoadfinished() - pageloadStart);
		} else if (mimeType.contains("image")) {
			summary.addImageSample(resource.getStatusCode(), resource.getLoadstart() - pageloadStart,
					resource.getLoadfinished() - pageloadStart);
		} else if (mimeType.contains("css")) {
			summary.addCssSample(resource.getLoadstart() - pageloadStart, resource.getLoadfinished() - pageloadStart);
		} else {
			summary.addOtherSample(resource.getLoadstart() - pageloadStart, resource.getLoadfinished() - pageloadStart);
		}
	}

	/**
	 * creates the parameter to report third party content summary to dynaTrace
	 * server.
	 *
	 * NOTE: Currently only summary information is sent. Details about loaded
	 * resources are not taken into account. Primarily to avoid the necessity of
	 * splitting the javascript xhr-signal due to length constaints of this
	 * request.
	 *
	 * @return signal for formatted for the javascript agent as expected by
	 *         dynaTrace server. null returned if no third party content
	 *         detected
	 *
	 * @author peter.lang
	 */
	public String createJavaAgentSignal(boolean includeOwnResources) {
		List<ThirdPartyHostSummary> summaryDetails = buildThirdPartySummary(includeOwnResources);
		if (summaryDetails.isEmpty()) {
			return null;
		}
		List<ThirdPartyHostSummary> sortedDetails = sortAccordingToFirstStartOffset(summaryDetails);

		return createSignalString(sortedDetails);
	}

	public String createResourceTimingsSignal(String url, NavigationTiming navigationTiming) {
		return createResourceTimingsSignal(url, navigationTiming, false, -1);
	}

	public String createResourceTimingsSignal(String url, NavigationTiming navigationTiming, boolean isRuxitSynthetic, int xhrId) {
		List<ResourceRequestSummary> resources = getLoadedResources();
		StringBuilder sb = new StringBuilder();
		if (xhrId >= 0) {
			sb.append(xhrId);
		}
		sb.append("-");
		sb.append(navigationTiming.getNavigationStartTime());
		sb.append(";");

		for (ResourceRequestSummary resource : resources) {
			sb.append("|");
			sb.append(resource.getResourceUrl());
			sb.append("|");
			sb.append(buildResourceTimingValues(resource, url, navigationTiming, isRuxitSynthetic));
		}
		return sb.toString();
	}
	
	public String buildResourceTimingValueForAction(NavigationTiming navigationTiming) {
		StringBuilder sb = new StringBuilder();
		sb.append("b");
		sb.append(navigationTiming.getNavigationStartTime());
		sb.append("e0");
		sb.append("m");
		sb.append(navigationTiming.getLoadEventEnd() - navigationTiming.getNavigationStartTime());
		sb.append("z");
		sb.append("11");
		return sb.toString();
	}

	private String buildResourceTimingValues(ResourceRequestSummary resource, String url, NavigationTiming navigationTiming, boolean isRuxitSynthetic) {
		StringBuilder sb = new StringBuilder();
		
		int resourceLoadStart = (int) (resource.getLoadstart() - navigationTiming.getNavigationStartTime());
		ResourceTimeSummary resourceTime = new ResourceTimeSummary(resource.getLoadDuration(), url, resource.getResourceUrl(), resourceLoadStart);
		resourceTime.setSpecialDomainValules();
		resourceTime.setLocalDomainValues();
		
		sb.append("b");
		sb.append(resourceTime.startTime);
		sb.append("e0");
		if (resourceTime.domainLookupEnd != -1 && resourceTime.connectStart != -1 && resourceTime.connectEnd != -1) {
			sb.append("f0"); //domainLookupStart
			sb.append("g");
			sb.append(resourceTime.domainLookupEnd);
			sb.append("h");
			sb.append(resourceTime.connectStart);
			sb.append("i");
			sb.append(resourceTime.connectEnd);
			sb.append("k");
			sb.append(resourceTime.connectEnd);
			sb.append("l");
			sb.append(resourceTime.getResponseStart()); // between requestStart and responseEnd
			sb.append("u");
			sb.append(resource.isLoadedFromCache()? 0 : (resource.getResponseHeadersSize() + resource.getResponseSize()));
			sb.append("v");
			sb.append(resource.getResponseSize());
			sb.append("w");
			if (resource.isImage() || resource.getResponseSize() <= 300) {
				sb.append(resource.getResponseSize());
			} else {
				double compressionFactor = 1.0 + ((resource.getResourceUrl().length() % 50.0)) / 100; 
				long decodedResponseSize = Math.round(resource.getResponseSize() * compressionFactor);
				sb.append(decodedResponseSize);
			}
			
			if (isRuxitSynthetic) {
				sb.append("x");
				sb.append(resource.getStatusCode());
			}
		} else {
			if (PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.FAILED_IMAGES_DETECTION) && isResourceImage(resource.getResourceUrl())) {
				sb.append("A1"); // failed resource
			}
		}
		
		sb.append("m");
		sb.append(resourceTime.responseEnd);

		return sb.toString();
	}
	
	private boolean isResourceImage(String resourceUrl) {
		return resourceUrl.endsWith(".jpeg") 
				|| resourceUrl.endsWith(".jpg")
				|| resourceUrl.endsWith(".png")
				|| resourceUrl.endsWith(".ico")
				|| resourceUrl.endsWith(".gif");
	}

	private static class ResourceTimeSummary {

		public int domainLookupEnd = -1;
		public int connectStart = -1;
		public int connectEnd = -1;
		public long responseEnd;
		public final int startTime;
		private URL hostURL = null; 
		private URL resourceURL = null;
		
		public ResourceTimeSummary(long responseEnd, String hostUrl, String resourceUrl, int resourceLoadStart) {
			this.responseEnd = responseEnd;
			this.startTime = resourceLoadStart;
			try {
				hostURL = new URL(hostUrl);
				resourceURL = new URL(resourceUrl);
				
			} catch (MalformedURLException e) {
				if (log.isLoggable(Level.FINER)) {
					log.finer(e.getMessage());
				}
			}
		}
		
		public void setSpecialDomainValules() {
			if (resourceURL != null && (resourceURL.getHost().toLowerCase().contains("facebook")
					|| resourceURL.getHost().toLowerCase().contains("pinterest"))) {
				domainLookupEnd = (int) (responseEnd * 0.05);
				connectStart = (int) (domainLookupEnd * 1.15);
				connectEnd = (int) (connectStart * 1.05);
				responseEnd += connectEnd;
			}
		}
		
		public void setLocalDomainValues() {
			if (hostURL != null && resourceURL != null &&
					hostURL.getHost().equals(resourceURL.getHost()) && hostURL.getPort() == resourceURL.getPort()) { // local domain
				domainLookupEnd = 0;
				connectStart = 0;
				connectEnd = 0;
			}
		}
		
		public int getResponseStart() {
			int responseStart = (int)((responseEnd - connectEnd) * 0.2 + connectEnd); // between connectEnd and responseEnd
			return responseStart <= 0 ? 0 : responseStart;
		}
		
	}

	private String createSignalString(List<ThirdPartyHostSummary> sortedDetails) {
		StringBuilder thirdPartySignal = new StringBuilder(1000);
		boolean first = true;

		if (this.xhrActionId >= 0) {// append xhrActionId if available for XHR Signal
			thirdPartySignal.append(xhrActionId);
			thirdPartySignal.append('-');
			thirdPartySignal.append(this.pageloadStart);
			thirdPartySignal.append(';');
		}

		for (ThirdPartyHostSummary tpn : sortedDetails) {
			if (first) {
				first = false;
			} else {
				thirdPartySignal.append(BaseConstants.SCOLON);
			}

			if (tpn.getHostname() == null)
				throw new IllegalArgumentException("Domain must not be null!");

			StatCounter scriptLoadStatistics = tpn.getScriptLoadStatistics();
			StatCounter customLoadStatistics = tpn.getCustomLoadStatistics();
			StatCounter cssLoadStatistics = tpn.getCssLoadStatistics();
			StatCounter otherLoadStatistics = tpn.getOtherLoadStatistics();

			int featureNumber = 0;
			String imageSignalPart = null;
			String scriptSignalPart = null;
			String cssSignalPart = null;
			String otherSignalPart = null;
			String customSignalPart = null;

			if (tpn.getImagesSuccessul() + tpn.getImagesFailed() + tpn.getImagesInterrupted() > 0) {
				featureNumber += BITVALUE_IMAGES;
				imageSignalPart = Joiner.on(BaseConstants.PIPE).join(tpn.getImagesSuccessul(), tpn.getImagesFailed(),
						tpn.getImagesInterrupted(), 0,
						tpn.getImagesSuccessul() + tpn.getImagesFailed() + tpn.getImagesInterrupted(), 0,
						appendIntervalSet(tpn.getImageDuration()), appendLoadStatistics(tpn.getImageLoadStatistics()),
						BaseConstants.EMPTY_STRING, // W3C Resource Timing
						0, 0, 0);
			}
			if (scriptLoadStatistics.getCount() > 0) {
				featureNumber += BITVALUE_SCRIPTS;
				scriptSignalPart = Joiner.on(BaseConstants.PIPE).join(scriptLoadStatistics.getCount(), 0, 0, 0,
						scriptLoadStatistics.getCount(), 0, appendIntervalSet(tpn.getScriptDuration()),
						appendLoadStatistics(scriptLoadStatistics), BaseConstants.EMPTY_STRING, // W3C
																								// Resource
																								// Timing
						0, 0, 0);
			}
			if (cssLoadStatistics.getCount() > 0) {// TODO
				featureNumber += BITVALUE_STYLESHEETS;
				cssSignalPart = Joiner.on(BaseConstants.PIPE).join(cssLoadStatistics.getCount(), 0, 0, 0,
						appendIntervalSet(tpn.getCssDuration()), appendLoadStatistics(cssLoadStatistics));
			}
			if (otherLoadStatistics.getCount() > 0) {// TODO
				featureNumber += BITVALUE_OTHER;
				otherSignalPart = Joiner.on(BaseConstants.PIPE).join(otherLoadStatistics.getCount(), 0, 0, 0,
						appendIntervalSet(tpn.getOtherDuration()), appendLoadStatistics(otherLoadStatistics));
			}
			if (customLoadStatistics.getCount() > 0) {
				featureNumber += BITVALUE_CUSTOM;
				customSignalPart = Joiner.on(BaseConstants.PIPE).join(customLoadStatistics.getCount(),
						appendIntervalSet(tpn.getCustomDuration()), appendLoadStatistics(customLoadStatistics));
			}

			// see thirdparty.js for order of values
			thirdPartySignal.append(Joiner.on(BaseConstants.PIPE).skipNulls().join(tpn.getHostname(),
					Integer.toString(featureNumber, 32), imageSignalPart, scriptSignalPart, cssSignalPart,
					otherSignalPart, customSignalPart));

		}

		String thirdPartySignalStr = thirdPartySignal.toString();
		if (log.isLoggable(Level.FINER)) {
			log.finer(String.format("Third party JavaScript signal: %s", thirdPartySignalStr));
		}
		return thirdPartySignalStr;
	}

	/**
	 *
	 * @param summaryDetails
	 * @author stefan.moschinski
	 * @return
	 */
	private List<ThirdPartyHostSummary> sortAccordingToFirstStartOffset(List<ThirdPartyHostSummary> summaryDetails) {
		Collections.sort(summaryDetails, new Comparator<ThirdPartyHostSummary>() {

			@Override
			public int compare(ThirdPartyHostSummary arg0, ThirdPartyHostSummary arg1) {
				long thisVal = arg0.getFirstStartOffset();
				long anotherVal = arg1.getFirstStartOffset();

				return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
			}
		});
		return summaryDetails;
	}

	/**
	 *
	 * @param res
	 * @param loadStatisticsCounter
	 * @author peter.lang
	 */
	private String appendLoadStatistics(StatCounter loadStatisticsCounter) {
		return loadStatisticsCounter.getCount() > 0 ? getAsString(BaseConstants.PIPE, loadStatisticsCounter.getAvg(),
				loadStatisticsCounter.getMin(), loadStatisticsCounter.getMax()) : "0|0|0";
	}

	private String appendIntervalSet(IntervalSet is) {
		if (is == null || is.getAsArray() == null) {
			return BaseConstants.EMPTY_STRING;
		}
		return getAsString(BaseConstants.UNDERSCORE, is.getAsArray());
	}

	private String getAsString(String separator, double... arr) {
		return Joiner.on(separator).join(
				FluentIterable.from(Arrays.asList(ArrayUtils.toObject(arr))).transform(new Function<Double, Long>() {

					@Override
					public Long apply(Double input) {
						return input.longValue();
					}
				}).toList());
	}

}
