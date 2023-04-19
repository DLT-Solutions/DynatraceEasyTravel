package com.dynatrace.easytravel.frontend.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;

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
public class RedirectServlet extends BaseServlet {

	private static final long serialVersionUID = 4844900152215549008L;

    private static final Logger log = LoggerFactory.make();

    private String prefix;

	@Override
	public void init() throws ServletException {
		super.init();
		prefix = getMandatoryInitParam("prefix");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String path = request.getRequestURI();
		String contextPrefix = contextRoot + prefix;
		path = path.startsWith(contextPrefix) ? path.substring(contextPrefix.length()) : "";
		if (log.isDebugEnabled()) log.debug("redirecting to: " + path);
		response.sendRedirect(contextRoot + path);
	}
}
