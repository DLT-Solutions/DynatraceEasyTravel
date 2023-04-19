package com.dynatrace.easytravel.util;

import java.io.File;
import java.io.IOException;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;

public class DirUtils {

	private static final Logger log = LoggerFactory.make();

	public static final String WILDCARD = "*";
	public static final String PATH_SEPARATOR = "/";

	/**
	 * Find the denoted path start at file, bubbling ob their parents until file system root
	 * or the path has been found.
	 * Wildcards are supported as a whole (*), start or end of a path element.
	 *
	 * @param parent
	 * @param path
	 * @return
	 * @author philipp.grasboeck
	 */
	public static File findPathRelativeTo(File parent, String path) {
		if (log.isDebugEnabled()) log.debug("Search for: " + path + ", starting here: " + parent);

		if (!parent.exists()) {
			return null;
		}

		int i = path.indexOf(PATH_SEPARATOR);
		File result = (i != -1)
			? findPathRelativeTo0(parent, path.substring(0, i), path.substring(i + 1), true)
			: findPathRelativeTo0(parent, path, null, true);

		if (log.isDebugEnabled()) log.debug("Result file: " + result);
		return result;
	}

	private static File findPathRelativeTo0(File file, String firstPathElement, String restPath, boolean moveup) {
		File[] children = file.listFiles();

		if (children != null) {
			if (log.isDebugEnabled()) log.debug("Searching in: " + file + ", firstPathElement=" + firstPathElement + ", restPath=" + restPath);
//			System.out.println("Searching in: " + file + ", firstPathElement=" + firstPathElement + ", restPath=" + restPath);
			for (File child : children) {

				if (match(child.getName(), firstPathElement)) {
					if (restPath == null) {
						return child; // finished - found it
					}
					int i = restPath.indexOf(PATH_SEPARATOR);
					File result = (i != -1)
						? findPathRelativeTo0(child, restPath.substring(0, i), restPath.substring(i + 1), false)
						: findPathRelativeTo0(child, restPath, null, false);
					if (result != null) {
						return result; // recursive call found it
					}
				}
			}
		}

		if (moveup) {
			File parent = file.getParentFile(); // search in parent
			if (parent != null) {
				return findPathRelativeTo0(parent, firstPathElement, restPath, true);
			}
		}

		return null; // didn't find it
	}

	private static boolean match(String text, String pattern) {
		if (pattern.equals(WILDCARD)) {
			return true; // wildcard
		}
		if (pattern.startsWith(WILDCARD)) {
			return text.endsWith(pattern.substring(1));
		}
		if (pattern.endsWith(WILDCARD)) {
			return text.startsWith(pattern.substring(0, pattern.length() - 1));
		}
		return text.equals(pattern); // exact match
	}

	public static File findDir(String dirName, String... relativePaths) {

		for (String relativePath : relativePaths) {

			File parent;
			try {
				parent = new File(relativePath).getCanonicalFile();
			} catch (IOException e) {
				parent = new File(relativePath).getAbsoluteFile();
			}

			if (log.isDebugEnabled()) log.debug("Search for dir: " + dirName + ", starting here: " + parent);

			File file = new File(parent, dirName);
			if (file.exists()) {
				return file;
			}
		}

		return null;
	}
}
