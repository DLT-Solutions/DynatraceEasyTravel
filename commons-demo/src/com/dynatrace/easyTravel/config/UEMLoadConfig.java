package com.dynatrace.easytravel.config;


public interface UEMLoadConfig {

	/**
	 * Compares this implementation of {@link UEMLoadConfig} to the given one
	 *
	 * @param otherCfg
	 * @return
	 * @author stefan.moschinski
	 */
	boolean hasDifferentLoadSettingsThan(UEMLoadConfig otherCfg);

	/**
	 * Compare customer load setting
	 * @param otherConfig
	 * @return
	 * @author cwpl-rpsciuk
	 */
	boolean hasDifferentCustomerLoad(UEMLoadConfig otherConfig);

	/**
	 *
	 * @return base load setting of this configuration instance
	 * @author stefan.moschinski
	 */
	int getDefaultLoad();

	/**
	 *
	 * @return load ratio of the B2B component
	 * @author stefan.moschinski
	 */
	double getB2bRatio();

	/**
	 *
	 * @return load ratio of the mobile component
	 * @author stefan.moschinski
	 */
	double getMobileNativeRatio();


	/**
	 *
	 * @return load ratio of the mobile browser component
	 */
	double getMobileBrowserRatio();

	/**
	 *
	 * @return load ratio of the customer component
	 * @author stefan.moschinski
	 */
	double getCustomerRatio();


	/**
	 * @return load ratio of RMI or JMS calls to the hot deal service on the backend
	 * @author wolfgang.groiss
	 */
	double getHotDealServiceRatio();

	double getHeadlessCustomerRatio();

	double getHeadlessAngularRatio();

	double getHeadlessMobileAngularRatio();
	
	double getHeadlessB2BRatio();

	/**
	 *
	 * @return the UEM load settings as a string
	 * @author stefan.moschinski
	 */
	String getUemLoadConfigAsString();

	/**
	 * @return type of the customer traffic scenario
	 */
	CustomerTrafficScenarioEnum getCustomerTrafficScenario();

	/**
	 * "returns the type of headless traffic scenario
	 */
	HeadlessTrafficScenarioEnum getHeadlessTrafficScenario();

}
