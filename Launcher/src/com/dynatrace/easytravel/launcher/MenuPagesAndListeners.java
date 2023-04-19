/**
 *
 */
package com.dynatrace.easytravel.launcher;

import java.util.List;

import com.dynatrace.easytravel.launcher.config.ScenarioConfiguration;
import com.dynatrace.easytravel.launcher.fancy.MenuPage;

/**
 * @author tomasz.wieremjewicz
 * @date 30 gru 2019
 *
 */
public class MenuPagesAndListeners {
	private List<MenuPage> menuPages;
	private List<ScenarioConfiguration> listeners;

	/**
	 *
	 */
	public MenuPagesAndListeners(List<MenuPage> menuPages, List<ScenarioConfiguration> listeners) {
		this.menuPages = menuPages;
		this.listeners = listeners;
	}

	/**
	 * @return the menuPages
	 */
	public List<MenuPage> getMenuPages() {
		return menuPages;
	}
	/**
	 * @param menuPages the menuPages to set
	 */
	public void setMenuPages(List<MenuPage> menuPages) {
		this.menuPages = menuPages;
	}
	/**
	 * @return the listeners
	 */
	public List<ScenarioConfiguration> getListeners() {
		return listeners;
	}
	/**
	 * @param listeners the listeners to set
	 */
	public void setListeners(List<ScenarioConfiguration> listeners) {
		this.listeners = listeners;
	}
}
