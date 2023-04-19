package com.dynatrace.easytravel.frontend.beans;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;

@ManagedBean
@RequestScoped
public class PluginBean implements Serializable
{
	private static final long serialVersionUID = 4050241000048043836L;

	private static final GenericPluginList plugins = new GenericPluginList(PluginConstants.FRONTEND_PAGE);
	
	private static final String AMP_IMG_PATTERN = 
			"<span style=\"margin-left: 10px;\">"
			+ "<a target=\"_blank\" href=\"/amp/\">"
			+ "<img alt=\"easyTravel AMP\" src=\"img/easytravel-amp.png\" title=\"easyTravel AMP website\" height=\"35\"/>"
			+ "</a>"
			+ "</span>";

	public String getHeadInjection() {
		return getPluginContent(PluginConstants.FRONTEND_PAGE_HEADINJECTION);
	}

	public String getHeader() {
		return getPluginContent(PluginConstants.FRONTEND_PAGE_HEADER);
	}

	public String getFooter() {
		return getPluginContent(PluginConstants.FRONTEND_PAGE_FOOTER);
	}

	public String getFooterScript() {
		return getPluginContent(PluginConstants.FRONTEND_PAGE_FOOTER_SCRIPT);
	}

	public String getContentFinish() {
		String pluginContent = getPluginContent(PluginConstants.FRONTEND_PAGE_CONTENT_FINISH);
		StringBuilder builder = new StringBuilder()
				.append("<div id=\"imagegallery\" class=\"recommendation\">")
				.append(pluginContent)
				.append(AMP_IMG_PATTERN)
				.append("</div>");
		return builder.toString();
	};

	private static String getPluginContent(String location) {
		return getPluginContent(location, null);
	}

	private static String getPluginContent(String location, String param) {
		StringBuilder buf = new StringBuilder();
		for (Object object : plugins.execute(location)) {
			if (object != null) {
				buf.append(object);
			}
		}

		return buf.toString();
	}
}
