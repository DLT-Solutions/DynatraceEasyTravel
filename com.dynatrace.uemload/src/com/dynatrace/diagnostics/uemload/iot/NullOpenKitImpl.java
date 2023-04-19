package com.dynatrace.diagnostics.uemload.iot;

import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.Session;
import com.dynatrace.openkit.api.SessionSurrogateData;
import com.dynatrace.openkit.core.objects.NullSession;

public class NullOpenKitImpl implements OpenKit {

	@Override
	public boolean waitForInitCompletion() {
		return false;
	}

	@Override
	public boolean waitForInitCompletion(long timeoutMillis) {
		return false;
	}

	@Override
	public boolean isInitialized() {
		return false;
	}

	@Override
	public Session createSession(String clientIPAddress) {
		return NullSession.INSTANCE;
	}

	@Override
	public Session createSession(String clientIPAddress, SessionSurrogateData sessionData) {
		return NullSession.INSTANCE;
	}

	@Override
	public void shutdown() {
		//do nothing
	}

	@Override
	public void close() {
		//do nothing
	}

	@Override
	public Session createSession() {
		return NullSession.INSTANCE;
	}

}
