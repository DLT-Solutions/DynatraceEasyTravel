/**
 *
 */
package com.dynatrace.easytravel.launcher;

import org.eclipse.swt.widgets.Shell;

import com.dynatrace.easytravel.launcher.engine.CloseCallback;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * @author tomasz.wieremjewicz
 * @date 12 gru 2019
 *
 */
public class LauncherUIHeadless extends LauncherUI {
	private static final Logger LOGGER = LoggerFactory.make();

	public LauncherUIHeadless() {
		this.setUiType(LauncherUIType.HEADLESS);
	}

	@Override
	public void messageBox(Shell parent, int flags, String title, String message, CloseCallback callback) {
		throw new UnsupportedOperationException("Cannot show dialog boxes in headless mode: " + title + "/" + message);
	}

	@Override
	public void openURL(String url) {
		throw new UnsupportedOperationException("Cannot show URLs in headless mode: " + url);
	}

	@Override
	public void shutdown() {
		throw new UnsupportedOperationException("Shutdown not implemented yet!");
	}
}
