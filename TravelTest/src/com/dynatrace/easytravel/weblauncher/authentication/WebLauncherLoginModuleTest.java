package com.dynatrace.easytravel.weblauncher.authentication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.dynatrace.easytravel.tomcat.FileUserServiceProvider;
import com.dynatrace.easytravel.weblauncher.jaas.WebLauncherLoginModule;

/**
 * cwpl-rorzecho
 */
public class WebLauncherLoginModuleTest {

    @Test
    public void userServiceProviderNoOptionsTest() {
        WebLauncherLoginModule webLauncherLoginModule = new WebLauncherLoginModule();

        assertTrue(webLauncherLoginModule.getUserServiceProvider(new HashMap<String, Object>()) instanceof FileUserServiceProvider);
        assertTrue(webLauncherLoginModule.getUserServiceProvider(null) instanceof FileUserServiceProvider);
    }

    @Test
    public void userServiceProviderEmptyOptoinTest() {
        WebLauncherLoginModule webLauncherLoginModule = new WebLauncherLoginModule();
        Map<String, Object> options = new HashMap<>();
        options.put(WebLauncherLoginModule.USER_SERVICE_PROVIDER_CLASS_OPTION, "");

        assertTrue(webLauncherLoginModule.getUserServiceProvider(options) instanceof FileUserServiceProvider);
    }

    @Test
    public void brokenUserServiceProviderClassNameTest() {
        WebLauncherLoginModule webLauncherLoginModule = new WebLauncherLoginModule();
        Map<String, Object> options = new HashMap<>();
        options.put(WebLauncherLoginModule.USER_SERVICE_PROVIDER_CLASS_OPTION, "DummyServiceProvider");

        assertTrue(webLauncherLoginModule.getUserServiceProvider(options) instanceof FileUserServiceProvider);
    }

    @Test
    public void correctUserServiceProviderClassNameTest() {
        WebLauncherLoginModule webLauncherLoginModule = new WebLauncherLoginModule();
        Map<String, Object> options = new HashMap<>();
        options.put(WebLauncherLoginModule.USER_SERVICE_PROVIDER_CLASS_OPTION, "com.dynatrace.easytravel.weblauncher.authentication.DummyServiceProvider");

        assertFalse(webLauncherLoginModule.getUserServiceProvider(options) instanceof FileUserServiceProvider);
        assertTrue(webLauncherLoginModule.getUserServiceProvider(options) instanceof DummyServiceProvider);
    }
}
