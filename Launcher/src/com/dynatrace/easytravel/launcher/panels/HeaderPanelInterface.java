package com.dynatrace.easytravel.launcher.panels;

import com.dynatrace.easytravel.config.CustomerTrafficScenarioEnum;
import com.dynatrace.easytravel.config.EasyTravelConfig;

/**
 * Abstraction of the HeaderPanel to make depending code independent of SWT classes.
 *
 * @author dominik.stadler
 */
public interface HeaderPanelInterface {
	/**
	 * Updates the URI of the B2B Frontend that is displayed in the HeaderPanel
	 *
	 * @param uri If null, the URI is removed, otherwise this URI is added or replaces the existing URI
	 * @author dominik.stadler
	 */
	void updateB2BFrontendUri(String uri);

	/**
	 * Updates the URI of the Customer Frontend that is displayed in the HeaderPanel
	 *
	 * @param id The unique id of this URI, to allow multiple uris to be set or re-set via null
	 * @param uri If null, the URI is removed, otherwise this URI is added or replaces the existing URI
	 * @author dominik.stadler
	 */
	void updateCustomerFrontendUri(String id, String uri);

	/**
	 * Updates the URI of the Apache Webserver that provides the Customer Frontend
	 * that is displayed in the HeaderPanel
	 *
	 * @param uri If null, the URI is removed, otherwise this URI is added or replaces the existing URI
	 * @author dominik.stadler
	 */
	void updateWebserverCustomerFrontendUri(String uri);

	/**
	 * Updates the URI of the Apache Webserver that provides the B2B Frontend
	 * that is displayed in the HeaderPanel
	 *
	 * @param uri If null, the URI is removed, otherwise this URI is added or replaces the existing URI
	 * @author stefan.moschinski
	 */
	void updateWebserverB2bFrontendUri(String uri);

	/**
	 * Updates the URI where the Mobile Apps can be downloaded which is displayed in the HeaderPanel
	 *
	 * @param uri
	 * @author dominik.stadler
	 */
	void updateAndroidFrontendUri(String uri);

	/**
	 * Updates the URI of the Apache Webserver that provides the Mobile Frontend
	 * that is displayed in the HeaderPanel
	 *
	 * @param uri If null, the URI is removed, otherwise this URI is added or replaces the existing URI
	 * @author stefan.moschinski
	 */
	void updateWebserverAndroidFrontendUri(String uri);

	/**
	 * Updates the URI where the Mobile Apps can be downloaded which is displayed in the HeaderPanel
	 *
	 * @param uri If null, the URI is removed, otherwise this URI is added or replaces the existing URI
	 * @author dominik.stadler
	 */
	void updateIOSFrontendUri(String uri);

	/**
	 * Updates the URI of the Apache Webserver that provides the Mobile Frontend
	 * that is displayed in the HeaderPanel
	 *
	 * @param uri If null, the URI is removed, otherwise this URI is added or replaces the existing URI
	 * @author stefan.moschinski
	 */
	void updateWebserverIOSFrontendUri(String uri);

	void activateUEMLoadPanel();

	void deactivateUEMLoadPanel();

	void enableTaggedWebRequest();

	void disableTaggedWebRequest();

	void setLoad(int value);

	void setTrafficLabel(String label);

	void resetTrafficLabel();
	
	void setCustomerTrafficScenario(CustomerTrafficScenarioEnum scenario);

	/**
	 *
	 * @return <code>true</code> if the settings of the {@link EasyTravelConfig} should
	 * 			be displayed in the {@link HeaderPanel}
	 * @author stefan.moschinski
	 */
	boolean isConfigControllingAllowed();

	void setDCRUMVisible(boolean visible);
	
	void setDebugInfo(String info);
}
