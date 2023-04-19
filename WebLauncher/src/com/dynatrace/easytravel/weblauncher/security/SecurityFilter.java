package com.dynatrace.easytravel.weblauncher.security;

import static com.dynatrace.easytravel.constants.BaseConstants.Security.LOGIN_SERVLET;

import java.io.IOException;

import javax.security.auth.Subject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dynatrace.easytravel.launcher.Launcher;

/**
 * SecurityFilter for tracking HttpSession Subject attribute
 *
 * @author cwpl-rorzecho
 */
public class SecurityFilter implements Filter {
	//private static final Logger LOGGER = LoggerFactory.make();

	protected FilterConfig filterConfig;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		HttpSession session = request.getSession(false);

		if (HttpSessionManager.isLoggedIn(session)) {
			Subject subject = HttpSessionManager.getSubject(session);
			Launcher.setLoggedInUser(HttpSessionManager.getUserPrincipal(subject));
			chain.doFilter(request, response);
		} else {
            dispatchLoginPage(request, response);
            chain.doFilter(request, response);
        }
	}

	@Override
	public void destroy() {
		// initially do nothing
	}

	private void dispatchLoginPage(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
		request.getRequestDispatcher(LOGIN_SERVLET).forward(request, response);
	}

}
