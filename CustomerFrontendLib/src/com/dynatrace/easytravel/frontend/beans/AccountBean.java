package com.dynatrace.easytravel.frontend.beans;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;

import com.dynatrace.easytravel.frontend.lib.CustomerFrontendUtil;
import com.dynatrace.easytravel.frontend.lib.User;
import com.dynatrace.easytravel.frontend.login.LoginLogic;

@ManagedBean
@RequestScoped
public class AccountBean implements Serializable {

	private static final long serialVersionUID = 1153473351876932214L;

	@ManagedProperty("#{loginBean}")
	private LoginBean loginBean;

	private String firstName = "";
	private String lastName = "";
	private String email = "";
	private String emailConfirm = "";
	private String password = "";
	private String passwordConfirm = "";
	private String state = "";
	private String city = "";
	private String street = "";
	private String door = "";
	private String phone = "";

	public void fillMock() {
		User user = CustomerFrontendUtil.getRandomUser();
		firstName = user.getFirstName();
		lastName = user.getLastName();
		email = emailConfirm = user.getEmail();
		password = passwordConfirm = user.getPassword();
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	private static String checkPassword(String password) {
		if (password.length() < 6) {
			return "The provided password is too short. Minimum length is 6 characters.";
		} else if (password.length() > 12) {
			return "The provided password is too long. Maxiumum length is 12 characters.";
		} else {
			return null;
		}
	}

	public String createAccount() {
		String userName = email;
		String fullName = firstName + " " + lastName;
		String passwordError;

		if (firstName.trim().isEmpty()) {
			ValidationUtils.addError("firstName", "Please enter a first name.");
		}
		if (lastName.trim().isEmpty()) {
			ValidationUtils.addError("lastName", "Please enter a last name.");
		}
		if (email.trim().isEmpty()) {
			ValidationUtils.addError("email", "Please enter an email address.");
		} else if (!EmailActionHelper.isValidEmailAddress(email)) {
			ValidationUtils.addError("email", "'" + email + "' is not a valid email address.");
		}
		if (password.trim().isEmpty()) {
			ValidationUtils.addError("password", "Please provide a password.");
		} else if ((passwordError = checkPassword(password)) != null) {
			ValidationUtils.addError("password", passwordError);
		}
		if (emailConfirm.trim().isEmpty()) {
			ValidationUtils.addError("emailConfirm", "Please confirm your email address.");
		} else if (!emailConfirm.equals(email)) {
			ValidationUtils.addError("emailConfirm", "The confirmed email address doesn't match.");
		}
		if (passwordConfirm.trim().isEmpty()) {
			ValidationUtils.addError("passwordConfirm", "Please confirm your password.");
		} else if (!passwordConfirm.equals(password)) {
			ValidationUtils.addError("passwordConfirm", "The confirmed password doesn't match.");
		}

		if (ValidationUtils.hasErrors()) { // a validation error
			return null;
		}

		boolean success = LoginLogic.addNewUser(userName, fullName, email, password);
		if (!success) {
			ValidationUtils.addError("email", "The email address you entered is already registered with another account.");
			return null;
		}

		loginBean.setUserName(userName);
		loginBean.setPassword(password);
		loginBean.doLogin();
		if (!loginBean.getUserContext().isAuthenticated()) {
			ValidationUtils.addError("email", "An unknown error occured while trying to login with your email address.");
			return null;
		}

		return Pages.NAVIGATION_CASE_ACCOUNT_CREATED;
	}

	public LoginBean getLoginBean() {
		return loginBean;
	}

	public void setLoginBean(LoginBean loginBean) {
		this.loginBean = loginBean;
	}

	public String getEmailConfirm() {
		return emailConfirm;
	}

	public void setEmailConfirm(String emailConfirm) {
		this.emailConfirm = emailConfirm;
	}

	public String getPasswordConfirm() {
		return passwordConfirm;
	}

	public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
	}

	public boolean isInputError() {
		return ValidationUtils.hasErrors();
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getDoor() {
		return door;
	}

	public void setDoor(String door) {
		this.door = door;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
}
