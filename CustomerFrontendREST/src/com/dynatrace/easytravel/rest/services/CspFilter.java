package com.dynatrace.easytravel.rest.services;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

@WebFilter("/*")
public class CspFilter implements Filter {
	private static final Logger LOGGER = LoggerFactory.make();

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletResponse httpResponse = (HttpServletResponse) response;
		httpResponse.setHeader("Content-Security-Policy-Report-Only", "style-src 'unsafe-inline'");
		chain.doFilter(request, httpResponse);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}
}
