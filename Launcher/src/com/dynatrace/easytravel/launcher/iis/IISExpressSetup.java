package com.dynatrace.easytravel.launcher.iis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;

public class IISExpressSetup {
	
	private static final String EASYTRAVEL_RESOURCES_PATH = Directories.getResourcesDir().getAbsolutePath();
	private static final String EASYTRAVEL_CONFIG_PATH = Directories.getConfigDir().getAbsolutePath();
	
	private static final String PLACEHOLDER_SITE_NAME = "{site-name}";
	private static final String PLACEHOLDER_APP_PATH = "{application-path}";
	private static final String PLACEHOLDER_APP_PORT = "{application-port}";
	private static final String PLACEHOLDER_LOG_LOCATION = "{log-location}";
	private static final String PLACEHOLDER_TRACELOG_LOCATION = "{tracelog-location}";
	
	private final IISExpressConfigs config;
	private final int port;
	
	public IISExpressSetup(IISExpressConfigs config, int port) {
		this.config = config;
		this.port = port;
	}
	
	public void generateConfig() throws IOException {
		try (BufferedReader br = initializeBufferedReader();
				PrintWriter writer = initializeFileWriter()) {
			String line;
			while ((line = br.readLine()) != null) {
				replacePlaceholderIfExistAndWriteLine(writer, line);
			}
		}
	}
	
	private PrintWriter initializeFileWriter() throws IOException {
		return new PrintWriter(new FileWriter(EASYTRAVEL_CONFIG_PATH + "/" + config.getName(), false));
	}
	
	private BufferedReader initializeBufferedReader() throws FileNotFoundException {
		return new BufferedReader(new FileReader(EASYTRAVEL_RESOURCES_PATH + "/" + IISExpressConfigs.TEMPLATE.getName()));
	}
	
	private void replacePlaceholderIfExistAndWriteLine(PrintWriter writer, String line) throws IOException {
		line = line.replace(PLACEHOLDER_SITE_NAME, config.getSiteName());
		line = replaceAppDir(line);
		line = line.replace(PLACEHOLDER_APP_PORT, Integer.toString(port));
		line = line.replace(PLACEHOLDER_LOG_LOCATION, Directories.getLogDir().getCanonicalPath());
		line = line.replace(PLACEHOLDER_TRACELOG_LOCATION, Directories.getLogDir().getCanonicalPath());
		writer.println(line);
	}
	
	private String replaceAppDir(String line) throws IOException {
		String appDir = null;
		if(config.equals(IISExpressConfigs.B2B_FRONTEND_CONFIG)) {
			appDir = EasyTravelConfig.read().b2bFrontendDir;
		} else if(config.equals(IISExpressConfigs.PAYMENT_BACKEND_CONFIG)) {
			appDir = EasyTravelConfig.read().paymentBackendDir;
		}
		return (appDir != null) ? line.replace(PLACEHOLDER_APP_PATH, new File(Directories.getInstallDir(), appDir).getCanonicalPath()) : line;
	}

}
