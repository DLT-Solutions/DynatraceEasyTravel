package com.dynatrace.diagnostics.uemload.openkit.action.definition.request;

import com.dynatrace.diagnostics.uemload.http.base.UemLoadHttpClient;
import com.dynatrace.diagnostics.uemload.openkit.action.definition.ActionParent;

public class WebRequestConfig {
	public final UemLoadHttpClient httpClient;
	public final ActionParent action;
	public final String url;

	private WebRequestConfig(UemLoadHttpClient httpClient, ActionParent action, String url) {
		this.httpClient = httpClient;
		this.action = action;
		this.url = url;
	}

	public static WebRequestConfigBuilder from(ActionParent action) {
		return new WebRequestConfigBuilder(action);
	}

	public static class WebRequestConfigBuilder {
		private final ActionParent action;

		private WebRequestConfigBuilder(ActionParent action) {
			this.action = action;
		}

		public AddressedWebRequestConfigBuilder to(String url) {
			return new AddressedWebRequestConfigBuilder(url);
		}

		public class AddressedWebRequestConfigBuilder {
			private final String url;

			private AddressedWebRequestConfigBuilder(String url) {
				this.url = url;
			}

			public WebRequestConfig using(UemLoadHttpClient httpClient) {
				return new WebRequestConfig(httpClient, action, url);
			}
		}
	}
}
