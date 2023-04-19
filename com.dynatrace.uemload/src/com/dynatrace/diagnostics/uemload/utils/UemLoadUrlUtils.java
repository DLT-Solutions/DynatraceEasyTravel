package com.dynatrace.diagnostics.uemload.utils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.Uem;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;


public class UemLoadUrlUtils {

	private static final Logger logger = LoggerFactory.make();

	private static final NameValuePair[] NO_ARGUMENTS = new NameValuePair[0];

	public static String getUrl(String host, EtPageType page) {
		return getUrl(host, page.getPath());
	}

	/**
	 * Combines the given host and path. For example, "http://localhost:8080" and "/blub"
	 * will return "http://localhost:8080/blub".
	 *
	 * <b>Attention:</b> if the host is not only a host, the rest of the host string is ignored.
	 * That is, "http://localhost:8080/local/folder/" as host and "/blub" as path will return
	 * "http://localhost:8080/blub" anyway.
	 *
	 * @param host URL of the host
	 * @param path
	 * @return
	 * @author stefan.moschinski
	 */
	public static String getUrl(String host, String path) {
		return getUrl(host, path, NO_ARGUMENTS);
	}

	public static String getUrlForJourney(String host, EtPageType page, int journeyId) {
		return getUrlForJourney(host, page.getPath(), journeyId);
	}

	public static String getUrlForJourney(String host, String path, int journeyId) {

		return Journeys.isValidJourneyId(journeyId) ?
				getUrl(host, path, new BasicNameValuePair(Uem.Argument.JOURNEY_ID, String.valueOf(journeyId)))
				: getUrl(host, path);
	}

	public static String getUrlForJourney(String host, String path, int journeyId, BasicNameValuePair additionalParameter) {

		return Journeys.isValidJourneyId(journeyId) ?
				getUrl(host, path, new BasicNameValuePair(Uem.Argument.JOURNEY_ID, String.valueOf(journeyId)), additionalParameter)
				: getUrl(host, path);
	}

	/**
	 *
	 * @param url URL that is used to extract the host name
	 *        for instance, if you pass 'http://localhost:8080/orange.jsf', the method returns 'localhost'
	 * @return the host name
	 * @author stefan.moschinski
	 * @throws RuntimeException if no host name can be extracted from the passed URL
	 */
	public static String getHost(String url) {
		try {
			return new URL(url).getHost();
		} catch (MalformedURLException e) {
			throw new RuntimeException("For URL: " + url, e);
		}
	}

	public static String getUrl(String host, String path, NameValuePair... arguments) {
		return getUrl(host, path, false, arguments);
	}
	
	public static String getUrl(String host, String path, boolean isJSAgentBeacon, NameValuePair... arguments) {
		try {
			URL url = new URL(host);
			String protocol = url.getProtocol();
			String normalizedHost = url.getHost();
			int port = url.getPort();

			if (!isJSAgentBeacon) {
				if(path == null) {
					path = "/";
				}
				else if (!path.startsWith("/")) {
					path = "/" + path;
				}
				
				URIBuilder builder = new URIBuilder().setScheme(protocol).setHost(normalizedHost).setPort(port).setPath(path);
				if(!(ArrayUtils.isEmpty(arguments) || arguments == null)) {
					builder.setParameters(arguments);
				}
				return builder.build().toString();
			} else { //JSAgentBeacons are sent differently
				StringBuilder queryBuilder = new StringBuilder("$");
				if (!ArrayUtils.isEmpty(arguments)) {
					for (NameValuePair nvp: arguments) {
						queryBuilder.append(URLEncodedUtils.format(Arrays.asList(nvp), BaseConstants.UTF8));
						queryBuilder.append("$");
					}
				}
				
				String query = queryBuilder.toString();

				if(path == null) {
					path = "/";
				}
				else if (!path.startsWith("/")) {
					path = "/" + path;
				}
				String urlString = protocol + "://" + normalizedHost + (port==-1?"":":" + port) + path;
				if (!query.equals("$")) {
					urlString += "?" + query;
				}
				return urlString;
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException("For URL: " + host, e);
		} catch (URISyntaxException e) {
			throw new RuntimeException("For URL: " + host, e);
		}
	}

	/**
	 *
	 * @param url
	 * @param dtMonitorPath
	 * @param params
	 * @return
	 * @author stefan.moschinski
	 */
	public static String getUrl(String host, String path, Collection<NameValuePair> params) {
		return getUrl(host, path, true, params == null ? NO_ARGUMENTS : params.toArray(new NameValuePair[params.size()]));
	}

	/**
	 *
	 * @param url the request URL
	 * @return the host URL in the form <protocol>://<hostname>:<port> (<b>without</b> trailing slash)
	 *         For example, if you pass SECRET,
	 *         the method returns SECRET
	 * @throws MalformedURLException if the passed url is not a valid URL
	 * @author stefan.moschinski
	 */
	public static String getExtendedHostUrl(String url) {
		return getExtendedHostUrl(url, /* trailingSlash */false);
	}

	/**
	 * like {@link UemLoadUrlUtils#getExtendedHostUrl(String)}, but <b>with</b> trailing slash
	 *
	 * @author stefan.moschinski
	 */
	public static String getExtendedHostUrlTrailingSlash(String url) {
		return getExtendedHostUrl(url, /* trailingSlash */true);
	}

	private static String getExtendedHostUrl(String baseUrl, boolean trailingSlash) {
		StringBuilder sb = new StringBuilder();
		sb.append(getUrl(baseUrl, null, NO_ARGUMENTS));
		if (!trailingSlash) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 *
	 * @param val to encode in UTF-8
	 * @return the passed value as encoded UTF-8 string or <code>null</code> if the UTF-8 encoding is not supported or the passed
	 *         value is <code>null</code>
	 * @author stefan.moschinski
	 */
	public static String encodeUrlUtf8(Object val) {
		if (val == null) {
			return null;
		}
		try {
			return URLEncoder.encode(String.valueOf(val), BaseConstants.UTF8);
		} catch (UnsupportedEncodingException e) {
			logger.warn(TextUtils.merge("The string ''{0}'' cannot be encoded via UTF-8", String.valueOf(val)), e);
		}
		return null;
	}

}
