package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import java.util.List;

import com.dynatrace.diagnostics.uemload.dtheader.DynaTraceTagMetaData;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Title;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Url;
import com.google.common.collect.Lists;

public enum EtPageType {

	START(Url.START, Title.START, DynaTraceTagMetaData.START, 700),
	REVIEW(Url.REVIEW, Title.REVIEW, DynaTraceTagMetaData.REVIEW, 900),
	PAYMENT(Url.PAYMENT, Title.PAYMENT, DynaTraceTagMetaData.PAYMENT, 600),
	FINISH(Url.PURCHASE, Title.PURCHASE, DynaTraceTagMetaData.PURCHASE, 600),
	TERMS(Url.TERMS, Title.TERMS, DynaTraceTagMetaData.TERMS, 400),
	PRIVACY(Url.PRIVACY, Title.PRIVACY, DynaTraceTagMetaData.PRIVACY, 400),
	CONTACT(Url.CONTACT, Title.CONTACT, DynaTraceTagMetaData.CONTACT, 800),
	ABOUT(Url.ABOUT, Title.ABOUT, DynaTraceTagMetaData.ABOUT, 850),
	SEO(Url.SEO, Title.SEO, DynaTraceTagMetaData.SEO, 850),
	SEO_ABOUT(Url.SEO_ABOUT, Title.ABOUT, DynaTraceTagMetaData.ABOUT, 850),
	SEO_CONTACT(Url.SEO_CONTACT, Title.CONTACT, DynaTraceTagMetaData.CONTACT, 850),
	LOGOUT(Url.LOGOUT, Title.LOGOUT, DynaTraceTagMetaData.LOGOUT, 200),
	TRIPDETAILS(Url.TRIPDETAILS, Title.TRIPDETAILS, DynaTraceTagMetaData.TRIPDETAILS, 600),
	WEATHERFORECAST(Url.WEATHERFORECAST, Title.WEATHERFORECAST, DynaTraceTagMetaData.WEATHERFORECAST, 600),
	IMAGE_GALLERY(Url.IMAGE_GALLERY, Title.IMAGE_GALLERY, DynaTraceTagMetaData.IMAGE_GALLERY, 400),
	BLOGDETAILS(Url.BLOGDETAILS, Title.BLOGDETAILS, DynaTraceTagMetaData.BLOGDETAILS, 600),
	SPECIAL_OFFERS(Url.SPECIAL, Title.SPECIAL, DynaTraceTagMetaData.SPECIAL, 800),
	AMP(Url.AMP, Title.AMP_TITLE, DynaTraceTagMetaData.AMP, 400),

	B2B_HOME(Url.HOME, Title.HOME, DynaTraceTagMetaData.B2B_HOME, 975),
	B2B_LOGIN(Url.LOGIN, Title.LOGIN, DynaTraceTagMetaData.B2B_LOGIN, 900),
	B2B_LOGOUT(Url.B2BLOGOUT, Title.B2BLOGOUT, DynaTraceTagMetaData.B2B_LOGOUT, 900),
	B2B_JOURNEY(Url.JOURNEY, Title.JOURNEY, DynaTraceTagMetaData.B2B_JOURNEYS, 900),
	B2B_REPORT(Url.REPORT, Title.REPORT, DynaTraceTagMetaData.B2B_REPORT, 900),
	B2B_BOOKING(Url.BOOKING, Title.BOOKING, DynaTraceTagMetaData.B2B_BOOKING, 900),

	MOBILE_CONTACT(Url.MOBILE_CONTACT, Title.CONTACT, DynaTraceTagMetaData.CONTACT, 500),
	MOBILE_TERMS(Url.MOBILE_TERMS, Title.TERMS, DynaTraceTagMetaData.TERMS, 330),
	MOBILE_PRIVACY(Url.MOBILE_PRIVACY, Title.PRIVACY, DynaTraceTagMetaData.PRIVACY, 250),
	
	WORDPRESS_SHOP_START_PAGE(Url.WORDPRESS_SHOP_START_PAGE, Title.WORDPRESS_SHOP_START_PAGE, DynaTraceTagMetaData.WORDPRESS_SHOP_START_PAGE, 600),
	WORDPRESS_SHOP_BLOG(Url.WORDPRESS_SHOP_BLOG, Title.WORDPRESS_SHOP_BLOG, DynaTraceTagMetaData.WORDPRESS_SHOP_BLOG, 600),
	WORDPRESS_SHOP_ACCESSORIES(Url.WORDPRESS_SHOP_ACCESSORIES, Title.WORDPRESS_SHOP_ACCESSORIES, DynaTraceTagMetaData.WORDPRESS_SHOP_ACCESSORIES, 600),
	WORDPRESS_SHOP_CLOTHING(Url.WORDPRESS_SHOP_CLOTHING, Title.WORDPRESS_SHOP_CLOTHING, DynaTraceTagMetaData.WORDPRESS_SHOP_CLOTHING, 600),
	WORDPRESS_SHOP_DECOR(Url.WORDPRESS_SHOP_DECOR, Title.WORDPRESS_SHOP_DECOR, DynaTraceTagMetaData.WORDPRESS_SHOP_DECOR, 600),
	WORDPRESS_SHOP_HOODIES(Url.WORDPRESS_SHOP_HOODIES, Title.WORDPRESS_SHOP_HOODIES, DynaTraceTagMetaData.WORDPRESS_SHOP_HOODIES, 600),
	WORDPRESS_SHOP_MUSIC(Url.WORDPRESS_SHOP_MUSIC, Title.WORDPRESS_SHOP_MUSIC, DynaTraceTagMetaData.WORDPRESS_SHOP_MUSIC, 600),
	WORDPRESS_SHOP_TSHIRTS(Url.WORDPRESS_SHOP_TSHIRTS, Title.WORDPRESS_SHOP_TSHIRTS, DynaTraceTagMetaData.WORDPRESS_SHOP_TSHIRTS, 600),
	WORDPRESS_SHOP_PRODUCT(Url.WORDPRESS_SHOP_PRODUCT, Title.WORDPRESS_SHOP_PRODUCT, DynaTraceTagMetaData.WORDPRESS_SHOP_PRODUCT, 600);

	private final String siteUrl;
	private final String title;
	private final int pageLoadTime;
	private final DynaTraceTagMetaData metaData;

	EtPageType(String url, String title, DynaTraceTagMetaData metaData, int pageLoadTime) {
		this.siteUrl = url;
		this.title = title;
		this.pageLoadTime = pageLoadTime;
		this.metaData = metaData;
	}
	
	@Override
	public String toString() {
		throw new IllegalArgumentException();
	}

	public String getPath() {
		return siteUrl;
	}

	public String getTitle() {
		return title;
	}


	public int getPageLoadTime() {
		return pageLoadTime;
	}

	public int getActionLoadTime(PageAction pageAction) {
		return pageAction.loadTime;
	}

	public enum PageAction {
		SEARCH(225),
		CALENDAR(115),
		TRIPDETAILS(220),
		BLOGDETAILS(230),
		NEXT(350),
		BACK(130),
		NEW_SEARCH(120),
		CLEAR(50),
		LOGIN(150),
		BOOK_NOW(300),
		FINISH(275),
		IMAGE_GALLERY(180),
		ONLOAD_XHR(500);

		private final int loadTime;

		private PageAction(int loadTime) {
			this.loadTime = loadTime;
		}
	}

	// List of front-end urls used by uemload: only on those pages we want to generate
	// JavaScript errors.
	public static List<String> EtCustomerPartialUrls = Lists.newArrayList(
		Url.START,
		Url.REVIEW,
		Url.PAYMENT,
		Url.PURCHASE,
		Url.TERMS,
		Url.PRIVACY,
		Url.CONTACT,
		Url.ABOUT,
		Url.SEO,
		Url.SEO_ABOUT,
		Url.SEO_CONTACT,
		Url.LOGOUT,
		Url.TRIPDETAILS,
		Url.SPECIAL,
		Url.MOBILE_CONTACT,
		Url.MOBILE_TERMS,
		Url.MOBILE_PRIVACY );
		
	/**
	 * We do not specify MAGENTO_SHOP_START_PAGE here because it is just "/" and possibly can be used in other applications 
	 */
	public static final List<String> EtMagentoShopUrls = Lists.newArrayList(
		Url.WORDPRESS_SHOP_BLOG,
		Url.WORDPRESS_SHOP_ACCESSORIES,
		Url.WORDPRESS_SHOP_CLOTHING,
		Url.WORDPRESS_SHOP_DECOR,
		Url.WORDPRESS_SHOP_HOODIES,
		Url.WORDPRESS_SHOP_MUSIC,
		Url.WORDPRESS_SHOP_PRODUCT,
		Url.WORDPRESS_SHOP_TSHIRTS);
	
	public DynaTraceTagMetaData getMetaData() {
		return metaData;
	}

}
