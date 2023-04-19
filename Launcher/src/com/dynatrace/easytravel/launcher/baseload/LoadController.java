package com.dynatrace.easytravel.launcher.baseload;

import com.dynatrace.easytravel.launcher.panels.HeaderPanelInterface;


public interface LoadController {

	/**
	 *
	 * @return the B2B UEM load controller
	 * @author stefan.moschinski
	 */
	BaseLoad getB2bLoadController();

	/**
	 *
	 * @return the customer frontend UEM load controller
	 * @author stefan.moschinski
	 */
	BaseLoad getCustomerLoadController();

	/**
	 *
	 * @return the mobile native UEM load controller
	 * @author stefan.moschinski
	 */
	BaseLoad getMobileNativeLoadController();

	/**
	 *
	 * @return the mobile browser UEM load controller
	 * @author benjamin.fellner
	 */
	BaseLoad getMobileBrowserLoadController();


	/**
	 *
	 * @return the hot deal JMS/RMI load controller
	 * @author wolfgang.groiss
	 */
	BaseLoad getHotDealLoadController();

	BaseLoad getIotDevicesLoadController();

	/**
	 *
	 * @return the Headless load controller
	 * @author Kacper.Olszanski
	 */
	BaseLoad getHeadlessCustomerLoadController();

	/**
	 *
	 * @return the Headless mobile load controller
	 * @author Tomasz.Wieremjewicz
	 */
	BaseLoad getHeadlessMobileAngularLoadController();

	BaseLoad getHeadlessAngularLoadController();
	
	/**
	 * 
	 * @return the Headless B2B Load Controller
	 * @author krzysztof.sajko
	 */
	BaseLoad getHeadlessB2BLoadController();

	/**
	 *
	 * @return the current {@link HeaderPanelInterface}
	 * @author stefan.moschinski
	 */
	HeaderPanelInterface getHeaderPanel();

}
