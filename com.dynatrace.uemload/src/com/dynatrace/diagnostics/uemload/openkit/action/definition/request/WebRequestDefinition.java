package com.dynatrace.diagnostics.uemload.openkit.action.definition.request;

import com.dynatrace.diagnostics.uemload.NavigationTiming;
import com.dynatrace.diagnostics.uemload.http.base.HttpRequest;
import com.dynatrace.diagnostics.uemload.http.base.HttpResponse;
import com.dynatrace.diagnostics.uemload.http.base.UemLoadHttpClient;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionParent;
import com.dynatrace.diagnostics.uemload.openkit.event.ElapsingEvent;
import com.dynatrace.diagnostics.uemload.openkit.event.lifetime.EventCallbackSet;
import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.ErrorBuilder;
import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.api.WebRequestTracer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Represents a monitored web request reported by an OpenKit {@link Action}. Can be artificially extended to simulate internet connection speed.
 *
 * @see ElapsingEvent for a detailed state timeline
 */
public class WebRequestDefinition extends ElapsingEvent<WebRequestDefinition> {
	private Action action;
	private final Supplier<Action> actionSupplier;

	private boolean reportActionErrors;

	private final UemLoadHttpClient httpClient;
	private HttpRequest.Type requestType = HttpRequest.Type.GET;
	private final String url;

	private byte[] body;
	private ContentType contentType;

	private String response;
	private int responseStatus = -1;

	private static final List<Integer> REPORTED_ERROR_CODES = Arrays.asList(403, 404, 500, 503);

	WebRequestDefinition(ActionParent parentAction, EventCallbackSet startCallbacks, String url, UemLoadHttpClient httpClient) {
		this.url = url;
		this.httpClient = httpClient;
		this.actionSupplier = parentAction::getAction;

		withParentAction(parentAction);
		addBeginCallbacks(startCallbacks);
	}

	public WebRequestDefinition ofPostType(byte[] body, ContentType contentType) {
		requestType = HttpRequest.Type.POST;
		this.contentType = contentType;
		this.body = body;
		return this;
	}

	public WebRequestDefinition ofPostType(byte[] body) {
		return ofPostType(body, ContentType.APPLICATION_JSON);
	}

	public WebRequestDefinition withActionErrorReporting() {
		reportActionErrors = true;
		return this;
	}

	@Override
	protected void run() {
		action = actionSupplier.get();
		WebRequestTracer tracer = action.traceWebRequest(url);
		tracer.start();
		try {
			List<Header> headers = new ArrayList<>();
			headers.add(new BasicHeader(OpenKitConstants.WEBREQUEST_TAG_HEADER, tracer.getTag()));
			if (body != null) {
				headers.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, contentType.getMimeType()));
				tracer.setBytesSent(body.length);
			}
			HttpRequest request = new HttpRequest(url, headers).setMethod(requestType);
			httpClient.execute(request, NavigationTiming.NONE, httpResponse -> handleResponse(httpResponse, tracer), body, error -> handleResponse(error, tracer));
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Exception: ", e);
			action.reportError(new ErrorBuilder.ErrorCodeEvent("Could not connect", -1).build());
		} finally {
			tracer.stop(responseStatus);
		}
		super.run();
	}

	private void handleResponse(HttpResponse response, WebRequestTracer tracer) throws IOException {
		responseStatus = response.getStatusCode();
		this.response = response.getTextResponse();
		tracer.setBytesReceived(response.getTextResponse().getBytes().length);
		if (reportActionErrors && REPORTED_ERROR_CODES.contains(responseStatus)) {
			action.reportError(new ErrorBuilder.ErrorCodeEvent("HTTP Error", responseStatus).build());
		}
	}

	@Override
	public WebRequestDefinition withStartDelay(int minStartDelay, int maxStartDelay) {
		return super.withStartDelay(minStartDelay, maxStartDelay);
	}

	@Override
	protected WebRequestDefinition getThis() {
		return this;
	}

	public int getResponseStatus() {
		return responseStatus;
	}

	public String getResponse() {
		return response;
	}

	public static String getRequestUrl(String url, NameValuePair... params) {
		try {
			return new URIBuilder(url).setParameters(params).build().toString();
		} catch (URISyntaxException e) {
			logger.log(Level.SEVERE, "Building web request url " + url + " failed", e);
		}
		return null;
	}

	public static <M> byte[] getRequestBody(M model) {
		return new Gson().toJson(model).getBytes();
	}

	public static <M> M mapResponse(String response, Class<M> clazz) {
		return new GsonBuilder().registerTypeAdapter(Calendar.class, (JsonDeserializer<Object>) (json, typeOfT, context) -> {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(json.getAsLong());
			return calendar;
		}).create().fromJson(response, clazz);
	}
}
