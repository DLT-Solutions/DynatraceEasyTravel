package com.dynatrace.easytravel.frontend.servlet;

import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

/**
 * Calls plugin points on generic plugins.
 * Convention for request is:
 * Resources: /plugins/PluginName/myresource.html
 * Execution: /plugins/PluginName/execute
 *
 * Query params (and POST also) are automatically injected into the plugin,
 * which makes it stateful.
 *
 * This servlet seems a lot like ServletDispatcher from WebWork :-)
 *
 * Simplified this class: it executes plugin points
 * "frontend.page.resource" and "frontend.page.execute"
 * on plugins.
 *
 * @author philipp.grasboeck
 *
 */
public abstract class BaseServlet extends HttpServlet {

	private static final long serialVersionUID = 4844900152215549008L;

    private static final Logger log = LoggerFactory.make();

    protected String contextRoot;

	@Override
	public void init() throws ServletException {
		super.init();
		contextRoot = TextUtils.appendTrailingSlash(getServletContext().getContextPath());
	}

	protected String getMandatoryInitParam(String name) {
		String value = getServletConfig().getInitParameter(name);
		if (log.isDebugEnabled())log.debug("initParam " + name + " = " + value);
		if (value == null)
		{
			throw new IllegalStateException("Mandatory init param missing: " + name);
		}
		return value;
	}

	protected String[] getMandatoryArrayInitParam(String name) {
		return getMandatoryInitParam(name).split(",");
	}

	protected Pattern getMandatoryPatternInitParam(String name) {
		return Pattern.compile(getMandatoryInitParam(name));
	}
}
