/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: NavigationTiming.java
 * @date: 21.03.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload;

import java.util.Random;

import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.constants.BaseConstants;

/**
 * This class holds the timestamps measured as described in the W3C Navigation Timing API.
 * like production EuNavigationTimingNodeAttachment
 *
 * Represents measurement via the W3C Navigation Timing API
 * See https://dvcs.w3.org/hg/webperf/raw-file/tip/specs/NavigationTiming/Overview.html
 *
 * @author peter.lang
 */
@SuppressWarnings("unused")
public class NavigationTiming {

	public static final NullNavigationTiming NONE = new NullNavigationTiming();

	public static enum NavigationType {
		NAVIGATE,
		/*
		 * RELOAD,
		 * BACK_FORWARD,
		 */
		OTHER;

		private static final NavigationType[] VALUES = values();

		public static NavigationType fromOrd(int i) {
			if (i < 0 || i >= VALUES.length) {
				throw new IndexOutOfBoundsException("Invalid ordinal");
			}
			return VALUES[i];
		}

	}

	private NavigationType navigationType = NavigationType.OTHER;  //a
	private long navigationStartTime = -1;  	                   //b
	// the following values are offsets relative to navigation start

	private long redirectStart = -1;
    private long redirectEnd = -1;
	private long domainLookupStart = -1;                            //f
	private long domainLookupEnd = -1;                              //g
	private long fetchStart = -1;
	private long connectStart = -1;                                 //h
	private long secureConnectionStart = -1;
	private long connectEnd = -1;                                   //i
	private long requestStart = -1;                                 //k
	private long responseStart = -1;                                //l
	private long responseEnd = -1;                                  //m
	private long domLoading = -1;                                   //n
	private long domInteractive = -1;
	private long domComplete = -1;
	private long domContentLoadedEventStart = -1;
	private long domContentLoadedEventEnd = -1;
	private long loadEventStart = -1;
	private long loadEventEnd = -1;                                 //t


	private int dnsSlowdownFactor = 1;

	private NavigationTiming() {
	}

	/**
	 * Creates a new {@link NavigationTiming} instance with {@link NavigationType#NAVIGATE} and starts the navigation timing.
	 *
	 * @return a new {@link NavigationTiming} instance
	 * @author stefan.moschinski
	 */
	public static NavigationTiming start() {
		NavigationTiming nt = new NavigationTiming();
		nt.setNavigationType(NavigationType.NAVIGATE);
		nt.setNavigationStartTime(System.currentTimeMillis());
		return nt;
	}

	public void createConnectionEstablishedData(final Bandwidth bandwidth) {

		if (fetchStart != -1) {

			return;

		}

		UemLoadUtils.waitRandomFetchStartDuration(50);
		setFetchStart(System.currentTimeMillis());
		UemLoadUtils.waitRandomDuration(10);
		setDomainLookupStart(System.currentTimeMillis());
		UemLoadUtils.waitRandomDnsDuration(bandwidth, 50, dnsSlowdownFactor);
		long ts = System.currentTimeMillis();
		setDomainLookupEnd(ts);
		setConnectStart(ts);

		UemLoadUtils.waitRandomConnectionDuration(bandwidth, 100);
		ts = System.currentTimeMillis();
		setConnectEnd(ts);
		setRequestStart(ts);
	}

	public void createNavigationTimingDataForResponseEnd(final Bandwidth bandwidth) {
		if (responseEnd != -1) {
			return;
		}
		setResponseEnd(System.currentTimeMillis());

		long retrieveDuration = getResponseEnd() - getRequestStart();
		long requestDuration = UemLoadUtils.getRequestDuration(bandwidth, retrieveDuration);

		setResponseStart(getRequestStart() + requestDuration);

		long domLoading = Math.min(getResponseStart() + UemLoadUtils.randomInt(1, 10), getResponseEnd());
		setDomLoading(domLoading); // usually response start + 1-3 ms
	}

	public void createNavigationTimingDataForOnload() {
		if (domComplete != -1) {
			return;
		}
		long loadStart = System.currentTimeMillis();
		setDomComplete(loadStart);
		setLoadEventStart(loadStart);
		setLoadEventEnd(loadStart + UemLoadUtils.getRandomOnloadDuration(100));

		long processingDuration = getDomComplete() - getDomLoading();

		long domContentLoadedEventStart = UemLoadUtils.getDomContentLoadedStart(getDomLoading(), getDomComplete());
		long domContentLoadedEventEnd = UemLoadUtils.getDomContentLoadedEnd(getDomLoading(), getDomComplete(), domContentLoadedEventStart);
		setDomContentLoadedEventStart(domContentLoadedEventStart);
		setDomContentLoadedEventEnd(domContentLoadedEventEnd);
		setDomInteractive(getDomContentLoadedEventStart());
	}

	public NavigationType getNavigationType() {
		return navigationType;
	}

	public void setNavigationType(NavigationType navigationType) {
		this.navigationType = navigationType;
	}

	public long getNavigationStartTime() {
		return navigationStartTime;
	}

	public void setNavigationStartTime(long navigationStartTime) {
		this.navigationStartTime = navigationStartTime;
	}

	public long getFetchStart() {
		return fetchStart;
	}

	public void setFetchStart(long fetchStart) {
		this.fetchStart = fetchStart;
	}

	public long getDomainLookupStart() {
		return domainLookupStart;
	}

	public void setDomainLookupStart(long domainLookupStart) {
//			LOGGER.info("setDomainLookupStart=" + (domainLookupStart- navigationStartTime));
		this.domainLookupStart = domainLookupStart;
	}

	public long getDomainLookupEnd() {
		return domainLookupEnd;
	}

	public void setDomainLookupEnd(long domainLookupEnd) {
//			LOGGER.info("setDomainLookupEnd=" + (domainLookupEnd - navigationStartTime));
		this.domainLookupEnd = domainLookupEnd;
	}

	public long getConnectStart() {
		return connectStart;
	}

	public void setConnectStart(long connectStart) {
//			LOGGER.info("setConnectStart=" + (connectStart - navigationStartTime));
		this.connectStart = connectStart;
	}

	public long getConnectEnd() {
		return connectEnd;
	}

	public void setConnectEnd(long connectEnd) {
//			LOGGER.info("setConnectEnd=" + (connectEnd - navigationStartTime));
		this.connectEnd = connectEnd;
	}

//		public int getSecureConnectionStart() {
//			return secureConnectionStart;
//		}
//
//		public void setSecureConnectionStart(int secureConnectionStart) {
//			this.secureConnectionStart = secureConnectionStart;
//		}
//
	public long getRequestStart() {
		return requestStart;
	}

	public void setRequestStart(long requestStart) {
		this.requestStart = requestStart;
	}

	public long getResponseStart() {
		return responseStart;
	}

	public void setResponseStart(long responseStart) {
		this.responseStart = responseStart;
	}

	public long getResponseEnd() {
		return responseEnd;
	}

	public void setResponseEnd(long responseEnd) {
		this.responseEnd = responseEnd;
	}

	public long getDomLoading() {
		return domLoading;
	}

	public void setDomLoading(long domLoading) {
		this.domLoading = domLoading;
	}

	public long getDomInteractive() {
		return domInteractive;
	}

	public void setDomInteractive(long domInteractive) {
		this.domInteractive = domInteractive;
	}

	public long getDomContentLoadedEventStart() {
		return domContentLoadedEventStart;
	}

	public void setDomContentLoadedEventStart(long domContentLoadedEventStart) {
		this.domContentLoadedEventStart = domContentLoadedEventStart;
	}

	public long getDomContentLoadedEventEnd() {
		return domContentLoadedEventEnd;
	}

	public void setDomContentLoadedEventEnd(long domContentLoadedEventEnd) {
		this.domContentLoadedEventEnd = domContentLoadedEventEnd;
	}

	public long getDomComplete() {
		return domComplete;
	}

	public void setDomComplete(long domComplete) {
		this.domComplete = domComplete;
	}

	public long getLoadEventStart() {
		return loadEventStart;
	}

	public void setLoadEventStart(long loadEventStart) {
		this.loadEventStart = loadEventStart;
	}

	public long getLoadEventEnd() {
		return loadEventEnd;
	}

	public void setLoadEventEnd(long loadEventEnd) {
		this.loadEventEnd = loadEventEnd;
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("NavigationTiming[");
		sb.append("\n  navigationStartTime = " + navigationStartTime);
		if (domainLookupStart >= 0)
			sb.append("\n  domainLookupStart = " + domainLookupStart);
		if (domainLookupEnd >= 0)
			sb.append("\n  domainLookupEnd = " + domainLookupEnd);
		if (connectStart >= 0)
			sb.append("\n  connectStart = " + connectStart);
		if (connectEnd >= 0)
			sb.append("\n  connectEnd = " + connectEnd);
		if (requestStart >= 0)
			sb.append("\n  requestStart = " + requestStart);
		if (responseStart >= 0)
			sb.append("\n  responseStart = " + responseStart);
		if (responseEnd >= 0)
			sb.append("\n  responseEnd = " + responseEnd);
		if (loadEventEnd >= 0)
			sb.append("\n  loadEventEnd = " + loadEventEnd);
		sb.append("\n]");
		return sb.toString();
	}

	public String createSignal() {
		StringBuilder sb = new StringBuilder();
		sb.append("a");
		sb.append(navigationType.ordinal());
		if (navigationStartTime > 0) {
			sb.append("b");
			sb.append(navigationStartTime);
			// c
			if (redirectStart > 0) {
				sb.append("c").append(redirectStart - navigationStartTime);
			}
			// d
			if (redirectEnd > 0) {
				sb.append("d").append(redirectEnd - navigationStartTime);
			}
			// e
			if (fetchStart > 0) {
				sb.append("e").append(fetchStart - navigationStartTime);
			}
			// f
			if (domainLookupStart > 0) {
				sb.append("f").append(domainLookupStart - navigationStartTime);
			}
			// g
			if (domainLookupEnd > 0) {
				sb.append("g").append(domainLookupEnd - navigationStartTime);
			}
			// h
			if (connectStart > 0) {
				sb.append("h").append(connectStart - navigationStartTime);
			}
			// i
			if (connectEnd > 0) {
				sb.append("i").append(connectEnd - navigationStartTime);
			}
			// k
			if (requestStart > 0) {
				sb.append("k").append(requestStart - navigationStartTime);
			}
			// l
			if (responseStart > 0) {
				sb.append("l").append(responseStart - navigationStartTime);
			}
			// m
			if (responseEnd > 0) {
				sb.append("m").append(responseEnd - navigationStartTime);
			}
			// n
			if (domLoading > 0) {
				sb.append("n").append(domLoading - navigationStartTime);
			}
			if (domInteractive > 0) {
				sb.append("o").append(domInteractive - navigationStartTime);
			}
			if (domContentLoadedEventStart > 0) {
				sb.append("p").append(domContentLoadedEventStart - navigationStartTime);
			}
			if (domContentLoadedEventEnd > 0) {
				sb.append("q").append(domContentLoadedEventEnd - navigationStartTime);
			}
			if (domComplete > 0) {
				sb.append("r").append(domComplete - navigationStartTime);
			}
			if (loadEventStart > 0) {
				sb.append("s").append(loadEventStart - navigationStartTime);
			}
			if (loadEventEnd > 0) {
				sb.append("t").append(loadEventEnd - navigationStartTime);
			}
		}
		return sb.toString();
	}


	public static class NullNavigationTiming extends NavigationTiming {

		@Override
		public void createConnectionEstablishedData(Bandwidth bandwidth) {
		}

		@Override
		public void createNavigationTimingDataForResponseEnd(Bandwidth bandwidth) {
		}

		@Override
		public NavigationType getNavigationType() {
			return super.getNavigationType();
		}

		@Override
		public void setNavigationType(NavigationType navigationType) {
		}

		@Override
		public long getNavigationStartTime() {
			return 0;
		}

		@Override
		public void setNavigationStartTime(long navigationStartTime) {
		}

		@Override
		public long getDomainLookupStart() {
			return 0;
		}

		@Override
		public void setDomainLookupStart(long domainLookupStart) {
		}

		@Override
		public long getDomainLookupEnd() {
			return 0;
		}

		@Override
		public void setDomainLookupEnd(long domainLookupEnd) {
		}

		@Override
		public long getConnectStart() {
			return 0;
		}

		@Override
		public void setConnectStart(long connectStart) {
		}

		@Override
		public long getConnectEnd() {
			return 0;
		}

		@Override
		public void setConnectEnd(long connectEnd) {
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.dynatrace.diagnostics.uemload.NavigationTiming#getRequestStart()
		 */
		@Override
		public long getRequestStart() {
			return 0;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.dynatrace.diagnostics.uemload.NavigationTiming#setRequestStart(long)
		 */
		@Override
		public void setRequestStart(long requestStart) {
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.dynatrace.diagnostics.uemload.NavigationTiming#getResponseStart()
		 */
		@Override
		public long getResponseStart() {
			return 0;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.dynatrace.diagnostics.uemload.NavigationTiming#setResponseStart(long)
		 */
		@Override
		public void setResponseStart(long responseStart) {
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.dynatrace.diagnostics.uemload.NavigationTiming#getResponseEnd()
		 */
		@Override
		public long getResponseEnd() {
			return 0;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.dynatrace.diagnostics.uemload.NavigationTiming#setResponseEnd(long)
		 */
		@Override
		public void setResponseEnd(long responseEnd) {
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.dynatrace.diagnostics.uemload.NavigationTiming#getDomLoading()
		 */
		@Override
		public long getDomLoading() {
			return 0;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.dynatrace.diagnostics.uemload.NavigationTiming#setDomLoading(long)
		 */
		@Override
		public void setDomLoading(long domLoading) {
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.dynatrace.diagnostics.uemload.NavigationTiming#getLoadEventEnd()
		 */
		@Override
		public long getLoadEventEnd() {
			return 0;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.dynatrace.diagnostics.uemload.NavigationTiming#setLoadEventEnd(long)
		 */
		@Override
		public void setLoadEventEnd(long loadEventEnd) {
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.dynatrace.diagnostics.uemload.NavigationTiming#toString()
		 */
		@Override
		public String toString() {
			return "NullNavigationTiming";
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.dynatrace.diagnostics.uemload.NavigationTiming#createSignal()
		 */
		@Override
		public String createSignal() {
			return BaseConstants.EMPTY_STRING;
		}

	}

	/**
	 * @param dnsSlowdownFactor the dnsSlowdownFactor to set
	 */
	public void setDNSSlowdownFactor(int dnsSlowdownFactor) {
		this.dnsSlowdownFactor = dnsSlowdownFactor;
	}
}
