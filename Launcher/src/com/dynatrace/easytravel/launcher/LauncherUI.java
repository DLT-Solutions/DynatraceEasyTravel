package com.dynatrace.easytravel.launcher;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.baseload.BaseLoadManager;
import com.dynatrace.easytravel.launcher.config.ScenarioConfiguration;
import com.dynatrace.easytravel.launcher.config.UIProperties;
import com.dynatrace.easytravel.launcher.engine.BatchStateListener;
import com.dynatrace.easytravel.launcher.engine.CloseCallback;
import com.dynatrace.easytravel.launcher.engine.LaunchEngine;
import com.dynatrace.easytravel.launcher.engine.ProcedureStateMultiplexer;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.engine.ThreadEngine;
import com.dynatrace.easytravel.launcher.fancy.MenuComponent;
import com.dynatrace.easytravel.launcher.fancy.MenuPage;
import com.dynatrace.easytravel.launcher.fancy.PageSelectionListener;
import com.dynatrace.easytravel.launcher.misc.AccessDeniedListener;
import com.dynatrace.easytravel.launcher.misc.DocumentStarter;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.panels.FooterPanel;
import com.dynatrace.easytravel.launcher.panels.HeaderPanel;
import com.dynatrace.easytravel.launcher.panels.HeaderPanelListener;
import com.dynatrace.easytravel.launcher.plugin.PluginStateListener;
import com.dynatrace.easytravel.launcher.plugin.PluginStateRefresher;
import com.dynatrace.easytravel.launcher.plugin.restore.BootPluginStateRestore;
import com.dynatrace.easytravel.launcher.plugin.restore.UserPluginStateRestore;
import com.dynatrace.easytravel.launcher.procedures.utils.CentralTechnologyActivator;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class LauncherUI {
	private static final Logger LOGGER = LoggerFactory.make();

	private static final int DEFAULT_WIDTH = 1050;
	private static final int DEFAULT_HEIGHT = 715;

	private Display display;
	private Shell shell;
	private LauncherUIType uiType;
	private HeaderPanelListener headerListener;
	private HeaderPanel headerPanel;
	private PluginStateRefresher pluginStateRefresher;
	private LaunchEngine restoreEngine;
	private int selectedPageIndex;
	private boolean createNowSelected = Launcher.isManualMode;

	private List<ScenarioConfiguration> listenersToDisposeFromCentral;

	public LauncherUI() {
		this.uiType = LauncherUIType.SWT;
	}

	public void init() {
		display = new Display();

		initRestoreEngine();
		initPluginStateRefresher();
		initPluginStateRestore();

		display.disposeExec(new Runnable() {

			@Override
			public void run() {
				Launcher.cleanupLauncherUIList(display);
				UIProperties.removeUIChangeListener(pluginStateRefresher);
				EasyTravelConfig.removeConfigChangeListener(headerListener);
				disposePluginStateRefresher();

				for(ScenarioConfiguration item : listenersToDisposeFromCentral) {
					CentralTechnologyActivator.getIntance().unregisterBackendListener(item);
				}
			}
		});
	}

	public Display getDisplay() {
		return display;
	}

	//for tests
	public void setDisplay(Display testDisplay) {
		display = testDisplay;
	}

	public LaunchEngine getRestoreEngine() {
		return restoreEngine;
	}

	public Shell createShell() {
	    shell = new Shell(display);

	    if (uiType.equals(LauncherUIType.RAP)) { // i.e. it's the WebLauncher
	    	shell.setFullScreen(true);
	    } else { // SWT Launcher
	    	shell.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	    }

	    shell.setText("easyTravel Configuration UI");

		if (uiType.equals(LauncherUIType.RAP)) {
			LOGGER.debug("Not setting an icon when running as web application.");
		} else {
			// we can only read a non-Vista format ico, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=234644
			InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("easyTravel_128x128.ico");
			if (stream == null) {
				LOGGER.info("Could not find icon 'easyTravel.ico' in the current classpath.");
			} else {
				try {
					shell.setImage(new Image(display, stream));
				} finally {
					IOUtils.closeQuietly(stream);
				}
			}
		}

		GridLayout mainLayout = new GridLayout(1, true);
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		mainLayout.verticalSpacing = 0;
		mainLayout.horizontalSpacing = 0;
		shell.setLayout(mainLayout);

		Composite mainPanel = createMainPanel(shell);

		headerListener = addHeaderForLauncher(mainPanel);
		EasyTravelConfig.addConfigChangeListener(headerListener);

		addSeparator(mainPanel);
		AccessDeniedListener errorPopup = new AccessDeniedListener(shell);
		initContent(mainPanel, errorPopup);
		initFooter(mainPanel);

		pluginStateRefresher.getPluginStateListener().setPluginEnabledListener(headerListener);
		pluginStateRefresher.getPluginStateListener().setHeaderPanel(headerPanel);
        UIProperties.addUIChangeListener(pluginStateRefresher);

		if (restoreEngine != null) {
			restoreEngine.addProcedureStateListener(headerListener);
		}

		return shell;
	}

	private HeaderPanelListener addHeaderForLauncher(Composite parent) {
		final HeaderPanel header = new HeaderPanel(parent, SWT.NONE, Launcher.getTaggedWebRequest(), createNowSelected, Launcher.getBaseLoadValue());
		header.registerTechnologyListener(CentralTechnologyActivator.getIntance());

		header.addTrafficSliderListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				int baseLoadValue = ((Scale) event.getSource()).getSelection();
				Launcher.setBaseLoadValue(baseLoadValue);
				Launcher.updateLoadValue(baseLoadValue);
			}
		});
		header.addWebRequestTaggingListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				Launcher.updateTaggedWebRequests(((Button) event.getSource()).getSelection());
			}
		});
		header.addCreateNowListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				createNowSelected = ((Button) event.getSource()).getSelection();
				updateCreateVisits(header, createNowSelected);
			}
		});
		header.addGenerateVisitsListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				createNowSelected = !((Button) event.getSource()).getSelection();
				updateCreateVisits(header, createNowSelected);
			}
		});
		header.addCreateCustomerVisitListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				Object value = ((Button) event.getSource()).getData(MessageConstants.VISITS_ID);
				if (value != null && value instanceof Integer) {
					int visits = (Integer) value;

					EasyTravelConfig config = EasyTravelConfig.read();
					if (config != null && config.baseLoadHeadlessCustomerRatio > 0) {
						BaseLoadManager.getInstance().setHeadlessCustomerVisits(visits);
					} else {
						BaseLoadManager.getInstance().setCustomerVisits(visits);
					}
				}
			}
		});

		header.addCreateB2bVisitListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				Object value = ((Button) event.getSource()).getData(MessageConstants.VISITS_ID);
				if (value != null && value instanceof Integer) {
					int visits = (Integer) value;
					BaseLoadManager.getInstance().setB2bVisits(visits);
				}
			}
		});

		header.addCreateMobileNativeVisitListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				Object value = ((Button) event.getSource()).getData(MessageConstants.VISITS_ID);
				if (value != null && value instanceof Integer) {
					int visits = (Integer) value;
					BaseLoadManager.getInstance().setMobileNativeVisits(visits);
				}
			}
		});

		header.addCreateMobileBrowserVisitListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				Object value = ((Button) event.getSource()).getData(MessageConstants.VISITS_ID);
				if (value != null && value instanceof Integer) {
					int visits = (Integer) value;
					BaseLoadManager.getInstance().setMobileBrowserVisit(visits);
				}
			}
		});

		BaseLoadManager.getInstance().setHeaderPanelInterface(header);
		headerPanel = header;

		return new HeaderPanelListener(header);
	}

	private Composite createMainPanel(final Shell shell) {
		Composite mainPanel = new Composite(shell, SWT.NONE);
		GridLayout mainLayout = new GridLayout(1, true);
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		mainLayout.verticalSpacing = 0;
		mainLayout.horizontalSpacing = 0;

		mainPanel.setLayout(mainLayout);
		mainPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		return mainPanel;
	}

	private void addSeparator(Composite mainPanel) {
		Label separator = new Label(mainPanel, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData separatorLayoutData = new GridData();
		separatorLayoutData.grabExcessHorizontalSpace = true;
		separatorLayoutData.horizontalAlignment = SWT.FILL;
		separator.setLayoutData(separatorLayoutData);
	}

	private void initContent(Composite parent, AccessDeniedListener errorPopup) {
		ScenarioMenuFactory scenarioFactory = ScenarioMenuFactory.getInstance();

		// add a listener which is informed about starting Business Backend and populates the Plugins-Page accordingly
		PluginStateListener pluginStateListener = pluginStateRefresher.getPluginStateListener();
		Autostart autostart = new Autostart();

		// add a listener which is informed about Scenario start/stop and puts an indication into the button-bar on the left showing which item has a running Scenario
		MenuBatchStateListener batchListener = new MenuBatchStateListener();

		// also register this in the restoreEngine so the WebLauncher correctly re-creates the "*" for the current scenario
		if(restoreEngine != null) {
			restoreEngine.addBatchStateListener(batchListener);
		}

		MenuPagesAndListeners result = scenarioFactory.createMenuPages(new ProcedureStateMultiplexer(pluginStateListener, headerListener, errorPopup, CentralTechnologyActivator.getIntance()), autostart, batchListener, headerListener);
		List<MenuPage> menuPages = result.getMenuPages();
		listenersToDisposeFromCentral = result.getListeners();

		MenuComponent menu = new MenuComponent(parent, menuPages);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.verticalSpan = 20;

		menu.setLayoutData(data);
		menu.selectPage(selectedPageIndex);
		menu.addPageSelectionListener(new PageSelectionListener() {

			@Override
			public void pageSelected(Composite source, int index) {
				selectedPageIndex = index;
			}
		});

		pluginStateListener.setMenu(menu);
		pluginStateListener.setHeaderPanel(headerPanel);
		batchListener.setMenu(menu);

		autostart.execute();
	}

	private void initFooter(Composite parent) {
		FooterPanel footer = new FooterPanel(parent, SWT.NONE);
		footer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	/**
	 * A helper class which listens for Batch State Changes and passes the information
	 * about running Scenarios on to the Menu for displaying more information, e.g.
	 * indicate which menu-entry is running currently.
	 */
	private static final class MenuBatchStateListener implements BatchStateListener {
		MenuComponent menu;

		@Override
		public void notifyBatchStateChanged(Scenario scenario, State oldState, State newState) {
			if(menu != null && !menu.isDisposed()) {
				switch (newState) {
					case OPERATING:
					case STARTING:
					case STOPPING:
					case TIMEOUT:
						menu.indicateRunning(scenario.getGroup(), scenario.getTitle(), true);
						break;

					default:
						menu.indicateRunning(scenario.getGroup(), scenario.getTitle(), false);
						break;
				}
			}
		}


		public void setMenu(MenuComponent menu) {
			this.menu = menu;
		}
	}

	private void initPluginStateRefresher() {
		if (pluginStateRefresher == null) { // NOSONAR create the plugin state singleton, this runs in the single-thread initialization only
			pluginStateRefresher = new PluginStateRefresher();
			pluginStateRefresher.startTimer();
		}
		if (restoreEngine != null) {
			restoreEngine.addProcedureStateListener(pluginStateRefresher.getPluginStateListener());
		}
	}

	private void initRestoreEngine() {
		// specially handle the "restart" case for WebLauncher: we have to wait until batch is ready again
		if (uiType.equals(LauncherUIType.RAP) && ScenarioController.isCurrentlyRestarting()) {
			final int MAX = 30; // timeout after 30 tries
			final int SLEEP_MS = 1000;
			for (int i = 0; ScenarioController.isCurrentlyRestarting() && LaunchEngine.getRunningBatch() == null; i++) {
		    	if (i >= MAX) {
					LOGGER.warn("Giving up waiting for restarting scenario after " + MAX + " tries. Launcher view will be incorrect.");
					break;
		    	}
				LOGGER.info("Waiting for restarting scenario...");
		    	try {
					Thread.sleep(SLEEP_MS);
				} catch (InterruptedException e) {
					LOGGER.warn("Interrupted while waiting for restarting scenario", e);
					break;
				}
			}
		}

		if (LaunchEngine.getRunningBatch() != null) {
			LOGGER.info("Restoring batch: " + LaunchEngine.getRunningBatch().getState());
			restoreEngine = LaunchEngine.getNewInstance();
		}
	}

	public void useRestoreEngine() {
		LOGGER.info("Restoring state.");
		restoreEngine.restore();
		restoreEngine = null;
	}

	private void updateCreateVisits(HeaderPanel header, boolean createVisitsVisible) {
		if (createVisitsVisible) {
			new Thread(new Runnable() {

				// stop the load simulators asynchronously to improve the responsiveness of the interface
				@Override
				public void run() {
					BaseLoadManager.getInstance().stopB2bLoadAndBlock();
					BaseLoadManager.getInstance().stopCustomerLoadAndBlock();
					BaseLoadManager.getInstance().stopMobileNativeLoadAndBlock();
					BaseLoadManager.getInstance().stopMobileBrowserLoadAndBlock();
					BaseLoadManager.getInstance().stopIotDevicesLoadAndBlock();
					BaseLoadManager.getInstance().stopHeadlessCustomerLoadAndBlock();
					BaseLoadManager.getInstance().stopHeadlessAngularLoadAndBlock();
					BaseLoadManager.getInstance().stopHeadlessMobileAngularLoadAndBlock();
				}
			}).start();

			if (Launcher.isWidgetDisposed(header.getMainComposite())) {
				// The user clicked "Create Visits manually" and then reloaded the WebLauncher in the Browser (F5)
				// In that case, stop now and don't set anything that would run into SWTException("Invalid thread access")
				return;
			}

			ThreadEngine.runInDisplayThread(new Runnable() {
				@Override
				public void run() {
					header.setTrafficSliderValue(0); // 0 is 1 in manual mode
					header.setCreateNowVisible(true);
				}
			}, display);
		} else {
			// reset properties
			header.setCreateNowVisible(false);
			BaseLoadManager.getInstance().removeScheduleBlocking();
			int loadValue = Launcher.getBaseLoadValue();
			BaseLoadManager.getInstance().setCustomerBaseLoad(loadValue);
			BaseLoadManager.getInstance().setB2bBaseLoad(loadValue);
			BaseLoadManager.getInstance().setMobileNativeBaseLoad(loadValue);
			BaseLoadManager.getInstance().setMobileBrowserBaseLoad(loadValue);
			BaseLoadManager.getInstance().setIotDevicesBaseLoad(loadValue);
			BaseLoadManager.getInstance().setHeadlessCustomerBaseLoad(loadValue);
			BaseLoadManager.getInstance().setHeadlessAngularBaseLoad(loadValue);
			BaseLoadManager.getInstance().setHeadlessMobileAngularBaseLoad(loadValue);

			ThreadEngine.runInDisplayThread(new Runnable() {
				@Override
				public void run() {
					header.setTrafficSliderValue(loadValue);
				}
			}, display);
		}
	}



	private void initPluginStateRestore() {
        if (uiType.equals(LauncherUIType.RAP) && EasyTravelConfig.read().isWebLauncherAuthEnabled) {
            pluginStateRefresher.getPluginStateListener().setPluginEnabledListener(new UserPluginStateRestore());
            pluginStateRefresher.getPluginStateListener().setPluginEnabledListener(new BootPluginStateRestore());
            LOGGER.info("Automatic plugin state restore has been initialized");
        }
    }

	public void disposePluginStateRefresher() {
		PluginStateRefresher toDispose = pluginStateRefresher;
		if (toDispose == null) {
			return;
		}
		toDispose.stopTimer();
		pluginStateRefresher.disposePluginStateRefresher();
		pluginStateRefresher = null;
	}

	public void setTaggedWebRequests(boolean enabled) {
		try {
			if (enabled) {
				headerPanel.enableTaggedWebRequest();
			} else {
				headerPanel.disableTaggedWebRequest();
			}
		} catch (NullPointerException e) {
			LOGGER.warn("easyTravel not ready for REST calls. Wait until easyTravel's starting procedures finish setup.", e);
		}
	}

	public void setManualVistisCreation(final boolean enabled) {
		headerPanel.setCreateNowVisible(enabled);
		createNowSelected = enabled;
		updateCreateVisits(headerPanel, enabled);
	}

	public void setLoadValue(int loadValue) {
		setHeaderPanelLoad(loadValue);
		Launcher.updateLoadValue(loadValue);
	}

	private void setHeaderPanelLoad(int loadValue) {
		if (headerPanel != null) {
			headerPanel.setLoad(loadValue);
		}
	}

	/**
	 * for tests only
	 * @return
	 */
	public boolean isCreateNowSelected() {
		return createNowSelected;
	}

	/**
	 * for tests only
	 */
	public void setHeaderPanel(HeaderPanel header) {
		headerPanel = header;
	}

	public void messageBox(Shell parent, int flags, String title, String message, CloseCallback callback) {
		MessageBox box = new MessageBox(parent, flags);
		box.setMessage(message);
		box.setText(title);
		int ret = box.open();
		if(callback != null) {
			callback.dialogClosed(ret);
		}
	}

	public void openURL(String url) {
		DocumentStarter starter = new DocumentStarter();
    	starter.openURL(url);
	}

	public void shutdown() {
		// run this in the background to not block the UI completely
    	ThreadEngine.createBackgroundThread("FooterPanel Stopper", new Runnable() {
			@Override
			public void run() {
            	// first stop any running scenario
            	LaunchEngine.stop();

            	// close GUI, needs to run in the display thread again
            	ThreadEngine.runInDisplayThread(new Runnable() {
					@Override
					public void run() {
		            	Launcher.exit();
					}
				}, getDisplay());
			}
    	}, getDisplay()).start();
	}

	public void logout() {
		//nothing to do
	}

	public LauncherUIType getUiType() {
		return uiType;
	}

	public void setUiType(LauncherUIType type) {
		uiType = type;
	}

	/**
	 * Display the contents of the file. In GUI Launcher this opens the file in the
	 * OS-Editor, in RAP it displays it as a popup-window.
	 *
	 * @param file
	 * @param parent
	 * @author dominik.stadler
	 */
	public void showFile(File file, Shell parent) {
		DocumentStarter starter = new DocumentStarter();
    	starter.openFile(file);
	}

	/**
	 * Display the contents of the specified file. In GUI Launcher this opens the file in the
	 * OS-Editor, in RAP it displays it as a popup-window.
	 *
	 * @param file
	 * @param parent
	 * @author dominik.stadler
	 */
	public void showFile(String file, Shell parent) {
		showFile(new File(file), parent);
	}
}
