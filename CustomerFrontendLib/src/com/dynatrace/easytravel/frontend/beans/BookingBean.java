package com.dynatrace.easytravel.frontend.beans;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.axis2.AxisFault;
import org.apache.commons.lang3.StringUtils;

import com.codahale.metrics.Timer.Context;
import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.metrics.Metrics;
import com.dynatrace.easytravel.misc.UserType;
import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;

import ch.qos.logback.classic.Logger;

/**
 * BookingBean stores stateful booking information.
 *
 * @author philipp.grasboeck
 */
@ManagedBean
@SessionScoped
public class BookingBean implements Serializable {

    private static final GenericPluginList plugins = new GenericPluginList(PluginConstants.FRONTEND_BOOKING);

    /**
     * The booking state on the booking finish page.
     *
     * @author philipp.grasboeck
     */
    public static enum BookingState {
        /** Payment is validated so far, actual booking was not yet performed. */
        initial,
        /** Booking performed successfully. */
        success,
        /** An error occured while booking, typically a remote exception. */
        error
    }

    private static final long serialVersionUID = 6745449095283826761L;

    private static Logger log = LoggerFactory.make();

    @ManagedProperty("#{loginBean}")
    private LoginBean loginBean;

    @ManagedProperty("#{dataBean}")
    private DataBean dataBean;

    private BookingState bookingState;
    private int selectedJourneyId;
    private JourneyDO selectedJourney;
    private String creditCardNumber; // this holds the already-validated credit card number
    private String bookingId;

    /* --- a few JSF map tricks --- */

    // keeps track of booked journeys, for access in markup to disable [Book Now] button
    private final Map<JourneyDO, Boolean> bookingMap = new HashMap<JourneyDO, Boolean>() {
        private static final long serialVersionUID = -4140587570184445796L;

        @Override
        public Boolean get(Object key) {
            return containsKey(key);
        }
    };

    // keeps a JourneyAsset for every JourneyDO that is displayed in the view
    private final Map<JourneyDO, JourneyAccount> accountMap = new HashMap<JourneyDO, JourneyAccount>() {
        private static final long serialVersionUID = 400102149986532595L;

        @Override
        public JourneyAccount get(Object key) {
            JourneyDO journey = (JourneyDO) key;
            JourneyAccount account = super.get(journey);
            if (account == null) {
            	account = new JourneyAccount();
                super.put(journey, account);
                account.setJourney(journey);
            }
            return account;
        }
    };

    // used to condionally disable the [Book Now] button
    public Map<JourneyDO, Boolean> getBookingMap() {
        return bookingMap;
    }

    // used to get journey assets for journeys
    public Map<JourneyDO, JourneyAccount> getAccountMap() {
        return accountMap;
    }

    public BookingState getBookingState() {
        return bookingState;
    }

    public void setBookingState(BookingState bookingState) {
        this.bookingState = bookingState;
    }

    // used to condionally render the [Next] buttons
    public boolean isStateValid() {
        return selectedJourney != null && loginBean.getUserContext().isAuthenticated();
    }

    public int getSelectedJourneyId() {
        return selectedJourneyId;
    }

    public void setSelectedJourneyId(int selectedJourneyId) {
        this.selectedJourneyId = selectedJourneyId;
    }

    /**
     * Load the journey from the selectedJourneyId view parameter.
     *
     * @author philipp.grasboeck
     */
    public void loadJourney() {
        if (selectedJourney == null || selectedJourney.getId() != selectedJourneyId) {
            final Context context = Metrics.getTimerContext(this, "loadJourney");
            try {
                selectedJourney = dataBean.getDataProvider().getJourneyById(selectedJourneyId);
                if (selectedJourney == null) {
                    FacesUtils.sendError(FacesUtils.ERROR_404, "Journey " + selectedJourneyId);
                }
            } catch (RemoteException ex) {
                throw new IllegalStateException("Cannot load journey: " + ex.getMessage());
            } finally {
                context.stop();
                context.close();
            }
        }
    }

    public String getLoginInclude() {
        return Pages.INCLUDE_BOOKING_LOGIN;
    }

    public String getReviewInclude() {
        return loginBean.getUserContext().isAuthenticated() ? Pages.INCLUDE_BOOKING_REVIEW : Pages.INCLUDE_BOOKING_LOGIN;
    }

    public String getPaymentInclude() {
        return loginBean.getUserContext().isAuthenticated() ? Pages.INCLUDE_BOOKING_PAYMENT : Pages.INCLUDE_BOOKING_LOGIN;
    }

    public String getFinishInclude() {
        return loginBean.getUserContext().isAuthenticated() ? Pages.INCLUDE_BOOKING_FINISH : Pages.INCLUDE_BOOKING_LOGIN;
    }

    /**
     * Actually perform the booking that has been validated before using
     * the bookingRequestBean.
     * This sets the bookingState accordingly.
     *
     * @return
     * @author philipp.grasboeck
     *
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     *  Note: this is used in the system profile for BTs, do not change/move it!
     */
    public String performBooking() {
        if (!bookingState.equals(BookingState.initial)) {
            return null;
        }

        String userName = loginBean.getUserContext().getUserName();
        log.info("STORE booking, userName=" + userName + " creditCardNumber=" + creditCardNumber);

        final Context context = Metrics.getTimerContext(this, "performBooking");
        try {
            JourneyAccount account = accountMap.get(selectedJourney);
            bookingId = dataBean.getDataProvider().storeBooking(selectedJourney.getId(), userName, UserType.WEB, creditCardNumber.replace(" ", ""), account.getTotalCosts());
            log.debug("STORED booking, id=" + bookingId);
        } catch (AxisFault e) {
            setBookingState(BookingState.error);
            ValidationUtils.addError("creditCardNumber", "Error while booking: " + e.getMessage());
            AtomicBoolean showAsHttp500 = new AtomicBoolean(false);
            plugins.execute(PluginConstants.FRONTEND_BOOKING_PERFORM_BOOKING, showAsHttp500);
            if (showAsHttp500.get()) {
                FacesUtils.setStatusCode(FacesUtils.ERROR_500);
            }
            log.info("AxisFault while booking: " + e.getMessage());
            return null;
        } catch (RemoteException e) {
            log.error(e.getMessage());
            setBookingState(BookingState.error);
            ValidationUtils.addError("creditCardNumber", "Error while booking: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            context.stop();
            context.close();
        }

        setBookingState(BookingState.success);
        bookingMap.put(selectedJourney, Boolean.TRUE);
        // this is a special hack for the JUnitHTML cause a redirect
        // on this page seems to cause a
        if (isHTMLUnitTest()) {
        	return null;
        }
        return Pages.NAVIGATION_CASE_BOOKING_FINISHED;
    }

    public void setLoginBean(LoginBean loginBean) {
        this.loginBean = loginBean;
    }

    public void setDataBean(DataBean dataBean) {
        this.dataBean = dataBean;
    }

    public JourneyDO getSelectedJourney() {
        return selectedJourney;
    }

    /**
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     *  Note: this is used in the system profile for BTs, do not change/move it!
     */
    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public double getBookingTotal() {
        JourneyAccount account = accountMap.get(selectedJourney);
        return account.getTotalCosts();
    }

    public double getBookingPricePerPerson() {
        JourneyAccount account = accountMap.get(selectedJourney);
        return account.getAvgPerPerson();
    }

    public double getAveragePricePerPerson() {
    	JourneyAccount account = accountMap.get(selectedJourney);
    	return account.getTotalCosts() / getNumberOfPerson();
    }

    public int getNumberOfPerson() {
       return getNumberAdults() + getNumberChildren();
    }

    public int getNumberAdults() {
        JourneyAccount account = accountMap.get(selectedJourney);
        int travellers = account.getTravellers();
        if (travellers == 1 || travellers == 2 || travellers == 3) {
            return 1;
        }
        return 2;
    }

    public int getNumberChildren() {
    	JourneyAccount account = accountMap.get(selectedJourney);
        int travellers = account.getTravellers();
        if (travellers == 1 || travellers == 4) {
            return 0;
        } else if (travellers == 2 || travellers == 5) {
        	return 1;
        }
        return 2;
    }

    public String getTravellersLabel() {
    	JourneyAccount account = accountMap.get(selectedJourney);
        int travellers = account.getTravellers();
        List<SelectItem> travellersOptions = account.getTravellersSelectList();

        if(travellers > 0){
        	return travellersOptions.get(travellers - 1).getLabel();
        } 
        
        return StringUtils.EMPTY;
        
        
    }

    public String getBookingId() {
        return bookingId;
    }

    public boolean isHTMLUnitTest() {
    	if (FacesContext.getCurrentInstance() != null
    			&& FacesContext.getCurrentInstance().getExternalContext() != null
    			&& FacesContext.getCurrentInstance().getExternalContext().getRequest() != null)
    	{
	    	final HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
	    	try {
	    		String userAgent = request.getHeader("User-Agent");
	    		return "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.3) Gecko/20100401 Firefox/3.6.3".equals(userAgent);
	    	} catch (Exception e) {
	    		// shouldn't happen but test and then remove if absolute certain
	    	}
    	}
		return false;
    }
}
