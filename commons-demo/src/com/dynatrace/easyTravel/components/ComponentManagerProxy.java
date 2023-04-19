package com.dynatrace.easytravel.components;

public interface ComponentManagerProxy {
	
	public void setComponent(String ip, String[] params);
	
	public void removeComponent(String ip);
	
	public String[] getComponentsIPList(String type);
}
