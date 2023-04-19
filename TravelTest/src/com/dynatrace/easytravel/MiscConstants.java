package com.dynatrace.easytravel;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;


public class MiscConstants {

    public static final String USER_NAME = "user_name";
    public static final String USER_NAME1 = "user1";
    public static final String USER_NAME2 = "user2";
    public static final String USER_PASSWORD = "userpassword";
    public static final String USER_PASSWORD1 = "password1";
    public static final String USER_PASSWORD2 = "password2";
    public static final String USER_FULLNAME = "user fullname";

    
    public static final String TENANT_NAME = "tenant";
    public static final String TENANT_NAME1 = "tenant1";
    public static final String TENANT_NAME2 = "tenant2";
    
    public static final String TENANT_PASSWORD = "tenantpwd";
    public static final String TENANT_DESCRIPTION = "tenant description";
    
    public static final String LOCATION_NAME1 = "locationName";
    public static final String LOCATION_NAME2 = "locationName2";
    
    public static final int JOURNEY_ID = 12;
    public static final int JOURNEY_ID1 = 1;
    public static final int JOURNEY_ID2 = 2;
    
    public static final String JOURNEY_NAME = "journey name";
    public static final java.util.Date FROM_DATE = new java.util.Date();
    public static final Calendar FROM_DATE1 = Calendar.getInstance();
    public static final Calendar FROM_DATE2 = Calendar.getInstance();
    public static final java.util.Date TO_DATE = new java.util.Date(System.currentTimeMillis());
    public static final Calendar TO_DATE1 = Calendar.getInstance();
    public static final Calendar TO_DATE2 = Calendar.getInstance();
    public static final double AMOUNT = 1234;
    public static final double AMOUNT1 = 1.0;
    public static final double AMOUNT2 = 2.0;
    public static final byte[] PICTURE1 = new byte[] {1, 2, 3, 4};
    public static final byte[] PICTURE2 = new byte[] {4, 3, 2, 1};
    
    public static final String CC_NUMBER = "1234567890";
    public static final Date VALID_THROUGH = new Date(System.currentTimeMillis() + DateUtils.MILLIS_PER_DAY * 700);
    
    public static final String BOOKING_ID = "bookingid";
    
    public static final Date PAYMENT_DATE = new Date();

    public static final String SCHEDULE_NAME = null;
    public static final long SCHEDULE_PERIOD = 0;
    
    public static final double DELTA = 0;

}