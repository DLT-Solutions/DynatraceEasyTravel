package com.dynatrace.easytravel.launcher.fancy;

import org.eclipse.swt.widgets.Composite;


/**
 *
 * @author richard.vogl
 */
public interface PageSelectionListener {

	/**
	 * Is called whenever a page of the menu is selected.
	 *
	 * @param source The page button pressed to select the corresponding page
	 * @param index The index of the page and the button.
	 * @author richard.vogl
	 */
	void pageSelected(Composite source, int index);

}
