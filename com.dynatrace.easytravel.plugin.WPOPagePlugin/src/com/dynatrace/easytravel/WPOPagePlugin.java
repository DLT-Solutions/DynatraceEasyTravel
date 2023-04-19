package com.dynatrace.easytravel;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractPagePlugin;

/**
 * This plugin is used to demo Web Performance Optimization Use Cases, e.g: too many JS files, ...
 * @author andreas.grabner
 *
 */
public class WPOPagePlugin extends AbstractPagePlugin {
	
	private static final Logger log = LoggerFactory.make();
	
	private static final String[] jQueryUICSS = new String[] {"ui.accordion", "ui.autocomplete","ui.button","ui.core","ui.datepicker","ui.dialog","ui.progressbar","ui.resizable","ui.selectable","ui.slider","ui.tabs"};
	private static final String[] jQueryUIPlugins = new String[] {"effects.blind", "effects.bounce", "effects.clip", "effects.core", "effects.drop", "effects.explode", "effects.fade", "effects.fold", "effects.highlight", "effects.pulsate", "effects.scale", "effects.shake", "effects.slide", "effects.transfer",
	                                                 "ui.accordion", "ui.autocomplete", "ui.button", "ui.core", "ui.datepicker", "ui.dialog", "ui.draggable", "ui.droppable", "ui.mouse", "ui.position", "ui.progressbar", "ui.resizable", "ui.selectable", "ui.slider", "ui.sortable", "ui.tabs", "ui.widgets"};
	
	// injected by the PluginServlet
	public boolean loadjQueryUI = true;
	public boolean loadOptimized = false;
	public boolean loadMinimized = false;
	public boolean simulate400s = true;
	public boolean enableCache = false; 
	
	// execution result
	public String fullName; 
	
	// used in test1.js
	public String getJavaScriptMessage() {
		return "Hello, I am a very nasty JavaScript injection, but you can turn me off on the " +getName() + " page!";
	}	
	
	// some really nasty JS injection!
	@Override
	public String getHeadInjection() {
		StringBuilder sb = new StringBuilder();
		
		// load jQuery
		if(loadMinimized) 
			sb.append("<script type='text/javascript' src='plugins/").append(getName()).append("/jquery-ui/js/jquery-1.4.4.min.js'></script>");
		else
			sb.append("<script type='text/javascript' src='plugins/").append(getName()).append("/jquery-ui/development-bundle/jquery-1.4.4.js'></script>");
		
		// optionally load jQuery UI
		if (loadjQueryUI) {			
			if(loadOptimized) {
				sb.append("<link type='text/css' href='plugins/").append(getName()).append("/jquery-ui/development-bundle/themes/base/jquery.ui.all.css' rel='stylesheet' />");
				if(loadMinimized)
					sb.append("<script type='text/javascript' src='plugins/").append(getName()).append("/jquery-ui/js/jquery-ui-1.8.10.custom.min.js'></script>");
				else
					sb.append("<script type='text/javascript' src='plugins/").append(getName()).append("/jquery-ui/development-bundle/ui/jquery-ui-1.8.10.custom.js'></script>");
			}
			else {
				for(String css : jQueryUICSS) {
					sb.append("<link type='text/css' href='plugins/").append(getName()).append("/jquery-ui/development-bundle/themes/base/jquery.").append(css).append(".css' rel='stylesheet' />");
				}
				for(String plugin : jQueryUIPlugins) {
					sb.append("<script type='text/javascript' src='plugins/").append(getName()).append("/jquery-ui/development-bundle/ui/jquery.").append(plugin).append(".js'></script>");
				}
			}
		}
		
		// return an invalid URL in case we want to simulate http 400s
		if(simulate400s) {
			sb.append("<script type='text/javascript' src='plugins/").append(getName()).append("/jquery-ui/development-bundle/ui/jquery.ui.myslider.js'></script>");		
			sb.append("<script type='text/javascript' src='plugins/").append(getName()).append("/jquery-ui/development-bundle/ui/jquery.ui.mydialog.js'></script>");
			sb.append("<script type='text/javascript' src='plugins/").append(getName()).append("/jquery-ui/development-bundle/ui/jquery.ui.myalert.js'></script>");
		}
		
		return sb.toString();
	}
	
	// provide a link to some page from inside the plugin
	@Override
	public String getHeader() {
		return "<a href='plugins/" + getName() + "/wpoconfigure.html'>WPO</a>";
	}

	@Override
	public String getFooter() {
		if (loadjQueryUI) {
			return "<script type='text/javascript' src='plugins/" + getName() + "/wpo.js'></script>";
		}
		return null;
	}
	
	@Override
	protected Object doExecute(Map<String, Object> params) {
		
		log.info(getName() + ": Execute with params: " + params);
		log.info("Plugin state: " + this);
		
		
		return getResource("wpoconfigure.html");
	}
		
	@Override
	protected Object doResource(String path) 
	{
		// we cache all resources but not our config html
		if(path.indexOf(".html") < 0) {
			HttpServletResponse response = getResponse();
			if(enableCache) {
				response.addHeader("Cache-Control", "private,public,max-age=5000000");
			} else {
				response.addHeader("Cache-Control", "private,public,max-age=-1");
			}
		}
		
		return super.doResource(path);
	}
	
	public static String someStaticMethod(String argument)
	{
		log.info("Invoked someStaticMethod with argument: " + argument);
		log.warn("You really shouldn't do that!");
		return "There might be a warning in the log";
	}

	@Override
	public String toString() {
		return "WPO Page";
	}
}
