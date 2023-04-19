package com.dynatrace.easytravel.weblauncher.authentication;

import static com.dynatrace.easytravel.constants.BaseConstants.Security.SUBJECT_SESSION_ATTRIBUTE;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpSession;

import org.junit.Test;

import com.dynatrace.easytravel.weblauncher.security.HttpSessionManager;

/**
 * cwpl-rorzecho
 */
public class HttpSessionManagerTest extends BaseAuthentication {

    @Test
    public void subjectTest() throws LoginException {
        HttpSession httpSession = createMock(HttpSession.class);
        expect(httpSession.getAttribute(SUBJECT_SESSION_ATTRIBUTE)).andReturn(loginContext.getSubject()).anyTimes();
        expect(httpSession.getLastAccessedTime()).andReturn(System.currentTimeMillis()).anyTimes();

        replay(httpSession);

        Subject subject = HttpSessionManager.getSubject(httpSession);

        assertEquals(subject.getPrincipals().size(), 3);
    }

    @Test
    public void subjectTestForNullHttpSession() {
        HttpSession httpSession = null;

        Subject subject = HttpSessionManager.getSubject(httpSession);

        assertEquals(subject.getPrincipals().size(), 0);
    }

    @Test
    public void subjectTestForNullSubject() {
        HttpSession httpSession = createMock(HttpSession.class);
        expect(httpSession.getAttribute(SUBJECT_SESSION_ATTRIBUTE)).andReturn(null).anyTimes();

        replay(httpSession);

        Subject subject = HttpSessionManager.getSubject(httpSession);

        assertEquals(subject.getPrincipals().size(), 0);
    }

}
