package com.dynatrace.diagnostics.uemload.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import com.dynatrace.diagnostics.uemload.Bandwidth;
import com.dynatrace.diagnostics.uemload.BrowserCache;
import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.diagnostics.uemload.NavigationTiming;
import com.dynatrace.diagnostics.uemload.http.base.*;
import com.dynatrace.diagnostics.uemload.http.callback.HttpReaderCallback;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravel;
import com.dynatrace.easytravel.constants.BaseConstants.Http;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Response;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.base.Strings;

public class UemLoadHttpUtils {
	private static final Pattern MAX_AGE_PATTERN = Pattern.compile("max-age=(\\d+)");

	private static final Logger logger = Logger.getLogger(UemLoadHttpUtils.class.getName());

	private static final int QUARTER_SECOND = 250;

	public static void readResponse(UemLoadConnection connection, Bandwidth bandwidth, BrowserType browserType, NavigationTiming nt,
			HttpReaderCallback callback) throws IOException {
		String url = connection.getUrl();
		if (HostAvailability.INSTANCE.isHostUnavailable(url)) {
			return;
		}

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		InputStream in = null;

		try {
			in = connection.getInputStream();
			if (EasyTravel.isUemCorrelationTestingMode || bandwidth == Bandwidth.UNLIMITED) {
				IOUtils.copy(in, bytes);
			} else {
				int limit = UemLoadUtils.randomBandwidth(bandwidth, browserType) / (8 * 1000 / QUARTER_SECOND); // maximal
				int cnt;
				int counter = 0;
				long nextStep = System.currentTimeMillis() + QUARTER_SECOND;

					byte[] buffer = new byte[4096];
					while ((cnt = in.read(buffer)) >= 0) {
						if (nt != null && nt.getResponseStart() <= 0) {
							long ts = System.currentTimeMillis();
							nt.setResponseStart(ts);
						}
						counter += cnt;
						if (counter > limit) {
							long now = System.currentTimeMillis();
							if (now + 100 < nextStep) {
								long s = nextStep - now;
								Thread.sleep(s);
							}
							nextStep += QUARTER_SECOND;
							counter = 0;
						}
						bytes.write(buffer, 0, cnt);
					}
			}
		} catch (SocketTimeoutException e) {
			// ignore
		} catch (SocketException e) {
			// ignore
		} catch (InterruptedException e) {
			// ignore
		} finally {
			UemLoadUtils.close(in);
			// set to null as we recurse deeply below and thus should try to free stack-variables as well
			in = null;
		}

		byte[] byteArray = bytes.toByteArray();
		bytes.close();
		// set to null as we recurse deeply below and thus should try to free stack-variables as well
		bytes = null;

		callback.readDone(byteArray);
	}

	/**
	 *
	 * @param address defines the address to which the method should establish a connection
	 * @return <code>true</code> if a connection could be established <b>ignoring</b> the response code
	 * @author stefan.moschinski
	 */
	public static boolean isConnectable(String address) {
		UemLoadHttpClient client = new UemLoadHttpClient(Bandwidth.UNLIMITED, BrowserType.NONE);
		try {
			return client.isConnectable(address);
		} catch (IOException e) {
			logger.log(Level.FINE, TextUtils.merge("Could not connect to ''{0}''", address), e);
		} finally {
			client.close();
		}
		return false;
	}

	public static Collection<Header> getHeaderSingleton(String name, String value) {
		return Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(value) ?
				Collections.<Header> emptySet() :
				Collections.<Header> singleton(new BasicHeader(name, value));
	}

	public static boolean isSessionExpired(HttpResponse response) throws IOException {
		return response.getTextResponse().contains(Response.SESSION_EXPIRED);
	}

	public static long getMaxAgeInMillis(ResponseHeaders responseHeaders) {
		String cacheControlValue = responseHeaders.getValue(Http.Headers.CACHE_CONTROL);
		if (cacheControlValue == null) {
			return BrowserCache.DO_NOT_CACHE;
		}

		Matcher maxAgeMatcher = MAX_AGE_PATTERN.matcher(cacheControlValue);
		return maxAgeMatcher.find()
				? Long.valueOf(maxAgeMatcher.group(1))
				: BrowserCache.DO_NOT_CACHE;
	}

	/**
	 * Creates a new {@link NameValuePair}
	 *
	 * @param name
	 * @param value
	 * @return a new instance implementing {@link NameValuePair}
	 * @author stefan.moschinski
	 */
	public static NameValuePair createPair(String name, Object value) {
		return new BasicNameValuePair(name, String.valueOf(value));
	}
}
