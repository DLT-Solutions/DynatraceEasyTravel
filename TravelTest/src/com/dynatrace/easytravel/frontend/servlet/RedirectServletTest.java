package com.dynatrace.easytravel.frontend.servlet;

import static org.easymock.EasyMock.*;

import org.junit.Test;


public class RedirectServletTest extends ServletTestBase {


    private static final String SERVLET_CONTEXT_PATH = "redirect";

    @Test
    public void testDoGet() throws Exception {
        RedirectServlet redirectServlet = new RedirectServlet();

        initMocks();

        expect(servletConfigMock.getServletContext()).andReturn(servletContextMock).atLeastOnce(); // dynaTrace Agent
        expect(servletContextMock.getContextPath()).andReturn(SERVLET_CONTEXT_PATH);
        expect(servletContextMock.getServletContextName()).andReturn(null).times(0,1);	// dynaTrace Agent
        expect(servletConfigMock.getInitParameter("prefix")).andReturn("redirect");
        expectDynaTraceAgent(requestMock);

        expect(requestMock.getHeader("range")).andReturn(null).anyTimes();
        expect(requestMock.getRequestURI()).andReturn(SERVLET_CONTEXT_PATH + "redirect").atLeastOnce(); // dynaTrace Agent
        expect(requestMock.getContextPath()).andReturn(null).anyTimes();					// may be called by ruxit agent
        expect(requestMock.getSession()).andReturn(null).anyTimes();						// may be called by ruxit agent
        expect(requestMock.getSession(false)).andReturn(null).anyTimes();					// may be called by ruxit agent
        expect(requestMock.getAttribute(anyString())).andReturn(null).anyTimes();			// may be called by ruxit agent
        requestMock.setAttribute(anyString(), anyInt());									// may be called by ruxit agent
        expectLastCall().anyTimes();

        responseMock.setHeader("X-ruxit-JS-Agent", "true");
        expectLastCall().anyTimes();
        responseMock.addCookie(anyCookie());
        expectLastCall().times(0,1);

        responseMock.sendRedirect(SERVLET_CONTEXT_PATH + "/");
        expectLastCall();
        expect(servletConfigMock.getServletName()).andReturn(null).times(0,1);	// dynaTrace Agent
        expect(responseMock.isCommitted()).andReturn(false).times(0,1);	// dynaTrace Agent

        replayMocks();

        redirectServlet.init(servletConfigMock);
        redirectServlet.doGet(requestMock, responseMock);

        verifyMocks();
    }
}
