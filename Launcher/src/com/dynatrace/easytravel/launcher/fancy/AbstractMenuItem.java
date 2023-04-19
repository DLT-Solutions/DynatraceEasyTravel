package com.dynatrace.easytravel.launcher.fancy;

import org.eclipse.swt.widgets.Composite;


/**
 * Base Class for menu items for fancy menu.
 * MenuItems are the data classes for items in the page area of the fancy menu.
 * The visualization is done by the corresponding classes extending {@link AbstractMenuItemComponent}.
 *
 * @author richard.vogl
 */

public abstract class AbstractMenuItem extends VisibilityAware implements Filterable {

	protected FilterTaskParams filterTaskParams;

	/**
	 * Constructs the MenuItemComponent
	 *
	 * @param visibility
	 * @author richard.vogl
	 */
	public AbstractMenuItem(MenuVisibilityCallback visibility){
		super(visibility);
	}

	/**
	 * Creates the component which visualizes this menu item. Components have to
	 * extend {@link AbstractMenuItemComponent}.
	 *
	 * @param parent The parent composite for the AbstractMenuItemComponent
	 * @param page The {@link MenuPage} which holds the menu item.
	 * @return
	 * @author richard.vogl
	 */
    public abstract AbstractMenuItemComponent<? extends AbstractMenuItem> createComponent(Composite parent, MenuPage page, LayoutCallback layoutCallback);

	@Override
	public void applyFilter(FilterTaskParams filterTaskParams) {
		// just store filter params, actual filtering is done via checkVisible
		this.filterTaskParams = filterTaskParams;
	}

	@Override
	public void clearFilter() {
		this.filterTaskParams = null;
	}

	@Override
	public boolean checkVisible() {
		return this.checkFilter() && super.checkVisible();
	}

	/**
	 * @return true if filterTaskParams match with this object
	 * @author christoph.neumueller
	 */
	protected abstract boolean checkFilter();
}
