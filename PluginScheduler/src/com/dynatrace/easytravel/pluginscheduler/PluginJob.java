package com.dynatrace.easytravel.pluginscheduler;

import java.util.logging.Logger;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.dynatrace.easytravel.plugin.RemotePluginController;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.base.Strings;

/**
 * cwpl-rorzecho
 */
public class PluginJob implements Job {
	private static Logger LOGGER = Logger.getLogger(PluginJob.class.getName());
	
	public static final int RETRIES_NR = 3;
	public static final String PLUGIN_NAME_KEY = "pluginName";
	public static final String PLUGIN_ACTION_KEY = "pluginAction";
	public static final String START_ACTION = "start";
	public static final String STOP_ACTION = "stop";
	private final RemotePluginController remotePluginController = new RemotePluginController();

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

		String pluginName = jobExecutionContext.getMergedJobDataMap().getString(PLUGIN_NAME_KEY);
		String pluginAction = jobExecutionContext.getMergedJobDataMap().getString(PLUGIN_ACTION_KEY);
		
		validDatePluginAction(pluginAction);
		
		LOGGER.info(TextUtils.merge("Plugin job started with plugin name: {0} and pluginAction: {1}", Strings.nullToEmpty(pluginName), Strings.nullToEmpty(pluginAction)));
		
		String result = null;
		Exception lastException = null;
		int cnt = 0;
		
		while(result == null && cnt++ < RETRIES_NR) {
			try {
				LOGGER.fine(TextUtils.merge("PluginJob, try to change plugin state. Retry nr: {0}", cnt));
				if (START_ACTION.equals(pluginAction)) {
					remotePluginController.registerPlugin(pluginName);
					result = remotePluginController.sendEnabled(pluginName, true, null);
				} else if (Strings.emptyToNull(pluginAction) == null  || STOP_ACTION.equals(pluginAction)) { 
					//if no action is defined disable plugin; this allow to define chainJob as the same job, but without any parameters 
					result = remotePluginController.sendEnabled(pluginName, false, null);
				}
				
				if (result == null && cnt < RETRIES_NR) {					
					Thread.sleep(1000);
				}
			} catch (Exception e) { //catch all possible exceptions and wrap them into JobExecutionException
				lastException = e; 
			}
		}
		
		if(lastException != null) {
			throw new JobExecutionException(lastException);
		}
	}
	
	private void validDatePluginAction(String pluginAction) {
		if(pluginAction != null && !STOP_ACTION.equals(pluginAction) && !START_ACTION.equals(pluginAction)) {
			throw new IllegalArgumentException("Unknown plugin action: " + pluginAction);
		}
	}
}
