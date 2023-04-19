package com.dynatrace.easytravel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.spring.SpringUtils;
import com.dynatrace.easytravel.spring.TomcatHolderBean;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

/**
 * Plugin simulates new war deployment. When it is started it checks what version of application is installed in
 * context
 * @author cwpl-rpsciuk
 *
 */
public class WarDeploymentPlugin extends AbstractGenericPlugin {

	private static final Logger log = LoggerFactory.make();

	public static final String DST_APP_NAME = "easyTravelMonitor.war";
	public static final String SRC_APP_NAME_V1 = "easyTravelMonitor1.war.src";
	public static final String SRC_APP_NAME_V2 = "easyTravelMonitor2.war.src";

	@Override
	public Object doExecute(String location, Object... context) {
		final EasyTravelConfig config = EasyTravelConfig.read();

		if (PluginConstants.LIFECYCLE_PLUGIN_DISABLE.equals(location)) {
			log.info("webApplicationDeployment plugin disabled.");
			return null;
		}

		if (!PluginConstants.LIFECYCLE_PLUGIN_ENABLE.equals(location)) {
			return null;
		}

		log.debug("webApplicationDeployment plugin started.");

		// if we get passed a TomcatHolderBean we use it (this is used for tests!)
		final TomcatHolderBean tomcatHolder;
		if (context.length > 0 && context[0] instanceof TomcatHolderBean) {
			tomcatHolder = (TomcatHolderBean) context[0];
		} else {
			// otherwise retrieve the entity manager via Spring
			tomcatHolder = SpringUtils.getBean("tomcatHolderBean", TomcatHolderBean.class);
		}

		if (tomcatHolder == null || tomcatHolder.getTomcat() == null) {
			log.warn(TextUtils.merge("Tomcat instance not found. {0}", tomcatHolder));
			return null;
		}
		log.debug("Tomcat instance found.");

		String webAppContext = config.backendEasyTravelMonitorContextRoot;
		Context webApp = findWebApp(tomcatHolder.getTomcat(), webAppContext);
		if (webApp == null) {
			log.warn(TextUtils.merge("Web app not found for context: {0}",webAppContext));
			return null;
		}
		log.debug(TextUtils.merge("Web app found for context: {0}", webAppContext));

		//copy new version of the application (war file) to the easyTravelMonitor.war
		try {
			upgradeApp(tomcatHolder, webApp);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		log.debug(TextUtils.merge("Reloading application: {0}", config.backendEasyTravelMonitorContextRoot));
		webApp.reload();

		return null;
	}

	/**
	 * Find Context for EasyTravelMonitor app in the Tomcat
	 * @param tomcat
	 * @param appName
	 * @return
	 */
	private Context findWebApp(Tomcat tomcat, String appName) {
		Host host = tomcat.getHost();
		if (host == null) {
			return null;
		}

		return (Context)host.findChild(appName);
	}

	/**
	 * Get application version. Parse the result read from the network.
	 * @param tomcatHolder
	 * @return
	 * @throws IOException
	 */
	private String getVersion(TomcatHolderBean tomcatHolder) throws IOException {
		final EasyTravelConfig conf = EasyTravelConfig.read();

		HttpGet request = new HttpGet("http://" + getEasyTravelMonitorHostPort(tomcatHolder, conf)
				+  conf.backendEasyTravelMonitorContextRoot);

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		try {
			HttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity);
			log.trace(responseString);

			return getVersionFromResponse(responseString);
		} catch (ClientProtocolException e) {
			log.error("Cannot get current easyTravelMonitor version", e);
		} catch (IOException e) {
			log.error("Cannot get current easyTravelMonitor version", e);
		} finally {
			httpClient.close();
		}
		return null;
	}

	/**
	 * Parse http response and return actual version
	 * @param httpResponse
	 * @return
	 */
	private String getVersionFromResponse(String httpResponse){
		Pattern pattern = Pattern.compile("<h1>version: (.*?)</h1>");
		Matcher matcher = pattern.matcher(httpResponse);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	/**
	 * Get name for the new application.
	 * @param currentVersion
	 * @return
	 */
	private String getNewAppName(String currentVersion) {
		if ("1.0".equals(currentVersion)) {
			return SRC_APP_NAME_V2;
		} else {
			return SRC_APP_NAME_V1;
		}
	}

	/**
	 * Copy new application to the easyTravelMonitor.war. If version 1 of application is currently running then copy version 2.
	 * @param tomcatHolder
	 * @param webApp
	 * @throws IOException
	 */
	private void upgradeApp(TomcatHolderBean tomcatHolder, Context webApp) throws IOException {
		String currentVersion = getVersion(tomcatHolder);
		String srcAppName = getNewAppName(currentVersion);
		String webAppDir = FilenameUtils.getFullPath(webApp.getDocBase());;

		File dstFile = new File(webAppDir + DST_APP_NAME);
		URL srcWarURL = getClass().getClassLoader().getResource(srcAppName);
		InputStream in;
		try {
			in = srcWarURL.openStream();
			FileUtils.copyInputStreamToFile(in, dstFile);
			log.info(TextUtils.merge("Application copied: from {0} to {1}", srcAppName, dstFile));
		} catch (IOException e) {
			log.error("Error copying application", e);
		}
	}

	private String getEasyTravelMonitorHostPort(TomcatHolderBean tomcatHolder, EasyTravelConfig config) {
		int port = config.backendPort;
		String host = config.backendHost;

		if (tomcatHolder.getReservation() != null) {
			port = tomcatHolder.getReservation().getPort();
			host = BaseConstants.LOCALHOST;
		}

		return host + ":" + port;
	}
}
