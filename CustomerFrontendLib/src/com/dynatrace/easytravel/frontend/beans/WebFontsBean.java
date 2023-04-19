package com.dynatrace.easytravel.frontend.beans;

import java.util.Iterator;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;

@ManagedBean
@RequestScoped
public class WebFontsBean {

	private static final GenericPluginList plugins = new GenericPluginList(PluginConstants.FRONTEND_WEBFONTS);

	private static final String DEFAULT_RESPONSE = 	""; // by default we do not load any fonts from the web

	public String getWebFonts() {
		String webFonts = getWebFontsFromPlugin();
		if(webFonts == null) {
			return DEFAULT_RESPONSE;
		}
		return webFonts;
	}

	private String getWebFontsFromPlugin() {
		return getResponse(plugins.execute(PluginConstants.FRONTEND_WEBFONTS));
	}

	private String getResponse(Iterable<Object> execute) {
		if(execute == null) {
			return null;
		}

		for(Iterator<Object> iterator = execute.iterator(); iterator.hasNext();) {
			Object temp = iterator.next();
			if((temp) instanceof String) {
				return (String) temp;
			}
		}

		return null;
	}

	@TestOnly
	public GenericPluginList getPluginList() {
		return plugins;
	}		
}