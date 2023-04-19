package com.dynatrace.easytravel;

import com.dynatrace.easytravel.spring.AbstractPagePlugin;

/**
 * This plugin adds third party social media content to all easyTravel pages.
 * The content is added to the footer.
 *
 * @author peter.lang
 *
 */
public class ThirdPartyContent extends AbstractPagePlugin {

	// injected by the PluginServlet
	public boolean showFooter = true;

	@Override
	public String getFooter() {
		if (showFooter) {

			String facebookJSCode = "<div id=\"fb-root\"></div>"+
				"<script>(function(d, s, id) {                             " +
				"  var js, fjs = d.getElementsByTagName(s)[0];             " +
				"  if (d.getElementById(id)) {return;}                     " +
				"  js = d.createElement(s); js.id = id;                    "
				+ "js.onload = libraryLoaded;" +
				"  js.src = \"//connect.facebook.net/en_US/all.js#xfbml=1\"; " +
				"  fjs.parentNode.insertBefore(js, fjs);                   " +
				"}(document, 'script', 'facebook-jssdk'));</script>";

			String includeFacebook = "<div class=\"fb-like\" data-href=\"www.dynatrace.com\" data-send=\"false\" data-width=\"300\" data-show-faces=\"false\" data-font=\"arial\" style=\"position: static !important\"></div>";


			String includeTwitter = "<div class=\"twitter-facebook\" style=\"float:left;margin-right:5px\"><a href=\"https://twitter.com/dynatrace\" " +
					"class=\"twitter-follow-button\">Follow @Dynatrace</a>" +
					"<script type=\"text/javascript\" src=\"//platform.twitter.com/widgets.js\" onload='libraryLoaded()'></script></div>";

			String includeLinkedIn = "<div class=\"twitter-facebook\" style=\"float:left;margin-right:5px\"><script src=\"https://platform.linkedin.com/in.js\" type=\"text/javascript\" onload='libraryLoaded()'>lang: en_US</script><script type=\"IN/Share\" data-counter=\"right\"></script></div>";

			String includePinterest = "<div class=\"twitter-facebook\" style=\"float:left;margin-right:5px\"><a href=\"https://www.pinterest.com/pin/create/button/\" data-pin-do=\"buttonBookmark\" ><img src=\"https://assets.pinterest.com/images/pidgets/pinit_fg_en_rect_gray_20.png\" /></a><script type=\"text/javascript\" async src=\"https://assets.pinterest.com/js/pinit.js\" onload='libraryLoaded()'></script></div>";

			return "<div style=\"padding:5px\">" + facebookJSCode + includeLinkedIn + includeTwitter + includePinterest + includeFacebook + "</div>";
		}
		return null;
	}

	@Override
	public String getHeadInjection() {
		return "<script type='text/javascript' src='js/usertimings.js'></script>"
				+ "<script type='text/javascript'>window.librariesToLoad = 5;</script>"
				+ "<script type=\"text/javascript\" src=\"https://apis.google.com/js/plusone.js\" onload=\"libraryLoaded()\"></script>";


	}

	@Override
	public String toString() {
		return "ThirdPartyContent [showFooter=" + showFooter + "]";
	}
}
