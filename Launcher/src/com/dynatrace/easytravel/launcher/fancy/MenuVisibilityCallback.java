package com.dynatrace.easytravel.launcher.fancy;

/**
 * Provides visibility check for Menu Items
 *
 * @author richard.vogl
 */
public interface MenuVisibilityCallback {

	/**
	 * Returns <code>true</code> if the calling menu item should be enabled
	 *
	 * @return
	 * @author richard.vogl
	 */
	boolean isEnabled();

	/**
	 * Returns <code>true</code> if the calling menu item should be visible
	 *
	 * @return
	 * @author richard.vogl
	 */
	boolean isVisible();

}
