package com.dynatrace.easytravel.magento;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractPagePlugin;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.base.Strings;

import ch.qos.logback.classic.Logger;

/**
 * @author Rafal.Psciuk
 *
 */
public class MagentoShop extends AbstractPagePlugin {

	private static final Logger log = LoggerFactory.make();
	
	private final String magentoShopUrl;
		
	private static final String MAGENTO_SHOP_IMG_PATTERN = 
			"<span style=\"margin-left: 10px;\">"
			+ "<a target=\"_blank\" href=\"{0}\">"
			+ "<img alt=\"Madison Island\" src=\"img/travelgear.png\" title=\"Madison Island - shop for travel gear and more.\" height=\"35\"/>"
			+ "</a>"
			+ "</span>";


	public MagentoShop() {
		String url = EasyTravelConfig.read().magentoShopUrl;
		if (Strings.isNullOrEmpty(url)) {
			log.warn("Magento shop URL is not set");
			magentoShopUrl = null;
		} else {
			magentoShopUrl = TextUtils.merge(MAGENTO_SHOP_IMG_PATTERN, url);
		}
	}
	
	@Override
	public Object getContentFinish() {
		if (Strings.isNullOrEmpty(magentoShopUrl)) {
			log.warn("Image Gallery URL is not set.");
			return null;
		}
		return magentoShopUrl;
	}
}
