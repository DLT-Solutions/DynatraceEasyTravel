package com.dynatrace.easytravel.frontend.beans;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;

/**
 * @author peter.kaiser
 */
@ManagedBean
@RequestScoped
public class JavascriptBean {

	private static final GenericPluginList plugins = new GenericPluginList(PluginConstants.FRONTEND_JAVASCRIPT);
	private static final GenericPluginList bootstrapPlugins = new GenericPluginList(PluginConstants.FRONTEND_JAVASCRIPT_BOOTSTRAP);

    public String getJavascript() {
        StringBuilder sb = new StringBuilder();
        for (Object o : plugins.execute(PluginConstants.FRONTEND_JAVASCRIPT)) {
            sb.append(o).append('\n');
        }
        return sb.toString();
    }

	public String getBootstrapAgent() {
		StringBuilder sb = new StringBuilder();
		for (Object o : bootstrapPlugins.execute(PluginConstants.FRONTEND_JAVASCRIPT_BOOTSTRAP)) {
			if (sb.length() > 0)
				sb.append('\n');
			sb.append(o);
		}
		return sb.toString();
	}

	@TestOnly
	public GenericPluginList getPluginList() {
		return plugins;
	}

	@TestOnly
	public GenericPluginList getBootstrapPluginList() {
		return bootstrapPlugins;
	}
}
