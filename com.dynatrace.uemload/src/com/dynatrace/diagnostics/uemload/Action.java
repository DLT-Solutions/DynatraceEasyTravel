package com.dynatrace.diagnostics.uemload;

import java.io.IOException;



public abstract class Action {

	public abstract void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception;

	public void cont(UEMLoadCallback continuation) throws IOException {
		if(continuation != null) {
			continuation.run();
		}
	}

}
