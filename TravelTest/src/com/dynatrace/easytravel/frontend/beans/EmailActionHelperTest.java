package com.dynatrace.easytravel.frontend.beans;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EmailActionHelperTest {

    @Test
    public void testIsValidEmailAddress() {
        assertTrue(EmailActionHelper.isValidEmailAddress("test@test.com"));
        assertTrue(EmailActionHelper.isValidEmailAddress("test.test@test.com"));
        assertTrue(EmailActionHelper.isValidEmailAddress("max.muster@dynatrace.com"));
        assertFalse(EmailActionHelper.isValidEmailAddress("test @test.com"));
        assertFalse(EmailActionHelper.isValidEmailAddress("test@ test.com"));
        assertFalse(EmailActionHelper.isValidEmailAddress("test@test"));
        assertFalse(EmailActionHelper.isValidEmailAddress("test@test@test.com"));
    }
    
}
