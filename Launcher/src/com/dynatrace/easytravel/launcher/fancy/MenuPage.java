package com.dynatrace.easytravel.launcher.fancy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.ScenarioMenuFactory;
import com.dynatrace.easytravel.launcher.engine.LaunchEngine;
import com.dynatrace.easytravel.launcher.fancy.custom.ScenarioMenuItemComponent;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;


/**
 * Menu page for fancy menu.
 *
 * @author richard.vogl
 */
public class MenuPage extends VisibilityAware implements Filterable {

	private static final Logger LOGGER = LoggerFactory.make();

	private String image;
	private String title;
	private MenuActionCallback action;
	private final List<AbstractMenuItem> items = new ArrayList<AbstractMenuItem>(10);
	private final List<AbstractMenuItemComponent<? extends AbstractMenuItem>> menuItemComponents = new ArrayList<AbstractMenuItemComponent<? extends AbstractMenuItem>>();
	private final List<FilterListener> filterListeners = new ArrayList<FilterListener>();
	private FilterTaskParams filterTaskParams;
	private boolean isFilterEnabled = false;

	/**
	 * Constructs a new MenuPage
	 *
	 * @param image
	 * @param title
	 * @param action A callback which is called after the page has been selected and the content (= controls) has been created
	 * @param visibilityCallback Callback is asked at runtime whether the page is selectable or not
	 * @author richard.vogl
	 */
	public MenuPage(String image, String title, MenuActionCallback action, MenuVisibilityCallback visibilityCallback) {
		super(visibilityCallback);

		this.image = image;
		this.title = title;
		this.action = action;
	}

	/**
	 * Sets the image for the menu page
	 *
	 * @param image
	 * @author richard.vogl
	 */
	public void setImage(String image) {
		this.image = image;
	}

	/**
	 * Returns the image for the menu page
	 *
	 * @return
	 * @author richard.vogl
	 */
	public String getImage() {
		return image;
	}

	/**
	 * Sets the title for the menu page
	 *
	 * @param title
	 * @author richard.vogl
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Returns the title for the menu page
	 *
	 * @return
	 * @author richard.vogl
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the action for the menu page (notified when the page is selected)
	 *
	 * @param action Callback or Null
	 * @author richard.vogl
	 */
	public void setAction(MenuActionCallback action) {
		this.action = action;
	}

	/**
	 * Returns the action for the menu page
	 *
	 * @return Callback or Null
	 * @author richard.vogl
	 */
	public MenuActionCallback getAction() {
		return action;
	}


	/**
	 * Adds a menu item to this menu page
	 *
	 * @param item
	 * @return
	 * @author richard.vogl
	 */
	public boolean addItem(MenuItem item) {
		return this.items.add(item);
	}

	/**
	 * Creates the component for this page's tab button
	 *
	 * @param tabs
	 * @return
	 * @author richard.vogl
	 */
	public MenuPageButtonComponent createButton(Composite tabs) {
		return new MenuPageButtonComponent(this, tabs, SWT.NONE);
	}

	/**
	 * Creates the component for this menu page
	 *
	 * @param pageArea
	 * @return
	 * @author richard.vogl
	 */
    public Composite createContent(Composite pageArea, LayoutCallback layoutCallback) {
		if(LOGGER.isDebugEnabled()) LOGGER.info("Creating menu page with " + items.size() + " items");

		Composite panel = new Composite(pageArea, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		//panel.setData(MessageConstants.CUSTOM_WIDGET_ID, MessageConstants.ID_MENU_PAGE_PANEL + "_" + title.replaceAll("[^a-zA-Z0-9]", "_"));

		menuItemComponents.clear();
		for (AbstractMenuItem item : items) {
			AbstractMenuItemComponent<? extends AbstractMenuItem> component = item.createComponent(panel, this, layoutCallback);
			if (item == ScenarioMenuFactory.getInstance().getActiveMenuItem()) {
				rememberRestore((ScenarioMenuItemComponent) component);
			}
			menuItemComponents.add(component);
		}
		return panel;
	}

    // needed for RAP
    private void rememberRestore(ScenarioMenuItemComponent component) {
    	LaunchEngine restoreEngine = Launcher.getLauncherUI(component.getDisplay()).getRestoreEngine();
    	if (restoreEngine != null) {
	    	restoreEngine.addBatchStateListener(component);
	    	restoreEngine.addProcedureStateListener(component);
    	}
    }

	/**
	 * Adds all items from another page to this page.
	 *
	 * @param page
	 * @author richard.vogl
	 */
	public void add(MenuPage page) {
		items.addAll(page.items);
	}

	/**
	 * @author christoph.neumueller
	 */
	@Override
	public void applyFilter(FilterTaskParams filterTaskParams) {
		notifyBeforeFilterAppliedListener();

		// set filterTaskParams in every menu item and then update visibility
		this.filterTaskParams = filterTaskParams;
		for (AbstractMenuItem item : items) {
			item.applyFilter(filterTaskParams);
		}
		for (AbstractMenuItemComponent<? extends AbstractMenuItem> menuItemComponent : menuItemComponents) {
			menuItemComponent.updateVisibility(false);
		}

		notifyAfterFilterAppliedListener();
	}

	/**
	 * @author christoph.neumueller
	 */
	@Override
	public void clearFilter() {
		for (AbstractMenuItem item : items) {
			item.clearFilter();
		}
	}

	/**
	 * @author christoph.neumueller
	 */
	public FilterTaskParams getFilterTaskParams() {
		return filterTaskParams;
	}

	/**
	 * @author christoph.neumueller
	 */
	public void addFilterListener(FilterListener filterListener) {
		synchronized (filterListeners) {
			filterListeners.add(filterListener);
		}
	}

	/**
	 * @author christoph.neumueller
	 */
	private void notifyBeforeFilterAppliedListener() {
		synchronized (filterListeners) {
			for (FilterListener listener : filterListeners) {
				listener.beforeApplyFilter();
			}
		}
	}

	/**
	 * @author christoph.neumueller
	 */
	private void notifyAfterFilterAppliedListener() {
		synchronized (filterListeners) {
			for (FilterListener listener : filterListeners) {
				listener.afterApplyFilter();
			}
		}
	}

	/**
	 * Compares if all items are equal to the items of the other menupage
	 *
	 * @author christoph.neumueller
	 */
	public boolean itemsEqual(MenuPage other) {
		if (other.items.size() != this.items.size())
			return false;

		for (int i = 0; i < other.items.size(); i++) {
			if (!other.items.get(i).equals(this.items.get(i)))
				return false;
		}
		return true;
	}

	/**
	 * @author christoph.neumueller
	 */
	public boolean isFilterEnabled() {
		return this.isFilterEnabled;
	}

	/**
	 * @author christoph.neumueller
	 */
	public void setIsFilterEnabled(boolean isFilterEnabled) {
		this.isFilterEnabled = isFilterEnabled;
	}
}
