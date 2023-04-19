package com.dynatrace.easytravel.frontend.beans;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import static com.dynatrace.easytravel.MiscConstants.*;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import com.dynatrace.easytravel.frontend.data.JourneyDO;


public class AdBeanTest extends BeanTestBase {

    @Test
    public void testGetCurrentPromotion() throws Exception {
        AdBean adBean = new AdBean();
        adBean.setDataBean(dataBeanMock);
        
        JourneyDO journey = new JourneyDO(JOURNEY_ID, JOURNEY_NAME, Calendar.getInstance(), Calendar.getInstance(), LOCATION_NAME1, LOCATION_NAME2, TENANT_NAME, AMOUNT, null);
        JourneyDO[] journeys = new JourneyDO[] {journey};
        
        expect(dataBeanMock.getDataProvider()).andReturn(dataProviderMock);
        expect(dataProviderMock.findJourneys(anyObject(String.class), anyObject(Date.class), anyObject(Date.class))).andReturn(journeys);
        
        replayMocks();
        
        Promotion promotion = adBean.getCurrentPromotion();
        assertEquals(journey, promotion.getJourney());
        
        verifyMocks();
        
    }
    
}
