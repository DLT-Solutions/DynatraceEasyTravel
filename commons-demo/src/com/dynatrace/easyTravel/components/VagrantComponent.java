package com.dynatrace.easytravel.components;

public class VagrantComponent extends AbstractComponent {
	private String type;
	
	public VagrantComponent(String ip, String[] params) {
		super(ip);
		setupComponent(params);
	}

	@Override
	public void setupComponent(String[] params) {
		this.type = params[1];
	}

	@Override
	public String getType() {
		return this.type;
	}

}
