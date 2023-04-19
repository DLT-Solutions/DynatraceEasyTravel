package com.dynatrace.easytravel;

import java.rmi.RemoteException;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.frontend.data.DataProvider;
import com.dynatrace.easytravel.frontend.data.DataProviderInterface;
import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.metrics.Metrics;
import static com.codahale.metrics.Timer.Context;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.PluginConstants;

import ch.qos.logback.classic.Logger;

/**
 * This plugin manipulates the SearchBean to provide hot deals. The hot deals
 * are received via RMI or JMS depending on the given mode property.
 *
 * @author stefan.moschinski
 */
public class HotDealClient extends AbstractGenericPlugin {

	private static final Logger log = LoggerFactory.make();

	private MessageConnector messageConnector;

	private WebServiceDeals webServiceDeals;

	private MessagingMode messagingMode;
	
	private MessageConnectorFactory messageConnectorFactory;
	private DataProviderInterface dataProvider; 
	
	private long updateRate = 10000; 
	private AtomicLong lastUpdate = new AtomicLong(System.currentTimeMillis() - updateRate); 
	
	public int port;
	public String host = null;

	private JourneyDO[] deals;

	
	public HotDealClient(){
		messageConnectorFactory  = new ProductionMessageConnectorFactory();
		dataProvider = new DataProvider(); 
		host = EasyTravelConfig.read().backendHost;
	}
	
	public HotDealClient(MessageConnectorFactory factory, DataProviderInterface dataProvider) {
		if(factory == null || dataProvider == null)
			throw new NullPointerException(); 
		
		this.messageConnectorFactory = factory;
		this.dataProvider = dataProvider;
		host = EasyTravelConfig.read().backendHost;
	}

	@Override
	public Object doExecute(String location, Object... context) {
		if (PluginConstants.LIFECYCLE_PLUGIN_ENABLE.equals(location)) {
			initialize(messageConnectorFactory, dataProvider);
		} else if (PluginConstants.LIFECYCLE_PLUGIN_DISABLE.equals(location)) {
			close();
		} else if (PluginConstants.FRONTEND_SEARCH_JOURNEY_PAGE.equals(location)) {
			return generateDeals(context);
		}
		return null;
	}

	private Object generateDeals(Object... context) {
		if (isInitialized() && timeForRefresh()) {
			deals = getHotDeals();
		}
		return deals;
	}
	
	private boolean timeForRefresh(){
		long currentTime = System.currentTimeMillis();
		if(currentTime - lastUpdate.get() >= updateRate){
			lastUpdate.set(currentTime);
			return true; 
		}
		return false; 
	}
	


	public void setMode(String mode) {

		for (MessagingMode messagingMode : EnumSet.allOf(MessagingMode.class)) {
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
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public void setUpdateRate(long updateRate){
		this.updateRate = updateRate * 1000; 
	}

	protected JourneyDO[] getHotDeals() {
        final Context context = Metrics.getTimerContext(this, "getHotDeals");
		try {
			List<Integer> journeyIds = messageConnector.getHotDealIds();
			return webServiceDeals.getDeals(journeyIds);
		} catch (RemoteException e) {
			log.warn("Unable to receive the hot deals", e.getMessage());
			return new JourneyDO[0];
		} finally {
            context.stop();
            context.close();
        }
    }

	protected void initialize(MessageConnectorFactory messagefactory, DataProviderInterface dataProvider) {
		if (messageConnector == null) {
			messageConnector = messagefactory.create(messagingMode, host, port);
		}

		if (webServiceDeals == null) {
			webServiceDeals = new WebServiceDeals(dataProvider);
		}
	}

	protected boolean isInitialized() {
		return messageConnector != null && webServiceDeals != null;
	}

	private void close() {
		messageConnector = null;
		webServiceDeals = null;
	}

}
