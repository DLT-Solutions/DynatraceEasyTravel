package com.dynatrace.easytravel.frontend.beans;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;

/**
 * Provides login functionality for the login popup and the booking login page.
 */
@ManagedBean
@RequestScoped
public class LoginRequestBean implements Serializable {

	private static final long serialVersionUID = -6685841381588919764L;

	private static Logger log = LoggerFactory.make();

	@ManagedProperty("#{loginBean}")
	private LoginBean loginBean;

	private boolean popupVisible;
	private boolean userListVisible;
	private boolean userListBookVisible;
	private boolean loginFailed;
	private boolean loginSuccessful;

	public boolean isPopupVisible() {
		// when to show the Login-Dialog
		return popupVisible || userListVisible;
	}

	public boolean isUserListVisible() {
        return userListVisible;
    }

	public boolean isUserListBookVisible() {
        return userListBookVisible;
    }

	public boolean isLoginFailed() {
		return loginFailed;
	}

	public boolean isLoginSuccessful() {
		return loginSuccessful;
	}

	public void setLoginBean(LoginBean loginBean) {
		this.loginBean = loginBean;
	}

	/**
	 * Show the login popup.
	 *
	 * @author philipp.grasboeck
	 */
	public void showPopup() {
		popupVisible = true;
	}

	/**
	 * Hide the login popup.
	 *
	 * @author philipp.grasboeck
	 */
	public void hidePopup() {
		// nothing to do, login popup will disappear since this bean is request-scoped
	}

	private void doLogin() {
		loginBean.doLogin();
		loginFailed = !loginBean.getUserContext().isAuthenticated();
		loginSuccessful = !loginFailed;
	}

	/**
	 * Perform a login from the login popup window.
	 * Note that the form must be it's username and password to
	 * the loginBean.
	 *
	 * @author philipp.grasboeck
	 */
	public void popupLogin() {
		if(log.isDebugEnabled()) {
			log.debug("popup login " + loginBean.getUserName() + " / " + loginBean.getPassword() );
		}
		doLogin();
		if (loginFailed) {
			showPopup();
		}
	}


	/**
     * Perform a login from the login popup window.
     * Note that the form must be it's username and password to
     * the loginBean.
     *
     * @author philipp.grasboeck
     */
    public void loginFromUserList() {
        String userName = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("userName");
        String password = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("password");
        loginBean.setUserName(userName);
        loginBean.setPassword(password);
        //log.info("popup login " + loginBean.getUserName() + " / " + loginBean.getPassword() );
        doLogin();
        if (loginFailed) {
            showPopup();
        }
    }

	/**
	 * Perform a login from the booking login page.
	 * Note that the form must be it's username and password to
	 * the loginBean.
	 *
	 * @author philipp.grasboeck
	 */
	public void bookingLogin() {
		doLogin();
	}


	public void showUserList() {
        loginBean.retrieveUsers();
        userListVisible = true;
    }


	public void showUserListBook() {
        loginBean.retrieveUsers();
        userListBookVisible = true;
    }


	public void hideUserList() {
	    userListVisible = false;
	}


	public void hideUserListBook() {
	    userListBookVisible = false;
	}

}
