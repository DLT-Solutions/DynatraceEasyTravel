package com.dynatrace.easytravel.frontend.servlet;

import static org.easymock.EasyMock.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.easymock.internal.matchers.Any;
import org.junit.Before;

import com.dynatrace.easytravel.spring.SpringTestBase;


public class ServletTestBase extends SpringTestBase {

    protected HttpServletRequest requestMock;
    protected HttpServletResponse responseMock;
    protected HttpSession sessionMock;
    protected ServletConfig servletConfigMock;
    protected ServletContext servletContextMock;


    @Before
    public void initMocks() {
        requestMock = createMock(HttpServletRequest.class);
        responseMock = createMock(HttpServletResponse.class);
        sessionMock = createMock(HttpSession.class);
        servletConfigMock = createMock(ServletConfig.class);
        servletContextMock = createMock(ServletContext.class);
    }


    protected void resetMocks() {
        reset(requestMock, responseMock, sessionMock, servletConfigMock, servletContextMock);
    }


    protected void replayMocks() {
        replay(requestMock, responseMock, sessionMock, servletConfigMock, servletContextMock);
    }


    protected void verifyMocks() {
        verify(requestMock, responseMock, sessionMock, servletConfigMock, servletContextMock);
    }


	public static void expectDynaTraceAgent(HttpServletRequest request) {
        // getHeader may be called by the dynaTrace Agent...

		expect(request.getHeader("user-agent")).andReturn(null).anyTimes();
        expect(request.getHeader("X-Forwarded-Host")).andReturn(null).times(0, 3);
        expect(request.getHeader("X-Host")).andReturn(null).times(0, 3);
        expect(request.getHeader("Host")).andReturn(null).times(0, 3);
        expect(request.getHeader("rproxy_remote_address")).andReturn(null).times(0,1);
        expect(request.getHeader("True-Client-IP")).andReturn(null).times(0,1);
        expect(request.getHeader("X-Client-Ip")).andReturn(null).times(0,1);
        expect(request.getHeader("X-Forwarded-For")).andReturn(null).times(0,1);
        expect(request.getHeader("X-Http-Client-Ip")).andReturn(null).times(0,1);
        expect(request.getHeader("X-dynaTrace")).andReturn(null).anyTimes();
        expect(request.getHeader("dynaTrace")).andReturn(null).times(0,2);
        expect(request.getHeader("X-dynatrace-Origin-URL")).andReturn(null).times(0,1);

        // ruxit
        expect(request.getHeader("x-forwarded-host")).andReturn(null).anyTimes();
        expect(request.getHeader("x-host")).andReturn(null).anyTimes();
        expect(request.getHeader("host")).andReturn(null).anyTimes();
        expect(request.getHeader("true-client-ip")).andReturn(null).anyTimes();
        expect(request.getHeader("x-client-ip")).andReturn(null).anyTimes();
        expect(request.getHeader("x-cluster-client-ip")).andReturn(null).anyTimes();
        expect(request.getHeader("x-forwarded-for")).andReturn(null).anyTimes();
        expect(request.getHeader("x-http-client-ip")).andReturn(null).anyTimes();
        expect(request.getHeader("If-None-Match")).andReturn(null).anyTimes();
        expect(request.getHeader("If-Modified-Since")).andReturn(null).anyTimes();
        expect(request.getHeader("Origin")).andReturn(null).times(0,1);
        expect(request.getHeader("x-dtPC")).andReturn(null).times(0,1);
        expect(request.getHeader("x-dtReferer")).andReturn(null).times(0,1);
        expect(request.getHeader("Referer")).andReturn(null).times(0,1);
        expect(request.getHeader("If-Match")).andReturn(null).times(0,1);
        expect(request.getHeader("If-Unmodified-Since")).andReturn(null).times(0,1);
        expect(request.getHeader("If-Range")).andReturn(null).times(0,1);
        expect(request.getCookies()).andReturn(null).anyTimes();

        expect(request.getScheme()).andReturn(null).anyTimes();
        expect(request.getServerName()).andReturn(null).anyTimes();
        expect(request.getServerPort()).andReturn(0).anyTimes();
        expect(request.getQueryString()).andReturn(null).anyTimes();
        expect(request.getMethod()).andReturn(null).times(0, 3);
        expect(request.getRemoteAddr()).andReturn(null).times(0, 1);
        expect(request.getHeaderNames()).andReturn(null).times(0, 1);
	}

    public static String anyString() {
        reportMatcher(Any.ANY);
        return null;
    }

    public static Cookie anyCookie() {
        reportMatcher(Any.ANY);
        return null;
    }
}
