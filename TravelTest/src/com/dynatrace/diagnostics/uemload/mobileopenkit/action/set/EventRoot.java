package com.dynatrace.diagnostics.uemload.mobileopenkit.action.set;

interface EventRoot<A> {
	A getAction();
	String getName();
	void setVerification(Runnable verification);
}
