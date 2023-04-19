package com.dynatrace.easytravel.launcher.panels;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.config.ScenarioConfigurationAdditionalSettings;
import com.dynatrace.easytravel.launcher.config.UIProperties;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.ImageManager;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;

/**
 * A component which creates a button with a small gear-wheel. If clicked, it displays a menu with configuration related
 * entires like showing property files, scenario files and logfiles.
 *
 * @author cwat-dstadler
 */
public class ConfigButton {
	private static final String HOSTS_DOC = "SECRET";
	private static final String HOSTS_FILE = "C:\\Windows\\System32\\drivers\\etc\\hosts";

	private final ImageManager imageManager;

	public ConfigButton(ImageManager imageManager) {
		super();
		this.imageManager = imageManager;
	}

	public Button createButton(final Composite parent) {
		final Button configButton = new Button(parent, SWT.NONE);
		configButton.setImage(imageManager.createImage(Constants.Images.IMG_COGWHEEL));
		configButton.setData(MessageConstants.CUSTOM_WIDGET_ID, MessageConstants.ID_CONFIG_BUTTON);

		final Menu configMenu = new Menu(configButton);
		configButton.setMenu(configMenu);

		configButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// calculate the position for WebLauncher where RAP does not position the menu correctly otherwise
				Point location = configButton.getLocation();
				Point shLoc = parent.getShell().getLocation();
				Point size = configButton.getSize();
				configMenu.setLocation(new Point(location.x + shLoc.x + size.x, location.y + shLoc.y + (int) (size.y * 1.5)));
				configMenu.setVisible(true);

				if (configMenu.getItems().length == 0) {
					createMenuItems(parent, configMenu);
				}
			}
		});
		parent.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				imageManager.disposeImages();
			}
		});

		return configButton;
	}

	private void createMenuItems(final Composite parent, Menu configMenu) {
		// easyTravelConfig.properties
		addFileMenuItem(parent.getShell(), configMenu, MessageConstants.CONTEXT_PROPERTIES, new File(EasyTravelConfig.read().filePath));

		// easyTravelLocalConfig.properties
		File localFile = EasyTravelConfig.getEasyTravelLocalPropertiesFile();
		if (localFile.exists()) {
			addFileMenuItem(parent.getShell(), configMenu, MessageConstants.CONTEXT_LOCAL_PROPERTIES, localFile);
		}

		// scenarios.xml
		addFileMenuItem(parent.getShell(), configMenu, MessageConstants.CONTEXT_SCENARIO, new File(Directories.getConfigDir(), Constants.Misc.SCENARIOS_FILE));

		// userScenarios.xml
		localFile = new File(Directories.getConfigDir(), Constants.Misc.USER_SCENARIOS_FILE);
		if (localFile.exists()) {
			addFileMenuItem(parent.getShell(), configMenu, MessageConstants.CONTEXT_USER_SCENARIO, localFile);
		}


        addCheckMenuItem(parent.getShell(), configMenu, MessageConstants.CONTEXT_PROBLEM_PATTERNS_SHOW,
                UIProperties.PROBLEM_PATTERNS.getEnabled());


		// Launcher.0.0.log
		addFileMenuItem(parent.getShell(), configMenu, MessageConstants.CONTEXT_LOGFILE, new File(Directories.getLogDir(),
				(!Launcher.isWeblauncher() ? MessageConstants.LAUNCHER : MessageConstants.WEBLAUNCHER) +
						".log"));

		// help user setting up www.easytravel.com/admin.easytravel.com
		if(OperatingSystem.IS_WINDOWS) {
			MenuItem menuItemPropertyFile = new MenuItem(configMenu, SWT.NONE);
			menuItemPropertyFile.setText(MessageConstants.CONTEXT_CONFIGURE_HOSTS_FILE);
			menuItemPropertyFile.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					try {
						Runtime.getRuntime().exec(ArrayUtils.add(BaseConstants.Browser.CMC_C_START_ARRAY, HOSTS_DOC));
						Runtime.getRuntime().exec(ArrayUtils.add(BaseConstants.Browser.CMC_C_START_ARRAY, HOSTS_FILE));

						Launcher.getLauncherUI(parent.getDisplay()).messageBox(parent.getShell(),  SWT.OK | SWT.ICON_INFORMATION,
								MessageConstants.CONTEXT_CONFIGURE_HOSTS_FILE.replace("&",  ""), "Follow the provided documentation how you can adjust the HOSTS file to redirect www.easytravel.com to your local installation.\n\nPlease make sure you restart easyTravel completely after changes are made.", null);
					} catch (IOException e) {
						Launcher.getLauncherUI(parent.getDisplay()).messageBox(parent.getShell(),  SWT.OK | SWT.ERROR | SWT.ICON_WARNING,
								"Cannot show URL/File", "Could not open '" + HOSTS_DOC + "' and '" + HOSTS_FILE + "'", null);
					}
				}
			});
		}
	}

	/**
	 * Add a menu item with the given label which will open the given file.
	 *
	 * @param shell
	 * @param configMenu
	 * @param label
	 * @param file
	 */
	private void addFileMenuItem(final Shell shell, final Menu configMenu, String label, final File file) {
		MenuItem menuItemPropertyFile = new MenuItem(configMenu, SWT.NONE);
		menuItemPropertyFile.setText(label);
		menuItemPropertyFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Launcher.getLauncherUI(shell.getDisplay()).showFile(file, shell);
			}
		});
	}

	private MenuItem addCheckMenuItem(final Shell shell, final Menu configMenu, String label, boolean selected) {
		final MenuItem menuItemCheck = new MenuItem(configMenu, SWT.CHECK);

		menuItemCheck.setText(label);
		menuItemCheck.setSelection(selected);

		menuItemCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
                menuItemCheck.setText(MessageConstants.CONTEXT_PROBLEM_PATTERNS_SHOW);
                UIProperties.PROBLEM_PATTERNS.setEnabled(menuItemCheck.getSelection());
				ScenarioConfigurationAdditionalSettings.INSTANCE.persistConfigurationState(UIProperties.PROBLEM_PATTERNS.getPropertyName(),
						UIProperties.PROBLEM_PATTERNS.getEnabled());
			}
		});
		return menuItemCheck;
	}

}
