package com.dynatrace.easytravel.frontend.servlet;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;


public class CachingFilter extends BaseFilter {

	private static final GenericPluginList plugins = new GenericPluginList(PluginConstants.FRONTEND_RESOURCE);
	
	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		
		if(chain != null) chain.doFilter(request, response);
		
		AtomicBoolean cachingEnabled = new AtomicBoolean(false);
		
		plugins.execute(PluginConstants.FRONTEND_RESOURCE_CACHING, cachingEnabled);
		
		if(cachingEnabled.get())
			response.addHeader("Cache-Control", "private,public,max-age=5000000");
	}

}
