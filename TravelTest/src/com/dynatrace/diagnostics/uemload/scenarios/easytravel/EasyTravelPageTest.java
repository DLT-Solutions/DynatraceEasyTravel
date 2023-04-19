package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.dynatrace.diagnostics.uemload.Bandwidth;
import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.diagnostics.uemload.BrowserWindowSize;
import com.dynatrace.diagnostics.uemload.DemoUserData;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.easytravel.misc.CommonUser;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.NanoHTTPD;


public class EasyTravelPageTest {

	@Test
	public void testEasyTravelPage() throws Exception {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		try {
			for(int c = 0;c < 100;c++) {
				Browser browser = new Browser(BrowserType.FF_530, new Location("Asia", "Tokio", "192.182.0.22"), 0, Bandwidth.BROADBAND, BrowserWindowSize._1024x768);
				CustomerSession session = new CustomerSession("http://localhost:" + server.getPort(),
						new CommonUser(DemoUserData.MARIA, DemoUserData.MARIA),
						new Location("Asia", "Tokio", "192.182.0.22"),
						false);

				EasyTravelPage page = new EasyTravelPage(EtPageType.START, session, true);

				page.enablePartialResponseLogging();

				final AtomicBoolean called = new AtomicBoolean(false);
				browser.startPageLoad("http://localhost:" + server.getPort(), "sometitle", session, null, null, new UEMLoadCallback() {
					@Override
					public void run() throws IOException {
						called.set(true);
					}
				});
				assertTrue(called.get());

				called.set(false);
				page.loadPage(browser, "http://localhost:" + server.getPort(), new UEMLoadCallback() {
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
