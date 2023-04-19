package com.dynatrace.easytravel.weblauncher.authentication;

import static com.dynatrace.easytravel.constants.BaseConstants.Security.SUBJECT_SESSION_ATTRIBUTE;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.security.Principal;
import java.util.Calendar;
import java.util.Date;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpSession;

import org.junit.Test;

import com.dynatrace.easytravel.launcher.security.PasswordPrincipal;
import com.dynatrace.easytravel.launcher.security.RolePrincipal;
import com.dynatrace.easytravel.launcher.security.UserPrincipal;
import com.dynatrace.easytravel.weblauncher.beans.LoggedInUserBean;

/**
 * cwpl-rorzecho
 */
public class LoggedInUserBeanTest extends BaseAuthentication {

    @Test
    public void subjectTest() throws LoginException {
        Subject subject = loginContext.getSubject();
        assertNotNull(subject);
        assertEquals(subject.getPrincipals().size(), 3);

        for (Principal principal : subject.getPrincipals()) {
            if (principal instanceof UserPrincipal) {
                assertEquals(principal.getName(), "admin");
            } else if (principal instanceof RolePrincipal) {
                assertEquals(principal.getName(), "demo");
            } else if (principal instanceof PasswordPrincipal) {
                assertEquals(principal.getName(), "adminpass");
            }
        }

    }

    @Test
    public void loggedInUserBeanTest() {
        HttpSession httpSession = createMock(HttpSession.class);

        Subject subject = loginContext.getSubject();

        expect(httpSession.getAttribute(SUBJECT_SESSION_ATTRIBUTE)).andReturn(subject).anyTimes();
        expect(httpSession.getLastAccessedTime()).andReturn(System.currentTimeMillis()).anyTimes();

        replay(httpSession);

        assertNotNull(httpSession.getAttribute(SUBJECT_SESSION_ATTRIBUTE));

        LoggedInUserBean loggedInUserBean = new LoggedInUserBean(httpSession);

        assertEquals(loggedInUserBean.getUserName(), "admin");

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());

        assertTrue(loggedInUserBean.getLastAccessTime().contains(String.valueOf(c.get(Calendar.YEAR))));

        httpSession = null;

        LoggedInUserBean loggedInUserBean2 = new LoggedInUserBean(httpSession);

        assertEquals(loggedInUserBean2.getUserName(), "Guest");
    }

}
