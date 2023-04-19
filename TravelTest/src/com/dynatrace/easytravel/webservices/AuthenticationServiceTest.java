package com.dynatrace.easytravel.webservices;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.dynatrace.easytravel.business.webservice.AuthenticationService;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.jpa.business.Tenant;
import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.model.LoyaltyStatus;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServiceTest extends WSTestBase {

    private static final int VERIFY_LOCATION_TIMEOUT = 3000;
    
    @Before
    public void enableDBSlowdown(){
    	EasyTravelConfig.read().enableDBSlowdown=true;
    }
    
    @After
    public void backToPreviousState(){
    	EasyTravelConfig.resetSingleton();
    }
    
    @Test
    public void testDisableVerifyLocationCall(){
    	//It is the only test that we need to disable DBSlowdown in
    	EasyTravelConfig.read().enableDBSlowdown=false;
    	
    	when(config.isDBSpammingAuthEnabled()).thenReturn(false);
		when(dbAccess.getUser(USER_NAME)).thenReturn(new User(USER_NAME, USER_FULLNAME, USER_EMAIL, USER_PASSWORD));
        assertTrue(authService.getUserRoles(USER_NAME) == null);

        assertNull(authService.getUserRoles("notexisting"));
    	verify(dbAccess, never()).verifyLocation(EasyTravelConfig.read().authServiceGetUserDelay);
    }

	@Test
	public void testAddNewUser() {
		dbAccess.addUser(new User(USER_NAME, USER_FULLNAME, USER_EMAIL, USER_PASSWORD));

		assertThat(authService.addNewUser(USER_NAME, USER_FULLNAME, USER_EMAIL, USER_PASSWORD), is(true));

		when(dbAccess.getUser(USER_NAME)).thenReturn(new User());
		assertThat(authService.addNewUser(USER_NAME, USER_FULLNAME, USER_EMAIL, USER_PASSWORD), is(false));
    }


    @Test
	public void testAuthenticate() {
    	when(config.isDBSpammingAuthEnabled()).thenReturn(false);
		when(dbAccess.getUser(USER_NAME)).thenReturn(new User(USER_NAME, USER_FULLNAME, USER_EMAIL, USER_PASSWORD));
		assertThat(authService.authenticate(USER_NAME, USER_PASSWORD), is(true));
		verify(dbAccess).getUser(USER_NAME);
		verifyNoDBSpammingTest();

		assertFalse(authService.authenticate(USER_NAME,  "wrongpassword"));
		assertFalse(authService.authenticate("nonexistinguser",  null));
    }
    
    /**
     * Check if {@link AuthenticationService.authenticate} method triggers calls about all users 
     */
    @Test
    public void testAuthenticateWithDBSpamming() {    	
    	configureDBSpammingTest(10, 3);

		try {
			authService.authenticate(USER_NAME, USER_PASSWORD);
			verifyDBSpammingTest(10, 3);
		} finally {
			EasyTravelConfig.resetSingleton();
		}
    }
    
    @Test
    public void testAuthenticateWithDBSpammingMultiple() {    	    	
		try {
			configureDBSpammingTest(10, 3);
			authService.authenticate(USER_NAME, USER_PASSWORD);
			verifyDBSpammingTest(10, 3);
			reset(dbAccess);
			
			configureDBSpammingTest(15, 5);
			authService.authenticate(USER_NAME, USER_PASSWORD);
			verifyDBSpammingTest(15, 5);
			reset(dbAccess);
			
			configureDBSpammingTest(2, 5);
			authService.authenticate(USER_NAME, USER_PASSWORD);
			verifyDBSpammingTest(2, 5);
			reset(dbAccess);
			
			configureDBSpammingTest(2, 0);
			authService.authenticate(USER_NAME, USER_PASSWORD);
			verifyDBSpammingTest(2, 0);

		} finally {
			EasyTravelConfig.resetSingleton();
		}
    }
    
    @Test
	public void testAuthenticateNullPassword() {
		when(dbAccess.getUser(USER_NAME)).thenReturn(new User(USER_NAME, USER_FULLNAME, USER_EMAIL, null));
		assertThat(authService.authenticate(USER_NAME, null), is(true));
		assertFalse(authService.authenticate(USER_NAME, "somepassword"));
    }

    @Test
	public void testAuthenticateTenant() {
		when(dbAccess.getTenant(TENANT_NAME)).thenReturn(new Tenant(TENANT_NAME, TENANT_PASSWORD, ""));
//        dbAccess.updateTenant(anyObject(Tenant.class));
		assertThat(authService.authenticateTenant(TENANT_NAME, TENANT_PASSWORD), is(true));

		assertFalse(authService.authenticateTenant(TENANT_NAME, "wrongpassword"));
    }

    @Test
	public void testAuthenticateTenantNullPassword() {
		when(dbAccess.getTenant(TENANT_NAME)).thenReturn(new Tenant(TENANT_NAME, null, ""));
		assertThat(authService.authenticateTenant(TENANT_NAME, null), is(true));
		assertFalse(authService.authenticateTenant(TENANT_NAME, "somepassword"));
    }

    @Test
	public void testGetFullName() {
    	when(config.isDBSpammingAuthEnabled()).thenReturn(false);
		when(dbAccess.getUser(USER_NAME)).thenReturn(new User(USER_NAME, USER_FULLNAME, USER_EMAIL, USER_PASSWORD));
		assertThat(authService.getFullName(USER_NAME), is(USER_FULLNAME));

		assertNull(authService.getFullName("notexisting"));
		
		verifyNoDBSpammingTest();
    }

    @Test
    public void testGetFullNameWithDBSpamming() {
    	configureDBSpammingTest(5,10);
		when(dbAccess.getUser(USER_NAME)).thenReturn(new User(USER_NAME, USER_FULLNAME, USER_EMAIL, USER_PASSWORD));
		try {
			assertThat(authService.getFullName(USER_NAME), is(USER_FULLNAME));
			verifyDBSpammingTest(5, 10);
		} finally {
			EasyTravelConfig.resetSingleton();
		}
    }

    @Test
	public void testGetPassword() {
		when(dbAccess.getUser(USER_NAME)).thenReturn(new User(USER_NAME, USER_FULLNAME, USER_EMAIL, USER_PASSWORD));
		when(config.isDBSpammingAuthEnabled()).thenReturn(false);
		assertThat(authService.getPassword(USER_NAME), is(USER_PASSWORD));
		verifyNoDBSpammingTest();
    }
    
    @Test
    public void testGetPasswordWithDBSpamming() {
    	configureDBSpammingTest(5,10);
    	when(dbAccess.getUser(USER_NAME)).thenReturn(new User(USER_NAME, USER_FULLNAME, USER_EMAIL, USER_PASSWORD));
		try {
			assertThat(authService.getPassword(USER_NAME), is(USER_PASSWORD));
			verifyDBSpammingTest(5, 10);
		} finally {
			EasyTravelConfig.resetSingleton();
		}
    }
    
    @Test
    public void getTenants() {
        List<Tenant> list = new ArrayList<Tenant>();
        list.add(new Tenant(TENANT_NAME, TENANT_PASSWORD, ""));
        list.add(new Tenant(TENANT_NAME2, TENANT_PASSWORD, ""));
		when(dbAccess.allTenants()).thenReturn(list);

        Tenant[] tenants = authService.getTenants();
        assertEquals(list.size(), tenants.length);
        for (int i = 0; i < tenants.length; i++) {
            assertEquals(list.get(i), tenants[i]);
        }

    }

    @Test
    public void getUserRoles() {
    	when(config.isDBSpammingAuthEnabled()).thenReturn(false);
		when(dbAccess.getUser(USER_NAME)).thenReturn(new User(USER_NAME, USER_FULLNAME, USER_EMAIL, USER_PASSWORD));
        assertTrue(authService.getUserRoles(USER_NAME) == null);

        assertNull(authService.getUserRoles("notexisting"));
        verifyNoDBSpammingTest();
    }
    
    @Test
    public void getUserRolesWithDBSpamming() {
    	configureDBSpammingTest(5,10);
    	when(dbAccess.getUser(USER_NAME)).thenReturn(new User(USER_NAME, USER_FULLNAME, USER_EMAIL, USER_PASSWORD));
		try {
			assertTrue(authService.getUserRoles(USER_NAME) == null);
			verifyDBSpammingTest(5, 10);
		} finally {
			EasyTravelConfig.resetSingleton();
		}    	
    }

    
    @Test
    public void getUsers() {
        List<User> list = new ArrayList<User>();
        list.add(new User(USER_NAME, USER_FULLNAME, USER_EMAIL, USER_PASSWORD));
        list.add(new User(USER_NAME2, USER_FULLNAME, USER_EMAIL, USER_PASSWORD));
		when(dbAccess.allUsers()).thenReturn(list);

        User[] users = authService.getUsers();
        assertEquals(list.size(), users.length);
        for (int i = 0; i < users.length; i++) {
            assertEquals(list.get(i), users[i]);
        }
    }

    @Test
    public void getLoyaltyStatus() {
    	User user = new User(USER_NAME, USER_FULLNAME, USER_EMAIL, USER_PASSWORD);
    	user.setLoyaltyStatus(LoyaltyStatus.Gold.name());
		when(dbAccess.getUser(USER_NAME)).thenReturn(user);
    	when(config.isDBSpammingAuthEnabled()).thenReturn(false);
        assertEquals(LoyaltyStatus.Gold.name(), authService.getLoyaltyStatus(USER_NAME));
        
        verifyNoDBSpammingTest();
    }
    
    @Test
    public void getLoyaltyStatusWithDBSpamming() {
    	configureDBSpammingTest(5,10);
    	User user = new User(USER_NAME, USER_FULLNAME, USER_EMAIL, USER_PASSWORD);
    	user.setLoyaltyStatus(LoyaltyStatus.Gold.name());
		when(dbAccess.getUser(USER_NAME)).thenReturn(user);

		try {
			assertEquals(LoyaltyStatus.Gold.name(), authService.getLoyaltyStatus(USER_NAME));
			verifyDBSpammingTest(5, 10);
		} finally {
			EasyTravelConfig.resetSingleton();
		}
    }
    
    @Test
    public void getLoyaltyStatusNone() {
        assertNull(authService.getLoyaltyStatus("notexisting"));
    }
    
    /**
     * stup all mocks to verify if getUser() method performs db spamming DBSpammingAuth problem pattern is enabled
     */
    private void configureDBSpammingTest(int numberOfUsers, int spamSize) {
    	//db spamming enabled
    	when(config.isDBSpammingAuthEnabled()).thenReturn(true);

    	//setup response for dbAccess.allUsers
    	List<User> allUsers = new ArrayList<User>();
    	allUsers.add(new User(USER_NAME, USER_FULLNAME, USER_EMAIL, USER_PASSWORD));
    	for(int i=0; i<numberOfUsers; i++) {
    		String userStr = "user" +i;
    		User user = new User(userStr, userStr, userStr, userStr);
    		allUsers.add(user);
    	}
    	when(dbAccess.allUsers()).thenReturn(allUsers);
    	
		//set delay for getLocations method
		EasyTravelConfig etConfig = EasyTravelConfig.read();
		etConfig.authServiceGetUserDelay = VERIFY_LOCATION_TIMEOUT;
		etConfig.authServiceSpamSize = spamSize;
    }
    
    /**
     * Check that all methods were called in case of db spamming
     */
    private void verifyDBSpammingTest(int numberOfUsers, int spamSize) {
		verify(dbAccess).allUsers();
		//in case when spamSize < numberOfUsers we can have one additional query for the user 
		verify(dbAccess, atLeast(spamSize)).getUser(anyString());		
		verify(dbAccess, atMost(spamSize+1)).getUser(anyString());

		verify(dbAccess, times(spamSize)).verifyLocation(VERIFY_LOCATION_TIMEOUT);		    	
    }
    
    /**
     * Check that certain method were called/not called when db spamming is not enabled
     */
    private void verifyNoDBSpammingTest() {
		verify(dbAccess, never()).allUsers();
		verify(dbAccess, atLeast(1)).verifyLocation(EasyTravelConfig.read().authServiceGetUserDelay);
		verify(dbAccess).getUser(USER_NAME);    	
    }
}
