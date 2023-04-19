package com.dynatrace.easytravel.webapi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import java.util.Objects;

public class WebAPITest {

	private static final String USER_AGENT = "Mozilla/5.0";

	private String testName;
	private String expectedResponseCode;
	private String urlString;
	private String testRunUUID;



	public static void main(String[] args) throws IOException {

		if (args.length != 4) {
			throw new IllegalArgumentException("Expected [4] parameters to be passed to the WebAPITest. " +
					"Got [" + args.length + "]. " +
					"Expected parameters: testName, expectedResponseCode, urlString, testRunUUID");
		}

		try {
			WebAPITest http = new WebAPITest();
			http.setTestName(args[0]);
			http.setExpectedResponseCode(args[1]);
			http.setUrlString(args[2]);
			http.setTestRunUUID(args[3]);

			http.sendGet();
		} catch (Exception e) {
			System.out.println("Exception for WebAPITest class: " + e);
		}
	}

	// HTTP GET request
	private void sendGet() throws Exception {

		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("X-dynatrace",
				"TN=" + testName +
				";RC=" + expectedResponseCode +
				";TR=" + testRunUUID
		);

		System.out.println("\nSending 'GET' request to URL : " + url + "   testRunUUID:" + testRunUUID);
		int responseCode = con.getResponseCode();
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		while (true) {
			if (in.readLine() == null) break;
		}
		in.close();
	}





	private void setExpectedResponseCode(String expectedResponseCode) {
		if (expectedResponseCode == null || Objects.equals(expectedResponseCode, "")) {
			throw new IllegalArgumentException(
					"empty or null WebAPITest ant task parameter [expectedResponseCode:" + expectedResponseCode + "]");
		}
		this.expectedResponseCode = expectedResponseCode;
	}

	private void setTestName(String testName) {
		if (testName == null || Objects.equals(testName, "")) {
			throw new IllegalArgumentException(
					"empty or null WebAPITest ant task parameter [testName:" + testName + "]");
		}
		this.testName = testName;
	}

	private void setTestRunUUID(String testRunUUID) {
		if (testRunUUID == null || Objects.equals(testRunUUID, "")) {
			throw new IllegalArgumentException(
					"empty or null WebAPITest ant task parameter [testRunUUID:" + testRunUUID + "]");
		}
		this.testRunUUID = testRunUUID;
	}

	private void setUrlString(String urlString) {
		if (urlString == null || Objects.equals(urlString, "")) {
			throw new IllegalArgumentException(
					"empty or null WebAPITest ant task parameter [urlString:" + urlString + "]");
		}
		this.urlString = urlString;
	}
}
