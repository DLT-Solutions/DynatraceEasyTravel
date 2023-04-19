package com.dynatrace.easytravel.launcher.remote;

import javax.ws.rs.core.MediaType;

import com.dynatrace.easytravel.launcher.misc.Constants;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;


public class TestHttpServiceClient {

	/**
	 *
	 * @param args
	 * @author dominik.stadler
	 */
	public static void main(String[] args) {
		ClientConfig clientConfig = new DefaultClientConfig();
	    Client client = Client.create(clientConfig);

	    WebResource r = client.resource("http://localhost:1697/start/" + Constants.Procedures.INPROCESS_DBMS_ID.replace(" ", "%20"));
	    String response = r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
	    System.out.println("\nStarting Database: " + response);


	    r = client.resource("http://localhost:1697/start/" + Constants.Procedures.CREDIT_CARD_AUTH_UNIT_ID.replace(" ", "%20"));
	    response = r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
	    System.out.println("\nStarting CCA: " + response);


	    r = client.resource("http://localhost:1697/start/" + Constants.Procedures.BUSINESS_BACKEND_ID.replace(" ", "%20"));
	    response = r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
	    System.out.println("\nStarting Business Backend: " + response);


	    r = client.resource("http://localhost:1697/statusAll");
	    response = r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
	    System.out.println("\nStatusAll: " + response);


	    r = client.resource("http://localhost:1697/stopAll");
	    response = r.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
	    System.out.println("\nStopAll: " + response);
	}
}
