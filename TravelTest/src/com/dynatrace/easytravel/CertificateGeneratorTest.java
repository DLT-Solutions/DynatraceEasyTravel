package com.dynatrace.easytravel;

import static org.junit.Assert.*;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.httpd.CertificateGenerator;

public class CertificateGeneratorTest {
	
    @Before
    public void installDirCorrection() {
        System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");
    }

	@Test
	public void testGenerateCertificate() {

		if (SystemUtils.IS_OS_LINUX) {
			
			String ex =	CertificateGenerator.executeCommand("openssl version");
			Assume.assumeTrue(ex != "ERROR");
		}
	
		EasyTravelConfig config = EasyTravelConfig.read();
		config.apacheWebServerSslHost = "123.123.123.123";
		CertificateGenerator.generateCertificate();
		String output = CertificateGenerator.inspectCertificate();
		assertTrue(output.contains("DNS:123.123.123.123"));
		EasyTravelConfig.resetSingleton();
	}
}
