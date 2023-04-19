package com.dynatrace.easytravel.jquery;

import com.dynatrace.easytravel.spring.AbstractPagePlugin;
import com.dynatrace.easytravel.spring.PluginConstants;


public abstract class JQueryEffects extends AbstractPagePlugin {

	private HtmlHeaderBuilder contentBuilder;
	private HtmlHeaderBuilder jQueryPathBuilder;

	@Override
	public String getHeadInjection() {
		contentBuilder = new HtmlHeaderBuilder(getName());
		addContent();
		return contentBuilder.generate();
	}

	protected abstract void addContent();

	@Override
	public String getFooter() {
		return 
			new HtmlHeaderBuilder(getName()).
				appendScript("/jQueryEffects.js").
					generate();
	}


	@Override
	public Object doExecute(String location, Object... context) {
		if (PluginConstants.FRONTEND_JQUERY_PATHS.equals(location)) {
			return getJQueryPaths();
		}
		return super.doExecute(location, context);
	}

	protected String getJQueryPaths() {
		jQueryPathBuilder = new HtmlHeaderBuilder(getName());
		addJQueryPaths();
		return jQueryPathBuilder.generate();
	}

	
	protected abstract void addJQueryPaths();

	protected HtmlHeaderBuilder getjQueryPathBuilder() {
		return jQueryPathBuilder;
	}
	
	protected HtmlHeaderBuilder getContentBuilder() {
		return contentBuilder;
	}	
	
	
	
}
