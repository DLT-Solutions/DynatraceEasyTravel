package com.dynatrace.easytravel.launcher.baseload;


public class BaseLoadUtil {

	public static int getLoadNumberPerMinute(int value, double ratio) {
		return (int) Math.ceil(value * ratio);
	}

	public static int getRelativeLoadPerMinute(int destinationLoad, double ratio) {
		return (int) Math.ceil(destinationLoad / ratio);
	}
}
