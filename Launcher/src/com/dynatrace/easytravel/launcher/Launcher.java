package com.dynatrace.easytravel.launcher;

import java.io.File;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.quartz.SchedulerException;

import com.dynatrace.diagnostics.uemload.UemLoadScheduler;
import com.dynatrace.diagnostics.uemload.networkpacketdrop.NetworkPacketDrop;
import com.dynatrace.diagnostics.uemload.scalemicrojourneyservice.ScaleMicroJourneyService;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.diagnostics.uemload.utils.ShutdownUtils;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.ThirdPartyContentProxySelector;
import com.dynatrace.easytravel.config.Version;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.ipc.SocketUtils;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.baseload.BaseLoad;
import com.dynatrace.easytravel.launcher.baseload.BaseLoadManager;
import com.dynatrace.easytravel.launcher.config.ConfigFileInitializer;
import com.dynatrace.easytravel.launcher.config.ScenarioConfiguration;
import com.dynatrace.easytravel.launcher.engine.CloseCallback;
import com.dynatrace.easytravel.launcher.engine.LaunchEngine;
import com.dynatrace.easytravel.launcher.engine.ProcedureFactory;
import com.dynatrace.easytravel.launcher.engine.ThreadEngine;
import com.dynatrace.easytravel.launcher.httpd.ApacheHttpdUtils;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.misc.ProcedureControlPluginListener;
import com.dynatrace.easytravel.launcher.plugin.LoadChange;
import com.dynatrace.easytravel.launcher.pluginscheduler.JobGroupFactory;
import com.dynatrace.easytravel.launcher.procedures.AbstractDotNetProcedure;
import com.dynatrace.easytravel.launcher.procedures.B2BFrontendProcedure;
import com.dynatrace.easytravel.launcher.procedures.PaymentBackendProcedure;
import com.dynatrace.easytravel.launcher.procedures.utils.CentralTechnologyActivator;
import com.dynatrace.easytravel.launcher.remote.HttpServiceThread;
import com.dynatrace.easytravel.launcher.remote.RESTProcedureClient;
import com.dynatrace.easytravel.launcher.security.UserPrincipal;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.logging.RootLogger;
import com.dynatrace.easytravel.pluginscheduler.ChainJobListener;
import com.dynatrace.easytravel.pluginscheduler.Quartz;
import com.dynatrace.easytravel.util.NetstatUtil;
import com.dynatrace.easytravel.util.TextUtils;
import com.dynatrace.easytravel.util.process.HeadlessProcessKillerFactory;

import ch.qos.logback.classic.Logger;

public class Launcher {
	private static final String FILE_IGNOREWINXPERROR = "ignorewinxperror";

	private static final Logger LOGGER = LoggerFactory.make();

	private final static CopyOnWriteArrayList<LauncherUI> launcherUIList = new CopyOnWriteArrayList<LauncherUI>();
	private static final Map<String,ServerPushSession> activeCallbacks = new HashMap<>();

	private static boolean taggedWebRequest = EasyTravelConfig.read().xDynaTraceHeaders;
	private static int baseLoadValue = EasyTravelConfig.read().baseLoadDefault;
	private static String origin = "unknown";
	private static UserPrincipal loggedInUser;

	private static boolean isInitializedForPluginChanges = false;

	private static final AtomicBoolean isWeblauncher = new AtomicBoolean(false);
	static volatile boolean isManualMode = false;

	public static Options createOptions() {
		Options options = new Options();

		Option propertiesFilePath = new Option(BaseConstants.CmdArguments.PROPERTY_FILE, true,
				"the path to the configuration file");
		options.addOption(propertiesFilePath);

        Option manualStart = new Option(Constants.CmdArguments.MANUAL_START, false,
        		"specifies that procedures are not started when a scenario is started, but you can start them manually one by one. This is mostly useful for testing easyTravel itself!");
        options.addOption(manualStart);

        Option help = new Option("h", Constants.CmdArguments.HELP, false,
        		"List usage and options");
        options.addOption(help);

        return options;
    }

	public static void setTaggedWebRequest(boolean taggedWebRequest) {
		Launcher.taggedWebRequest = taggedWebRequest;
	}

	@SuppressWarnings("unchecked")
	public static void main(final String[] args) {
    	ConfigFileInitializer.initializeForLauncher();

    	// commandline can specify the scenario that should be started automatically
    	if(args.length > 0 &&
    			(args[0].equals("-h") || args[0].equals("--help"))) {
	        HelpFormatter formatter = new HelpFormatter();
	        formatter.printHelp( "Launcher <options> [<Autostart-Group>] <Autostart-Scenario>", "Options:", createOptions(), "");
    		return;
    	}

		// try to load the config from the classpath here...
		try {
			LoggerFactory.initLogging();
		} catch (IOException e) {
			System.err.println("Could not initialize logging from classpath: "); // NOPMD
			e.printStackTrace();
		}

		RootLogger.setup(MessageConstants.LAUNCHER);

		Parser parser = new BasicParser();
        try {
            CommandLine commandLine = parser.parse(createOptions(), args);
            String propertiesFilePath = commandLine.getOptionValue(BaseConstants.CmdArguments.PROPERTY_FILE);
            if (propertiesFilePath != null && new File(propertiesFilePath).exists()) {
            	EasyTravelConfig.resetSingleton();
                EasyTravelConfig.createSingleton(propertiesFilePath);
            }

            setAutostartFromCommandline(commandLine.getArgList());

            // initialize plugin job scheduler for Launcher
            initPluginScheduler(MessageConstants.LAUNCHER);

            // pass on the commandline option to the class that creates the Scenario
            ScenarioConfiguration.setManualStart(commandLine.hasOption(Constants.CmdArguments.MANUAL_START));

            addShutdownHook();
            run(new LauncherUI());
        } catch (ParseException pe) {
            LOGGER.warn("Exception parsing Command Line", pe);
        }


	}

	private static void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread("Shutdown hook") {
			@Override
			public void run() {
				// do a println here as logging might already be stopped
                System.out.println("Stopping Engine because shutdown request was received."); // NOSONAR - do a println here as logging might already be stopped
		ShutdownUtils.shutdown();
                LaunchEngine.stop();
			}
        });
	}

	public static void setAutostartFromCommandline(final List<String> args) {
	    if (args.size() > 0) {
	    	EasyTravelConfig config = EasyTravelConfig.read();
            if (args.size() == 1) {
                config.autostart = args.get(0);
            } else {
                config.autostartGroup = args.get(0);
                config.autostart = args.get(1);
            }

	    }
	}

	public static void run(LauncherUI myUI) {
		launcherUIList.add(myUI);
		myUI.init();

		initBaseLoadGenerators();

		Display display = myUI.getDisplay();

		initForPluginChanges();

	    final Shell shell = myUI.createShell();
	    shell.open();
	    final AtomicReference<HttpServiceThread> remoteController = new AtomicReference<HttpServiceThread>(null);
	    final AtomicBoolean stopped = new AtomicBoolean(false);

	    // run checks in background to not block the UI because of it
    	ThreadEngine.createBackgroundThread("Port and Setup Check", new Runnable() {
			@Override
			public void run() {
				runSetupAndPortChecks(shell);

				// we should only start the REST Service after we have checked the ports, otherwise we report our
				// own REST Server as taking away the port
				if (myUI.getUiType().equals(LauncherUIType.SWT) && !stopped.get()) {
					synchronized (remoteController) {
						remoteController.set(startHttpService(myUI.getDisplay()));
					}
				}
			}
	    }, true, display).start();

		// run Launcher Version Detection for distributed environment
		ThreadEngine.createBackgroundThread("Remote Launcher Version Detector", new Runnable() {
			@Override
			public void run() {
				runRemoteLauncherVersionCheck(shell);
			}
		}, true, display).start();

		if (myUI.getRestoreEngine() != null) {
			myUI.useRestoreEngine();
		}

	    // RAP >= 1.5 does not allow a normal GUI-Loop any more,
		// even handling is done differently anyway and
		// we shutdown things separatedely also
		if(isWeblauncher()) {
			return;
		}

	    // the event loop
		while (!shell.isDisposed()) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (Exception e) {
				LOGGER.error("Exception in Launcher main loop", e);
			}
		}

		// avoid starting the HTTP Service if we are faster with shutdown than the port check above
		stopped.set(true);
		synchronized (remoteController) {
			if (remoteController.get() != null) {
				remoteController.get().stopService();
			}
		}

		// stop all running scenarios
		myUI.disposePluginStateRefresher();
		LaunchEngine.stop();

		LOGGER.info("easyTravel Demo Application terminated");
	}

	/**
	 * Display MessageBox when remote launcher versions differs from master-launcher version
	 *
	 * @param shell
	 * @author cwpl-rorzecho
	 */
	public static void runRemoteLauncherVersionCheck(final Shell shell) {
		if(isWeblauncher()) {
		final Semaphore done = new Semaphore(0);
		ThreadEngine.runInDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					boolean retry = true;
					while (retry) {
						retry = false;
						final String remoteLauncherErrorMessage = getRemoteLauncherVersionErrorMessage();
						if (!remoteLauncherErrorMessage.isEmpty()) {
							getLauncherUI(shell.getDisplay()).messageBox(shell, SWT.ICON_WARNING | SWT.OK,
									MessageConstants.LAUNCHER_INCOMPATIBILITY,
									TextUtils.merge(MessageConstants.REMOTE_LAUNCHER_VERSION_DETECTION, remoteLauncherErrorMessage), null);
						}
					}
				} finally {
					done.release();
				}
			}
		}, shell);

		done.acquireUninterruptibly();
	    }
	}

	public static void runSetupAndPortChecks(final Shell shell) {
		// WebLauncher can only check this when no Batch is running
		// In WebLauncher, we get here every time the WebLauncher is opened
		// in a new Browser Window, i.e. all procedures can already be running here...
		// TODO: add these checks for WebLauncher in a different place
		// Also if there is an Autostart defined, we already start procedures here, thus these checks will like report false positives
		if (!isWeblauncher() && StringUtils.isEmpty(EasyTravelConfig.read().autostart) && LaunchEngine.getRunningBatch() == null) {
			final Semaphore done = new Semaphore(0);
			ThreadEngine.runInDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						boolean retry = true;
						while(retry) {
							retry = false;
							final String errorMessage = getAvailablePortsErrorMessage().trim();
							if (!errorMessage.isEmpty() && !errorMessage.equals(EasyTravelConfig.read().lastPortWarning)) {
								MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.ABORT | SWT.RETRY | SWT.IGNORE);
								box.setText(MessageConstants.PORT_ALREADY_IN_USE);
								box.setMessage(TextUtils.merge(MessageConstants.PORT_SEEMS_TO_BE_IN_USE_PREFIX, errorMessage));
								int ret = box.open();
								/*if (ret == SWT.YES) {
									RAPSupport.showFile(new File(EasyTravelConfig.read().filePath), shell);
									exit();
									return null;
								} else*/ if (ret == SWT.ABORT) {
									exit();
								} else if (ret == SWT.RETRY) {
									retry = true;
								} else {
									// store the current port-warning to not show it again unless it changes
									EasyTravelConfig.read().lastPortWarning = errorMessage;
									try {
										EasyTravelConfig.writeLocalSetting("config.lastPortWarning", errorMessage);
									} catch (IOException e) {
										LOGGER.warn("Could not store last port warning text", e);
									}
								}
							}
						}
					} finally {
						done.release();
					}
				}
			}, shell);

			done.acquireUninterruptibly();
		}

		// JLT-44873: Warn if using Windows XP or Windows Server 2003 as .NET Procedures have startup problems here
		if((SystemUtils.IS_OS_WINDOWS_XP ||
				(SystemUtils.IS_OS_WINDOWS && SystemUtils.OS_NAME.startsWith("Windows 2003"))) &&
				!new File(Directories.getConfigDir(), FILE_IGNOREWINXPERROR).exists()) {
			ThreadEngine.runInDisplayThread(new Runnable() {

				@Override
				public void run() {
					getLauncherUI(shell.getDisplay()).messageBox(shell, SWT.ICON_WARNING | SWT.OK /*| SWT.YES | SWT.NO */,
							MessageConstants.DOT_NET_PROCEDURES_MIGHT_NOT_WORK_TITLE,
							MessageConstants.DOT_NET_PROCEDURES_MIGHT_NOT_WORK, null);
					try {
						FileUtils.write(new File(Directories.getConfigDir(), FILE_IGNOREWINXPERROR), "shown on " + new Date());
					} catch (IOException e) {
						LOGGER.warn("Could not write ignore-file at: " + new File(Directories.getConfigDir(), FILE_IGNOREWINXPERROR), e);
					}
					/*if(ret == SWT.YES) {
				        RAPSupport.showFile(new File(Directories.getConfigDir(), Constants.Misc.SCENARIOS_FILE), shell);
						exit();
						return null;
					}*/
				}
			}, shell);
		}

		// check for COR_PROFILER being set either as environment variable or as part of the .NET procedure config properties
		// config.paymentBackendEnvArgs=DT_WAIT=5,RUXIT_WAIT=5,COR_ENABLE_PROFILING=0x1,COR_PROFILER={DA7CFC47-3E35-4c4e-B495-534F93B28683}
		EasyTravelConfig config = EasyTravelConfig.read();
		if(SystemUtils.IS_OS_WINDOWS && CentralTechnologyActivator.getIntance().isAllowed(Technology.DOTNET_20) &&
				// only check if one of the .NET procedures is running locally and not both are running on IIS
				(!B2BFrontendProcedure.checkIfRunningOnLocalIIS() || !PaymentBackendProcedure.checkIfRunningOnLocalIIS())) {
			if(System.getenv("COR_PROFILER") == null &&
					(!starts(config.paymentBackendEnvArgs, "COR_PROFILER=") ||
							!starts(config.b2bFrontendEnvArgs, "COR_PROFILER=")
					)) {
				ThreadEngine.runInDisplayThread(new Runnable() {

					@Override
					public void run() {
						getLauncherUI(shell.getDisplay()).messageBox(shell, SWT.ICON_WARNING | SWT.OK | SWT.CANCEL,
							MessageConstants.DOT_NET_AGENT_SETUP,
							MessageConstants.COR_PROFILER_NOT_SET,
							new CloseCallback() {

								@Override
								public void dialogClosed(int returnCode) {
									if (returnCode == SWT.CANCEL) {
										exit();
									}
								}
						});
					}
				}, shell);
			}
		}

		// finally apply proxy settings if we did not yet apply them elsewhere (i.e. Weblauncher sets them during startup)
		if (!(ProxySelector.getDefault() instanceof ThirdPartyContentProxySelector)) {
			ThirdPartyContentProxySelector.applyProxy();
		}
	}

	private static boolean starts(String[] args, String starts) {
		for(String str : args) {
			if(str.startsWith(starts)) {
				return true;
			}
		}
		return false;
	}

	private static HttpServiceThread startHttpService(Display display) {
		Runnable exitInDisplayThreadRunnable = new Runnable() {

			@Override
			public void run() {
				ThreadEngine.runInDisplayThread(new Runnable() {

					@Override
					public void run() {
						exit();
					}
				}, display);
			}
		};

		HttpServiceThread remoteController;
		EasyTravelConfig config = EasyTravelConfig.read();
		try {
			remoteController = new HttpServiceThread(config.launcherHttpPort, exitInDisplayThreadRunnable);
			remoteController.start();
			return remoteController;
		} catch (IOException e) {
			LOGGER.warn("Unable to start HTTP Service at port " + config.launcherHttpPort + ": " + e.getMessage());
		}

		return null;
	}

	public static void exit() {
		for (LauncherUI launcher : launcherUIList) {
			Display display = launcher.getDisplay();
			if (display == null) {
				LOGGER.warn("Unable to exit Launcher. Display already disposed.");
				continue;
			}

			Display toDispose = display;
			display = null;
			toDispose.dispose();
		}


		UemLoadScheduler.shutdownNow();
		
		tryKillApacheOnLinuxAfterWaitingForChrome();
	}
	
	private static void tryKillApacheOnLinuxAfterWaitingForChrome() {
		LOGGER.debug("Trying to kill apache at easyTravel shutdown");
		if (!ApacheHttpdUtils.isUsedOsWindows()) {
			LOGGER.debug("The system is not windows - trying to kill apache after chrome synchro");
			try {
				int count=0;
				int retries=100;
				
				LOGGER.debug("Waiting for chrome killer to finish");
				while (HeadlessProcessKillerFactory.isStopInProgress() && count++ < retries) {
					Thread.sleep(500);
				}
				LOGGER.debug("Waiting finished after " + count + " sleeps");
				
				ApacheHttpdUtils.killIfNotTerminatedLinux();	
			}
			catch (Exception e) {
				LOGGER.debug("Stopping the apache failed with an exception", e);
			}	
		}
		else {
			LOGGER.debug("The system is windows, so we don't kill apache");
		}
	}

	private static String getRemoteLauncherVersionErrorMessage() {
		Collection<String> allRemoteHosts = ProcedureFactory.getAllRemoteHosts();
		if(allRemoteHosts.isEmpty()) {
			return BaseConstants.EMPTY_STRING;
		}

		String masterLauncherHost = ProcedureFactory.getMasterLauncherHost();
		Version masterLauncherVersion = Version.read();
		RESTProcedureClient client = new RESTProcedureClient();

		StringBuilder builder = new StringBuilder();

		appendVersionDetection(builder, MessageConstants.MASTER_LAUNCHER_VERSION, masterLauncherHost, masterLauncherVersion.toString());

		for (String remoteHost : allRemoteHosts) {
			Version remoteLauncherVerison = client.getLauncherVersion(remoteHost);
			if (isRemoteLauncherAvailable(remoteLauncherVerison)) {
				if (isLauncherVersionCompatibile(masterLauncherVersion, remoteLauncherVerison)) {
					appendVersionDetection(builder, MessageConstants.REMOTE_LAUNCHER_VERSION, remoteHost, remoteLauncherVerison.toString());
				} else {
					launchersAreTheSame(builder);
				}
			} else {
				appendLauncherDetection(builder, MessageConstants.NO_CMD_LAUNCHER, remoteHost);
			}
		}
		return builder.toString();
	}

	private static boolean isRemoteLauncherAvailable(Version remoteLauncherVersion) {
		return remoteLauncherVersion != null;
	}

	private static boolean isLauncherVersionCompatibile(Version masterLauncherVersion, Version remoteLauncherVersion) {
		return !masterLauncherVersion.equals(remoteLauncherVersion);
	}

	private static void launchersAreTheSame(StringBuilder builder) {
		builder.setLength(0);
	}

	private static void appendLauncherDetection(StringBuilder builder, String messageConstant, String remoteHost) {
		String message = TextUtils.merge(messageConstant, remoteHost);
		builder.append(message);
		builder.append(BaseConstants.CRLF);

		LOGGER.warn(message);
	}

	private static void appendVersionDetection(StringBuilder builder, String messageConstant, String remoteHost, String version) {
		String message = TextUtils.merge(messageConstant, remoteHost, version);
		builder.append(message);
		builder.append(BaseConstants.CRLF);

		LOGGER.warn(message);
	}

	private static String getAvailablePortsErrorMessage() {
		EasyTravelConfig config = EasyTravelConfig.read();
		StringBuilder builder = new StringBuilder();

		// only check ports if we are actually starting these things locally
		if (!ProcedureFactory.isRemote("com.dynatrace.easytravel.host.business_backend")) {
			checkPort(config.backendPort, builder, "Java Backend");
			checkPort(config.backendShutdownPort, builder, "Java Backend Shutdown");
		}

		if (!ProcedureFactory.isRemote("com.dynatrace.easytravel.host.b2b_frontend")) {
			NetstatUtil netstatUtil = new NetstatUtil(Runtime.getRuntime());
			String process = netstatUtil.findProcessForPort(config.b2bFrontendPortRangeStart);
			if (!AbstractDotNetProcedure.isProcessOnPortWhitelist(process)) {
				appendUsedPort(config.b2bFrontendPortRangeStart, builder, ".NET Frontend", process);
			} else if (!B2BFrontendProcedure.checkIfRunningOnLocalIIS()) {
				checkPort(config.b2bFrontendPortRangeStart, builder, ".NET Frontend");
			}
		}
		if (!ProcedureFactory.isRemote("com.dynatrace.easytravel.host.payment_backend")) {
			NetstatUtil netstatUtil = new NetstatUtil(Runtime.getRuntime());
			String process = netstatUtil.findProcessForPort(config.paymentBackendPort);
			if (!AbstractDotNetProcedure.isProcessOnPortWhitelist(process)) {
				appendUsedPort(config.paymentBackendPort, builder, ".NET Backend", process);
			} else if (!PaymentBackendProcedure.checkIfRunningOnLocalIIS()) {
				checkPort(config.paymentBackendPort, builder, ".NET Backend");
			}
		}

		if (!ProcedureFactory.isRemote("com.dynatrace.easytravel.host.customer_frontend")) {
			checkPort(config.frontendPortRangeStart, builder, "Java Frontend");
			checkPort(config.frontendShutdownPortRangeStart, builder, "Java Frontend Shutdown");
			checkPort(config.frontendAjpPortRangeStart, builder, "Java Frontend AJP");
		}
		if (config.internalDatabaseEnabled) {
			checkPort(config.internalDatabasePort, builder, "Internal Database");
		}
		checkPort(config.launcherHttpPort, builder, "Launcher REST Server");
		checkPort(config.weblauncherPort, builder, "WebLauncher");
		checkPort(config.weblauncherShutdownPort, builder, "WebLauncher Shutdown");
		checkPort(config.apacheWebServerPort, builder, "Apache HTTPD Java");
		checkPort(config.apacheWebServerB2bPort, builder, "Apache HTTPD .NET");
		if (config.apacheWebServerProxyPort > 0) {
			checkPort(config.apacheWebServerProxyPort, builder, "Apache HTTPD Proxy");
		}
		if(config.apacheWebServerStatusPort > 0) {
			checkPort(config.apacheWebServerStatusPort, builder, "Apache HTTPD Status");
		}
		checkPort(config.memcachedServerPort, builder, "Memcached Server");

    	checkPort(config.creditCardAuthorizationSocketPort, builder, "CreditCardAuthorization-Socket");

		if (!ProcedureFactory.isRemote(config.couchDBHost)) {
			checkPort(config.couchDBPort, builder, "CouchDB Database");
			checkPort(config.couchDBShutdownPort, builder, "CouchDB Database");
		}

		return builder.toString();
	}

	private static void checkPort(int port, StringBuilder builder, String purpose) {
		boolean available = true;
		available &= SocketUtils.isPortAvailable(port, null);
		available &= SocketUtils.isPortAvailable(port, "localhost");
		if (!available) {
			appendUsedPort(port, builder, purpose, /* usedBy */null);
		}
	}

	private static void appendUsedPort(int port, StringBuilder builder, String purpose, String usedBy) {
		if (builder.length() > 0) {
			builder.append(BaseConstants.COMMA_WS);
		}
		String usedByDesc = usedBy == null ? BaseConstants.EMPTY_STRING : String.format("; port used by '%s'", usedBy);
		builder.append(Integer.toString(port)).append("(").append(purpose).append(usedByDesc).append(")");
	}

	public static void initPluginScheduler(String initContext) {
        EasyTravelConfig config = EasyTravelConfig.read();

        if (config.pluginSchedulerEnabled) {
            Quartz.initialize();
            LOGGER.info(TextUtils.merge("PluginScheduler initialized for: {0} ", initContext));
            try {
                Quartz.getScheduler().pauseJobs(JobGroupFactory.getScenarioGroup());
                Quartz.getScheduler().getListenerManager().addJobListener(new ChainJobListener());
                Quartz.getScheduler().start();
                LOGGER.info(TextUtils.merge("Scheduler {0} has been started", Quartz.getSchedulerInstanceName()));
            } catch (SchedulerException e) {
                LOGGER.error("Quartz Scheduler cannot be started", e);
            }
        }
    }

	public static void initForPluginChanges() {
		if (!isInitializedForPluginChanges) {
			PluginChangeMonitor.registerForPluginChanges(new NetworkPacketDrop());
			PluginChangeMonitor.registerForPluginChanges(new ScaleMicroJourneyService());
			PluginChangeMonitor.registerForPluginChanges(new ProcedureControlPluginListener());
			PluginChangeMonitor.registerForPluginChanges(new LoadChange());
			isInitializedForPluginChanges = true;
		}
	}

	public static CopyOnWriteArrayList<LauncherUI> getLauncherUIList() {
		return launcherUIList;
	}

	public static LauncherUI getLauncherUI(Display display) {
		if (display != null) {
			for(LauncherUI launcher : launcherUIList){
			    if(launcher.getDisplay() == display){
			        return launcher;
			    }
			}
		}

		return null;
	}

	public static void cleanupLauncherUIList(Display displayToRemove) {
		for (int i = launcherUIList.size(); i > 0; i--) {
			LauncherUI ui = launcherUIList.get(i-1);
			Display display = ui.getDisplay();
			if (display == null || display.isDisposed() || display == displayToRemove) {
				launcherUIList.remove(ui);
			}
		}
	}

	public static boolean isWidgetDisposed(Widget widget) {
		if (widget == null || widget.isDisposed() || widget.getDisplay() == null) {
			return true;
		}

		// test if the display is out-dated (Browser Reload F5)
		LauncherUI result = getLauncherUI(widget.getDisplay());
		return widget == null || widget.isDisposed() || result == null;
	}

	public static void notifyBackgroundActionStart(String actionId, Thread thread) {
		ServerPushSession serverSession = new ServerPushSession();
		activeCallbacks.put(actionId, serverSession);
		serverSession.start();
		if(LOGGER.isTraceEnabled() && thread != null) {
			LOGGER.trace("start: " + actionId + " " + thread.getName());
			LOGGER.trace("active callbacks: " + activeCallbacks);
		}
	}

	public static void notifyBackgroundActionEnd(String actionId, Thread thread) {
		ServerPushSession serverSession = activeCallbacks.remove(actionId);
		if(serverSession != null) {
			serverSession.stop();
		}
		if(LOGGER.isTraceEnabled() && thread != null) {
			LOGGER.trace("finish: " + actionId  + " " + thread.getName());
			LOGGER.trace("active callbacks: " + activeCallbacks);

		}
	}

	public static boolean getTaggedWebRequest() {
		return taggedWebRequest;
	}

	public static void updateTaggedWebRequests(boolean enabled) {
		taggedWebRequest = enabled;
		BaseLoadManager.getInstance().getCustomerLoadController().setTaggeWebRequest(taggedWebRequest);
		BaseLoadManager.getInstance().getB2bLoadController().setTaggeWebRequest(taggedWebRequest);
		BaseLoadManager.getInstance().getMobileNativeLoadController().setTaggeWebRequest(taggedWebRequest);
		BaseLoadManager.getInstance().getMobileBrowserLoadController().setTaggeWebRequest(taggedWebRequest);
		BaseLoadManager.getInstance().getHotDealLoadController().setTaggeWebRequest(taggedWebRequest);
		BaseLoadManager.getInstance().getIotDevicesLoadController().setTaggeWebRequest(taggedWebRequest);
		BaseLoadManager.getInstance().getHeadlessMobileAngularLoadController().setTaggeWebRequest(taggedWebRequest);
		BaseLoadManager.getInstance().getHeadlessB2BLoadController().setTaggeWebRequest(taggedWebRequest);
	}

	public static void setTaggedWebRequests(boolean enabled) {
		Launcher.updateTaggedWebRequests(enabled);

		for (LauncherUI launcher : Launcher.getLauncherUIList()) {
			launcher.setTaggedWebRequests(enabled);
		}
	}

	/**
	 * for tests only
	 *
	 * @return value of taggedWebRequest
	 */
	public static boolean isTaggedWebRequest() {
		return taggedWebRequest;
	}

	/**
	 * for tests only
	 */
	public static int getBaseLoadValue() {
		return baseLoadValue;
	}

	public static void setBaseLoadValue(int loadValue) {
		baseLoadValue = loadValue;
		updateLoadValue(loadValue);
	}

	/**
	 * The load is only set if the channels are not blocked. This is the case if the channels are disabled.
	 */
	public static void updateLoadValue(int loadValue) {
		if (!isManualMode) {
			if (!BaseLoadManager.getInstance().getCustomerLoadController().isSchedulingBlocked()) {
				BaseLoadManager.getInstance().setCustomerBaseLoad(loadValue);
			}
			if (!BaseLoadManager.getInstance().getMobileNativeLoadController().isSchedulingBlocked()) {
				BaseLoadManager.getInstance().setMobileNativeBaseLoad(loadValue);
			}
			if (!BaseLoadManager.getInstance().getMobileBrowserLoadController().isSchedulingBlocked()) {
				BaseLoadManager.getInstance().setMobileBrowserBaseLoad(loadValue);
			}
			BaseLoadManager.getInstance().setB2bBaseLoad(loadValue);
			BaseLoadManager.getInstance().setHeadlessCustomerBaseLoad(loadValue);
			BaseLoadManager.getInstance().setHeadlessAngularBaseLoad(loadValue);
			BaseLoadManager.getInstance().setHeadlessMobileAngularBaseLoad(loadValue);
			BaseLoadManager.getInstance().setHeadlessB2BBaseLoad(loadValue);
		}
	}
	
	public static void setLoadValue(int loadValue) {
		Launcher.setBaseLoadValue(loadValue);
		
		for (LauncherUI launcher : Launcher.getLauncherUIList()) {
			launcher.setLoadValue(loadValue);
		}
	}

	private static void initBaseLoadGenerators() {
		EasyTravelConfig config = EasyTravelConfig.read();
		if (baseLoadValue < 0) {
			baseLoadValue = config.baseLoadDefault;
		}

		BaseLoadManager.getInstance().getCustomerBaseLoadInstance(config.getCustomerTrafficScenario(), baseLoadValue, config.baseLoadCustomerRatio,
				taggedWebRequest);

		BaseLoadManager.getInstance().getB2BBaseLoadInstance(baseLoadValue, config.baseLoadB2BRatio, Launcher.getTaggedWebRequest());

		BaseLoadManager.getInstance().getMobileNativeBaseLoadInstance(baseLoadValue, config.baseLoadMobileNativeRatio, taggedWebRequest);

		BaseLoadManager.getInstance().getMobileBrowserBaseLoadInstance(baseLoadValue, config.baseLoadMobileBrowserRatio, taggedWebRequest);

		BaseLoadManager.getInstance().getHotDealBaseLoadInstance(baseLoadValue, config.baseLoadHotDealServiceRatio, taggedWebRequest);

		BaseLoadManager.getInstance().getIotDevicesBaseLoadInstance(baseLoadValue, config.baseLoadIotDevicesRatio, taggedWebRequest);

		BaseLoadManager.getInstance().getHeadlessCustomerBaseLoadInstance(config.getHeadlessTrafficScenario(), baseLoadValue, config.baseLoadHeadlessCustomerRatio);

		BaseLoadManager.getInstance().getHeadlessAngularBaseLoadInstance(config.getHeadlessTrafficScenario(), baseLoadValue, config.baseLoadHeadlessAngularRatio);

		BaseLoadManager.getInstance().getHeadlessMobileAngularBaseLoadInstance(baseLoadValue, config.baseLoadHeadlessMobileAngularRatio);
		
		BaseLoadManager.getInstance().getHeadlessB2BBaseLoadInstance(baseLoadValue, config.baseLoadHeadlessB2BRatio, taggedWebRequest);
	}

	public static BaseLoad getCustomerBaseLoad() {
		initBaseLoadGenerators();
		return BaseLoadManager.getInstance().getCustomerLoadController();
	}

	public static BaseLoad getB2BBaseLoad() {
		initBaseLoadGenerators();
		return BaseLoadManager.getInstance().getB2bLoadController();
	}

	public static BaseLoad getMobileNativeBaseLoad() {
		initBaseLoadGenerators();
		return BaseLoadManager.getInstance().getMobileNativeLoadController();
	}

	public static BaseLoad getMobileBrowserBaseLoad() {
		initBaseLoadGenerators();
		return BaseLoadManager.getInstance().getMobileBrowserLoadController();
	}

	public static BaseLoad getHotDealBaseLoad() {
		initBaseLoadGenerators();
		return BaseLoadManager.getInstance().getHotDealLoadController();
	}

	public static BaseLoad getIotDevicesBaseLoad() {
		initBaseLoadGenerators();
		return BaseLoadManager.getInstance().getIotDevicesLoadController();
	}

	public static BaseLoad getHeadlessCustomerBaseLoad() {
		initBaseLoadGenerators();
		return BaseLoadManager.getInstance().getHeadlessCustomerLoadController();
	}

	public static BaseLoad getHeadlessAngularBaseLoad() {
		initBaseLoadGenerators();
		return BaseLoadManager.getInstance().getHeadlessAngularLoadController();
	}

	public static BaseLoad getHeadlessMobileAngularBaseLoad() {
		initBaseLoadGenerators();
		return BaseLoadManager.getInstance().getHeadlessMobileAngularLoadController();
	}
	
	public static BaseLoad getHeadlessB2BBaseLoad() {
		initBaseLoadGenerators();
		return BaseLoadManager.getInstance().getHeadlessB2BLoadController();
	}

	public static void setOrigin(String newOrigin) {
		origin = newOrigin;
	}

	public static String getOrigin() {
		return origin;
	}

	public static UserPrincipal getLoggedInUser() {
		return loggedInUser;
	}

	public static void setLoggedInUser(UserPrincipal userPrincipal) {
		loggedInUser = userPrincipal;
	}

	public static boolean isLoggedInUser() {
		if (getLoggedInUser() != null) {
			return true;
		}
		return false;
	}

	public static boolean isWeblauncher() {
		return isWeblauncher.get();
	}

	public static void setIsWeblauncher(boolean newValue) {
		isWeblauncher.set(newValue);
	}

	public static void setManualMode(boolean enabled) {
		isManualMode = enabled;
	}

	//for tests
	public static void addLauncherUI(LauncherUI testUI) {
		launcherUIList.add(testUI);
	}
	//for tests
	public static void addLauncherUI(Display testDisplay) {
		LauncherUI ui = new LauncherUI();
		ui.setDisplay(testDisplay);
		launcherUIList.add(ui);
	}
}
