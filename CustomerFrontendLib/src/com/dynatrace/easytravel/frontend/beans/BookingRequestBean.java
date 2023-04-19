package com.dynatrace.easytravel.frontend.beans;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.model.SelectItem;

import com.dynatrace.easytravel.frontend.beans.BookingBean.BookingState;
import com.dynatrace.easytravel.frontend.lib.CustomerFrontendUtil;
import com.dynatrace.easytravel.frontend.lib.User;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.metrics.Metrics;
import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;

import ch.qos.logback.classic.Logger;

import static com.codahale.metrics.Timer.Context;

/**
 * BookingRequestBean holds a single booking check request
 * (transition from booking-payment to booking-finish).
 *
 * @author philipp.grasboeck
 */
@ManagedBean
@RequestScoped
public class BookingRequestBean implements Serializable {

	private static final long serialVersionUID = 6745449095283826761L;

    private static Logger log = LoggerFactory.make();

	private static final GenericPluginList plugins = new GenericPluginList(PluginConstants.FRONTEND_BOOKING);

	@ManagedProperty("#{loginBean}")
	private LoginBean loginBean;

	@ManagedProperty("#{dataBean}")
	private DataBean dataBean;

	@ManagedProperty("#{bookingBean}")
	private BookingBean bookingBean;

	private String creditCardType = "";
	private String creditCardNumber = "";
	private String creditCardOwner = "";
	private String verificationNumber = "";
	private String expirationMonth = "";
	private String expirationYear = "";

	public void fillMock() {
		User user = CustomerFrontendUtil.getRandomUser();
		creditCardNumber = user.getCreditCardNumber();
		verificationNumber = user.getVerificationNumber();
		expirationMonth = user.getExpirationMonth();
		expirationYear = user.getExpirationYear();
		creditCardOwner = loginBean.getUserContext().getFullName();
	}

	/**
	 * Validate the payment information, i.e. credit card number, and
	 * return the next page to view, i.e. the payment page again,
	 * if the validation failed (returning null),
	 * or the finish page on success (returning the 'paymentValidated'
	 * navigation case).
	 *
	 * @return
	 * @author philipp.grasboeck
	 *
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 *  Note: this is used in the system profile for BTs, do not change/move it!
	 */
	public String validatePayment() {
		if (creditCardNumber.trim().isEmpty()) {
			ValidationUtils.addError("creditCardNumber", "Please enter your credit card number.");
		}
		if (creditCardOwner.trim().isEmpty()) {
			ValidationUtils.addError("creditCardNumber", "Please enter the name of the credit card owner.");
		}
		if (expirationMonth.trim().isEmpty() || expirationYear.trim().isEmpty()) {
			ValidationUtils.addError("expirationMonth", "Please enter your credit card's expiration date.");
		}
		if (verificationNumber.trim().isEmpty()) {
			ValidationUtils.addError("verificationNumber", "Please enter the credit card's verification number.");
		}

		if (ValidationUtils.hasErrors()) {
			return null;
		}

        final Context context = Metrics.getTimerContext(this, "validatePayment");
		try {
			boolean valid = dataBean.getDataProvider().checkCreditCard(creditCardNumber.replace(" ", ""));
			if (!valid) {
				ValidationUtils.addError("creditCardNumber", "'" + creditCardNumber + "' is not a valid credit card number.");
				return null;
			}
		} catch (RemoteException e) {
			log.error(e.getMessage());
			AtomicBoolean showAsHttp500 = new AtomicBoolean(false);
			plugins.execute(PluginConstants.FRONTEND_BOOKING_VALIDATE_PAYMENT, showAsHttp500);
			if (showAsHttp500.get()) {
				FacesUtils.setStatusCode(FacesUtils.ERROR_500);
			}
			ValidationUtils.addError("creditCardNumber", "Error while checking credit card: " + e.getMessage());
			return null;
		} finally {
            context.stop();
            context.close();
        }

		bookingBean.setBookingState(BookingState.initial);
		bookingBean.setCreditCardNumber(creditCardNumber);
		return Pages.NAVIGATION_CASE_PAYMENT_VALIDATED;
	}

	public void setDataBean(DataBean dataBean) {
		this.dataBean = dataBean;
	}

	public void setLoginBean(LoginBean loginBean) {
		this.loginBean = loginBean;
	}

	public String getCreditCardNumber() {
		return creditCardNumber;
	}

	public void setCreditCardNumber(String creditCardNumber) {
		this.creditCardNumber = creditCardNumber;
	}

	public String getCreditCardType() {
		return creditCardType;
	}

	public void setCreditCardType(String creditCardType) {
		this.creditCardType = creditCardType;
	}

	public String getCreditCardOwner() {
		return creditCardOwner;
	}

	public void setCreditCardOwner(String creditCardOwner) {
		this.creditCardOwner = creditCardOwner;
	}

	public String getVerificationNumber() {
		return verificationNumber;
	}

	public void setVerificationNumber(String verificationNumber) {
		this.verificationNumber = verificationNumber;
	}

	public boolean isInputError() {
		return ValidationUtils.hasErrors();
	}

	public String getExpirationMonth() {
		return expirationMonth;
	}

	public void setExpirationMonth(String expirationMonth) {
		this.expirationMonth = expirationMonth;
	}

	public String getExpirationYear() {
		return expirationYear;
	}

	public void setExpirationYear(String expirationYear) {
		this.expirationYear = expirationYear;
	}

	public void setBookingBean(BookingBean bookingBean) {
		this.bookingBean = bookingBean;
	}
	
	/**
	 * @author Michal.Bakula
	 */
	public List<SelectItem> getValidYears(){
		int year=Calendar.getInstance().get(Calendar.YEAR) - 1;
		List<SelectItem> validYears = new ArrayList<>();
		validYears.add(new SelectItem("", "Year"));
		for(int i = year; i<year + 10;i++){
			validYears.add(new SelectItem(i));
		}
		return validYears;
	}
}
