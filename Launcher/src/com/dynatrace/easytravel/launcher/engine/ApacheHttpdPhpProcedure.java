package com.dynatrace.easytravel.launcher.engine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.httpd.ApacheConf;
import com.dynatrace.easytravel.launcher.httpd.ApacheHttpdUtils;
import com.dynatrace.easytravel.launcher.mysqld.MysqlUtils;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.util.TextUtils;

/**
 *
 * Add some extra settings to support PHP applications
 *
 * @author chun-ting.chen
 *
 */
public class ApacheHttpdPhpProcedure extends ApacheHttpdProcedure {
	private static final String EASYTRAVEL_CONFIG_PATH = Directories.getConfigDir().getAbsolutePath();
	private static final File PHP_INI = new File(EASYTRAVEL_CONFIG_PATH, "php.ini");
	private static final Logger LOGGER = Logger.getLogger(ApacheHttpdPhpProcedure.class.getName());
	private static final String APACHE_DIR_DEPENDING_OS = ApacheHttpdUtils.getApacheInstallDirForUsedOs();
	private static final String PHP_WIN_MODULE_WINDOWS = "/" + APACHE_DIR_DEPENDING_OS + "/php8apache2_4.dll";
	private static final String PHP_MODULE_LINUX = "/" + APACHE_DIR_DEPENDING_OS + "/libphp.so";
	private static final String PHP_FILE_PATH = Directories.getPhpDir().getAbsolutePath();
	private static final File PHP_INI_CONF = new File(PHP_FILE_PATH, "plain_php.ini");
	private static final File PHP_INI_CONF_WIN = new File(PHP_FILE_PATH, "plain_php_win.ini");
	private static PrintWriter writer;
	private static boolean phpAgentFound = true;


	/**
	 * inherit from the ApacheHttpdProcedure class, create an Apache HTTP server instance,
	 * and set up PHP to work with Apache/2.2.18
	 * @param mapping
	 * @throws CorruptInstallationException
	 */
	public ApacheHttpdPhpProcedure(ProcedureMapping mapping)
			throws CorruptInstallationException {
		super(mapping);

		adaptHttpdConfToSupportPhp();
	}

	@Override
	protected void modifyApacheConf(ApacheConf apacheConf) {
		apacheConf.setStartPhp(true);

		super.modifyApacheConf(apacheConf);
	}

	/**
	 * check if the php.ini exists and set up PHP in httpd.conf
	 */
	private void adaptHttpdConfToSupportPhp() {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		if (EASYTRAVEL_CONFIG.apacheWebServerUsesGeneratedPhpIni ||
				isPHPIniFileMissing()) {
			try {
					if(OperatingSystem.isCurrent(OperatingSystem.LINUX)){	
						FileUtils.copyFile(PHP_INI_CONF, PHP_INI);
					}else{
						FileUtils.copyFile(PHP_INI_CONF_WIN, PHP_INI);
					}
					writeHttpdConfig();
				} catch (IOException e) {
					LOGGER.log(Level.WARNING,
							"Could not set up php.ini file", e);
				}
		}
	}

	private boolean isPHPIniFileMissing() {
		return !PHP_INI.exists();
	}

	private static void writeHttpdConfig() throws IOException {
		adjustHTTPDConfFile();
		if(OperatingSystem.isCurrent(OperatingSystem.LINUX)){		 
			adjustPHPIniFile();
		}else{		
			adjustPHPIniFileWindows();
		}	
	}
	
	static void adjustHTTPDConfFile() throws IOException {
		EasyTravelConfig config = EasyTravelConfig.read();
		
		if (!config.apacheWebServerUsesGeneratedHttpdConfig) {
			LOGGER.fine(TextUtils.merge("httpd.conf will not be modified (apacheWebServerUsesGeneratedHttpdConfig={0})", config.apacheWebServerUsesGeneratedHttpdConfig));
			return;
		}

		try {
			initializeFileWriter();
			writePHPRatingConf();
			writePHPBlogConf();
			writeModules();
			LOGGER.info("New HTTPD conf file was created successfully in the folder " + EASYTRAVEL_CONFIG_PATH);
		} finally {
			closeFileWriter();
		}
	}

	private static void initializeFileWriter() throws IOException {
		writer = new PrintWriter(new FileWriter(EASYTRAVEL_CONFIG_PATH
				+ "/httpd.conf", true));
		writer.println("\n\n#PHP Settings\n");
	}

	/**
	 * adjust the path of extension_dir based on the current location of PHP
	 * @throws IOException
	 */
	public static void adjustPHPIniFile() throws IOException {
		writer = new PrintWriter(new FileWriter(PHP_INI, true));
		writer.println(getApacheConfigEntry("\nextension_dir=", ApacheHttpdUtils.getPhpExtPathForUsedOs()));
		writer.println(getApacheConfigEntry("mysqli.default_socket=", MysqlUtils.MySQL_SOCKET));

		// add dtagent
		DtAgentConfig config = getAgentCfg();

		String agentPath;
		try {
			phpAgentFound = true;
			agentPath = config.getAgentPath(Technology.WEBPHPSERVER);
			if(agentPath != null) {
				writer.println(getApacheConfigEntry("extension=", agentPath));
			}
		} catch (ConfigurationException e) {
			LOGGER.log(Level.WARNING, "PHP agent not configured correctly", e);
			phpAgentFound = false;
		}

		writeRuxitAgentProperties(writer, false);

		writer.close();

	}

	public static void adjustPHPIniFileWindows() throws IOException {
		writer = new PrintWriter(new FileWriter(PHP_INI, true));
		writer.println(getApacheConfigEntry( "\nextension_dir=", ApacheHttpdUtils.getPhpExtPathForUsedOs()));
		writer.println(getApacheConfigEntry( "\nupload_tmp_dir=", ApacheHttpdUtils.getPhpTmpPathForUsedOs()));
		writer.println(getApacheConfigEntry( "\nsession.save_path=", ApacheHttpdUtils.getPhpTmpPathForUsedOs()));
		writer.println(getApacheConfigEntry( "\nxdebug.profiler_output_dir=", ApacheHttpdUtils.getPhpTmpPathForUsedOs())); 
		writer.println(getApacheConfigEntry( "mysql.default_socket=", MysqlUtils.MySQL_SOCKET));
		//config.apacheWebServerAgent 
		// add dtagent
		
		DtAgentConfig config = getAgentCfg();		
		String agentPath;
		try {
			phpAgentFound = true;
			agentPath = config.getAgentPath(Technology.WEBPHPSERVERWIN);
 
			if(agentPath != null) {
				 writer.println(getApacheConfigEntry("extension=",agentPath));
			}
		} catch (ConfigurationException e) {
			LOGGER.log(Level.WARNING, "PHP agent not configured correctly", e);
			phpAgentFound = false;
		}
 
		writeRuxitAgentProperties(writer, true);
		
		writer.close();

	}

	@Override
	public boolean agentFound() {
		// make it visible in the UI that the PHP Agent is not found
		if(!phpAgentFound) {
			return false;
		}

		return super.agentFound();
	}
	
	/**
	 * Generate configuration for custom ruxit php agnet
	 * 
	 * phpagent.server="SECRET"
	 * phpagent.tenant="1"
	 * phpagent.tenanttoken="1"
	 * 
	 * @param writer
	 * @param isWindows
	 */
	private static void writeRuxitAgentProperties(PrintWriter writer, boolean isWindows) {
		if (!DtVersionDetector.isAPM()) {
			return;				
		}
		
		DtAgentConfig agentConfig = getAgentCfg();
		Technology tech = (isWindows ? Technology.WEBPHPSERVERWIN : Technology.WEBPHPSERVER);
		String agentPath = null; 
		try {			
			agentPath = agentConfig.getAgentPath(tech);
		} catch (ConfigurationException e) {
			LOGGER.log(Level.INFO,
					"Could not detect location of php agent. ruxit agent configuration will not be added to the php.ini file", e);
		}
		if (agentPath == null) {
			return; 
		}
		
		EasyTravelConfig config = EasyTravelConfig.read();
		writer.println("#Ruxit agent properties");
		writer.println(TextUtils.merge("phpagent.server=\"{0}\"", config.apmServerWebURL));
		writer.println(TextUtils.merge("phpagent.tenant=\"{0}\"", config.apmTenant));
		writer.println(TextUtils.merge("phpagent.tenanttoken=\"{0}\"", config.apmTenantToken));		
	}
	
	private static DtAgentConfig getAgentCfg() {
		EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		DtAgentConfig agentConfig = new DtAgentConfig(
				null,
				EASYTRAVEL_CONFIG.phpAgent,
				null,
				EASYTRAVEL_CONFIG.phpEnvArgs);
		return agentConfig;
	}

	private static void writeModules() {
		writePhpModule();
	}

	private static void writePhpModule() {

		String phpModuleSubPath = PHP_WIN_MODULE_WINDOWS;
		if (OperatingSystem.isCurrent(OperatingSystem.LINUX)) {
			phpModuleSubPath = PHP_MODULE_LINUX;
		}

		final String phpModuleEntry = "LoadModule php_module \"" +  PHP_FILE_PATH + phpModuleSubPath + "\"";
		final String phpInitialDir = "PHPIniDir \"" + EASYTRAVEL_CONFIG_PATH + "\"";
		final String phpHandler = "AddHandler application/x-httpd-php .php .html .php3";
			if (OperatingSystem.isCurrent(OperatingSystem.WINDOWS)) {
				final String loadFileEntry = getApacheConfigEntry("Loadfile ",PHP_FILE_PATH + "/" + APACHE_DIR_DEPENDING_OS + "/libpq.dll");
				writer.println(loadFileEntry); 
			 }
		writer.println(phpModuleEntry);
		writer.println(phpInitialDir);
		writer.println(phpHandler);

	}

	private static void writePHPRatingConf() {
		writer.println(getApacheConfigEntry("Alias /rating", ApacheHttpdUtils.INSTALL_APACHE_PATH + "/htdocs/rating"));
		writer.println(getApacheConfigEntry("<Directory", ApacheHttpdUtils.INSTALL_APACHE_PATH + "/htdocs/rating") + ">");
		writer.println( "  Header set Access-Control-Allow-Origin \"*\"");
		writer.println( "  Options -Indexes +FollowSymLinks +MultiViews +ExecCGI"); // -Indexes, disable PHP directory browsing
		writer.println( "  AllowOverride All");
		writer.println( "  Require all granted");
		writer.println( "</Directory>");
	}
	
	private static void writePHPBlogConf() {
		writer.println("\n\n#PHP Blog in wordpress\n");
		writer.println(getApacheConfigEntry("Alias /blog", ApacheHttpdUtils.INSTALL_APACHE_PATH + "/htdocs/blog"));
		writer.println(getApacheConfigEntry("<Directory", ApacheHttpdUtils.INSTALL_APACHE_PATH + "/htdocs/blog") + ">");
		writer.println( "  Header set Access-Control-Allow-Origin \"*\"");
		writer.println( "  Options -Indexes +FollowSymLinks +MultiViews +ExecCGI"); // -Indexes, disable PHP directory browsing
		writer.println( "  AllowOverride All");
		writer.println( "  Require all granted");
		writer.println( "</Directory>");
		writer.println( "\n\nDirectoryIndex index.html index.php");
	}
	 
	
	private static void closeFileWriter() {
		if (writer != null) {
			writer.close();
			writer = null;
		}
	}

	private static String getApacheConfigEntry(String directiveName, String attribute) {
		return String.format("%s \"%s\"", directiveName, attribute);
	}
}
