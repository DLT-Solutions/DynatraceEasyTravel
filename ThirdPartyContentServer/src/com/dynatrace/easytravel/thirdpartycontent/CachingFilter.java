package com.dynatrace.easytravel.thirdpartycontent;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;


/**
 * This filter sets cache-control header for each requested resource to 3 hours
 *
 * @author cwat-sreiting
 */
public class CachingFilter extends BaseFilter {

	private static final String URLPATTERN = "/caching/";
	private static final Logger log = LoggerFactory.make();

	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		long cacheSeconds = TimeUnit.HOURS.toSeconds(3);
		response.addHeader("Cache-Control", "private,public,max-age=" + cacheSeconds);

		String uri = request.getRequestURI();
		String forwardUri = uri.replaceAll(URLPATTERN, "/");
		log.debug(String.format("forward request %s to %s", uri, forwardUri));

		RequestDispatcher requestDispatcher = request.getRequestDispatcher(forwardUri);
		requestDispatcher.forward(request, response);
		return;
	}

}
