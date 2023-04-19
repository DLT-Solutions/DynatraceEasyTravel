package com.dynatrace.easytravel.util;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import com.dynatrace.easytravel.util.RegexUtils.ScanVisitor;

public class ConfigurationProvider {

	private static final Logger log = Logger.getLogger(ConfigurationProvider.class.getName());

	private static final String PROPERTIES_SUFFIX = ".properties";

	public static <T> T createPropertyBean(Class<T> clazz, String fileName, String namespace)
	{
		try {
			Properties props = readPropertyFile(fileName);
			return createPropertyBean(clazz, props, namespace);
		} catch (Exception e) {
			log.severe(e.getMessage());
			throw new Error(e);
		}
	}

    /**
     * Try to find the resource by the following approaches (ordered):
     * <ol>
     * <li>interpret <code>file</code> as path name (external file)</li>
     * <li>system class loader (internal file)</li>
     * <li>thread context class loader (internal file)</li>
     * </ol>
     *
     * @param file
     * @return
     */
    public static URL getResource(String file) {
        URL url = getExternalResource(file);
        if (url != null) {
            return url; // use external file
        }

        return getInternalResource(file);
	}

    /**
     * Loads a resource from the environment. Tries the system class loader and the thread context
     * class loader.
     *
     * @param fileName the name of the resource file
     * @return the URL to the resource file or <code>null</code> if it could not be found
     * @author martin.wurzinger
     */
    private static URL getInternalResource(String fileName) {
        URL url = ClassLoader.getSystemResource(fileName);
        if (url != null) {
            return url;
        }

        return Thread.currentThread().getContextClassLoader().getResource(fileName);
    }

    /**
     * Get the URL to an external file.
     *
     * @param file
     * @return
     * @author martin.wurzinger
     */
    private static URL getExternalResource(String file) {
        File resource = new File(file);
        if (resource.exists()) {
            try {
                return resource.toURI().toURL();
            } catch (MalformedURLException e) {
                log.log(Level.SEVERE, "Unable to retrieve URL from file.", e);
                return null;
            }
        }

        return null;
    }

	public static <T> T createPropertyBean(Class<T> clazz, Map<?,?> propertyMap, String namespace) throws InstantiationException, IllegalAccessException
	{
		T target = clazz.newInstance();
		MvelUtils.injectProperties(target, propertyMap, namespace);
		return target;
	}

	/**
	 * Reads properties from the denoted file.
	 * Note, this supports recursive properties with the Ant-like syntax:
	 * my.property=abc
	 * my.other.property=xyz${my.property}
	 *
	 * @param fileName
	 * @return
	 * @throws IOException
	 * @author philipp.grasboeck
	 */
	public static Properties readPropertyFile(String fileName) throws IOException {
		return readPropertyFile(fileName, true);
	}

	public static Properties readPropertyFile(String fileName, boolean replaceRecursive) throws IOException {
		if (!fileName.endsWith(PROPERTIES_SUFFIX)) {
			fileName += PROPERTIES_SUFFIX;
		}

        URL url = getResource(fileName);
        if (url == null) {
            throw new IllegalArgumentException("Resource not found: " + fileName);
        }

		if (log.isLoggable(Level.FINE)) {
            log.fine("Property file: " + url);
        }

		final Properties props = new Properties();
		InputStream in = url.openStream();
		try {
			props.load(in);
		} finally {
			in.close();
		}
		if (replaceRecursive) {
			overrideProperties(props, System.getProperties());
			replaceRecursiveProperties(props, System.getProperties());
			replaceRecursiveProperties(props);
		}
		return props;
	}

	public static void overrideProperties(Map<Object, Object> map, Map<Object, Object> source) {
		for (Map.Entry<Object, Object> entry : map.entrySet()) {
			Object value = source.get(entry.getKey());
			if (value != null) {
				log.info("Overriding config value " + entry.getKey() + " with system property value " + value);
				entry.setValue(value);
			}
		}
	}

	// the regex that caputers ${property}
	// this regex supports for nesting: ${prefix-${middle}-postfix}
	// it also supports to define optional substring indices after ":", e.g. ${myvar:3} or ${myvar:0:5}
	// group(1) = property name
	// group(2) = optional fromIndex
	// group(3) = optional toIndex
	public static final String REGEX_PROPERTY = "[$][{]([^}$]+?)(?:[:](\\d+)(?:[:](\\d+))?)?[}]";

	/**
	 * Perform a recursive replacement of referenced properties
	 * Note that Map<Object,Object> is used to maintain compatibility with java.util.Properties
	 *
	 * @param map      the map to perform replacements in, and the source, at the same time.
	 * @author philipp.grasboeck
	 */
	public static void replaceRecursiveProperties(Map<Object,Object> map) {
		replaceRecursiveProperties(map, map);
	}


	/**
	 * Perform a recursive replacement of referenced properties
	 * Note that Map<Object,Object> is used to maintain compatibility with java.util.Properties
	 *
	 * @param map      the map to perform replacements in
	 * @param source   the source of the referenced properties. can be the map itself
	 * @author philipp.grasboeck
	 */
	public static void replaceRecursiveProperties(Map<Object,Object> map, final Map<Object, Object> source) {
        Pattern pattern = Pattern.compile(REGEX_PROPERTY);

		for (Map.Entry<Object, Object> entry : map.entrySet()) {
			StringBuilder text = new StringBuilder(entry.getValue().toString());
			RegexUtils.recursiveScan(text, pattern, new ScanVisitor() {
				@Override
				public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
					Object value = source.get(match.group(1));
					return value != null ? substringSafe(value.toString(), getIntGroup(match, 2), getIntGroup(match, 3)) : null;
				}
			});
			entry.setValue(text.toString());
		}
	}

	private static int getIntGroup(MatchResult match, int group) {
		String string = match.group(group);
		return string != null ? Integer.parseInt(string) : -1;
	}

	private static String substringSafe(String string, int fromIndex, int toIndex) {
		if (fromIndex == -1 || fromIndex > string.length()) {
			fromIndex = 0;
		}
		if (toIndex == -1 || toIndex > string.length()) {
			toIndex = string.length();
		} else if (toIndex < fromIndex) {
			toIndex = fromIndex;
		}
		return string.substring(fromIndex, toIndex);
	}

	public static byte[] getClassBytes(Class<?> clazz) throws IOException {
		String classResourceName = clazz.getName().replace('.', '/') + ".class";
		URL url = ClassLoader.getSystemClassLoader().getResource(classResourceName);
		InputStream in = url.openStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			IOUtils.copy(in, out);
			return out.toByteArray();
		} finally {
			in.close();
			out.close();
		}
	}

	public static Class<?> defineClass(String className, byte[] classBytes) {
		class ByteClassLoader extends ClassLoader {
			private Class<?> defineClass(String name, byte[] bytes) {
				return super.defineClass(name, bytes, 0, bytes.length);
			}
		}
		return new ByteClassLoader().defineClass(className, classBytes);
	}

	/**
	 * Update/Enhance the target with the settings from the provided properties.
	 *
	 * @param target The Bean to fill with properties
	 * @param properties
	 * @param namespace
	 *
	 * @author dominik.stadler
	 */
	public static <T> void enhancePropertyBean(T target, Properties properties, String namespace) {
		MvelUtils.injectProperties(target, properties, namespace);
	}

	/**
	 * Store the properties with the provided names in the provided location.
	 *
	 * @param target
	 * @param propertyNames
	 * @param location
	 * @throws IOException
	 * @author dominik.stadler
	 */
	public static <T> void store(T target, Set<String> propertyNames, File location, String namespace) throws IOException {
		Map<?,?> prop = MvelUtils.readProperties(target, propertyNames, namespace);
		Properties properties = new Properties();
		properties.putAll(prop);

		FileOutputStream str = new FileOutputStream(location);
		try {
			properties.store(str, "Automatically created properties");
		} finally {
			str.close();
		}
	}

	// the regex that caputers #{property}, group(1) = property
	// this regex supports for nesting: #{prefix-#{middle}-postfix}
	public static final String REGEX_DYNAMIC_PROPERTY = "\\#\\{([^\\}\\#]+)\\}";

	/**
	 * Perform replacement of so-called dynamic properties (#{...}) in text and return
	 * the replaced text.
	 *
	 * Supports replacing as-a-whole, i.e.
	 * "#{_port}"  will be replaced to e.g. "_8080", or "", if null is returned by value()
	 * "#{(duplicator)}" will be replaced to e.g. "(1)", or "", if null is returned by value()
	 *
	 * @param text   the text to perform replacements in
	 * @param properties a list of dynamic properties, see DynamicProperty
	 * @return the replaced text
	 * @author philipp.grasboeck
	 */
	public static String replaceDynamicProperties(String text, final DynamicProperty... properties) {
        Pattern pattern = Pattern.compile(REGEX_DYNAMIC_PROPERTY);
		StringBuilder buf = new StringBuilder(text);

		RegexUtils.dynamicScan(buf, pattern, new ScanVisitor() {
			@Override
			public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
				String candidate = match.group(1);

				for (DynamicProperty property : properties) {
					String name = property.name();
					int i = candidate.indexOf(name);
					if (i == -1) {
						continue; // next candidate
					}
					if (i > 0 && Character.isLetter(candidate.charAt(i - 1))) {
						continue; // it's not the start of a word
					}
					if (i + name.length() < candidate.length() && Character.isLetter(candidate.charAt(i + name.length()))) {
						continue; // it's not the end of a word
					}
					Object value = property.value();
					return (value == null) ? "" : (name.length() == candidate.length()) ? value.toString() : new StringBuilder()
						.append(candidate.substring(0, i))
						.append(value)
						.append(candidate.substring(i + name.length()));
				}
				return null;
			}
		});
		return buf.toString();
	}

	/**
	 * DynamicProperty defines the name of a property to be replaced by a dynamic value.
	 *
	 * @author philipp.grasboeck
	 */
	public static interface DynamicProperty {

		/**
		 * The name of the property to replace.
		 */
		String name();

		/**
		 * The dynamic value for this property.
		 */
		Object value();
	}
}
