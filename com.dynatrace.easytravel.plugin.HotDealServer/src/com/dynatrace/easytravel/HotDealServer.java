package com.dynatrace.easytravel;

import java.util.concurrent.TimeUnit;

import com.dynatrace.easytravel.jms.JmsHandler;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.rmi.RmiHandler;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.PluginConstants;

import ch.qos.logback.classic.Logger;

/**
 * This plugin provides the hot deals. For that purpose it request some random
 * journeys from the DB. The hot deals can be requested by the HotDealClient
 * plugin via RMI or JMS depending on the given mode property.
 *
 * @author stefan.moschinski
 */
public class HotDealServer extends AbstractGenericPlugin {

	private static final Logger log = LoggerFactory.make();

	public static final long DEFAULT_HEALTH_CHECK_INTERVAL = TimeUnit.MINUTES.toMillis(5);
	private long health_check_interval = DEFAULT_HEALTH_CHECK_INTERVAL; 
	
	private MessageHandler messageHandler;
	private MessagingMode messagingMode;

	private DataAccess databaseAccess;
	private int port;
	
	private volatile long lastCheckTime = 0;

	@Override
	public Object doExecute(String location, Object... context) {
		if (PluginConstants.LIFECYCLE_PLUGIN_ENABLE.equals(location)) {
			initialize();
			messageHandler.execute(port, databaseAccess);
		} else if (PluginConstants.LIFECYCLE_PLUGIN_DISABLE.equals(location)) {
			close();
		} else {
			checkIfRunning();
		}
		
		return null;
	}

	private void close() {
		if (messageHandler != null){
			messageHandler.close();
		}
	}

	private void initialize() {
		if (messageHandler == null) {
			messageHandler = newMessageHandlerInstance();
		}
	}
	
	/**
	 * call messageHandler.checkIfRunning method to verify if it is working correctly. Currently implemented only for JMS. 
	 */
	private void checkIfRunning() {
		if (lastCheckTime + health_check_interval < System.currentTimeMillis()) {
			lastCheckTime = System.currentTimeMillis();
			log.info("checkIfRunning messageHandler " +  messageHandler);
			if (messageHandler != null) {
				messageHandler.checkIfRunning();
			}			
		}
	}
	
	/**
	 * for tests
	 * @param interval
	 */
	void setHealthCheckInterval(long interval) {
		health_check_interval = interval;
	}

	// After the first setting of the messagingMode, the invocation of this
	// method does not have any effect
	public void setMode(String mode) {

		for (MessagingMode messagingMode : MessagingMode.values()) {
			if (messagingMode.toString().equals(mode))
				this.messagingMode = messagingMode;
		}

		if (this.messagingMode == null) {
			log.warn("Selected message provider does not exist");
		}
	}
	
	public void setPort(int port){
		this.port = port;
	}

	public void setDatabaseAccess(DataAccess databaseAccess) {
		this.databaseAccess = databaseAccess;
	}

	private MessageHandler newMessageHandlerInstance() {
		if (messagingMode.equals(MessagingMode.JMS)) {
			return new JmsHandler();
		} else if (messagingMode.equals(MessagingMode.RMI)) {
			return new RmiHandler();
		}
		return null;
	}

	private enum MessagingMode {
		RMI("RMI"), JMS("JMS");

		private String name;

		private MessagingMode(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	protected MessageHandler getMessageHandler() {
		return messageHandler;
	}
}
