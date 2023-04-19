package com.dynatrace.easytravel.launcher.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Test;

import com.dynatrace.easytravel.constants.BaseConstants;


public class ArchitectureTest {

    @Test
    public void testPickUp() {
        Assert.assertNotNull(Architecture.pickUp());
    }
    
    @Test
    public void testUnknown() {
    	assertNotNull(Architecture.UNKNOWN.toString());
    	
    	String prev = System.getProperty(BaseConstants.SystemProperties.OS_ARCH);
    	System.setProperty(BaseConstants.SystemProperties.OS_ARCH, "");
    	try {
    		assertEquals(Architecture.UNKNOWN, Architecture.pickUp());
    		
    		System.setProperty(BaseConstants.SystemProperties.OS_ARCH, "x86");
    		assertEquals(Architecture.BIT32, Architecture.pickUp());
    		assertEquals("lib", Architecture.pickUp().getLibDir());

    		System.setProperty(BaseConstants.SystemProperties.OS_ARCH, "x86_64");
    		assertEquals(Architecture.BIT64, Architecture.pickUp());
    		assertEquals("lib64", Architecture.pickUp().getLibDir());
    		
    		System.setProperty(BaseConstants.SystemProperties.OS_ARCH, "someother");
    		assertEquals(Architecture.UNKNOWN, Architecture.pickUp());
    		assertEquals("lib", Architecture.pickUp().getLibDir());
    	} finally {
    		System.setProperty(BaseConstants.SystemProperties.OS_ARCH, prev);
    	}
    }
}
