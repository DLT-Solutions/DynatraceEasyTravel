package com.dynatrace.diagnostics.uemload;

import java.io.IOException;

public interface UEMOnLoadCallback extends UEMLoadCallback{
	public void run(UEMLoadCallback callback) throws IOException;
}
