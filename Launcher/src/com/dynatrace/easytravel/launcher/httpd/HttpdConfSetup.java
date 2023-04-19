package com.dynatrace.easytravel.launcher.httpd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.util.TextUtils;



/**
 * This class configures the Apache HTTPD file at runtime. The httpd.conf
 * file can be found in the /.dynaTrace/easyTravel x.x.x/config folder.
 *
 * @author stefan.moschinski
 *
 */
public class HttpdConfSetup {
	private static final Logger LOGGER = LoggerFactory.make();

	private static final String EASYTRAVEL_TMP_PATH = Directories.getTempDir().getAbsolutePath();
	private static final String EASYTRAVEL_CONFIG_PATH = Directories.getConfigDir().getAbsolutePath();

	public static void write(DtAgentConfig agentConfig, boolean isStartPhp) throws IOException {
		ApacheConf apacheConf = new ApacheConf();
		apacheConf.setStartPhp(isStartPhp);
		write(agentConfig, apacheConf);
	}

	public static void write(DtAgentConfig agentConfig, ApacheConf apacheConf) throws IOException {
		PrintWriter writer = initializeFileWriter();
		try {
			writeHeaderOfHttpdConf(writer, apacheConf);
			writeModules(agentConfig, writer, apacheConf);
			writeBasicHttpdConfProperties(writer);
			writeServerStatus(writer);
			writeDocumentRootDirectoryDirective(writer);
			writeRuxitAgentProperties(agentConfig, writer);
			writeExtFilterDefine(apacheConf, writer);
			writeMutexDirective(writer);
			VirtualHost.writeCustomerAndB2bVirtualHostEntries(writer, apacheConf);
			LogWriter.writeModLogRotateConfigToHttpdConf(writer);
			writeMimeTypesAndPidFileEntries(writer);

			LOGGER.info("New HTTPD conf file was created successfully in the folder " + EASYTRAVEL_CONFIG_PATH);
		} finally {
			writer.close();
		}
	}

	private static void writeServerStatus(PrintWriter writer) {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		
		if (EASYTRAVEL_CONFIG.apacheWebServerStatusPort > 0) 
		{
			writer.println();
			writer.println("# enable Apache Status page at http://localhost:" + EASYTRAVEL_CONFIG.apacheWebServerStatusPort + "/server-status");
			writer.println("# server info page at http://localhost:" + EASYTRAVEL_CONFIG.apacheWebServerStatusPort + "/server-info");
			writer.println("# and the Load Balancer page at http://localhost:" + EASYTRAVEL_CONFIG.apacheWebServerStatusPort + "/balancer-manager");
			writer.println("ExtendedStatus On");
			writer.println("LoadModule status_module modules/mod_status.so");
			writer.println("LoadModule info_module modules/mod_info.so");
			writeServerStatusLocationBlock(writer, "server-status");
			writeServerStatusLocationBlock(writer, "balancer-manager");
			writeServerStatusLocationBlock(writer, "server-info");
			writer.println();
		}
	}

	private static void writeServerStatusLocationBlock(PrintWriter writer, String endpoint) {
		writer.println("<Location /" + endpoint + ">");
		writer.println("    SetHandler " + endpoint);
		writer.println("    Require all granted");
		writer.println("</Location>");
	}

	private static PrintWriter initializeFileWriter() throws IOException {
		return new PrintWriter(new FileWriter(EASYTRAVEL_CONFIG_PATH
				+ "/httpd.conf", false));
	}

	private static void writeHeaderOfHttpdConf(PrintWriter writer, ApacheConf apacheConf) {
		writer.println(getApacheConfigEntry("ServerRoot", ApacheHttpdUtils.APACHE_OS_SPECIFIC_PATH));
		writer.println(getApacheConfigEntry("DocumentRoot", ApacheHttpdUtils.INSTALL_APACHE_PATH + "/htdocs"));
		writer.println(getApacheConfigEntry("ServerName", "EasyTravelWebserver"));
		writer.println(getApacheConfigEntry("DefaultRuntimeDir", ApacheHttpdUtils.APACHE_RUNTIME_DIR));
		writeListeningPortsOfApache(writer, apacheConf);
	}

	private static void writeListeningPortsOfApache(PrintWriter writer, ApacheConf apacheConf) {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		
		writer.println(getApacheConfigEntry("Listen", EASYTRAVEL_CONFIG.apacheWebServerPort));
		if (EASYTRAVEL_CONFIG.apacheWebServerB2bPort != EASYTRAVEL_CONFIG.apacheWebServerPort) {
			writer.println(getApacheConfigEntry("Listen", EASYTRAVEL_CONFIG.apacheWebServerB2bPort));
		}
		
		if (EASYTRAVEL_CONFIG.apacheWebServerProxyPort > 0) {
			writer.println(getApacheConfigEntry("Listen", EASYTRAVEL_CONFIG.apacheWebServerProxyPort));
		}

		if(EASYTRAVEL_CONFIG.apacheWebServerStatusPort > 0) {
			writer.println(getApacheConfigEntry("Listen", EASYTRAVEL_CONFIG.apacheWebServerStatusPort));
		}
		
		int[] extraListenPorts = apacheConf.getExtraListenPorts();
		if (extraListenPorts != null) {
			for (int listenPort : extraListenPorts) {
				writer.println(getApacheConfigEntry("Listen", String.valueOf(listenPort)));
			}
		}

		// SSL Connection for CustomerFrontend
		if (EASYTRAVEL_CONFIG.apacheWebServerSslPort > 0) {
			writer.println(getApacheConfigEntry("Listen", EASYTRAVEL_CONFIG.apacheWebServerSslPort));
		}
		//Angular
		writer.println(getApacheConfigEntry("Listen", EASYTRAVEL_CONFIG.angularFrontendApachePort));
	}

	private static void writeModules(DtAgentConfig agentConfig, PrintWriter writer, ApacheConf apacheConf) {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();

		writeDtAgentLoadModule(agentConfig, writer);
		writeUnixdModule(writer);
		LogWriter.addModLogRotateToModules(writer);
		Rewrite.writeModule(writer);
		if (apacheConf.isApacheSlowDown()) {
			FilterDefine.writeModule(writer);
		}
		// SSL module
		if (EASYTRAVEL_CONFIG.apacheWebServerSslPort > 0) {
			writer.println("LoadModule ssl_module modules/mod_ssl.so");
		}
	}

	private static void writeDtAgentLoadModule(DtAgentConfig agentConfig, PrintWriter writer) {
		try {
			final String agentPath = agentConfig.getAgentPath(Technology.WEBSERVER);

			if (agentPath == null) {
				return;
			} else {
				if (DtVersionDetector.isAPM()) {
					// ruxit is being used.
					final String dtAgentModuleEntry = "LoadModule ruxitagent_module \"" + agentPath + "\"";
					writer.println(dtAgentModuleEntry);
				} else {
					final String dtAgentModuleEntry = "LoadModule dtagent_module \"" + agentPath + "\"";
					writer.println(dtAgentModuleEntry);
				}
			}
		} catch (ConfigurationException e) {
			LOGGER.warn("Could not detect location of webserver agent. Please make sure that you provide an agent.", e);
		}
	}
	
	private static void writeUnixdModule(PrintWriter writer) {
		if(!ApacheHttpdUtils.isUsedOsWindows()) {
			writer.println("LoadModule unixd_module modules/mod_unixd.so");
		}
	}

	/**
	 * The values in the httpd.conf file that are not configured at runtime
	 * are copied using this method.
	 */
	private static void writeBasicHttpdConfProperties(PrintWriter writer) throws FileNotFoundException, IOException {
		try (BufferedReader reader = new BufferedReader(
				new FileReader(EASYTRAVEL_CONFIG_PATH + "/plain_httpd.conf"));) {
			String line;

			while ((line = reader.readLine()) != null) {
				// replace customer-frontend-url in case we start it remotely,
				// i.e. Demo-In-The-Cloud
				writer.println(line);
			}
			reader.close();
		}
	}
	
	private static void writeDocumentRootDirectoryDirective(PrintWriter writer) {
		String directoryDirectiveHeader = String.format("<Directory \"%s\">", ApacheHttpdUtils.INSTALL_APACHE_PATH + "/htdocs");
		writer.println();
		writer.println(directoryDirectiveHeader);
		writer.println("Require all granted");
		writer.println("</Directory>");
		writer.println();
	}	
	
	/**
	 * Adds configuration properties for custom ruxit agent
	 * 
	 *  RuxitAgentConfig server=SECRET,tenant=1,tenanttoken=1
	 *  
	 * @param agentConfig
	 * @param writer
	 */
	private static void writeRuxitAgentProperties(DtAgentConfig agentConfig, PrintWriter writer){
		if (!DtVersionDetector.isAPM()) {
			return;
		}
		
		String agentPath = null; 
		try {
			agentPath = agentConfig.getAgentPath(Technology.WEBSERVER);
		} catch (ConfigurationException e) {
			LOGGER.info("Could not detect location of webserver agent. ruxit agent configuration will be not added to the httpd.conf file", e);
		}
		if (agentPath == null) {
			return; 
		}

		EasyTravelConfig config = EasyTravelConfig.read();
		
		writer.println();
		writer.println("#Ruxit agent properties");
		writer.println(TextUtils.merge("RuxitAgentConfig server={0},tenant={1},tenanttoken={2}", config.apmServerWebURL, config.apmTenant, config.apmTenantToken));
		writer.println();
	}

	private static void writeExtFilterDefine(ApacheConf apacheConf, PrintWriter writer) {
		if (apacheConf.isApacheSlowDown()) {
			FilterDefine.writeExtFilterDefine(writer);
			FilterDefine.writeFilterLocation(writer);
		}
	}	
	
	private static void writeMutexDirective(PrintWriter writer) {
		if (!ApacheHttpdUtils.isUsedOsWindows()){
			writer.println("Mutex posixsem");
		}
	}

	private static void writeMimeTypesAndPidFileEntries(PrintWriter writer) {
		final String pidFilePath = ("PidFile \""
				+ EASYTRAVEL_TMP_PATH + File.separator + "httpd.pid\"");

		final String mimeTypesPath = ("TypesConfig \""
				+ EASYTRAVEL_CONFIG_PATH + File.separator + "mime.types\"");

		writer.println(mimeTypesPath);
		writer.println(pidFilePath);
	}

	private static String getApacheConfigEntry(String directiveName, String attribute) {
		return String.format("%s \"%s\"", directiveName, attribute);
	}

	private static String getApacheConfigEntry(String directiveName, int attribute) {
		return String.format("%s %d", directiveName, attribute);
	}
}
