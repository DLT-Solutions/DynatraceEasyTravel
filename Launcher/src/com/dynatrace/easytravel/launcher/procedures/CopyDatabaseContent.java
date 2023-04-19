/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: CopyDatabaseContent.java
 * @date: 23.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.launcher.procedures;

import static com.dynatrace.easytravel.launcher.misc.Constants.Misc.SETTING_COPY_DERBY_DATA_FROM;
import static java.lang.String.format;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;


/**
 *
 * @author stefan.moschinski
 */
class CopyDatabaseContent {

	private static final int BUFFER_SIZE = 2048;

	private static final Logger log = Logger.getLogger(CopyDatabaseContent.class.getName());
	private final File destination;

	CopyDatabaseContent(File destination) {
		this.destination = destination;
	}

	void copyDbDataFrom(String sourcePath) {
		File source = new File(sourcePath);
		log.info(String.format("Try to copy/unzip given Derby data repository '%s' to '%s'", source.getAbsolutePath(),
				destination.getAbsolutePath()));

		removeOldDataFrom(destination);

		if (sourcePath.toLowerCase().endsWith(".zip")) {
			unzipDbContent(source);
			return;
		} else {
			copyDataFrom(source);
		}

	}

	private static void removeOldDataFrom(File destination) {
		if (destination.isDirectory()) {
			cleanDirectory(destination);
		}
	}

	private void unzipDbContent(File source) {
		if (!source.isFile()) {
			throw new IllegalStateException(String.format(
					"The given '%s' setting '%s' does either not exist or is not a ZIP archive",
					SETTING_COPY_DERBY_DATA_FROM, source.getAbsolutePath()));
		}

		try {
			ZipFile zip = new ZipFile(source);
			try {
				Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();

				while (zipFileEntries.hasMoreElements())
				{
					ZipEntry entry = zipFileEntries.nextElement();
					log.fine("Unzipping " + entry);

					String currentEntry = StringUtils.removeStart(entry.getName(), destination.getName() + "/");

					if (currentEntry.isEmpty()) {
						log.info("Skipping main directory: " + entry.getName());
						continue;
					}

					File destFile = new File(destination, currentEntry);

					File destinationParent = destFile.getParentFile();

					if (destinationParent != null) {
						// make sure that needed dirs exist
						destinationParent.mkdirs();
					}

					if (!entry.isDirectory()) {
						try (BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
								// write the current file to disk
								FileOutputStream fos = new FileOutputStream(destFile);
								BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE);) {

							int currentByte;
							// establish buffer for writing file
							byte data[] = new byte[BUFFER_SIZE];

							// read and write until last byte is encountered
							while ((currentByte = is.read(data, 0, BUFFER_SIZE)) != -1) {
								dest.write(data, 0, currentByte);
							}
							dest.flush();
						}
					}
				}
			} finally {
				zip.close();
			}
		} catch (IOException e) {
			throw new IllegalStateException(format("Failed to unzip '%s' to '%s'", source.getAbsolutePath(),
					destination.getAbsolutePath()), e);
		}

	}

	private void copyDataFrom(File source) {
		if (!source.isDirectory()) {
			throw new IllegalStateException(String.format(
					"The given '%s' setting '%s' does either not exist or is not a directory",
					SETTING_COPY_DERBY_DATA_FROM, source.getAbsolutePath()));
		}

		makeDirectory(destination);
		copyData(source, destination);
	}

	private static void cleanDirectory(File destination) {
		try {
			FileUtils.cleanDirectory(destination);
		} catch (IOException e) {
			log.log(Level.SEVERE,"Cannot clear directory", e);
			throw new IllegalStateException(String.format("Cannot clear directory '%s'", destination.getAbsolutePath()));
		}
	}

	private static void makeDirectory(File destination) {
		if (destination.isDirectory())
			return;
		if (!destination.mkdirs())
			throw new IllegalStateException(String.format("Could not create directory '%s'", destination.getAbsolutePath()));
	}

	private static void copyData(File source, File destination) {
		try {
			FileUtils.copyDirectory(source, destination);
		} catch (IOException e) {
			throw new IllegalStateException(String.format("Could not copy directory '%s' to '%s'", source.getAbsolutePath(),
					destination.getAbsolutePath()));
		}
	}
}
