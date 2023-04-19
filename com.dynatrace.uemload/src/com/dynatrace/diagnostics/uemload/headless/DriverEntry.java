package com.dynatrace.diagnostics.uemload.headless;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.littleshoot.proxy.HttpProxyServer;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;

import com.dynatrace.easytravel.util.process.HeadlessProcessNames;

/**
 * HeadlessVisitRunnable keeps a pool of ChromeDrivers currently being used
 * This class is an entry for one driver - it allows us to control how many times its been used,
 * release of the Chrome driver and its proxy
 *
 * @author Paul.Johnson
 *
 */
class DriverEntry {
    private static final Logger LOGGER = Logger.getLogger(HeadlessVisitRunnable.class.getName());

    private ChromeDriver driver;
    private ChromeDriverService driverService;
	private HttpProxyServer proxyServer;
	private String ipAddress;
	private int useCnt=0;			// allows to monitor the number of times this DriverEntry has been used
	private long startTime;
	private ChromeDevToolsConnection devToolsConnection;
	private int debugPort = 0;
	private final String userDir = Long.toHexString(System.currentTimeMillis());
	private boolean healthy = true;
	
	public DriverEntry( String ipAddress ) {
		// we always create a driver entry with an ipAddress to that getFilter always will return one
		this.ipAddress = ipAddress;
		this.startTime = 0;
		
	}

	public synchronized void update( ChromeDriver driver, ChromeDriverService driverService, HttpProxyServer proxyServer, String ipAddress ) {
		this.driver = driver;
		this.driverService = driverService;
		this.proxyServer = proxyServer;
		this.ipAddress = ipAddress;
		this.startTime = 0;
	}
	public synchronized ChromeDriver getDriver() {
		return driver;
	}
	public synchronized HttpProxyServer getProxy() {
		return proxyServer;
	}
	public synchronized String getIPAddress () {
		return ipAddress;
	}
	public synchronized int getUseCnt() {
		return useCnt;
	}
	public synchronized  void activateDriver( String ip ) {
		// driver made active
		ipAddress = ip;
		useCnt++;
		this.startTime = System.currentTimeMillis();
	}
	public synchronized  void passivateDriver( ) {
		// driver made idle
		this.startTime = 0;
	}
	
	public synchronized  long getStartTime( ) {
		return this.startTime;
	}
	
	public synchronized void shutDown( ) {
		LOGGER.log(Level.SEVERE, "DriverEntry shutDown. [" + this + " "  + print() + "]");
		
		shutDownDriver();		
		shutdownDriverService();		
		shutDownProxyServer();		
		stopDevToolsConnection();		
		deleteChromeDir();
	}

	private void deleteChromeDir() {
		try {			
			FileUtils.deleteDirectory(getChromeUserDir().toAbsolutePath().toFile());
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Exception when resetting DriverEntry. - deleteDirectory [" + this + "]", e);				
		}
	}

	private void stopDevToolsConnection() {
		try {			
			stopChromeDevToolsConnection();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Exception when resetting DriverEntry. - stopChromeDevToolsConnection [" + this + "]", e);				
		}
	}

	private void shutDownProxyServer() {
		try {			
			if(proxyServer != null) {
				proxyServer.stop();
				proxyServer.abort();
				proxyServer = null;
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Exception when resetting DriverEntry. - proxyServer [" + this + "]", e);				
		}
	}

	private void shutdownDriverService() {
		try {			
			if(driverService != null) {
				driverService.stop();
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Exception when resetting DriverEntry - driverService. [" + this + "]", e);				
		}
	}
	
	private void shutDownDriver() {
		try {			
			if(driver != null) {
				driver.quit();
				driver = null;
			}			
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Exception when resetting DriverEntry - driver. [" + this + "]", e);				
		}
	}

	public synchronized String print( ) {
		String drvstatus = driver==null ? "NULL" : driver.toString() + " " + driverService.getUrl();
		return 
			" useCnt: [" + useCnt + "]"
			+ " drv: [" + drvstatus + "]"
			+ " proxy: [" + proxyServer + " " + proxyServer.getListenAddress() + "]"
			+ " chrome_driver: [" + driverService.getUrl() + "]"
			+ " debug port: [" + debugPort + "]"
			+ " ip: [" + ipAddress + "]"
			+ " healthy: [" + healthy + "]";
	}
	
	public synchronized void markUnhealthy() {
		healthy = false;
	}
	
	public synchronized boolean isHealthy() {
		return healthy;
	}
		
	public void startChromeDevToolsConnection() {
		try {
			debugPort = findDebugPort();
			devToolsConnection = new ChromeDevToolsConnection(debugPort);
			devToolsConnection.startConnection();
			devToolsConnection.enableNetwork();
		} catch(Exception e) {
			LOGGER.log(Level.SEVERE, "Cannot connect to chrome dev tools", e);
		}
	}
		
	public void stopChromeDevToolsConnection() throws IOException {
		if(devToolsConnection != null) {
			devToolsConnection.closeConnection();
		}
	}
	
	public void setVisitData(HeadlessVisitConfig visitConfig) throws IOException {
		devToolsConnection.clearCookies();
		if(!visitConfig.isNewVisitor()) {
			devToolsConnection.setVisitorIdCookie(visitConfig.getVisitorId());
		}
		devToolsConnection.setUserAgent(visitConfig.getUserAgent());
		devToolsConnection.setLocation(visitConfig.getIpAddress());
	}
	
	public int getDebugPort() {
		return debugPort;
	}
	
	private int findDebugPort() throws IOException {
		@SuppressWarnings("unchecked")
		String userDataDir = ((Map<String, Object>)driver.getCapabilities().getCapability("chrome")).get("userDataDir").toString();
		List<String> allLines = Files.readAllLines(Paths.get(userDataDir, "DevToolsActivePort"));
		return allLines.stream()
				.map(Integer::valueOf)
				.findFirst()
				.orElseThrow(() ->  new NoSuchElementException("Port not found"));
	}
	
	public Path getChromeUserDir() {
		return Paths.get(HeadlessProcessNames.PATH_TO_CHROME_USER_DIR, userDir);
	}
}
