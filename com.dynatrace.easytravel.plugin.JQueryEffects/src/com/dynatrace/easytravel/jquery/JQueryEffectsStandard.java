package com.dynatrace.easytravel.jquery;

public class JQueryEffectsStandard extends JQueryEffects /* implements Filter */{

	private static final String[] jQueryUICSS = new String[] { "ui.accordion", "ui.autocomplete", "ui.button", "ui.core",
			"ui.datepicker", "ui.dialog", "ui.progressbar", "ui.resizable", "ui.selectable", "ui.slider", "ui.tabs" };
	private static final String[] jQueryUIPlugins = new String[] { "effects.blind", "effects.bounce", "effects.clip",
			"effects.core", "effects.drop", "effects.explode", "effects.fade", "effects.fold", "effects.highlight",
			"effects.pulsate", "effects.scale", "effects.shake", "effects.slide", "effects.transfer",
														"ui.accordion", "ui.autocomplete", "ui.button", "ui.core",
			"ui.datepicker", "ui.dialog", "ui.draggable", "ui.droppable", "ui.mouse", "ui.position", "ui.progressbar",
			"ui.resizable", "ui.selectable", "ui.slider", "ui.sortable", "ui.tabs", "ui.widget" };

	@Override
	protected void addContent() {
		addJQueryCss();
		addJQueryPlugins();
	}
	
	@Override
	protected void addJQueryPaths() {
		getjQueryPathBuilder().
			appendScript("/jquery-ui/development-bundle/jquery-1.6.1.js").
			appendScript("/jquery-ui/development-bundle/ui/jquery-ui-1.8.14.custom.js");
	}
	
	
	private void addJQueryCss() {
		for (String css : jQueryUICSS) {
			String linkPath = "/jquery-ui/development-bundle/themes/base/jquery." + css + ".css";
			getContentBuilder().appendLink(linkPath);
		}
	}

	private void addJQueryPlugins() {
		for (String plugin : jQueryUIPlugins) {
			String scriptPath = "/jquery-ui/development-bundle/ui/jquery." + plugin + ".js";
			getContentBuilder().appendScript(scriptPath);
		}
	}

}
