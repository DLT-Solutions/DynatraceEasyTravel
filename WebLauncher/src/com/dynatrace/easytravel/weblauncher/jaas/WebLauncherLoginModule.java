package com.dynatrace.easytravel.weblauncher.jaas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import com.dynatrace.easytravel.launcher.security.PasswordPrincipal;
import com.dynatrace.easytravel.launcher.security.RolePrincipal;
import com.dynatrace.easytravel.launcher.security.UserPrincipal;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.tomcat.FileUserServiceProvider;
import com.dynatrace.easytravel.tomcat.UserServiceProvider;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

/**
 * @author cwpl-rorzecho
 */
public class WebLauncherLoginModule implements LoginModule {
    private static final Logger LOGGER = LoggerFactory.make();

    public static final String USER_SERVICE_PROVIDER_CLASS_OPTION = "userServiceProviderClass";

    private CallbackHandler handler;
    private Subject subject;
    private String username = null;
    private char[] password = null;
    private List<String> roles;

    private UserPrincipal userPrincipal;
    private PasswordPrincipal passwordPrincipal;
    private RolePrincipal rolePrincipal;

	private UserServiceProvider webLauncherUsers;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.handler = callbackHandler;
        this.subject = subject;
        this.webLauncherUsers = getUserServiceProvider(options);
	}

    @Override
    public boolean login() throws LoginException {
        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("login");
        callbacks[1] = new PasswordCallback("password", true);
        try {
            handler.handle(callbacks);
            username = ((NameCallback) callbacks[0]).getName();
            password = ((PasswordCallback) callbacks[1]).getPassword();


			if (webLauncherUsers.verifyUser(username, password)) {
				roles = new ArrayList<String>();
				roles.add(webLauncherUsers.getUserRole(username));
				return true;
			}

            throw new LoginException("Authentication failed");
        } catch (IOException e) {
            throw new LoginException(e.getMessage());
        } catch (UnsupportedCallbackException e) {
            throw new LoginException(e.getMessage());
        }
    }

    @Override
    public boolean commit() throws LoginException {
        userPrincipal = new UserPrincipal(username);
        if (!subject.getPrincipals().contains(userPrincipal)) {
            subject.getPrincipals().add(userPrincipal);
            LOGGER.debug("User principal added: " + userPrincipal);
        }

        passwordPrincipal = new PasswordPrincipal(new String(password));
        if (!subject.getPrincipals().contains(passwordPrincipal)) {
            subject.getPrincipals().add(passwordPrincipal);
			LOGGER.debug("Password principal added: " + passwordPrincipal);
		}

        // populate subject with roles
        for (String role : roles) {
            rolePrincipal = new RolePrincipal(role);
            if (!subject.getPrincipals().contains(rolePrincipal)) {
                subject.getPrincipals().add(rolePrincipal);
                LOGGER.debug("Role principal added: " + rolePrincipal);
            }
        }

        LOGGER.info("Login Subject was successfully populated with principals and roles");
        return true;
    }

	@Override
	public boolean abort() throws LoginException {
		return false;
	}

    @Override
    public boolean logout() throws LoginException {
        LOGGER.info("Loggout has been performed for user: " + userPrincipal.getName());
        subject.getPrincipals().clear();
        return true;
    }

    /**
     * Read option {@link USER_SERVICE_PROVIDER_CLASS_OPTION} from login-module.config file for specified {@link UserServiceProvider}
     * Property should be specified as fully qualified class name.
     *
     * @param options
     * @return UserServiceProvider instance class
     */
    public UserServiceProvider getUserServiceProvider(Map<String, ?> options) {
        UserServiceProvider userServiceProvider;

        if (isUserServiceProviderOptionAvailable(options)) {
            String fullyQualifiedClassName = String.valueOf(options.get(USER_SERVICE_PROVIDER_CLASS_OPTION));
            try {
                userServiceProvider = getUserServiceProviderClass(fullyQualifiedClassName).newInstance();
                LOGGER.info(TextUtils.merge("UserServiceProvider for class {0} was created", fullyQualifiedClassName));
            } catch (Exception e) {
                LOGGER.error(TextUtils.merge("Cannot create UserServiceProvider for class {0}", fullyQualifiedClassName), e);
                return getDefaultUserServiceProvider();
            }
        } else {
            return getDefaultUserServiceProvider();
        }

        return userServiceProvider;
    }

    @SuppressWarnings("unchecked")
	private Class<? extends UserServiceProvider> getUserServiceProviderClass(String fullyQualifiedClassName) throws ClassNotFoundException {
        return (Class<? extends UserServiceProvider>) Class.forName(fullyQualifiedClassName);
    }

    private UserServiceProvider getDefaultUserServiceProvider() {
        LOGGER.info("Default FileUserServiceProvider was created");
        return new FileUserServiceProvider();
    }

    private boolean isUserServiceProviderOptionAvailable(Map<String, ?> options) {
        return options != null && options.containsKey(USER_SERVICE_PROVIDER_CLASS_OPTION) ? true : false;
    }

}