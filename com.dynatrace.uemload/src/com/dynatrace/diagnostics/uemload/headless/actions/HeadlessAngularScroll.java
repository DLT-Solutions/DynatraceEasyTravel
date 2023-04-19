package com.dynatrace.diagnostics.uemload.headless.actions;

import org.openqa.selenium.JavascriptExecutor;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.ActionExecutor;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.headless.HeadlessActionExecutor;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * @author Rafal.Psciuk
 *
 */
public class HeadlessAngularScroll extends Action {
	private static final Logger LOGGER = LoggerFactory.make();

	private final int scrollSize;
	private final int stepSize;


	public HeadlessAngularScroll(int scrollSize, int stepSize) {
		this.scrollSize = scrollSize;
		if (stepSize != 0) {
			this.stepSize = stepSize;	
		} else {
			LOGGER.warn("Invalid step value " + stepSize);
			this.stepSize = 1;
		}
	}

	@Override
	public void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception {
		scroll(browser);
	}

	private void scroll(ActionExecutor browser) {
		HeadlessActionExecutor exec = (HeadlessActionExecutor) browser;
		JavascriptExecutor javascriptExecutor = exec.getDriver();
		
		int steps = scrollSize/stepSize;
		int scrollto = (stepSize > 0 ? 0 : scrollSize); 
		for(int i=0; i<Math.abs(steps); i++) {
			scrollto += stepSize;
			javascriptExecutor.executeScript(String.format("document.body.scrollTop = %d", scrollto ));
			
		}
	}
}
