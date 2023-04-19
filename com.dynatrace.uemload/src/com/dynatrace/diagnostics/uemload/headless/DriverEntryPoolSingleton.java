/**
 *
 */
package com.dynatrace.diagnostics.uemload.headless;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * @author tomasz.wieremjewicz
 * @date 17 sty 2019
 *
 */
public class DriverEntryPoolSingleton {
	private static final Logger LOGGER = LoggerFactory.make();

	private static volatile DriverEntryPoolSingleton instance;

	private final DriverEntryPool pool;

    private DriverEntryPoolSingleton(){
    	EasyTravelConfig config = EasyTravelConfig.read();
    	pool = new DriverEntryPool("DriverEntryPool", new DriverEntryFactory(), config.maximumChromeDrivers, new HeadlessStatistics());
    }

    public static DriverEntryPoolSingleton getInstance(){
        if(instance == null){
            synchronized (DriverEntryPoolSingleton.class) {
                if(instance == null){
                    instance = new DriverEntryPoolSingleton();
                    LOGGER.info("Created an instance of DriverEntryPoolSingleton");
                }
            }
        }
        return instance;
    }

	public DriverEntryPool getPool() {
    	return pool;
    }
}
