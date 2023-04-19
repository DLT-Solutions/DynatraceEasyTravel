package com.dynatrace.easytravel.util;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class MySqlVcapServiceTest {

	private static final String TEST_DATA_PATH = "../TravelTest/testdata";
	private static final Set<String> propertynames = Sets.newSet("config.databasePassword", "config.databaseUser", "config.internalDatabaseEnabled",
			"config.databaseDriver", "config.databaseUrl");
	private Map<String, String> storedproperties = Maps.newHashMap();
	
	@Before
	public void backupProperties() throws IOException {
		propertynames.forEach( name -> storedproperties.put(name, System.getProperty(name)));
	}
	
	@After
	public void restoreProperties() {		
		propertynames.forEach( name -> restoreSystemProperty(name) );
		EasyTravelConfig.resetSingleton();
		EasyTravelConfig config = EasyTravelConfig.read();
		assertThat( config.databaseUser, is("APP"));
	}
	
	private void restoreSystemProperty(String name) {
		String value = storedproperties.get(name);
		if( Strings.isNullOrEmpty(value) ) {
			System.clearProperty(name);
		} else {
			System.setProperty(name, value);		
		}
	}
	
	@Test
	public void test() throws IOException { 
			EasyTravelConfig config = EasyTravelConfig.read();

			config.databasePassword = "APP";
			config.databaseUser = "APP";
			config.internalDatabaseEnabled = true;
			config.databaseDriver = "org.apache.derby.jdbc.ClientDriver";
			config.databaseUrl = "jdbc:someurl";

			String vcapservices = FileUtils.readFileToString(new File(TEST_DATA_PATH, "/vcapservices.json"));
			System.setProperty("VCAP_SERVICES", vcapservices);
			assertThat(MySqlVcapService.parseEnv("easyTravel-Business*"), is(true));

			EasyTravelConfig.reload();
			config = EasyTravelConfig.read();

			assertThat( System.getProperty("config.databasePassword"), is("vcappassword"));
			assertThat( System.getProperty("config.databaseUser"), is("vcapusername"));
			assertThat( System.getProperty("config.internalDatabaseEnabled"), is("false"));
			assertThat( System.getProperty("config.databaseDriver"), is("com.mysql.jdbc.Driver"));
			assertThat( System.getProperty("config.databaseUrl"), is("jdbc:mysql://vcaphost/vcapservice"));
						
			assertThat( config.databasePassword, is("vcappassword"));
			assertThat( config.databaseUser, is("vcapusername"));
			assertThat( config.internalDatabaseEnabled, is(false));
			assertThat( config.databaseDriver, is("com.mysql.jdbc.Driver"));
			assertThat( config.databaseUrl, is("jdbc:mysql://vcaphost/vcapservice"));			
	}	
}
