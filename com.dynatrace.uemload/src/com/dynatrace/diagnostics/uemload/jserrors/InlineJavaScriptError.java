package com.dynatrace.diagnostics.uemload.jserrors;

import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.diagnostics.uemload.BrowserType.BrowserFamily;

public class InlineJavaScriptError extends JavaScriptError {


	public InlineJavaScriptError(String currentPageUrl) {

		super(currentPageUrl);

		setCode("438");

		setMessage(BrowserFamily.Firefox, "TypeError: test is undefined");
		setMessage(BrowserFamily.Chrome, "Uncaught TypeError: Cannot read property 'fail'");
		setMessage(BrowserFamily.Opera, "Uncaught TypeError: Cannot read property 'fail' of undefined");
		setMessage(BrowserFamily.IE, "Object doesn't support this property or method");
		setMessage(BrowserFamily.Safari, "TypeError: Result of expression 'test' [undefined] is not an object");

		setMessage(BrowserType.IE_9, "Unable to get value of the property 'fail': object is null or undefined");
		setMessage(BrowserType.IE_10, "Unable to get value of the property 'fail': object is null or undefined");

		setMessage(BrowserType.OPERA_35, "Uncaught TypeError: Cannot read property 'fail'");

		setMessage(BrowserType.SAFARI_IPAD, "TypeError: 'undefined' is not an object (evaluating 'test.fail')");

		setMessage(BrowserType.ANDROID_22, "Uncaught TypeError: Cannot read property 'fail' of undefined");
		setMessage(BrowserType.ANDROID_24, "Uncaught TypeError: Cannot read property 'fail' of undefined");
		setMessage(BrowserType.ANDROID_403, "Uncaught TypeError: Cannot read property 'fail' of undefined");
		setMessageForMobileAndroidDevices("Uncaught TypeError: Cannot read property 'fail' of undefined");
		setMessageForMobileWindowsDevices("Object doesn't support this property or method");
		setMessageForMobileIOSDevices("TypeError: Result of expression 'test' [undefined] is not an object");

		setDefaultMessage("default error");
		setColumn(-1);
		setLine(-1);

		// setUserAction("");
	}

}
