package com.dynatrace.diagnostics.uemload.http.callback;

import java.io.IOException;

import com.dynatrace.diagnostics.uemload.http.base.HttpResponse;


public interface HttpResponseCallback {

	HttpResponseCallback NONE = new HttpResponseCallback() {
		@Override
		public void readDone(HttpResponse response) throws IOException {
			// empty
		}
	};

    public void readDone(HttpResponse response) throws IOException;

}
