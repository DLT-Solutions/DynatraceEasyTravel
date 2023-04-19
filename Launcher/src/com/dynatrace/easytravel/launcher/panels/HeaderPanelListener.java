package com.dynatrace.easytravel.launcher.panels;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.config.ConfigChangeListener;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.UEMLoadConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.UrlType;
import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.engine.BatchStateListener;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.Procedure;
import com.dynatrace.easytravel.launcher.engine.ProcedureStateListener;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.engine.StatefulProcedure;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.plugin.PluginEnabledListener;
import com.dynatrace.easytravel.launcher.procedures.BrowserProcedure;
import com.dynatrace.easytravel.launcher.process.Process;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.PluginInfoList;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

/**
 * This listener watches the CustomerFrontend and the B2BFrontend procedures
 * for state changes and informs the HeaderPanel about necessary updates to
 * the displayed links to the web pages.
 *
 * @author dominik.stadler
 *
 */
public class HeaderPanelListener implements ProcedureStateListener, ConfigChangeListener, PluginEnabledListener, BatchStateListener {
	private static final Logger LOGGER = LoggerFactory.make();

	private final HeaderPanelInterface panel;
	private UEMLoadConfig lastAppliedConfig = null;

	// for shortening the names of uris
	private String firstCustomerUri = null;
	private String firstCustomerUriId = null;

	public HeaderPanelListener(final HeaderPanelInterface panel) {
		super();
		this.panel = panel;
		//set lastAppliedConfig to some initial value
		this.lastAppliedConfig = EasyTravelConfig.read();
	}


	@Override
	public void notifyProcedureStateChanged(final StatefulProcedure subject,
			final State oldState, final State newState) {
		String id = subject.getMapping().getId();
		if (Constants.Procedures.B2B_FRONTEND_ID.equalsIgnoreCase(id)) {
			panel.updateB2BFrontendUri(getUri(subject, newState, subject.getURI()));
		} else if (Constants.Procedures.CUSTOMER_FRONTEND_ID.equalsIgnoreCase(id)) {
			// getName() is not unique as multiple frontends have the same name, therefore use the toString of the Procedure, which is the object-id
			panel.updateCustomerFrontendUri(subject.toString(), getUri(subject, newState, subject.getURI()));
			try {
				// these links only makes sense in WebLauncher
				if(Launcher.isWeblauncher()) {
					panel.updateAndroidFrontendUri(getUri(new MobileDummyProcedure(subject, true), newState, "Android"));
					panel.updateIOSFrontendUri(getUri(new MobileDummyProcedure(subject, false), newState, "iOS"));
				}
			} catch (CorruptInstallationException e) {
				LOGGER.warn("Unexpected exception while handling mobile uri", e);
			}
		} else if(Constants.Procedures.APACHE_HTTPD_ID.equals(id) ||
				Constants.Procedures.APACHE_HTTPD_PHP_ID.equals(id)){
			panel.updateWebserverCustomerFrontendUri(getUri(subject, newState, BaseConstants.UrlType.APACHE_JAVA_FRONTEND, subject.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND)));
			panel.updateWebserverB2bFrontendUri(getUri(subject, newState, BaseConstants.UrlType.APACHE_B2B_FRONTEND, subject.getURI(BaseConstants.UrlType.APACHE_B2B_FRONTEND)));
			try {
				// these links only makes sense in WebLauncher
				if(Launcher.isWeblauncher()) {
					panel.updateWebserverAndroidFrontendUri(getUri(new MobileDummyProcedure(subject, true), newState, BaseConstants.UrlType.APACHE_JAVA_FRONTEND, "Android"));
					panel.updateWebserverIOSFrontendUri(getUri(new MobileDummyProcedure(subject, false), newState, BaseConstants.UrlType.APACHE_JAVA_FRONTEND, "iOS"));
				}
			} catch (CorruptInstallationException e) {
				LOGGER.warn("Unexpected exception while handling mobile uri", e);
			}
		} else if (Constants.Procedures.NGINX_WEBSERVER_ID.equals(id)) {
            panel.updateWebserverCustomerFrontendUri(getUri(subject, newState, UrlType.NGINX_JAVA_FRONTEND, subject.getURI(BaseConstants.UrlType.NGINX_JAVA_FRONTEND)));
            panel.updateWebserverB2bFrontendUri(getUri(subject, newState, BaseConstants.UrlType.NGINX_B2B_FRONTEND, subject.getURI(BaseConstants.UrlType.NGINX_B2B_FRONTEND)));
            try {
                // these links only makes sense in WebLauncher
                if(Launcher.isWeblauncher()) {
                    panel.updateWebserverAndroidFrontendUri(getUri(new MobileDummyProcedure(subject, true), newState, BaseConstants.UrlType.NGINX_JAVA_FRONTEND, "Android"));
                    panel.updateWebserverIOSFrontendUri(getUri(new MobileDummyProcedure(subject, false), newState, BaseConstants.UrlType.NGINX_JAVA_FRONTEND, "iOS"));
                }
            } catch (CorruptInstallationException e) {
                LOGGER.warn("Unexpected exception while handling mobile uri", e);
            }
		}
	}

	@Override
	public void notifyBatchStateChanged(Scenario scenario, State oldState, State newState) {
		// reset cached values when batch-state changes to not shorten the only URL if a new scenario is started
		if(State.STARTING.equals(newState)) {
			firstCustomerUri = null;
			firstCustomerUriId = null;
		}
	}



	// small helper to get the correct Download URIs for the Mobile Apps
	private class MobileDummyProcedure extends BrowserProcedure {
		private final boolean android;
		private final String uri;

		public MobileDummyProcedure(Procedure subject, boolean android) throws CorruptInstallationException {
			super(subject.getMapping());

			this.android = android;
			this.uri = subject.getURI(BaseConstants.UrlType.APACHE_JAVA_FRONTEND);
		}

		@Override
		protected Process createProcess(ProcedureMapping mapping) throws CorruptInstallationException {
			return null;
		}

		@Override
		public String getURI() {
			if(uri == null) {
				return null;
			}

			if(android) {
				return TextUtils.merge("{0}apps/EasyTravelAndroid.apk", uri);
			} else {
				return TextUtils.merge("itms-services://?action=download-manifest&url={0}apps/easyTravel.plist", uri);
			}
		}

		@Override
		public String getURI(UrlType urlType) {
			return getURI();
		}
	}

	/**
	 * @return Returns a shortened name if the compare ends with the same characters than the given name. Keeps at least 7 characters always to not return an empty name
	 */
	protected static String shortenName(String name, String compare) {
		if(compare == null || name == null) {
			return name;
		}

		// reverse strings as StringUtils only has a commonPrefix method, but we want to cut off at the end
		String revName = new StringBuffer(name).reverse().toString();
		String revCompare = new StringBuffer(compare).reverse().toString();

		// we are just interested in how many characters are common
		int len = StringUtils.getCommonPrefix(revName, revCompare).length();

		// cut off common part, but keep at least 7 chars
		return name.substring(0,
				// use name.length()-len as substring-index with additional min/max to prevent out of bounds accesses
				Math.min(name.length(), Math.max(7, name.length()-len)));
	}

	/**
	 * Prepare a URI depending on the new state and the URI that is returned by the procedure itself.
	 *
	 * @param subject the Procedure to query for URI
	 * @param newState the new state of the Procedure
	 * @param name The name to give the link
	 * @return
	 * @author dominik.stadler
	 */
	private String getUri(Procedure subject, State newState, String name) {
		switch (newState) {
		case STOPPING:
		case STARTING:
		case UNKNOWN:
			// display an uri if it is available, but do not make it clickable, i.e. don't wrap it in <a href...

			// handle shortening of multiple URIs
			if (Constants.Procedures.CUSTOMER_FRONTEND_ID.equalsIgnoreCase(subject.getMapping().getId()) &&
					// exclude iOS/Android which use CUSTOMER_FRONTEND_ID as well...
					!(subject instanceof MobileDummyProcedure) &&
					// only shorten if this is not the same uri
					(firstCustomerUriId == null || !firstCustomerUriId.equals(subject.toString()))) {
				String ret = shortenName(name, firstCustomerUri);
				// store the first found uri for the next one
				rememberCustomerUri(subject, name);
				return ret;
			}
			return name;

		case OPERATING:
		case TIMEOUT:	// we don't know so we assume it is running
			// display an uri as a clickable link
			String uri = subject.getURI();
			if(uri != null) {
				// handle shortening of multiple URIs
				if (Constants.Procedures.CUSTOMER_FRONTEND_ID.equalsIgnoreCase(subject.getMapping().getId()) &&
						// exclude iOS/Android which use CUSTOMER_FRONTEND_ID as well...
						!(subject instanceof MobileDummyProcedure) &&
						// only shorten if this is not the same uri
						(firstCustomerUriId == null || !firstCustomerUriId.equals(subject.toString()))) {
					String ret = TextUtils.merge(BaseConstants.LINK_HREF, uri, shortenName(name, firstCustomerUri));
					// store the first found uri for the next one
					rememberCustomerUri(subject, name);
					return ret;
				}

				return TextUtils.merge(BaseConstants.LINK_HREF, uri, name);
			}
		default:
			// do not display an uri for STOPPED, FAILED, ACCESS_DENIED...
			return null;
		}
	}


	private void rememberCustomerUri(Procedure subject, String name) {
		// only remember very first id/URI to always use this URL fully and shorten all others
		if(firstCustomerUri == null) {
			firstCustomerUri = name;
			firstCustomerUriId = subject.toString();
		}
	}

	private String getUri(Procedure subject, State newState, BaseConstants.UrlType urlType, String name) {
		switch (newState) {
		case STOPPING:
		case STARTING:
		case UNKNOWN:
			return name;
		case OPERATING:
		case TIMEOUT:	// we don't know so we assume it is running
			String uri = subject.getURI(urlType);
			if(uri != null) {
				return TextUtils.merge(BaseConstants.LINK_HREF, uri, name);
			}
		default:
			return null;
		}
	}

	@Override
	public void notifyConfigLoaded(EasyTravelConfig oldCfg, EasyTravelConfig newCfg) {
		boolean changed = false;
		if (newCfg.hasDifferentCustomerLoad(lastAppliedConfig)) {
			panel.setCustomerTrafficScenario(newCfg.getCustomerTrafficScenario());
			changed = true;
		}

		if (!panel.isConfigControllingAllowed()) {
			// set it null to enforce the reload if the panel.isConfigControllingAllowed()
			// returns true
			lastAppliedConfig = null;
			return;
		}

		if (newCfg.hasDifferentLoadSettingsThan(lastAppliedConfig)) {
			panel.setLoad(newCfg.baseLoadDefault);
			changed = true;
		}

		if (changed) {
			this.lastAppliedConfig = newCfg;
		}
	}

	@Override
	public void notifyEnabledPlugins(PluginInfoList enabledPlugins) {
		// report DC-RUM Plugin state to the Header Panel
		if(enabledPlugins.contains(BaseConstants.Plugins.DC_RUM_EMULATOR)) {
			panel.setDCRUMVisible(true);
		} else {
			panel.setDCRUMVisible(false);
		}
	}
}
