package com.dynatrace.diagnostics.uemload.scenarios.easytravel.pages.wordpressshop;

import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.CustomerSession;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUrlUtils;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class WordPressShopProductPage extends WordPressShopBasePage{

	private Product[] products;
	
	public WordPressShopProductPage(CustomerSession session, Product[] products) {
		super(EtPageType.WORDPRESS_SHOP_PRODUCT, session);
		this.products = products;
	}
	
	@Override
	public void runInBrowser(Browser browser, UEMLoadCallback continuation)
			throws Exception {
		String url = UemLoadUrlUtils.getUrl(getHost(), getPage().getPath()+products[UemLoadUtils.randomInt(products.length)].getPath());
		loadPage(browser, url, continuation);
	}

}
