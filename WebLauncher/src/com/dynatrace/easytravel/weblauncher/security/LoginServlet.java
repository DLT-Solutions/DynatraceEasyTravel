package com.dynatrace.easytravel.weblauncher.security;

import static com.dynatrace.easytravel.constants.BaseConstants.Security.*;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.launcher.security.RolePrincipal;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;
import com.dynatrace.easytravel.weblauncher.jaas.WebLauncherCallbackHandler;
import com.google.common.base.Strings;

import ch.qos.logback.classic.Logger;

/**
 * Main HttpServlet userd for Users Authentication
 *
 * @author cwpl-rorzecho
 */
@WebServlet(name = LOGIN_SERVLET, urlPatterns = {"/" + LOGIN_SERVLET})
public class LoginServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7064389339829737227L;
	private static final Logger LOGGER = LoggerFactory.make();

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UserContext userContext = UserContext.create(request);
		HttpSession session = request.getSession(true);
		LoginContext loginContext = null;

		if (userContext.isLoginPossible()) {
			final String username = userContext.getUserName();
			final String password = userContext.getUserPass();

			loginContext = doLogin(username, password);

			if (loginContext != null) {
				// set 1h for inactive interval
				session.setMaxInactiveInterval(3600);

				Subject subject = loginContext.getSubject();

				Set<Principal> principals = subject.getPrincipals();

				if (isAdminRole(principals) || HttpSessionManager.getStoredUserName().equals(username)) {
					HttpSessionManager.replaceHttpSession(session, subject);
					redirectTo(response, MAIN_SERVLET);
				} else if (!HttpSessionManager.isLoggedIn()) {
					HttpSessionManager.storeHttpSession(session, subject);
					redirectTo(response, MAIN_SERVLET);
				} else {
					dispatchLoginPage(request, response);
				}
			} else {
				dispatchLoginPage(request, response);
			}
		} else {
			userContext.emptyUserAttributes(request);
			dispatchLoginPage(request, response);
		}
	}

	private LoginContext doLogin(String username, String password) {
		LoginContext loginContext = null;

		try {
			loginContext = new LoginContext(JAAS_LOGIN_CONTEXT_NAME, new WebLauncherCallbackHandler(username, password));
			LOGGER.debug(TextUtils.merge("Creating LoginContext for user {0}", username));
		} catch (LoginException e) {
			LOGGER.error(TextUtils.merge("Cannot create LoginContext for user: {0}", username), e);
			return null;
		}

		try {
			loginContext.login();
			LOGGER.info(TextUtils.merge("Login user {0}", username));
		} catch (LoginException e) {
			LOGGER.error(TextUtils.merge("Login failed for user {0}", username));
			return null;
		}

		return loginContext;
	}


	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	private void dispatchLoginPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getRequestDispatcher(SERVICE_LOGIN_SERVLET).forward(request, response);
	}

	private void redirectTo(HttpServletResponse response, String page) {
		try {
			response.sendRedirect(page);
		} catch (IOException e) {
			LOGGER.error(TextUtils.merge("Cannot redirect request to page {0}", page));
		}
	}

	private static String getRequestParameter(String paramName, HttpServletRequest request) {
		return Strings.nullToEmpty(request.getParameter(paramName)).trim();
	}

	private boolean isAdminRole(Set<Principal> principals) {
		for (Principal principal : principals) {
			if (principal instanceof RolePrincipal) {
				return principal.getName().equals("admin");
			}
		}
		return false;
	}

	/**
	 * UserContect created from request params send from login form
	 */
	static class UserContext {
		private static final String PARAM_NAME_USER = "user";
		private static final String PARAM_NAME_PASS = "pass";

		private static final String ATTRIBUTE_IS_EMPTY_NAME = "isEmptyName";
		private static final String ATTRIBUTE_IS_EMPTY_PASS = "isEmptyPass";

		private String userName;
		private String userPass;

		private UserContext() {
		}

		public static UserContext create(HttpServletRequest request) {
			UserContext userContext = new UserContext();
			userContext.userName = getRequestParameter(PARAM_NAME_USER, request);
			userContext.userPass = getRequestParameter(PARAM_NAME_PASS, request);
			return userContext;
		}

		public String getUserName() {
			return userName;
		}

		public String getUserPass() {
			return userPass;
		}

		/**
		 * When user or pass or both parameters are empty set
		 * isEmptyName, isEmptyPass attribute to true
		 * @param request
		 */
		public void emptyUserAttributes(HttpServletRequest request) {
			if (StringUtils.isEmpty(userName)) {
				request.setAttribute(ATTRIBUTE_IS_EMPTY_NAME, Boolean.TRUE.toString());
			}

			if (StringUtils.isEmpty(userPass)) {
				request.setAttribute(ATTRIBUTE_IS_EMPTY_PASS, Boolean.TRUE.toString());
			}
		}

		public boolean isLoginPossible() {
			return !StringUtils.isEmpty(userName) && !StringUtils.isEmpty(userPass);
		}

	}

}
