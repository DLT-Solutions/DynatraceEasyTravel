package com.dynatrace.easytravel.launcher.httpd;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.util.TextUtils;

public final class ExtFilterDefineConfig {

	private ExtFilterDefineConfig() {
	}

	private static final OperatingSystem USED_OS = OperatingSystem.pickUp();

	public static final String JAVA_EXE = setupOSspecificJava();
	public static final String EASYTRAVEL_JRE = adjustPath(Directories.getEasytravelJRE().getAbsolutePath() + BaseConstants.BACK_SLASH) ;
	public static final String PLUGINS_SHARED_DIR = adjustPath(Directories.getPluginsSharedDir().getAbsolutePath() + BaseConstants.BACK_SLASH);
	public static final String SLOW_DOWN_PLUGIN = TextUtils.merge("com.dynatrace.easytravel.plugin.{0}.jar", BaseConstants.Plugins.SLOW_APACHE_WEBSERVER.toLowerCase());
	public static final String APACHE_WAIT_TIME = EasyTravelConfig.read().apacheWebServerWaitTime;

	// Create command to execute for slowed down request
	public static final String CMD = TextUtils.merge("cmd=\"{0}{1} {2}{3} {4}\"", EASYTRAVEL_JRE, JAVA_EXE, PLUGINS_SHARED_DIR, SLOW_DOWN_PLUGIN, APACHE_WAIT_TIME);

	public static final String DIRECTIVE = "ExtFilterDefine" + BaseConstants.WS;
	public static final String FILTER_NAME = "slowDownInput" + BaseConstants.WS;
	public static final String MODE_INPUT = "mode=input" + BaseConstants.WS;
	public static final String MODE_OUTPUT = "mode=output" + BaseConstants.WS;


	// Define what to slow down if parameter is set to "/" all requests are slowed down
	private static final String SLOW_DOWN_ALL_REQUESTS = "/";
	public static final String REQUEST_LOCATION_SLOWDOWN = TextUtils.merge("<Location {0}>", SLOW_DOWN_ALL_REQUESTS);
	public static final String END_LOCATION_XML_TAG = "</Location>";
	public static final String SET_INPUT_FILTER = "\tSetInputFilter" + BaseConstants.WS;


	private static String setupOSspecificJava() {
		String java = "";

		if (USED_OS == OperatingSystem.WINDOWS) {
			java = "javaw.exe -jar";
		} else if (USED_OS == OperatingSystem.LINUX) {
			java = "java -jar";
		}

		return java;
	}

	public static String adjustPath(String path) {
		String adjustedPath = "";

		if (USED_OS == OperatingSystem.WINDOWS) {
			adjustedPath = path.replace(BaseConstants.BACK_SLASH, BaseConstants.FSLASH).replace(BaseConstants.WS, BaseConstants.BACK_SLASH + BaseConstants.WS);
		} else if (USED_OS == OperatingSystem.LINUX) {
			adjustedPath = path.replace(BaseConstants.BACK_SLASH, BaseConstants.FSLASH);
		}
		return adjustedPath;
	}
}

