package com.dynatrace.easytravel.frontend.beans;

import static com.dynatrace.easytravel.MiscConstants.USER_FULLNAME;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.dynatrace.easytravel.frontend.beans.BookingBean.BookingState;

public class BookingRequestBeanTest extends BeanTestBase {


    @Test
    public void testFillMock() {
        BookingRequestBean bookingRequestBean = new BookingRequestBean();

        bookingRequestBean.setLoginBean(loginBeanMock);

        expect(loginBeanMock.getUserContext()).andReturn(userContextMock);
        expect(userContextMock.getFullName()).andReturn(USER_FULLNAME);

        replayMocks();

        bookingRequestBean.fillMock();
        assertNotNull(bookingRequestBean.getCreditCardNumber());
        assertNotNull(bookingRequestBean.getVerificationNumber());
        assertNotNull(bookingRequestBean.getExpirationMonth());
        String expirationYear = bookingRequestBean.getExpirationYear();
		assertNotNull(expirationYear);
        assertTrue("Had: " + expirationYear, Integer.parseInt(expirationYear) > 2015);
        assertEquals(USER_FULLNAME, bookingRequestBean.getCreditCardOwner());

        verifyMocks();
    }


    @Test
    public void testValidatePayment() throws Exception {
        new DummyFacesContext().setFacesContextInstance();
        BookingRequestBean bookingRequestBean = new BookingRequestBean();
        assertNull(bookingRequestBean.validatePayment());

        new DummyFacesContext().setFacesContextInstance();
        bookingRequestBean = new BookingRequestBean();
        bookingRequestBean.setDataBean(dataBeanMock);
        bookingRequestBean.setBookingBean(bookingBeanMock);
        bookingRequestBean.setLoginBean(loginBeanMock);

        expect(loginBeanMock.getUserContext()).andReturn(userContextMock);
        expect(userContextMock.getFullName()).andReturn(USER_FULLNAME);

        expect(dataBeanMock.getDataProvider()).andReturn(dataProviderMock);
        expect(dataProviderMock.checkCreditCard(anyObject(String.class))).andReturn(true);
        bookingBeanMock.setBookingState(BookingState.initial);
        expectLastCall();
        bookingBeanMock.setCreditCardNumber(anyObject(String.class));
        expectLastCall();
        replayMocks();

        bookingRequestBean.fillMock();
        assertEquals(Pages.NAVIGATION_CASE_PAYMENT_VALIDATED, bookingRequestBean.validatePayment());

        verifyMocks();
    }




}
