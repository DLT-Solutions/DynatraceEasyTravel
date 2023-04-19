package com.dynatrace.easytravel.util;


import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.net.UrlUtils;

/**
 * Helper class to read data on SSL secured websites, currently we
 * more or less disable all security checks and just ensure that we
 * read the data that we get without doing any checking!
 *
 * @author cwat-dstadler
 */
public class DtSSLHelper {
	private static final Logger LOGGER = Logger.getLogger(DtSSLHelper.class.getName());

	private final CloseableHttpClient httpclient;
	private final RequestConfig reqConfig;
	private final int timeoutMs;

	public DtSSLHelper(int timeoutMs, String userAgent) {
		final CredentialsProvider credsProvider = createCredentialsProvider();

		reqConfig = RequestConfig.custom()
			    .setSocketTimeout(timeoutMs)
			    .setConnectTimeout(timeoutMs)
			    .setConnectionRequestTimeout(timeoutMs)
			    .setRedirectsEnabled(true)
			    .build();

		// configure the builder for HttpClients
		HttpClientBuilder builder = HttpClients.custom()
		        .setDefaultCredentialsProvider(credsProvider)
				.setDefaultRequestConfig(reqConfig)
				.setUserAgent(userAgent);

		// add a permissive SSL Socket Factory to the builder
		builder = UrlUtils.trustAllHttpsCertificates(builder);

		// finally create the HttpClient instance
		httpclient = builder.build();
		this.timeoutMs = timeoutMs;
	}

	private static CredentialsProvider createCredentialsProvider() {
		EasyTravelConfig config = EasyTravelConfig.read();

		String username = config.dtServerUsername;
		if (config.apmServerUsername != null && !config.apmServerUsername.isEmpty()) {
			username = config.apmServerUsername;
		}

		String password = config.dtServerPassword;
		if (config.apmServerPassword != null && !config.apmServerPassword.isEmpty()) {
			password = config.apmServerPassword;
		}

		final CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(
	            new AuthScope(null, -1),
	            new UsernamePasswordCredentials(username, password));
		return credsProvider;
	}

	public String getData(String sUrl) throws IOException {
		HttpGet httpGet = new HttpGet(sUrl);

		CloseableHttpResponse response = httpclient.execute(httpGet);

		try {
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode != 200) {
				LOGGER.info("Could not read " + sUrl + ": " + statusCode);
				return "";
			}

		    HttpEntity entity = response.getEntity();

		    String data = IOUtils.toString(entity.getContent());

		    // ensure all content is taken out to free resources
		    EntityUtils.consume(entity);

	    	return data;
		} finally {
		    response.close();
		}
	}
}
