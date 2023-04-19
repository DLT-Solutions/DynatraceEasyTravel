package com.dynatrace.easytravel.launcher.fancy;


/**
 * Provides action to be run when menu entry is selected.
 *
 * @author richard.vogl
 */
public interface MenuActionCallback {

	/**
	 * Executes actions for menu entry.
	 *
	 * @author richard.vogl
	 */
	void run();

    /**
     * Stop running the action.
     * 
     * @author martin.wurzinger
     */
    void stop();

    /**
     * Sets the text which is used to visualize the action.
     * 
     * @param text the text which is used to visualize the action
     * @author roland.mungenast
     */
	void setText(String text);

	/**
	 * Returns the text which is used to visualize the action.
	 *
	 * @return the text which is used to visualize the action.
	 * @author roland.mungenast
	 */
	String getText();

    Boolean isEnabled();

}
