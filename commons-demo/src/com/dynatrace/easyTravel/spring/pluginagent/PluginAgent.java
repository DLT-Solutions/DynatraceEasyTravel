package com.dynatrace.easytravel.spring.pluginagent;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.components.ComponentController;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import ch.qos.logback.classic.Logger;

public abstract class PluginAgent extends AbstractGenericPlugin {

	public static final int MAXIMUM_POOL_SIZE = 5;
	public static final int CORE_POOL_SIZE = 1;
	private String url;
	private static final Logger log = LoggerFactory.make();

	private final BlockingQueue<Runnable> queue = new SynchronousQueue<>();
	private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
			.setNameFormat("PluginAgentThread-%d")
			.setDaemon(true)
			.build();
	private ExecutorService executor;
	private ComponentController externalComponentsController;

	public PluginAgent(String type, String url) {
		this.url = url;
		externalComponentsController = new ComponentController(type);

	}

	@Override
	public Object doExecute(String location, Object... context) {
		if (PluginConstants.LIFECYCLE_PLUGIN_ENABLE.equals(location)) {
			createExecutor();
		} else if (PluginConstants.LIFECYCLE_PLUGIN_DISABLE.equals(location)) {
			shutDownExecutor();
		} else if (shouldStartWithLocation(location)) {
			
			if(StringUtils.isNotBlank(this.url)){
				submitNewTask(this.url);
			} else {
				List<String> componentURLs = externalComponentsController.getEnabledComponents();
				for (String url : componentURLs) {
					submitNewTask(url);
				}	
			}
		}
		return null;
	}

	private void createExecutor() {
		if (executor == null || executor.isShutdown()) {
			executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 0L, TimeUnit.MILLISECONDS, queue,
					threadFactory);
			log.debug("New executor created: " + executor.toString());
		}
	}

	private void shutDownExecutor() {
		if (executor != null && !executor.isShutdown()) {
			log.debug("Shutting down executor");
			ComponentController.stopRefreshThread();
			executor.shutdownNow();
		}
	}

	private boolean shouldStartWithLocation(String location) {
		return location != null && !location.startsWith(PluginConstants.LIFECYCLE);
	}

	private void submitNewTask(String url) {
		try {
			executor.submit(new PluginAgentThread(url));
		} catch (RejectedExecutionException r) {
			log.info("Execution of PluginAgentThread with URL: " + url + " failed."
					+ executor.toString());
			log.debug("Execution of PluginAgentThread with URL: " + url + " failed."
					+ executor.toString(), r);
		} catch (Exception e) {
			log.warn("Unexpected error when submitting a task", e);
		}
	}

	public void waitForShutdown() throws InterruptedException {
		if (executor != null && executor.isShutdown()) {
			executor.awaitTermination(30, TimeUnit.SECONDS);
		}
	}
	
	public void setComponentController(ComponentController componentController){
		this.externalComponentsController = componentController;
	}

}
