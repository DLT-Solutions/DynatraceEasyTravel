package com.dynatrace.easytravel.weblauncher.security;

import static com.dynatrace.easytravel.constants.BaseConstants.Security.LOGIN_SERVLET;
import static com.dynatrace.easytravel.constants.BaseConstants.Security.LOGOUT_SERVLET;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.plugin.restore.AbstractPluginStateRestore;
import com.dynatrace.easytravel.launcher.plugin.restore.RestorePointHolder;
import com.dynatrace.easytravel.launcher.plugin.restore.ScenarioStateRestore;
import com.dynatrace.easytravel.launcher.security.UserPrincipal;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

/**
 * @author cwpl-rorzecho
 */
@WebServlet(name = LOGOUT_SERVLET, urlPatterns = {"/" + LOGOUT_SERVLET})
public class LogoutServlet extends HttpServlet {
	/**
	 *
	 */
	private static final long serialVersionUID = 6092328845060715110L;
	private static final Logger LOGGER = LoggerFactory.make();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
        boolean restoreState = false;
		if (session != null) {
            restoreState = restoreState(session);
            doRestoreState(restoreState);
            Launcher.setLoggedInUser(new UserPrincipal("none"));
            HttpSessionManager.logoutFromRapSession(session);
            HttpSessionManager.removeStoredHttpSession(session.getId());
            invalidateCookies(request, response);
            session.invalidate();
            redirectLoginPage(response);
            LOGGER.info(TextUtils.merge("Logout from httpSessionId: {0} performed", session.getId()));
        }
    }

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	private void redirectLoginPage(HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect(LOGIN_SERVLET);
	}

    private void invalidateCookies(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] c = request.getCookies();
        for (Cookie cookie : c) {
            if (cookie.getName().equals("JSESSIONID")) {
                cookie.setMaxAge(0);
                LOGGER.info(TextUtils.merge("Invalidate cookie: {0}", cookie.getName()));
                response.addCookie(cookie);
            }
        }
    }

    private boolean restoreState(HttpSession session) {
        if (isAdmin()) {
            return false;
        } else {
            return (Boolean) session.getAttribute("restore");
        }
    }

    private void doRestoreState(boolean restoreState) {
        if (restoreState) {
            if (ScenarioStateRestore.isScenarioChagned()) {
                AbstractPluginStateRestore.revertPlugins();
                LOGGER.info(TextUtils.merge("Restoring default scenario: {0}", RestorePointHolder.getInstance().getScenarioRestorePoint()));
                ScenarioStateRestore.revertScenario();
            } else {
                LOGGER.info("Restoring default plugins state");
                AbstractPluginStateRestore.revertPlugins();
            }
        }
    }

    private boolean isAdmin() {
        return Launcher.getLoggedInUser() != null && Launcher.getLoggedInUser().equals(new UserPrincipal("admin"));
    }
}
