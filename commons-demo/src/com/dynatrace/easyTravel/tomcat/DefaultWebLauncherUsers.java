package com.dynatrace.easytravel.tomcat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Generate default WebLaucher user file
 * @author cwpl-rorzecho
 */
public class DefaultWebLauncherUsers {

	public static void create(File authenticatedUsersFile) throws IOException {
		PrintWriter writer = initializeFileWriter(authenticatedUsersFile);
		writeDefaultUserName(writer);
		closeFileWriter(writer);
	}

	private static void writeDefaultUserName(PrintWriter writer) {
		writer.println("admin,adminpass,demo");
	}

	private static PrintWriter initializeFileWriter(File authenticatedUsersFile) throws IOException {
		return new PrintWriter(new FileWriter(authenticatedUsersFile, true));
	}

	private static void closeFileWriter(PrintWriter writer) {
		writer.close();
	}
}
