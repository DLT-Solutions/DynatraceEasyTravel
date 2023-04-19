package com.dynatrace.easytravel.frontend.servlet;

import static org.easymock.EasyMock.*;

import javax.servlet.FilterChain;

import org.junit.Test;

import com.dynatrace.easytravel.utils.TestHelpers;

import ch.qos.logback.classic.Level;

public class LogoutFilterTest extends ServletTestBase {
    private static final String REQUEST_URI = "http://request.uri/";
    private static final String REQUEST_URI_HTTPS = "https://request.uri/";
    private static final String REQUEST_HOST = "request.uri";
    private static final String LOGOUT_LANDING_URI = "http://request.uri/logout.jsf";
    private static final String LOGOUT_LANDING_URI_HTTPS = "https://request.uri/logout.jsf";
	private static final String CLEAR = "clear";

    @Test
    public void doFilterTest() throws Exception {
        LogoutFilter logoutFilter = new LogoutFilter();

        initMocks();
        FilterChain chainMock = createMock(FilterChain.class);

        expect(requestMock.getRequestURI()).andReturn("");
        chainMock.doFilter(requestMock, responseMock);

        expect(requestMock.getRequestURI()).andReturn(REQUEST_URI + LogoutFilter.INVALIDATE_SESSION).anyTimes();
        expect(requestMock.getSession(false)).andReturn(sessionMock);
        expect(requestMock.getHeader("referer")).andReturn(REQUEST_URI);
        expect(requestMock.getHeader("host")).andReturn(REQUEST_HOST);
        sessionMock.invalidate();
        expect(requestMock.getParameter(CLEAR)).andReturn(CLEAR);
        responseMock.setContentType("text/html");
        responseMock.sendRedirect(LOGOUT_LANDING_URI);
        expect(responseMock.isCommitted()).andReturn(false).anyTimes();

        replayMocks();
        replay(chainMock);

        logoutFilter.doFilter(requestMock, responseMock, chainMock);
        logoutFilter.doFilter(requestMock, responseMock, chainMock);

        verifyMocks();
        verify(chainMock);
    }

    @Test
    public void doFilterTestNullSession() throws Exception {
        LogoutFilter logoutFilter = new LogoutFilter();

        initMocks();
        FilterChain chainMock = createMock(FilterChain.class);

        expect(requestMock.getRequestURI()).andReturn("");
        chainMock.doFilter(requestMock, responseMock);

        expect(requestMock.getRequestURI()).andReturn(REQUEST_URI + LogoutFilter.INVALIDATE_SESSION).anyTimes();
        expect(requestMock.getSession(false)).andReturn(null);
        expect(requestMock.getHeader("referer")).andReturn(REQUEST_URI);
        expect(requestMock.getHeader("host")).andReturn(REQUEST_HOST);
        expect(requestMock.getParameter(CLEAR)).andReturn(CLEAR);
        responseMock.setContentType("text/html");
        responseMock.sendRedirect(LOGOUT_LANDING_URI);
        expect(responseMock.isCommitted()).andReturn(false).anyTimes();

        replayMocks();
        replay(chainMock);

        logoutFilter.doFilter(requestMock, responseMock, chainMock);
        logoutFilter.doFilter(requestMock, responseMock, chainMock);

        verifyMocks();
        verify(chainMock);
    }

    @Test
    public void doFilterTestNoClear() throws Exception {
        LogoutFilter logoutFilter = new LogoutFilter();

        initMocks();
        FilterChain chainMock = createMock(FilterChain.class);

        expect(requestMock.getRequestURI()).andReturn("");
        chainMock.doFilter(requestMock, responseMock);

        expect(requestMock.getRequestURI()).andReturn(REQUEST_URI + LogoutFilter.INVALIDATE_SESSION).anyTimes();
        expect(requestMock.getSession(false)).andReturn(sessionMock);
        expect(requestMock.getHeader("referer")).andReturn(REQUEST_URI);
        expect(requestMock.getHeader("host")).andReturn(REQUEST_HOST);
        sessionMock.invalidate();
        expect(requestMock.getParameter(CLEAR)).andReturn(null);
        responseMock.setContentType("text/html");
        responseMock.sendRedirect(LOGOUT_LANDING_URI);
        expect(responseMock.isCommitted()).andReturn(false).anyTimes();

        replayMocks();
        replay(chainMock);

        logoutFilter.doFilter(requestMock, responseMock, chainMock);
        logoutFilter.doFilter(requestMock, responseMock, chainMock);

        verifyMocks();
        verify(chainMock);
    }

    @Test
    public void testWithDifferentLogLevel() {
    	TestHelpers.runTestWithDifferentLogLevel(new Runnable() {

			@Override
			public void run() {
				try {
					doFilterTest();
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}
		}, LogoutFilter.class.getName(), Level.DEBUG, Level.INFO);
    }

    @Test
    public void doFilterProtocolTest() throws Exception {
    	/* Tests protocol used by redirection URI according to referer's protocol. */
        LogoutFilter logoutFilter = new LogoutFilter();

        initMocks();
        FilterChain chainMock = createMock(FilterChain.class);

        // 1st test, null referer and http response
        expect(requestMock.getRequestURI()).andReturn("");
        chainMock.doFilter(requestMock, responseMock);

        expect(requestMock.getRequestURI()).andReturn(REQUEST_URI + LogoutFilter.INVALIDATE_SESSION).anyTimes();
        expect(requestMock.getSession(false)).andReturn(sessionMock);
        expect(requestMock.getHeader("referer")).andReturn(null);
        expect(requestMock.getHeader("host")).andReturn(REQUEST_HOST);
        sessionMock.invalidate();
        expect(requestMock.getParameter(CLEAR)).andReturn(CLEAR);
        responseMock.setContentType("text/html");
        responseMock.sendRedirect(LOGOUT_LANDING_URI);
        expect(responseMock.isCommitted()).andReturn(false).anyTimes();

        replayMocks();
        replay(chainMock);

        logoutFilter.doFilter(requestMock, responseMock, chainMock);
        logoutFilter.doFilter(requestMock, responseMock, chainMock);

        verifyMocks();
        verify(chainMock);
        resetMocks();

        // 2nd test, http referer and http response
        initMocks();
        chainMock = createMock(FilterChain.class);

        expect(requestMock.getRequestURI()).andReturn("");
        chainMock.doFilter(requestMock, responseMock);

        expect(requestMock.getRequestURI()).andReturn(REQUEST_URI + LogoutFilter.INVALIDATE_SESSION).anyTimes();
        expect(requestMock.getSession(false)).andReturn(sessionMock);
        expect(requestMock.getHeader("referer")).andReturn(REQUEST_URI);
        expect(requestMock.getHeader("host")).andReturn(REQUEST_HOST);
        sessionMock.invalidate();
        expect(requestMock.getParameter(CLEAR)).andReturn(CLEAR);
        responseMock.setContentType("text/html");
        responseMock.sendRedirect(LOGOUT_LANDING_URI);
        expect(responseMock.isCommitted()).andReturn(false).anyTimes();

        replayMocks();
        replay(chainMock);

        logoutFilter.doFilter(requestMock, responseMock, chainMock);
        logoutFilter.doFilter(requestMock, responseMock, chainMock);

        verifyMocks();
        verify(chainMock);
        resetMocks();

        // 3rd test, https referer and https response
        initMocks();
        chainMock = createMock(FilterChain.class);

        expect(requestMock.getRequestURI()).andReturn("");
        chainMock.doFilter(requestMock, responseMock);

        expect(requestMock.getRequestURI()).andReturn(REQUEST_URI + LogoutFilter.INVALIDATE_SESSION).anyTimes();
        expect(requestMock.getSession(false)).andReturn(sessionMock);
        expect(requestMock.getHeader("referer")).andReturn(REQUEST_URI_HTTPS);
        expect(requestMock.getHeader("host")).andReturn(REQUEST_HOST);
        sessionMock.invalidate();
        expect(requestMock.getParameter(CLEAR)).andReturn(CLEAR);
        responseMock.setContentType("text/html");
        responseMock.sendRedirect(LOGOUT_LANDING_URI_HTTPS);
        expect(responseMock.isCommitted()).andReturn(false).anyTimes();

        replayMocks();
        replay(chainMock);

        logoutFilter.doFilter(requestMock, responseMock, chainMock);
        logoutFilter.doFilter(requestMock, responseMock, chainMock);

        verifyMocks();
        verify(chainMock);
    }
}
