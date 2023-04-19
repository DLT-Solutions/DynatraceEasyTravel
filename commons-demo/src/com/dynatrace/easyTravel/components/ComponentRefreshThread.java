package com.dynatrace.easytravel.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;

public class ComponentRefreshThread extends Thread {
	private static final Logger log = LoggerFactory.make();

	private static int REFRESH_INTERVAL = 10000;

	private static final int REFRESH_SLEEP_FACTOR = 20;

	private volatile boolean shouldStop = false;

	private List<String> components;

	private String componentType;

	public ComponentRefreshThread(String componentType) {
		super("Components Refresh Thread");
		this.componentType = componentType;
		this.components = new ArrayList<String>();
		retrieveEnabledComponents();

		setDaemon(true);
	}

	public List<String> getEnabledComponents() {
		return components;
	}

	@Override
	public void run() {
		int i = 0;
		while (!shouldStop) {
			try {

				Thread.sleep(REFRESH_INTERVAL / REFRESH_SLEEP_FACTOR);

				if (shouldStop) {
					break;
				}

				if (i >= REFRESH_SLEEP_FACTOR) {
					retrieveEnabledComponents();

					i = 0;
				}

				i++;
			} catch (Exception e) {
				log.warn("Had Exception in plugin refresh thread", e);
			}
		}
	}
	
	/**
	 * Indicate that the thread should stop. The method will block for 1s and wait
	 * for the thread to stop. If shutdown of the thread takes longer, it will continue
	 * and leave the thread in an undecided state.
	 */
	public void shouldStop() {
		shouldStop = true;

		// wait some time for the thread to stop, as we sleep 500ms below, we are joining twice as long to ensure in most cases that the thread stopped now
		try {
			join(REFRESH_INTERVAL/REFRESH_SLEEP_FACTOR*2);
		} catch (InterruptedException e) {
			log.warn("Joining refresh thread was interrupted", e);
		}
	}

	private void retrieveEnabledComponents() {

		EasyTravelConfig config = EasyTravelConfig.read();
		String pluginServiceHost = config.pluginServiceHost;
		final ComponentManagerProxy componentsManagerProxy;
		if (StringUtils.isNotBlank(pluginServiceHost)) {
			componentsManagerProxy = new RemoteComponentManager(pluginServiceHost, config.pluginServicePort);
		} else {
			componentsManagerProxy = new ComponentManagerAccess();
		}
		
		for(String ip : components){
			log.info("Component in a refresh Thread: "+ip+" with type:"+componentType);
		}
		
		components = Arrays.asList( ArrayUtils.nullToEmpty(componentsManagerProxy.getComponentsIPList(componentType)) );
		
	}
}
