package com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.wordpressshop;

import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class WordPressShopHomePage extends WordPressShopBasePage {

	public WordPressShopHomePage(CustomerSession session) {
		super(EtPageType.WORDPRESS_SHOP_START_PAGE, session);
	}

}
