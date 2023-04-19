package com.dynatrace.easytravel.webservices;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.Mock;

import com.dynatrace.easytravel.DummyNativeApplication;
import com.dynatrace.easytravel.DummyPaymentService;
import com.dynatrace.easytravel.business.cache.LocationCache;
import com.dynatrace.easytravel.business.webservice.AuthenticationService;
import com.dynatrace.easytravel.business.webservice.BookingService;
import com.dynatrace.easytravel.business.webservice.Configuration;
import com.dynatrace.easytravel.business.webservice.JourneyService;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.plugin.PluginTestBase;
import com.dynatrace.easytravel.spring.Plugin;
import com.dynatrace.easytravel.spring.SpringUtils;


public abstract class WSTestBase extends PluginTestBase {

	@Mock
	protected DataAccess dbAccess;

	@Mock
	protected Configuration config;

    protected static final Date FROM_DATE = new Date();

    protected static final Date TO_DATE = new Date(FROM_DATE.getTime() + 14 * DateUtils.MILLIS_PER_DAY);

    protected static final int JOURNEY_ID = 0;

    protected static final String JOURNEY_NAME = "journey name";

    protected static final String JOURNEY_NAME2 = "journey name2";

    protected static final String JOURNEY_FROM = "journey from";

    protected static final String JOURNEY_TO = "journey to";

    protected static final double JOURNEY_AMOUNT = 124;

    protected static final byte[] JOURNEY_PICTURE = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

    protected static final Location LOCATION1 = new Location("location1");

    protected static final Location LOCATION2 = new Location("location2");

    protected static final int PAGE_COUNT = 10;

    protected static final int PAGE_FROM = 0;

    protected static final String TENANT_NAME = "tenant name";

    protected static final String TENANT_NAME2 = "tenant name2";

    protected static final String TENANT_PASSWORD = "tenant pwd";

    protected static final String USER_FULLNAME = "user fullname";

    protected static final String USER_EMAIL = "user email";

    protected static final String USER_NAME = "user name";

    protected static final String USER_NAME2 = "user name2";

    protected static final String USER_PASSWORD = "user pwd";



    protected AuthenticationService authService;

    protected BookingService bookingService;

    protected JourneyService journeyService;


    @BeforeClass
    public static void initSpring() {
        SpringUtils.initBusinessBackendContextForTest();
        Plugin dummyNativeApplication = new DummyNativeApplication();
        SpringUtils.getPluginHolder().addPlugin(dummyNativeApplication);
        SpringUtils.getPluginStateProxy().setPluginEnabled(dummyNativeApplication.getName(), true);
        Plugin dummyPaymentService = new DummyPaymentService();
        SpringUtils.getPluginHolder().addPlugin(dummyPaymentService);
        SpringUtils.getPluginStateProxy().setPluginEnabled(dummyPaymentService.getName(), true);
    }


    @AfterClass
    public static void disposeSpring() {
        SpringUtils.disposeBusinessBackendContext();
    }


    @Before
    public void initMocks() {

        bookingService = new BookingService();
        bookingService.setDatabaseAccess(dbAccess);
        bookingService.setConfiguration(config);
        authService = new AuthenticationService();
        authService.setDatabaseAccess(dbAccess);
        authService.setConfiguration(config);
        journeyService = new JourneyService();
        journeyService.setDatabaseAccess(dbAccess);
        journeyService.setConfiguration(config);
        LocationCache locationCache = new LocationCache();
        journeyService.setLocationCache(locationCache);
    }


}
