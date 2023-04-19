package com.dynatrace.easytravel.tomcat;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.google.common.base.Strings;

/**
 * Tomcat configuration
 *
 * @param hostname the name of the host this tomcat instance should register itself. Default value if nothing else specified
 *        should
 *        be "localhost".
 * @param port the port where to listen to HTTP. Note that this may be 0 (AJP-only configuration), in that case ajpPort MUST
 *        be set.
 * @param shutdownPort the port where to listen to the SHUTDOWN command. Note that this may be 0 (no shutdown via port).
 * @param ajpPort the port where to listen to AJP. Note that this may be 0 (HTTP-only configuration), unless port is 0 too.
 * @param contextRoot the base url where the webapp will be listed, can be "/" to list it on the base url directly
 * @param webappBase the webappBase where to find the documents.
 * @param cookies whether to use cookies for the session id
 * @param addPersistentSessionManager TODO
 */

public class Tomcat7Config {
	private final String hostname;
	private final int port; 
	private final int shutdownPort; 
	private final String routePrefix; 
	private final int ajpPort; 
	private final String contextRoot;
	private final String webappBase; 
	private final boolean webappIsAbsolute;
	private final boolean cookies; 
	private final boolean addPersistentSessionManager; 
	private final boolean addAuthentication;
	@SuppressWarnings("rawtypes")
	private final Class parentClass;
	
	private Tomcat7Config(Tomcat7ConfigBuilder builder) {
		this.hostname = builder.hostname;
		this.port = builder.port;
		this.shutdownPort = builder.shutdownPort;
		this.routePrefix = builder.routePrefix;
		this.ajpPort = builder.ajpPort;
		this.contextRoot = builder.contextRoot;
		this.webappBase = builder.webappBase;
		this.webappIsAbsolute = builder.webappIsAbsolute;
		this.cookies = builder.cookies;
		this.addPersistentSessionManager = builder.addPersistentSessionManager;
		this.addAuthentication = builder.addAuthentication;
		this.parentClass = builder.parentClass;
	}
	
	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}

	public int getShutdownPort() {
		return shutdownPort;
	}

	public String getRoutePrefix() {
		return Strings.nullToEmpty(routePrefix);
	}

	public int getAjpPort() {
		return ajpPort;
	}

	public String getContextRoot() {
		return contextRoot;
	}

	public String getWebappBase() {
		return webappBase;
	}

	public boolean isWebappIsAbsolute() {
		return webappIsAbsolute;
	}

	public boolean isCookies() {
		return cookies;
	}

	public boolean isAddPersistentSessionManager() {
		return addPersistentSessionManager;
	}

	public boolean isAddAuthentication() {
		return addAuthentication;
	}

	@SuppressWarnings("rawtypes")
	public Class getParentClass() {
		return parentClass;
	}

	public static class Tomcat7ConfigBuilder {
		private String hostname = BaseConstants.LOCALHOST;
		private int port = 0; 
		private int shutdownPort = 0 ; 
		private String routePrefix = null; 
		private int ajpPort = 0; 
		private String contextRoot= "/";
		private String webappBase = EasyTravelConfig.read().webappBase; 
		private boolean webappIsAbsolute = false;
		private boolean cookies = false; 
		private boolean addPersistentSessionManager = false; 
		private boolean addAuthentication = false;
		@SuppressWarnings("rawtypes") private Class parentClass = null;
				
		public Tomcat7ConfigBuilder withHostName(String hostname) {
			this.hostname = hostname;
			return this;
		}
		
		public Tomcat7ConfigBuilder withPort(int port) {
			this.port = port;
			return this;
		}
		
		public Tomcat7ConfigBuilder withShutdownPort(int port) {
			this.shutdownPort = port;
			return this;
		}
		
		public Tomcat7ConfigBuilder  withRoutePrefix(String routePrefix) {
			this.routePrefix = routePrefix;
			return this;
		}
		
		public Tomcat7ConfigBuilder withAjpPort(int port) {
			this.ajpPort = port;
			return this;
		}
		
		public Tomcat7ConfigBuilder withContextRoot(String contextRoot) {
			this.contextRoot = contextRoot;
			return this;
		}
		
		public Tomcat7ConfigBuilder withWebappBase(String webappBase) {
			this.webappBase = webappBase;
			return this;					
		}
		
		public Tomcat7ConfigBuilder withWebappIsAbsolute(boolean webappIsAbsolute) {
			this.webappIsAbsolute = webappIsAbsolute;
			return this;
		}
		
		public Tomcat7ConfigBuilder withCookies(boolean cookies) {
			this.cookies = cookies;
			return this;
		}
		
		public Tomcat7ConfigBuilder withPersistentSessionManager(boolean addPersistentSessionManager) {
			this.addPersistentSessionManager = addPersistentSessionManager;
			return this;
		}
		
		public Tomcat7ConfigBuilder withAuthentication(boolean addAuthentication) {
			this.addAuthentication = addAuthentication;
			return this;
		}
		
		public Tomcat7ConfigBuilder withParentClass(@SuppressWarnings("rawtypes") Class parentClass) {
			this.parentClass = parentClass;
			return this;
		}
		
		public Tomcat7Config build() {
			return new Tomcat7Config(this);
		}
	}
}
