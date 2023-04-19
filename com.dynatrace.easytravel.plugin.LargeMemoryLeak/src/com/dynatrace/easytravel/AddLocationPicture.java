package com.dynatrace.easytravel;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.PluginConstants;

import ch.qos.logback.classic.Logger;


public class AddLocationPicture extends AbstractGenericPlugin {
	private static final Logger log = LoggerFactory.make();

	// how long to wait after an OOM was caused before we trigger another
	private static final int MEMORY_OOM_DELAY = 5 * 60 * 1000; // 5 minutes

	private long lastOOM = 0;

	// this is overwritten by setter usually!
	private int allocationSize = 200 * 1024;

	public void setGrowSize(int growSize) {
		this.allocationSize = growSize*1024;
	}

	@Override
	public Object doExecute(String location, Object... context) {
		// enable memory leaking in one and actually cause a big memory leak in the second extension point
		if (location.equals(PluginConstants.BACKEND_LOCATION_SEARCH_BEFORE)) {
			log.debug("Plugin for memory leak in business backend is enabled.");

			// expect an AtomicBoolean for this extension point,
			// simply set this to true as we are only called here if the plugin is enabled
			((AtomicBoolean) context[0]).set(true);
		} else if (location.equals(PluginConstants.BACKEND_LOCATION_SEARCH)) {
			// we get a new Object[] {name, locations }
			// location
			@SuppressWarnings("unchecked")
			List<Location> locations = (List<Location>) context[1];

			if (System.currentTimeMillis() - lastOOM < MEMORY_OOM_DELAY) {
				log.debug("Not causing another memory leak");

				// replace all picture locations that we find with normal ones again
				cleanup(locations);
			} else {
				log.debug("Allocating a bunch of large objects which will result in an out-of-memory error soon.");
				try {
					// increase the memory by replacing the items with a "proxy" which keeps a lot more memory
					for (int i = 0; i < locations.size(); i++) {
						log.debug("Create new Picture of size " + allocationSize);
						Location old = locations.get(i);
						locations.set(i, new Location(old.getName()));

						locations.get(i).setCreated(old.getCreated());
						locations.get(i).storePicture(new byte[allocationSize]);
					}
				} catch (OutOfMemoryError e) {
					log.info("Had OOM, not triggering another OOM for " + MEMORY_OOM_DELAY/1000 + " seconds.");
					lastOOM = System.currentTimeMillis();
					cleanup(locations);
					throw e;
				}
			}
		}

		return null;
	}

	private void cleanup(List<Location> locations) {
		for (int i = 0; i < locations.size(); i++) {
			locations.get(i).storePicture(null);
		}
	}
}
