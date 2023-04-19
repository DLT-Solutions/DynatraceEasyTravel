package com.dynatrace.easytravel.launcher.panels;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import ch.qos.logback.classic.Logger;

import com.dynatrace.diagnostics.uemload.utils.ShutdownUtils;
import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.LauncherUI;
import com.dynatrace.easytravel.launcher.engine.CloseCallback;
import com.dynatrace.easytravel.launcher.engine.ThreadEngine;
import com.dynatrace.easytravel.launcher.misc.FontManager;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;

public class FooterPanel extends Composite {
	private static final Logger LOGGER = LoggerFactory.make();

	private final FontManager fontManager;

	public FooterPanel(final Composite parent, int style) {
		super(parent, style);

		fontManager = new FontManager();
		parent.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent event) {
				fontManager.disposeFonts();
			}
		});

		init();
	}

	private void init() {
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 7;
		layout.marginWidth = 13;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 10;

		this.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, true));

		this.setLayout(layout);

		Button exitButton = new Button(this, SWT.NONE);
		exitButton.setText(MessageConstants.EXIT);
		exitButton.setFont(fontManager.createFont(+2, exitButton.getDisplay()));

		GridData data = new GridData(SWT.END, SWT.CENTER, true, true);
		exitButton.setLayoutData(data);

		exitButton.addListener(SWT.Selection, new ExitListener());

		if (Launcher.getLoggedInUser() != null) {
			GridData logoutGrid = new GridData(SWT.END, SWT.CENTER, false, true);
			Button logoutButton = new Button(this, SWT.NONE);
			logoutButton.setText(MessageConstants.LOGOUT);
			logoutButton.setFont(fontManager.createFont(+2, logoutButton.getDisplay()));
			logoutButton.addListener(SWT.Selection, new LogoutListener(logoutButton.getDisplay()));
			logoutButton.setLayoutData(logoutGrid);
		}
	}

	private class ExitListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			// in WebLauncher, ask for confirmation
			LauncherUI ui = Launcher.getLauncherUI(getDisplay());
			if (Launcher.isWeblauncher()) {
				ui.messageBox(FooterPanel.this.getShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION,
						MessageConstants.SHUTDOWN_WEBLAUNCHER_TITLE, MessageConstants.SHUTDOWN_WEBLAUNCHER,
						new CloseCallback() {

							@Override
							public void dialogClosed(int returnCode) {
								if (SWT.YES == returnCode) {
									LOGGER.warn("Shutting down upon request from " + Launcher.getOrigin());

									// disable UI
									getParent().setEnabled(false);

									LauncherUI launcher = Launcher.getLauncherUI(getParent().getDisplay());
									if (launcher != null) {
										// run this in the background to not block the UI completely
										ThreadEngine.createBackgroundThread("FooterPanel Stopper", new Runnable() {
											@Override
											public void run() {
											ShutdownUtils.shutdown();
												launcher.shutdown();
											}
										}, getDisplay()).start();
									}
								}
							}
						});
			} else {
				// disable UI
				getParent().setEnabled(false);
				LauncherUI launcher = Launcher.getLauncherUI(getParent().getDisplay());
				launcher.shutdown();
			}
		}
	}

	private class LogoutListener implements Listener {
		private final Display display;

		public LogoutListener(Display display) {
			this.display = display;
		}

		@Override
		public void handleEvent(Event event) {
			Launcher.getLauncherUI(getDisplay()).logout();
		}
	}
}
