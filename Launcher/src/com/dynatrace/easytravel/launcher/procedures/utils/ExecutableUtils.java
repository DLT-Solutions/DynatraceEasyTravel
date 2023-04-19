package com.dynatrace.easytravel.launcher.procedures.utils;

import java.io.File;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.dynatrace.easytravel.launcher.agent.OperatingSystem;

public class ExecutableUtils {
	private static final Logger LOGGER = Logger.getLogger(ExecutableUtils.class.getName());
	
	public static String getInstallDirDependingOs(String baseInstallDir) {
		String osPart = isUsedOsWindows() ? "Windows" : "Linux";
		
		Optional<String> fileName = Stream.of(osPart + "_x64", osPart + "_x86")
			.filter(name -> checkIfFileExists(baseInstallDir, name))
			.findFirst();
		
		if (fileName.isPresent()) {
			return fileName.get();
		}				
				
		throw new IllegalStateException("Installation directory not found for operating system " + osPart + " and install path " + baseInstallDir);
	}
		
	private static boolean isUsedOsWindows() {
		return OperatingSystem.pickUp() == OperatingSystem.WINDOWS;
	}
	
	private static boolean checkIfFileExists(String baseDir, String fileName) {
		File f = new File(baseDir, fileName);
		if(LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("Checking file " + f.getAbsolutePath() + " " + f.exists());
		}
		return f.exists();
	}
}
