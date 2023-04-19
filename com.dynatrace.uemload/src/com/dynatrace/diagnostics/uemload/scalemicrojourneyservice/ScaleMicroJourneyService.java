package com.dynatrace.diagnostics.uemload.scalemicrojourneyservice;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.diagnostics.uemload.UemLoadScheduler;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeListener;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.json.JSONArray;
import com.dynatrace.easytravel.json.JSONException;
import com.dynatrace.easytravel.json.JSONObject;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.util.TextUtils;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class ScaleMicroJourneyService implements PluginChangeListener {
	private static Logger LOGGER = Logger.getLogger(ScaleMicroJourneyService.class.getName());

	private static final String MARATHON_URI = EasyTravelConfig.read().marathonURI;
	private static final String MARATHON_USER = EasyTravelConfig.read().marathonUser;
	private static final String MARATHON_PASS = EasyTravelConfig.read().marathonPassword;

	private static final int MARATHON_SCALED_NUMBER_OF_MICROSERVICES = EasyTravelConfig
			.read().marathonScaledNumberOfMicroservices;
	private static final int MARATHON_DEFAULT_NUMBER_OF_MICROSERVICES = EasyTravelConfig
			.read().marathonDefaultNumberOfMicroservices;
	private static final String[] MARATHON_MICROSERVICES = EasyTravelConfig.read().marathonMicroservices;
	private static final int DEPLOYMENTS_DELETE_CHECKS_LIMIT = 30;

	private static Client client = createAndConfigureJerseyClient();
	private static int numberOfMicroservices = (MARATHON_MICROSERVICES != null ? MARATHON_MICROSERVICES.length : 0);

	volatile boolean isPluginEnabled = PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.SCALE_MICRO_JOURNEY_SERVICE);

	private static Client createAndConfigureJerseyClient() {
		UrlUtils.trustAllHttpsCertificates();
		return Client.create();
	}

	@Override
	public void pluginsChanged() {

		/*
		 * If application is in deployment state you cannot scale it - for
		 * example Mesos could be waiting for available resources from previous
		 * state change
		 *
		 */

		if (isPluginEnabled != PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.SCALE_MICRO_JOURNEY_SERVICE)
				&& !MARATHON_URI.equals("") && numberOfMicroservices != 0) {
			/*
			 * Executing functionality in a separate thread
			 */
			isPluginEnabled = !isPluginEnabled;
			UemLoadScheduler.schedule(new ScaleMicroJourneyServiceRunnable(), 0, TimeUnit.SECONDS);

		}
	}

	public class ScaleMicroJourneyServiceRunnable implements Runnable {

		@Override
		public void run() {
			try {
				addBasicAuthentication();
				if (isMarathonRunning()) {

					deleteCurrentDeployments();

					if (areDeploymentsDeleted()) {

						if (PluginChangeMonitor
								.isPluginEnabled(PluginChangeMonitor.Plugins.SCALE_MICRO_JOURNEY_SERVICE)) {
							scale(MARATHON_SCALED_NUMBER_OF_MICROSERVICES);
						} else {
							scale(MARATHON_DEFAULT_NUMBER_OF_MICROSERVICES);
						}
					}
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Unexpected error.", e);
			}
		}
	}

	protected void addBasicAuthentication(){
		if(StringUtils.isNotBlank(MARATHON_USER) && StringUtils.isNotBlank(MARATHON_PASS)){
			client.addFilter(new HTTPBasicAuthFilter(MARATHON_USER, MARATHON_PASS));
		}
		else {
			LOGGER.log(Level.INFO, "Marathon user or pass not provided.");
		}

	}

	protected boolean isMarathonRunning() {
		String marathonInfoURI = MARATHON_URI + "v2/info";

			ClientResponse response = client.resource(marathonInfoURI).type(MediaType.APPLICATION_JSON)
					.get(ClientResponse.class);
			response.bufferEntity();
			String jsonResponse = response.getEntity(String.class);
			LOGGER.log(Level.INFO, TextUtils.merge("Marathon Info: {0}", jsonResponse));
			try {
				JSONObject info = new JSONObject(jsonResponse);
				/*
				 * We need to make sure that this is really Marathon and not
				 * some other application with REST-API
				 */
				if (info.getString("name").equals("marathon")){
					LOGGER.log(Level.INFO, "Marathon is running.");
					return true;
				}

			} catch (JSONException e) {
				LOGGER.log(Level.SEVERE, "Failed to create JSONObject based on server response.");
			}

		LOGGER.log(Level.SEVERE, "Marathon is not running.");
		return false;
	}

	void scale(int desiredNumberOfInstances) {
		String marathonURI = MARATHON_URI + "v2/apps/";
		for (int i = 0; i < numberOfMicroservices; i++) {
			client.resource(marathonURI + MARATHON_MICROSERVICES[i]).type(MediaType.APPLICATION_JSON)
					.put(ClientResponse.class, "{\"instances\":" + Integer.toString(desiredNumberOfInstances) + "}");
		}
		LOGGER.log(Level.INFO, "Microservices scaled.");
	}

	void deleteCurrentDeployments() {
		String marathonDeployments = MARATHON_URI + "v2/deployments/";
		ClientResponse response = client.resource(marathonDeployments).type(MediaType.APPLICATION_JSON)
				.get(ClientResponse.class);
		response.bufferEntity();
		String jsonResponse = response.getEntity(String.class);
		LOGGER.log(Level.INFO, TextUtils.merge("Deployments: {0}", jsonResponse));
		try {
			JSONArray deployments = new JSONArray(jsonResponse);
			for (int i = 0; i < deployments.length(); i++) {
				client.resource(marathonDeployments + deployments.getJSONObject(i).getString("id"))
						.type(MediaType.APPLICATION_JSON).delete(ClientResponse.class);
			}

		} catch (JSONException e) {
			LOGGER.log(Level.SEVERE, "Failed to create JSONArray based on server response.");
		}

	}

	boolean areDeploymentsDeleted() {
		String jsonResponse;
		int countRequests = 0;
		do {

			String marathonDeployments = MARATHON_URI + "v2/deployments/";
			ClientResponse response = client.resource(marathonDeployments).type(MediaType.APPLICATION_JSON)
					.get(ClientResponse.class);
			response.bufferEntity();
			jsonResponse = response.getEntity(String.class);

			/*
			 * Preventing looping forever
			 */
			if (countRequests > DEPLOYMENTS_DELETE_CHECKS_LIMIT){
				LOGGER.log(Level.SEVERE, "Deployments delete checks limit reached. Please check Marathon.");
				return false;
			}


			try {
				Thread.sleep(1000);
				countRequests++;
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE, "Thread sleep was interrupted.");
			}
		} while (!jsonResponse.equals("[]"));
		return true;
	}

}
