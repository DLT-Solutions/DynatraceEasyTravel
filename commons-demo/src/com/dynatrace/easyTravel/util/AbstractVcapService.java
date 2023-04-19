package com.dynatrace.easytravel.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.json.JSONArray;
import com.dynatrace.easytravel.json.JSONException;
import com.dynatrace.easytravel.json.JSONObject;


public abstract class AbstractVcapService {
	
	private static final Logger LOGGER = Logger.getLogger(AbstractVcapService.class.getName());
	
	private static final String ENV_VARIABLE_VCAP_SERVICES = "VCAP_SERVICES".intern();

	private static final String CONFIG_DATABASE_DRIVER				= "config.databaseDriver".intern();
	private static final String CONFIG_INTERNAL_DATABASE_ENABLED	= "config.internalDatabaseEnabled".intern();
	private static final String CONFIG_DATABASE_URL					= "config.databaseUrl".intern();
	private static final String CONFIG_DATABASE_USER				= "config.databaseUser".intern();
	private static final String CONFIG_DATABASE_PASSWORD			= "config.databasePassword".intern();
	
	protected static final String SERVICE_PROPERTY_JDBC_URL	= "credentials.jdbcUrl".intern();
	protected static final String SERVICE_PROPERTY_USER		= "credentials.username".intern();
	protected static final String SERVICE_PROPERTY_PASSWORD	= "credentials.password".intern();
	protected static final String SERVICE_PROPERTY_LABEL		= "label".intern();
	protected static final String SERVICE_PROPERTY_NAME		= "name".intern();
	
	public abstract String getJDBCDriverClass();
	public abstract Map<String, String> getRequiredProperties();
	
	private static enum MatchType {
		EQUALS, STARTS, ENDS, CONTAINS
	}
	
	private static boolean matches(String expected, String actual) {
		return matches(expected, actual, MatchType.EQUALS);
	}
	
	private static boolean matches(String expected, String actual, MatchType matchType) {
		if (expected == null || actual == null) {
			return false;
		}
		
		if ("*".equals(expected)) {
			return true;
		}
		if (expected.startsWith("*")) {
			switch (matchType) {
			case CONTAINS:
			case STARTS:
				return matches(expected.substring(1), actual, MatchType.CONTAINS);
			case ENDS:
			case EQUALS:
				return matches(expected.substring(1), actual, MatchType.ENDS);
			}
		} else if (expected.endsWith("*")) {
			switch (matchType) {
			case EQUALS:
			case STARTS:
				return matches(expected.substring(0, expected.length() - 1), actual, MatchType.STARTS);
			case CONTAINS:
			case ENDS:
				return matches(expected.substring(1, expected.length() - 1), actual, MatchType.CONTAINS);
			}
		}
		
		switch (matchType) {
		case EQUALS:
			return actual.equals(expected);
		case STARTS:
			return actual.startsWith(expected);
		case CONTAINS:
			return actual.contains(expected);
		case ENDS:
			return actual.endsWith(expected);
		}
		
		return true;
	}
	
	private boolean defineConfigs(Map<String, String> serviceProperties) {
		Map<String, String> configProperties = mapToConfigProperties(serviceProperties);
		if (configProperties == null) {
			return false;
		}
		for (Entry<String, String> requiredProperty : getRequiredProperties().entrySet()) {
			String key = requiredProperty.getKey();
			String requiredValue = requiredProperty.getValue();
			String configPropertyValue = serviceProperties.get(key);
			if (!matches(requiredValue, configPropertyValue)) {
				LOGGER.info("value for '" + key + "' does not match. expected: '" + requiredValue + "', actual: '" + configPropertyValue + "'");
				return false;
			}
		}
		
		for (Entry<String, String> configProperty : configProperties.entrySet()) {
			LOGGER.log(Level.SEVERE, configProperty.getKey() + ": " + configProperty.getValue());			
			System.getProperties().put(configProperty.getKey(), configProperty.getValue());
		}
		return true;
	}
	
	private Map<String, String> mapToConfigProperties(Map<String, String> serviceProperties) {
		Map<String, String> configProperties = new HashMap<>();
		
		if (!putConfigProperty(configProperties, CONFIG_DATABASE_DRIVER, getJDBCDriverClass())) {
			return null;
		}
		if (!putConfigProperty(configProperties, CONFIG_INTERNAL_DATABASE_ENABLED, "false")) {
			return null;
		}
		if (!putConfigProperty(configProperties, CONFIG_DATABASE_URL, serviceProperties.get(SERVICE_PROPERTY_JDBC_URL))) {
			return null;
		}
		if (!putConfigProperty(configProperties, CONFIG_DATABASE_USER, serviceProperties.get(SERVICE_PROPERTY_USER))) {
			return null;
		}
		if (!putConfigProperty(configProperties, CONFIG_DATABASE_PASSWORD, serviceProperties.get(SERVICE_PROPERTY_PASSWORD))) {
			return null;
		}
		
		return configProperties;
	}
	
	private boolean putConfigProperty(Map<String, String> configProperties, String key, String value) {
		Objects.requireNonNull(configProperties);
		Objects.requireNonNull(key);
		
		if ((value == null) || value.trim().isEmpty()) {
			return false;
		}
		
		configProperties.put(key, value);
		
		return true;
	}

	protected boolean parse() {
		String vcapServices = System.getenv(ENV_VARIABLE_VCAP_SERVICES);
		if (vcapServices == null) {
			vcapServices = System.getProperty(ENV_VARIABLE_VCAP_SERVICES);
		}
		if (StringUtils.isNotBlank(vcapServices)) {
			return handleVcapServices(vcapServices);
		} else {
			LOGGER.info(ENV_VARIABLE_VCAP_SERVICES + " not present");
			return false;
		}
	}
	
	private boolean handleVcapService(JSONObject vcapService) {
		Map<String, String> serviceProperties = exctractServiceProperties(vcapService);
		if (serviceProperties.isEmpty()) {
			return false;
		}
				
		return defineConfigs(serviceProperties);
	}
	
	private static Map<String, String> exctractServiceProperties(JSONObject vcapService) {
		Map<String, String> serviceProperties = new HashMap<>();
		
		if (!extractServiceProperty(vcapService, serviceProperties, SERVICE_PROPERTY_LABEL)) {
			return Collections.emptyMap();
		}
		if (!extractServiceProperty(vcapService, serviceProperties, SERVICE_PROPERTY_NAME)) {
			return Collections.emptyMap();
		}
		JSONObject credentials = extractObject(vcapService, "credentials");
		if (credentials == null) {
			return Collections.emptyMap();
		}
		
		if (!extractServiceProperty(credentials, serviceProperties, "jdbcUrl", SERVICE_PROPERTY_JDBC_URL)) {
			return Collections.emptyMap();
		}
		if (!extractServiceProperty(credentials, serviceProperties, "username", SERVICE_PROPERTY_USER)) {
			return Collections.emptyMap();
		}
		if (!extractServiceProperty(credentials, serviceProperties, "password", SERVICE_PROPERTY_PASSWORD)) {
			return Collections.emptyMap();
		}
		
		return serviceProperties;
	}
	
	private static boolean extractServiceProperty(JSONObject jsonObject, Map<String, String> serviceProperties, String key) {
		return extractServiceProperty(jsonObject, serviceProperties, key, key);
	}
	
	private static boolean extractServiceProperty(JSONObject jsonObject, Map<String, String> serviceProperties, String key, String servicePropertiesKey) {
		Objects.requireNonNull(jsonObject);
		Objects.requireNonNull(serviceProperties);
		Objects.requireNonNull(servicePropertiesKey);
		Objects.requireNonNull(key);
		
		String value = extractString(jsonObject, key);
		if (value == null) {
			return false;
		}
		serviceProperties.put(servicePropertiesKey, value);
		return true;
	}
	
	private static JSONObject extractObject(JSONObject vcapService, String key) {
		Objects.requireNonNull(key);
		
		Object oValue = vcapService.opt(key);
		if (oValue == null) {
			return null;
		}
		if (oValue instanceof JSONObject) {
			return (JSONObject) oValue;
		}
		
		return null;
	}
	
	private static String extractString(JSONObject vcapService, String key) {
		Objects.requireNonNull(key);
		
		Object oValue = vcapService.opt(key);
		if (oValue == null) {
			return null;
		}
		if (oValue instanceof String) {
			return (String) oValue;
		}
		
		return null;
	}
	
	private boolean handleVcapServices(JSONObject vcapServices) {
		if (vcapServices == null) {
			return false;
		}
		@SuppressWarnings("unchecked")
		Set<String> keySet = vcapServices.keySet();
		for (String key : keySet) {
			Object value = null;
			try {
				value = vcapServices.get(key);
			} catch (JSONException e) {
				LOGGER.log(Level.SEVERE, "Cannot get JSONObject for key: " + key, e );
			}
				
			if (value == null) {
				continue;
			}
			if (!(value instanceof JSONArray)) {
				continue;
			}
			JSONArray vcapServiceArray = (JSONArray) value;
			int length = vcapServiceArray.length();
			for (int i = 0; i < length; i++) {
				Object oVcapService = null;
				try {
					oVcapService = vcapServiceArray.get(i);
				} catch (JSONException e) {
					LOGGER.log(Level.SEVERE, "Cannot get JSONObject for id: " + i, e );
				}
				if (oVcapService == null) {
					continue;
				}
				if (!(oVcapService instanceof JSONObject)) {
					LOGGER.info("!(oVcapService instanceof JSONObject)");
					continue;
				}
				JSONObject vcapService = (JSONObject) oVcapService;
				if (handleVcapService(vcapService)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean handleVcapServices(String sVcapServices) {
		return handleVcapServices(resolveVcapAllServices(sVcapServices));
	}
	
	private static JSONObject resolveVcapAllServices(String sVcapServices) {
		try {
			return new JSONObject(sVcapServices);
		} catch (JSONException e) {
			LOGGER.log(Level.SEVERE, "There was an error creating JSON Array for VCAP_SERVICES.", e);
			return null;
		}
	}

}
