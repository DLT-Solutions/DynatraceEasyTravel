/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: JavascriptChangeDetectionWithSlowBrowserCode.java
 * @date: 18.09.2014
 * @author: cwat-dladenha
 */
package com.dynatrace.easytravel;


/**
 *
 * @author cwat-dladenha
 */
public class JavascriptChangeDetectionWithSlowBrowser extends JavascriptChangeDetection {

	// see http://stackoverflow.com/questions/9639703/ie8-javascript-very-slow-to-load-large-list-of-options-in-select-element
	private final String FILE_CONTENT =

		"$(document).ready(function () {" +

			"var locations = ['Linz', 'Boston', 'Gdansk', 'Detroit', 'Hawaii', 'Newcastle', 'Stockholm', 'Vienna', 'Berlin', 'Madrid', 'Barcelona', 'Zurich', 'Budapest', 'Tahiti', 'Bali', 'Madagascar', 'Florida', 'San Francisco', 'Porto', 'Dijon' ];" +

			"var o = document.getElementById('destinations');" +
			"var form = document.createElement('form');" +
			"var select = document.createElement('select');" +
			"o.appendChild(form);" +
			"form.appendChild(select);" +
			"for (var i = 0; i < 6000; i++) {" +
				"var option = document.createElement('option');" +
				"option.text = locations[i % locations.length];" +
				"option.value = option.text;" +
				"select.add(option, select.options[null]);" +
			"}" +
		"});";

	@Override
	protected String getFileContent() {
		return FILE_CONTENT;
	}

}
