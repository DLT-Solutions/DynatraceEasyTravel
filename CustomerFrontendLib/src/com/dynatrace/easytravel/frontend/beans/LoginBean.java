package com.dynatrace.easytravel.frontend.beans;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import com.dynatrace.easytravel.frontend.data.UserDO;
import com.dynatrace.easytravel.frontend.login.LoginLogic;
import com.dynatrace.easytravel.frontend.login.UserContext;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.metrics.Metrics;

import ch.qos.logback.classic.Logger;

import static com.codahale.metrics.Timer.Context;

@ManagedBean
@SessionScoped
public class LoginBean implements Serializable
{
	private static final long serialVersionUID = 5667372063395122519L;

    private static Logger log = LoggerFactory.make();

	private final UserContext userContext = new UserContext();
	
	@ManagedProperty("#{dataBean}")
    private DataBean dataBean;
	
	private String password;
	private String userName;
	private UserDO[] users;
	
		
	
	public void setDataBean(DataBean dataBean) {
        this.dataBean = dataBean;
    }
	

	// use the JSF map trick to easily invoke isUserInRole in JSF markup
	// #{loginBean.roleMap.admin}
	private final Map<String, Boolean> roleMap = new HashMap<String, Boolean>() {
		private static final long serialVersionUID = -8488829845667168073L;

		@Override
		public Boolean get(Object key) {
			return userContext.isUserInRole(key.toString());
		}
	};

	public Map<String, Boolean> getRoleMap() {
		return roleMap;
	}

	/**
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 *  Note: this is used in the system profile for BTs, do not change/move it!
	 */
	public UserContext getUserContext() {
		return userContext;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	// package-private for use in other managed beans
	void doLogin() {
		log.debug("try to login: " + userName + "/" + password);
		LoginLogic.authenticate(userName, password, userContext);
        /* set X-Username HTTP header on login responses; purpose: demonstrate story JLT-33757 */
        if (userContext.isAuthenticated()) {
            FacesUtils.setHeader("X-Username", userName);
        }
	}
	
	
	public void retrieveUsers() {
        final Context context = Metrics.getTimerContext(this, "retrieveUsers");
        try {
        	//users = dataBean.getDataProvider().getUsers();
            users = dataBean.getDataProvider().getUsersWithPrefix(userName);
            Arrays.sort(users, new Comparator<UserDO>() {
                @Override
				public int compare(UserDO o1, UserDO o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
        } catch (RemoteException re) {
            log.error("LoginBean.getUsers", re);
            users = new UserDO[0];
        } finally {
            context.stop();
            context.close();
        }
    }
	
	
	public UserDO[] getUsers() {
        return users;
    }

}
