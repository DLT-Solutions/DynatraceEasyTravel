package com.dynatrace.easytravel.launcher.fancy;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class ProblemPatternItem extends MenuItem {

	private String groupName;
	private String title;
	private String descriptionText;
	
	public ProblemPatternItem(String image, String title, String groupName, String desciptionText, MenuActionCallback action,MenuVisibilityCallback visibility) {
		super(image, title, desciptionText, action, visibility);
		
		this.groupName = groupName;
		this.title = title;
		this.descriptionText = desciptionText;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	@Override
	public AbstractMenuItemComponent<? extends AbstractMenuItem> createComponent(Composite parent, MenuPage page, LayoutCallback layoutCallback) {
		return new MenuItemComponent<ProblemPatternItem>(parent, SWT.NONE, this, page);
	}
	
	@Override
	protected boolean checkFilter() {
		if (filterTaskParams == null || filterTaskParams.getFilterText() == null)
			return true;
		String filterText = filterTaskParams.getFilterText().toLowerCase();
		return (this.title != null && this.title.toLowerCase().contains(filterText)) || (this.descriptionText != null && this.descriptionText.toLowerCase().contains(filterText) || this.groupName !=null && this.groupName.toLowerCase().contains(filterText));
	}
	
}
