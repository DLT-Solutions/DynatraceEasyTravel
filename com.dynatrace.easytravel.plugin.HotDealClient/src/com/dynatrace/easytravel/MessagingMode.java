package com.dynatrace.easytravel;


public enum MessagingMode {
	RMI("RMI"), JMS("JMS");

	private String name;

	private MessagingMode(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
