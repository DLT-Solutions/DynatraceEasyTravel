package com.dynatrace.easytravel.frontend.login;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.dynatrace.easytravel.business.client.AuthenticationServiceStub;
import com.dynatrace.easytravel.business.client.VerificationServiceStub;
import com.dynatrace.easytravel.business.webservice.*;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.util.ServiceStubProvider;

import ch.qos.logback.classic.Logger;

public class LoginLogic {

    private static final Logger log = LoggerFactory.make();

	private static final GenericPluginList plugins = new GenericPluginList(PluginConstants.FRONTEND_LOGIN_LOGIC);

	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 *  Note: this is used in the system profile for BTs, do not change/move it!
	 */
	public static void authenticate(String userName, String password, UserContext context) {
		plugins.execute(PluginConstants.FRONTEND_LOGIN, userName);
		doAuthenticate(userName, password, context);
	}

	private static void doAuthenticate(String userName, String password, UserContext context) {
		boolean ok = false;
		try {
			ok = tryInvokeAuthenticate(userName, password) && !isUserBlacklisted(userName);
		} catch (RemoteException e) {
			log.error(e.getMessage());
		}

		context.setUserName(userName);
		context.setPassword(password);
		context.setAuthenticated(ok);
		if (ok) {
			try {
				String fullName = tryInvokeGetFullName(userName);
				context.setFullName(fullName);

				String[] roles = tryInvokeGetUserRoles(userName);
				context.setRoles(new HashSet<String>(Arrays.asList(roles)));

				String loyaltyStatus = tryInvokeGetLoyaltyStatus(userName);
				context.setLoyaltyStatus(loyaltyStatus);

				if(log.isDebugEnabled()) {
					log.debug("fetched fullName: " + fullName + ", roles: " + Arrays.toString(roles) + ", loyaltyStatus: " + loyaltyStatus);
				}
			} catch (RemoteException e) {
				log.error(e.getMessage());
			}
		}
		else {
			Set<String> emptySet = Collections.emptySet();
			context.setRoles(emptySet);
		}
	}

	private static boolean isUserBlacklisted(String userName) throws RemoteException {
		log.debug("try blacklistcheck: " + userName);

		IsUserBlacklistedDocument blackListDoc = IsUserBlacklistedDocument.Factory.newInstance();
		blackListDoc.setIsUserBlacklisted(IsUserBlacklistedDocument.IsUserBlacklisted.Factory.newInstance());
		blackListDoc.getIsUserBlacklisted().setUserName(userName);

		VerificationServiceStub verificationService = ServiceStubProvider.getServiceStub(VerificationServiceStub.class);
		try
		{
			IsUserBlacklistedResponseDocument res = verificationService.isUserBlacklisted(blackListDoc);
			ServiceStubProvider.returnServiceStub(verificationService);
			return res.getIsUserBlacklistedResponse().getReturn();
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(verificationService);
			throw e;
		}
	}

	private static boolean tryInvokeAuthenticate(String userName, String password) throws RemoteException {
		log.debug("try auth: " + userName + ", " + password);

    	AuthenticateDocument doc = AuthenticateDocument.Factory.newInstance();
    	doc.setAuthenticate(AuthenticateDocument.Authenticate.Factory.newInstance());
    	doc.getAuthenticate().setUserName(userName);
    	doc.getAuthenticate().setPassword(password);

		AuthenticationServiceStub authenticationService = ServiceStubProvider.getServiceStub(AuthenticationServiceStub.class);
		try
		{
	    	AuthenticateResponseDocument res = authenticationService.authenticate(doc);
	    	ServiceStubProvider.returnServiceStub(authenticationService);
	    	return res.getAuthenticateResponse().getReturn();
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(authenticationService);
			throw e;
		}
	}

	private static String[] tryInvokeGetUserRoles(String userName) throws RemoteException {
		log.debug("try getroles: " + userName);

    	GetUserRolesDocument userRolesDoc = GetUserRolesDocument.Factory.newInstance();
    	userRolesDoc.setGetUserRoles(GetUserRolesDocument.GetUserRoles.Factory.newInstance());
    	userRolesDoc.getGetUserRoles().setUserName(userName);

		AuthenticationServiceStub authenticationService = ServiceStubProvider.getServiceStub(AuthenticationServiceStub.class);
		try {
			GetUserRolesResponseDocument userRolesRes = authenticationService.getUserRoles(userRolesDoc);
			ServiceStubProvider.returnServiceStub(authenticationService);
			return userRolesRes.getGetUserRolesResponse().getReturnArray();
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(authenticationService);
			throw e;
		}
	}

	private static String tryInvokeGetFullName(String userName) throws RemoteException{
		log.debug("try getroles: " + userName);

		GetFullNameDocument doc = GetFullNameDocument.Factory.newInstance();
		doc.setGetFullName(GetFullNameDocument.GetFullName.Factory.newInstance());
		doc.getGetFullName().setUserName(userName);


		AuthenticationServiceStub authenticationService = ServiceStubProvider.getServiceStub(AuthenticationServiceStub.class);
		try {
			GetFullNameResponseDocument res = authenticationService.getFullName(doc);
			ServiceStubProvider.returnServiceStub(authenticationService);
			return res.getGetFullNameResponse().getReturn();
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(authenticationService);
			throw e;
		}
	}

	private static String tryInvokeGetLoyaltyStatus(String userName) throws RemoteException{
		log.debug("try getloyaltystatus: " + userName);

		GetLoyaltyStatusDocument doc = GetLoyaltyStatusDocument.Factory.newInstance();
		doc.setGetLoyaltyStatus(GetLoyaltyStatusDocument.GetLoyaltyStatus.Factory.newInstance());
		doc.getGetLoyaltyStatus().setUserName(userName);

		AuthenticationServiceStub authenticationService = ServiceStubProvider.getServiceStub(AuthenticationServiceStub.class);
		try {
			GetLoyaltyStatusResponseDocument res = authenticationService.getLoyaltyStatus(doc);
			ServiceStubProvider.returnServiceStub(authenticationService);
			return res.getGetLoyaltyStatusResponse().getReturn();
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(authenticationService);
			throw e;
		}
	}

	public static String getUserPassword(String userName) {
		log.info("getpwd: " + userName);

		plugins.execute(PluginConstants.FRONTEND_PASSWORD, userName);

		GetPasswordDocument userDoc = GetPasswordDocument.Factory.newInstance();
    	userDoc.setGetPassword(GetPasswordDocument.GetPassword.Factory.newInstance());
    	userDoc.getGetPassword().setUserName(userName);

    	final GetPasswordResponseDocument userRes;
		AuthenticationServiceStub authenticationService = ServiceStubProvider.getServiceStub(AuthenticationServiceStub.class);
		try {
	    	userRes = authenticationService.getPassword(userDoc);
	    	ServiceStubProvider.returnServiceStub(authenticationService);
		} catch (RemoteException e) {
			log.error(e.getMessage());
			ServiceStubProvider.invalidateServiceStub(authenticationService);
			return null;
		}

    	return userRes.getGetPasswordResponse().getReturn();
	}

    public static boolean addNewUser(String userName, String fullName, String email, String password) {
    	try {
    		log.info("Adding new user '" + userName + "' with email '" + email);
			return tryAddNewUser(userName, fullName, email, password);
		} catch (RemoteException e) {
			log.error(e.getMessage());
			return false;
		}
    }

    private static boolean tryAddNewUser(String userName, String fullName, String email, String password) throws RemoteException {
    	AddNewUserDocument doc = AddNewUserDocument.Factory.newInstance();
    	doc.setAddNewUser(AddNewUserDocument.AddNewUser.Factory.newInstance());
    	doc.getAddNewUser().setUserName(userName);
    	doc.getAddNewUser().setFullName(fullName);
    	doc.getAddNewUser().setEmail(email);
    	doc.getAddNewUser().setPassword(password);
    	AuthenticationServiceStub authenticationService = ServiceStubProvider.getServiceStub(AuthenticationServiceStub.class);
    	try {
    		AddNewUserResponseDocument res = authenticationService.addNewUser(doc);
    		ServiceStubProvider.returnServiceStub(authenticationService);
    		return res.getAddNewUserResponse().getReturn();
		} catch (RemoteException e) {
			ServiceStubProvider.invalidateServiceStub(authenticationService);
			throw e;
		}
    }
}
