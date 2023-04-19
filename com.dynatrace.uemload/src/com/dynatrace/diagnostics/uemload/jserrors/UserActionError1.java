package com.dynatrace.diagnostics.uemload.jserrors;

import com.dynatrace.diagnostics.uemload.BrowserType.BrowserFamily;


/**
 * Some Error to add to a user action in easyTravel
 * 
 * @author cwat-moehler
 */
public class UserActionError1 extends JavaScriptError {

	public UserActionError1(String currentPageUrl) {

		super(currentPageUrl);

		setFile("<domain>/fec/besteapps.ruxit.com/3.9.5/hook.js");
		setLine(33);
		setColumn(69);

		setDefaultMessage("Cannot set property 'w' of undefined");
		setCode("0");	 
		
		String stackTrace =  "TypeError: Cannot set property 'w' of undefined\r\n"
		 		+ "    at d (<domain>/assets/bundles/flot.js?version=201502201044:3:2608)\n"
		 		+ "    at J.event.dispatch (<domain>/assets/bundles/core.js?version=201502201044:2:15778)\n"
		 		+ "    at g.handle.h (<domain>/assets/bundles/core.js?version=201502201044:2:11700)\n"
		 		+ "    at nrWrapper (<domain>/dashboard/:33:11989)\n"
		 		+ "    at Gb.a.rxewrapper.a.rxewrapper (<domain>/ruxitagentjs_2bnr_1006301540010919.js:42:84)\n";
		
		setStackTrace(BrowserFamily.Chrome, stackTrace);
		setStackTrace(BrowserFamily.Opera, stackTrace);
		setStackTrace(BrowserFamily.Firefox, stackTrace);
	}

}

