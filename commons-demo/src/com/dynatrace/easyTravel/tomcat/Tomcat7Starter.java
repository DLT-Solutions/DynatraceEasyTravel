package com.dynatrace.easytravel.tomcat;

import static com.dynatrace.easytravel.constants.BaseConstants.PIPE;
import static com.dynatrace.easytravel.constants.BaseConstants.UEM_LOAD_HOST_AVAILABILITY_THREAD;
import static com.dynatrace.easytravel.constants.BaseConstants.UEM_LOAD_HOST_PLUGIN_ENABLEMENT_WATCHER_THREAD;
import static com.dynatrace.easytravel.constants.BaseConstants.UEM_LOAD_THREAD;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Manager;
import org.apache.catalina.Server;
import org.apache.catalina.Session;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.deploy.SecurityCollection;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.loader.WebappClassLoader;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.session.PersistentManager;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.ErrorReportValve;
import org.apache.commons.io.FileUtils;
import org.apache.coyote.AbstractProtocol;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.Security;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.logging.LoggingSuppresser;
import com.dynatrace.easytravel.spring.PluginFinder;
import com.dynatrace.easytravel.spring.PluginList;
import com.dynatrace.easytravel.spring.SpringUtils;
import com.dynatrace.easytravel.spring.TomcatHolderBean;
import com.dynatrace.easytravel.util.CallbackRunnable;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

public class Tomcat7Starter {

	private static final String CERTIFICATE_PATH = Directories.getInstallDir() + File.separator + "resources" + File.separator + "easyTravelDemo.jks";
	private static final String KEYSTORE_PASSWORD = "adminpass";
	private static final String CERT_ALIAS = "easyTravelDemo";
	private static Runnable shutdownExecutor = null;

	private static final String MEMORY_LEAK_LOG_REGEX = Pattern.quote("The web application [/] appears to have started a thread named [") +
			"(" +
			UEM_LOAD_THREAD + PIPE + UEM_LOAD_HOST_PLUGIN_ENABLEMENT_WATCHER_THREAD + PIPE +
			UEM_LOAD_HOST_AVAILABILITY_THREAD + ").*";

	private static final Logger log = LoggerFactory.make();
	private int shutdownPort;
	private AutomaticMemoryManager manager;
	private String serverName;

	private Context lastContext = null;
	
	/**
	 * Run the Tomcat and return the context created.
	 *
	 */		
	public Tomcat run(Tomcat7Config config) throws IOException, LifecycleException {
		// needed for PersistentManager to work properly
		// System.setProperty("org.apache.catalina.session.StandardSession.ACTIVITY_CHECK", "true");

		this.shutdownPort = config.getShutdownPort();

		String hostname = config.getHostname();
		int port = config.getPort();
		
		String webDir = getWebDirInternal(config);
		
		String currentDir = new File(".").getCanonicalPath();
		String tomcatDir = resolveDir(currentDir, "tomcat", port, false);
		String workDir = Directories.getWorkDir(port).getAbsolutePath();

		log.info("starting from webDir=" + webDir + ", tomcatDir=" + tomcatDir + ", workDir=" + workDir);

		if (new File(workDir).exists()) {
			if (!FileUtils.deleteQuietly(new File(workDir))) {
				log.warn("Could not delete existing workDir at " + workDir);
			}
		}

		Tomcat tomcat = new Tomcat();
		
		if (hostname != null && !hostname.isEmpty() && !BaseConstants.LOCALHOST.equals(hostname)) {
			registerHttpConnector(tomcat, hostname, port);
		}
		tomcat.setPort(port);
		tomcat.setBaseDir(tomcatDir);
		((StandardHost) tomcat.getHost()).setWorkDir(workDir);
		((StandardHost) tomcat.getHost()).setUnpackWARs(false);
		ErrorReportValve errorReportValve = new ErrorReportValve();
		errorReportValve.setShowReport(false);
		errorReportValve.setShowServerInfo(false);
		tomcat.getHost().getPipeline().addValve(errorReportValve);
		

		// make Tomcat requests talk UTF-8 so e.g. one can search for journeys to 'Mödling'
		tomcat.getConnector().setURIEncoding(BaseConstants.UTF8);

		serverName = "Tomcat@" + tomcat.getServer().getAddress() + ":" + port;

		Context context = tomcat.addWebapp(parseContext(config.getContextRoot()), webDir);
		context.setCookies(config.isCookies());
		registerPlugins(context, config.getParentClass());

		if (config.isAddAuthentication()) {
			context.setCookies(true);
			useOrCreateAuthenticatedUsersFile(new File(Directories.getConfigDir(), Security.WEB_LAUNCHER_USERS));
			registerSSLConnector(tomcat, EasyTravelConfig.read().sslPort);
			context.addFilterDef(securityFilterDef());
			context.addFilterMap(securitFilterMapping());
			context.addConstraint(sslSecurityConstraint());
			log.info("WebLauncher Authentication and SSL encryption has been enabled");
		}

		if (shutdownPort > 0) {
			registerShutdownListener(shutdownPort, tomcat.getServer());
		}
		String routePrefix = config.getRoutePrefix();
		int ajpPort = config.getAjpPort();
		if (ajpPort > 0) { // add an AJP connector for usage with Apache
			registerAjpConnector(routePrefix, ajpPort, tomcat, port == 0);
		} else {
			tomcat.getEngine().setJvmRoute(routePrefix + "jvmRoute-0");
		}

		tomcat.start();

		if (config.isAddPersistentSessionManager()) {
			createPersistentManager(workDir, context);
		}
		addCleanupCallback(context.getManager());

		// JLT-80446: disable persisting in any started Tomcat as we never want to persist sessions upon shutdown
		if(context.getManager() instanceof StandardManager) {
			log.info("Disabling persisting to file: " + ((StandardManager)context.getManager()).getPathname());
			((StandardManager)context.getManager()).setPathname(null);
		}

		new LoggingSuppresser(WebappClassLoader.class.getName()).addLogPatternToSuppress(MEMORY_LEAK_LOG_REGEX).suppressLogging();

		registerTomcatInSpringContext(tomcat);

		lastContext = context;
		return tomcat;
	}
	
	private String getWebDirInternal(Tomcat7Config config) throws IOException {
		if (config.isWebappIsAbsolute()) {
			return resolveDir(config.getWebappBase(), config.getPort(), false);
		} else {
			String currentDir = new File(".").getCanonicalPath();
			return resolveDir(getWebDir(currentDir), config.getWebappBase(), config.getPort(), false);
		}

	}

	/**
	 * Return the created context.  This can be used to e.g. add a servlet to the context.
	 * @return
	 */
	public Context getContext() {
		return lastContext;
	}

    /**
     * configures number of max. threads, connections and backlog of the tomcat frontend server
     *
     * @param tomcat
     * @author cwat-shauser
     */
	public static void adjustThreads(Tomcat tomcat, int maxBacklog, int maxThreads, int maxConnections) {
		log.info("Setting max-threads for the Tomcat Protocol Handler to: " + maxThreads);
		if (tomcat.getConnector() != null && tomcat.getConnector().getProtocolHandler() != null &&
				tomcat.getConnector().getProtocolHandler() instanceof AbstractProtocol) {

			AbstractProtocol httpProtocolHandler = (AbstractProtocol) tomcat.getConnector().getProtocolHandler();
			httpProtocolHandler.setTcpNoDelay(true);
			httpProtocolHandler.setBacklog(maxBacklog);
			httpProtocolHandler.setMaxThreads(maxThreads);
			httpProtocolHandler.setMaxConnections(maxConnections);
		}
	}

	/**
	 * Security Filter definition
	 * @return
	 */
	private FilterDef securityFilterDef() {
		FilterDef securityFilterDef = new FilterDef();
		securityFilterDef.setFilterName("SecurityFilter");
		securityFilterDef.setFilterClass("com.dynatrace.easytravel.weblauncher.security.SecurityFilter");
		return securityFilterDef;
	}

	/**
	 * Security filter mapping
	 * @return
	 */
	private FilterMap securitFilterMapping() {
		FilterMap securityFilterMapping = new FilterMap();
		securityFilterMapping.setFilterName("SecurityFilter");
		securityFilterMapping.addURLPattern("/main");
		return securityFilterMapping;
	}

	/**
	 * Put tomcat instance to the spring context
	 * @param tomcat
	 * @author cwpl-rpsciuk
	 */
	private void registerTomcatInSpringContext(Tomcat tomcat) {
		try{
			TomcatHolderBean tomcatHolder = SpringUtils.getBean("tomcatHolderBean", TomcatHolderBean.class);
			tomcatHolder.setTomcat(tomcat);
		} catch (Exception e) {
			log.info("Tomcat instance will not be registered in the spring contex.");
			log.debug("Error registering tomcat in spring context.", e);
		}
	}

	/**
	 * Add new context with given parameters
	 * @param tomcat
	 * @param port
	 * @param contextRoot
	 * @param webappBase
	 * @return created web application context
	 * @throws IOException
	 * @throws ServletException
	 */
	public Context addContext(Tomcat tomcat, int port, String contextRoot, String webappBase) throws IOException, ServletException {
		if (contextRoot == null || webappBase == null) {
			log.warn("Invalid context prameters: " + contextRoot + " " + webappBase);
			return null;
		}
		log.info(TextUtils.merge("Adding context. contextroot: {0} webAppBase: {1} port: {2}", contextRoot, webappBase, port ));
		String currentDir = new File(".").getCanonicalPath();
		String webDir = resolveDir(getWebDir(currentDir), webappBase, port, false);
		Context webapp = tomcat.addWebapp(parseContext(contextRoot), webDir);
		webapp.setReloadable(true);
		return webapp;
	}

	protected String getWebDir(String currentDir) throws IOException {
		return currentDir;
	}

	private String parseContext(String context) {
		if (context == null || context.equals("/")) {
			return "";
		}

		return removeTrailingSlashes(context);
	}

	private String removeTrailingSlashes(String context) {
		String newContext = "";
		if (context != null && context.length() > 0) {
			newContext = context;
			while (newContext.endsWith("/")) {
				newContext = newContext.substring(0, newContext.length() - 1);
			}
		}
		return newContext;
	}

	/**
	 * Add a CLEANUP callback that clears the session, and also the session store,
	 * in case of a PersistentManager.
	 *
	 * @param manager
	 * @author philipp.grasboeck
	 */
	private void addCleanupCallback(final Manager manager) {
		CallbackRunnable.CLEANUP.add(new Runnable() {

			@Override
			public void run() {
				if (manager instanceof PersistentManager) {
					if (log.isInfoEnabled()) {
						log.info("Discarding session store...");
					}
					((PersistentManager) manager).clearStore();
				}

				Session[] sessions = manager.findSessions();
				if (log.isInfoEnabled()) {
					log.info("Discarding all sessions... count=" + sessions.length);
				}
				for (Session session : sessions) {
					manager.remove(session);
				}
			}
		});
	}

	/**
	 * Create a PersistentManager for this context that stores sessions in workDir.
	 *
	 * @param workDir
	 * @param context
	 * @return
	 * @author philipp.grasboeck
	 */
	protected void createPersistentManager(String workDir, Context context) {
		// Override the default Manager and set a persistent manager which can store Sessions on disk
		/*
		 * PersistentManager manager = new PersistentManager();
		 * FileStore store = new FileStore();
		 * store.setDirectory(new File(workDir, "store").getAbsolutePath());
		 * manager.setStore(store);
		 */
		manager = new CleanupMemoryManager(serverName);
		context.setManager(manager);
	}

	/**
	 * SSL enablement
	 * @return
	 */
	private SecurityConstraint sslSecurityConstraint() {
		SecurityConstraint securityConstraint = new SecurityConstraint();
		securityConstraint.setUserConstraint("CONFIDENTIAL");
		securityConstraint.addCollection(sslSecurityCollection());
		return securityConstraint;
	}

	/**
	 * Pattern for SSL encryption
	 * @return
	 */
	private SecurityCollection sslSecurityCollection() {
		SecurityCollection securityCollection = new SecurityCollection();
		securityCollection.addPattern("/*");
		return securityCollection;
	}

	/**
	 * Create default WebLauncher Users configuration file
	 * @param webLauncherUsersFile
	 * @throws IOException
	 */
	private void useOrCreateAuthenticatedUsersFile(File webLauncherUsersFile) throws IOException {
		if (!webLauncherUsersFile.exists()) {
			DefaultWebLauncherUsers.create(webLauncherUsersFile);
			log.info("Default WebLauncher Users file has been created: " + webLauncherUsersFile.getAbsoluteFile());
		}
	}

	/**
	 * If a shutdown port was provided during startup then send ourselves a
	 * shutdown command to stop the Tomcat server.
	 *
	 * @throws LifecycleException
	 * @throws IOException
	 * @author dominik.stadler
	 */
	public void stop() throws LifecycleException, IOException {
		if (shutdownPort <= 0) {
			log.warn("Cannot stop because shutdownPort is not set");
			return;
		}

		try {
			Socket socket = new Socket(InetAddress.getByName(null), shutdownPort);
			try {
				byte[] buffer = BaseConstants.TOMCAT_SHUTDOWN.getBytes();

				OutputStream outStream = socket.getOutputStream();
				try {
					outStream.write(buffer, 0, buffer.length);
					outStream.flush();

					log.info(TextUtils.merge("Shutdown command successfully sent to ''{0}:{1,number,#}''.",
							InetAddress.getByName(null).getHostName(), shutdownPort));
				} finally {
					outStream.close();
				}
			} finally {
				socket.close();
			}
		} catch (IOException ex) {
			log.warn("Failed to write out shutdown command.", ex);
		}
	}

	private String resolveDir(String parentName, String childName, int port, boolean tryPortSpecific) throws IOException {
		File dir;
		if (tryPortSpecific)
		{
			dir = new File(parentName, childName + "-" + port);
			if (dir.isDirectory())
			{
				return dir.getPath();
			}
		}
		dir = new File(parentName, childName);
		if (dir.isDirectory())
		{
			return dir.getPath();
		}
		log.info("Directory does not exist: " + dir.getPath());
		return dir.getPath();
	}

	private String resolveDir(String dirName, int port, boolean tryPortSpecific) throws IOException {
		File dir;
		if (tryPortSpecific)
		{
			dir = new File(dirName + "-" + port);
			if (dir.isDirectory())
			{
				return dir.getPath();
			}
		}
		dir = new File(dirName);
		if (dir.isDirectory())
		{
			return dir.getPath();
		}
		log.info("Directory does not exist: " + dir.getPath());
		return dir.getPath();
	}

	private void registerPlugins(Context context, @SuppressWarnings("rawtypes") Class myClass) {
		WebappLoader loader = new WebappLoader(getClass().getClassLoader());
		loader.setDelegate(true);
		context.setLoader(loader);
		context.setReloadable(true);
		try {
			List<String> urls = PluginFinder.getPlugins(myClass);
			for (String url : urls) {
				loader.addRepository(url);
			}
		} catch (IOException e) {
			log.warn("Could not read plugin locations.", e);
		}
		try {
			List<String> urls = PluginFinder.getClasspathJars(EasyTravelConfig.read().wsmqClasspath);
			for (String url : urls) {
				loader.addRepository(url);
			}
		} catch (IOException e) {
			log.warn("Could not read classpath locations.", e);
		}
	}

	private void registerHttpConnector(Tomcat tomcat, String hostname, int port) {
		log.info("Adding HttpConnector at address: " + hostname + " port: " + port);
		Connector httpConnector = new Connector("HTTP/1.1");
		httpConnector.setProperty("address", hostname);
		httpConnector.setPort(port);
		tomcat.getService().addConnector(httpConnector);
		tomcat.setConnector(httpConnector);
	}

	/**
	 * Register SSL Connector to Tomcat instance
	 * HttpConnector port is redirected to SSL port
	 *
	 * @param tomcat
	 * @param sslPort
	 */
	private void registerSSLConnector(Tomcat tomcat, int sslPort) {
		log.info("Adding HttpsConnector at sslPort: " + sslPort);
		Connector httpsConnector = new Connector("HTTP/1.1");
		httpsConnector.setPort(sslPort);
		httpsConnector.setSecure(true);
		httpsConnector.setScheme("https");
		httpsConnector.setAttribute("keyAlias", CERT_ALIAS);
		httpsConnector.setAttribute("keystorePass", KEYSTORE_PASSWORD);
		httpsConnector.setAttribute("keystoreFile", CERTIFICATE_PATH);
		httpsConnector.setAttribute("clientAuth", "false");
		httpsConnector.setAttribute("sslProtocol", "TLS");
		httpsConnector.setAttribute("SSLEnabled", "true");
		tomcat.getService().addConnector(httpsConnector);
		// redirect HttpConnector port to SSL port
		tomcat.getConnector().setRedirectPort(sslPort);
	}

	private void registerAjpConnector(String routePrefix, int ajpPort, Tomcat tomcat, boolean ajpOnly) {
		log.info("Adding AJP Connector at port: " + ajpPort);
		Connector ajpConnector = new Connector("AJP/1.3");
		ajpConnector.setPort(ajpPort);
		tomcat.getService().addConnector(ajpConnector);

		if (ajpOnly) { // Tomcat listens to AJP only, no HTTP (set with port=0)
			tomcat.setPort(ajpPort);
			tomcat.setConnector(ajpConnector);
		}

		// also set jvmRouteId for session stickyness when used in load-balancing env.
		EasyTravelConfig config = EasyTravelConfig.read();
		String jvmRoute = "jvmRoute-" + ajpPort;
		if (config.clusterNode != null && !config.clusterNode.isEmpty()) {
			jvmRoute =  "jvmRoute-" + config.clusterNode + "-" + ajpPort;
		}
		jvmRoute = routePrefix + jvmRoute;
		tomcat.getEngine().setJvmRoute(jvmRoute);
		log.info("jvmRoute: " + jvmRoute);
	}

	private void registerShutdownListener(final int shutdownPort, final Server server) {
		server.setShutdown(BaseConstants.TOMCAT_SHUTDOWN);
		server.setPort(shutdownPort);

		// start thread that waits for shutdown command on shutdownPort
		Thread thread = new Thread("Shutdown Listener") {

			@Override
			public void run() {
				log.info(TextUtils.merge("Listen to shutdown command at port {0,number,#}", shutdownPort));

				// listen to shutdown command
				server.await();
				log.info(serverName + ": shutdown command received");
				long time = System.currentTimeMillis();

				try {
					PluginList.stopRefreshThread();

					if (manager != null) {
						manager.clearRandomSessions();
					}
					server.stop();
					log.info(TextUtils.merge("{0}: shutdown completed in {1} s", serverName,
							(System.currentTimeMillis() - time) / 1000.0));
				} catch (LifecycleException e) {
					log.error(TextUtils.merge(
							"A clean shutdown of ''{0}'' was not possible, because a ''{1}'' happened.",
							serverName, e.getClass().getSimpleName()), e);
				} finally {
					exitProcess(); // ensures that Tomcat exits despite an error happened
				}
			}

			private void exitProcess() {
				if (shutdownExecutor != null) {
					shutdownExecutor.run();
					try {
						server.stop();
						server.destroy();
					} catch (LifecycleException e) {
						e.printStackTrace();
					}
				} else {
				// Note that this will kill unit tests because it exits Java
					System.exit(0);
				}
			}
		};
		thread.start();
	}


	protected AutomaticMemoryManager getManager() {
		return manager;
	}

	protected void setManager(AutomaticMemoryManager manager) {
		this.manager = manager;
	}


	protected String getServerName() {
		return serverName;
	}

	public static void registerShutdownExecutor(Runnable newShutdownExecutor) {
		shutdownExecutor = newShutdownExecutor;
	}
}
