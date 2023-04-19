package com.dynatrace.easytravel.frontend.beans;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static com.dynatrace.easytravel.MiscConstants.*;

import org.junit.Test;

import com.dynatrace.easytravel.frontend.data.UserDO;


public class LoginBeanTest extends BeanTestBase {

    private static final String USER_PASSWORD = "user_password";

    
    @Test
    public void testRetrieveUsers() throws Exception {
        LoginBean loginBean = new LoginBean();
        
        loginBean.setDataBean(dataBeanMock);
        
        UserDO user = new UserDO(USER_NAME, USER_PASSWORD);
        UserDO user2 = new UserDO(USER_NAME + 1, USER_PASSWORD + 1);
        UserDO user3 = new UserDO(USER_NAME + 2, USER_PASSWORD + 2);
        
        expect(dataBeanMock.getDataProvider()).andReturn(dataProviderMock);
        expect(dataProviderMock.getUsersWithPrefix(null)).andReturn(new UserDO[] {user, user2, user3});
        
        replayMocks();
        
        loginBean.retrieveUsers();
        assertEquals(user, loginBean.getUsers()[0]);
        
        verifyMocks();
    }
    
    
}