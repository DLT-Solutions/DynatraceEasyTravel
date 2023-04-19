package com.dynatrace.easytravel.components;

public abstract class AbstractComponent {
	String uri;
	
	public AbstractComponent(String uri){
		this.uri = uri;
	}
	
	abstract public void setupComponent(String[] params);
	
	abstract public String getType();
	
	public String getURI(){
		return this.uri;
	};
}
