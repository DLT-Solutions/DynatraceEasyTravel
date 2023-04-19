package com.dynatrace.easytravel.weblauncher.security;

import static com.dynatrace.easytravel.constants.BaseConstants.Security.SUBJECT_SESSION_ATTRIBUTE;

import java.security.Principal;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.rap.rwt.client.WebClient;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.rap.rwt.internal.service.UISessionImpl;

import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.LauncherUI;
import com.dynatrace.easytravel.launcher.security.UserPrincipal;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

/**
 * Class for managing HttpSession
 *
 * @author cwpl-rorzecho
 */
public class HttpSessionManager {
	private static final Logger LOGGER = LoggerFactory.make();

	private static final ConcurrentHashMap<String, HttpSession> storedHttpSession = new ConcurrentHashMap<String, HttpSession>();

	private HttpSessionManager() {
	}

	/**
	 * Check if someone is LoggedIn
	 *
	 * @param request
	 * @return
	 */
	public static boolean isLoggedIn(HttpServletRequest request) {
		return isLoggedIn(request.getSession(false));
	}

	public static boolean isLoggedIn(HttpSession session) {
        if (session == null) {
            return false;
        }
        return getSubject(session).getPrincipals().size() != 0;
	}

	public static boolean isLoggedIn() {
		return isLoggedIn(getStoredHttpSession());
	}

	public static boolean isLoggedIn(String username) {
		Subject subject = getSubject(getStoredHttpSession());

		if (subject != null) {
			for (Principal principal : subject.getPrincipals()) {
				if (principal instanceof UserPrincipal) {
					return principal.getName().equals(username);
				}
			}

		}
		return false;
	}

	/**
	 * Invalidate HttpSession
	 *
	 * @param session
	 */
	public static void logOut(HttpSession session) {
		if (session != null) {
			removeStoredHttpSession(session.getId());
			session.invalidate();
			LOGGER.info(TextUtils.merge("SessionId: {0} was invalidated", session.getId()));
		} else {
			for(LauncherUI ui : Launcher.getLauncherUIList()) {
				ui.logout();
			}
		}
	}

	/**
	 * Assign JAAS Subject to HttpSession
	 *
	 * @param httpSession
	 * @param subject
	 */
	public static void setSubject(HttpSession httpSession, Subject subject) {
		if (httpSession == null) {
			subject = null;
			throw new IllegalStateException("Cannot assign Subject to invalidate HttpSession");
		}

		httpSession.setAttribute(SUBJECT_SESSION_ATTRIBUTE, subject);

		LOGGER.info(TextUtils.merge("Setting Subject with Principals {0} to httpSessionId: {1}", subject.getPrincipals(), httpSession.getId()));
	}

	/**
	 * Store active HttpSession
	 *
	 * @param httpSession
	 */
	public static void storeHttpSession(HttpSession httpSession, Subject subject) {
        setRestorePluginsState(httpSession, true);
		storedHttpSession.put(httpSession.getId(), httpSession);
		setSubject(httpSession, subject);
	}

    /**
     * Set arrribute for restoring plugins state after user logout
     *
     * restore: true - restore plugins default state
     * restore: false - do not change plugins state,
     *
     * @param restore
     * @param httpSession
     */
    private static void setRestorePluginsState(HttpSession httpSession, boolean restore) {
        httpSession.setAttribute("restore", restore);
    }

	public static void replaceHttpSession(HttpSession httpSession, Subject subject) {
		if (storedHttpSession.size() != 0) {
            HttpSession storedHttpSession = getStoredHttpSession();
            // disallow resstoring plugins state
            setRestorePluginsState(storedHttpSession, false);
            logoutFromRapSession(storedHttpSession);
        }
        clearStoredHttpSession();
        // store HttpSession with ready for restore plugins state
        setRestorePluginsState(httpSession, true);
        storedHttpSession.put(httpSession.getId(), httpSession);
		setSubject(httpSession, subject);
    }

    public static void logoutFromRapSession(final HttpSession httpSession) {
        /*
        *  In Rap 2.3 distribution ui session attribute has been changed
        *
        * -  private static final String ATTR_UI_SESSION = UISessionImpl.class.getName();
        * +  private static final String ATTR_UI_SESSION = UISessionImpl.class.getName() + "#uisession:";
        *
        * */

        Enumeration<String> attributeNames = httpSession.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            if (attributeName.contains("org.eclipse.rap.rwt.internal.service.UISessionImpl")) {
                ((UISessionImpl) httpSession.getAttribute(attributeName)).exec(
                    new Runnable() {
                        @Override
                        public void run() {
                            new WebClient().getService(JavaScriptExecutor.class).execute("window.location.href=\"/logout\"");
                        }
                    });
            }
        }

    }

	public static HttpSession getStoredHttpSession() {
		for (HttpSession httpSession : storedHttpSession.values()) {
			return httpSession;
		}
		return null;
	}

	public static void removeStoredHttpSession(String sessionId) {
		storedHttpSession.remove(sessionId);
	}

	public static void clearStoredHttpSession() {
		storedHttpSession.clear();
	}

	/**
	 * Get JAAS Subject from HttpSession
	 *
	 * @param httpSession
	 * @return
	 */
	public static Subject getSubject(HttpSession httpSession) {
        if (httpSession == null) {
            LOGGER.debug("No valid HttpSession available");
            // return empty Subject
            return new Subject();
        }

        Subject subject = null;
        try {
            subject = (Subject) httpSession.getAttribute(SUBJECT_SESSION_ATTRIBUTE);
        } catch (IllegalStateException ex) {
            LOGGER.error(TextUtils.merge("Cannot get attribute from session {0}", httpSession.getId()), ex);
            removeStoredHttpSession(httpSession.getId());
        }

        return subject != null ? subject : new Subject();
	}

	public static String getStoredUserName() {
		if (getStoredHttpSession() != null) {
			for (Principal principal : getSubject(getStoredHttpSession()).getPrincipals()) {
				if (principal instanceof UserPrincipal) {
					return principal.getName();
				}
			}
		}

		return "Guest";
	}

	public static UserPrincipal getUserPrincipal(Subject subject) {
		for (Principal principal : subject.getPrincipals()) {
			if (principal instanceof UserPrincipal) {
				return (UserPrincipal) principal;
			}
		}
		return new UserPrincipal("Guest");
	}
}
