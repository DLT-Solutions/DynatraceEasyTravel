package com.dynatrace.easytravel.spring;

import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;

/**
 * Class used to define template for event notification.
 *
 * @author tomasz.wieremjewicz
 * @date 20 lis 2017
 *
 */
public class PluginNotificationTemplate {
	public static final String DEFAULT_PLUGIN_NAME = "default";
	public static final String PARAMETER_VERSION = "_etVersion_";
	public static final String PARAMETER_TIMESTAMP = "_timestamp_";
	public static final String PARAMETER_PLUGIN_STATE = "_pluginState_";
	public static final String PARAMETER_PLUGIN_NAME = "_pluginName_";
	public static final String PARAMETER_START = "_start_";
	public static final String PARAMETER_END = "_end_";

	public String[] pluginNames; //NOSONAR
	public String[] entityIds; //NOSONAR
	public String title; //NOSONAR
	public String source; //NOSONAR
	public String version; //NOSONAR
	public Map<String, String> customProperties; //NOSONAR
	public String deploymentProject; // NOSONAR - public on purpose
	public String ciBackLink; // NOSONAR - public on purpose
	public String remediationAction; // NOSONAR - public on purpose

	@JsonIgnore
	public boolean isTemplateDefinitionComplete() {
		return !ArrayUtils.isEmpty(pluginNames)
				&& !ArrayUtils.isEmpty(entityIds)
				&& !Strings.isNullOrEmpty(title)
				&& !Strings.isNullOrEmpty(source)
				&& !Strings.isNullOrEmpty(version)
				&& !Strings.isNullOrEmpty(deploymentProject)
				&& !Strings.isNullOrEmpty(ciBackLink)
				&& !Strings.isNullOrEmpty(remediationAction);
	}
}
