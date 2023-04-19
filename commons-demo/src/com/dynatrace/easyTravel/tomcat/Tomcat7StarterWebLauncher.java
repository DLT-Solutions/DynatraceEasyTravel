package com.dynatrace.easytravel.tomcat;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.dynatrace.easytravel.config.Directories;


public class Tomcat7StarterWebLauncher extends Tomcat7Starter {

	private static final Logger logger = Logger.getLogger(Tomcat7StarterWebLauncher.class.getName());

	private File webDir = Directories.getWebAppDir();
	private File tempDir = new File(Directories.getTempDir(), "webapp");

	public Tomcat7StarterWebLauncher() {
		try {
			FileUtils.copyDirectory(webDir, tempDir);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not copy " + webDir + " to " + tempDir);
		}
	}

	@Override
	protected String getWebDir(String currentDir) throws IOException {
		return Directories.getTempDir().getCanonicalPath();
	}
}
