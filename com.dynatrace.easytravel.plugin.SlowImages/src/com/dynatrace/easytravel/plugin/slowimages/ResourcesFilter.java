package com.dynatrace.easytravel.plugin.slowimages;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dynatrace.easytravel.spring.GenericPlugin;
import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;

/**
 * this ServletFilter uses the SlowImagesPlugin to simulate bigger loading times of image elements
 *
 * therefore, the filter is being configured for the URL pattern "/img/" - see web.xml.
 *
 * in order to configure this filter properly, the init-parameter "maxdelay" can be used to define
 * a maximum delay value (in ms) for the image resoures. if not specified, 1000ms will be used as default value.
 *
 * @see SlowImagesPlugin
 * @author cwat-shauser
 *
 */
public class ResourcesFilter implements Filter {

	/** plugin list containing the slow images plugin */
	private static final GenericPluginList plugins = new GenericPluginList(PluginConstants.FRONTEND_RESOURCES);

	/** maximum delay value in ms, if not specified by filter parameter */
	private static final int MAX_DELAY = 1000;

	/** integer value of init-parameter "maxdelay" */
	private int maxdelay = MAX_DELAY;

	/**
	 * initializes the filter; reads parameter value "maxdelay" from configuration.
	 *
	 * if configuration parameter does not exist or is malformed, 1000ms will be used as default delay
	 *
	 * @author cwat-shauser
	 * @param FilterConfig config the filter configuration object
	 */
	@Override
	public void init(FilterConfig config) throws ServletException {
		try {
			this.maxdelay = Integer.valueOf(config.getInitParameter("maxdelay"));
		} catch(NumberFormatException ex){
			this.maxdelay = MAX_DELAY;
		}
	}

	@Override
	public void destroy() {

	}

	/**
	 * executes the SlowImagesPlugin, if activated
	 *
	 * @author cwat-shauser
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		if (response instanceof HttpServletResponse){

			HttpServletRequest req = (HttpServletRequest)request;

			String uri = req.getRequestURI();

			for(GenericPlugin plugin : plugins){
				plugin.execute(PluginConstants.FRONTEND_RESOURCES, uri, Integer.valueOf(this.maxdelay));
			}
		}

		chain.doFilter(request, response);
	}
}