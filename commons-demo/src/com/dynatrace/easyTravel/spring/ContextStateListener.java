package com.dynatrace.easytravel.spring;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This context state listener sends start and shutdown events to all generic plugins (including disabled)
 * to give them opportunity to initialize and dispose.
 *
 * Needs two servlet-context parameters: 'extenesion.point.start' and 'extension.point.shutdown'
 *
 * @author philipp.grasboeck
 */
public class ContextStateListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent event) {
		SpringUtils.initAppContext(WebApplicationContextUtils.getRequiredWebApplicationContext(event.getServletContext()));
		executePlugins(event.getServletContext(), PluginConstants.EXTENSION_POINT_START);
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		executePlugins(event.getServletContext(), PluginConstants.EXTENSION_POINT_SHUTDOWN);
		SpringUtils.disposeAppContext();
	}

	private static void executePlugins(ServletContext context, String pluginPoint) {
		String extensionPoint = context.getInitParameter(pluginPoint);
		if (extensionPoint == null) {
			throw new IllegalStateException("Missing servlet-context parameter: " + pluginPoint);
		}
		PluginLifeCycle.executeAll(extensionPoint);
	}
}
