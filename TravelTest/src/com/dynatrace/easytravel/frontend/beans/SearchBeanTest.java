package com.dynatrace.easytravel.frontend.beans;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static com.dynatrace.easytravel.MiscConstants.*;

import java.util.Calendar;
import java.util.Date;

import javax.faces.component.UIComponentBase;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.junit.Test;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.frontend.data.LocationDO;
import com.icesoft.faces.component.selectinputtext.SelectInputText;
import com.icesoft.faces.component.selectinputtext.TextChangeEvent;

public class SearchBeanTest extends BeanTestBase {
    private static final String LOCATION_NAME = "location name";
    private static final String LOCATION_NAME2 = null;
    private static final int JOURNEY_ID2 = 0;
    private static final String JOURNEY_NAME2 = null;
    private static final Date FROM_DATE = null;
    private static final Date TO_DATE = null;

    @Test
    public void testSearchDestination() throws Exception {
        
        SearchBean searchBean = new SearchBean();
        searchBean.setDataBean(dataBeanMock);
        
        expect(dataBeanMock.getDataProvider()).andReturn(dataProviderMock);
        LocationDO location = new LocationDO(LOCATION_NAME);
        LocationDO[] locations = new LocationDO[] {location};
        expect(dataProviderMock.findLocations(LOCATION_NAME, SearchBean.LIMIT_LOCATIONS)).andReturn(locations);
        
        replayMocks();
        
        TextChangeEvent tce = new TextChangeEvent(new UIComponentBase() {
            
            @Override
            public String getFamily() {
                // TODO Auto-generated method stub
                return null;
            }
        }, null, LOCATION_NAME);
        searchBean.searchDestination(tce);
        assertEquals(LOCATION_NAME, searchBean.getSelectList().get(0).getLabel());
        assertEquals(location, searchBean.getSelectList().get(0).getValue());
        
        verifyMocks();
        
    }
    
    
    @Test
    public void testSearchJourneys() throws Exception {
        SearchBean searchBean = new SearchBean();
        searchBean.setDataBean(dataBeanMock);
        
        expect(dataBeanMock.getDataProvider()).andReturn(dataProviderMock);
        LocationDO location1 = new LocationDO(LOCATION_NAME);
        LocationDO location2 = new LocationDO(LOCATION_NAME2);
        
        
        expect(dataProviderMock.findLocations(LOCATION_NAME, SearchBean.LIMIT_LOCATIONS_INEXACT)).andReturn(new LocationDO[] {location1, location2});
        JourneyDO journey1 = new JourneyDO(JOURNEY_ID, JOURNEY_NAME, Calendar.getInstance(), Calendar.getInstance(), LOCATION_NAME, LOCATION_NAME2, TENANT_NAME, AMOUNT, null);
        JourneyDO journey2 = new JourneyDO(JOURNEY_ID2, JOURNEY_NAME2, Calendar.getInstance(), Calendar.getInstance(), LOCATION_NAME2, LOCATION_NAME, TENANT_NAME, AMOUNT, null);
        
        expect(dataBeanMock.getDataProvider()).andReturn(dataProviderMock);
        expect(dataProviderMock.findJourneys(LOCATION_NAME, null, null)).andReturn(new JourneyDO[] {journey1});
        expect(dataBeanMock.getDataProvider()).andReturn(dataProviderMock);
        expect(dataProviderMock.findJourneys(LOCATION_NAME2, null, null)).andReturn(new JourneyDO[] {journey2});
        
        replayMocks();
        
        SearchDelayStateBean searchDelayStateBean = new SearchDelayStateBean();
        searchBean.setSearchDelayStateBean(searchDelayStateBean);
        searchBean.setDestinationName(LOCATION_NAME);
        searchBean.searchJourneys(new ActionEvent(new UIComponentBase() {
            
            @Override
            public String getFamily() {
                // TODO Auto-generated method stub
                return null;
            }
        }));
        
        assertEquals(1, searchBean.getPageCount());
        assertEquals(2, searchBean.getJourneyCount());
        assertEquals(journey1, searchBean.getJourneys()[0]);
        assertEquals(journey2, searchBean.getJourneys()[1]);
        assertEquals(journey1, searchBean.getJourneyPage()[0]);
        assertEquals(journey2, searchBean.getJourneyPage()[1]);
        verifyMocks();
        
    }
    
    
    @Test
    public void testSearchJourneysExact() throws Exception {
        SearchBean searchBean = new SearchBean();
        searchBean.setDataBean(dataBeanMock);
        
        expect(dataBeanMock.getDataProvider()).andReturn(dataProviderMock);
        final LocationDO location = new LocationDO(LOCATION_NAME);
        LocationDO[] locations = new LocationDO[] {location};
        expect(dataProviderMock.findLocations(LOCATION_NAME, SearchBean.LIMIT_LOCATIONS)).andReturn(locations);
        
        JourneyDO journey = new JourneyDO(JOURNEY_ID, JOURNEY_NAME, Calendar.getInstance(), Calendar.getInstance(), LOCATION_NAME, LOCATION_NAME2, TENANT_NAME, AMOUNT, null);
        
        expect(dataBeanMock.getDataProvider()).andReturn(dataProviderMock);
        expect(dataProviderMock.findJourneys(LOCATION_NAME, FROM_DATE, TO_DATE)).andReturn(new JourneyDO[] {journey});
        
        
        replayMocks();
        
        searchBean.searchDestination(new TextChangeEvent(new UIComponentBase() {
            
            @Override
            public String getFamily() {
                // TODO Auto-generated method stub
                return null;
            }
        }, null, LOCATION_NAME));
        SearchDelayStateBean searchDelayStateBean = new SearchDelayStateBean();
        searchBean.setSearchDelayStateBean(searchDelayStateBean);
        
        searchBean.setDestinationName(LOCATION_NAME);
        searchBean.selectDestination(new ActionEvent(new SelectInputText() {
            @Override
            public SelectItem getSelectedItem() {
                return new SelectItem(location);
            }
        }));
        searchBean.setFromDate(FROM_DATE);
        searchBean.setToDate(TO_DATE);
        
        searchBean.searchJourneys(new ActionEvent(new UIComponentBase() {
            
            @Override
            public String getFamily() {
                // TODO Auto-generated method stub
                return null;
            }
        }));
        
        assertEquals(1, searchBean.getPageCount());
        assertEquals(1, searchBean.getJourneyCount());
        assertEquals(journey, searchBean.getJourneys()[0]);
        assertEquals(1, searchBean.getPageList().size());
        searchBean.clearSearch();
        assertNull(searchBean.getJourneys());
        assertNull(searchBean.getJourneyPage());
        //assertEquals(-1, searchBean.getJourneyCount());
        assertNull(searchBean.getDestinationName());
        assertNull(searchBean.getFromDate());
        assertNull(searchBean.getToDate());
        assertEquals(BaseConstants.EMPTY_STRING, searchBean.getFromDateAsString());
        assertEquals(BaseConstants.EMPTY_STRING, searchBean.getToDateAsString());
        assertEquals(0, searchBean.getPageList().size());
        
        verifyMocks();
        
    }
    
}
