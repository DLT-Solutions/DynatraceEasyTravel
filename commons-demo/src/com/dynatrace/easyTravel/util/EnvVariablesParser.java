package com.dynatrace.easytravel.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.json.JSONArray;
import com.dynatrace.easytravel.json.JSONException;
import com.dynatrace.easytravel.json.JSONObject;

public class EnvVariablesParser {
	private static Logger LOGGER = Logger.getLogger(EnvVariablesParser.class.getName());
	private static final String CLOUD_FOUNDRY_DB_NAME = "easytravel-mongodb";
	public static String dbUser;
	public static String dbPass;
	public static String dbUri;

	public static final Map<String, String> environmentVariables;
	static {
		environmentVariables = new HashMap<String, String>();
		environmentVariables.put("ET_BACKEND_URL", "config.webServiceBaseDir");
		environmentVariables.put("ET_DATABASE_USER", "config.mongoDbUser");
		environmentVariables.put("ET_DATABASE_PASSWORD", "config.mongoDbPassword");
		environmentVariables.put("ET_DATABASE_LOCATION", "config.mongoDbInstances");
		environmentVariables.put("ET_MONGO_AUTH_DB", "config.mongoDbAuthDatabase");
	}

	/*
	 * Parse VCAP_SERVICES to find neccessary db info
	 */
	public static String findDbValueInVcapServices(String key, JSONArray jsonArray) {
		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject object = jsonArray.optJSONObject(i);
				Iterator<?> iterator = object.keys();
				while (iterator.hasNext()) {
					String currentKey = (String) iterator.next();
					/*
					 * All informations for connection are listed in JsonArray
					 * 'credentials'
					 */
					if (currentKey.equals("credentials")) {
						return findDbValueInVcapServices(key,
								new JSONArray(JsonUtils.addJsonArrayBrackets(object.get("credentials").toString())));
					}
					if (currentKey.equals(key)) {
						return object.getString(key);
					}
				}
			}

		} catch (JSONException e) {
			LOGGER.log(Level.SEVERE, "There was an error parsing JSON Object for VCAP_SERVICES.", e);
		} catch (NullPointerException e) {
			LOGGER.log(Level.SEVERE, "Couldn't find object: " + key, e);
		}
		LOGGER.log(Level.SEVERE, "There was no key: " + key + " found in VCAP_SERVICES.");
		return null;
	}

	public static void parseEnv() {
		/*
		 * If there is an environment variable VCAP_SERVICE we start to parse it
		 */
		String vcapServices = System.getenv("VCAP_SERVICES");
		if (StringUtils.isNotBlank(vcapServices)) {
			parseVcapServices(vcapServices, true);
		}
		/*
		 * Parse available ENV VARIABLES
		 */
		setLocalEnvProperties();
	}

	public static String appendServicesLocation(String webServiceBaseDirectory) {
		if (webServiceBaseDirectory.endsWith("/services/")) {
			return webServiceBaseDirectory;
		} else if (webServiceBaseDirectory.endsWith("/")) {
			webServiceBaseDirectory += "services/";
			return webServiceBaseDirectory;
		} else if (webServiceBaseDirectory.endsWith("/services")) {
			webServiceBaseDirectory += "/";
			return webServiceBaseDirectory;
		} else {
			webServiceBaseDirectory += "/services/";
			return webServiceBaseDirectory;
		}
	}

	public static String formatDatabaseLocation(String url) {
		if (url.startsWith("http://")) {
			return url.replace("http://", "");
		} else if (url.startsWith("https://")) {
			return url.replace("https://", "");
		} else
			return url;
	}

	public static void setLocalEnvProperties() {
		for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
			String envVariable = System.getenv(entry.getKey());
			if (StringUtils.isNotBlank(envVariable)) {
				if (entry.getKey().equals("ET_BACKEND_URL")) {
					envVariable = appendServicesLocation(envVariable);
				} else if (entry.getKey().equals("ET_DATABASE_LOCATION")) {
					envVariable = formatDatabaseLocation(envVariable);
				} else if (entry.getKey().equals("ET_DATABASE_USER") && envVariable.equals("!disabled!")){
					envVariable = StringUtils.EMPTY;
				}				
				System.setProperty(entry.getValue(), envVariable);
			} else {
				LOGGER.log(Level.INFO, entry.getKey() + " environment variable is not set. Using default.");
			}
		}			
		
		parsePluginServiceLocation();
	}	
			
	public static void parsePluginServiceLocation() {
		parsePluginServiceLocation(System.getenv("ET_PLUGIN_SERVICE_LOCATION"));
	}
	//this method is public because it is used in unit tests
	public static void parsePluginServiceLocation(String envVariable) {		
		if (StringUtils.isNotBlank(envVariable)) {
			String[] tab = envVariable.trim().split(":");
			if (tab.length > 0) {
				LOGGER.log(Level.INFO, "Set plugin config.pluginServiceHost " + tab[0]);
				System.setProperty("config.pluginServiceHost", tab[0]);			
				if (tab.length > 1) {
					LOGGER.log(Level.INFO, "Set plugin config.pluginServicePort " + tab[1]);
					System.setProperty("config.pluginServicePort", tab[1]);	
				}	
			}
		}
	}

	private static void setSystemProperties(Map<String, String> propertiesMap) {
		for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
			System.setProperty(entry.getKey(), entry.getValue());
		}
	}

	private static String findDbService(Set<?> services) {
		Iterator<?> servicesIter = services.iterator();

		while (servicesIter.hasNext()) {
			String key = (String) servicesIter.next();
			if (key.toLowerCase().equals(CLOUD_FOUNDRY_DB_NAME)) {
				return key;
			}
		}
		return null;
	}

	public static void parseVcapServices(String envVariable, boolean doSetSystemProperties) {
		Map<String, String> propertiesMap = new HashMap<String, String>();

		try {

			JSONObject vcapServices = new JSONObject(envVariable);
			String dbService = findDbService(vcapServices.keySet());
			if (StringUtils.isBlank(dbService)) {
				LOGGER.log(Level.SEVERE, "There is no service that matches name: " + CLOUD_FOUNDRY_DB_NAME);
				return;
			}
			JSONArray dbEasyTravel = vcapServices.getJSONArray(dbService);
			String hostname = findDbValueInVcapServices("hostname", dbEasyTravel);
			String port = findDbValueInVcapServices("port", dbEasyTravel);
			String user = findDbValueInVcapServices("username", dbEasyTravel);
			String pass = findDbValueInVcapServices("password", dbEasyTravel);

			if (StringUtils.isNotBlank(user)) {
				dbUser = user;
				propertiesMap.put("config.mongoDbUser", dbUser);
			}
			if (StringUtils.isNotBlank(pass)) {
				dbPass = pass;
				propertiesMap.put("config.mongoDbPassword", dbPass);
			}
			if (StringUtils.isBlank(port)) {
				port = "27017";
				LOGGER.log(Level.SEVERE, "Using default port for mongoDB: 27017.");
			}
			if (StringUtils.isNotBlank(hostname)) {
				dbUri = hostname + ":" + port;
				propertiesMap.put("config.mongoDbInstances", dbUri);
			}
			if (doSetSystemProperties) {
				setSystemProperties(propertiesMap);
			}

		} catch (JSONException e) {
			LOGGER.log(Level.SEVERE, "There was an error creating JSON Array for VCAP_SERVICES.", e);
		}
	}
}
