package com.dynatrace.easytravel.weblauncher;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.dynatrace.easytravel.frontend.servlet.ServletTestBase;
import com.dynatrace.easytravel.launcher.Launcher;


public class HttpRequestFilterTest {
	HttpRequestOriginFilter filter = new HttpRequestOriginFilter();

	HttpServletRequest request = createStrictMock(HttpServletRequest.class);
	HttpServletResponse response = createStrictMock(HttpServletResponse.class);
	FilterChain chain = createStrictMock(FilterChain.class);


	@Test
	public void test() throws IOException, ServletException {
		expect(request.getRemoteHost()).andReturn("host1");
		chain.doFilter(request, response);

		replay(request,response, chain);

		filter.init(null);
		filter.doFilter(request, response, chain);
		filter.destroy();

		assertEquals("host1", Launcher.getOrigin());

		verify(request, response, chain);
	}


	@Test
	public void testNullOrigin() throws IOException, ServletException {
		expect(request.getRemoteHost()).andReturn(null);
		chain.doFilter(request, response);

		replay(request,response, chain);

		filter.init(null);
		filter.doFilter(request, response, chain);
		filter.destroy();

		assertNull(Launcher.getOrigin());

		verify(request, response, chain);
	}

	@Test
	public void testDoFilter() throws IOException, ServletException {
        expect(request.getRequestURI()).andReturn(null).anyTimes(); // ruxit Agent

        ServletTestBase.expectDynaTraceAgent(request);
        expect(request.getAttribute(ServletTestBase.anyString())).andReturn(null).anyTimes();			// may be called by ruxit agent

        expect(request.getHeader("X-dynaTrace")).andReturn(null).anyTimes();	// ruxit Agent
        expect(request.getHeaderNames()).andReturn(null).anyTimes();	// ruxit Agent
        expect(request.getContextPath()).andReturn(null).anyTimes();	// ruxit Agent

		expect(request.getRemoteHost()).andReturn(null);
		chain.doFilter(request, response);

		replay(request,response, chain);

		filter.init(null);
		filter.doFilter((ServletRequest)request, (ServletResponse)response, chain);
		filter.destroy();

		assertNull(Launcher.getOrigin());

		verify(request, response, chain);
	}
}
