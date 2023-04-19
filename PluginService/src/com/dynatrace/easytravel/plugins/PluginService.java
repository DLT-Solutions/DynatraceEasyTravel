package com.dynatrace.easytravel.plugins;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;

import com.dynatrace.easytravel.components.ComponentManager;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.json.JSONArray;
import com.dynatrace.easytravel.json.JSONException;
import com.dynatrace.easytravel.json.JSONObject;
import com.dynatrace.easytravel.json.JSONWriter;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.logging.RootLogger;
import com.dynatrace.easytravel.spring.PluginStateManager;
import com.dynatrace.easytravel.spring.RemotePluginService;
import com.dynatrace.easytravel.tomcat.Tomcat7Config;
import com.dynatrace.easytravel.tomcat.Tomcat7Starter;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import ch.qos.logback.classic.Logger;


/**
 * Web Service Based Plugin Service - it uses a {@link PluginStateManager} for keeping
 * the actual state. Usually it is called via REST by the {@link RemotePluginService}.
 *
 * @author cwat-rpilz
 *
 * Modified to use embedded Tomcat by cwpl-wjarosz
 *
 */

//PluginService will be part of the path by virtue of being the application name, hence here we just have root.
//@Path("/PluginService")
@Path("/")
public class PluginService {

	protected static final String CONTEXT = "/PluginService";

	private static final Logger LOGGER = LoggerFactory.make();

	private static final int MAX_BACKLOG = 50;
	private static final int MAX_THREADS = 100;
	private static final int MAX_CONNECTIONS = 100;

	private static final Object lock = new Object();

	// needs to be static as PluginService is instantiated by the Jersey REST implementation!
	private static HashMap<String,String[]> hostsByPluginName = new HashMap<String,String[]>();

	// needs to be static as PluginService is instantiated by the Jersey REST implementation!
	private static PluginStateManager pluginStateManager;

	// needs to be static as PluginService is instantiated by the Jersey REST implementation!
	private static ComponentManager componentsManager;
	
	static {
		pluginStateManager = new PluginStateManager();
        componentsManager = new ComponentManager();		
	}

	private ServletContainer resourceConfig() {
		ResourceConfig rc = new ClassNamesResourceConfig(PluginService.class.getName());
	    return new ServletContainer(rc);
	}


/* The above creation of ServletContainer, for Jersey 2.x would look as follows
 - this may be useful in case we upgrade in the future:

        private ServletContainer resourceConfig() {
            return new ServletContainer(new ResourceConfig(
                    new ResourceLoader().getClasses()));
        }

And then you need to declare ResourceLoader:

        import java.util.HashSet;
		import java.util.Set;
		import javax.ws.rs.core.Application;
		public class ResourceLoader extends Application{
    		@Override
    		public Set<Class<?>> getClasses() {
    	    	final Set<Class<?>> classes = new HashSet<Class<?>>();
    	    	// register root resource
    	    	classes.add(PluginService.class);
    	    	return classes;
			}
		}
*/

	/**
	 * For launching it stand-alone
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {

		RootLogger.setup(BaseConstants.LoggerNames.PLUGIN_SERVICE);
        Parser parser = new BasicParser();
        CommandLine commandLine = null;

        try {
            commandLine = parser.parse(createOptions(), args);
        } catch (ParseException pe) {
            pe.printStackTrace();
            return;
        }

		String propertiesFilePath = commandLine.getOptionValue(BaseConstants.CmdArguments.PROPERTY_FILE);
		if (propertiesFilePath != null && new File(propertiesFilePath).exists()) {
			LOGGER.info("Loading custom properties from file: " + propertiesFilePath);
			EasyTravelConfig.resetSingleton();
			EasyTravelConfig.createSingleton(propertiesFilePath);

			// tell Spring about the changed file
			System.setProperty("com.dynatrace.easytravel.propertiesfile", "file:" + propertiesFilePath);
        } else {
            LOGGER.warn("Run with default configuration because custom configuration file not available");
		}

	    String installationMode = commandLine.getOptionValue(BaseConstants.CmdArguments.INSTALLATION_MODE);
    	DtVersionDetector.enforceInstallationType(InstallationType.fromString(installationMode));
        LOGGER.info("Running in mode: " + DtVersionDetector.getInstallationType());

		// instantiate with correct mode here
		pluginStateManager = new PluginStateManager();

		componentsManager = new ComponentManager();

		// NOTE: although we instantiate the PluginService once here, this is not the instance that is
		// then used afterwards, in fact, the REST implementation will instantiate the class every time
		// a REST call is received
		PluginService inst = new PluginService();
		inst.startHttpService();
	}

    @SuppressWarnings("static-access")
	public static Options createOptions() {
        Options options = new Options();

        Option propertiesFilePath = new Option(BaseConstants.CmdArguments.PROPERTY_FILE, true, "the path to the configuration file");
        options.addOption(propertiesFilePath);

        options.addOption(OptionBuilder.withArgName( "mode" )
        .isRequired() // make it required
        .hasArg()
        .withDescription(  "the current installation mode" )
        .create( BaseConstants.CmdArguments.INSTALLATION_MODE ));

        return options;
    }

	public void startHttpService() throws Exception {

		EasyTravelConfig config = EasyTravelConfig.read();

		try {
			File webappDir= Directories.getExistingTempDir();

			Tomcat7Starter tomcatStarter = new Tomcat7Starter();
			Tomcat7Config tomcatConfig = new Tomcat7Config.Tomcat7ConfigBuilder()
					.withHostName(BaseConstants.LOCALHOST) // In preference to config.pluginServiceHost, as the latter would cause problems
					// by only being accessible through an external IP address.
					.withPort(config.pluginServicePort)
					.withShutdownPort(config.pluginServiceShutdownPort)
					.withContextRoot(config.pluginServiceContextRoot)
					.withWebappBase(webappDir.getAbsolutePath())
					.withWebappIsAbsolute(true)
					.withParentClass(this.getClass())
					.build();

			Tomcat tomcat = tomcatStarter.run(tomcatConfig);			
			Tomcat7Starter.adjustThreads(tomcat, MAX_BACKLOG, MAX_THREADS, MAX_CONNECTIONS);

			// We could add an app here, but the starter has already done that,
			// so we only need to add the servlet.
			// 		Context context = tomcat.addWebapp(config.pluginServiceContextRoot,
			//			webappDir.getAbsolutePath());
			Context context = tomcatStarter.getContext();
			Tomcat.addServlet(context,"jersey-container-servlet",resourceConfig());
			context.addServletMapping("/*", "jersey-container-servlet");

		} catch (Exception ex) {
			//LifecycleException InterruptedException
			LOGGER.warn("Unable to start Tomcat for Plugin Service");
			throw ex;
		}
	}

	//==============================================================
	//
	// REST definitions
	//
	//==============================================================

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("ping")
	public String ping() {
		return "Plugin Service on embedded Tomcat";
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("getAllPluginNames")
	public String getAllPluginNames() {
		synchronized (lock) {
			try {
				String[] allPluginNames = pluginStateManager.getAllPluginNames();
				return JSONObject.valueToString(allPluginNames);
			} catch (JSONException e) {
				throw new WebApplicationException(e);
			}
		}
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("getEnabledPluginNames")
	public String getEnabledPluginNames() {
		synchronized (lock) {
			try {
				return JSONObject.valueToString(pluginStateManager.getEnabledPluginNames());
			} catch (JSONException e) {
				throw new WebApplicationException(e);
			}
		}
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("getEnabledPluginNamesForHost/{host}")
	public String getEnabledPluginNamesForHost(@PathParam("host") String host) {
		synchronized (lock) {
			try {
				return JSONObject.valueToString(pluginStateManager.getEnabledPluginNamesForHost(host));
			} catch (JSONException e) {
				throw new WebApplicationException(e);
			}
		}
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("getAllPlugins")
	public String getAllPlugins() {
		synchronized (lock) {
			try {
				return JSONObject.valueToString(pluginStateManager.getAllPlugins());
			} catch (JSONException e) {
				throw new WebApplicationException(e);
			}
		}
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("getEnabledPlugins")
	public String getEnabledPlugins() {
		synchronized (lock) {
			try {
				return JSONObject.valueToString(pluginStateManager.getEnabledPlugins());
			} catch (JSONException e) {
				throw new WebApplicationException(e);
			}
		}
	}

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Path("setPluginHosts/{name}")
	public String setPluginHosts(@PathParam("name") String name, @FormParam("hosts") String hosts) {
		synchronized (lock) {
			String[] hostsArray = jsonToStringArray(hosts);
			hostsByPluginName.put(name, hostsArray);
			String[] allPluginNames = pluginStateManager.getAllPluginNames();
			if (allPluginNames != null) {
				for (String pluginName : allPluginNames) {
					if (pluginName == null) {
						continue;
					}
					if (pluginName.equals(name)) {
						pluginStateManager.setPluginHosts(name, hostsArray);
						break;
					}
				}
			}
			return Boolean.TRUE.toString();
		}
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("getEnabledPluginsForHost/{host}")
	public String getEnabledPluginsForHost(@PathParam("host") String host) {
		synchronized (lock) {
			try {
				return JSONObject.valueToString(pluginStateManager.getEnabledPluginsForHost(host));
			} catch (JSONException e) {
				throw new WebApplicationException(e);
			}
		}
	}

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Path("registerPlugins")
	public String registerPlugins(@FormParam("pluginData") String pluginData) {
		synchronized (lock) {
			try {
				String[] plugins = jsonToStringArray(pluginData);
				pluginStateManager.registerPlugins(plugins);
				if (plugins != null) {
					for (String plugin : plugins) {
						if (plugin == null) {
							continue;
						}
						int idx = plugin.indexOf(':');
						if (idx >= 0) {
							plugin = plugin.substring(0, idx);
						}
						String[] hosts = hostsByPluginName.get(plugin);
						if (hosts != null) {
							pluginStateManager.setPluginHosts(plugin, hosts);
						}
					}
				}
				return Boolean.TRUE.toString();
			} catch (RuntimeException t) {
				throw new WebApplicationException(t);
			}
		}
	}

	private static String[] jsonToStringArray(String json) {
		String[] result = null;
		try {
			JSONArray jsonArray = new JSONArray(json);
			int len = jsonArray.length();
			result = new String[len];
			for (int i = 0; i < len; i++) {
				result[i] = jsonArray.getString(i);
			}
			return result;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}


	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("setPluginEnabled/{name}/{enabled}")
	public String setPluginEnabled(@PathParam("name") String name, @PathParam("enabled") boolean enabled) {
		synchronized (lock) {
			pluginStateManager.setPluginEnabled(decode(name), enabled);
			return Boolean.TRUE.toString();
		}
	}

	private static final String decode(String s) {
		try {
			return URLDecoder.decode(s, BaseConstants.UTF8);
		} catch (UnsupportedEncodingException e) {
			throw new InternalError(e.getMessage());
		}
	}

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Path("addDynamicPlugin/{className}/{classBytes}")
	public String addDynamicPlugin(@PathParam("className") String className, @FormParam("classBytes") String sClassBytes) {
		// not supported at the moment
		return Boolean.FALSE.toString();
	}

	protected void reset() {
		synchronized (lock) {
			pluginStateManager = new PluginStateManager();
			hostsByPluginName.clear();
		}
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("status")
	public String getStatus() throws IOException {
		synchronized (lock) {
			try {
				String[] allPluginNames = pluginStateManager.getAllPluginNames();
				StringWriter stringWriter = new StringWriter();
				try {
					Set<String> enabledPlugins = new HashSet<String>(Arrays.asList(pluginStateManager.getEnabledPluginNames()));
					for(String plugin : allPluginNames) {
						// need to create the writer for each object as it cannot append multiple items
						JSONWriter writer = new JSONWriter(stringWriter);
						writer.object().
							key("plugin").value(plugin).
							key("enabled").value(enabledPlugins.contains(plugin)).
							key("hosts").value(hostsByPluginName.get(plugin)).
						endObject();
					}
				} finally {
					stringWriter.close();
				}

				return stringWriter.toString();
			} catch (JSONException e) {
				throw new WebApplicationException(e);
			}
		}
	}

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Path("setComponent/{ip}")
	public String setComponent(@PathParam("ip") String ip, @FormParam("params") String params) {
		synchronized (lock) {
			String[] paramsArray = jsonToStringArray(params);
			componentsManager.setComponent(ip, paramsArray);
			return Boolean.TRUE.toString();
		}
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("getComponentsIPList/{type}")
	public String getComponentsIPList(@PathParam("type") String type) {
		synchronized (lock) {
			try {
				return JSONObject.valueToString(componentsManager.getComponentsIPList(type));
			} catch (JSONException e) {
				throw new WebApplicationException(e);
			}
		}
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("removeComponent/{ip}")
	public String removeComponent(@PathParam("ip") String ip) {
		synchronized (lock) {
			componentsManager.removeComponent(ip);
			return Boolean.TRUE.toString();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("setPluginTemplateConfiguration")
	public String setPluginTemplateConfiguration(String body) {
		synchronized (lock) {
			LOGGER.debug("PluginService - setPluginTemplateConfiguration with config of: " + body);
			pluginStateManager.setPluginTemplateConfiguration(body);
			return Boolean.TRUE.toString();
		}
	}
}
