package com.dynatrace.easytravel.frontend.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractPagePlugin;
import com.dynatrace.easytravel.spring.GenericPlugin;
import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.util.MvelUtils;
import com.dynatrace.easytravel.util.RegexUtils;
import com.dynatrace.easytravel.util.WebUtils;

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
public class PluginServlet extends BaseServlet {

	private static final long serialVersionUID = 4844900152215549008L;

    private static final Logger log = LoggerFactory.make();

	private static final GenericPluginList plugins = new GenericPluginList(PluginConstants.FRONTEND_PAGE);

    // init-parameters
    private String prefix;            // the prefix of the servlet path, like "/plugins/"
    private String[] dynamicSuffix ;  // extensions of files that will undergo the regex replacement
    private String[] downloadSuffix ; // extensions of files that will be provided for download
    private String redirectURL;       // the url to redirect to if no result view was found
    private Pattern varPattern;       // a regex with one matching group (property) for dynamic replacement
    private Pattern exprPattern;      // a regex with one matching group (expression) for dynamic replacement
    private Pattern ifPattern;        // a regex with two matching groups (ifCondition, ifBody) for conditional dynamic replacement

	@Override
	public void init() throws ServletException {
		super.init();
		prefix         = getMandatoryInitParam("prefix");
		dynamicSuffix  = getMandatoryArrayInitParam("dynamicSuffix");
		downloadSuffix = getMandatoryArrayInitParam("downloadSuffix");
		redirectURL    = getMandatoryInitParam("redirectURL");
		varPattern     = getMandatoryPatternInitParam("varRegex");
		exprPattern    = getMandatoryPatternInitParam("exprRegex");
		ifPattern      = getMandatoryPatternInitParam("ifRegex");
	}

	// like "PluginName/resource.name" or "PluginName/execute"
	private boolean handlePath(String path, Map<String, Object> params, HttpServletResponse response) throws IOException
	{
		if (log.isDebugEnabled()) log.debug("Service path: " + path);
		for (GenericPlugin plugin : plugins)
		{
			String prefix = plugin.getName() + "/";
			if (path.startsWith(prefix))
			{
				path = path.substring(prefix.length());
				if (log.isDebugEnabled()) log.debug("Found plugin: " + plugin);
				Object result;

				if (path.equals("execute")) // execute the plugin
				{
					MvelUtils.injectProperties(plugin, params, "default");
					MvelUtils.injectProperties(plugin, params, null);
					result = plugin.execute(PluginConstants.FRONTEND_PAGE_EXECUTE, params);
				}
				else // get a resource
				{
					result = plugin.execute(PluginConstants.FRONTEND_PAGE_RESOURCE, path);
				}

				if (result instanceof URL) // this is a resource that we deliver directly
				{
					return handleResource((URL) result, response, plugin);
				}
				else if (result instanceof byte[]) // handle an inline resource
				{
					return handleInlineResource((byte[]) result, response);
				}
				else if (result instanceof String) // this is a new plugin point that we execute now
				{
					return handlePath((String) result, params, response);
				}
			}
		}
		return false;
	}

	/** handle a (static or dynamic) resource. */
	private boolean handleResource(URL resource, HttpServletResponse response, GenericPlugin plugin) throws IOException
	{
		String resourcePath = resource.getFile();
		File resourceFile = new File(resourcePath);

		if (hasSuffix(resourcePath, dynamicSuffix)) // undergo the regex replacement
		{
			if (log.isDebugEnabled()) log.debug("Dynamic resource: " + resourcePath);
			InputStream in = resource.openStream();
			StringBuilder buf = new StringBuilder(IOUtils.toString(in));
			// note that the order of replacements is important here
			replaceVars(buf, varPattern, plugin);
			replaceExprs(buf, exprPattern, plugin);
			replaceIfs(buf, ifPattern, plugin);
			// write out the result
			IOUtils.write(buf.toString(), response.getOutputStream());
			IOUtils.closeQuietly(in);
		}
		else // static
		{
			if (log.isDebugEnabled()) log.debug("Static resource: " + resourcePath);
			try (InputStream in = resource.openStream();) {
				IOUtils.copy(in, response.getOutputStream());
				IOUtils.closeQuietly(in);
			}
		}

		response.setHeader("Content-Type", WebUtils.getContentType(resourcePath));

		if (hasSuffix(resourcePath, downloadSuffix)) // add the HTTP header to download a file
		{
			response.setHeader("Content-Disposition", "attachment; filename=" + resourceFile.getName());
		}

		return true;
	}

	private boolean handleInlineResource(byte[] content, HttpServletResponse response) throws IOException
	{
		if (log.isDebugEnabled()) log.debug("Inline resource: " + Arrays.toString(content));
		IOUtils.write(content, response.getOutputStream());
		response.setHeader("Content-Type", "text/html; charset=UTF-8");
		return true;
	}

	/** returns the fileName (without folders), if it has one of the extensions. */
	private static boolean hasSuffix(String path, String[] fileExtensions)
	{
		for (String fileExtension : fileExtensions)
		{
			if (path.endsWith(fileExtension))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		handleRequest(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		handleRequest(request, response);
	}

	private void handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		String path = request.getRequestURI(); // "/plugins/PluginName/..."
		String contextPrefix = contextRoot + prefix;
		if (path.startsWith(contextPrefix))
		{
			path = path.substring(contextPrefix.length()); // "PluginName/..."
			AbstractPagePlugin.setResponse(response);
			boolean hadResource = handlePath(path, getParams(request.getParameterMap()), response);
			AbstractPagePlugin.setResponse(null);
			if (hadResource)
			{
				return; // response already written
			}
			else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
		}

		// no response written, redirect
		if (log.isDebugEnabled()) log.debug("No result, redirecting to: " + contextRoot + redirectURL);
		response.sendRedirect(contextRoot + redirectURL);
	}

	private static Map<String, Object> getParams(Map<String, String[]> map)
	{
		Map<String, Object> result = new HashMap<String, Object>();
		for (Map.Entry<String, String[]> entry : map.entrySet())
		{
			String key = entry.getKey();
			String[] values = entry.getValue();
			String value = (values.length > 0) ? values[0] : null;
			result.put(key, value);
		}
		return result;
	}

	/** Replace variables like #{foo} with their value from the actual plugin */
	private static void replaceVars(StringBuilder buf, Pattern pattern, final Object root)
	{
		if (log.isDebugEnabled()) log.debug("replaceVars regex=" + pattern + ", input=" + buf);

		RegexUtils.dynamicScan(buf, pattern, new RegexUtils.ScanVisitor()
		{
			@Override
			public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
				String property = match.group(1);
				Object value = MvelUtils.getProperty(property, root);

				if (log.isDebugEnabled()) // for regex-debugging
				{
					log.debug("property: '" + property + "'");
					log.debug("value: '" + value + "'");
				}

				return (value != null) ? value.toString() : "";
			}
		});
	}

	/** Replace expressions like #{(1+1)} with their value from the actual plugin */
	private static void replaceExprs(StringBuilder buf, Pattern pattern, final Object root)
	{
		if (log.isDebugEnabled()) log.debug("replaceExprs regex=" + pattern + ", input=" + buf);

		RegexUtils.dynamicScan(buf, pattern, new RegexUtils.ScanVisitor()
		{
			@Override
			public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
				String expression = match.group(1);
				Object value = MvelUtils.evaluate(expression, root);

				if (log.isDebugEnabled()) // for regex-debugging
				{
					log.debug("expression: '" + expression + "'");
					log.debug("value: '" + value + "'");
				}

				return (value != null) ? value.toString() : "";
			}
		});
	}

	/** Replace fis like #{if (ifCondition) ifBody} with their value from the actual plugin */
	private static void replaceIfs(StringBuilder buf, Pattern pattern, final Object root)
	{
		if (log.isDebugEnabled()) log.debug("replaceIfs regex=" + pattern + ", input=" + buf);

		RegexUtils.dynamicScan(buf, pattern, new RegexUtils.ScanVisitor()
		{
			@Override
			public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
				String ifCondition = match.group(1);
				String ifBody = match.group(2);
				boolean result = Boolean.TRUE.equals(MvelUtils.evaluate(ifCondition, root));

				if (log.isDebugEnabled()) // for regex-debugging
				{
					log.debug("ifCondition: '" + ifCondition + "'");
					log.debug("ifBody: '" + ifBody + "'");
					log.debug("result: " + result);
				}

				return result ? ifBody : "";
			}
		});
	}
}
