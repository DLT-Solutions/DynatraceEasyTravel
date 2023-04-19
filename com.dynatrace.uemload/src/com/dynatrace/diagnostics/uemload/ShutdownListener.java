package com.dynatrace.diagnostics.uemload;

import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;


@WebServiceProvider
@ServiceMode(value = Service.Mode.MESSAGE)
public class ShutdownListener implements Provider<Source> {

	@javax.annotation.Resource(type = Object.class)
	protected WebServiceContext wsContext;


	@Override
	public Source invoke(Source source) {
			String replyElement = "<p>call_accepted</p>";
			StreamSource reply = new StreamSource(
										new StringReader(replyElement));

			new Thread(new Shutdown()).start();
			return reply;

	}

	private class Shutdown implements Runnable {

		@Override
		public void run() {
			CLI.stopUemLoad();
		}
	}



}
