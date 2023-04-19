package com.dynatrace.easytravel;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.jpa.business.Booking;
import com.dynatrace.easytravel.jpa.business.LoginHistory;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.SpringUtils;

public class CalculateUserStats extends AbstractGenericPlugin {

    static final int CHECK_TIME = 15;

    private final Logger log = LoggerFactory.make();
    private EntityManager entityManager;


    public CalculateUserStats() {
    }


	public CalculateUserStats(EntityManager entityManager) {
	    this.entityManager = entityManager;
    }


    @Override
	public Object doExecute(String location, Object... context) {

	    EntityManager em = entityManager;
	    try {
	        if (em == null) {
                em = SpringUtils.getBean("entityManagerFactory", EntityManagerFactory.class).createEntityManager();
            }
	        String userName = (String) context[0];
		    TypedQuery<Booking> qBooking = em.createQuery("select b from Booking b where b.user.name = :userName", Booking.class);
			qBooking.setParameter("userName", userName);
			List<Booking> bookings = qBooking.getResultList();
			double totalAmount = 0;
			EntityTransaction transaction = em.getTransaction();
			transaction.begin();
			for (Booking booking : bookings) {
			    verifyBooking(em);
			    totalAmount += booking.getJourney().getAmount();
			}
	        transaction.commit();
			TypedQuery<LoginHistory> qLoginHistory = em.createQuery("select lh from LoginHistory lh where lh.user.name = :userName", LoginHistory.class);
            qLoginHistory.setParameter("userName", userName);
			log.info(userName + " has been logged in " + qLoginHistory.getResultList().size() + " times");
			log.info(userName + "'s  total revenue so far: " + totalAmount + " � (" + bookings.size() + " bookings)");
	    } finally {
	        if (entityManager == null) {
	            em.close();
	        }
	    }
		return null;
	}


    public void verifyBooking(EntityManager em) {
        Query q;
        if (EasyTravelConfig.isDerbyDatabase() || EasyTravelConfig.isMySqlDatabase()) {
            q = em.createNativeQuery("call verify_location(?)");
        } else if (EasyTravelConfig.isOracleDatabase()) {
        	q = em.createNativeQuery("{call verify_location(?)}");
        } else {
            q = em.createNativeQuery("exec sp_verifyLocation @location=?");
        }
        q.setParameter(1, CHECK_TIME);
        q.executeUpdate();
    }
}
