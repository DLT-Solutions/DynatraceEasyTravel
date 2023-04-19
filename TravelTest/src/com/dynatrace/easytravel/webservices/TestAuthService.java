package com.dynatrace.easytravel.webservices;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.logging.Logger;

import com.dynatrace.easytravel.business.client.AuthenticationServiceStub;
import com.dynatrace.easytravel.business.webservice.AuthenticateDocument;
import com.dynatrace.easytravel.business.webservice.AuthenticateResponseDocument;
import com.dynatrace.easytravel.business.webservice.GetUserRolesDocument;
import com.dynatrace.easytravel.business.webservice.GetUserRolesResponseDocument;
import com.dynatrace.easytravel.util.ServiceStubProvider;

public class TestAuthService {
	
    private static Logger log = Logger.getLogger(TestAuthService.class.getName());
	
	public static void main(String args[]) throws RemoteException {
    
		log.info("Arguments: " + Arrays.toString(args));
    	String userName = args.length > 0 ? args[0] : "hainer";
    	String password = args.length > 1 ? args[1] : "hainer";
    	AuthenticationServiceStub authenticationService = ServiceStubProvider.getServiceStub(AuthenticationServiceStub.class);
    	
    	AuthenticateDocument doc = AuthenticateDocument.Factory.newInstance();
    	doc.setAuthenticate(AuthenticateDocument.Authenticate.Factory.newInstance());
    	doc.getAuthenticate().setUserName(userName);
    	doc.getAuthenticate().setPassword(password);
    	
    	AuthenticateResponseDocument res = authenticationService.authenticate(doc);

    	log.info("auth result: " + res.getAuthenticateResponse().getReturn());
    	
    	GetUserRolesDocument userRolesDoc = GetUserRolesDocument.Factory.newInstance();
    	userRolesDoc.setGetUserRoles(GetUserRolesDocument.GetUserRoles.Factory.newInstance());
    	userRolesDoc.getGetUserRoles().setUserName(userName);
    	
    	GetUserRolesResponseDocument userRolesRes = authenticationService.getUserRoles(userRolesDoc);
    	log.info("user roles: " + Arrays.toString(userRolesRes.getGetUserRolesResponse().getReturnArray()));
    }
}
