package com.dynatrace.easytravel.constants;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;

public class BaseConstantsTest {

	private static final Logger LOGGER = LoggerFactory.make();

	@Test
	public void testInstantiate() {
		LOGGER.info("SystemProperties: " + BaseConstants.SystemProperties.AGENT_LOOKUP_DIR);
		LOGGER.info("SubDirectories: " + BaseConstants.SubDirectories.AGENT);
		LOGGER.info("CustomerFrontendArguments: " + BaseConstants.CustomerFrontendArguments.AJP_PORT);
		LOGGER.info("CmdArguments: " + BaseConstants.CmdArguments.PROPERTY_FILE);
		LOGGER.info("Browser: " + BaseConstants.Browser.BROWSER_FIREFOX);
		LOGGER.info("Browser-Choices: " + ArrayUtils.toString(BaseConstants.Browser.BROWSER_CHOICES));
		LOGGER.info("LoggerNames: " + BaseConstants.LoggerNames.ANT);
		LOGGER.info("UrlType: " + BaseConstants.UrlType.APACHE_B2B_FRONTEND);
		LOGGER.info("ProcedureId: " + BaseConstants.ProcedureId.APACHE_HTTPD_WEBSERVER);
		LOGGER.info("Plugin: " + BaseConstants.Plugins.DC_RUM_EMULATOR);
		LOGGER.info("PluginsDir: " + BaseConstants.PluginsDir.PLUGINS_BACKEND);
		LOGGER.info("Cassandra: " + BaseConstants.CassandraArgument.HOST);
		LOGGER.info("BusinessBackend: " + BaseConstants.BusinessBackend.Persistence.CASSANDRA);
		LOGGER.info("REST: " + BaseConstants.Images.HEADER_APM_EASY_TRAVEL);
		LOGGER.info("REST: " + BaseConstants.Labels.APM_SERVER);
	}

	@Test
	public void testEnums() {
		assertEquals(BaseConstants.UrlType.APACHE_B2B_FRONTEND, BaseConstants.UrlType.valueOf("APACHE_B2B_FRONTEND"));
		assertEquals(BaseConstants.ProcedureId.APACHE_HTTPD_WEBSERVER, BaseConstants.ProcedureId.valueOf("APACHE_HTTPD_WEBSERVER"));
	}

	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(BaseConstants.class);
		PrivateConstructorCoverage.executePrivateConstructor(BaseConstants.SystemProperties.class);
		PrivateConstructorCoverage.executePrivateConstructor(BaseConstants.SubDirectories.class);
		PrivateConstructorCoverage.executePrivateConstructor(BaseConstants.CustomerFrontendArguments.class);
		PrivateConstructorCoverage.executePrivateConstructor(BaseConstants.CmdArguments.class);
		PrivateConstructorCoverage.executePrivateConstructor(BaseConstants.Browser.class);
		PrivateConstructorCoverage.executePrivateConstructor(BaseConstants.LoggerNames.class);
		//PrivateConstructorCoverage.executePrivateConstructor(BaseConstants.UrlType.class);
		//PrivateConstructorCoverage.executePrivateConstructor(BaseConstants.ProcedureId.class);
		PrivateConstructorCoverage.executePrivateConstructor(BaseConstants.Plugins.class);
		PrivateConstructorCoverage.executePrivateConstructor(BaseConstants.PluginsDir.class);
		PrivateConstructorCoverage.executePrivateConstructor(BaseConstants.CassandraArgument.class);
		PrivateConstructorCoverage.executePrivateConstructor(BaseConstants.BusinessBackend.class);
		PrivateConstructorCoverage.executePrivateConstructor(BaseConstants.BusinessBackend.Persistence.class);
		PrivateConstructorCoverage.executePrivateConstructor(BaseConstants.Images.class);
		PrivateConstructorCoverage.executePrivateConstructor(BaseConstants.Labels.class);
	}
}
