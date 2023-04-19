package com.dynatrace.easytravel.frontend.beans;

import static org.junit.Assert.*;

import org.junit.Test;


public class AccountBeanTest extends BeanTestBase {
    
    @Test
    public void testFillMock() {
        AccountBean accountBean = new AccountBean();
        accountBean.fillMock();
        assertNotNull(accountBean.getFirstName());
        assertNotNull(accountBean.getLastName());
        assertNotNull(accountBean.getEmail());
        assertNotNull(accountBean.getPassword());
        assertEquals(accountBean.getPassword(), accountBean.getPasswordConfirm());
    }
    
    
    @Test
    public void testCreateAccount() {
        new DummyFacesContext().setFacesContextInstance();
        AccountBean accountBean = new AccountBean();
        
        assertNull(accountBean.createAccount());
        
        new DummyFacesContext().setFacesContextInstance();
        accountBean = new AccountBean();
        accountBean.fillMock();
        accountBean.setPasswordConfirm(accountBean.getPassword() + "a");
        
        assertNull(accountBean.createAccount());
        
    }
}
