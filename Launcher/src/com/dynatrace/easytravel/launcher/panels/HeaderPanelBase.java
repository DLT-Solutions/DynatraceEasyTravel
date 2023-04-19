package com.dynatrace.easytravel.launcher.panels;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.procedures.utils.UserSelectionTechListener;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.google.common.base.Strings;

import ch.qos.logback.classic.Logger;

/**
 * Base class for the {@link HeaderPanel} which extracts non-GUI functionality for easier testing and
 * re-use.
 *
 * @author cwat-dstadler
 */
public abstract class HeaderPanelBase implements HeaderPanelInterface {
	private static final Logger LOGGER = LoggerFactory.make();

	private Map<String, String> customerFrontendUris = new TreeMap<String, String>();
	private String webserverCustomerFrontendUri = null;
	private String webserverB2bFrontendUri = null;
	private String b2bFrontendUri = null;
	private String webserverAndroidFrontendUri = null;
	private String androidFrontendUri = null;
	private String webserverIOSFrontendUri = null;
	private String iOSFrontendUri = null;
	protected static boolean manualVisitsCreation;
	protected boolean b2bEnabled = false;
	protected boolean customerEnabled = false;
	protected boolean configControllingAllowed = true;

	private Set<UserSelectionTechListener> techListeners = new HashSet<UserSelectionTechListener>();

	protected static String getAgentPath(EasyTravelConfig CONFIG) {
		String path = "";

		// get path of agent
		DtAgentConfig config = new DtAgentConfig(CONFIG.backendSystemProfile, CONFIG.backendAgent, CONFIG.backendAgentOptions, CONFIG.backendEnvArgs);
		try {
			path = config.getAgentPath(Technology.JAVA);
		} catch (ConfigurationException e) {
			// no need to warn here, we do this later during starting the procedures anyway...
			// LOGGER.log(Level.WARNING, "Could not determine Java Agent Path", e);
		}

		return Strings.nullToEmpty(path);
	}

	protected static String encode(String path) {
		try {
			return URLEncoder.encode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn("Cannot encode system profile: " + path);
			return null;
		}
	}

	protected static File getSystemProfile(String agentPath, String dtVersion) {
		String path;

		// if we find "5.5" in the agent path, we expect this to be a 5.5 installation
		if ((agentPath != null && agentPath.contains("5.5")) ||
				(dtVersion != null && dtVersion.startsWith("5.5"))) {
			LOGGER.info("Providing 5.5 version of system profile");
			path = Directories.getInstallDir() + File.separator + MessageConstants.EASYTRAVEL_PLUGINNAME_55;
		} else {
			path = Directories.getInstallDir() + File.separator + MessageConstants.EASYTRAVEL_PLUGINNAME;
		}

		try {
			return new File(path).getCanonicalFile(); // may not exist
		} catch (IOException e) {
			LOGGER.warn("Cannot get canonical path of system profile: " + path);
			return null;
		}
	}

	@Override
	public void updateCustomerFrontendUri(final String id, final String uri) {
		// only perform an update if there is any change to be done, i.e.
		//		on uri == null, update only if there is an entry with this id
		//		on uri != null, update only if there is not yet exactly such an entry
		if (uri != null && !uri.equals(customerFrontendUris.get(id)) &&
				// don't display duplicate values
				!customerFrontendUris.containsValue(uri)) {
			customerFrontendUris.put(id, uri);
			updateDescription();
		} else if (uri == null && customerFrontendUris.containsKey(id)){
			customerFrontendUris.remove(id);
			updateDescription();
		}
	}

	@Override
	public void updateWebserverCustomerFrontendUri(final String uri) {
		// only perform an update if there is any change to be done
		if(!StringUtils.equals(uri, webserverCustomerFrontendUri)) {
			webserverCustomerFrontendUri = uri;
			updateDescription();
		}

	}

	@Override
	public void updateWebserverB2bFrontendUri(final String uri) {
		// only perform an update if there is any change to be done
		if(!StringUtils.equals(uri, webserverB2bFrontendUri)) {
			webserverB2bFrontendUri = uri;
			updateDescription();
		}
	}

	@Override
	public void updateB2BFrontendUri(final String uri) {
		b2bFrontendUri = uri;
		updateDescription();
	}

	@Override
	public void updateWebserverAndroidFrontendUri(final String uri) {
		// only perform an update if there is any change to be done
		if(!StringUtils.equals(uri, webserverAndroidFrontendUri)) {
			webserverAndroidFrontendUri = uri;
			updateDescription();
		}
	}

	@Override
	public void updateAndroidFrontendUri(final String uri) {
		androidFrontendUri = uri;
		updateDescription();
	}

	@Override
	public void updateWebserverIOSFrontendUri(final String uri) {
		// only perform an update if there is any change to be done
		if(!StringUtils.equals(uri, webserverIOSFrontendUri)) {
			webserverIOSFrontendUri = uri;
			updateDescription();
		}
	}

	@Override
	public void updateIOSFrontendUri(final String uri) {
		iOSFrontendUri = uri;
		updateDescription();
	}

	/**
	 * Computes how the links should look like for customer frontend and b2b frontend
	 * and will update the description accordingly.
	 *
	 * @author dominik.stadler
	 */
	protected void updateDescription() {
		// nothing to update if UI is not there or already gone
		if(isDisposed()) {
			return;
		}

		final StringBuilder customer = new StringBuilder();
		if (webserverCustomerFrontendUri == null) {
			if (customerFrontendUris.isEmpty()) {
				// nothing to display for Customer
				customer.append(MessageConstants.NOT_AVAILABLE);
				customerEnabled = false;
			} else {
				for (Map.Entry<String, String> entry : customerFrontendUris.entrySet()) {
					customer.append(entry.getValue()).append(" ");
				}
			}
		} else {
			customer.append(webserverCustomerFrontendUri);
		}
		setCustomerEnabled(customer);

		final StringBuilder mobile = new StringBuilder();
		if (androidFrontendUri == null && iOSFrontendUri == null) {
			// if there is no special Mobile to display, re-use Customer
			//mobile.append(MessageConstants.NOT_AVAILABLE);
			mobile.append(customer);
		} else {
			if (webserverAndroidFrontendUri == null && androidFrontendUri != null) {
				mobile.append(androidFrontendUri);
			} else if (webserverAndroidFrontendUri != null){
				mobile.append(webserverAndroidFrontendUri);
			}

			if(mobile.length() > 0) {
				mobile.append(" ");
			}

			if (webserverIOSFrontendUri == null && iOSFrontendUri != null) {
				mobile.append(iOSFrontendUri);
			} else if (webserverIOSFrontendUri != null){
				mobile.append(webserverIOSFrontendUri);
			}
		}

		final StringBuilder b2b = new StringBuilder();
		if (b2bFrontendUri == null) {
			// nothing to display for B2B
			b2b.append(MessageConstants.NOT_AVAILABLE);
		} else {
			if (webserverB2bFrontendUri == null) {
				b2b.append(b2bFrontendUri);
			} else {
				b2b.append(webserverB2bFrontendUri);
			}
		}
		setB2bEnabled(b2b);

		updateLinks(customer.toString(), b2b.toString(), mobile.toString());
	}

	protected abstract void updateLinks(String customer, String b2b, String mobile);
	protected abstract boolean isDisposed();

	private void setCustomerEnabled(StringBuilder customer) {
		customerEnabled = setButtonEnabled(customer);
	}

	private void setB2bEnabled(StringBuilder b2b) {
		b2bEnabled = setButtonEnabled(b2b);
	}

	private boolean setButtonEnabled(StringBuilder link) {
		if (link.toString().contains("<a href")) {
			return true;
		}
		return false;
	}

	public boolean isInManualMode() {
		return manualVisitsCreation;
	}


	protected void notifyUserChangedState(Technology technology, boolean enabled) {
		for (UserSelectionTechListener techListener : techListeners) {
			techListener.notifyUserChangedState(technology, enabled);
		}
	}

	public void registerTechnologyListener(UserSelectionTechListener techListener) {
		techListeners.add(techListener);
	}

	@Override
	public boolean isConfigControllingAllowed() {
		return configControllingAllowed && !manualVisitsCreation;
	}

	protected static boolean isSystemProfileLinkVisible() {
		return DtVersionDetector.isClassic();
	}
}
