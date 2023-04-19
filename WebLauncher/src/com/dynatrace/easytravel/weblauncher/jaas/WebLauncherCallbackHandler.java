package com.dynatrace.easytravel.weblauncher.jaas;

import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

import javax.security.auth.callback.*;

import java.io.IOException;

/**
 * CallbackHandler for {@link WebLauncherLoginModule}
 *
 * @author cwpl-rorzecho
 */
public class WebLauncherCallbackHandler implements CallbackHandler {
	private static final Logger LOGGER = LoggerFactory.make();

	private final String name;
	private final String password;

	public WebLauncherCallbackHandler(String name, String password) {
		this.name = name;
		this.password = password;
	}

	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		LOGGER.debug("Callback Handler - handle called");
		for (int i = 0; i < callbacks.length; i++) {
			if (callbacks[i] instanceof NameCallback) {
				((NameCallback) callbacks[i]).setName(name);
			} else if (callbacks[i] instanceof PasswordCallback) {
				((PasswordCallback) callbacks[i]).setPassword(password.toCharArray());
			} else {
				throw new UnsupportedCallbackException(callbacks[i], "The submitted Callback is unsupported");
			}
		}
	}

}
