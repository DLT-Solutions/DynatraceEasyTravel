package com.dynatrace.easytravel.frontend.beans;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestSearchDelayUtility {
    
    
    private static final int WAIT_TIME = 1000;

    
    @Test
    public void testDoWait() throws Exception {
        SearchDelayUtility searchDelayUtility = new SearchDelayUtility();
        
        for (SearchDelayStrategyEnum sds : SearchDelayStrategyEnum.values()) {
            long time = System.currentTimeMillis();
            searchDelayUtility.doWait(WAIT_TIME, sds);
            time = System.currentTimeMillis() - time;
            if (sds == SearchDelayStrategyEnum.NONE) {
                assertTrue("time should have been less than 1000ms but was " + time + " ms", time < WAIT_TIME);
            } else {
                assertTrue("time should have been at least 1000ms but was " + time + " ms", time >= WAIT_TIME);
            }
        }
    }
}
