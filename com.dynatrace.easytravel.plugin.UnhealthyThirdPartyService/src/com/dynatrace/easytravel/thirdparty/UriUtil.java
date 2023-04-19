package com.dynatrace.easytravel.thirdparty;

import java.net.URI;
import java.net.URISyntaxException;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;


public class UriUtil {

	private static final Logger log = LoggerFactory.make();

	private final static String SLOWDOWN_FILTER = "static";
	private final static String UNAVAILABLE_DIR = "unavailable";
	private final static String CACHING_FILTER = "caching";

	public static URI buildUri(String url, String dir, String resource, boolean caching) {
		return buildUri(url, dir, resource, caching, false, false);
	}

	public static URI buildUri(String url, String dir, String resource, boolean caching, boolean unavailable, boolean delayed) {
		StringBuilder sb = new StringBuilder();
		sb.append(TextUtils.appendTrailingSlash(url));


		if (unavailable) {
			sb.append(TextUtils.appendTrailingSlash(UNAVAILABLE_DIR));
		}

		if (delayed) {
			sb.append(TextUtils.appendTrailingSlash(SLOWDOWN_FILTER));
		}

		if (caching) {
			sb.append(TextUtils.appendTrailingSlash(CACHING_FILTER));
		}

		if (dir != null) {
			sb.append(TextUtils.appendTrailingSlash(dir));
		}

		if (resource != null) {
			sb.append(resource);
		}

		try {
			URI uri = new URI(sb.toString());
			return uri;
		} catch (URISyntaxException e) {
			String msg = "URI is invalid. " + e.getMessage();
			log.warn(msg);
			throw new RuntimeException(e);
		}
	}

}
