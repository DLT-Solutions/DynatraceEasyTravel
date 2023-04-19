package com.dynatrace.easytravel.webfonts;

public class WebFontGoogle extends WebFont {

	// load Google fonts from the Web
	static final String WEB_FONT_GOOGLE_SCRIPT = "<script src=\"//ajax.googleapis.com/ajax/libs/webfont/1.4.7/webfont.js\"></script>" +
										"<script>WebFont.load({google: {families: ['Pacifico']}});</script>";

	@Override
	protected String getWebFontScript() {
		return WEB_FONT_GOOGLE_SCRIPT;
	}

}
