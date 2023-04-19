package com.dynatrace.easytravel.launcher.fancy.custom;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.ApacheHttpdProcedure;
import com.dynatrace.easytravel.launcher.engine.NginxWebserverProcedure;
import com.dynatrace.easytravel.launcher.engine.ProcedureFactory;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.engine.StatefulProcedure;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.engine.ThreadEngine;
import com.dynatrace.easytravel.launcher.httpd.ApacheHttpdUtils;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.ImageManager;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.mysqld.MysqlUtils;
import com.dynatrace.easytravel.launcher.procedures.AntProcedure;
import com.dynatrace.easytravel.launcher.procedures.RemoteProcedure;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.sync.Predicate;
import com.dynatrace.easytravel.launcher.sync.PredicateMatcher;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

public class ProcedureComponent extends Composite {
    private static final int TOOLTIP_CHAR_WIDTH = 75;
    private static final int TOOLTIP_CHAR_MAX = TOOLTIP_CHAR_WIDTH * 15;	// MAX 15 Lines

	private static final Logger LOGGER = LoggerFactory.make();

    private final StatefulProcedure procedure;
    private final Label nameLabel;
    private final Label iconLabel;
    private final Label agentIconLabel;
    private final Label stateLabel;
    private boolean continuously;

    private final ImageManager imageManager = new ImageManager();

    private final Listener startListener = new Listener() {
        @Override
        public void handleEvent(Event e) {
            runProcedure();
        }
    };

    private final ProcedureWithTimeoutListener startAndStop6Hours = new ProcedureWithTimeoutListener(TimeUnit.HOURS.toMillis(6));
    private final ProcedureWithTimeoutListener startAndStop12Hours = new ProcedureWithTimeoutListener(TimeUnit.HOURS.toMillis(12));
    private final ProcedureWithTimeoutListener startAndStop1Day = new ProcedureWithTimeoutListener(TimeUnit.DAYS.toMillis(1));
    private final ProcedureWithTimeoutListener startAndStop3Days = new ProcedureWithTimeoutListener(TimeUnit.DAYS.toMillis(3));

    /**
     *
     * @author Michal.Bakula
     *
     */
    private class ProcedureWithTimeoutListener implements Listener {

    	private final long timeout;

    	public ProcedureWithTimeoutListener(final long timeout) {
			super();
			this.timeout=timeout;
		}

		@Override
		public void handleEvent(Event e) {
			LOGGER.info(TextUtils.merge("Starting procedure {0} with automatic stop after {1}.", procedure.getName(), timeout));
			procedure.setupRunWithTimeout(timeout);
			runProcedure();
		}

    }

    private final Listener stopListener = new Listener() {
        @Override
        public void handleEvent(Event e) {
            stopProcedure();
        }
    };

    private final Listener restartListener = new Listener() {
        @Override
        public void handleEvent(Event e) {
            restartProcedure();
        }
    };

	public ProcedureComponent(final Composite parent, final StatefulProcedure procedure) {
        super(parent, SWT.NONE);

        continuously = false;
        this.procedure = procedure;

        GridLayout layout = new GridLayout(1, true);
        this.setLayout(layout);

        nameLabel = new Label(this, SWT.WRAP);
        // add invisible spaces to improve wrapping on RAP
        nameLabel.setText(procedure.getName().replaceAll("([\\.-:])", "\u200B$1"));
        if (!Launcher.isWeblauncher()) { // RWT doesnot render WRAP + CENTER correctly
        	nameLabel.setAlignment(SWT.CENTER);
        }
        nameLabel.setForeground(getStatefulColor());
        GridData nameLabelData = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
        if(!Launcher.isWeblauncher()) {
        	nameLabelData.heightHint = 45;
        } else {
        	nameLabelData.heightHint = 72;
        }
        nameLabel.setLayoutData(nameLabelData);
        nameLabel.setToolTipText(procedure.getName());

        iconLabel = new Label(this, SWT.NONE);
        iconLabel.setImage(getStateImage());
    	iconLabel.setAlignment(SWT.CENTER);
        iconLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		iconLabel.addListener(SWT.MouseEnter, new Listener() {
			@Override
			public void handleEvent(Event e) {
				// the actual tool tip may change, a static generated tool tip may show wrong values
				// (esp. if a procedure has scenario specific values and must be restarted)
				setToolTip();
			}
		});

        Composite stateComposite = new Composite(this, SWT.WRAP);
        GridData stateCompositeLabelData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        stateCompositeLabelData.heightHint = 20;
        stateComposite.setLayoutData(stateCompositeLabelData);
        RowLayout stateCompositeLayout = new RowLayout();
        stateCompositeLayout.wrap = true;
        stateComposite.setLayout(stateCompositeLayout);

        agentIconLabel = new Label(stateComposite, SWT.LEFT);
        agentIconLabel.setImage(null);
        agentIconLabel.setLocation(10, 10);

        stateLabel = new Label(stateComposite, SWT.WRAP);
        stateLabel.setText(procedure.getStateLabel());
    	stateLabel.setAlignment(SWT.CENTER);
        stateLabel.setForeground(Constants.Colors.DARK_GRAY);

        final Menu popupmenu = new Menu(getShell(), SWT.POP_UP);
		popupmenu.addListener(SWT.Show, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// currently we do not support retrieving logfiles on RWT
				boolean hasLogfile = procedure.hasLogfile();

				MenuItem[] menuItems = popupmenu.getItems();
				for (int i = 0; i < menuItems.length; i++) {
					menuItems[i].dispose();
				}

				if (State.STOPPED.equals(procedure.getState()) || State.UNKNOWN.equals(procedure.getState())
						|| State.FAILED.equals(procedure.getState())
						|| State.ACCESS_DENIED.equals(procedure.getState())) {

					if (procedure.getTechnology() == Technology.VAGRANT) {
						MenuItem miStart6Hours = new MenuItem(popupmenu, SWT.PUSH);
						miStart6Hours.setText(MessageConstants.CONTEXT_START_STOP_6_HOURS);
						miStart6Hours.addListener(SWT.Selection, startAndStop6Hours);

						MenuItem miStart12Hours = new MenuItem(popupmenu, SWT.PUSH);
						miStart12Hours.setText(MessageConstants.CONTEXT_START_STOP_12_HOURS);
						miStart12Hours.addListener(SWT.Selection, startAndStop12Hours);

						MenuItem miStart1Day = new MenuItem(popupmenu, SWT.PUSH);
						miStart1Day.setText(MessageConstants.CONTEXT_START_STOP_1_DAY);
						miStart1Day.addListener(SWT.Selection, startAndStop1Day);

						MenuItem miStart3Day = new MenuItem(popupmenu, SWT.PUSH);
						miStart3Day.setText(MessageConstants.CONTEXT_START_STOP_3_DAYS);
						miStart3Day.addListener(SWT.Selection, startAndStop3Days);
					} else {
						MenuItem miStart = new MenuItem(popupmenu, SWT.PUSH);
						miStart.setText(MessageConstants.CONTEXT_START);
						miStart.addListener(SWT.Selection, startListener);
						// miStart.setData(MessageConstants.CUSTOM_WIDGET_ID, MessageConstants.ID_START_BUTTON + "_" + this.hashCode());

						if (procedure.isDotNetIISProcedure()) {
							miStart.setEnabled(false);
						}
					}
				} else if ((State.OPERATING.equals(procedure.getState()) || State.TIMEOUT.equals(procedure.getState()))
						&& procedure.getStopMode() != StopMode.NONE) {
					MenuItem miStop = new MenuItem(popupmenu, SWT.PUSH);
					miStop.setText(MessageConstants.CONTEXT_STOP);
					miStop.addListener(SWT.Selection, stopListener);
					// miStop.setData(MessageConstants.CUSTOM_WIDGET_ID, MessageConstants.ID_STOP_BUTTON + "_" + this.hashCode());

					MenuItem miRestart = new MenuItem(popupmenu, SWT.PUSH);
					miRestart.setText(MessageConstants.CONTEXT_RESTART);
					miRestart.addListener(SWT.Selection, restartListener);
					// miRestart.setData(MessageConstants.CUSTOM_WIDGET_ID, MessageConstants.ID_RESTART_BUTTON + "_" + this.hashCode());

					final String uri = procedure.getURI();
					if (uri != null) {
						MenuItem miOpen = new MenuItem(popupmenu, SWT.PUSH);
						miOpen.setText(MessageConstants.CONTEXT_OPEN_IN_BROWSER);
						miOpen.addListener(SWT.Selection, new Listener() {
							@Override
							public void handleEvent(Event e) {
								Launcher.getLauncherUI(getDisplay()).openURL(uri);
							}
						});
					}
				} else if (State.OPERATING.equals(procedure.getState()) && procedure.isDotNetIISProcedure()) {
					final String uri = procedure.getURI();
					if (uri != null) {
						MenuItem miOpen = new MenuItem(popupmenu, SWT.PUSH);
						miOpen.setText(MessageConstants.CONTEXT_OPEN_IN_BROWSER);
						miOpen.addListener(SWT.Selection, new Listener() {
							@Override
							public void handleEvent(Event e) {
								Launcher.getLauncherUI(getDisplay()).openURL(uri);
							}
						});

					}

				} else if (State.STARTING.equals(procedure.getState()) && procedure.getStopMode() != StopMode.NONE) {
					MenuItem miStop = new MenuItem(popupmenu, SWT.PUSH);
					miStop.setText(MessageConstants.CONTEXT_STOP);
					miStop.addListener(SWT.Selection, stopListener);
					// miStop.setData(MessageConstants.CUSTOM_WIDGET_ID, MessageConstants.ID_STOP_BUTTON + "_" + this.hashCode());

					if (procedure.isDotNetIISProcedure()) {
						miStop.setEnabled(false);
					}
				} else {
					// only show "no action available" if we do not even have a logfile
					// Apache has some custom actions added below instead of the default logfile-menu entry
					if (!hasLogfile
							&& !Constants.Procedures.APACHE_HTTPD_ID.equalsIgnoreCase(procedure.getMapping().getId())
							&& !Constants.Procedures.APACHE_HTTPD_PHP_ID
									.equalsIgnoreCase(procedure.getMapping().getId())) {
						MenuItem miNA = new MenuItem(popupmenu, SWT.PUSH);
						miNA.setText(MessageConstants.CONTEXT_NO_ACTION);
						miNA.setEnabled(false);
					}
				}

				// TODO: make this work remotely as well!
				if (Constants.Procedures.ANT_ID.equals(procedure.getMapping().getId())
						&& !(procedure.getDelegate() instanceof RemoteProcedure)) {
					final MenuItem miStartLoop = new MenuItem(popupmenu, SWT.PUSH);
					miStartLoop.setText(MessageConstants.CONTEXT_CONTINUOUSLY);
					if (continuously) {
						miStartLoop.setImage(new ImageManager().createImage(Constants.Images.MENU_ON));
					} else if ("0".equals(procedure.getMapping().getSettingValue(AntProcedure.RECURRENCE))) {
						miStartLoop.setImage(new ImageManager().createImage(Constants.Images.MENU_ON));
						miStartLoop.setEnabled(false);
					} else {
						miStartLoop.setImage(null);
					}
					miStartLoop.setSelection(continuously);
					miStartLoop.addListener(SWT.Selection, new Listener() {

						@Override
						public void handleEvent(Event arg0) {
							if (procedure.setContinuously(!continuously)) {
								continuously = !continuously;
							}
						}
					});

					MenuItem miOpenAntScript = new MenuItem(popupmenu, SWT.PUSH);
					miOpenAntScript.setText(MessageConstants.CONTEXT_ANT_SCRIPT);
					miOpenAntScript.addListener(SWT.Selection, new Listener() {
						@Override
						public void handleEvent(Event e) {
							try {
								Launcher.getLauncherUI(parent.getDisplay()).showFile(new File(Directories.getInstallDir(),
										procedure.getMapping().getSettingValue(AntProcedure.FILE)).getCanonicalFile(),
										getShell());
							} catch (IOException e1) {
								LOGGER.warn("Could not get canonical path of file: " + new File(Directories.getInstallDir(),
												procedure.getMapping().getSettingValue(AntProcedure.FILE)),
										e);
							}
						}
					});

				}

				// add some special menu entries for Apache HTTPD
				// TODO: make this work remotely as well!
				if ((Constants.Procedures.APACHE_HTTPD_ID.equalsIgnoreCase(procedure.getMapping().getId())
						|| Constants.Procedures.APACHE_HTTPD_PHP_ID.equalsIgnoreCase((procedure.getMapping().getId())))
						&& !(procedure.getDelegate() instanceof RemoteProcedure)) {
					MenuItem miHttpdConf = new MenuItem(popupmenu, SWT.PUSH);
					miHttpdConf.setText(MessageConstants.CONTEXT_HTTPD_CONF);
					miHttpdConf.addListener(SWT.Selection, new Listener() {
						@Override
						public void handleEvent(Event e) {
							Launcher.getLauncherUI(parent.getDisplay()).showFile(ApacheHttpdUtils.APACHE_CONF, getShell());
						}
					});

					// add php.ini to Apache Server
					if (Constants.Procedures.APACHE_HTTPD_PHP_ID.equalsIgnoreCase((procedure.getMapping().getId()))) {
						MenuItem miPhpIni = new MenuItem(popupmenu, SWT.PUSH);
						miPhpIni.setText(MessageConstants.CONTEXT_PHP_INI);

						miPhpIni.addListener(SWT.Selection, new Listener() {
							@Override
							public void handleEvent(Event e) {
								Launcher.getLauncherUI(parent.getDisplay()).showFile(ApacheHttpdUtils.PHP_INI, getShell());
							}
						});
					}

					// Add error.log for local Apache as well
					if (hasLogfile && !ProcedureFactory.isRemote(procedure.getMapping())) {
						MenuItem miLogfile = new MenuItem(popupmenu, SWT.PUSH);
						miLogfile.setText(MessageConstants.CONTEXT_ERROR_LOGFILE);
						miLogfile.addListener(SWT.Selection, new Listener() {
							@Override
							public void handleEvent(Event e) {
								Launcher.getLauncherUI(parent.getDisplay()).showFile(((ApacheHttpdProcedure) procedure.getDelegate()).getErrorLogfile(),
										getShell());
							}
						});
					}

					// reset as we show logfiles differently for Apache
					hasLogfile = false;
					final EasyTravelConfig config = EasyTravelConfig.read();
					if (config.apacheWebServerStatusPort > 0 && (State.STARTING.equals(procedure.getState())
							|| State.OPERATING.equals(procedure.getState())
							|| State.TIMEOUT.equals(procedure.getState()))) {
						MenuItem miStatus = new MenuItem(popupmenu, SWT.PUSH);
						miStatus.setText(MessageConstants.CONTEXT_STATUS);
						miStatus.addListener(SWT.Selection, new Listener() {
							@Override
							public void handleEvent(Event e) {
								Launcher.getLauncherUI(parent.getDisplay()).openURL("http://localhost:" + config.apacheWebServerStatusPort + "/server-status");
							}
						});

						MenuItem miBalancer = new MenuItem(popupmenu, SWT.PUSH);
						miBalancer.setText(MessageConstants.CONTEXT_BALANCE_MANAGER);
						miBalancer.addListener(SWT.Selection, new Listener() {
							@Override
							public void handleEvent(Event e) {
								Launcher.getLauncherUI(parent.getDisplay()).openURL("http://localhost:" + config.apacheWebServerStatusPort + "/balancer-manager");
							}
						});
					}
				}

				// add nginx_error.log to NginxWebserver
				if (Constants.Procedures.NGINX_WEBSERVER_ID.equalsIgnoreCase((procedure.getMapping().getId()))
						&& !(procedure.getDelegate() instanceof RemoteProcedure)) {
					MenuItem nginxErrorLog = new MenuItem(popupmenu, SWT.PUSH);
					nginxErrorLog.setText(MessageConstants.CONTEXT_ERROR_LOGFILE);

					nginxErrorLog.addListener(SWT.Selection, new Listener() {
						@Override
						public void handleEvent(Event e) {
							Launcher.getLauncherUI(parent.getDisplay()).showFile(((NginxWebserverProcedure) procedure.getDelegate()).getErrorLogfile(),
									getShell());
						}
					});
				}

				// add some special menu entries to MySQL database
				if (Constants.Procedures.INPROCESS_MYSQL_ID.equalsIgnoreCase(procedure.getMapping().getId())) {
					MenuItem miMysqlIni = new MenuItem(popupmenu, SWT.PUSH);
					miMysqlIni.setText(MessageConstants.CONTEXT_MYSQL_INI);
					miMysqlIni.addListener(SWT.Selection, new Listener() {
						@Override
						public void handleEvent(Event e) {
							// show the my.ini file
							Launcher.getLauncherUI(parent.getDisplay()).showFile(MysqlUtils.MySQL_INI, getShell());
						}
					});
				}

				// if this procedure has a logfile then add a menu
				// item which opens the logfile with the default editor
				// for RAP, this is opened in a dialog box instead
				if (hasLogfile) {
					MenuItem miLogfile = new MenuItem(popupmenu, SWT.PUSH);
					miLogfile.setText(MessageConstants.CONTEXT_LOGFILE);
					miLogfile.addListener(SWT.Selection, new Listener() {
						@Override
						public void handleEvent(Event e) {
							// TODO: currently we always open the logfile with suffix 0.0.log, this might
							// not be the correct one if multiple Customer Frontends are started as part of a scenario...
							File logFile = new File(procedure.getLogfile().replace("%u", "0").replace("%g", "0"));
							Launcher.getLauncherUI(parent.getDisplay()).showFile(logFile, getShell());
						}
					});
				}

				// add a context-menu to open the property-file that was passed to the process
				final File propertyFile = procedure.getPropertyFile();
				if (propertyFile != null) {
					MenuItem miOpenPropertyFile = new MenuItem(popupmenu, SWT.PUSH);
					miOpenPropertyFile.setText(MessageConstants.CONTEXT_PROPERTIES);
					miOpenPropertyFile.addListener(SWT.Selection, new Listener() {
						@Override
						public void handleEvent(Event e) {
							try {
								Launcher.getLauncherUI(parent.getDisplay()).showFile(propertyFile.getCanonicalFile(), getShell());
							} catch (IOException e1) {
								LOGGER.warn("Could not get canonical path of file: " + propertyFile, e);
							}
						}
					});

				}

				// show the tooltip-data also as a popup-dialog as the tooltips are not supported on RAP
				final String detail = procedure.getDetails();
				if (StringUtils.isNotEmpty(detail)) {
					MenuItem miDetail = new MenuItem(popupmenu, SWT.PUSH);
					miDetail.setText(MessageConstants.CONTEXT_DETAILS);
					miDetail.addListener(SWT.Selection, new Listener() {
						@Override
						public void handleEvent(Event e) {
							Shell shell = new Shell(getShell(), SWT.TITLE | SWT.CLOSE | SWT.MAX | SWT.MIN | SWT.RESIZE);
							shell.setImage(getShell().getImage());
							shell.setText("Details");
							shell.setLayout(new FillLayout());
							Composite compo = new Composite(shell, SWT.NONE);
							compo.setLayout(new FillLayout());
							Text text = new Text(compo, SWT.MULTI | SWT.WRAP);
							text.setText(detail);
							shell.open();
						}
					});
				}
			}
		});

		/* Popup menu under left click - Michal.Bakula */
		iconLabel.addListener(SWT.MouseDown, new PopupListener(popupmenu, iconLabel));
		nameLabel.addListener(SWT.MouseDown, new PopupListener(popupmenu, nameLabel));
		stateLabel.addListener(SWT.MouseDown, new PopupListener(popupmenu, stateLabel));

        iconLabel.setMenu(popupmenu);
        nameLabel.setMenu(popupmenu);
        stateLabel.setMenu(popupmenu);

        this.setMenu(popupmenu);

        this.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent event) {
                nameLabel.dispose();
                iconLabel.dispose();
                agentIconLabel.dispose();
                stateLabel.dispose();
                imageManager.disposeImages();
            }
        });
    }

	/**
	 *
	 * @author Michal.Bakula
	 *
	 */
	private class PopupListener implements Listener {
		private final Menu popupmenu;
		private final Label label;

		public PopupListener(final Menu popupmenu, final Label label) {
			super();
			this.popupmenu=popupmenu;
			this.label=label;
		}
		@Override
		public void handleEvent(Event e) {
			Rectangle bounds = label.getBounds();
			Point point = label.getParent().toDisplay(bounds.x + e.x, bounds.y + e.y);
			popupmenu.setLocation(point);
			popupmenu.setVisible(true);
		}
	}

	private void setToolTip() {
		StringBuilder builder = new StringBuilder();

		// newline for lines that are longer than 50 chars
		for(String str : procedure.getDetails().split("\n")) {
			int pos = builder.length();
			builder.append(str).append("\n");
			for(int i = TOOLTIP_CHAR_WIDTH;i < str.length();i+=50) {
				builder.insert(pos + i, "\n");
			}
		}

		// display some details as tooltip
        iconLabel.setToolTipText(TextUtils.getEndEllipsis(builder.toString(), TOOLTIP_CHAR_MAX));
	}

	private void runProcedure() {
		// remove possible "disabled" setting
		enableProcedure();

		ThreadEngine.createBackgroundThread("Procedure Runner " + procedure.getName(), new Runnable() {
           @Override
            public void run() {
                boolean areAllProceduresOperating = procedure.run().isOk();
                areAllProceduresOperating = areAllProceduresOperating && waitUntilOperating(procedure); // ATTENTION: short-circuit AND
                if (continuously) {
                    procedure.setContinuously(true);
                }
            }
        }, getDisplay()).start();
	}

	public void enableProcedure() {
		DefaultProcedureMapping mapping = ((DefaultProcedureMapping)procedure.getMapping());
		if(mapping.getSetting(null, Constants.Misc.SETTING_ENABLED) != null) {
			mapping.removeSetting(mapping.getSetting(null, Constants.Misc.SETTING_ENABLED));
		}
	}

	private void stopProcedure() {
		ThreadEngine.createBackgroundThread("Procedure Stopper " + procedure.getName(), new Runnable() {
            @Override
            public void run() {
        		procedure.stop();
            }
        }, getDisplay()).start();
	}

	private void restartProcedure() {
		// remove possible "disabled" setting
		enableProcedure();

   		ThreadEngine.createBackgroundThread("Procedure Restarter " + procedure.getName(), new Runnable() {
            @Override
            public void run() {
            	// stop and wait
                boolean areAllProceduresStopping = procedure.stop().isOk();
                areAllProceduresStopping = areAllProceduresStopping && waitUntilStopped(); // ATTENTION: short-circuit AND
                if(!areAllProceduresStopping) {
                	LOGGER.warn("Could not stop procedure '" + procedure.getName() + "', not starting again.");
                	return;
                }

                // start again
                boolean areAllProceduresOperating = procedure.run().isOk();
                areAllProceduresOperating = areAllProceduresOperating && waitUntilOperating(procedure); // ATTENTION: short-circuit AND
                if (continuously) {
                    continuously = procedure.setContinuously(true);
                }
            }
        }, getDisplay()).start();
	}

    private final static class StartingFinishedPredicate implements Predicate<Object> {
        private final StatefulProcedure procedure;

        public StartingFinishedPredicate(StatefulProcedure procedure) {
            this.procedure = procedure;
        }

		@Override
		public boolean eval(Object obj) {
            if (!procedure.isEnabled() || !procedure.isOperatingCheckSupported()) {
            	return true;
            }

            // Note: this will also adjust state of the procedure
            return procedure.isStartingFinished();
		}

		@Override
		public boolean shouldStop() {
			return false;
		}
    }

    public static boolean waitUntilOperating(StatefulProcedure procedure) {

    	// Static methods are difficult to mock. Therefore, for testing purposes, we mock it this way.
    	// This code will be always executed, but it is a small overhead.  Besides, it is executed when we
    	// are already  waiting for something anyway, so it should not add any additional delay.
    	// cwpl-wjarosz
    	String tmpName = procedure.getName(); if (tmpName != null && tmpName.equals("unitTest")) { return true; }


    	final EasyTravelConfig config = EasyTravelConfig.read();
        StartingFinishedPredicate nonStartingPredicate = new StartingFinishedPredicate(procedure);


        // allow one timeout period for each procedure
		PredicateMatcher<Object> matcher = new PredicateMatcher<Object>(null, procedure.getTimeout(), config.processOperatingCheckIntervalMs);

        boolean areAllOperating = matcher.waitForMatch(nonStartingPredicate);
        if (!areAllOperating) {
            LOGGER.warn(TextUtils.merge("Unable to wait until {0} is operating. Timeout reached.", procedure.getName()));
            procedure.setState(State.TIMEOUT);
        }

        return areAllOperating;
    }

	private final static class IsStoppedPredicate implements Predicate<Object> {

        private final StatefulProcedure procedure;

        public IsStoppedPredicate(StatefulProcedure procedure) {
            this.procedure = procedure;
        }

        @Override
        public boolean eval(Object unused) {
            if (!procedure.isEnabled()) {
                return true;
            }

            if (procedure.getState().equals(State.STOPPED) ||
            		procedure.getState().equals(State.UNKNOWN) ||
            		procedure.getState().equals(State.FAILED) ||
            		procedure.getState().equals(State.ACCESS_DENIED)) {
                return true;
            }

            return false;
        }

		@Override
		public boolean shouldStop() {
			return false;
		}
    }

    private boolean waitUntilStopped() {
    	final EasyTravelConfig config = EasyTravelConfig.read();
		PredicateMatcher<Object> matcher = new PredicateMatcher<Object>(null, config.syncProcessTimeoutMs, config.processOperatingCheckIntervalMs);

        IsStoppedPredicate nonStartingPredicate = new IsStoppedPredicate(procedure);
        boolean areAllOperating = matcher.waitForMatch(nonStartingPredicate);
        if (!areAllOperating) {
        	LOGGER.warn(TextUtils.merge("Unable to wait until {0} is stopped. Timeout reached.", procedure.getName()));
        }

        return areAllOperating;
    }

    public void updateState() {
        nameLabel.setForeground(getStatefulColor());
        stateLabel.setText(procedure.getStateLabel());
		iconLabel.setImage(getStateImage());
        updateAgentStateIcon();
    }

	private Image getStateImage() {
        return imageManager.createImage(getStateImagePath());
    }

    private String getStateImagePath() {
        switch (procedure.getState()) {
            case OPERATING:
            	if(procedure.isDotNetIISProcedure()) {
            		return Constants.Images.PROCEDURE_OK_IIS;
            	} else if (procedure.isNginxWebserver()) {
                    return Constants.Images.PROCEDURE_OK_NGINX;
                }
                return Constants.Images.PROCEDURE_OK;
            case STARTING:
            case STOPPING:
                if (procedure.isNginxWebserver()) {
                    return Constants.Images.PROCEDURE_PROGRESS_NGINX;
                }
                return Constants.Images.PROCEDURE_PROGRESS;
            case TIMEOUT:
                return Constants.Images.PROCEDURE_PROGRESS_ORANGE;
            case STOPPED:
            case UNKNOWN:
            case ACCESS_DENIED:
            case FAILED:
                if (procedure.isNginxWebserver()) {
                    return Constants.Images.PROCEDURE_STOPPED_NGINX;
                }
                return Constants.Images.PROCEDURE_STOPPED_GRAY;
        }

        throw new IllegalStateException(TextUtils.merge("Unable to find icon to visualize procedure state ''{0}''.", procedure.getState()));
    }

    private Color getStatefulColor() {
    	switch (procedure.getState()) {
			case STOPPED:
	            return Constants.Colors.DARK_GRAY;

			case FAILED:
			case ACCESS_DENIED:
	            return Constants.Colors.DARK_RED;

			default:
		        return Constants.Colors.DARK_BLUE;
		}
    }

	private void updateAgentStateIcon() {
		if (procedure.isInstrumentationSupported()) {
			if (procedure.agentFound()) {
				agentIconLabel.setImage(imageManager.createImage(Constants.Images.PROCEDURE_AGENT_FOUND));
				agentIconLabel.setToolTipText("Agent is available.");
			} else {
				agentIconLabel.setImage(imageManager.createImage(Constants.Images.PROCEDURE_AGENT_NOT_FOUND));
				agentIconLabel.setToolTipText("Agent is not available.");
			}
		} else {
			agentIconLabel.setImage(null);
		}
	}
}
