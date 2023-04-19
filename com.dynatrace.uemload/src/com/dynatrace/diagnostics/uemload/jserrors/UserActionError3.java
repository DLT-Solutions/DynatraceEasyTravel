package com.dynatrace.diagnostics.uemload.jserrors;

import com.dynatrace.diagnostics.uemload.BrowserType.BrowserFamily;


/**
 * Some Error to add to a user action in easyTravel
 * 
 * @author cwat-moehler
 */
public class UserActionError3 extends JavaScriptError {

	public UserActionError3(String currentPageUrl) {

		super(currentPageUrl);

		setFile("<domain>/javascript/nexgen/kendoui/assignmentDetails.js?t=.1s");
		setLine(346);
		setColumn(31);
		

		setDefaultMessage("Cannot read property 'attr' of undefined");
		setCode("0");	 
		
		 
		String stackTraceChrome  = "TypeError: Cannot read property 'attr' of undefined\r\n"
			 + "   at HTMLButtonElement.<anonymous> (<domain>/javascript/nexgen/kendoui/assignmentDetails.js?t=.1:346:31)\n"
			 + "   at HTMLDocument.m.event.dispatch (<domain>/javascript/nexgen/jQuery/jquery.min.1.11.js?t=.1:3:8436)\n"
			 + "   at HTMLDocument.r.handle (<domain>/javascript/nexgen/jQuery/jquery.min.1.11.js?t=.1:3:5146)\n"
			 + "   at Gb.a.rxewrapper.a.rxewrapper (<domain>/ruxitagentjs_2bnr_1006301540010919.js:42:84)\n";
						 
		setStackTrace(BrowserFamily.Chrome, stackTraceChrome);
		setStackTrace(BrowserFamily.Opera, stackTraceChrome);
		setStackTrace(BrowserFamily.Firefox, stackTraceChrome);
		
	}

}

	 
