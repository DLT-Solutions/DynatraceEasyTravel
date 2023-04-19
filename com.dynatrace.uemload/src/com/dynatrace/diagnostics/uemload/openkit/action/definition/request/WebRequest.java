package com.dynatrace.diagnostics.uemload.openkit.action.definition.request;

import com.dynatrace.diagnostics.uemload.openkit.event.lifetime.EventCallbackSet;

public class WebRequest {
	private WebRequest() {}

	public static WebRequestBuilder sent(WebRequestConfig config) {
		return new WebRequestBuilder(config);
	}

	public static class WebRequestBuilder {
		private final WebRequestConfig config;

		private WebRequestBuilder(WebRequestConfig config) {
			this.config = config;
		}

		public WebRequestDefinition begin(EventCallbackSet eventCallbacks) {
			return new WebRequestDefinition(config.action, eventCallbacks, config.url, config.httpClient);
		}
	}
}
