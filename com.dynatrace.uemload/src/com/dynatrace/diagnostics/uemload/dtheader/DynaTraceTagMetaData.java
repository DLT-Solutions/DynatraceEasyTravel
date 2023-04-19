package com.dynatrace.diagnostics.uemload.dtheader;

import com.dynatrace.easytravel.constants.BaseConstants.DtHeader.PageContext;
import com.dynatrace.easytravel.constants.BaseConstants.DtHeader.ScriptName;
import com.dynatrace.easytravel.constants.BaseConstants.DtHeader.TimerName;


public enum DynaTraceTagMetaData {
	START(TimerName.START_PAGE, ScriptName.BOOKING_JOURNEY),
	LOGIN(TimerName.LOGIN, ScriptName.BOOKING_JOURNEY),
	SEARCH(TimerName.SEARCH, ScriptName.BOOKING_JOURNEY),
	REVIEW(TimerName.BOOKING_REVIEW, ScriptName.BOOKING_JOURNEY),
	PAYMENT(TimerName.PAYMENT, ScriptName.BOOKING_JOURNEY),
	PURCHASE(TimerName.PURCHASE, ScriptName.BOOKING_JOURNEY),
	TERMS(TimerName.TERMS, ScriptName.TERMS_CONTRACT),
	PRIVACY(TimerName.PRIVACY, ScriptName.PRIVACY_AGREEMENT),
	CONTACT(TimerName.CONTACT, ScriptName.CONTACT_INFORMATION),
	ABOUT(TimerName.ABOUT, ScriptName.ABOUT_INFORMATION),
	SEO(TimerName.SEO, ScriptName.SEO_INFORMATION),
	LOGOUT(TimerName.LOGOUT, ScriptName.LOGOUT_INFORMATION),
	ENDING(TimerName.FINISH_BOOKING, ScriptName.BOOKING_JOURNEY),
	B2B_HOME(TimerName.B2B_HOME, ScriptName.ADMINISTRATION),
	TRIPDETAILS(TimerName.TRIPDET, ScriptName.TRIPDETAILS),
	WEATHERFORECAST(TimerName.WEATHERFORECAST, ScriptName.WEATHERFORECAST),
	IMAGE_GALLERY(TimerName.IMAGE_GALLERY, ScriptName.IMAGE_GALLERY),
	SPECIAL(TimerName.SPECIAL, ScriptName.SECIAL),
	AMP(TimerName.AMP_WEBSITE, ScriptName.AMP_WEBSITE),

	BLOGDETAILS(TimerName.BLOGDET, ScriptName.BLOGDETAILS)
	{

		@Override
		public String getPageContext() {
			return PageContext.B2B_FRONTEND;
		}
	},
	B2B_LOGIN(TimerName.B2B_LOGIN, ScriptName.ADMINISTRATION) {

		@Override
		public String getPageContext() {
			return PageContext.B2B_FRONTEND;
		}
	},
	B2B_LOGOUT(TimerName.B2B_LOGOUT, ScriptName.ADMINISTRATION) {

		@Override
		public String getPageContext() {
			return PageContext.B2B_FRONTEND;
		}
	},
	B2B_JOURNEYS(TimerName.B2B_JOURNEYS, ScriptName.ADMINISTRATION) {

		@Override
		public String getPageContext() {
			return PageContext.B2B_FRONTEND;
		}
	},
	B2B_REPORT(TimerName.B2B_REPORT, ScriptName.ADMINISTRATION) {

		@Override
		public String getPageContext() {
			return PageContext.B2B_FRONTEND;
		}
	},
	B2B_BOOKING(TimerName.B2B_BOOKING, ScriptName.ADMINISTRATION) {

		@Override
		public String getPageContext() {
			return PageContext.B2B_FRONTEND;
		}
	},
	WORDPRESS_SHOP_START_PAGE(TimerName.WORDPRESS_SHOP_START_PAGE, ScriptName.WORDPRESS_SHOP){
		@Override
		public String getPageContext() {
			return PageContext.WORDPRESS;
		}
	},
	WORDPRESS_SHOP_BLOG(TimerName.WORDPRESS_SHOP_BLOG, ScriptName.WORDPRESS_SHOP){
		@Override
		public String getPageContext() {
			return PageContext.WORDPRESS;
		}
	},
	WORDPRESS_SHOP_ACCESSORIES(TimerName.WORDPRESS_SHOP_ACCESSORIES, ScriptName.WORDPRESS_SHOP){
		@Override
		public String getPageContext() {
			return PageContext.WORDPRESS;
		}
	},
	WORDPRESS_SHOP_CLOTHING(TimerName.WORDPRESS_SHOP_CLOTHING, ScriptName.WORDPRESS_SHOP){
		@Override
		public String getPageContext() {
			return PageContext.WORDPRESS;
		}
	},
	WORDPRESS_SHOP_DECOR(TimerName.WORDPRESS_SHOP_DECOR, ScriptName.WORDPRESS_SHOP){
		@Override
		public String getPageContext() {
			return PageContext.WORDPRESS;
		}
	},
	WORDPRESS_SHOP_HOODIES(TimerName.WORDPRESS_SHOP_HOODIES, ScriptName.WORDPRESS_SHOP){
		@Override
		public String getPageContext() {
			return PageContext.WORDPRESS;
		}
	},
	WORDPRESS_SHOP_MUSIC(TimerName.WORDPRESS_SHOP_MUSIC, ScriptName.WORDPRESS_SHOP){
		@Override
		public String getPageContext() {
			return PageContext.WORDPRESS;
		}
	},
	WORDPRESS_SHOP_TSHIRTS(TimerName.WORDPRESS_SHOP_TSHIRTS, ScriptName.WORDPRESS_SHOP){
		@Override
		public String getPageContext() {
			return PageContext.WORDPRESS;
		}
	},
	WORDPRESS_SHOP_PRODUCT(TimerName.WORDPRESS_SHOP_PRODUCT, ScriptName.WORDPRESS_SHOP){
		@Override
		public String getPageContext() {
			return PageContext.WORDPRESS;
		}
	},
	;

	private final String timerName;
	private final String scriptName;

	DynaTraceTagMetaData(String timerName, String scriptName) {
		this.timerName = timerName;
		this.scriptName = scriptName;
	}

	@Override
	public String toString() {
		return timerName;
	}

	public String getPageContext() {
		return PageContext.CUSTOMER_FRONTEND;
	}


	String getTimerName() {
		return timerName;
	}


	String getScriptName() {
		return scriptName;
	}
}