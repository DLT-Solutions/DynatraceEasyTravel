package com.dynatrace.easytravel.frontend.beans;

import static com.dynatrace.easytravel.MiscConstants.*;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;

import java.rmi.RemoteException;
import java.util.Calendar;

import org.junit.Test;

import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.utils.TestHelpers;


public class JourneyBeanTest extends BeanTestBase {


    @Test
    public void testLoadJourney() throws Exception {
        JourneyBean journeyBean = new JourneyBean();
        journeyBean.setDataBean(dataBeanMock);

        expect(dataBeanMock.getDataProvider()).andReturn(dataProviderMock).times(2);
        JourneyDO journey1 = new JourneyDO(JOURNEY_ID, JOURNEY_NAME, Calendar.getInstance(), Calendar.getInstance(), LOCATION_NAME1, LOCATION_NAME2, TENANT_NAME, AMOUNT, null);
        expect(dataProviderMock.getJourneyById(JOURNEY_ID)).andReturn(journey1);

        JourneyDO journey2 = new JourneyDO(JOURNEY_ID1, JOURNEY_NAME, Calendar.getInstance(), Calendar.getInstance(), LOCATION_NAME1, LOCATION_NAME2, TENANT_NAME, AMOUNT, null);
        expect(dataProviderMock.getJourneyById(JOURNEY_ID1)).andReturn(journey2);

        replayMocks();
        journeyBean.setSelectedJourneyId(JOURNEY_ID);
        journeyBean.loadJourney();
        checkJourney(journeyBean, journey1);

        // a second load does not do anything unless the id is the same
        journeyBean.loadJourney();
        checkJourney(journeyBean, journey1);

        // load a different journey  
        journeyBean.setSelectedJourneyId(JOURNEY_ID1);
        journeyBean.loadJourney();
        checkJourney(journeyBean, journey2);

        verifyMocks();
    }


	protected void checkJourney(JourneyBean journeyBean, JourneyDO journey) {
		assertEquals(journey, journeyBean.getSelectedJourney());
        assertEquals(journey.getId(), journeyBean.getSelectedJourneyId());
        assertNotNull(journeyBean.getTripDetailsInclude());
        assertNotNull(journeyBean.getRatingResultsInclude());
        assertNotNull(journeyBean.getRatingActionInclude());
        assertNotNull(journeyBean.getDataBean());
	}


    @Test
    public void testPHPEnabled() throws Exception {
        JourneyBean journeyBean = new JourneyBean();
        assertFalse(journeyBean.isStartPhpServer());
    }

    @Test
    public void testLoadJourneyFails() throws Exception {
        JourneyBean journeyBean = new JourneyBean();
        journeyBean.setDataBean(dataBeanMock);

        expect(dataBeanMock.getDataProvider()).andReturn(dataProviderMock);
        expect(dataProviderMock.getJourneyById(JOURNEY_ID)).andReturn(null);

        replayMocks();
        journeyBean.setSelectedJourneyId(JOURNEY_ID);
        try {
        	journeyBean.loadJourney();
        	fail("Will catch NPE in unit tests");
        } catch (NullPointerException e) {
        	//
        }
        assertNull(journeyBean.getSelectedJourney());
        assertEquals(JOURNEY_ID, journeyBean.getSelectedJourneyId());
        assertNotNull(journeyBean.getDataBean());

        verifyMocks();
    }

    @Test
    public void testLoadJourneyRemoteException() throws Exception {
        JourneyBean journeyBean = new JourneyBean();
        journeyBean.setDataBean(dataBeanMock);

        expect(dataBeanMock.getDataProvider()).andReturn(dataProviderMock);
        expect(dataProviderMock.getJourneyById(JOURNEY_ID)).andThrow(new RemoteException("Testexception"));

        replayMocks();
        journeyBean.setSelectedJourneyId(JOURNEY_ID);
        try {
        	journeyBean.loadJourney();
        	fail("Will catch NPE in unit tests");
        } catch (IllegalStateException e) {
        	TestHelpers.assertContains(e, "Testexception", "Cannot load journey");
        }
        assertNull(journeyBean.getSelectedJourney());
        assertEquals(JOURNEY_ID, journeyBean.getSelectedJourneyId());
        assertNotNull(journeyBean.getDataBean());

        verifyMocks();
    }
}
