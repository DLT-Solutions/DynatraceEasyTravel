package com.dynatrace.easytravel.jms;

import java.util.HashMap;
import java.util.Map;

import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;

import com.dynatrace.easytravel.MessageServer;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class JmsServer implements MessageServer {
	private static final String CLOSE_EXCEPTION_MESSAGE = "Cannot close the JMS server: the JMS server has been already stopped or has not been started yet!";

	private static Map<Integer, JmsServer> servers = new HashMap<Integer, JmsServer>(3);

	private EmbeddedActiveMQ embeddedJMSServer;
	private Configuration configuration;
	private final int JMS_PORT;
	private final String JMS_HOST;


	private static Logger logger = LoggerFactory.make();


	public static JmsServer getJmsServerSingletonPerPort(int port, String host) {
		if(servers.get(port) == null) {
			servers.put(port, new JmsServer(port, host));
		}
		return servers.get(port);
	}

	private JmsServer(int port, String host) {
		this.JMS_PORT = port;
        this.JMS_HOST = host;
		configureActiveMQServer();
	}

	private void configureActiveMQServer() {
		try {
			configuration = new ConfigurationImpl();
			configuration.setPersistenceEnabled(false);
			configuration.setSecurityEnabled(false);
			configuration.setJournalDirectory(Directories.getTempDir().getAbsolutePath());

			String url = String.format("tcp://%s:%s", JMS_HOST, JMS_PORT);
			configuration.addAcceptorConfiguration("tcp", url);
		}
		catch (Exception e) {
			logger.error("Error in configureActiveMQServer", e);
		}
	}

	@Override
	public void start() {
		if(embeddedJMSServer != null) {
			return;
		}

		try {
			embeddedJMSServer = new EmbeddedActiveMQ();
			embeddedJMSServer.setConfiguration(configuration);
			embeddedJMSServer.start();

			logger.info("JMS server started successfully");
		} catch (Exception exception) {
			logger.error("Starting JMS Server failed!", exception);
		}
	}

	public EmbeddedActiveMQ getEmbeddedJMSServer(){
		return embeddedJMSServer;
	}

	@Override
	public void close() {
		if(embeddedJMSServer == null) {
			throwIllegalStateException();
		}

		try {
			embeddedJMSServer.stop();
			embeddedJMSServer = null;
		} catch (Exception e) {
			throwIllegalStateException(e);
		}

	}

	private void throwIllegalStateException() {
		throwIllegalStateException(null);
	}

	private void throwIllegalStateException(Exception e) {
		throw new IllegalStateException(CLOSE_EXCEPTION_MESSAGE, e);
	}

}
