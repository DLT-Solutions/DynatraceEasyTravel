package com.dynatrace.easytravel.page;

import java.util.Date;
import java.util.Map;
import java.util.Random;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractPagePlugin;

/**
 * This plugin is used for showing the cluster node in the page,
 * in a cluster / load balancing setup.
 * @author philipp.grasboeck
 *
 */
public class DummyPagePlugin extends AbstractPagePlugin {

	private static final Logger log = LoggerFactory.make();

	// to demonstrate dynamic texts
	private final Random random = new Random();

	// injected by the PluginServlet
	public String firstName;
	public String lastName;
	public int age = 50;
	public boolean scriptInjection = true;
	public boolean showFooter = true;

	// execution result
	public String fullName;

	// used in test1.js
	public String getJavaScriptMessage() {
		return "Hello, I am a very nasty JavaScript injection, but you can turn me off on the " +getName() + " page!";
	}

	// some really nasty JS injection!
	@Override
	public String getHeadInjection() {
		if (scriptInjection){
			return "<script type='text/javascript' src='plugins/" + getName() + "/test1.js'></script>";
		}
		return null;
	}

	// provide a link to some page from inside the plugin
	@Override
	public String getHeader() {
		return "<a href='plugins/" + getName() + "/test1.html'>DummyPage</a>";
	}

	@Override
	public String getFooter() {
		if (showFooter) {
			return "DemoFooter " + random.nextInt(30);
		}
		return null;
	}

	@Override
	protected Object doExecute(Map<String, Object> params) {
		log.info(getName() + ": Execute with params: " + params);
		log.info("Plugin state: " + this);

		// let's compute something
		fullName = firstName + " " + lastName;

		if (params.containsKey("downloadFile"))
		{
			return getResource("result.txt");
		}
		if (params.containsKey("showResultPage"))
		{
			return getResource("result.html");
		}

		return getResource("test1.html");
//		return "DummyPagePlugin/test1.html"; // plugin chaining
	}

	@Override
	protected Object doResource(String path)
	{
		if (path.equals("test2.js")) // provide this as an inline resource
		{
			return getContent("alert ('I am an inline resource. Time is: " + new Date() + "')");
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
		return "DummyPagePlugin [firstName=" + firstName + ", lastName="
				+ lastName + ", age=" + age + ", scriptInjection="
				+ scriptInjection + ", showFooter=" + showFooter
				+ ", fullName=" + fullName + "]";
	}
}
