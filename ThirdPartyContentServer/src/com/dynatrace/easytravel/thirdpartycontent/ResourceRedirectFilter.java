package com.dynatrace.easytravel.thirdpartycontent;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;

/**
 * This filter redirects resources which start with certain URI pattern "/redirect-resource/[0-9]+".
 * As this pattern gets completely removed, it can be used to request the same resource several times
 * (just increase number within pattern).
 *
 * Example:
 * URI "/redirect-resource/42/image/cdn_image.png" will be redirected to "/image/cdn_image.png"
 *
 * @author cwat-sreiting
 */
public class ResourceRedirectFilter extends BaseFilter {

	private static final String URLPATTERN = "/resource-redirect/[0-9]+/";
	private static final Logger log = LoggerFactory.make();

	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		String uri = request.getRequestURI();
		String forwardUri = uri.replaceAll(URLPATTERN, "/");
		log.debug(String.format("forward request %s to %s", uri, forwardUri));

		RequestDispatcher requestDispatcher = request.getRequestDispatcher(forwardUri);
		requestDispatcher.forward(request, response);
		return;

	}

}
