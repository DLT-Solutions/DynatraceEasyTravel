package com.dynatrace.easytravel.spring;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ArrayUtils;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.util.DynatraceUrlUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.ClientHandlerException;

import ch.qos.logback.classic.Logger;

/**
 * Class used to create new event notification in Dynatrace when plugin state changes.
 * The information sent is defined in template file.
 *
 * @author tomasz.wieremjewicz
 * @date 20 lis 2017
 *
 */
public class PluginEventNotifier {
	private final ClientConfig clientConfig = new DefaultClientConfig();
    private final Client client = Client.create(clientConfig);
    private final String apiToken;
    private ObjectMapper mapper;

    private static final Logger log = LoggerFactory.make();

    private ConcurrentHashMap<String, String> eventObjectTemplates;

	public PluginEventNotifier() {
		mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);

		EasyTravelConfig config = EasyTravelConfig.read();
		apiToken = config.apmTenantToken;

    	eventObjectTemplates = new ConcurrentHashMap<String, String>();
	}

	/**
	 * Load templates. Already configured templates are not overwritten.
	 * @param templates Templates to load
	 * @throws JsonProcessingException
	 */
	private void loadTemplates(PluginNotificationTemplate[] templates) throws JsonProcessingException {
		if (!ArrayUtils.isEmpty(templates)) {
			for(PluginNotificationTemplate element : templates) {
				if (element.isTemplateDefinitionComplete()) {
					for(String name : element.pluginNames) {
						if (!eventObjectTemplates.containsKey(name.toLowerCase())) {
							eventObjectTemplates.put(name.toLowerCase(), mapper.writeValueAsString(getEventObject(element)));
						}
						else {
							log.warn("Multiple templates for the same plugin name: " + name);
						}
					}
				}
			}
		}
	}

	/**
	 * Initialize templates with a JSON configuration string
	 * @param configuration
	 */
	public void initializeTemplatesFromJson(String configuration) {
		log.info("PluginEventNotifier: Initializing with JSON string: " + configuration);
		try {
			PluginNotificationTemplate[] templates;
			templates = mapper.readValue(configuration, PluginNotificationTemplate[].class);
			loadTemplates(templates);
		} catch (IOException e) {
			log.error("Problem parsing json configuration", e);
		}
	}

	/**
	 * Create a new event with supplied data if an appropriate template exists.
	 * @param info New event description
	 */
	public void sendPluginStateChangeEvent(PluginChangeInfo info) {
		if (info != null && DtVersionDetector.isAPM()) {
			String template = eventObjectTemplates.get(info.pluginName.toLowerCase());
			if (template == null) {
				template = eventObjectTemplates.get(PluginNotificationTemplate.DEFAULT_PLUGIN_NAME.toLowerCase());
			}

			if (template != null) {
				log.info("Sending an event notification for plugin " + info.pluginName);
				template = template
						.replaceAll(PluginNotificationTemplate.PARAMETER_VERSION, info.version)
						.replaceAll(PluginNotificationTemplate.PARAMETER_TIMESTAMP, info.timestamp)
						.replaceAll(PluginNotificationTemplate.PARAMETER_PLUGIN_STATE, info.enabled)
						.replaceAll(PluginNotificationTemplate.PARAMETER_PLUGIN_NAME, info.pluginName)
						.replaceAll(PluginNotificationTemplate.PARAMETER_START, Long.toString(System.currentTimeMillis() - (3*60*1000)))
						.replaceAll(PluginNotificationTemplate.PARAMETER_END, Long.toString(System.currentTimeMillis() + (3*60*1000)));
				sendEvent(template);
			}
		}
		else {
			log.info("Event notification could not be sent: " + info != null ? "the DtVersionDetector.isAPM() returned false" : "PluginChangeInfo object is null");
		}
	}

	private void sendEvent(String postBody) {
		log.info("Sending event notification with a body of: " + postBody);
		String url = DynatraceUrlUtils.getDynatraceUrl() + "/api/v1/events/";
		try {
			WebResource webResource = client.resource(url);
			ClientResponse clientResponse = webResource
					.type("application/json")
					.header("Authorization", "Api-Token " + apiToken)
					.post(ClientResponse.class, postBody);
	
			if (clientResponse.getStatus() != 200) {
				log.error("sendEvent failed. PostBody: " + postBody + ". Reason: " + clientResponse.getEntity(String.class));
			}
			else {
				log.info("An event notification has been created.");
			}
		} catch (ClientHandlerException e) {
			log.error("Couldn't connect to " + url + ". Event not sent. Message: " + e.getMessage());
		}
	}

	private EventObject getEventObject(PluginNotificationTemplate template) {
		EventObject eventObject = new EventObject();
		eventObject.eventType = EventObject.EVENT_TYPE_CUSTOM_DEPLOYMENT;
		eventObject.attachRules.entityIds = template.entityIds;
		eventObject.source = template.source;
		eventObject.deploymentName = template.title;
		eventObject.deploymentVersion = template.version;
		eventObject.customProperties = template.customProperties;
		eventObject.start = PluginNotificationTemplate.PARAMETER_START;
		eventObject.end = PluginNotificationTemplate.PARAMETER_END;
		eventObject.deploymentProject = template.deploymentProject;
		eventObject.remediationAction = template.remediationAction;
		eventObject.ciBackLink = template.ciBackLink;

		return eventObject;
	}

	public Map<String, String> getEventObjectTemplates() {
		return eventObjectTemplates;
	}
}
