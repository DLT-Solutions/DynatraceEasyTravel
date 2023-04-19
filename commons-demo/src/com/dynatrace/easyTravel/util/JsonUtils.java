package com.dynatrace.easytravel.util;

public class JsonUtils {
	/*
	 * 
	 */
	public static String addJsonArrayBrackets(String str){
		if(!str.startsWith("[") && !str.endsWith("]")){
			return "["+str+"]";
		}
		return str;
	}
}
