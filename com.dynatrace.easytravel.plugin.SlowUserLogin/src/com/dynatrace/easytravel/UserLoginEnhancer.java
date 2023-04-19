package com.dynatrace.easytravel;

import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class UserLoginEnhancer {
	
	private static final Logger LOGGER = LoggerFactory.make(); 

	public void waitForData() {
		Object availabilityLatch = new Object();

		try {
			// TODO: replace this with something a bit more sophisticated, Thread.sleep() is immediately recognizeable as fake in a live Demo!
			for(int i = 0;i < 100;i++) {
				synchronized (availabilityLatch) {
					// TODO: do something to not spend all time waiting here, the URL-Check is too unpredictable and did take a long time and thus made it unbearable slow
					//UrlUtils.isAvailable("http://localhost:" + EasyTravelConfig.read().launcherHttpPort + "/ping", true);

					// wait some time to not cause only CPU time
					availabilityLatch.wait(100);
				}
			}
		} catch (InterruptedException e) {
			LOGGER.error("Plugin execution interrupted: ", e);
		}
	}

}
