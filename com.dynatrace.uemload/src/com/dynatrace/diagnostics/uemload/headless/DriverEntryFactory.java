package com.dynatrace.diagnostics.uemload.headless;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.websocket.DeploymentException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSource;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import com.dynatrace.diagnostics.uemload.ThirdpartyResourceCache;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.process.HeadlessProcessNames;

import ch.qos.logback.classic.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * HeadlessVisitRunnable stores an object pool of chrome drivers
 * The object pool stores DriverEntry objects. This is a class factory for managing those objects
 *
 * @author Paul.Johnson
 * @author Michal.Bakula
 *
 */

public class DriverEntryFactory extends BasePoolableObjectFactory<DriverEntry> {
    private static final Logger LOGGER = LoggerFactory.make();
    private static final HttpHeaders trailingHeaders = new DefaultHttpHeaders();
    
	private static final String EMPTY_IMAGE = "/empty.png";
	static final byte[] EMPTY_IMAGE_BYTES = getImageBytes(EMPTY_IMAGE);
	private static final String DYNATRACE_ICO = "/dynatrace.ico";
	static final byte[] DYNATRACE_ICO_BYTES = getImageBytes(DYNATRACE_ICO);
	
	private int reUseChromeDriverFrequency=1;
	HeadlessVisitConfig visitConfig = null;
		
	static {
		setChromeDirs();
	}

	public static void setChromeDirs() {
		if(SystemUtils.IS_OS_LINUX) {
			System.setProperty("webdriver.chrome.driver", HeadlessProcessNames.PATH_TO_CHROME_DRIVER_LINUX);
		} else {
			System.setProperty("webdriver.chrome.driver", HeadlessProcessNames.PATH_TO_CHROME_DRIVER_WINDOWS);
		}
	}

	private static byte[] getImageBytes(String resourceName) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
        
		URL resource = getImageResource(resourceName);
		if (resource != null) {
			try ( InputStream input = resource.openStream() ) {
				int n;
				byte[] read = new byte[64];
				while ((n = input.read(read, 0, read.length)) != -1) {
					output.write(read, 0, n);
				}

			} catch (IOException e) {
				LOGGER.warn("Cannot get content of empty image from resource: " + EMPTY_IMAGE, e);
			}
		}
		return output.toByteArray();
	}
	
	private static URL getImageResource(String resourceName) {
		URL resource  = DriverEntryFactory.class.getResource(resourceName); //if started in process                
        if (resource == null) {
        	resource = DriverEntryFactory.class.getResource("/resources" + resourceName); //if started as procedure
		}
        return resource;
	}

	public DriverEntryFactory() {
		EasyTravelConfig config = EasyTravelConfig.read();
		reUseChromeDriverFrequency = config.reUseChromeDriverFrequency;
	}

	/**
	 * called on pool.borrowObject( ) if a new object needs to be created
	 * ie we haven't got an idle one and we haven't gone over maxActive
	 *
	 */
	@Override
    public DriverEntry makeObject() throws Exception {
		LOGGER.trace( "makeObject" );
		ChromeDriver driver;
		// create the new DriverEntry so that we can pass it to the proxyConfigurator
		// it will be updated with a ChromeDriver and HttpProxyServer later on...
		ChromeDriverService chromeDriverService = ChromeDriverService.createDefaultService();
		DriverEntry driverEntry = new DriverEntry(visitConfig.getIpAddress());
		HttpProxyServer proxyServer = runProxyServer(driverEntry);
		
		try {
			int proxyPort = proxyServer.getListenAddress().getPort();
			driver = new ChromeDriver(chromeDriverService, createChromeOptions(proxyPort, driverEntry));
		} catch (Exception e) {
			chromeDriverService.stop();
			proxyServer.stop();
			proxyServer.abort();
			FileUtils.deleteDirectory(driverEntry.getChromeUserDir().toAbsolutePath().toFile());
			throw e;
		}

		driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
		// set up the driverEntry with the new data
		driverEntry.update(driver, chromeDriverService, proxyServer, visitConfig.getIpAddress());
		driverEntry.startChromeDevToolsConnection();

		LOGGER.debug("create driver " + driverEntry + " " + driverEntry.print());	
		return driverEntry;
    }
	
	HttpProxyServer runProxyServer(DriverEntry driverEntry) {
		HttpProxyServer proxyServer=null;
		try {

			HttpProxyServerBootstrap proxyConfiguartor = DefaultHttpProxyServer.bootstrap().withPort(0).withFiltersSource(getFilter(driverEntry));

			// this allows throttling to be turned off
			if (EasyTravelConfig.read().headlessThrottlingEnabled) {
				proxyConfiguartor.withThrottling(visitConfig.getBandWidthLimit(), visitConfig.getBandWidthLimit());
				LOGGER.info( "makeObject - added throttling" );
			}

			proxyServer = proxyConfiguartor.start();
			LOGGER.trace( "makeObject - created DriverproxyConfigurator - started" );

		} catch (Exception e) {
			LOGGER.error( "Proxy error [" + e.getMessage() +"]" );
			throw(e);
		}		
		return proxyServer;
	}

	private ChromeOptions createChromeOptions(int proxyPort, DriverEntry driverEntry) {
		EasyTravelConfig config = EasyTravelConfig.read();
		ChromeOptions co = new ChromeOptions();
		configureChromeOptions(co, proxyPort);
		
		co.addArguments("no-sandbox");
		
		String userDir = driverEntry.getChromeUserDir().toAbsolutePath().toString();
		co.addArguments("user-data-dir=" + userDir);
		
        // by default showHeadlessbrowser is off - so it should run silently
        // but for debugging it is handy to see the browser
        if (!config.showHeadlessBrowser) {
        	co.addArguments("headless");
        }

        // additional Chrome driver options to pass
        if(config.chromeDriverOpts != null) {
            co.addArguments(config.chromeDriverOpts);
        }

        // We are using Chrominum since this is open source so we wont be restricted on redistribution in the license
        // downloaded from http://www.chromium.org/getting-involved/download-chromium
        // then click link https://download-chromium.appspot.com
        //
        // Chrome is placed in the Installation directory (the easyTravel root folder on installation)
        // note - no need to log the path to the binary - if it is wrong the exception on automation with report the path
		if (SystemUtils.IS_OS_LINUX){
	        co.setBinary(HeadlessProcessNames.PATH_TO_CHROMIUM_LINUX);
		} else {
			// assume default windows
	        co.setBinary(HeadlessProcessNames.PATH_TO_CHROMIUM_WINDOWS);
		}
		return co;
	}

	protected void configureChromeOptions(ChromeOptions co, int port) {
		co.addArguments(
       		 "disable-gpu"
       		, "user-agent=" + visitConfig.getUserAgent()
       		, "proxy-server=localhost:" + port
       		, visitConfig.getBrowserWindowSize()
   		);
	}

    /**
     * Called either
     * 1. when validateObject returns false - ie we have reached our max use count
     * or 2. when pool.clear()
     */
    @Override
    public void destroyObject( DriverEntry drv ) {
    	// DriverEntry is about to be removed from the pool
    	// clear down the ChromeDriver and proxy objects
		LOGGER.info("Driver being destroyed: " + drv + " " + drv.print() );
    	drv.shutDown();
    }

    @Override
    public boolean validateObject( DriverEntry drv ) {
    	// when testOnBorrow or testOnReturn is set the pool will call validateObject. Objects that return false will
    	// be dropped from the pool
    	try {
    		LOGGER.debug("validateObject: " + drv + " " + drv.print());
    		return drv.isHealthy() && drv.getUseCnt() < reUseChromeDriverFrequency;
    	} catch (Exception e) {
    		LOGGER.error("Error validating object"+ drv.print() + "  for: " + visitConfig.getUser(), e);
    		return false;
    	}    	
    }

    @Override
    public void activateObject( DriverEntry drv ) throws DeploymentException, IOException, URISyntaxException {
    	// we could be re-using an existing object so assign the current factory IP Address
    	try {
    		LOGGER.debug("activateObject: " + drv.print());
    		drv.activateDriver(visitConfig.getIpAddress());
    		drv.setVisitData(visitConfig);
    	} catch (Exception e) {
    		LOGGER.error("Error activating object " + drv.print() + " for: " + visitConfig.getUser(), e);
		}
    }

    @Override
    public void passivateObject( DriverEntry drv ) {
    	// called when returnObject is called.
    }

    public void setReUseChromeDriverFrequency( int reUseChromeDriverFrequency ) {
    	this.reUseChromeDriverFrequency = reUseChromeDriverFrequency;
    }

    // API to call prior to borrowObject to prime the factory with the ip and userAgent to use
    public void setData( HeadlessVisitConfig visitConfig ) {
    	this.visitConfig = visitConfig;
    }

	/**
	 * This function is called for each DriverEntry (or associated proxy)
	 * When a web request occurs it will get called and use the IP address currently set in the DriverEntry
	 *
	 * @param driverEntry
	 * @return
	 */
	HttpFiltersSource getFilter(final DriverEntry driverEntry) {
		return new HttpFiltersSourceAdapter() {
            
			@Override
            public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                return new HttpFiltersAdapter(originalRequest) {
                    
                	@Override
                    public HttpResponse clientToProxyRequest(HttpObject httpObject) {                    	 
                        if(httpObject instanceof HttpRequest) {
                        	HttpRequest request = (HttpRequest) httpObject;
                            parseCookies(request);
                            int responseSize = ThirdpartyResourceCache.getResponseSize(request.uri());
                            if(responseSize != ThirdpartyResourceCache.NO_THIRD_PARTY_RESOURCE) {
                            	String mimeType = ThirdpartyResourceCache.getMimeType(request.uri());
                            	byte[] bodyContent = getResponseBody(mimeType, responseSize);
                            	ByteBuf wrappedBuffer = Unpooled.wrappedBuffer( bodyContent );
                            	HttpHeaders headers = new DefaultHttpHeaders().add("Content-Length", bodyContent.length);                            	
                            	if(mimeType != null) {
                            		headers = headers.add("Content-Type", mimeType);                            		
                            	}
                            	
                            	return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, wrappedBuffer, headers, trailingHeaders);
                            }                            
                        }
                        return null;
                    }
                    
                	private void parseCookies(HttpRequest request) {
                    	String cookies = request.headers().get("Cookie");
                    	if(cookies != null && cookies.contains("xhr_fail=xhr_fail") && request.uri().contains("orange.jsf")) {
                        	// Because of the xhr_fail cookie this request must come from a HeadlessXhrErrorAction.
                            request.setMethod(HttpMethod.GET);
                            request.setUri(request.uri().substring(0, request.uri().indexOf("orange.jsf")) + "error500");
                        }
                    }
                    
                    private byte[] getResponseBody(String mimeType, int responseSize) {
                    	if ("image/png".equals(mimeType) ) {
                    		return Arrays.copyOf(EMPTY_IMAGE_BYTES, EMPTY_IMAGE_BYTES.length);
                    	} 
                    	
                    	if("image/vnd.microsoft.icon".equals(mimeType) ) {
                    		return Arrays.copyOf(DYNATRACE_ICO_BYTES, DYNATRACE_ICO_BYTES.length);
                    	}
                    	
                    	return new byte[responseSize];
                    }
                };
            }
        };
	}

}
