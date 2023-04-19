package com.dynatrace.easytravel.frontend.beans;

import java.util.Iterator;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;

@ManagedBean
@RequestScoped
public class JQueryBean {

	private static final GenericPluginList plugins = new GenericPluginList(PluginConstants.FRONTEND_JQUERY);

	private static final String DEFAULT_RESPONSE = 	"<script src=\"js/jquery-1.8.1.js\" type=\"text/javascript\"></script>" +
													"<script type=\"text/javascript\" src=\"js/jquery-ui-1.8.2.min.js\"></script>";

	public String getPath() {
		String jQueryPaths = getJQueryPathsFromPlugin();
		if(jQueryPaths == null) {
			return DEFAULT_RESPONSE;
		}
		return jQueryPaths;
	}

	private String getJQueryPathsFromPlugin() {
		return getResponse(plugins.execute(PluginConstants.FRONTEND_JQUERY_PATHS));
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
