package com.dynatrace.easytravel.launcher.fancy;

/**
 * Base Class for classes which maintain a visibility callback which defines if the represented component is visible and enabled.
 *
 * @author richard.vogl
 */
public class VisibilityAware {

	private MenuVisibilityCallback visibility;

	public VisibilityAware(MenuVisibilityCallback visibilityCallback){
		this.visibility = visibilityCallback;
	}

	/**
	 * Sets the visibility callback for the menu page (menu page will be disabled if not visible).
	 * If set to null the page is always visible.
	 *
	 * @param visibility Callback or Null
	 * @author richard.vogl
	 */
	public void setVisibility(MenuVisibilityCallback visibility) {
		this.visibility = visibility;
	}

	/**
	 * Checks if the component is visible.
	 *
	 * @return true if component should be drawn, false otherwise.
	 * @author richard.vogl
	 */
	public boolean checkVisible(){
		return visibility == null || visibility.isVisible();
	}

	/**
	 * Checks if the component is enabled.
	 *
	 * @return true if component should be enabled, false otherwise.
	 * @author richard.vogl
	 */
	public boolean checkEnabled(){
		return visibility == null || visibility.isEnabled();
	}

}
