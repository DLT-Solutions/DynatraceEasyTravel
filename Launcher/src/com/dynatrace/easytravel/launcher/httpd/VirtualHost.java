package com.dynatrace.easytravel.launcher.httpd;

import java.io.File;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.UrlType;
import com.dynatrace.easytravel.launcher.engine.ProcedureFactory;
import com.dynatrace.easytravel.launcher.httpd.ProxyDirective.CustomerFrontedProxyDirective;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;

/**
 * Helper class for creating the virtual hosts for the generated httpd.conf
 *
 */
public class VirtualHost {
	private static final String CONFIG_DIRECTORY = Directories.getConfigDir().getAbsolutePath() + File.separator;
	private static final Logger log = LoggerFactory.make();
	private static class VirtualHostBasicType {

		
		
		public static VirtualHostBasicType CUSTOMER_FRONTEND() {
			return new VirtualHostBasicType(
                "RewriteEngine On",
                "RewriteOptions Inherit",
				"ProxyPass / balancer://mycluster/",
				"ProxyPassReverse / balancer://mycluster/");
		}

		public static VirtualHostBasicType BUSINESS_BACKEND() {
			return new VirtualHostBasicType(
				"ProxyPass / balancer://backendcluster/",
				"ProxyPassReverse / balancer://backendcluster/");
		}

		// pass the actual url of the B2B Frontend in case we start it remotely (i.e. Demo-In-The-Cloud)
		public static VirtualHostBasicType B2B_FRONTEND() {
			final EasyTravelConfig config = EasyTravelConfig.read();
			return new VirtualHostBasicType(
				"ProxyPass / " + "http://" + ProcedureFactory.getHostOrLocal(Constants.Procedures.B2B_FRONTEND_ID)+ ":" +
						config.b2bFrontendPortRangeStart + "/",
				"ProxyPassReverse / " + "http://" + ProcedureFactory.getHostOrLocal(Constants.Procedures.B2B_FRONTEND_ID)+ ":" +
						config.b2bFrontendPortRangeStart + "/");
		}

		public static VirtualHostBasicType REAL_INTERNET_URL() {
			return new VirtualHostBasicType(
				"ProxyRequests Off",
				"ProxyPreserveHost On");
		}

		public static VirtualHostBasicType INJECTION_PROXY() {
			return new VirtualHostBasicType(
				"RequestHeader unset Accept-Encoding",
				"ProxyRequests On");
		}

		public static VirtualHostBasicType ANGULAR_FRONTEND_PROXY() {
			return new VirtualHostBasicType(
					"ProxyPass / balancer://angcluster/",
					"ProxyPassReverse / balancer://angcluster/");
		}

		private final String[] arguments;

		private VirtualHostBasicType(String... arguments) {
			this.arguments = arguments;
		}


		public String[] getArguments() {
			return arguments;
		}


	}

	private static class VirtualHostType {

		public static VirtualHostType CUSTOMER_FRONTEND_INTERNET_URL() {
			return new VirtualHostType(EasyTravelConfig.read().apacheWebServerVirtualIp,
				new CustomerFrontedProxyDirective(),
				VirtualHostBasicType.REAL_INTERNET_URL().getArguments(),
				VirtualHostBasicType.CUSTOMER_FRONTEND().getArguments());
		}

		public static VirtualHostType CUSTOMER_FRONTEND_NO_INTERNET_URL() {
			return new VirtualHostType("*:" + EasyTravelConfig.read().apacheWebServerPort,
				new CustomerFrontedProxyDirective(),
				VirtualHostBasicType.CUSTOMER_FRONTEND().getArguments());
		}

		public static VirtualHostType B2B_FRONTEND_INTERNET_URL() {
			return new VirtualHostType(EasyTravelConfig.read().apacheWebServerB2bVirtualIp,
				VirtualHostBasicType.REAL_INTERNET_URL().getArguments(),
				VirtualHostBasicType.B2B_FRONTEND().getArguments());
		}

		public static VirtualHostType B2B_FRONTEND_NO_INTERNET_URL() {
			return new VirtualHostType("*:" + EasyTravelConfig.read().apacheWebServerB2bPort,
				VirtualHostBasicType.B2B_FRONTEND().getArguments());
		}

		public static VirtualHostType INJECTION_PROXY_URL() {
			return new VirtualHostType("*:" + EasyTravelConfig.read().apacheWebServerProxyPort,
				VirtualHostBasicType.INJECTION_PROXY().getArguments());
		}

		public static VirtualHostType BUSINESS_BACKEND_INTERNET_URL() {
			return new VirtualHostType(EasyTravelConfig.read().apacheWebServerVirtualIp,
					VirtualHostBasicType.REAL_INTERNET_URL().getArguments(),
					VirtualHostBasicType.BUSINESS_BACKEND().getArguments());
		}

		public static VirtualHostType BUSINESS_BACKEND_NO_INTERNET_URL() {
			return new VirtualHostType("*:" + EasyTravelConfig.read().apacheWebServerBackendPort,
				VirtualHostBasicType.BUSINESS_BACKEND().getArguments());
		}

		public static VirtualHostType ANGULAR_FRONTEND() {
			return new VirtualHostType("*:" + EasyTravelConfig.read().angularFrontendApachePort,
					new ProxyDirective.AngularFrontedProxyDirective(),
					VirtualHostBasicType.ANGULAR_FRONTEND_PROXY().getArguments());
		}

		private final List<String> arguments;
		private final String virtualHostName;
		private final ProxyDirective proxyDirective;

		private VirtualHostType(String virtualHostName, String... arguments) {
			this(virtualHostName, (ProxyDirective) null, arguments);
		}

		private VirtualHostType(String virtualHostName, ProxyDirective proxyDirective, String... arguments) {
			this.arguments = Arrays.asList(arguments);
			this.virtualHostName = clearUrl(virtualHostName);
			this.proxyDirective = proxyDirective;
		}

		private static String clearUrl(String url) {
			final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();

			url = url.replaceFirst("http://", "");
			url = clearSlashesAtEnd(url);
			String frontendContext = clearSlashesAtEnd(EASYTRAVEL_CONFIG.frontendContextRoot);
			if (url.endsWith(frontendContext)) {
				return url.substring(0, url.length() - frontendContext.length());
			}

			return url;
		}

		private static String clearSlashesAtEnd(String url) {
			StringBuffer clearedUrl = new StringBuffer(url);
			while (clearedUrl.length() > 0 && clearedUrl.charAt(clearedUrl.length() - 1) == '/') {
				clearedUrl = clearedUrl.deleteCharAt(clearedUrl.length() - 1);
			}
			return clearedUrl.toString();
		}

		private VirtualHostType(String virtualHostName, ProxyDirective proxyDirective, String[] arguments1, String[] arguments2) {
			this(virtualHostName, proxyDirective, (String[]) ArrayUtils.addAll(arguments1, arguments2));
		}

		private VirtualHostType(String virtualHostName, String[] arguments1, String[] arguments2) {
			this(virtualHostName, (ProxyDirective) null, (String[]) ArrayUtils.addAll(arguments1, arguments2));
		}

		public List<String> getArguments() {
			return arguments;
		}

		public String getVirtualHostName() {
			return virtualHostName;
		}

		public ProxyDirective getProxyDirective() {
			return proxyDirective;
		}
	}

	private static VirtualHostType getVirtualHost(UrlType urlType) {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		if (UrlType.APACHE_JAVA_FRONTEND.equals(urlType) &&
				EASYTRAVEL_CONFIG.apacheWebServerEnableVirtualIp &&
				UrlUtils.isInternetUrl(EASYTRAVEL_CONFIG.apacheWebServerHost)) {
			return VirtualHostType.CUSTOMER_FRONTEND_INTERNET_URL();
		}

		if (UrlType.APACHE_JAVA_FRONTEND.equals(urlType)) {
			return VirtualHostType.CUSTOMER_FRONTEND_NO_INTERNET_URL();
		}

		if (UrlType.APACHE_B2B_FRONTEND.equals(urlType) &&
				EASYTRAVEL_CONFIG.apacheWebServerEnableVirtualIp &&
				UrlUtils.isInternetUrl(EASYTRAVEL_CONFIG.apacheWebServerB2bHost)) {
			return VirtualHostType.B2B_FRONTEND_INTERNET_URL();
		}

		if (UrlType.APACHE_B2B_FRONTEND.equals(urlType)) {
			return VirtualHostType.B2B_FRONTEND_NO_INTERNET_URL();
		}

		if(UrlType.APACHE_PROXY.equals(urlType)) {
			return VirtualHostType.INJECTION_PROXY_URL();
		}

		if (UrlType.APACHE_BUSINESS_BACKEND.equals(urlType) &&
				EASYTRAVEL_CONFIG.apacheWebServerEnableVirtualIp &&
				UrlUtils.isInternetUrl(EASYTRAVEL_CONFIG.apacheWebServerHost)) {
			return VirtualHostType.BUSINESS_BACKEND_INTERNET_URL();
		}

		if (UrlType.APACHE_BUSINESS_BACKEND.equals(urlType)) {
			return VirtualHostType.BUSINESS_BACKEND_NO_INTERNET_URL();
		}

		if (UrlType.APACHE_ANGULAR_FRONTEND.equals(urlType)) {
			return VirtualHostType.ANGULAR_FRONTEND();
		}

		throw new IllegalArgumentException("Virtual host for requested UrlType " + urlType + " does not exist.");
	}

	public static void writeCustomerAndB2bVirtualHostEntries(PrintWriter writer, boolean ifStartPhp) {
		ApacheConf apacheConf = new ApacheConf();
		apacheConf.setStartPhp(ifStartPhp);
		writeCustomerAndB2bVirtualHostEntries(writer, apacheConf);
	}

	public static void writeCustomerAndB2bVirtualHostEntries(PrintWriter writer, ApacheConf apacheConf) {
		for (UrlType urlType : apacheConf.getRelevantUrlTypes()) {
			if (urlType != null) {
				write(urlType, writer, apacheConf);
			}
		}

		// SSL Connection for CustomerFrontend
		writeCustomerSslHostEntry(writer);
	}

	public static void writeCustomerSslHostEntry(PrintWriter writer) {
		EasyTravelConfig config = EasyTravelConfig.read();
		File certificateFile = new File(CONFIG_DIRECTORY, "easytravel-san.crt");
		File certificateKey = new File(CONFIG_DIRECTORY, "easytravel-san.key");
		String certificateDirectory = CONFIG_DIRECTORY;
		
		if (config.apacheWebServerSslPort > 0) {
			String uriApacheWebServer = "http://" + config.apacheWebServerHost + ":" + config.apacheWebServerPort + "/";
			String hostName = config.apacheWebServerSslHost;

			if (hostName == null || hostName.trim().equals(BaseConstants.EMPTY_STRING)) {
				try {
				    InetAddress addr = InetAddress.getLocalHost();
				    hostName = addr.getHostName();
				} catch (UnknownHostException e) {
					hostName = "localhost";
				}
			}
			
			/*
			 * If there is no certificate in user area Apache will not be running
			 */
			if(!(certificateFile.exists() && certificateKey.exists())){
				certificateDirectory="conf/ssl/";
				log.warn("Certificate was not generated in config directory, using one from installation directory.");
			}
			
			writer.println("<VirtualHost *:" + config.apacheWebServerSslPort + ">");
			writer.println("    ServerName \"" + hostName + "\"");
			writer.println("    SSLEngine on");
			writer.println("    SSLProtocol -all +TLSv1.2");
			writer.println("    SSLCertificateFile \""+certificateDirectory+"easytravel-san.crt\"");
			writer.println("    SSLCertificateKeyFile \""+certificateDirectory+"easytravel-san.key\"");
			writer.println("    ProxyPass / " + uriApacheWebServer);
			writer.println("    ProxyPassReverse / " + uriApacheWebServer);
			writer.println("</VirtualHost>");
		}
	}

	private static void write(UrlType urlType, PrintWriter writer, ApacheConf apacheConf) {
		VirtualHostType virtualHost = getVirtualHost(urlType);
		
		//If the port is not set, then the virtual block is not needed
		if (virtualHost.getVirtualHostName().equalsIgnoreCase("*:0")) {
			return;
		}

		ProxyDirective proxyDirective = virtualHost.getProxyDirective();
		if (proxyDirective != null) {
			proxyDirective.write(writer, apacheConf);
		}

		List<String> mergedArgs = new ArrayList<String>(virtualHost.getArguments());

		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		if (apacheConf.isStartPhp() && virtualHost.getVirtualHostName().equals("*:"+ EASYTRAVEL_CONFIG.apacheWebServerPort)) {
			mergedArgs.add(0, "ProxyPass /rating !");
			mergedArgs.add(1, "ProxyPass /blog !");
		}

		writer.println("<VirtualHost " + virtualHost.getVirtualHostName() + ">");
		if (urlType == UrlType.APACHE_JAVA_FRONTEND) {
			Rewrite.writeRedirectToCustomContext(writer);
		}


		for (String argument : mergedArgs) {
			writer.println("	" + argument);
		}
		simulateFirwallIfActivated(writer);
		writer.println("</VirtualHost>");
	}

	private static void simulateFirwallIfActivated(PrintWriter writer) {
		final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig.read();
		if (EASYTRAVEL_CONFIG.apacheWebServerSimulatesFirewall) {
			// TODO=(stefan.moschinski): make blocked links configurable in configuration?
			List<String> linksToBlock = Arrays.asList("\"^/dtagent_.*\\.js\"", "\"^/dynaTraceMonitor.*\"");
			for (String linkToBlock : linksToBlock) {
				writer.println("	<LocationMatch " + linkToBlock + ">");
				writer.println("		Order allow,deny");
				writer.println("		Deny from all");
				writer.println("	</LocationMatch>");
			}
		}
	}
}
