package com.dynatrace.diagnostics.uemload.jserrors;

import com.dynatrace.diagnostics.uemload.BrowserType.BrowserFamily;

public class CurCSSNotDefinedError extends JavaScriptError {

	public CurCSSNotDefinedError(String currentPageUrl) {

		super(currentPageUrl);

		setFile("<domain>/js/jquery-ui-1.8.2.min.js");

		setLine(160);

		setColumn(-1);
		setColumn(BrowserFamily.Chrome, 69);
		setColumn(BrowserFamily.Opera, 69);
		setColumn(BrowserFamily.IE, 287);
		setColumn(BrowserFamily.Firefox, 286);

		setDefaultMessage("TypeError: f.curCSS is not a function");

		setMessage(BrowserFamily.Chrome, "Uncaught TypeError: undefined is not a function");
		setMessage(BrowserFamily.Opera, "Uncaught TypeError: undefined is not a function");
		setMessage(BrowserFamily.Firefox, "TypeError: f.curCSS is not a function");
		setMessage(BrowserFamily.IE, "Object doesn't support property or method 'curCSS'");
		setMessage(BrowserFamily.Safari, "TypeError: 'undefined' is not a function (evaluating 'f.curCSS(c,a)')");

		setMessageForMobileIOSDevices("TypeError: 'undefined' is not a function (evaluating 'f.curCSS(c,a)')");
		setMessageForMobileWindowsDevices("Object doesn't support property or method 'curCSS'");
		setMessageForMobileAndroidDevices("Uncaught TypeError: undefined is not a function");

		// error message 10438 (see https://github.com/errorception/ie-error-languages/blob/master/error-strings/en-US.txt)
		setCode("10438");
		setLanguageSpecificMessage(ENGLISH, "Object doesn't support property or method 'curCSS'");
		setLanguageSpecificMessage(GERMAN, "Das Objekt unterst\u00fctzt die Eigenschaft oder Methode \"curCSS\" nicht");
		setLanguageSpecificMessage(FRENCH, "L\u2019objet ne g\u00e8re pas la propri\u00e9t\u00e9 ou la m\u00e9thode \u00ab curCSS \u00bb");
		setLanguageSpecificMessage(ITALIAN, "L'oggetto non supporta la propriet\u00e0 o il metodo 'curCSS'");
		setLanguageSpecificMessage(SPANISH, "El objeto no acepta la propiedad o el m\u00e9todo 'curCSS'");

		setStackTrace(BrowserFamily.Chrome,
			"TypeError: undefined is not a function\n"
			    + "    at q (<domain>/js/jquery-ui-1.8.2.min.js:160:291)\n"
			    + "    at Object.f.fx.step.(anonymous function) [as color] (<domain>/js/jquery-ui-1.8.2.min.js:162:472)\n"
			    + "    at Object.Tween.propHooks._default.set (<domain>/js/jquery-1.8.1.js:8825:33)\n"
			    + "    at Tween.run (<domain>/js/jquery-1.8.1.js:8795:29)\n"
			    + "    at Animation.tick (<domain>/js/jquery-1.8.1.js:8491:31)\n"
			    + "    at Function.jQuery.fx.timer (<domain>/js/jquery-1.8.1.js:9032:7)\n"
			    + "    at Animation (<domain>/js/jquery-1.8.1.js:8555:12)\n"
			    + "    at HTMLLabelElement.doAnimation (<domain>/js/jquery-1.8.1.js:8871:16)\n"
			    + "    at Function.jQuery.extend.dequeue (<domain>/js/jquery-1.8.1.js:1894:7)\n"
			    + "    at HTMLLabelElement.<anonymous> (<domain>/js/jquery-1.8.1.js:1937:13)"
		);

		setStackTrace(BrowserFamily.Firefox,
			"q@<domain>/js/jquery-ui-1.8.2.min.js:160:287\n"
			 	+ "f.fx.step[a]@<domain>/js/jquery-ui-1.8.2.min.js:162:464\n"
				+ "Tween.propHooks._default.set@<domain>/js/jquery-1.8.1.js:8825:5\n"
			 	+ "Tween.prototype.run@<domain>/js/jquery-1.8.1.js:8795:4\n"
				+ "Animation/tick@<domain>/js/jquery-1.8.1.js:8491:5\n"
			 	+ "jQuery.fx.timer@<domain>/js/jquery-1.8.1.js:9032:1\n"
				+ "Animation@<domain>/js/jquery-1.8.1.js:8559:4\n"
			 	+ ".animate/doAnimation@<domain>/js/jquery-1.8.1.js:8871:9\n"
				+ ".dequeue@<domain>/js/jquery-1.8.1.js:1894:4\n"
			 	+ ".queue/@<domain>/js/jquery-1.8.1.js:1937:6\n"
				+ ".each@<domain>/js/jquery-1.8.1.js:611:1\n"
			 	+ "jQuery.prototype.each@<domain>/js/jquery-1.8.1.js:241:3\n"
				+ ".queue@<domain>/js/jquery-1.8.1.js:1930:1\n"
			 	+ ".animate@<domain>/js/jquery-1.8.1.js:8881:1\n"
				+ "@<domain>/:30:179\n"
			 	+ "jQuery.event.dispatch@<domain>/js/jquery-1.8.1.js:3060:1\n"
				+ "jQuery.event.add/eventHandle@<domain>/js/jquery-1.8.1.js:2681:1"
		);

		addUserAction("C|Trip Destination");
		addUserAction("C|Date From");
		addUserAction("C|Date To");
	}

}
