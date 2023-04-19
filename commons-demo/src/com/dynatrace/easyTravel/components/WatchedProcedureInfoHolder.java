package com.dynatrace.easytravel.components;

public class WatchedProcedureInfoHolder {
	String uri;
	String[] params;
	public WatchedProcedureInfoHolder(String uri, String[] params){
		this.uri = uri;
		this.params = params;	
	}
	
	public String getURI(){
		return uri;
	}
	
	public String[] getParams(){
		return params;
	}
}
