package com.dynatrace.easytravel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.dynatrace.easytravel.util.EnvVariablesParser;

public class EnvVariablesTest {
	
	private void resetDbUri(){
		EnvVariablesParser.dbUri = null;
	}
	@Test
	public void testParseVcapServices() {
		/*
		 * VCAP_SERVICES
		 */
		String dummyVcap1 = "{" + "\"eAsyTravel-mongodb\": [" + "{" + "\"credentials\": {"
				+ "\"dbname\": \"easyTravel-Business\"," + "\"hostname\": \"172.168.19.19\","
				+ "\"password\": \"admin\"," + "\"port\": \"32762\"," + "\"ports\": {" + "\"27017/tcp\": \"32769\","
				+ "\"28017/tcp\": \"32768\"" + "},"
				+ "\"uri\": \"mongodb://admin:admin@172.168.19.19:32762/easyTravel-Business\","
				+ "\"username\": \"admin\"" + "}," + "\"label\": \"mongodb-easytravel\","
				+ "\"name\": \"mongodb-easytravel\"," + "\"plan\": \"free\"," + "\"tags\": [" + "\"mongodb\","
				+ "\"easyTravel\"" + "]" + "}" + "]" + "}";
		String dummyVcap2 = "{" + "\"easytravel-mongodb\": [" + "{" + "\"credentials\": {"
				+ "\"dbname\": \"easyTravel-Business\"," + "\"hostname\": \"172.168.29.29\","
				+ "\"password\": \"admin\"," + "\"port\": \"20000\"," + "\"ports\": {" + "\"27017/tcp\": \"32769\","
				+ "\"28017/tcp\": \"32768\"" + "},"
				+ "\"uri\": \"mongodb://admin:admin@172.168.29.29:20000/easyTravel-Business\","
				+ "\"username\": \"admin\"" + "}," + "\"label\": \"mongodb-easytravel\","
				+ "\"name\": \"mongodb-easytravel\"," + "\"plan\": \"free\"," + "\"tags\": [" + "\"mongodb\","
				+ "\"easyTravel\"" + "]" + "}" + "]" + "}";
		String dummyVcap3 = "{" + "\"Dasytravel-mongodb\": [" + "{" + "\"credentials\": {"
				+ "\"dbname\": \"easyTravel-Business\"," + "\"hostname\": \"172.168.29.29\","
				+ "\"password\": \"admin\"," + "\"port\": \"20000\"," + "\"ports\": {" + "\"27017/tcp\": \"32769\","
				+ "\"28017/tcp\": \"32768\"" + "},"
				+ "\"uri\": \"mongodb://admin:admin@172.168.29.29:20000/easyTravel-Business\","
				+ "\"username\": \"admin\"" + "}," + "\"label\": \"mongodb-easytravel\","
				+ "\"name\": \"mongodb-easytravel\"," + "\"plan\": \"free\"," + "\"tags\": [" + "\"mongodb\","
				+ "\"easyTravel\"" + "]" + "}" + "]" + "}";
		
		//======================================================
		resetDbUri();
		EnvVariablesParser.parseVcapServices(dummyVcap1,false);
		
		assertNotNull("Database Uri is null - error during parsing VCAP_SERVICES",EnvVariablesParser.dbUri);
		assertTrue("There was an error parsing dbUri.", EnvVariablesParser.dbUri.equals("172.168.19.19:32762"));
		
		//======================================================
		resetDbUri();
		EnvVariablesParser.parseVcapServices(dummyVcap2,false);
		
		assertFalse("Failed during parsing VCAP_SERVICES - wrong hostname and/or port", EnvVariablesParser.dbUri.equals("172.168.29.29:20001"));
		
		//======================================================
		resetDbUri();
		EnvVariablesParser.parseVcapServices(dummyVcap3,false);
		
		assertNull("Database Uri should be null but is not.", EnvVariablesParser.dbUri);
	}
	
	@Test
	public void testAppendingServicesLocation(){
		assertEquals("http://xyz:8094/services/",EnvVariablesParser.appendServicesLocation("http://xyz:8094"));
		assertEquals("http://xyz:8094/services/",EnvVariablesParser.appendServicesLocation("http://xyz:8094/"));
		assertEquals("http://xyz:8094/services/",EnvVariablesParser.appendServicesLocation("http://xyz:8094/services"));
		assertEquals("http://xyz:8094/services/",EnvVariablesParser.appendServicesLocation("http://xyz:8094/services/")); 
	}
	
	@Test
	public void testFormattingDatabaseLocation(){
		assertEquals("xyz:8094",EnvVariablesParser.formatDatabaseLocation("http://xyz:8094"));
		assertEquals("xyz:8094",EnvVariablesParser.formatDatabaseLocation("https://xyz:8094"));
		assertEquals("xyz:8094",EnvVariablesParser.formatDatabaseLocation("xyz:8094"));
	}
	
	@Test
	public void testPluginServiceHost() {
		String pluginServiceHostOrg = getPluginServiceHostProperty();
		String pluginServicePortOrg = getPluginServicePortProperty();

		System.setProperty("config.pluginServiceHost","");
		System.setProperty("config.pluginServicePort", "7654");
		
		try {
		assertEquals("", getPluginServiceHostProperty());
		assertEquals("7654", getPluginServicePortProperty());
		
		EnvVariablesParser.parsePluginServiceLocation("");
		assertEquals("", getPluginServiceHostProperty());
		assertEquals("7654", getPluginServicePortProperty());
		
		EnvVariablesParser.parsePluginServiceLocation(":");
		assertEquals("", getPluginServiceHostProperty());
		assertEquals("7654", getPluginServicePortProperty());
		
		EnvVariablesParser.parsePluginServiceLocation("abcd");		
		assertEquals("abcd", getPluginServiceHostProperty());
		assertEquals("7654", getPluginServicePortProperty());
		
		EnvVariablesParser.parsePluginServiceLocation("abcd:");		
		assertEquals("abcd", getPluginServiceHostProperty());
		assertEquals("7654", getPluginServicePortProperty());
		
		EnvVariablesParser.parsePluginServiceLocation("efg:8080");
		assertEquals("efg", getPluginServiceHostProperty());
		assertEquals("8080", getPluginServicePortProperty());
		} finally {
			setPluginServiceHostProperty(pluginServiceHostOrg);
			setPluginServicePortProperty(pluginServicePortOrg);
		}
	}
	
	private String getPluginServiceHostProperty() {
		return System.getProperty("config.pluginServiceHost");
	}
	
	private void setPluginServiceHostProperty(String host) {
		setProperty("config.pluginServiceHost", StringUtils.defaultString(host));
	}
	
	private void setProperty(String property, String value) {
		if (value == null) {
			System.clearProperty(property);
		} else {
			System.setProperty(property, value);
		}
	}
	
	private String getPluginServicePortProperty() {
		return System.getProperty("config.pluginServicePort");
	}
	
	private void setPluginServicePortProperty(String port) {		
		setProperty("config.pluginServicePort", port);
	}
	
	
}
