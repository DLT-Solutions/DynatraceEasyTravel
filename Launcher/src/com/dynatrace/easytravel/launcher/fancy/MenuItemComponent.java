package com.dynatrace.easytravel.launcher.fancy;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;



/**
 * Widget for menu item
 *
 * @author richard.vogl
 */
public class MenuItemComponent<T extends MenuItem> extends AbstractMenuItemComponent<T>{
	private static final Logger LOGGER = LoggerFactory.make();

	private Control description;
	private Control titleLabel;
	private Control imageLabel;

	/**
	 * Create new menu item component
	 *
	 * @param parent
	 * @param style
	 * @author richard.vogl
	 * @param page
	 */
	public MenuItemComponent(Composite parent, int style, T item, MenuPage page) {
		super(parent, style, item, page);
	}

	@Override
	protected void updateState() {
		updateVisibility(true);
		if (checkEnabled() != titleLabel.isEnabled()){
			enableControls(checkEnabled());
			this.layout();
		}
	}

	private void enableControls(boolean enable) {
		if (this.description != null){
			this.description.setEnabled(enable);
			if (this.description instanceof Composite){
				Control[] children = ((Composite) this.description).getChildren();
				if (children != null) { // NOPMD
					for (Control child : children){
						child.setEnabled(enable);
					}
				}
			}
		}
		if (this.titleLabel != null){
			this.titleLabel.setEnabled(enable);
		}
		if (this.imageLabel != null){
			this.imageLabel.setEnabled(enable);
		}
	}



	@Override
	protected void createContent() {
		GridLayout layout = new GridLayout();
		if (getItem().getImage() == null){
			layout.numColumns = 1;
		} else {
			layout.numColumns = 2;
		}
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
        layout.marginHeight = 7;
        layout.marginWidth = 10;
		this.setLayout(layout);
		GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		layoutData.verticalIndent = 0;
		layoutData.horizontalIndent = 0;
		this.setLayoutData(layoutData);

		final SelectionListener selectionListener = new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			    // do nothing
			}

			@Override
			public void widgetSelected(SelectionEvent event) {
				MenuActionCallback action = getItem().getAction();
				if (action == null){
				    LOGGER.warn(TextUtils.merge(Constants.InternalMessages.MENU_ACTION_WAS_NULL, getItem().getTitle()));
				    return;
				}

				action.run();
			}};

		imageLabel = createImageControl(this, selectionListener);

		titleLabel = createTitleControl(this, selectionListener);

		description = createDescriptionControl(this);
	}

	/**
	 * Traverse the given component's parents up to a ScrolledComposite
	 * and instruct it the show the given component (i.e. scroll it to visible).
	 * Does nothing if component is disposed or there is no ScrolledComposite
	 * found whan traversing the parents.
	 */
	public static void scrollToVisible(Composite component) {
		if (Launcher.isWidgetDisposed(component)) {
			return;
		}
		// find the parent ScrollComposite
		ScrolledComposite scrollPane = null;
		Composite parent = component.getParent();
		while (parent != null) {
			if (parent instanceof ScrolledComposite) {
				scrollPane = (ScrolledComposite) parent;
				break;
			}
			parent = parent.getParent();
		}

		if (scrollPane != null) {
			scrollPane.showControl(component);
		}
	}

	/**
	 * Creates the title component above the description.
	 *
	 * @param selectionListener The listener which should be notified on click. Listener runs the action.
	 * @return
	 * @author richard.vogl
	 */
	protected Control createTitleControl(final Composite parent, final SelectionListener selectionListener) {
		Control control = null;
		if (getItem().getAction() != null) {
			Link titleLabel = new Link(parent, SWT.NONE);
			titleLabel.setText(Constants.Html.BEGIN_LINK+getItem().getTitle()+Constants.Html.END_LINK);
			titleLabel.setFont(getFontTitle());
			titleLabel.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					scrollToVisible(parent);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					scrollToVisible(parent);
				}
			});
			titleLabel.addSelectionListener(selectionListener);
			titleLabel.setLayoutData(createTitleLayoutData());
			control = titleLabel;
		} else {
			Label titleLabel = new Label(parent, SWT.NONE);
			titleLabel.setText(getItem().getTitle());
			titleLabel.setFont(getFontTitle());
			titleLabel.setLayoutData(createTitleLayoutData());
			control = titleLabel;
		}

        final Menu popupmenu = new Menu(getShell(), SWT.POP_UP);

	    popupmenu.addListener(SWT.Show, new Listener() {
	      @Override
		public void handleEvent(Event event) {
	    	org.eclipse.swt.widgets.MenuItem[] menuItems = popupmenu.getItems();
	        for (int i = 0; i < menuItems.length; i++) {
	          menuItems[i].dispose();
	        }

	        org.eclipse.swt.widgets.MenuItem miScenarioFile = new org.eclipse.swt.widgets.MenuItem(popupmenu, SWT.PUSH);
	        miScenarioFile.setText(MessageConstants.CONTEXT_SCENARIO);
	        miScenarioFile.addListener(SWT.Selection, new Listener() {
	            @Override
				public void handleEvent(Event e) {
	            	Launcher.getLauncherUI(parent.getDisplay()).showFile(new File(Directories.getConfigDir(), Constants.Misc.SCENARIOS_FILE),
	                		getShell());
	            }
	        });

	        // userScenarios.xml
			final File localFile = new File(new File(Directories.getConfigDir(), Constants.Misc.USER_SCENARIOS_FILE).getAbsolutePath());
			if (localFile.exists()) {
		        org.eclipse.swt.widgets.MenuItem miUserScenarioFile = new org.eclipse.swt.widgets.MenuItem(popupmenu, SWT.PUSH);
		        miUserScenarioFile.setText(MessageConstants.CONTEXT_USER_SCENARIO);
		        miUserScenarioFile.addListener(SWT.Selection, new Listener() {
		            @Override
					public void handleEvent(Event e) {
		            	Launcher.getLauncherUI(parent.getDisplay()).showFile(localFile,getShell());
		            }
		        });
			}

	        org.eclipse.swt.widgets.MenuItem miPropertiesFile = new org.eclipse.swt.widgets.MenuItem(popupmenu, SWT.PUSH);
	        miPropertiesFile.setText(MessageConstants.CONTEXT_PROPERTIES);
	        miPropertiesFile.addListener(SWT.Selection, new Listener() {
	            @Override
				public void handleEvent(Event e) {
	            	Launcher.getLauncherUI(parent.getDisplay()).showFile(new File(EasyTravelConfig.read().filePath), getShell());
	            }
	        });

	        if(EasyTravelConfig.getEasyTravelLocalPropertiesFile().exists()) {
		        org.eclipse.swt.widgets.MenuItem miLocalPropertiesFile = new org.eclipse.swt.widgets.MenuItem(popupmenu, SWT.PUSH);
		        miLocalPropertiesFile.setText(MessageConstants.CONTEXT_LOCAL_PROPERTIES);
		        miLocalPropertiesFile.addListener(SWT.Selection, new Listener() {
		            @Override
					public void handleEvent(Event e) {
		            	Launcher.getLauncherUI(parent.getDisplay()).showFile(EasyTravelConfig.getEasyTravelLocalPropertiesFile(), getShell());
		            }
		        });
	        }


	        /* This does not work currently as we keep the config statically in many places
	         * See also JLT-41104
			miReloadConfig.setText(MessageConstants.CONTEXT_RELOAD_CONFIG);
			miReloadConfig.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					EasyTravelConfig.reload();
				}
			});
			*/

			org.eclipse.swt.widgets.MenuItem miLogFile = new org.eclipse.swt.widgets.MenuItem(popupmenu, SWT.PUSH);
	        miLogFile.setText(MessageConstants.CONTEXT_LOGFILE);
	        miLogFile.addListener(SWT.Selection, new Listener() {
	            @Override
				public void handleEvent(Event e) {
	            	Launcher.getLauncherUI(parent.getDisplay()).showFile(new File(Directories.getLogDir(),
	    	        		(!Launcher.isWeblauncher() ? MessageConstants.LAUNCHER : MessageConstants.WEBLAUNCHER) +
	    	        		"_0-0.log"),
	                		getShell());
	            }
	        });

	        /* TODO: Not yet done: we could provide an "Add procedure" menu item here to adjust the scenario
	        MenuItem miAdd = new MenuItem (popupmenu, SWT.CASCADE);
	        miAdd.setText(MessageConstants.CONTEXT_ADD);
	        final Menu procMenu = new Menu(getShell(), SWT.DROP_DOWN);
	        miAdd.setMenu(procMenu);

	        MenuItem miCustomerFrontend = new MenuItem (procMenu, SWT.PUSH);
	        miCustomerFrontend.setText(MessageConstants.MODULE_CUSTOMER_FRONTEND);
	        miCustomerFrontend.addListener(SWT.Selection, new Listener() {
	            @Override
				public void handleEvent(Event e) {
	                throw new UnsupportedOperationException("Not yet implemented");
	            }
	        });*/
	      }
	    });

	    control.setMenu(popupmenu);

        this.setMenu(popupmenu);
		return control;
	}

	/**
	 * Creates the image component to the left of the title and description.
	 *
	 * @param selectionListener The listener which should be notified on click. Listener runs the action.
	 * @return
	 * @author richard.vogl
	 */
	protected Control createImageControl(Composite parent, final SelectionListener selectionListener) {
		if (getItem().getImage() != null){
			Label imageLabel = new Label(parent, SWT.NONE);
			imageLabel.setImage(imageManager.createImage(getItem().getImage()));
			imageLabel.setLayoutData(createImageLayoutData());
			if (getItem().getAction() != null){
				imageLabel.setCursor(Constants.Cursors.HAND);
				imageLabel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseUp(MouseEvent e) {
						if (e.button == 1){
							Event event = new Event();
							event.button = e.button;
//							event.count = e.count;
							event.data = e.data;
							event.display = e.display;
							event.stateMask = e.stateMask;
							event.time = e.time;
							event.widget = e.widget;
							event.x = e.x;
							event.y = e.y;
							selectionListener.widgetSelected(new SelectionEvent(event));
						}
					}

				});
			}
			return imageLabel;
		}
		return null;
	}

	/**
	 * Creates the control put under the header next to the icon.
	 *
	 * @return
	 * @author richard.vogl
	 */
	protected Control createDescriptionControl(Composite parent) {
		if (getItem().getDescriptionText() != null){
			Link description = new Link(parent, SWT.WRAP);

			// by using "Link" we could also use links in the scenario-description, however
			// there is a bug in our version of RAP which breaks newlines in Weblauncher
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=354129
			// I tried to upgrade RAP to 1.5.1, but ran into various other problems then...
			/* Link description = new Link(parent, SWT.WRAP);
			description.addListener (SWT.Selection, new Listener () {
				@Override
				public void handleEvent(Event event) {
					LOGGER.info("Opening menu item link: " + event.text);
					DocumentStarter starter = new DocumentStarter();
					starter.openURL(event.text);
				}
			});*/

			description.setFont(getFont());
			description.setLayoutData(createDescriptionLayoutData());
			description.setText(getItem().getDescriptionText());

			return description;
		}
		return null;
	}

	/**
	 * Creates the layout data object for the description text/composite control.
	 *
	 * @return
	 * @author richard.vogl
	 */
	protected GridData createDescriptionLayoutData() {
		GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		layoutData.widthHint = 400;
		layoutData.horizontalIndent = 7;
		layoutData.verticalIndent = 5;
		return layoutData;
	}

	/**
	 * Creates the layout data object for the description link controls (within description composite).
	 *
	 * @return
	 * @author richard.vogl
	 */
	protected GridData createLinkLayoutData() {
		GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		layoutData.widthHint = 400;
		layoutData.horizontalIndent = 0;
		layoutData.verticalIndent = 3;
		return layoutData;
	}

	/**
	 * Creates the layout data object for the title text/link control.
	 *
	 * @return
	 * @author richard.vogl
	 */
	protected GridData createTitleLayoutData() {
		GridData layoutData;
		layoutData = new GridData();
		layoutData.horizontalIndent = 5;
		return layoutData;
	}

	/**
	 * Creates the layout data object for the image control.
	 *
	 * @return
	 * @author richard.vogl
	 */
	protected GridData createImageLayoutData() {
		GridData layoutData = new GridData();
		layoutData.verticalSpan = 2;
		layoutData.verticalAlignment = SWT.TOP;
		return layoutData;
	}
}
