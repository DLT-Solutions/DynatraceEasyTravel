package com.dynatrace.easytravel.launcher.fancy;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;


/**
 * Menu item for fancy menu.
 *
 * @author richard.vogl
 */
public class MenuItem extends AbstractMenuItem {

	private String image;
	private String title;
	private String descriptionText;
	private MenuActionCallback action;
    private String unavailableLinkText; // NOPMD


	/**
	 * Creates new MenuItem for fancy menu.
	 *
	 * @param image ID for image to be used.
	 * @param title Title of item.
	 * @param desciptionText Description of item.
	 * @param action Will be run when item is selected.
	 * @param visibility Callback is asked at runtime whether the item is selectable or not
	 * @author richard.vogl
	 *
	 */
	public MenuItem(String image, String title, String desciptionText, MenuActionCallback action,
			MenuVisibilityCallback visibility) {
		this(image, title, desciptionText, action, visibility, null);
	}

	/**
	 * Creates new MenuItem for fancy menu.
	 *
	 * @param image ID for image to be used
	 * @param title Title of item.
	 * @param desciptionText Description of item.
	 * @param action Will be run when item is selected.
	 * @param visibility If isVisible of callback returns true, the item is enabled. If null, item is always enabled.
	 * @param unavailableLink Link which is used for "this item is unavailable..." text.
	 * @author richard.vogl
	 */
	public MenuItem(String image, String title, String desciptionText, MenuActionCallback action,
			MenuVisibilityCallback visibility, String unavailableLink) {
		super(visibility);

		this.title = title;
		this.image = image;
		this.descriptionText = desciptionText;
		this.action = action;

		if (action != null) {
			action.setText(title);
		}

		this.unavailableLinkText = unavailableLink;
	}

	/**
	 * Setter for item title
	 *
	 * @param title
	 * @author richard.vogl
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Getter for item title
	 *
	 * @return
	 * @author richard.vogl
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Setter for item image
	 *
	 * @param image
	 * @author richard.vogl
	 */
	public void setImage(String image) {
		this.image = image;
	}

	/**
	 * Getter for item image
	 *
	 * @return
	 * @author richard.vogl
	 */
	public String getImage() {
		return image;
	}

	/**
	 * Setter for item description
	 *
	 * @param text
	 * @author richard.vogl
	 */
	public void setDescriptionText(String text) {
		this.descriptionText = text;
	}

	/**
	 * Getter for item description
	 *
	 * @return
	 * @author richard.vogl
	 */
	public String getDescriptionText() {
		return descriptionText;
	}

	/**
	 * Setter for item action
	 *
	 * @param action
	 * @author richard.vogl
	 */
	public void setAction(MenuActionCallback action) {
		this.action = action;
	}

	/**
	 * Getter for item action
	 *
	 * @return
	 * @author richard.vogl
	 */
	public MenuActionCallback getAction() {
		return action;
	}

	/**
	 * Getter for alternative link which will be used if item is disabled.
	 *
	 * @return
	 * @author richard.vogl
	 */
	public String getUnavailableLinkText() {
		return unavailableLinkText;
	}

	/**
	 * Setter for alternative link which will be used if item is disabled.
	 *
	 * @param unavailableLinkText
	 * @author richard.vogl
	 */
	public void setUnavailableLinkText(String unavailableLinkText) {
		this.unavailableLinkText = unavailableLinkText;
	}

	/*
	 * (non-Javadoc)
	 * @see com.dynatrace.diagnostics.client.fancymenu.AbstractMenuItem#createComponent(org.eclipse.swt.widgets.Composite, com.dynatrace.diagnostics.client.fancymenu.MenuPage)
	 */
	@Override
    public AbstractMenuItemComponent<? extends AbstractMenuItem> createComponent(Composite parent, MenuPage page, LayoutCallback layoutCallback) {
		return new MenuItemComponent<MenuItem>(parent, SWT.NONE, this, page);
	}

	/**
	 * @author christoph.neumueller
	 */
	@Override
	protected boolean checkFilter() {
		// false if any of the required pieces is not set
		if(action == null || filterTaskParams == null || (filterTaskParams.getFilterText() == null  && filterTaskParams.getRadioBoxState() == null)   ){
            return true;
        } else if (filterTaskParams.getRadioBoxState() != null) {
        	// if the radio box is queried, return if the action matches
            return filterTaskParams.getRadioBoxState().equals(action.isEnabled());
        } else{
        	// otherwise match title and description for contains of the filter text
            String filterText = filterTaskParams.getFilterText().toLowerCase();
            return (this.title != null && this.title.toLowerCase().contains(filterText)) || (this.descriptionText != null && this.descriptionText.toLowerCase().contains(filterText));
        }
    }



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((descriptionText == null) ? 0 : descriptionText.hashCode());
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	/**
	 * @author christoph.neumueller
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MenuItem) {
			MenuItem otherMenuItem = (MenuItem) obj;
			return equalsSafe(title, otherMenuItem.title) && equalsSafe(descriptionText, otherMenuItem.descriptionText) && equalsSafe(image, otherMenuItem.image) ;
		} else {
			return super.equals(obj);
		}
	}

	/***
	 * Equals on two strings which may be null
	 *
	 * @author christoph.neumueller
	 */
	private boolean equalsSafe(String first, String second) {
		if (first == second)
			return true; // identity and null==null

		if (first == null || second == null)
			return false;

		return first.equals(second);
	}
}
