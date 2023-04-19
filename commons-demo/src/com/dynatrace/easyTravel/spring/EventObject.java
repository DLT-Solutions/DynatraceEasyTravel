package com.dynatrace.easytravel.spring;

import java.util.HashMap;
import java.util.Map;

/**
 * This class has the fields needed to send an event notification.
 * After filling the fields it should be serialized to JSON and attached as body in a POST request.
 *
 * @author tomasz.wieremjewicz
 * @date 20 lis 2017
 *
 */
public class EventObject {
	public static final String EVENT_TYPE_CUSTOM_DEPLOYMENT = "CUSTOM_DEPLOYMENT";
	public static final String EVENT_TYPE_CUSTOM_ANNOTATION = "CUSTOM_ANNOTATION";

	public String eventType; // NOSONAR - public on purpose
	public AttachRules attachRules; // NOSONAR - public on purpose
	public String deploymentName; // NOSONAR - public on purpose
	public String deploymentVersion; // NOSONAR - public on purpose
	public String source; // NOSONAR - public on purpose
	public Map<String, String> customProperties; // NOSONAR - public on purpose
	public String start; // NOSONAR - public on purpose
	public String end; // NOSONAR - public on purpose
	public String deploymentProject; // NOSONAR - public on purpose
	public String ciBackLink; // NOSONAR - public on purpose
	public String remediationAction; // NOSONAR - public on purpose

	public class AttachRules {
		public String[] entityIds; // NOSONAR - public on purpose
		public TagRule[] tagRule; // NOSONAR - public on purpose
	}

	public class TagRule {
		public String[] meTypes; // NOSONAR - public on purpose
		public String[] tags; // NOSONAR - public on purpose
		//The option of tags having context and key is not supported
	}

	public EventObject() {
		attachRules = new AttachRules();
		customProperties = new HashMap<>();
	}
}

