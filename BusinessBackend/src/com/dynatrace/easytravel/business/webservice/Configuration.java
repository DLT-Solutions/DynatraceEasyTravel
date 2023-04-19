package com.dynatrace.easytravel.business.webservice;

public interface Configuration {

	abstract void setExternalUrl(String external);

	abstract String getExternalUrl();

	/**
	 * triggers memory leak in {@link JourneyService}
	 *
	 * @param memoryLeakEnabled
	 * @author peter.kaiser
	 */
	void setMemoryLeakEnabled(boolean memoryLeakEnabled);

	boolean isMemoryLeakEnabled();

	/**
	 * triggers database spamming in {@link BookingService}
	 *
	 * @param dBSpammingEnabled
	 * @author peter.kaiser
	 */
	void setDBSpammingEnabled(boolean dBSpammingEnabled);

	boolean isDBSpammingEnabled();
	
	/**
	 * triggers database spamming in {@link AuthenticationService}
	 * @param dbSpammingJourneyAuthEnabled 
	 */
	void setDBSpammingAuthEnabled(boolean dbSpammingJourneyAuthEnabled);
	
	/**
	 * @return true if dbspammingauthplugin is enabled
	 */
	boolean isDBSpammingAuthEnabled();

	/**
	 * triggers deadlock in frontend - promotions
	 *
	 * @param frontendDeadlockEnabled
	 * @author peter.kaiser
	 */
	void setFrontendDeadlockEnabled(boolean frontendDeadlockEnabled);

	boolean isFrontendDeadlockEnabled();

	/**
	 * Allows to adjust the amount of CPU that is consumed by this instance of the Business Backend.
	 *
	 * See easyTravelConfig.properties for more description about the behavior of this setting.
	 *
	 * Note: This has only effect on the one instance of the Business Backend, this is not handled
	 * like a problem pattern!
	 *
	 * @param timeMS
	 * @author cwat-dstadler
	 */
	void setBackendCPUCycleTime(long timeMS);

	/**
	 * Returns the currently used backendCPUCycleTime of this instance of the Business Backend.
	 *
	 * @return
	 * @author cwat-dstadler
	 */
	long getBackendCPUCycleTime();

	/**
	 * Allows to adjust the amount of additional CPU that is consumed by JourneyService when calling the ValidateName plugin.
	 *
	 * Note: This has only effect on the one instance of the Business Backend, this is not handled
	 * like a problem pattern!
	 *
	 * @param timeMS
	 * @author cwpl-wjarosz
	 */
	void setCPULoadJourneyServiceWaitTime(long timeMS);

	/**
	 * Returns the currently used cpuLoadJourneyServiceWaitTime of this instance of the Business Backend.
	 *
	 * @return
	 * @author cwpl-wjarosz
	 */
	long getCPULoadJourneyServiceWaitTime();

	/**
	 * Allows to adjust the CPU calibration coefficient for this Business Backend.
	 *
	 * See CPUHelper class for the usage of this coefficient.
	 *
	 * Note: This has only effect on the one instance of the Business Backend, this is not handled
	 * like a problem pattern!
	 *
	 * @param double
	 * @author cwpl-wjarosz
	 */
	void setBackendCPUCalibration(double calibration);

	/**
	 * Returns the currently used CPUCalibration coefficient of this instance of the Business Backend.
	 *
	 * @return
	 * @author cwpl-wjarosz
	 */
	double getBackendCPUCalibration();
	
	/**
	 * @author cwpl-rpsciuk
	 */	
	void setDBSpammingAuthSize(int spamSize );
	
	/**
	 * @author cwpl-rpsciuk
	 * @return
	 */
	int getDBSpammingAuthSize();
	
	/**
	 * @author cwpl-rpsciuk
	 */
	void setDBSpammingAuthDelay(int delay);
	
	/**
	 * @return
	 * @author cwpl-rpsciuk
	 */
	int getDBSpammingAuthDelay();
	
	/**
	 * @return
	 * @author cwpl-rpsciuk
	 */
	boolean isFullAuthServiceSpammingEnabled();
	
	/**
	 * @author cwpl-rpsciuk
	 */
	void setFullAuthServiceSpamming(boolean full);

    /**
     * @return
     * @author cwpl-wjarosz
     */
    void setDatabaseSlowDownDelay(int delay);

    /**
     * @return
     * @author cwpl-wjarosz
     */
    int getDatabaseSlowDownDelay();
}
