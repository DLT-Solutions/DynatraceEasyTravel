package com.dynatrace.easytravel.launcher.fancy;



/**
 * Interface to encapsulate the replacing of pages in the MenuComponent
 * to make things easier to test.
 *
 * @author dominik.stadler
 */
public interface PageProvider {
	/**
	 * Replace the page at the id (0-based) with the
	 * new page and redraw the menu component.
	 *
	 * @param id The id of the page to replace
	 * @param page The new page to use
	 *
	 * @author dominik.stadler
	 */
	public abstract void replacePage(final int id, final MenuPage page);

	/**
	 * Returns the current number of pages that are contained in this MenuComponent.
	 *
	 * @return
	 * @author dominik.stadler
	 */
	public abstract int getPageCount();
}
