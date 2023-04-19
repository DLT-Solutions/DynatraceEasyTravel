/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: JourneyJpaProvider.java
 * @date: 18.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.provider;

import static com.dynatrace.easytravel.jpa.business.Journey.JOURNEY_DESTINATION;
import static com.dynatrace.easytravel.jpa.business.Journey.JOURNEY_START;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.time.DateUtils;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.jpa.QueryNames;
import com.dynatrace.easytravel.jpa.business.Booking;
import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.jpa.business.Tenant;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.persistence.JpaDatabaseController;

/**
 *
 * @author stefan.moschinski
 */
public class JourneyJpaProvider extends JpaProvider<Journey> implements JourneyProvider {
	
	private static Logger log = LoggerFactory.make();

	private TenantProvider tenantProvider;

	public JourneyJpaProvider(JpaDatabaseController controller, TenantProvider tenantProvider) {
		super(controller, Journey.class);
		this.tenantProvider = tenantProvider;
	}
	
	@Override
	public List<Journey> findJourneys(String destination, Date fromDate, Date toDate, boolean normalize) {
		
		List<Journey> myJourneyList = null;
		EasyTravelConfig config = EasyTravelConfig.read();
		// Which query we execute is dictated only by which database we are using:
		// if the database supports the delay function, then we use the query with the delay function.
		// The plugin (in the form of the normalize parameter) determines only the amount
		// of the delay to use: from configuration or 0 (if off).
		// query with normalize location is executed only when enableDBSlowdown property is set to true
		if (config.enableDBSlowdown
				&& (EasyTravelConfig.isDerbyDatabase()
					|| EasyTravelConfig.isOracleDatabase()
					|| EasyTravelConfig.isMySqlDatabase())) {
			
			int queryDelay = 0;
			if (normalize) {
				// Get the actual delay value and then execute the query with the delay factor.
				
				queryDelay = config.databaseSlowdownDelay;
				
				// no need to reduce delay for Derby, as the query should only return one row.
			} // else queryDelay remains 0

			long myTime1 = 0L;
			long myTime2 = 0L;
			if (log.isDebugEnabled()) {
				myTime1 = System.currentTimeMillis();
				log.debug("findJourneys: creating query for JOURNEY_FIND_NORMALIZED, location <" + destination + "> and delay <" + queryDelay + ">");
			}

			myJourneyList = createNamedQuery(QueryNames.JOURNEY_FIND_NORMALIZED, Journey.class)
					.setParameter("destination", destination)
					.setParameter("fromDate", fromDate)
					.setParameter("factor", Integer.toString(queryDelay))
					.setParameter("toDate", toDate)
					.getResultList();
			if (log.isDebugEnabled()) {
				myTime2 = System.currentTimeMillis();
				log.debug("findJourneys: query creation end, DURATION: <" + (myTime2 - myTime1) + ">");
			}
			return myJourneyList;
			
		} else {
			// No delay function implemented for the current database -
			// execute the query with no added delay.
			return createNamedQuery(QueryNames.JOURNEY_FIND, Journey.class).setParameter("destination", destination).setParameter(
				"fromDate", fromDate).setParameter("toDate", toDate).getResultList();
		}
	}

	@Override
	public Journey getJourneyById(Integer id) {
		return find(id);
	}
	
	@Override
	public Journey getJourneyByIdNormalize(Integer id, boolean normalize) {
		Journey myJourney = null;
		EasyTravelConfig config = EasyTravelConfig.read();
		// Which query we execute is dictated only by which database we are using:
		// if the database supports the delay function, then we use the query with the delay function.
		// The plugin (in the form of the normalize parameter) determines only the amount
		// of the delay to use: from configuration or 0 (if off).
		if (config.enableDBSlowdown
				&& (EasyTravelConfig.isDerbyDatabase()
					|| EasyTravelConfig.isOracleDatabase()
					|| EasyTravelConfig.isMySqlDatabase())) {
			int queryDelay = 0;
			if (normalize) {
				// Get the actual delay value and then execute the query with the delay factor.
				queryDelay = 20*config.databaseSlowdownDelay;
				
				// no need to reduce delay for Derby, as the query should only return one row.
			} // else queryDelay remains 0

			long myTime1 = 0L;
			long myTime2 = 0L;
			log.info("findJourneys: creating query for JOURNEY_FIND_BY_ID_NORMALIZED, id <" + id + "> and delay <" + queryDelay + ">");
			if (log.isDebugEnabled()) {
				myTime1 = System.currentTimeMillis();
				log.debug("findJourneys: creating query for JOURNEY_FIND_BY_ID_NORMALIZED, id <" + id + "> and delay <" + queryDelay + ">");
			}
			myJourney = createNamedQuery(QueryNames.JOURNEY_FIND_BY_ID_NORMALIZED, Journey.class)
					.setParameter("id", id)
					.setParameter("factor", Integer.toString(queryDelay))
					.getSingleResult();
			if (log.isDebugEnabled()) {
				myTime2 = System.currentTimeMillis();
				log.debug("findJourneys: query creation end, DURATION: <" + (myTime2 - myTime1) + ">");
			}
			return myJourney;
			
		} else {
			return find(id);
		}
		
	}
	@Override
	public Collection<Journey> getJourneysByTenant(String tenantName) {
		Tenant t = tenantProvider.getTenantByName(tenantName);
		return createNamedQuery(QueryNames.JOURNEY_FIND_BY_TENANT).setParameter("tenant", t).getResultList();
	}

	@Override
	public Collection<Journey> getJourneysByTenant(String tenantName, int fromIdx, int count) {
		Tenant t = tenantProvider.getTenantByName(tenantName);
		return createQuery("select j from Journey j where j.tenant = :tenant order by lower(j.name)", Journey.class)
				.setParameter("tenant", t)
				.setFirstResult(fromIdx)
				.setMaxResults(count)
				.getResultList();
	}

	@Override
	public int getJourneyCountByTenant(String tenantName) {
		Tenant t = tenantProvider.getTenantByName(tenantName);
		return createQuery("select count(j) from Journey j where j.tenant = :tenant", Long.class)
				.setParameter("tenant", t)
				.getSingleResult().intValue();
	}

	@Override
	public int getJourneyIndexByName(String tenantName, String journeyName) {
		Tenant t = tenantProvider.getTenantByName(tenantName);

		List<String> list = createNamedQuery(QueryNames.JOURNEY_ALL_ALPHABETICALLY, String.class).setParameter("tenant", t).getResultList();
		int index = -1;
		for (String s : list) {
			index++;
			if (s.equalsIgnoreCase(journeyName)) {
				return index;
			}
		}

		return -1;
	}


	@Override
	public Journey getJourneyByName(String journeyName) {
		List<Journey> list = createNamedQuery(QueryNames.JOURNEY_GET, Journey.class).setParameter("name", journeyName).getResultList();
		if (list != null && list.size() > 0) {
			if (list.size() != 1) {
				throw new IllegalStateException("Had multiple Journeys with name: " + journeyName);
			}
			return list.get(0);
		}

		return null;
	}

	@Override
	public Collection<Integer> getAllJourneyIds() {
		return createNamedQuery(QueryNames.JOURNEY_ALL_IDS, Integer.class).getResultList();
	}

	@Override
	public boolean isJourneyDestination(String locationName) {
		return !createNamedQuery(QueryNames.JOURNEY_FIND_BY_LOCATION_DEST, Journey.class).setParameter(JOURNEY_DESTINATION,
				locationName).getResultList().isEmpty();
	}

	@Override
	public boolean isJourneyStart(String locationName) {
		return !createNamedQuery(QueryNames.JOURNEY_FIND_BY_LOCATION_START, Journey.class).setParameter(JOURNEY_START, locationName).getResultList().isEmpty();
	}

	@Override
	public Collection<Location> getMatchingJourneyDestinations(String name, boolean normalize) {

		Collection<Location> myLocations = null;

		// Which query we execute is dictated only by which database we are using:
		// if the database supports the delay function, then we use the query with the delay function.
		// The plugin (in the form of the normalize parameter) determines only the amount
		// of the delay to use: from configuration or 0 (if off).
		if (EasyTravelConfig.isDerbyDatabase()
			|| EasyTravelConfig.isOracleDatabase()
			|| EasyTravelConfig.isMySqlDatabase()) {
			
			int queryDelay = 0;
			if (normalize) {
				// Get the actual delay value and then execute the query with the delay factor.
				EasyTravelConfig config = EasyTravelConfig.read();
				
				queryDelay = config.databaseSlowdownDelay;
			
				// For some databases the delay function will be executed for every row obtained from
				// the first two components of the query. This is usually less than 20 and on average perhaps around 10
				// hence divide it here by that number.
				//
				// Databases verified to-date:
				// 		Derby - does multiple executions
				// 		Oracle - executes the function always only once per query.
				// 		mySQL - executes the function always only once per query.
				
				if (EasyTravelConfig.isDerbyDatabase()) {
					queryDelay /= 10;
				}
			} // else queryDelay remains 0

			if (log.isDebugEnabled()) {
				log.debug("getMatchingJoruneyDestinations: creating query for LOCATION_FIND_WITH_JOURNEYS_AND_NORMALIZE, location <" + name + "> at time <" + System.currentTimeMillis() + ">");
			}

			myLocations = createNamedQuery(QueryNames.LOCATION_FIND_WITH_JOURNEYS_AND_NORMALIZE, Location.class)
				.setParameter("name", name.toLowerCase())
				.setParameter("factor", Integer.toString(queryDelay)).getResultList();
				
			if (log.isDebugEnabled()) {
				log.debug("getMatchingJoruneyDestinations: query creation end, time at time <" + System.currentTimeMillis() + ">");
			}
			return myLocations;
			
		} 
		else {
			// No delay function implemented for the current database -
			// execute the query with no added delay.
			return createNamedQuery(QueryNames.LOCATION_FIND_WITH_JOURNEYS, Location.class)
					.setParameter("name", name.toLowerCase()).getResultList();
		}
	}

	@Override
	public void removeJourneyById(int id) {
		Journey j = find(id);

		// delete Bookings first
		List<Booking> bookings = createNamedQuery(QueryNames.BOOKING_BY_JOURNEY, Booking.class).setParameter("journey", j).getResultList();
		for (Booking b : bookings) {
			remove(b);
		}

		remove(j);
	}

	@Override
	public int refreshJourneys() {
		Date d = new Date(System.currentTimeMillis() + DateUtils.MILLIS_PER_DAY);
		List<Journey> journeys = createQuery("select j from Journey j where j.fromDate < :date", Journey.class).setParameter(
				"date", d).getResultList();
		if (journeys.size() == 0) {
			return 0;
		}
		Random rand = new Random();
		int cnt = 0;
		for (Journey j : journeys) {
			int add = rand.nextInt(180 * 24) + (int) ((d.getTime() - j.getFromDate().getTime()) / DateUtils.MILLIS_PER_HOUR) + 1;
			j.setFromDate(DateUtils.addHours(j.getFromDate(), add));
			j.setToDate(DateUtils.addHours(j.getToDate(), add));
			cnt++;
		}
		flush();
		return cnt;
	}


	
}
