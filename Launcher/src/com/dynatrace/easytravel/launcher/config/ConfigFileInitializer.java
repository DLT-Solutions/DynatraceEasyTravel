package com.dynatrace.easytravel.launcher.config;

import java.io.File;

import com.dynatrace.diagnostics.uemload.utils.RentalCarsGenerator;
import com.dynatrace.diagnostics.uemload.utils.UserFileGenerator;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.launcher.httpd.CertificateGenerator;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.PluginNotificationConfigFileGenerator;
import com.dynatrace.easytravel.util.ResourceFileReader;

import ch.qos.logback.classic.Logger;

/**
 * This class contains the initialization of files when using launcher and commandlineLauncher.
 *
 * @author tomasz.wieremjewicz
 * @date 20 lis 2017
 *
 */
public class ConfigFileInitializer {
	private static final Logger log = LoggerFactory.make();

	private ConfigFileInitializer() {
		throw new IllegalAccessError("Static method class is being instantiated");
	}

	public static void initializeForLauncher() {
		CertificateGenerator.generateCertificate();
		extendedUserFileGeneration();
		rentalCarsFileGeneration();
		pluginConfigFileGeneration();
	}

	public static void initializeForCommandlineLauncher() {
		CertificateGenerator.generateCertificate();
		extendedUserFileGeneration();
	}

	private static void extendedUserFileGeneration(){
    	if(!(new File(Directories.getConfigDir(), ResourceFileReader.EXTENDEDUSERS).exists())){
			UserFileGenerator generator = new UserFileGenerator();
			generator.generateUserFile();
		}
    }

	private static void rentalCarsFileGeneration(){
    	if(!(new File(Directories.getConfigDir(), ResourceFileReader.RENTALCARS).exists())){
			RentalCarsGenerator generator = new RentalCarsGenerator();
			generator.generateUserFile();
		}
    }

	private static void pluginConfigFileGeneration() {
		if(!(new File(Directories.getConfigDir(), ResourceFileReader.PLUGINNOTIFICATIONCONFIG).exists())){
			log.info("No template file for notifications found - generating a new one.");
			PluginNotificationConfigFileGenerator generator = new PluginNotificationConfigFileGenerator();
			generator.generateConfigFile();
		}
	}
}
