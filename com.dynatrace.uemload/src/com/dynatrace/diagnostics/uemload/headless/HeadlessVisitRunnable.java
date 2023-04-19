package com.dynatrace.diagnostics.uemload.headless;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.HeadlessMobileAngularSimulator;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

/**
 * Performance improvements to allow multiple headless Chromium browsers to be run without overloading:
 * There were instances when at 25 visits per minute we could have 40+ chromium browsers all running at the same time that could bring the system down
 * the code now performs the following:
 * 1. An ObjectPool of chromedrivers and their associated proxy's is now kept
 * 2. Each chromedriver can be used up to reUseChromeDriverFrequency times - this reduces the overhead of creating a new chromedriver for each visit.
 *    However the bandwidth and useragent string will be the same for each visit/chromedriver as the same proxy is used.
 *    Therefore we delete and recreate every reUseChromeDriverFrequency times.
 * 3. The IP address of each visit is stored in the DriverEntry object and determined when DriverEntryFactory.getFilter( ) is called for each web request.
 * 4. The maximumChromeDriver setting is used to limit the number of ChromeDrivers available at any one time
 *
 * @author Paul.Johnson
 * @author Michal.Bakula
 *
 */

public abstract class HeadlessVisitRunnable implements Runnable {
    private static final Logger LOGGER = LoggerFactory.make();

	private DriverEntry driverEntry;					// entry in driverMap currently being used
	private final Visit visit;
	private final HeadlessVisitConfig visitConfig;
	private final DriverEntryPool pool;
	private final boolean isMobile;
	private final Simulator simulator;
	
	public HeadlessVisitRunnable(Visit visit, ExtendedCommonUser user, Simulator simulator) {
		this.visit = visit;
		this.visitConfig = new HeadlessVisitConfig(user);
		this.isMobile = isMobileSimulator(simulator);
		this.pool = isMobile ? MobileDriverEntryPoolSingleton.getInstance().getPool() : DriverEntryPoolSingleton.getInstance().getPool();
		this.simulator = simulator;
	}
	
	public static final boolean isMobileSimulator(Simulator simulator) {
		return (simulator instanceof HeadlessMobileAngularSimulator);
	}

	abstract boolean simulatorVisitsGenerationEnabled();
	protected abstract int incrementAndGetRunningVisits();
	protected abstract int decrementAndGetRunningVisits();
	protected abstract int getRunningVisits();

	@Override
	public void run() {
		boolean createVisit = true;	// flag to indicate if we have enough drivers available to create a new visit

		// don't run if we are stopped
		if (shouldGenerateVisits()) {
			HeadlessActionExecutor exec = new HeadlessActionExecutor(visitConfig.getLocation(), 0, null, null, visitConfig.getUserAgent());
			ChromeDriver drv = null;
			try {

				LOGGER.trace(addIpAddressToMessage( pool.getPoolInfo() ));
				
				if(isMobile && visitConfig.getUser().getMobileDevice() == null) {
					LOGGER.info("Skipping mobile visit for user without mobile device: " + visitConfig.getUser());
					createVisit = false;
				}

				if (createVisit) {
					try {
						driverEntry = pool.getDriverEntry(visitConfig);
						createVisit = true;
					}catch (Exception e) {
						LOGGER.info(addIpAddressToMessage("All objects currently active - visit will be ignored [" + e.getMessage() + "]"), e);
						createVisit = false;
					}
				}

				if (createVisit) {
					drv = driverEntry.getDriver();
					exec.setDriver( drv );
					int runningVists = incrementAndGetRunningVisits();
					logVisit(runningVists);
					pool.getStats().addVisitStarted(driverEntry);
					for(Action action : visit.getActions(visitConfig.getUser(), visitConfig.getLocation())) {
						if (shouldGenerateVisits()) {
							action.run(exec, null);
						} else {
							LOGGER.trace(addIpAddressToMessage("Stop button pressed whilst executing visit [" + visit.getVisitName() + "] [" + visitConfig.getIpAddress() + "], visit will be abandonded"));
							break;
						}
					}
										
				} else {
					pool.getStats().addVisitSkipped();
				}				
			} catch(Exception e) {
				// we could be here because...
				// 1. a timeout occurred (actions have a 15 second timeout)
				// 2. we are shutting down and action.run was against a browser that no longer exists (this should not happen)
				// 3. something unexpected happened
				LOGGER.error(addIpAddressToMessage("Exception during execution of action: visitGenerationEnabled [" + shouldGenerateVisits()
						+ "] ["+ visit.getVisitName() + "] [" + visitConfig.getIpAddress() + "] message [" + e.getMessage() + "]" + " " + driverEntry.print()), e);
				pool.getStats().addVisitException(driverEntry);
			} finally {
				// in order for us to re-use the ChromeDriver without closing the browser
				// we need to perform the following so that a new dtCookie gets created and
				// therefore a new visit on the next use of this ChromeDriver
				if (drv != null && shouldGenerateVisits()) {
					sendEndVisitSignal(drv);
					cleanupChromeDriver(drv);
				}

				if (createVisit) {
					pool.getStats().addVisitCompleted(driverEntry);
					decrementAndGetRunningVisits();
				}

				if(driverEntry != null) {
					pool.returnObject(driverEntry);
				}
			}
		} else {

			String	msg = "Stop button has been pressed. Visit will be abandoned, running visits [" + getRunningVisits()  + "]" ;
			LOGGER.trace(addIpAddressToMessage(msg));
		}
		
		simulator.incNumberOfFinishedVisits();
		LOGGER.trace("Finished visit: " + visitConfig.getIpAddress() );
	}		
	
	private void sendEndVisitSignal(ChromeDriver drv) {
		LOGGER.debug("start closing visit " + visitConfig.getIpAddress());
		try {
			if (drv instanceof JavascriptExecutor) {
				Thread.sleep(5000);
				((JavascriptExecutor) drv).executeScript("if(typeof dtrum !== 'undefined') dtrum.endSession();");
				Thread.sleep(2000);
			}
		} catch (Exception e) {
			LOGGER.error("Error sending dtrum.endSession() signal", e);
		}
		LOGGER.debug("finished closing visit " + visitConfig.getIpAddress() + " " + drv);
	}
	
	private void cleanupChromeDriver(ChromeDriver chromeDriver) {
    	try {
    		chromeDriver.manage().deleteAllCookies();
    		chromeDriver.getSessionStorage().clear();
    		chromeDriver.getLocalStorage().clear();
    	} catch (Exception e) {
    		driverEntry.markUnhealthy();
    		LOGGER.error("Error passivating object " + driverEntry.print() + " for: " + visitConfig.getUser(), e);
    	}

	}

	private boolean shouldGenerateVisits() {
		return simulatorVisitsGenerationEnabled() && pool.isVisitGenerationEnabled();
	}
	
	private String addIpAddressToMessage(String msg) {
		return String.format("[%s] %s", visitConfig.getIpAddress(), msg);
	}
			
	private void logVisit(int runningVists) {
		LOGGER.trace(addIpAddressToMessage(addUserInfoToMessage(TextUtils.merge("Running visit type [{0}] running visits [{1}]", visit.getVisitName(), runningVists))));		
	}
	
	private String addUserInfoToMessage(String msg) {
 		return TextUtils.merge("{0} user [{1}] user-agent [{2}] x-forwarded-for [{3}] visitorID [{4}] mobile [{5}]",
 				msg, 
				visitConfig.getUser().getName(), visitConfig.getUserAgent(), visitConfig.getLocation().getIp(), 
				visitConfig.isNewVisitor() ? "" : visitConfig.getVisitorId(),
				isMobile);		 		
	}
}