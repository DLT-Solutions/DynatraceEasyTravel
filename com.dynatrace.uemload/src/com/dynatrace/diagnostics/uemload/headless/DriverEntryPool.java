package com.dynatrace.diagnostics.uemload.headless;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.pool.impl.GenericObjectPool;

import com.dynatrace.diagnostics.uemload.UemLoadScheduler;
import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.process.HeadlessProcessKillerFactory;

import ch.qos.logback.classic.Logger;

/**
 * @author Rafal.Psciuk
 * @Date 2018.11.05
 *
 * @author tomasz.wieremjewicz
 * @date 17 sty 2019
 */
public class DriverEntryPool {
	private static final Logger LOGGER = LoggerFactory.make();

	private DriverEntryFactory driverEntryFactory;
	private GenericObjectPool<DriverEntry> pool;
	private AtomicBoolean shutDownInProgress;			// set to prevent re-entrace of shutDownChromeDrivers();
	private volatile boolean visitGenerationEnabled;
	private int maximumChromeDrivers;
	private final HeadlessStatistics stats;
	private final String name;
	
	static {
		UemLoadScheduler.scheduleAtFixedRate(() -> HeadlessProcessKillerFactory.removeOrphanedChromeBrowsers(), 5, 5, TimeUnit.MINUTES);
	}
	
	DriverEntryPool(String name, DriverEntryFactory factory, int maximumChromeDrivers, HeadlessStatistics stats) {
		this.maximumChromeDrivers = maximumChromeDrivers;
		this.driverEntryFactory = factory;
		this.stats = stats;
		this.name = name;
		initialize();
	}

	private void initialize() {
		pool = new GenericObjectPool<>(driverEntryFactory);
		shutDownInProgress = new AtomicBoolean(false);
		visitGenerationEnabled = true;

		// create the pool
		GenericObjectPool.Config genericObjectPoolConfig = new GenericObjectPool.Config();

		// max number of active  objects
		genericObjectPoolConfig.maxActive = maximumChromeDrivers;

		// set to WHEN_EXHAUSTED_FAIL so that an immediate exception is thrown when we don't have any more idle objects to use
		genericObjectPoolConfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;

		// When returnObject() is called - causes PoolableObjectFactory.vaildateObject to be called
		// Objects that fail to validate will be dropped from the pool
		genericObjectPoolConfig.testOnReturn = true;
		pool.setConfig(genericObjectPoolConfig);
	}

	public DriverEntry getDriverEntry(HeadlessVisitConfig visitConfig) throws NoSuchElementException, IllegalStateException, Exception {
		if(!visitGenerationEnabled) {
			throw new IllegalStateException("Pool shutdown in progress");
		}

		synchronized(driverEntryFactory) {
			//tell the factory what the IP and user agent will be if we borrow a new object (or reuse an existing one)
			driverEntryFactory.setData(visitConfig);
			LOGGER.trace(String.format("[%s] %s", visitConfig.getIpAddress(), "calling pool.borrowObject"));
			DriverEntry driverEntry = pool.borrowObject();
			LOGGER.debug(String.format("%s: %s %s", " pool.borrowObject - done", driverEntry, driverEntry.print()));
			return driverEntry;
		}
	}

	public void returnObject(DriverEntry driverEntry) {
		try {
			pool.returnObject(driverEntry);
			LOGGER.debug(String.format("%s: %s", "pool.returnObject", driverEntry.print()));
		}catch (Exception e) {
			LOGGER.error("Unable to returnObject driverEntry ipAddress [" + driverEntry.getIPAddress() + "] " + driverEntry.print(), e);
		}
	}

	public void start() {
		visitGenerationEnabled = true;
	}

	public void stopAll() {
		// stop button was pressed
		// when Manual Visits is clicked
		// when Stop (Standard) is clicked
		visitGenerationEnabled = false;
		shutDownChromeDrivers();
	}

	private void shutDownChromeDrivers( ) {

		// clear will cause the DriverEntryFactory.destroyObject( ) to be called for each passive object
		// shutting down each DriverEntry gracefully
		//
		// visitGenerationEnabled will be set to false on entry so each active pool entry will complete its work
		// without further actions then become idle
		if (shutDownInProgress.getAndSet(true))	return;

		// wait for all DriverEntry's to go idle
		int maxWait = 120;			// max loop of 1 minute - we should be done by then
		LOGGER.debug( getPoolInfo() );
		while ( (--maxWait > 0) && (pool.getNumActive() > 0 ))  {
			if(visitGenerationEnabled) { //stop shutdown execution
				shutDownInProgress.set(false);;
				return;
			}

			try {
				Thread.sleep(1000);			// one second
			} catch (InterruptedException e) {
				LOGGER.trace("shutDownChromeDrivers: thread sleep interupted.");
				e.printStackTrace();
			}
			LOGGER.trace( getPoolInfo() +", wait [" + maxWait + "]" + shutDownInProgress);
		}

		if (pool.getNumActive() > 0) {
			LOGGER.trace("shutDownChromeDrivers: waited for two minute and [" + pool.getNumActive() + "] pool objects are still active.");
		}

		// now release the DriverEntries as all should be idle
		try {
			LOGGER.trace("shutDownChromeDrivers: pool.clear()");
			pool.clear();
		} catch (UnsupportedOperationException e) {
			LOGGER.trace("shutDownChromeDrivers: pool.clear(): Unsupported exception [" + e.getMessage() + "]");
			e.printStackTrace();
		} catch (Exception e) {
			LOGGER.trace("shutDownChromeDrivers: pool.close(); Exception [" + e.getMessage() + "]");
		}
		shutDownInProgress.set(false);
		LOGGER.trace("shutDownChromeDrivers: complete");
	}

	public boolean isShutDownInProgress() {
		return shutDownInProgress.get();
	}

	public boolean isVisitGenerationEnabled() {
		return visitGenerationEnabled;
	}

	public String getPoolInfo() {
		return name + " Pool Info [max " + maximumChromeDrivers + "] > Active Objects [" + pool.getNumActive() + "], Idle Objects [" + pool.getNumIdle() + "]";
	}

	// for tests its handy to get access to the driver pool stats
	public int getNumActive( ) {
		return pool.getNumActive();
	}
	public int getNumIdle( ) {
		return pool.getNumIdle();
	}
	public int getTotalInPool() {
		return pool.getNumIdle() + pool.getNumActive();
	}
	public int getMaxActive() {
		return pool.getMaxActive();
	}
	public HeadlessStatistics getStats() {
		return stats;
	}
	
	@TestOnly
	void setMaxActive(int maxActive) {
		pool.setMaxActive(maxActive);
	}
}
