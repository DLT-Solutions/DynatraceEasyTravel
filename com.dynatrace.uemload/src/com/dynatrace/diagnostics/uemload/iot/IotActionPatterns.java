package com.dynatrace.diagnostics.uemload.iot;

import com.dynatrace.diagnostics.uemload.iot.visit.IotActionType;
import com.dynatrace.diagnostics.uemload.openkit.action.ActionSetPatterns;
import com.dynatrace.openkit.api.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;

/**
 * @author Michal.Bakula
 */
public class IotActionPatterns extends ActionSetPatterns {

	protected final CloseableHttpClient httpClient = HttpClientBuilder.create().build();

	public IotActionPatterns(Session session) {
		super(session);
	}

	public void reportCrash(IotDevice device) {
		try {
			device.crash();
		} catch (Exception e) {
			activeSession.reportCrash(device.getCrashName(), device.getCrashReason(), e.toString());
			logger.log(Level.FINEST, "device crash", e);
		}
	}

	public void actionWithServerCall(String name, String host, String path) {
		RootAction rootAction = activeSession.enterAction(name);
		rootAction.reportValue("command", path);
		String url = null;
		try {
			url = new URIBuilder(host).setPath(path).build().toString();
			Action action = rootAction.enterAction("openConnection");
			WebRequestTracer tag = null;
			CloseableHttpResponse response = null;
			try {
				tag = rootAction.traceWebRequest(url);
				tag.start();

				HttpGet get = new HttpGet(url);
				get.addHeader(OpenKitConstants.WEBREQUEST_TAG_HEADER, tag.getTag());
				response = httpClient.execute(get);
			} finally {
				if (tag != null) {
					tag.stop(response.getStatusLine().getStatusCode());
				}
				if (response != null) {
					response.close();
				}
			}
			action.leaveAction();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Exception: ", e);
			rootAction.reportError("Can't reach server " + url, e);
		} catch (URISyntaxException e) {
			logger.log(Level.SEVERE, "Exception: ", e);
			rootAction.reportError("URL syntax error", e);
		}
		rootAction.leaveAction();
	}

	public void userAuthenticationCall(String name, String host, String username, String password, String fullName) {
		RootAction rootAction = activeSession.enterAction(name);
		rootAction.reportValue("command", "/RentalCarAuthentication");
		String url = null;
		try {
			url = new URIBuilder(host).setPath("/RentalCarAuthentication").build().toString();
			Action action = rootAction.enterAction("openConnection");
			WebRequestTracer tag = null;
			CloseableHttpResponse response = null;
			try {
				tag = rootAction.traceWebRequest(url);
				tag.start();

				StringEntity body = new StringEntity("{\"name\": \"" + username + "\", \"password\": \"" + password + "\"}", ContentType.APPLICATION_JSON);
				HttpPost post = new HttpPost(url);
				post.setEntity(body);
				post.addHeader(OpenKitConstants.WEBREQUEST_TAG_HEADER, tag.getTag());
				response = httpClient.execute(post);
			} finally {
				if (tag != null) {
					tag.stop(response.getStatusLine().getStatusCode());
				}
				if (response != null) {
					response.close();
				}
			}
			action.leaveAction();
			
			if (IotActionType.AUTHENTICATION.getValue().contentEquals(name)) {
				activeSession.identifyUser(fullName);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Exception: ", e);
			rootAction.reportError("Can't reach server " + url, e);
		} catch (URISyntaxException e) {
			logger.log(Level.SEVERE, "Exception: ", e);
			rootAction.reportError("URL syntax error", e);
		}
		rootAction.leaveAction();
	}

}
