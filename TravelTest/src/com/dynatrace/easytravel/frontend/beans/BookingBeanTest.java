package com.dynatrace.easytravel.frontend.beans;

import static com.dynatrace.easytravel.MiscConstants.AMOUNT;
import static com.dynatrace.easytravel.MiscConstants.BOOKING_ID;
import static com.dynatrace.easytravel.MiscConstants.CC_NUMBER;
import static com.dynatrace.easytravel.MiscConstants.JOURNEY_NAME;
import static com.dynatrace.easytravel.MiscConstants.LOCATION_NAME1;
import static com.dynatrace.easytravel.MiscConstants.LOCATION_NAME2;
import static com.dynatrace.easytravel.MiscConstants.TENANT_NAME;
import static com.dynatrace.easytravel.MiscConstants.USER_NAME;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.junit.Test;

import com.dynatrace.easytravel.frontend.beans.BookingBean.BookingState;
import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.misc.UserType;


public class BookingBeanTest extends BeanTestBase {

    @Test
    public void testBookingBean() throws Exception {
        BookingBean bookingBean = new BookingBean();

        bookingBean.setDataBean(dataBeanMock);
        bookingBean.setLoginBean(loginBeanMock);
      //loadJourney
        expect(dataBeanMock.getDataProvider()).andReturn(dataProviderMock);
        JourneyDO journey = new JourneyDO(1, JOURNEY_NAME, Calendar.getInstance(), Calendar.getInstance(), LOCATION_NAME1, LOCATION_NAME2, TENANT_NAME, AMOUNT, null);
        expect(dataProviderMock.getJourneyById(1)).andReturn(journey);
      //performBooking
        expect(loginBeanMock.getUserContext()).andReturn(userContextMock);
        expect(userContextMock.isAuthenticated()).andReturn(true);
        expect(loginBeanMock.getUserContext()).andReturn(userContextMock);
        expect(userContextMock.getUserName()).andReturn(USER_NAME);
        expect(dataBeanMock.getDataProvider()).andReturn(dataProviderMock);
        expect(dataProviderMock.storeBooking(1, USER_NAME, UserType.WEB, CC_NUMBER, 2 * AMOUNT)).andReturn(BOOKING_ID);
        expect(loginBeanMock.getUserContext()).andReturn(userContextMock);
        expect(userContextMock.isAuthenticated()).andReturn(true);
        expect(loginBeanMock.getUserContext()).andReturn(userContextMock);
        expect(userContextMock.isAuthenticated()).andReturn(true);
        expect(loginBeanMock.getUserContext()).andReturn(userContextMock);
        expect(userContextMock.isAuthenticated()).andReturn(true);
        expect(loginBeanMock.getUserContext()).andReturn(userContextMock);
        expect(userContextMock.isAuthenticated()).andReturn(false);
        expect(loginBeanMock.getUserContext()).andReturn(userContextMock);
        expect(userContextMock.isAuthenticated()).andReturn(false);
        expect(loginBeanMock.getUserContext()).andReturn(userContextMock);
        expect(userContextMock.isAuthenticated()).andReturn(false);

        replayMocks();

      //loadJourney
        bookingBean.setBookingState(BookingState.initial);
        bookingBean.setSelectedJourneyId(1);
        bookingBean.loadJourney();
        assertEquals(1, bookingBean.getSelectedJourneyId());
        assertEquals(journey, bookingBean.getSelectedJourney());
      //performBooking
        bookingBean.setCreditCardNumber(CC_NUMBER);
        assertTrue(bookingBean.isStateValid());
        bookingBean.performBooking();
        assertEquals(BookingState.success, bookingBean.getBookingState());
        assertEquals(BOOKING_ID, bookingBean.getBookingId());
        assertEquals(Pages.INCLUDE_BOOKING_REVIEW, bookingBean.getReviewInclude());
        assertEquals(Pages.INCLUDE_BOOKING_PAYMENT, bookingBean.getPaymentInclude());
        assertEquals(Pages.INCLUDE_BOOKING_FINISH, bookingBean.getFinishInclude());
        assertEquals(Pages.INCLUDE_BOOKING_LOGIN, bookingBean.getReviewInclude());
        assertEquals(Pages.INCLUDE_BOOKING_LOGIN, bookingBean.getPaymentInclude());
        assertEquals(Pages.INCLUDE_BOOKING_LOGIN, bookingBean.getFinishInclude());
        assertEquals(Pages.INCLUDE_BOOKING_LOGIN, bookingBean.getLoginInclude());
        JourneyAccount journeyAccount = bookingBean.getAccountMap().get(journey);
        assertEquals(journey, journeyAccount.getJourney());
        verifyMocks();

    }

}
