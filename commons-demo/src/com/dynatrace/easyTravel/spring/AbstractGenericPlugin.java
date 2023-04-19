package com.dynatrace.easytravel.spring;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Base class for all generic plugins.
 *
 * @author philipp.grasboeck
 */
public abstract class AbstractGenericPlugin extends AbstractPlugin implements GenericPlugin {

	private String[] extensionPoint;

	/**
	 * Actually executes the plugin.
	 * Invokes doExecute() only if the path matches, and the plugin is still enabled.
	 * This query avoids a disabled plugin still hanging in a PluginList, pending to be removed
	 * at the next interval, to receive doExecute() after LIFECYCLE_PLUGIN_DISABLE.
	 */
	@Override
	public final Object execute(String location, Object... context) {
		if (PluginLifeCycle.pathMatches(this, location, false)) {
			if ((isEnabled() && isEnabledForCurrentHost()) || location.startsWith(PluginConstants.LIFECYCLE)) {
				PluginLifeCycle.logPluginExecute(location, this);
				return doExecute(location, context);
			}
		}
		return null;
	}

	/**
	 * Really execute the plugin.
	 * This method is only invoked if the given location path matches with this plugin's
	 * extensionPoint.
	 *
	 * @param location
	 * @param context
	 * @return
	 * @author philipp.grasboeck
	 */
	protected abstract Object doExecute(String location, Object... context);

	@Override
	public String[] getExtensionPoint() {
		return extensionPoint;
	}

	public void setExtensionPoint(String[] extensionPoint) {
		this.extensionPoint = ArrayUtils.clone(extensionPoint);
	}
}
