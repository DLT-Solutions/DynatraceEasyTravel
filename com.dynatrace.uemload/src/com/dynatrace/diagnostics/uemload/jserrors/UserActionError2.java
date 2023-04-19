package com.dynatrace.diagnostics.uemload.jserrors;

import com.dynatrace.diagnostics.uemload.BrowserType.BrowserFamily;


/**
 * Some Error to add to a user action in easyTravel
 * 
 * @author cwat-moehler
 */
public class UserActionError2 extends JavaScriptError {

	public UserActionError2(String currentPageUrl) {

		super(currentPageUrl);

		setFile("<domain>/blog/generator-information/");
		setLine(101);
		setColumn(199);

		setDefaultMessage("$(...).cycle is not a function");
		setCode("0");
		
		String stackTrace = "@http://norwall.com/javascript/common.js?91d5d:101:199\r\n"
			  + ".ready@<domain>/javascript/jquery.js?91d5d:2:5394\n"
			  + "L@<domain>/javascript/jquery.js?91d5d:3:2215\n"
			  + "nrWrapper@<domain>/blog/generator-information/locating-standby-home-generator-installation/:4:11956\n"
			  + "Gb/a.rxewrapper@<domain>/ruxitagentjs_2bjnr_1006301540010919.js:42:82";
		
		setStackTrace(BrowserFamily.Chrome, stackTrace);
		setStackTrace(BrowserFamily.Opera, stackTrace);
		setStackTrace(BrowserFamily.Firefox, stackTrace);
	}
}


