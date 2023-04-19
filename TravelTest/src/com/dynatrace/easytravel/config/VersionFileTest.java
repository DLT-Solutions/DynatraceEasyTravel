package com.dynatrace.easytravel.config;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import org.junit.Test;

import com.dynatrace.easytravel.util.ConfigurationProvider;


import static org.junit.Assert.*;

public class VersionFileTest {
    private static Logger log = Logger.getLogger(VersionFileTest.class.getName());

    @Test
	public void testVersionFile() throws Exception {
		Properties props = ConfigurationProvider.readPropertyFile("easyTravel");

		Version version = ConfigurationProvider.createPropertyBean(Version.class, props, "version");
		log.info("Version object: " + version);
		log.info("Version date: " + version.getBuilddate());
	}


	@Test
	public void testVersionNumbersEquals() throws IOException {
		String localVersion = "2.0.0.1119";
		String remoteVersion = "2.0.0.1119";

		Version local = Version.read(localVersion);
		Version remote = Version.read(remoteVersion);

		assertTrue(local.equals(remote));
	}

	@Test
	public void testVersionNumbersNotEquals() {
		String localVersion = "2.0.0.1120";
		String remoteVersion = "2.0.0.1119";

		Version local = Version.read(localVersion);
		Version remote = Version.read(remoteVersion);

		assertFalse(local.equals(remote));

	}

}
