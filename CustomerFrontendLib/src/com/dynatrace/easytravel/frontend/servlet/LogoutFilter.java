package com.dynatrace.easytravel.frontend.servlet;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.CallbackRunnable;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

// handles a logout request (j_invalidate_session)
public class LogoutFilter extends BaseFilter {

	public static final String INVALIDATE_SESSION = "j_invalidate_session";

	public static final String LOGOUT_PAGE = "logout.jsf";

	private static final Logger log = LoggerFactory.make();

	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		String redirectURI = removeURISuffix(request.getRequestURI(), INVALIDATE_SESSION);
		boolean logoutRequest = redirectURI != null;
		if (log.isDebugEnabled()) log.debug("logoutRequest: " + logoutRequest + ", uri=" + request.getRequestURI()+  ",redirect=" +redirectURI);

		if (logoutRequest) {
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.invalidate();
			}
			if (request.getParameter("clear") != null) { // perform cleanup
				CallbackRunnable.CLEANUP.run();
			}
			String referer = request.getHeader("referer");
			String host = request.getHeader("host");
			if(referer != null && host != null){
				redirectURI = (referer.startsWith("https")) ? TextUtils.merge("https://{0}/{1}", host, LOGOUT_PAGE) : TextUtils.merge("http://{0}/{1}", host, LOGOUT_PAGE);
			} else {
				redirectURI = redirectURI + LOGOUT_PAGE;
			}
			response.setContentType("text/html");
			if (!response.isCommitted()) {
				response.sendRedirect(redirectURI);
			}
			return;
		}

		chain.doFilter(request, response);
	}
}
