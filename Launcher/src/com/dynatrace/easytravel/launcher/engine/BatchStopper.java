package com.dynatrace.easytravel.launcher.engine;

import java.util.Collection;
import java.util.logging.Logger;



class BatchStopper extends AbstractStopper {

	private static final Logger log = Logger.getLogger(BatchStopper.class.getName());

	protected BatchStopper(Collection<? extends Procedure> procedures) {
		// -------------------------

		// stop UEM load firstly
//		boolean uemStopped = BaseLoadManager.stopAll();
//		if (!uemStopped) {
//			log.warning("Not all UEMLoad activities could be stopped in given time");
//		}
		super(procedures);
	}

	@Override
	public boolean execute() {
		if (super.execute()) {
			log.info("All easyTravel procedures stopped successfully.");
			return true;
		}
		log.warning("Not all easyTravel procedures could be stopped in expected time.");
		return false;
	}


}
