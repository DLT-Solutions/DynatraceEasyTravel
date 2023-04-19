package com.dynatrace.easytravel.jquery;



public class JQueryEffectsOptimized extends JQueryEffects {

	@Override
	protected void addContent() {}

	@Override
	protected void addJQueryPaths() {
		getjQueryPathBuilder().
				appendScript("/jquery-ui/js/jquery-1.6.1.min.js").
				appendScript("/jquery-ui/js/jquery-ui-1.8.14.custom.min.js").
				appendLink("/jquery-ui/development-bundle/themes/base/jquery.ui.all.css");
	}


}
