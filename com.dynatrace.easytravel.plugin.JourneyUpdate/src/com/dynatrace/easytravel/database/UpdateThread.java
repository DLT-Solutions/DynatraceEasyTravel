package com.dynatrace.easytravel.database;

import java.util.Random;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.model.DataAccess;

// UpdateThread which updates passed journeys
public class UpdateThread implements Runnable {

	private static Logger log = LoggerFactory.make();
	private Random random = new Random();

	private JourneyUpdate.Mode mode;
	private Iterable<Journey> journeys;
	private final DataAccess dataAccess;

	UpdateThread(DataAccess dataAccess, JourneyUpdate.Mode mode,
			Iterable<Journey> journeys) {
		this.dataAccess = dataAccess;
		this.mode = mode;
		this.journeys = journeys;
	}

	@Override
	public void run() {
		try {
			// difference in price of journey
			double diff = random.nextInt(100);

			dataAccess.startTransaction();

			// slow update - one journey updated after another
			if (mode == JourneyUpdate.Mode.slow) {
				log.info("Updating Journeys - slow");
//				Query q = em
//						.createQuery("update Journey set amount = :newAmount where id = :jId");

				for (Journey journey : journeys) {
					journey.setAmount(journey.getAmount() + diff);
					dataAccess.updateJourney(journey);
				}
			}

			// fast update - batch updating
			else {
				// TODO@(stefan.moschinski): Does not work for persistence stores that do not use JPA
				log.info("Updating Journeys - fast");
				for (Journey journey : journeys) {
					journey.setAmount(journey.getAmount() + diff);
				}
			}
			dataAccess.commitTransaction();
		} finally {
			dataAccess.close();
		}
	}
}
