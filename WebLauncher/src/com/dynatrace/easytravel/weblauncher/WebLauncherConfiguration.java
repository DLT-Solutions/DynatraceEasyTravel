package com.dynatrace.easytravel.weblauncher;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.client.WebClient;


public class WebLauncherConfiguration implements ApplicationConfiguration {

	@Override
	public void configure(Application application) {
		Map<String, String> properties = new HashMap<String, String>();

		properties.put(WebClient.PAGE_TITLE, "easyTravel Configuration Web-UI");

		properties.put(WebClient.HEAD_HTML,
				"<link href=\"img/favicon_orange_plane.ico\" rel=\"shortcut icon\" />\n" +
				"<link href=\"img/favicon_orange_plane.png\" rel=\"apple-touch-icon\" />" + 
				"<script type=\"text/javascript\">(function(){try{var e=navigator.userAgent;if(e.indexOf(\"like Gecko\")===-1&&e.indexOf(\"Gecko/\")!==-1){if(!window.controllers){window.controllers={}}if(!navigator.product){navigator.product=\"Gecko\"}}}catch(t){}})()</script>");
		
		//properties.put(WebClient.FAVICON, "img/favicon_orange_plane.ico");
		//application.addResource(arg0)

		properties.put(WebClient.BODY_HTML, "<div style=\"background-image: url(../img/header.png); " +
		"background-repeat: no-repeat; " +
		"background-color:orange; " +
		"width:860px;" +
		"height:59px;" +
		"\">" +
		"<div style=\"" +
			"float:left;" +
			"margin-top:32px;" +
			"margin-left:155px;" +
			"height:27px;" +
			"color:white;\">Welcome to the easyTravel Configuration Web-UI...</div>" +
		"</div>");

		application.addEntryPoint("/main", WebLauncher.class, properties);

		// breaks lots of UI stuff: application.setOperationMode(OperationMode.SWT_COMPATIBILITY);

		/*ExitConfirmation confirmation = RWT.getClient().getService( ExitConfirmation.class );
		confirmation.setMessage( MessageConstants.SHUTDOWN_WEBLAUNCHER );*/
	}
}
