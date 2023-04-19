package com.dynatrace.easytravel.spring;

/**
 * Simple interface for generic plugins that are plugged in
 * without specific handling.
 *
 * Added extensionPoint semantics:
 * All generic plugins must provide an extension point, specifying
 * the plugin point where they want to plug in.
 * A plugin point is some location in the application.
 *
 * A generic plugin may be interested in several plugin points; this
 * can be achieved by providing a comma-separated list like:
 * 'frontend.point1,backend.point2'
 * Wild cards (*) are also allowed, e.g. to hierarchically catch all plugin
 * points starting with backend: 'backend.*'
 * Combinig the two is also possible: 'frontend.point1,backend.*'
 *
 * @author dominik.stadler
 */
public interface GenericPlugin extends Plugin {

	/**
	 * Execute the plugin action. The context carries information which is
	 * specific to the place where this plugin is invoked.
	 *
	 * @param location A string that contains information about from where
	 * 					the plugin is invoked.
	 * @param context Depending on the location some type of object with additional
	 * 					information or entry-points is provided.
	 *
	 * @author dominik.stadler
	 */
	public Object execute(String location, Object... context);

	/**
	 * Returns the extension point this plugin is interested in.
	 *
	 * @return
	 * @author philipp.grasboeck
	 */
	public String[] getExtensionPoint();
}
