package com.dynatrace.easytravel.config;


/**
 * Classes implementing this interface are informed whenever the {@link EasyTravelConfig} settings are reloaded
 * 
 * @author stefan.moschinski
 */
public interface ConfigChangeListener {

	/**
	 * Method is called when a new {@link EasyTravelConfig} is loaded.
	 * A new load does not imply that the newCfg has different properties than the 
	 * oldCfg. The class implementing this method may check for a change
	 * using the {@link EasyTravelConfig#equals(Object)} method. 
	 * 
	 * @param oldCfg {@link EasyTravelConfig} that was active, may be <code>null</code>
	 * @param newCfg {@link EasyTravelConfig} that is now active
	 * @author stefan.moschinski
	 */
	void notifyConfigLoaded(EasyTravelConfig oldCfg, EasyTravelConfig newCfg);
}
