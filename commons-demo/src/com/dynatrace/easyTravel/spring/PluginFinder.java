/*****************************************************
  *  dynaTrace Diagnostics (c) dynaTrace software GmbH
  *
  * @file: JarRunner.java
  * @date: 08.04.2010
  * @author: dominik.stadler
  *
  */

package com.dynatrace.easytravel.spring;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;

import org.apache.commons.lang3.ArrayUtils;

import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Helper class which retrieves a list of plugins from the current jars on the classpath.
 *
 * @author dominik.stadler
 *
 */
public class PluginFinder {
    private static final Logger log = LoggerFactory.make();

	private static final String BUNDLE_PLUGIN_DIR = "Plugin-Path"; //$NON-NLS-1$
	private static final String BUNDLE_CLASS_PATH = "Class-Path";

	/**
	 * Retrieve the list of plugins from attributes in the jar-files on the classpath.
	 *
	 * It first looks in the current jar-file and all jar-files that are found on the
	 * classpath in the MANIFEST.MF for a property 'Plugin-Path' which is a space-separated
	 * list of directories that are searched for jar-files
	 *
	 * The resulting list contains all jar-files and directories that are found there.
	 * Additionally any jar-file which is listed in the property 'Class-Path' in the MANIFEST.MF
	 * of the loaded jar-files is included, if it actually exists.
	 *
	 * @return A list with all jar-files and directories that are found.
	 * @throws IOException
	 */
	
	public static List<String> getPlugins(@SuppressWarnings("rawtypes") Class myClass) throws IOException {
		String pluginDirNames = retrieveManifestAttributeFromAllJars(BUNDLE_PLUGIN_DIR, myClass);
		//URLClassLoader loader = (URLClassLoader)Thread.currentThread().getContextClassLoader();
		if(pluginDirNames == null || pluginDirNames.isEmpty()) {
			log.info("No location for plugin directories found, did not find '" + BUNDLE_PLUGIN_DIR + "' defined in any jar, plugins will not be loaded.");
			return Collections.emptyList();
		}
		List<String> validDirs = new ArrayList<String>();
		for(String pluginDirName : pluginDirNames.split(" ")) {
			File file = new File(pluginDirName);
			if(!file.exists() || !file.isDirectory()) {
				log.warn("Specified plugin directory '" + pluginDirName + "' could not be found or is not a directory, plugins will not be loaded.");
			} else {
				// build a chain of loaders for each directory that is specified
				log.info("Adding directory '" + pluginDirName + "' as additional classpath/plugin directory");
				validDirs.add(pluginDirName);
			}
		}
		return getJarURLs(validDirs, true);
	}

	/**
	 * Searches the given directories for Jars and returns the found jars as a list of urls.
	 * @param classPathDirectories
	 * @return urls to the found jars, or an empty {@link List} if nothing was found or the given array was empty or <code>null</code>.
	 * @throws IOException
	 */
	public static List<String> getClasspathJars(String[] classPathDirectories)  throws IOException {
		if (ArrayUtils.isEmpty(classPathDirectories)) {
			return Collections.emptyList();
		}
		List<String> validClasspathDirs = new ArrayList<String>();
		for (String dir : classPathDirectories) {
			File f = new File (dir);
			if (f.exists() && f.isDirectory()) {
				validClasspathDirs.add(dir);
				log.info("Adding directory '" + dir + "' as additional classpath directory");
			} else {
				log.warn("Specified classpath directory '" + dir + "' not found or is not a directory.");
			}
		}
		return getJarURLs(validClasspathDirs, false);
	}


	/**
	 *
	 * @param pluginDirNames
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	private static List<String> getJarURLs(List<String> pluginDirNames, boolean resolveDependencies) throws IOException, MalformedURLException {
		if(pluginDirNames == null || pluginDirNames.isEmpty()) {
			log.info("Did not find valid locations for plugins");
			return Collections.emptyList();
		}
		List<String> urls = new ArrayList<String>();

		for (String pluginDir : pluginDirNames) {
			if(!new File(pluginDir).exists()) {
				throw new IOException("Specified plugin directory '" + pluginDir + "' does not exist.");
			}
			if(!new File(pluginDir).isDirectory()) {
				throw new IOException("Specified plugin directory '" + pluginDir + "' is not a directory.");
			}

			// add the directory itself as classpath
			urls.add(new File(pluginDir).toURI().toURL().toString());

			// find all jars in "plugin" dir
			File file = new File(pluginDir);
			File[] listFiles = file.listFiles(new JarFilenameFilter() );

			if (listFiles != null) {
				for (File f : listFiles) {
					//URL url = new URL("file", "/", f.getAbsolutePath());
					log.info("Adding plugin/classpath-jar: " + f);
					urls.add(f.toURI().toURL().toString());
					if (resolveDependencies) {
						// add all libs referenced in the jar-file
						List<String> urls2 = getClasspathLibraries(f);
						urls.addAll(urls2);
					}
				}
			}
		}
		return urls;
	}


	private static class JarFilenameFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			// exclude some, but read all others
			return name.endsWith(".jar");
		}
	}

	/**
	 * Retrieves the "Class-Path" attribute from the manifest of the provided jarfile
	 * and returns a list of all classpath elements found there.
	 *
	 * @param jarfile
	 * @return
	 * @throws IOException
	 */
	public static List<String> getClasspathLibraries(File jarfile) throws IOException {
		List<String> urls2 = new ArrayList<String>();
		String name = retrieveManifestAttribute(jarfile.toURI().toURL(), BUNDLE_CLASS_PATH);
		if(name != null) {
			for(String path : name.split(" ")) {
				if(!new File(path).exists()) {
					if(!new File("../" + path).exists()) {
						// workaround for plugins that depend on themselves (dbspamming -> wardeployment)
						if(!new File("../plugins-backend/" + path).exists()) {
							log.warn("Could not find path: " + path + " (required by jar'" + jarfile + "')");
							continue;
						}

						path = "../plugins-backend/" + path;
					} else {
						path = "../" + path;
					}
				}

				log.debug("Adding contained path from plugin-jar: " + path);
				urls2.add(new File(path).toURI().toURL().toString());
			}
		}
		return urls2;
	}

	private static String retrieveManifestAttributeFromAllJars(String attribute, @SuppressWarnings("rawtypes") Class myClass) throws MalformedURLException, IOException {
		ProtectionDomain protectionDomain = myClass.getProtectionDomain();
		CodeSource codeSource = protectionDomain.getCodeSource();
		URL rootUrl = codeSource.getLocation();
		
		String name = retrieveManifestAttribute(rootUrl, attribute);
		if(name != null) {
			return name;
		}

		if(PluginFinder.class.getClassLoader() instanceof URLClassLoader) {
			URL[] urls = ((java.net.URLClassLoader)PluginFinder.class.getClassLoader()).getURLs();
			for(URL url : urls) {
				name = retrieveManifestAttribute(url, attribute);
				if(name != null) {
					return name;
				}
			}
		}

		log.debug("Did not find attribute '" + attribute + ", none of the jar files on the classpath did contain it.");
		return null;
	}

	private static String retrieveManifestAttribute(URL url, String attribute) throws MalformedURLException, IOException {
		if(log.isDebugEnabled()) {
			log.debug("Looking at URL '" + url + "' for attribute '" + attribute + "'");
		}

		// Note: WebStart uses names without ".jar", but we don't observe that here for now
		if (url.getFile().toLowerCase().endsWith(".jar"))
		{
			URL rootJarUrl = new URL("jar", "", url + "!/"); //$NON-NLS-1$ //$NON-NLS-2$
			JarURLConnection uc = (JarURLConnection) rootJarUrl.openConnection();
			Attributes attr = uc.getMainAttributes();

			if(attr == null || attr.getValue(attribute) == null) {
				return null;
			}
			return attr.getValue(attribute);
		}

		return null;
	}
}
