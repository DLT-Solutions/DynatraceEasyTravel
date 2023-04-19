package com.dynatrace.easytravel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.dynatrace.easytravel.config.InstallationType;

/**
 * Small helper class which parses all plugin descriptions and produces text output which is suitable for
 * putting it onto the Wiki at SECRET
 *
 * @author dominik.stadler
 */
public class ListPlugins {

	private static Map<String, Plugin> pluginMap = new TreeMap<String, Plugin>();
	private static Map<String, Set<String>> groupMap = new TreeMap<String, Set<String>>();

	private static List<String> unusedPlugins = new ArrayList<String>();

	private static class Plugin implements Comparable<Plugin> {

		public String name;
		public String description;
		public String group;
		public String compatibility = "";
		public String dependencies = "";

		@Override
		public int compareTo(Plugin o) {
			return name.compareTo(o.name);
		}
	}

	/**
	 *
	 * @param args
	 * @author dominik.stadler
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException, SAXException {
		// start in top-level easyTravel source dir
		File base = new File("..");

		// search for all "plugins"
		File[] plugins = searchPlugins(base);

		System.out.println("Found " + plugins.length + " plugin-directories: " + Arrays.toString(plugins));

		populateGroupAndPluginMap(plugins);

		System.out.println("Found " + pluginMap.size() + " plugins in " + groupMap.size() + " groups.");

		// print out group-information
		StringBuilder wikiText = new StringBuilder();

		writeGroups(wikiText);

		writePlugins(wikiText);

		System.out.println(wikiText.toString());
		/*
		 * TODO: how to get the text into the Clipboard, the following did not work
		 * Clipboard clipboard = new Clipboard(new Display());
		 * clipboard.setContents(new Object[] { wikiText.toString() },
		 * new Transfer[] { TextTransfer.getInstance() });
		 */

		// automatically open the URL where the plugins are documented to put the contents in place there
		// new DocumentStarter().openURL("SECRET");
	}

	private static File[] searchPlugins(File base) {
		File[] plugins = base.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.contains("plugin");
			}
		});
		return plugins;
	}

	private static void populateGroupAndPluginMap(File[] plugins) throws IOException, SAXException {
		for (File plugin : plugins) {
			final File buildXml = new File(plugin, "build.xml");
			final String buildXmlStr;
			if (!buildXml.exists()) {
				System.err.println("Could not find build.xml for plugin: '" + plugin + "'");
				buildXmlStr = "";
			} else {
				buildXmlStr = FileUtils.readFileToString(buildXml);
			}

			// for each plugin, make sure there is a sub-directory "resources/META-INF/services" which contains the *.ctx.xml file
			String resourcePath = "resources" + File.separator + "META-INF" + File.separator + "services";
			File dir = new File(plugin.getAbsolutePath() + File.separator + resourcePath);
			if (!dir.exists()) {
				System.out.println("Plugin '" + plugin + "' did not have a resources directory at '" + resourcePath + "'.");
				continue;
			}

			// *.ctx.xml files contain the plugin registration
			File[] contexts = dir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".ctx.xml");
				}
			});

			// sanity check
			if (contexts == null || contexts.length == 0) {
				System.out.println("Did not find any matchine xml file in directory '" + dir + "'");
				continue;
			}

			// should not be, but there can be multiple files in one plugin
			for (File file : contexts) {
				// System.out.println("Reading plugins from file: " + file);

				// read plugins from the XML and verify that none of the names is duplicated
				for (Plugin entry : PluginXMLContentHandler.parseContent(file)) {
					if (pluginMap.put(entry.name, entry) != null) {
						throw new IllegalStateException("Had two plugins with name: " + entry.name + ", file: " + file);
					}

					if (buildXmlStr.contains("plugins-unused")) {
						unusedPlugins.add(entry.name);
					}

					if (!groupMap.containsKey(entry.group)) {
						groupMap.put(entry.group, new TreeSet<String>());
					}
					groupMap.get(entry.group).add(entry.name);
				}
			}
		}
	}

	private static void writeGroups(StringBuilder wikiText) {
		wikiText.append("<p>\n").
		append("  <ac:macro ac:name=\"toc\"/>\n").
		append("</p>\n").
		append("<h1>Available groups of problem patterns</h1>\n");

		for (String name : groupMap.keySet()) {
			wikiText.append("<h2>Group - ").append(name).append("</h2>\n").append(
							"<ul>\n");
			for (String plugin : groupMap.get(name)) {
				wikiText.append("  <li>").append(plugin).append("</li>\n");
			}

			wikiText.append("</ul>\n");
		}
	}

	private static void writePlugins(StringBuilder wikiText) {
		wikiText.append("<h1>Plugins available in Launcher and for inclusion in Scenario XML</h1>\n").
			append("<ac:macro ac:name=\"show-if\">\n").
			append("  <ac:parameter ac:name=\"spacePermission\"/>\n").
			append("  <ac:parameter ac:name=\"atlassian-macro-output-type\">BLOCK</ac:parameter>\n").
			append("  <ac:parameter ac:name=\"user\">noone</ac:parameter>\n").
			append("  <ac:rich-text-body>\n").
			append("    <p>This list is automatically generated from source via application \"PluginList\" in project TravelTest!</p>\n").
			append("  </ac:rich-text-body>\n").
			append("</ac:macro>\n").
			append("<table>\n").
			append("  <tbody>\n").
			append("    <tr>\n").
			append("      <th>\n").
			append("        <p>Pattern</p>\n").
			append("      </th>\n").
			append("      <th>\n").
			append("        <p>Description</p>\n").
			append("      </th>\n").
			append("      <th>\n").
			append("        <p>State (see below)</p>\n").
			append("      </th>\n").
			append("      <th>\n").
			append("        <p>Suitable for APM/dynaTrace</p>\n").
			append("      </th>\n").
			append("      <th>\n").
			append("        <p>Dependency (i.e. required DB type)</p>\n").
			append("      </th>\n").
			append("    </tr>\n");

		// is already sorted by name
		for (String name : pluginMap.keySet()) {
			String state = unusedPlugins.contains(name) ? "\n          <ac:emoticon ac:name=\"warning\"/>\n        " : "";
			Plugin plugin = pluginMap.get(name);
			wikiText.append("    <tr>\n").
				append("      <td>\n").
				append("        <p>").append(name).append("</p>\n").
				append("      </td>\n").
				append("      <td>\n").
				append("        <p>").append(plugin.description).append("</p>\n").
				append("      </td>\n").
				append("      <td>\n").
				append("        <p>").append(state.isEmpty() ? " " : state).append("</p>\n").
				append("      </td>\n").
				append("      <td>\n").
				append("        <p>").append(plugin.compatibility.equals(InstallationType.Both.name()) ? " " : plugin.compatibility).append("</p>\n").
				append("      </td>\n").
				append("      <td>\n").
				append("        <p>").append(plugin.dependencies.isEmpty() ? " " : plugin.dependencies).append("</p>\n").
				append("      </td>\n").
				append("    </tr>\n");
		}

		wikiText.append("  </tbody>\n").
			append("</table>\n").
			append("<p>Note: <ac:emoticon ac:name=\"warning\"/> means that the plugin is not automatically deployed in order to allow easyTravel to run on older hardware and smaller systems. If you want to enable this plugin, please copy the respective jar-file from the folder \"plugins-unused\" to \"plugins-frontend\" or \"plugins-backend\" respectively and restart easyTravel.</p>");
	}

	public static class PluginXMLContentHandler extends DefaultHandler {

		private final Set<Plugin> plugins = new TreeSet<Plugin>();

		private String bean;
		private Plugin plugin;

		public static Set<Plugin> parseContent(File file) throws IOException, SAXException {
			XMLReader parser = XMLReaderFactory.createXMLReader();

			PluginXMLContentHandler handler = new PluginXMLContentHandler();

			parser.setContentHandler(handler);
			parser.setErrorHandler(handler);

			FileInputStream fileInputStream = new FileInputStream(file);
			try {
				InputSource source = new InputSource(fileInputStream);

				parser.parse(source);
			} catch (IllegalStateException e) {
				throw new IllegalStateException("While parsing file: " + file, e);
			} finally {
				fileInputStream.close();
			}

			if (handler.plugins.size() == 0) {
				throw new IllegalStateException("Could not find any plugins in: " + file);
			}

			return handler.plugins;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (localName.equals("bean")) {
				if ("com.dynatrace.easytravel.spring.PluginBeanFactoryPostProcessor".equals(attributes.getValue("class"))) {
					// ignore PostProcessor-beans
					return;
				}
			}

			if (localName.equals("bean")) {
				bean = attributes.getValue("id");
				plugin = new Plugin();

				if (bean == null || bean.isEmpty()) {
					throw new IllegalStateException("Could not read id of bean.");
				}
			} else if (bean != null) {
				String name = attributes.getValue("name");
				String value = attributes.getValue("value");
				if (localName.equals("property") && "name".equals(name)) {
					plugin.name = value;
				} else if (localName.equals("property") && "description".equals(name)) {
					plugin.description = value;
				} else if (localName.equals("property") && "groupName".equals(name)) {
					plugin.group = value;
				} else if (localName.equals("property") && "compatibility".equals(name)) {
					// don't use "Classic" in places that are visible to the outside, make it clear that this is "dynaTrace"
					if(value.equals("Classic")) {
						plugin.compatibility = "dynaTrace";
					} else {
						plugin.compatibility = value;
					}
				} else if (localName.equals("property") && "dependencies".equals(name)) {
					plugin.dependencies = value;
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (localName.equals("bean") && bean != null) {
				if (plugin.name == null) {
					throw new IllegalStateException("Could not read name for bean!");
				}

				/*
				 * if(name == null) {
				 * throw new IllegalStateException("Tried to add empty-name for bean '" + bean + "'");
				 * }
				 */
				System.out.println("Having bean: " + bean + " with group: " + plugin.group);
				plugins.add(plugin);
				plugin = null;
				bean = null;
			}
		}
	}

	/*
	 * <?xml version="1.0" encoding="UTF-8"?>
	 * <beans xmlns="http://www.springframework.org/schema/beans"
	 * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	 * xsi:schemaLocation=
	 * "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd">
	 *
	 * <bean id="dummypaymentservice" class="com.dynatrace.easytravel.DummyPaymentService">
	 * <property name="name" value="DummyPaymentService" />
	 * <property name="groupName" value="DummyPaymentService" />
	 * <property name="description" value="Always returns 'payment accepted'." />
	 * </bean>
	 *
	 * <bean class="com.dynatrace.easytravel.spring.PluginBeanFactoryPostProcessor">
	 * <property name="extensionBeanName" value="pluginHolder" />
	 * <property name="propertyName" value="plugins" />
	 * <property name="pluginBeanName" value="dummypaymentservice" />
	 * </bean>
	 * </beans>
	 */
}
