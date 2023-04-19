package com.dynatrace.easytravel.frontend.servlet;

import static org.easymock.EasyMock.*;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import com.dynatrace.easytravel.spring.GenericPlugin;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.spring.SpringUtils;

public class PluginServletTest extends ServletTestBase {

    private static final String SERVLET_CONTEXT_PATH = "/plugins/";
    private static final String REQUEST_URI = "/plugins/prefix/PluginName/execute";


    private PluginServlet initServlet() throws Exception {
        initMocks();
        PluginServlet pluginServlet = new PluginServlet();
        expect(servletConfigMock.getServletContext()).andReturn(servletContextMock).times(0, 1);
        expect(servletContextMock.getContextPath()).andReturn(SERVLET_CONTEXT_PATH);
        expect(servletConfigMock.getInitParameter("prefix")).andReturn("prefix/");
        expect(servletConfigMock.getInitParameter("dynamicSuffix")).andReturn("dynamic,Suffix");
        expect(servletConfigMock.getInitParameter("downloadSuffix")).andReturn("download,Suffix");
        expect(servletConfigMock.getInitParameter("redirectURL")).andReturn("redirectURL");
        expect(servletConfigMock.getInitParameter("varRegex")).andReturn("varRegex");
        expect(servletConfigMock.getInitParameter("exprRegex")).andReturn("exprRegex");
        expect(servletConfigMock.getInitParameter("ifRegex")).andReturn("ifRegex");

        replayMocks();
        pluginServlet.init(servletConfigMock);
        verifyMocks();
        resetMocks();
        return pluginServlet;
    }


    @Test
    public void testInit() throws Exception {
        initServlet();
    }

    @Test
    public void testDoGet() throws Exception {

    	GenericPlugin plugin = createMock(GenericPlugin.class);

        PluginServlet pluginServlet = initServlet();

        expectDynaTraceAgent(requestMock);

        expect(requestMock.getRequestURI()).andReturn(REQUEST_URI).atLeastOnce();			// may be called more than once due to ruxit agent
        expect(requestMock.getContextPath()).andReturn(null).anyTimes();					// may be called by ruxit agent
        expect(requestMock.getSession()).andReturn(null).anyTimes();						// may be called by ruxit agent
        expect(requestMock.getSession(false)).andReturn(null).anyTimes();					// may be called by ruxit agent
        expect(requestMock.getHeader("range")).andReturn(null).anyTimes();
        expect(requestMock.getParameterMap()).andReturn(new HashMap<String, String[]>());
        expect(requestMock.getAttribute(anyString())).andReturn(null).anyTimes();			// may be called by ruxit agent
        requestMock.setAttribute(anyString(), anyInt());									// may be called by ruxit agent
        expectLastCall().anyTimes();

		expect(plugin.isActivatable()).andReturn(true).anyTimes();
        expect(plugin.getName()).andReturn("PluginName").anyTimes();
        expect(plugin.getGroupName()).andReturn("TestGroup").anyTimes();
        expect(plugin.getCompatibility()).andReturn("Both").anyTimes();
        expect(plugin.getDescription()).andReturn("").anyTimes();
        expect(plugin.execute(eq(PluginConstants.FRONTEND_PAGE_EXECUTE), anyObject(String.class))).andReturn(new byte[] {1, 2, 3, 4});
        expect(plugin.getExtensionPoint()).andReturn(new String[] {PluginConstants.FRONTEND_PAGE});
        expect(plugin.isEnabled()).andReturn(true);
        expect(plugin.getExtensionPoint()).andReturn(new String[] {PluginConstants.FRONTEND_PAGE}).anyTimes();

        expect(responseMock.getOutputStream()).andReturn(new ServletOutputStream() {
			@Override
			public void write(int b) throws IOException {
				//do nothing, just a dummy method
			}
		});
        responseMock.setContentType("text/html; charset=UTF-8");
        expectLastCall().times(0, 1);	// ruxit agent
        responseMock.setHeader("Content-Type", "text/html; charset=UTF-8");
        expectLastCall().times(0, 1);

        // called by ruxit Agent
        responseMock.setHeader("X-ruxit-JS-Agent", "true");
        expectLastCall().times(0,1);
        responseMock.addCookie(anyCookie());
        expectLastCall().times(0,1);

//        responseMock.sendRedirect(SERVLET_CONTEXT_PATH + "redirectURL");
//        expectLastCall();

        expect(servletConfigMock.getServletContext()).andReturn(servletContextMock).anyTimes();		// called by ruxit agent

        replayMocks();
        replay(plugin);

        // make the plugin available
        SpringUtils.getPluginHolder().addPlugin(plugin);
    	SpringUtils.getPluginStateProxy().setPluginEnabled("PluginName", true);

        pluginServlet.doGet(requestMock, responseMock);

        verifyMocks();
        verify(plugin);
    }
}
