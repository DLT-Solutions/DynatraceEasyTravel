package com.dynatrace.easytravel.frontend.beans;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;

@ManagedBean
@RequestScoped
public class StreamingMediaBean {

	private static final GenericPluginList plugins = new GenericPluginList(PluginConstants.FRONTEND_MEDIA);

	public String getHomepage() {
		return getPluginContent(PluginConstants.FRONTEND_MEDIA_HOMEPAGE);
	}

	private static String getPluginContent(String location) {
		StringBuilder buf = new StringBuilder();
		for (Object object : plugins.execute(location)) {
			if (object != null) {
				buf.append(object);
			}
		}

		return buf.toString();
	}

}
