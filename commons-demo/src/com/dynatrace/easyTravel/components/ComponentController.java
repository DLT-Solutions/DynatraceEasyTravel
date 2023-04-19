package com.dynatrace.easytravel.components;

import java.util.List;

public class ComponentController {
	private static volatile ComponentRefreshThread refreshThread;
	
	private String type;
	
	public ComponentController(String type){
		this.type = type;
	}
	
	public List<String> getEnabledComponents() {
		return refreshEnabledComponents();
	}

	public static void stopRefreshThread() {
		if(refreshThread != null) {
			synchronized (ComponentController.class) {
				if(refreshThread != null) {
					refreshThread.shouldStop();

					refreshThread = null;
				}
			}
		}
	}
	
	private List<String> refreshEnabledComponents() {
		if(refreshThread == null) {
			synchronized (ComponentController.class) {
				if(refreshThread == null) {
					refreshThread = new ComponentRefreshThread(type);
					refreshThread.start();
				}
			}
		}
		List<String> enabledComponents = refreshThread.getEnabledComponents();
		
		return enabledComponents;
	}

}
