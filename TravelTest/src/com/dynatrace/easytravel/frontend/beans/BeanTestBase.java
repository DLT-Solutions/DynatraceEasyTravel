package com.dynatrace.easytravel.frontend.beans;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import org.junit.Before;

import com.dynatrace.easytravel.frontend.data.DataProvider;
import com.dynatrace.easytravel.frontend.data.DataProviderInterface;
import com.dynatrace.easytravel.frontend.login.UserContext;
import com.dynatrace.easytravel.spring.SpringTestBase;


public class BeanTestBase extends SpringTestBase {
    
    protected DataBean dataBeanMock;
    protected DataProviderInterface dataProviderMock;
    
    protected LoginBean loginBeanMock;
    protected UserContext userContextMock;
    
    protected BookingBean bookingBeanMock;
    
    
    
    @Before
    public void initMocks() {
        dataBeanMock = createMock(DataBean.class);
        dataProviderMock = createMock(DataProvider.class);
        
        loginBeanMock = createMock(LoginBean.class);
        userContextMock = createMock(UserContext.class);
        
        bookingBeanMock = createMock(BookingBean.class);
    }
    
    
    protected void resetMocks() {
        reset(dataBeanMock, dataProviderMock, loginBeanMock, userContextMock, bookingBeanMock);
    }
   
    
    protected void replayMocks() {
        replay(dataBeanMock, dataProviderMock, loginBeanMock, userContextMock, bookingBeanMock);
    }
    
    
    protected void verifyMocks() {
        verify(dataBeanMock, dataProviderMock, loginBeanMock, userContextMock, bookingBeanMock);
    }
    
}
