package com.dynatrace.diagnostics.uemload.mobileopenkit.crash;

public class Crash {
	public final int minOsVersion;
	public final String name;
	public final String file;
	public final int signalNumber;
	public final String reason;
	private String stackTrace;

	public Crash(int minOsVersion, String name, String file, int signalNumber, String os) {
		this.minOsVersion = minOsVersion;
		this.name = name;
		this.file = file;
		this.signalNumber = signalNumber;
		reason = os + " Crash Reason";
	}

	public Crash(String name, String file, int signalNumber, String os) {
		this(0, name, file, signalNumber, os);
	}

	public String getName() {
		return name;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}
}
