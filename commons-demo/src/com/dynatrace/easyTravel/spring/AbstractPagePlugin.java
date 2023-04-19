package com.dynatrace.easytravel.spring;

import java.net.URL;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.ConfigurationProvider;

/**
 * Abstract subclass of a plugin that wants to extend the website.
 * Serves the extension point 'frontend.page'
 * Reacts on the following locations:
 * - frontend.page.headinjection  - inject something into the html header
 * - frontend.page.header         - getHeader()  - displayed in the page
 * - frontend.page.footer         - getFooter()  - displayed in the page
 * - frontend.page.resource       - doResource() - tries to get a resource from the plugin
 * - frontend.page.execute        - doExecute()  - executes the plugin
 *
 * Resources are scoped by plugin-name.
 * Example: /plugins/DemoSitePlugin/myresource.html
 * will deliver resource 'myresource.html' from plugin named 'DemoSitePlugin' if the
 * prefix servlet init-param of PluginServlet is set to '/plugins/'.
 *
 * @author philipp.grasboeck
 */
public abstract class AbstractPagePlugin extends AbstractGenericPlugin {

	private static final Logger log = LoggerFactory.make();

	private static ThreadLocal<HttpServletResponse> responseLocal = new ThreadLocal<HttpServletResponse>();

	@SuppressWarnings("unchecked")
	@Override
	public Object doExecute(String location, Object... context)
	{
		/*HttpServletResponse response =*/ getResponse();
		if (location.equals(PluginConstants.FRONTEND_PAGE_RESOURCE))
		{
			return doResource((String) context[0]);
		}
		else if (location.equals(PluginConstants.FRONTEND_PAGE_EXECUTE))
		{
			try {
				return doExecute((Map<String, Object>) context[0]);
			} catch (Exception e) {
				log.warn("Plugin threw exception: " + e.getMessage());
			}
		}
		else if (location.equals(PluginConstants.FRONTEND_PAGE_HEADINJECTION))
		{
			return getHeadInjection();
		}
		else if (location.equals(PluginConstants.FRONTEND_PAGE_HEADER))
		{
			return getHeader();
		}
		else if (location.equals(PluginConstants.FRONTEND_PAGE_FOOTER))
		{
			return getFooter();
		}
		else if (location.equals(PluginConstants.FRONTEND_PAGE_FOOTER_SCRIPT))
		{
			return getFooterScript();
		}
		else if (location.equals(PluginConstants.FRONTEND_PAGE_CONTENT_FINISH))
		{
			return getContentFinish();
		}

		return null;
	}

	/**
	 * Actually execute the plugin ('plugin.page.execute')
	 * @param params  the map of parameters for this plugin. Will actually be the
	 *  request parameter map. Note that this plugin may modify the parameters
	 *  so the next plugin in the chain sees these modifications.
	 *
	 * @return
	 * - null                - if redirect is desired (no result view)
	 * - java.net.URL        - a resource that will be delivered.
	 * - java.lang.String    - a new path that will be chained  e.g. "PluginName/resource" or "PluginName/execute"
	 * - byte[]              - inline resource, deliver it directly
	 */
	protected Object doExecute(Map<String, Object> params) throws Exception
	{
		return null;
	}

	/**
	 * Get a resource from the plugin ('plugin.page.resource').
	 * @param context
	 * @return
	 * - null              - if the resource was not found
	 * - java.net.URL      - a resource that will be delivered
	 * - byte[]            - inline resource, deliver it directly
	 */
	protected Object doResource(String path)
	{
		return getResource(path);
	}

	protected final HttpServletResponse getResponse() {
		return responseLocal.get();
	}

	public String getHeadInjection() {
		return null;
	}

	public String getHeader() {
		return null;
	}

	public String getFooter() {
		return null;
	}

	public String getFooterScript() {
		return null;
	}

	protected static URL getResource(String name)
	{
        return ConfigurationProvider.getResource(name);
	}

	protected static byte[] getContent(String content)
	{
		return content.getBytes();
	}

	public static void setResponse(HttpServletResponse response) {
		responseLocal.set(response);
	}
	
	public Object getContentFinish() {
		return null;
	}

}
