package com.dynatrace.easytravel;

import java.util.ArrayList;
import java.util.List;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.PluginConstants;

import ch.qos.logback.classic.Logger;

public class CPULoad extends AbstractGenericPlugin  {
    private static Logger log = LoggerFactory.make();
    private static final int THREAD_COUNT = 8;
	private boolean running = false;
	private final List<Logarithmizer> runningWorkers = new ArrayList<Logarithmizer>();

	@Override
	public Object doExecute(String location, Object... context) {
		if (PluginConstants.LIFECYCLE_PLUGIN_ENABLE.equals(location)) {
			log.info("cpuload starting");
			if (!running) {
				spawnLoadWorkers(THREAD_COUNT);
				running = true;
			}
		} else if (PluginConstants.LIFECYCLE_PLUGIN_DISABLE.equals(location)){
			log.info("cpuload stopping");
			running = false;
			stopWorkers();
		}
		return null; //nothing to do
	}

	private void spawnLoadWorkers(int threadCount) {
		synchronized(runningWorkers) {
			for (int i = 0; i< threadCount; i++) {
				Logarithmizer logarithmizer = new Logarithmizer();
				Thread thread = new Thread(logarithmizer);
				thread.setDaemon(true);
				thread.start();
				runningWorkers.add(logarithmizer);
			}
		}
	}
	
	private void stopWorkers() {
		synchronized(runningWorkers) {
			for (Logarithmizer logarithmizer : runningWorkers) {
				logarithmizer.setShouldRun(false);
			}
			runningWorkers.clear();
		}
	}

	private static class Logarithmizer implements Runnable {

		private volatile boolean shouldRun = true;
		
		@Override
		public void run() {
			while (shouldRun) {
				Math.log(System.currentTimeMillis());
			}
		}

		public void setShouldRun(boolean shouldRun) {
			this.shouldRun = shouldRun;
		}
	}
	
}
