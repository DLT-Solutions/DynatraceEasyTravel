package com.dynatrace.diagnostics.uemload.scalemicrojourneyservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Properties;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPResponseRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.NanoHTTPD.Response;

import ch.qos.logback.classic.Logger;



public class ScaleMicroJourneyServiceTest{
	private static final Logger LOGGER = LoggerFactory.make();
	private MockRESTServer server;

	@Test
	public void testIfMarathonIsRunning(){
		//Marathon-like info JSON
		final String marathonInfo = "{\"name\":\"marathon\",\"version\":\"0.13.0\","
				+ "\"elected\":true,\"leader\":\"172.18.156.169:8080\","
				+ "\"frameworkId\":\"746cbccc-bb1b-461e-90ea-a62b69230520-0000\","
				+ "\"marathon_config\":{\"master\":\"zk://172.18.156.169:2181/mesos\","
				+ "\"failover_timeout\":604800,\"framework_name\":\"marathon\",\"ha\":true,\"checkpoint\":true,"
				+ "\"local_port_min\":10000,\"local_port_max\":20000,\"executor\":\"//cmd\","
				+ "\"hostname\":\"172.18.156.169\",\"webui_url\":null,\"mesos_role\":null,"
				+ "\"task_launch_timeout\":300000,\"reconciliation_initial_delay\":15000,"
				+ "\"reconciliation_interval\":300000,\"mesos_user\":\"root\",\"leader_proxy_connection_timeout_ms\":5000,"
				+ "\"leader_proxy_read_timeout_ms\":10000,\"mesos_leader_ui_url\":\"http://172.18.156.169:5050/\"},"
				+ "\"zookeeper_config\":{\"zk\":\"zk://172.18.156.169:2181/marathon\",\"zk_timeout\":10000,"
				+ "\"zk_session_timeout\":10000,\"zk_max_versions\":25},\"event_subscriber\":{\"type\":\"http_callback\","
				+ "\"http_endpoints\":null},\"http_config\":{\"assets_path\":null,\"http_port\":8080,\"https_port\":8443}}";
		try {
			EasyTravelConfig config = EasyTravelConfig.read();
			server = new MockRESTServer(new HTTPResponseRunnable(){

				@Override
				public Response run(String uri, String method, Properties header, Properties parms) {

					if(uri.contains("/v2/info")){
						return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, marathonInfo);
					}
					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "OK.");

				}

			});
			config.marathonHost="localhost";
			config.marathonPort= Integer.toString(server.getPort());
			config.marathonURI= "http://"+config.marathonHost+":"+config.marathonPort+"/";
			ScaleMicroJourneyService scaleMJS = new ScaleMicroJourneyService();

			LOGGER.info("Port is: "+server.getPort());
			assertTrue(scaleMJS.isMarathonRunning());

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}

	// This test tries to connect to a running marathon instance that uses a self signed certificate - it tells if the SSL setup works fine
	// In order to run this test, please fill in the following config in the properties file:
	// config.marathonURI, config.marathonMicroservices, config.marathonUser, config.marathonPassword
	@Ignore("Integration test")
	@Test
	public void testMarathonConnectivityWithSelfsignedCerts(){
		ScaleMicroJourneyService microJourneyService = new ScaleMicroJourneyService();
		microJourneyService.addBasicAuthentication();
		assertTrue(microJourneyService.isMarathonRunning());
	}

	@Test
	public void testMicrojourneyServiceScaling(){
		final int numberToScale = 3;
		try {
			EasyTravelConfig config = EasyTravelConfig.read();
			server = new MockRESTServer(new HTTPResponseRunnable(){

				@Override
				public Response run(String uri, String method, Properties header, Properties parms) {

					assertTrue(parms.getProperty("requestBody").equals("{\"instances\":"+Integer.toString(numberToScale)+"}"));
					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "OK.");
				}

			});
			config.marathonHost="localhost";
			config.marathonMicroservices= new String[]{"micro-journey-service","micro-journey-service2"};
			config.marathonPort= Integer.toString(server.getPort());
			config.marathonURI= "http://"+config.marathonHost+":"+config.marathonPort+"/";
			ScaleMicroJourneyService scaleMJS = new ScaleMicroJourneyService();

			LOGGER.info("Port is: "+server.getPort());
			scaleMJS.scale(numberToScale);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			EasyTravelConfig.resetSingleton();

		}
	}

	@Test
	public void testDeletingAllDeployments(){
		final String[] deploymentIDs = {"deccd40a-c8d7-4c9f-bfcf-a8aff55a1e3e", "d4ccd40a-c8d7-4c9f-bfcf-a8aff55a1e3e"};
		//Marathon-like deployment json
		final String deployments = "[{\"id\":\""+deploymentIDs[0]+"\",\"version\":\"2015-12-17T12:36:35.974Z\",\"affectedApps\":[\"/micro-journey-service\"],\"steps\":[[{\"action\":\"ScaleApplication\",\"app\":\"/micro-journey-service\"}]],\"currentActions\":[{\"action\":\"ScaleApplication\",\"app\":\"/micro-journey-service\"}],\"currentStep\":1,\"totalSteps\":1},"
							+ "{\"id\":\""+deploymentIDs[1]+"\",\"version\":\"2015-12-17T12:36:35.974Z\",\"affectedApps\":[\"/micro-journey-service\"],\"steps\":[[{\"action\":\"ScaleApplication\",\"app\":\"/micro-journey-service\"}]],\"currentActions\":[{\"action\":\"ScaleApplication\",\"app\":\"/micro-journey-service\"}],\"currentStep\":1,\"totalSteps\":1}]";

		EasyTravelConfig config = EasyTravelConfig.read();
		try {
			server = new MockRESTServer(new HTTPResponseRunnable() {
				@Override
				public Response run(String uri, String method, Properties header, Properties parms) {
					if(method.equals("GET")){

					    return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, deployments);

					}
					else if (method.equals("DELETE")){
						assertTrue(uri.equals("/v2/deployments/"+deploymentIDs[0]) || uri.equals("/v2/deployments/"+deploymentIDs[1]));

					}
					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "OK.");

				}
			});
			config.marathonHost="localhost";
			config.marathonMicroservices= new String[]{"micro-journey-service"};
			config.marathonPort= Integer.toString(server.getPort());
			config.marathonURI= "http://"+config.marathonHost+":"+config.marathonPort+"/";
			ScaleMicroJourneyService scaleMJS = new ScaleMicroJourneyService();


			scaleMJS.deleteCurrentDeployments();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testWaitingForDeploymentsToDelete(){
		final String[] deploymentIDs = {"deccd40a-c8d7-4c9f-bfcf-a8aff55a1e3e", "d4ccd40a-c8d7-4c9f-bfcf-a8aff55a1e3e"};
		final String emptyDeployment="[]";
		//Marathon-like deployment json
		final String deployments = "[{\"id\":\""+deploymentIDs[0]+"\",\"version\":\"2015-12-17T12:36:35.974Z\",\"affectedApps\":[\"/micro-journey-service\"],\"steps\":[[{\"action\":\"ScaleApplication\",\"app\":\"/micro-journey-service\"}]],\"currentActions\":[{\"action\":\"ScaleApplication\",\"app\":\"/micro-journey-service\"}],\"currentStep\":1,\"totalSteps\":1},"
							+ "{\"id\":\""+deploymentIDs[1]+"\",\"version\":\"2015-12-17T12:36:35.974Z\",\"affectedApps\":[\"/micro-journey-service\"],\"steps\":[[{\"action\":\"ScaleApplication\",\"app\":\"/micro-journey-service\"}]],\"currentActions\":[{\"action\":\"ScaleApplication\",\"app\":\"/micro-journey-service\"}],\"currentStep\":1,\"totalSteps\":1}]";

		EasyTravelConfig config = EasyTravelConfig.read();
		try {
			server = new MockRESTServer(new HTTPResponseRunnable() {
				int count = 0;
				@Override
				public Response run(String uri, String method, Properties header, Properties parms) {
					LOGGER.info("Header: " + header);
					LOGGER.info("Params: " + parms);
					LOGGER.info("URI: " + uri);
					LOGGER.info("Method: " + method);

					count++;
					if(count==5){
						return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, emptyDeployment);
					}
					else return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, deployments);

				}
			});
			config.marathonHost="localhost";
			config.marathonMicroservices= new String[]{"micro-journey-service"};
			config.marathonPort= Integer.toString(server.getPort());
			config.marathonURI= "http://"+config.marathonHost+":"+config.marathonPort+"/";
			ScaleMicroJourneyService scaleMJS = new ScaleMicroJourneyService();
			scaleMJS.areDeploymentsDeleted();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			EasyTravelConfig.resetSingleton();
		}

	}

	@Test
	public void testIfScaleMicroJourneyServiceRunnableRunsOnlyWhenStateChanged(){
		try {
			EasyTravelConfig config = EasyTravelConfig.read();
			MyHttpRunnable runnable = new MyHttpRunnable();
			server = new MockRESTServer(runnable);

			config.marathonHost="localhost";
			config.marathonPort= Integer.toString(server.getPort());
			config.marathonURI= "http://"+config.marathonHost+":"+config.marathonPort+"/";

			PluginChangeMonitor.setupPlugin(PluginChangeMonitor.Plugins.SCALE_MICRO_JOURNEY_SERVICE, Boolean.FALSE);
			ScaleMicroJourneyService scaleMJS = new ScaleMicroJourneyService();

			scaleMJS.pluginsChanged();
			Thread.sleep(3000);
			assertEquals(0, runnable.getServerContactCount());

			PluginChangeMonitor.setupPlugin(PluginChangeMonitor.Plugins.SCALE_MICRO_JOURNEY_SERVICE, Boolean.TRUE);
			scaleMJS.pluginsChanged();
			Thread.sleep(1000);
			scaleMJS.pluginsChanged();
			Thread.sleep(3000);
			assertEquals(1, runnable.getServerContactCount());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}

	class MyHttpRunnable implements HTTPResponseRunnable {
		int serverContactCount = 0;

		@Override
		public Response run(String uri, String method, Properties header, Properties parms) {
			serverContactCount++;

			if(uri.contains("/v2/info")){
				return new Response(NanoHTTPD.HTTP_NOTFOUND, NanoHTTPD.MIME_PLAINTEXT, "No such server");
			}
			return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "OK.");
		}

		int getServerContactCount() {
			return serverContactCount;
		}

	}

	@After
	public void tearDown() throws InterruptedException {
		if(server != null) {
			server.stop();
		}
	}
}
