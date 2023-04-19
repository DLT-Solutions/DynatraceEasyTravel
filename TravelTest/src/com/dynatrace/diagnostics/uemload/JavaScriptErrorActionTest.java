package com.dynatrace.diagnostics.uemload;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 *
 * @author cwat-slexow
 *
 */
public class JavaScriptErrorActionTest {


	@Test
	public void testJsErrorActionGeneration() {

		JavaScriptErrorAction errorAction = new JavaScriptErrorAction();
		String errorMessage = "errorMessage";
		int depth = 1;
		int id = 5;
		int tmpId = id;
		long time = 100000000;

		long errorActionStartTime = time;
		long errorActionEndTime = time + 10;

		errorAction.setDepth(depth);
		errorAction.setErrorMessage(errorMessage);
		errorAction.setStartTime(time);

		String compareStr = depth + "|" + tmpId + "|" + errorMessage + "|" + "_error_" + "|" + "-" + "|" + errorActionStartTime + "|" + errorActionEndTime + "|" + "-1";

		String originalStr = errorAction.toString(id);
		assertTrue("JavascriptErrorAction not equal.", (compareStr.compareTo(originalStr) == 0));
	}


	@Test
	public void testStaticJavaScriptErrorActionGeneration() {
		JavaScriptErrorAction action = JavaScriptErrorActionHelper.generateRandomJavascriptAction("http://www.ruxit.com/test.html", BrowserType.NONE);
		assertNotNull(action);
		assertTrue(action.toString(1).contains("_error_"));
	}

}
