package com.dynatrace.diagnostics.uemload.dtheader;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.Contains;


/**
 *
 * @author stefan.moschinski
 */
public class XDynatraceHeaderTest{

	DynaTraceHeaderImpl header;
	@Before
	public void setUp() throws Exception {
		header = new DynaTraceHeaderImpl();
	}

	@Test
	public void testGetHeaderName() {
		String headerName = "X-dynaTrace";
		assertEquals(headerName, header.getHeaderName());
	}

	@Test
	public void testGetHeaderValue() {
		String virtualUserId1 = "1";
		header.setVirtualUserId(virtualUserId1);
		String headerValue = getVirtualUserId(virtualUserId1);
		assertThat(header.getHeaderValue(), new Contains(headerValue));

		String pageContext1 = "B2CScenario";
		header.setPageContext(pageContext1);
		assertTrue(header.getHeaderValue().contains(getPageContextString(pageContext1)));
		assertTrue(header.getHeaderValue().contains(getVirtualUserId(virtualUserId1)));

		header.setTimerName(DynaTraceTagMetaData.SEARCH.getTimerName());
		assertTrue(header.getHeaderValue().contains(getTimerNameString(DynaTraceTagMetaData.SEARCH.toString())));
		assertTrue(header.getHeaderValue().contains(getPageContextString(pageContext1)));
		assertTrue(header.getHeaderValue().contains(getVirtualUserId(virtualUserId1)));
	}

	private String getVirtualUserId(String virtualUserId) {
		return getHeaderString("VU", virtualUserId);
	}

	private String getPageContextString(String pageContext) {
		return getHeaderString("PC", pageContext);
	}

	private String getTimerNameString(String timerName) {
		return getHeaderString("NA", timerName);
	}

	private String getHeaderString(String key, String value) {
		return String.format("%s=%s;", key, value);
	}

}
