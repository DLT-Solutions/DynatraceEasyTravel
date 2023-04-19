package com.dynatrace.easytravel.business.webservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jws.WebMethod;

import org.springframework.transaction.annotation.Transactional;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.jpa.business.Tenant;
import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.spring.GenericPlugin;
import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;

import ch.qos.logback.classic.Logger;

public class AuthenticationService {
	private static final Logger log = LoggerFactory.make();

    private DataAccess databaseAccess;
    private Configuration configuration;

	private final GenericPluginList plugins = new GenericPluginList(PluginConstants.BACKEND_AUTHENTICATION_SERVICE);

    @WebMethod(exclude=true)
    public void setDatabaseAccess(DataAccess bookingService) {
		this.databaseAccess = bookingService;
	}
    
    @WebMethod(exclude=true)
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 *  Note: this is used in the system profile for BTs, do not change/move it!
	 */
	public boolean authenticate(String userName, String password){
		Iterator<GenericPlugin> pluginList = plugins.iterator();
		while(pluginList.hasNext()){
			GenericPlugin p = pluginList.next();
			if(p.getName().toLowerCase().contains("javascripttagging")) 
				return authenticateInternal2(userName, password);
		}
		return authenticateInternal(userName, password);
    }
	public boolean authenticateInternal(String userName, String password) {
		return authenticateInternal2(userName, password);
    }
	
	public boolean authenticateInternal2(String userName, String password) {
		User user = getUser(userName);
		
    	boolean ok = (user != null) && ((password == null && user.getPassword() == null) || (password != null && password.equals(user.getPassword())));
    	log.info("authenticate userName: " + userName + ", password: " + password + ", user: " + (user == null ? "<null>" : user.getName()) + ", ok: " + ok);

    	// let plugins deny login
    	for (Object obj : plugins.execute(PluginConstants.BACKEND_AUTHENTICATE, userName, password)) {
    		if(obj != null && obj instanceof Boolean) {
    			ok = ok & (Boolean)obj;
    		}
    	}

    	if (ok)
    	{
    		user.setLastLogin(new Date());
    		databaseAccess.updateUser(user);
    	}
    	
    	return ok;
    }
	
	/**
	 * The method return user object for given userName. If DBSpammingAuth plugin is enabled then some database spamming is started here.
	 * @param userName
	 * @param getUserDetails
	 * @return
	 */
	private User getUser(String userName) {
		//execute plugins before authenticate; for example dbSpamming
		AtomicBoolean dbSpammingEnabled = new AtomicBoolean(false);
		AtomicBoolean dbSlowdown = new AtomicBoolean(false);
		plugins.execute(PluginConstants.BACKEND_AUTHENTICATE_GETUSER, dbSpammingEnabled, dbSlowdown);
		boolean getUserDetails = dbSpammingEnabled.get() || configuration.isDBSpammingAuthEnabled();
		EasyTravelConfig config = EasyTravelConfig.read();
		int timeout = config.authServiceGetUserDelay;
		
		if(getUserDetails) {
			User user = null;
			User tmpUser = null;
			User[] allUsers = databaseAccess.allUsers().toArray(new User[0]);
			
			//preapare additional delay
			log.debug("Database spamming in authentication service enabled. verifyLocation timeout: " + timeout  + " spamSize: " + config.authServiceSpamSize);
			
			int cnt = 0;
			while (cnt < config.authServiceSpamSize) {
				verifyLocation(timeout);
				
				int idx = cnt % allUsers.length;
				cnt++;				
				
				tmpUser = databaseAccess.getUser(allUsers[idx].getName());
				if(tmpUser != null && tmpUser.getName() != null && tmpUser.getName().equals(userName)) {
					user = tmpUser;
				}
			}
			
			//this may happen when spam size is smaller than number of users
			if (user == null) {
				user = databaseAccess.getUser(userName);
			}
			return user;
		}
		else if(dbSlowdown.get()){
			timeout = config.databaseSlowdownDelay;
			log.debug("DB Slowdown in authentication service enabled. verifyLocation timeout: " + timeout);
			verifyLocation(timeout);
			return databaseAccess.getUser(userName);	
		}
		else{
			log.debug("verifyLocation timeout: " + timeout);
			verifyLocation(timeout);
			return databaseAccess.getUser(userName);			
		} 
		
	}
	
	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 *  Note: this is used in the system profile for BTs, do not change/move it!
	 */
	public boolean authenticateTenant(String tenantName, String password) {

    	Tenant tenant = databaseAccess.getTenant(tenantName);
        boolean ok = (tenant != null) && ((password == null && tenant.getPassword() == null) || (password != null && password.equals(tenant.getPassword())));
        log.info("authenticate tenantName: " + tenant + ", password: " + password + ", ok: " + ok);

    	// let plugins deny login
    	for (Object obj : plugins.execute(PluginConstants.BACKEND_TENANT_AUTHENTICATE, tenantName, password)) {
    		if(obj != null && obj instanceof Boolean) {
    			ok = ok & (Boolean)obj;
    		}
    	}

        if (ok) {
            tenant.setLastLogin(new Date());
            databaseAccess.updateTenant(tenant);
        }
        return ok;

    }

    public String[] getUserRoles(String userName) {
    	User user = (EasyTravelConfig.read().isFullAuthServiceSpammingEnabled ? getUser(userName) : databaseAccess.getUser(userName));
    	if (user != null) {
    		// user role from database is turned off but a plugin can provide
    		// user roles in the context
	    	for (Object object : plugins.execute(PluginConstants.BACKEND_USER_ROLES, userName)) {
		    	if (object instanceof String[]) {
		    		return (String[]) object;
		    	}
	    	}
    	}
		return null;
    }

    public String getFullName(String userName) {
    	User user = (EasyTravelConfig.read().isFullAuthServiceSpammingEnabled ? getUser(userName) : databaseAccess.getUser(userName));
    	return (user != null) ? user.getFullName() : null;
    }

    public String getLoyaltyStatus(String userName) {
    	User user = (EasyTravelConfig.read().isFullAuthServiceSpammingEnabled ? getUser(userName) : databaseAccess.getUser(userName));
    	return (user != null) ? user.getLoyaltyStatus() : null;
    }

    // Only for "forgotPwd" functionality in UI!
    public String getPassword(String userName) {
    	User user = (EasyTravelConfig.read().isFullAuthServiceSpammingEnabled ? getUser(userName) : databaseAccess.getUser(userName));
    	plugins.execute(PluginConstants.BACKEND_USER_PASSWORD, user.getPassword());
    	return user.getPassword();
    }
    
    /**
     * Provide all users defined in Users.txt file
     */

    public User[] getUsers(){
    	Collection<User> users = databaseAccess.allUsers();
    	plugins.execute(PluginConstants.BACKEND_USER_ALL, users);
        return users.toArray(new User[users.size()]);
    }
    
    /**
     * Provide top twenty users from Users.txt matching given prefix 
     * or random twenty users if given prefix is empty/don't contain letters
     * @param String from name field
     * @return User[] array
     * 
     * @author Michal.Bakula
     */

    public User[] getTwentyUsers(String pref) {
		Collection<User> allUsers = databaseAccess.allUsers();
		ArrayList<User> users = new ArrayList<>();
		
		// All logins in Users.txt file contains only lowercase letters, 
		// so we delete any non-letter character and change rest to lowercase from prefix string
		pref=pref.replaceAll("[^A-Za-z]+", "");
		pref=pref.toLowerCase();
		
		if(pref.length()==0){
			Set<Integer> set = getRandomUsersIndexes(allUsers, 20);
			
			int i=0;
			for(User u : allUsers){
				if(set.contains(i))
					users.add(u);
				i++;
			}
		} else {
			int i=0;
			for(User u : allUsers){
	    		if(u.getName().startsWith(pref)){
	    			users.add(u);
	    			i++;
	    			if(i==20)
	    				break;
	    		}
	    	}
		}
    	plugins.execute(PluginConstants.BACKEND_USER_ALL, users);
        return users.toArray(new User[users.size()]);
    }
    
    /**
     * 
     * @param Set of all users
     * @param Number of random user's indexes to return
     * @return Set of indexes of chosen random users
     * 
     * @author Michal.Bakula
     */
	private Set<Integer> getRandomUsersIndexes(Collection<User> users, int number) {		
		if(users.size() == 0 || number <= 0) {
			return Collections.emptySet();
		}
		
		Set<Integer> set = new HashSet<>();
		if (users.size() <= number) {
			for (int i = 0; i < users.size(); i++) {
				set.add(i);
			}
		} else {
			Random r = new Random();
			int i = 0;
			while (set.size() < number && i < 2 * number) {
				set.add(r.nextInt(users.size()));
				i++;
			}
		}

		return set;
	}

    public Tenant[] getTenants() {
		Collection<Tenant> tenants = databaseAccess.allTenants();
        plugins.execute(PluginConstants.BACKEND_TENANT_ALL, tenants);
        return tenants.toArray(new Tenant[tenants.size()]);
    }

	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 *  Note: this is used in the system profile for BTs, do not change/move it!
	 */
    public boolean addNewUser(String userName, String fullName, String email, String password) {
    	log.info("Adding new user with name '" + userName + "' and email '" + email + "'");
    	User user = databaseAccess.getUser(userName);
    	if (user != null) {
    		return false;
    	}

    	databaseAccess.addUser(new User(userName, fullName, email, password));
    	return true;
    }
    
	/**
	 * call verify location procedure to introduce additional delay; if timeout parameter is <0 procedure will not be called
	 */
	@Transactional
	private void verifyLocation(int timeout) {
		if (timeout > 0 && EasyTravelConfig.read().enableDBSlowdown) {
			try {
				databaseAccess.verifyLocation(timeout);
			} catch (Exception e){
				log.warn("Cannot execute verifylocation stored procedure", e);
			}
		}
	}
}
