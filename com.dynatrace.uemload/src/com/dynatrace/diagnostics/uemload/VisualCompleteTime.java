package com.dynatrace.diagnostics.uemload;

import com.dynatrace.diagnostics.uemload.perceivedrendertime.PerceivedRenderTime;

public class VisualCompleteTime {

	private enum Trigger {
		
		COMPLETED("c"),
		FORCED("f"),
		USER("u");
		
		private final String trigger;
		
		Trigger(String trigger) {
			this.trigger = trigger;
		}
		
		private String getTrigger() {
			return trigger;
		}
	}
	
	private final long time;
	private final Trigger trigger;
	
	private VisualCompleteTime(long time, Trigger trigger) {
		this.time = time;
		this.trigger = trigger;
	}
	
	public static VisualCompleteTime create(PerceivedRenderTime prt) {
		return prt != null && prt.getValue() > 0 ? new VisualCompleteTime(prt.getValue(), Trigger.COMPLETED) : null;
	}
	
	public long getTime() {
		return time;
	}

	public String getValue() {
		return time + "|" + trigger.getTrigger();
	}

}
