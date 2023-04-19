package com.dynatrace.easytravel.jquery;

public class JQueryEffectsCloudflare extends JQueryEffects /* implements Filter */{

	@Override
	protected void addContent() {}

	@Override
	protected void addJQueryPaths() {
		getjQueryPathBuilder().
				appendRemoteScript("//cdnjs.cloudflare.com/ajax/libs/jquery/1.8.1/jquery.js").
				appendRemoteScript("//cdnjs.cloudflare.com/ajax/libs/jquery/1.8.1/jquery.min.js");
	}

	// make it public - this is simply for JUnit test purposes
	public String getJQueryPaths() {
		return super.getJQueryPaths();
	}
	
	// make it public - this is simply for JUnit test purposes
	public String WgetJQueryPaths() {
		return super.getJQueryPaths();
	}
}
