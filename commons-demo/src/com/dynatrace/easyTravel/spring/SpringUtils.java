package com.dynatrace.easytravel.spring;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.dynatrace.easytravel.components.ComponentManagerProxy;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.DirUtils;

import ch.qos.logback.classic.Logger;

/**
 * Collection of getters for all needed Spring beans.
 *
 * @author philipp.grasboeck
 *
 */
public class SpringUtils {
	private static final Logger log = LoggerFactory.make();

	private static final String PLUGIN_ENABLE_PREFIX = "plugin.enable.";
	private static final String PLUGIN_HOLDER = "pluginHolder";
	private static final String PLUGIN_STATE_PROXY = "pluginStateProxy";
	private static final String COMPONENT_MANAGER_PROXY = "componentsManagerProxy";

	private static final String[] APP_DIRS = { ".", "..", "../Distribution/dist" };
	private static final String CUSTOMER_FRONTEND_APP_DIR = "customer";
	private static final String BUSINESS_BACKEND_APP_DIR = "business";
	private static final String CONFIG_XML = "webapp/WEB-INF/spring/root-context.xml";
    private static final String TEST_CONFIG_XML = "webapp/WEB-INF/spring/unit-test-root-context.xml";

	private static ApplicationContext appContext;

	public static void initCustomerFrontendContext() {
		initFileSystemContext(CUSTOMER_FRONTEND_APP_DIR, false);
		PluginLifeCycle.executeAll(PluginConstants.LIFECYCLE_FRONTEND_START);
	}

	/**
	 * Note: In tests you usually need to set system properties for
	 * @link {@link BaseConstants.SystemProperties.PERSISTENCE_MODE} and
	 * "com.dynatrace.easytravel.propertiesfile"
	 **/
	public static void initBusinessBackendContext() {
		initFileSystemContext(BUSINESS_BACKEND_APP_DIR, false);
		PluginLifeCycle.executeAll(PluginConstants.LIFECYCLE_BACKEND_START);
	}

	public static void initBusinessBackendContextForTest() {
	    initFileSystemContext(BUSINESS_BACKEND_APP_DIR, true);
		PluginLifeCycle.executeAll(PluginConstants.LIFECYCLE_BACKEND_START);
	}

	public static void disposeCustomerFrontendContext() {
		PluginLifeCycle.executeAll(PluginConstants.LIFECYCLE_FRONTEND_SHUTDOWN);
		disposeAppContext();
	}

	public static void disposeBusinessBackendContext() {
		PluginLifeCycle.executeAll(PluginConstants.LIFECYCLE_BACKEND_SHUTDOWN);
		disposeAppContext();
	}

	static void initFileSystemContext(String appDirName, boolean forTest) {
		File appDir = DirUtils.findDir(appDirName, APP_DIRS);
		if (!appDir.exists()) {
			throw new IllegalStateException("Cannot find app dir: " + appDirName);
		}
		File configFile = new File(appDir, forTest ? TEST_CONFIG_XML : CONFIG_XML);
		if (!configFile.exists()) {
			throw new IllegalStateException("Cannot find the configFile: " + configFile);
		}

		log.info("Initialize FileSystemXmlApplicationContext with path: " + configFile.getPath());
		initAppContext(new FileSystemXmlApplicationContext("file:" + configFile.getPath()));
	}

	static void initAppContext(ApplicationContext context) {
		appContext = context;

		Properties props = System.getProperties();
		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			String key = String.valueOf(entry.getKey());
			if (key.startsWith(PLUGIN_ENABLE_PREFIX)) {
				String pluginName = key.substring(PLUGIN_ENABLE_PREFIX.length());
				getPluginStateProxy().setPluginEnabled(pluginName, "true".equals(String.valueOf(entry.getValue())));
			}
		}
	}

	public static void disposeAppContext() {
		appContext = null;
	}

	public static PluginHolder getPluginHolder() {
		return getBean(PLUGIN_HOLDER, PluginHolder.class);
	}

	public static PluginStateProxy getPluginStateProxy() {
		return getBean(PLUGIN_STATE_PROXY, PluginStateProxy.class);
	}

	public static ComponentManagerProxy getComponentsManagerProxy() {
		return getBean(COMPONENT_MANAGER_PROXY, ComponentManagerProxy.class);
	}

	/**
	 * Returns a Spring Bean with the given name.
	 *
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String name, Class<T> expectedClass) {
		Object bean = getAppContext().getBean(name);

		if (bean == null) {
			throw new IllegalStateException("Necessary Spring bean not found: " + name);
		}
		if (!expectedClass.isAssignableFrom(bean.getClass())) { // i.e. instanceof
			throw new IllegalStateException("Spring bean has wrong class: " + name + ", actual=" + bean.getClass() + ", expected=" + expectedClass);
		}

		return (T) bean;
	}

	public static ApplicationContext getAppContext() {
		if (appContext == null) {
			throw new IllegalStateException("appContext is not initialized");
		}
		return appContext;
	}

	public static boolean hasAppContext() {
		return appContext != null;
	}
}
