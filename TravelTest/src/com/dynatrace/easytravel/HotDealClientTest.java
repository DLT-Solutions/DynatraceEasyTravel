package com.dynatrace.easytravel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.frontend.data.DataProviderInterface;
import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.spring.PluginConstants;


public class HotDealClientTest {
	private HotDealClient client;
	private MessageConnectorFactory messagefactory;
	private DataProviderInterface dataProvider;
	private long updateRate = 10; // in seconds


	@Before
	public void setUp () {
		messagefactory = new MockedConnectorFactory();
		dataProvider = mock(DataProviderInterface.class);
		client = new HotDealClient(messagefactory, dataProvider);
		client.setEnabled(true);
		client.setUpdateRate(updateRate);
	}

	@Test
	public void doExecuteCausesInitiation () {
		setRmiConfiguration();
		client.doExecute(PluginConstants.LIFECYCLE_PLUGIN_ENABLE);
		assertEquals(client.isInitialized(), true);
	}

	@Test
	public void doExcecuteReturnsValidValues() throws RemoteException, InterruptedException{

		setRmiConfiguration();
		initialize();

		List<JourneyDO> journeys = getJourneys();

		prepareDataProvider(journeys);

		JourneyDO[]  returnedJourneys =
			(JourneyDO[]) client.doExecute(PluginConstants.FRONTEND_SEARCH_JOURNEY_PAGE);

		assertEquals(returnedJourneys.length, 4);

		assertEquals(journeys.get(0), returnedJourneys[0]);
		assertEquals(journeys.get(1), returnedJourneys[1]);
		assertEquals(journeys.get(2), returnedJourneys[2]);
		assertEquals(journeys.get(3), returnedJourneys[3]);

		returnedJourneys =
			(JourneyDO[]) client.doExecute(PluginConstants.FRONTEND_SEARCH_JOURNEY_PAGE);

		assertEquals(returnedJourneys.length, 4);

		assertEquals(journeys.get(0), returnedJourneys[0]);
		assertEquals(journeys.get(1), returnedJourneys[1]);
		assertEquals(journeys.get(2), returnedJourneys[2]);
		assertEquals(journeys.get(3), returnedJourneys[3]);

		Thread.sleep(updateRate * 1100);


		returnedJourneys =
			(JourneyDO[]) client.doExecute(PluginConstants.FRONTEND_SEARCH_JOURNEY_PAGE);

		assertEquals(returnedJourneys.length, 4);
		assertEquals(journeys.get(4), returnedJourneys[0]);
		assertEquals(journeys.get(5), returnedJourneys[1]);
		assertEquals(journeys.get(6), returnedJourneys[2]);
		assertEquals(journeys.get(7), returnedJourneys[3]);
	}

	@Test
	public void doExecuteFiltersPreviouslyShownDeals () throws RemoteException, InterruptedException {
		setRmiConfiguration();
		initialize();

		List<JourneyDO> journeys = getJourneys();

		prepareDataProvider(journeys);

		JourneyDO[]  returnedJourneys =
			(JourneyDO[]) client.doExecute(PluginConstants.FRONTEND_SEARCH_JOURNEY_PAGE);
		assertEquals(4, returnedJourneys.length);
	}

	@Ignore("This test requires that the RMI server is running. You can use it as local integration test")
	@Test
	public void integrationTest() throws RemoteException, InterruptedException {

		messagefactory = new ProductionMessageConnectorFactory();
		client = new HotDealClient(messagefactory, dataProvider);
		setRmiConfiguration();
		
		//overwrite some settings
		client.setPort(11230);
		//update this if you are testing on remote host 
		client.setHost("localhost");
		
		List<JourneyDO> journeys = getJourneys();
		prepareDataProvider(journeys);
		
		
		initialize();

		JourneyDO[]  returnedJourneys = (JourneyDO[]) client.doExecute(PluginConstants.FRONTEND_SEARCH_JOURNEY_PAGE);
		assertEquals(4, returnedJourneys.length);

		Thread.sleep(10000);
		JourneyDO[] toFilter = {journeys.get(0), journeys.get(1)};
		/*JourneyDO[] */ returnedJourneys =
			(JourneyDO[]) client.doExecute(PluginConstants.FRONTEND_SEARCH_JOURNEY_PAGE, (Object[])
					toFilter);
		assertEquals(4, returnedJourneys.length);
		
		
		returnedJourneys =
			(JourneyDO[]) client.doExecute(PluginConstants.FRONTEND_SEARCH_JOURNEY_PAGE, (Object[]) returnedJourneys);
		assertEquals(4, returnedJourneys.length);


		Thread.sleep(10000);
		returnedJourneys = (JourneyDO[]) client.doExecute(PluginConstants.FRONTEND_SEARCH_JOURNEY_PAGE);
		assertEquals(4, returnedJourneys.length);
	}

	@Test
	public void getHotDealsThrowsRemoteException() throws RemoteException {
		when(dataProvider.getJourneyById(anyInt())).thenThrow(new RemoteException());
		setRmiConfiguration();
		initialize();
		//		exception.expect(RemoteException.class);
		// Exception is handled within getHotDeals method
		JourneyDO[] emptyJourneys = client.getHotDeals();
		assertEquals(0, emptyJourneys.length);
	}

	@Test
	public void doExecuteCausesClose () {
		setRmiConfiguration();
		client.doExecute(PluginConstants.LIFECYCLE_PLUGIN_DISABLE);
		assertEquals(client.isInitialized(), false);
	}

	@Test
	public void initializeWorksCorrectlyRmi () {
		setRmiConfiguration();
		initialize();
		assertEquals(client.isInitialized(), true);
	}

	@Test
	public void initializeWorksCorrectlyJms () {
		setJmsConfiguration();
		initialize();
		assertEquals(client.isInitialized(), true);
	}
	
	private void initialize() {
		client.initialize(messagefactory, dataProvider);
	}

	private void setJmsConfiguration() {
		client.setPort(8080);
		client.setMode("JMS");
	}

	private void setRmiConfiguration() {
		client.setPort(1130);
		client.setMode("RMI");
	}

	private void prepareDataProvider(List<JourneyDO> journeys) throws RemoteException {
		for(int i = 0, n = journeys.size(); i < n; i++) {
			when(dataProvider.getJourneyById(i + 1)).thenReturn(journeys.get(i));
		}
	}

	private List<JourneyDO> getJourneys() {
		List<JourneyDO> journeys = new ArrayList<JourneyDO>();
		journeys.add(new JourneyDO(1, "One", null, null, null, null, null, 100, null));
		journeys.add(new JourneyDO(2, "Two", null, null, null, null, null, 200, null));
		journeys.add(new JourneyDO(3, "Three", null, null, null, null, null, 300, null));
		journeys.add(new JourneyDO(4, "Four", null, null, null, null, null, 400, null));
		journeys.add(new JourneyDO(5, "One", null, null, null, null, null, 100, null));
		journeys.add(new JourneyDO(6, "Two", null, null, null, null, null, 200, null));
		journeys.add(new JourneyDO(7, "Three", null, null, null, null, null, 300, null));
		journeys.add(new JourneyDO(8, "Four", null, null, null, null, null, 400, null));
		return journeys;
	}
}
