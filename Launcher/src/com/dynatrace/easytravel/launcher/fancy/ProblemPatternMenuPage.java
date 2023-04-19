package com.dynatrace.easytravel.launcher.fancy;

import java.util.ArrayList;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public class ProblemPatternMenuPage extends MenuPage {

	private ArrayList<AbstractMenuItem> problemPatternItems = null;
	private final TreeMap<String, ArrayList<AbstractMenuItem>> groupPluginsItems = new TreeMap<String,ArrayList<AbstractMenuItem>>();

	public ProblemPatternMenuPage(String image, String title, MenuActionCallback action, MenuVisibilityCallback visibilityCallback) {
		super(image, title, action, visibilityCallback);
	}

	@Override
	public boolean addItem(MenuItem item) {

		String groupName = getGroupName(item);

		if(!groupPluginsItems.containsKey(groupName)) {
			problemPatternItems =  new ArrayList<AbstractMenuItem>(10);
			problemPatternItems.add(item);
			groupPluginsItems.put(groupName, problemPatternItems);
		} else {
			groupPluginsItems.get(groupName).add(item);
		}

		return super.addItem(item);
	}


	@Override
	public Composite createContent(Composite pageArea, LayoutCallback layoutCallback) {

		Composite panel = new Composite(pageArea, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;

		panel.setLayout(layout);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		//panel.setData(MessageConstants.CUSTOM_WIDGET_ID, MessageConstants.ID_PROBLEM_PATTERN_MENU_PAGE_PANEL);

		for(String groupName: groupPluginsItems.keySet()) {

			ArrayList<AbstractMenuItem> problemPatterns = getVisibleProblemPatterns(groupPluginsItems, groupName);

			if (! problemPatterns.isEmpty()) {

				Group group = createGroup(panel, layout, groupName);

				displayPatterns(problemPatterns, group, this, layoutCallback);

			}
		}
		return panel;
	}

	/**
	 * Display Problem Patterns items
	 *
	 * @param problemPatterns
	 * @param group
	 * @param page
	 * @param layoutCallback
	 */
	private void displayPatterns (ArrayList<AbstractMenuItem> problemPatterns, Group group, MenuPage page, LayoutCallback layoutCallback ) {
		for (AbstractMenuItem item : problemPatterns) {
			AbstractMenuItemComponent<? extends AbstractMenuItem> component = item.createComponent(group, page, layoutCallback);
			component.pack();
		}

	}

	/**
	 * Get visible problem pattern items
	 *
	 * When using filter criteria plugins are marked as "invisible". This situation causes
	 * that item is not visible on UI, but there is area where it should be, therefore this method selects only
	 * visible items.
	 *
	 * @param pluginsGroup
	 * @param groupName
	 * @return ArrayList<AbstractMenuItem>
	 */
	private ArrayList<AbstractMenuItem> getVisibleProblemPatterns (TreeMap<String,ArrayList<AbstractMenuItem>> pluginsGroup, String groupName) {

		ArrayList<AbstractMenuItem> plugins;
		ArrayList<AbstractMenuItem> enabledPlugins = new ArrayList<AbstractMenuItem>();

		plugins = pluginsGroup.get(groupName);

		for (AbstractMenuItem plugin: plugins) {

			if(plugin.checkVisible()) {
				enabledPlugins.add(plugin);
			}

		}

		return enabledPlugins;
	}

	/**
	 * Create group area for specified group name
	 *
	 * @param panel
	 * @param layout
	 * @param groupName
	 * @return Group group
	 */
	private Group createGroup (Composite panel, GridLayout layout, String groupName) {
		Group group = new Group(panel,SWT.SHADOW_ETCHED_IN);
		group.setLayout(layout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		setFont(panel,group);
		setGroupName(group,groupName);
		return group;
	}

	/**
	 * Create group name text
	 *
	 * @param group
	 * @param groupName
	 */
	private void setGroupName (Group group, String groupName) {
			group.setText(groupName);
	}

	/**
	 * Get group name from MenuItem
	 *
	 * @param item
	 * @return
	 */
	private String getGroupName(MenuItem item) {
		String groupName = ((ProblemPatternItem)item).getGroupName();
		if(groupName == null) {
			groupName = ".NET";
		}
		return groupName;
	}

	/**
	 * Set bold style to group name
	 *
	 * @param panel
	 * @param group
	 */
	private void setFont(Composite panel, Group group) {
		FontData[] fontData = panel.getFont().getFontData();
		for(int i=0; i< fontData.length; ++i){
			fontData[i].setStyle(SWT.BOLD);
		}
		group.setFont(new Font(panel.getDisplay(),fontData));
	}

}
