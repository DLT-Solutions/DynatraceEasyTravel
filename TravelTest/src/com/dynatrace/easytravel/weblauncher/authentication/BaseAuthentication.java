package com.dynatrace.easytravel.weblauncher.authentication;

import static com.dynatrace.easytravel.constants.BaseConstants.Security.JAAS_LOGIN_CONTEXT_NAME;
import static com.dynatrace.easytravel.constants.BaseConstants.Security.JAAS_LOGIN_MODULE_CONFIG_SYSTEM_PROPERTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.dynatrace.easytravel.utils.TestEnvironment;
import com.dynatrace.easytravel.weblauncher.jaas.WebLauncherCallbackHandler;

/**
 * cwpl-rorzecho
 */
public class BaseAuthentication {

    protected static LoginContext loginContext;

    private static String JAAS_TEST_LOGIN_MODULE = new File(TestEnvironment.TEST_DATA_PATH, "login-module-test.config").getPath();

    @BeforeClass
    public static void setUp() throws LoginException {
        System.setProperty(JAAS_LOGIN_MODULE_CONFIG_SYSTEM_PROPERTY, JAAS_TEST_LOGIN_MODULE);
        loginContext = new LoginContext(JAAS_LOGIN_CONTEXT_NAME, new WebLauncherCallbackHandler("admin", "adminpass"));
        assertNotNull(loginContext);
        loginContext.login();
    }

    @AfterClass
    public static void clear() throws LoginException {
        System.clearProperty(JAAS_LOGIN_MODULE_CONFIG_SYSTEM_PROPERTY);
        loginContext.logout();
        assertEquals(loginContext.getSubject().getPrincipals().size(), 0);
    }
}
