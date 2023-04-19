package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class B2BPageTest {

	@Mock B2BSession session;
	private B2BPage page;

	@Before
	public void setUp() {
		page = new B2BPage(EtPageType.B2B_JOURNEY, session);
	}

	@Test
	public void testGetRandomLink() {
		String html1 = "<a href=\"/Journey?Page=1\">";
		String html2 = "<a href=\"/Journey?Page=2\">";
		String html3 = "<a href=\"/Journey?Page=3\">";
		when(session.getHtml()).
				thenReturn(html1).
				thenReturn(html2).
				thenReturn(html3);

		String host = "http://localhost:8900/";
		when(session.getHost()).thenReturn(host);

		String regex = "/Journey\\?Page=\\d*";
		assertEquals(host + "Journey?Page=1", page.getRandomLink(regex));
		assertEquals(host + "Journey?Page=2", page.getRandomLink(regex));
		assertEquals(host + "Journey?Page=3", page.getRandomLink(regex));
	}

}
