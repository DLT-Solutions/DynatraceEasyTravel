package com.dynatrace.diagnostics.uemload;


public class SpeedIndex {
	
	private final long time;
	
	private SpeedIndex(long time) {
		this.time = time;
	}
	
	public static SpeedIndex create(VisualCompleteTime vt) {
		
		if (vt != null && vt.getTime() > 0) {
			return new SpeedIndex((long)(vt.getTime() * 0.8));
		}
		return null;
	}

	public long getTime() {
		return time;
	}
	
}
