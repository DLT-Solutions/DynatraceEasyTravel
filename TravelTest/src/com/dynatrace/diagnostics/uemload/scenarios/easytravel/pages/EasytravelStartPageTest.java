/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: EasytravelStartPageTest.java
 * @date: 21.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.dynatrace.diagnostics.uemload.Bandwidth;
import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.diagnostics.uemload.BrowserWindowSize;
import com.dynatrace.diagnostics.uemload.DemoUserData;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.http.base.HttpResponse;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.EasytravelStartPage.State;
import com.dynatrace.easytravel.misc.CommonUser;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.NanoHTTPD;


/**
 *
 * @author stefan.moschinski
 */
@RunWith(MockitoJUnitRunner.class)
public class EasytravelStartPageTest {

	@Mock
	HttpResponse response;

	@Test
	public void testGetRandomJourneyId() throws IOException {
		EasytravelStartPage startPage = new EasytravelStartPage(null, null);
		when(response.getTextResponse()).thenReturn(
				"<a href=\"/orange-trip-details.jsf?journeyId=260230480\"");
		assertThat(startPage.getRandomJourneyId(response), is(260230480));

		when(response.getTextResponse()).thenReturn(
				"<a href=\"/orange-trip-details.jsf?journeyId=-130\"");
		assertThat(startPage.getRandomJourneyId(response), is(-130));
	}



	@Test
	public void testSearchMultiple() throws IOException, InterruptedException {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		try {
			for(int c = 0;c < 2000;c++) {
				Browser browser = new Browser(BrowserType.FF_530, new Location("Asia", "Tokio", "192.182.0.22"), 0, Bandwidth.BROADBAND, BrowserWindowSize._1024x768);
				CustomerSession session = new CustomerSession("http://localhost:" + server.getPort(),
						new CommonUser(DemoUserData.MARIA, DemoUserData.MARIA),
						new Location("Asia", "Tokio", "192.182.0.22"),
						false);

				EasytravelStartPage page = new EasytravelStartPage(session, State.SEARCH);

				page.enablePartialResponseLogging();

				final AtomicBoolean called = new AtomicBoolean(false);
				browser.startPageLoad("http://localhost:" + server.getPort(), "sometitle", session, null, null, new UEMLoadCallback() {
					@Override
					public void run() throws IOException {
						called.set(true);
					}
				});
				assertTrue(called.get());

				browser.close();

				//System.out.println("read " + c);
			}
		} finally {
			server.stop();
		}
	}
}
