package com.dynatrace.easytravel.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;

/**
 * Collection of ZIP utilities
 *
 * @author philipp.grasboeck
 */
public class ZipUtils {

	private static final Logger log = LoggerFactory.make();

	private static String getRelativePath(File file, String root, boolean includeRootName) {
		String fullPath = file.getPath().replace('\\', '/');
		String rootPath = root + '/';
		int i = fullPath.indexOf(rootPath);
		return i < 0 ? fullPath : includeRootName ? fullPath.substring(i) : fullPath.substring(i + rootPath.length());
	}

	private static ZipOutputStream putFiles(File destFile, ZipOutputStream out, File[] files, String root, String fileExcludePattern, String dirExcludePattern, boolean includeRootName) throws IOException {
		for (File child : files) {
			if (child.isDirectory() && (dirExcludePattern == null || !child.getName().matches(dirExcludePattern))) {
				out = putFiles(destFile, out, child.listFiles(), root, fileExcludePattern, dirExcludePattern, includeRootName);
			} else if (child.isFile() && (fileExcludePattern == null || !child.getName().matches(fileExcludePattern))) {
				String entryName = getRelativePath(child, root, includeRootName);
				if (out == null) {
					out = new ZipOutputStream(new FileOutputStream(destFile));
				}
				out.putNextEntry(new ZipEntry(entryName));
				FileInputStream in = new FileInputStream(child);
				try {
					IOUtils.copy(in, out);
				} catch (IOException e) {
					log.warn("Error while adding " + child.getPath() + ": " + e.getMessage());
				} finally {
					IOUtils.closeQuietly(in);
				}
			}
		}
		return out;
	}

	/**
	 * Tries to zip the contents of dir into destFile, recursively.
	 *
	 * @param destFile   the resulting ZIP file. Make sure this file can be written to.
	 * @param dir        the directory.
	 * @param fileExcludePattern   a regex applied to file names to exclude.
	 * @param dirExcludePattern    a regex applied to dir names to exclude.
	 * @param includeRootName      if true, the dir name itself will be part of the ZIP entry paths.
	 * @return  true if a ZIP file was written.
	 * @throws IOException
	 * @author philipp.grasboeck
	 */
	public static boolean compressDir(File destFile, File dir, String fileExcludePattern, String dirExcludePattern, boolean includeRootName) throws IOException {
		ZipOutputStream out = null;
		try {
			out = putFiles(destFile, out, dir.listFiles(), dir.getName(), fileExcludePattern, dirExcludePattern, includeRootName);
		} finally {
			IOUtils.closeQuietly(out);
		}
		return out != null;
	}

	private static String getNamePart(String entryName) {
		int i = entryName.lastIndexOf('/');
		return (i != -1) ? entryName.substring(i + 1) : entryName;
	}

	/**
	 * Extract a ZipEntry from a zip file, copy it to a file and return it.
	 */
	public static File extractZipEntry(File zipFile, String name, File destDir) throws IOException {
		try (ZipFile zip = new ZipFile(zipFile);) {
			OutputStream out = null;
			try {
				ZipEntry entry = zip.getEntry(name);
				if (entry != null) {
					File tmp = new File(destDir, getNamePart(name));
					out = new FileOutputStream(tmp);
					InputStream in = zip.getInputStream(entry);
					IOUtils.copy(in, out);
					IOUtils.closeQuietly(in);
					IOUtils.closeQuietly(out);
					return tmp;
				}
			} finally {
				if (out != null)
					out.close();
			}

			return null;
		}
	}
}
