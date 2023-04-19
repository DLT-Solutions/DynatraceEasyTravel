package com.dynatrace.easytravel.thirdpartycontent;

import java.io.IOException;
import java.util.Random;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter to slow down loading of content served by URI pattern "/static/xxx".
 * Afterwards the request is forwarded to standard handler for URI "/xxx".
 * Example: loading of /static/image/easyTravel_banner.png will be first delayed
 * by Thread.sleep(). Afterwards the request will be forwarded internally to
 * /image/easyTravel_banner.png.
 *
 * @author peter.lang
 */
public class SlowdownFilter extends BaseFilter {

	private static final String URLPATTERN = "/static/";
	// private static final Logger log = LoggerFactory.make();
	private static final Random rnd = new Random(System.currentTimeMillis());

	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		String uri = request.getRequestURI();
		if (uri.contains(URLPATTERN)) {
			try {
				String forwardUri = uri.substring(URLPATTERN.length() - 1);
				Thread.sleep(500 + rnd.nextInt(2000));

				RequestDispatcher requestDispatcher = request.getRequestDispatcher(forwardUri);
				requestDispatcher.forward(request, response);

				return;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		chain.doFilter(request, response);
	}

}
