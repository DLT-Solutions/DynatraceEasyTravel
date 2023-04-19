package com.dynatrace.easytravel.launcher.httpd;

import java.io.PrintWriter;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.util.TextUtils;

/**
 * Small helper-class to add rewrite-rules to the generated httpd.conf
 *
 */
public class Rewrite {

	public static void writeModule(PrintWriter writer) {
		if (isCustomContextRoot()) {
			writer.println("LoadModule rewrite_module modules/mod_rewrite.so");
		}
	}

	public static void writeRedirectToCustomContext(PrintWriter writer) {
		if (isCustomContextRoot()) {
			final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
			writer.println("	RewriteEngine On");
			writer.println("	RewriteRule   ^/$  " + TextUtils.appendTrailingSlash(EASYTRAVEL_CONFIG.frontendContextRoot) +
					" [R]");
		}
	}

	private static boolean isCustomContextRoot() {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		return !EASYTRAVEL_CONFIG.frontendContextRoot.trim().equals("/");
	}

}
