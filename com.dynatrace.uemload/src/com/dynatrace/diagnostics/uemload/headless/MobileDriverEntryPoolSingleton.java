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
public class MobileDriverEntryPoolSingleton {
	private static final Logger LOGGER = LoggerFactory.make();

	private static volatile MobileDriverEntryPoolSingleton instance;

	private final DriverEntryPool pool;

    private MobileDriverEntryPoolSingleton(){
    	EasyTravelConfig config = EasyTravelConfig.read();
    	pool = new DriverEntryPool("MobileDriverEntryPool", new MobileDriverEntryFactory(), config.maximumChromeDriversMobile, new HeadlessMobileStatistics());
    }

    public static MobileDriverEntryPoolSingleton getInstance(){
    	if(instance == null){
    		synchronized (MobileDriverEntryPoolSingleton.class) {
                if(instance == null){
                    instance = new MobileDriverEntryPoolSingleton();
                    LOGGER.info("Created an instance of MobileDriverEntryPoolSingleton");
                }
            }
        }
        return instance;
    }

	public DriverEntryPool getPool() {
    	return pool;
    }
}
