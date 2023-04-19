package com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.wordpressshop;

import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerPage;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUrlUtils;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.google.common.base.Strings;

import ch.qos.logback.classic.Logger;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class WordPressShopBasePage extends CustomerPage {
	
	private final String wordpressShop;
	private static final Logger LOGGER = LoggerFactory.make();
	
	public enum State {
		LOAD, RANDOM
	}

	public WordPressShopBasePage(EtPageType page, CustomerSession session) {
		super(page, session);
		wordpressShop = EasyTravelConfig.read().magentoShopUrl;
	}
	
	@Override
	protected String getHost() {
		if(Strings.isNullOrEmpty(wordpressShop)) {
			LOGGER.warn("PHP Shop host is not set in the configuration.");
			return super.getHost();
		}
		return wordpressShop;
	}
	
	@Override
	public void runInBrowser(Browser browser, UEMLoadCallback continuation)
			throws Exception {
		String url = UemLoadUrlUtils.getUrl(getHost(), getPage());
		loadPage(browser, url, continuation);
	}

}
