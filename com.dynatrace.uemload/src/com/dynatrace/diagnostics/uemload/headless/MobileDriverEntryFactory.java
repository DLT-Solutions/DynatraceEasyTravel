/**
 *
 */
package com.dynatrace.diagnostics.uemload.headless;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.chrome.ChromeOptions;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * @author tomasz.wieremjewicz
 * @date 24 sty 2019
 *
 */
public class MobileDriverEntryFactory extends DriverEntryFactory {	
	EasyTravelConfig config = EasyTravelConfig.read();
	private static final Logger LOGGER = LoggerFactory.make();
	
	@Override
	protected void configureChromeOptions(ChromeOptions co, int port) {
		try {
			Map<String, Object> deviceMetrics = new HashMap<>();
			deviceMetrics.put("width", visitConfig.getUser().getMobileDevice().getScreenWidth());
			deviceMetrics.put("height", visitConfig.getUser().getMobileDevice().getScreenHeight());
			deviceMetrics.put("pixelRatio", visitConfig.getUser().getMobileDevice().getPixelRatio());

			Map<String, Object> mobileEmulation = new HashMap<>();
			mobileEmulation.put("deviceMetrics", deviceMetrics);
			mobileEmulation.put("userAgent", visitConfig.getUser().getMobileDevice().getUserAgent());
			mobileEmulation.put("proxy-server", "localhost:" + port);

			co.setExperimentalOption("mobileEmulation", mobileEmulation);
		} catch (Exception e) {
			LOGGER.error("MobileDriverEntryFactory: cannot configure chrome for user: " + visitConfig.getUser().getName(), e);
		}
	}
}
