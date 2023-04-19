package com.dynatrace.easytravel.launcher.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.dynatrace.diagnostics.uemload.UemLoadScheduler;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.engine.ProcedureStateListener;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.engine.StatefulProcedure;
import com.dynatrace.easytravel.launcher.fancy.MenuActionCallback;
import com.dynatrace.easytravel.launcher.fancy.MenuItem;
import com.dynatrace.easytravel.launcher.fancy.MenuPage;
import com.dynatrace.easytravel.launcher.fancy.MenuVisibilityCallbackAdapter;
import com.dynatrace.easytravel.launcher.fancy.PageProvider;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.panels.HeaderPanelInterface;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.plugin.RemotePluginController;
import com.dynatrace.easytravel.spring.Plugin;
import com.dynatrace.easytravel.spring.PluginInfoList;
import com.dynatrace.easytravel.util.LocalUriProvider;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

/**
 * A listener which is notified of applications becoming online/offline via the
 * {@link ProcedureStateListener}. It listens for the Business Backend process to become available
 * and replaces the initial plugin-menu page with a list of plugins.
 *
 * If the Business Backend application becomes offline, it will replace the plugin-page with one
 * stating that plugins are not available.
 *
 * It also handles enabling/disabling plugins via a {@link MenuActionCallback} and handles any
 * required refreshing of the menu.
 */
public final class PluginStateListener implements ProcedureStateListener {

    private static final Logger LOGGER = LoggerFactory.make();

    private State state = State.getDefault();
    private PageProvider menu = null;
    private HeaderPanelInterface headerPanel = null;
    private Collection<PluginEnabledListener> pluginEnabledListeners = new ArrayList<PluginEnabledListener>();

    /**
     * Main listener method which is notified about each application startup/stop.
     */
    @Override
    public void notifyProcedureStateChanged(final StatefulProcedure subject, final State oldState, final State newState) {
        if (!Constants.Procedures.BUSINESS_BACKEND_ID.equalsIgnoreCase(subject.getMapping().getId())) {
            return;
        }

        if (State.OPERATING.equals(newState) || State.TIMEOUT.equals(newState)) {
            state = State.OPERATING;
		        populatePluginMenuPage();
        } else if (State.STOPPING.equals(newState)) {
            state = State.STOPPED;
            disablePluginMenuPage();
        } else {
            state = State.STOPPED;
        }
    }

    /**
     * If we currently show plugins then fetch the current list from the backend server and update
     * the page with it.
     *
     * @author dominik.stadler
     */
    public void refreshPluginMenuPage() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Periodic refresh of the plugin menu page");
        }

	    populatePluginMenuPage();

	    updateLoadTooltip();
    }

    private void updateLoadTooltip() {
	    if(headerPanel != null) {
	    	headerPanel.setDebugInfo("Load-Threads: " + UemLoadScheduler.getActiveCount() + ", Load-Queue: " + UemLoadScheduler.getQueueSize());
	    }
    }

    private synchronized void populatePluginMenuPage() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Updating plugin menu page with current state of plugins");
        }

        // refresh and enable menu page for plugins
        MenuPage page;
        try {
            RemotePluginController controller = new RemotePluginController();
            PluginInfoList allPlugins = new PluginInfoList(controller.requestAllPlugins());
            PluginInfoList enabledPlugins = new PluginInfoList(controller.requestEnabledPluginNames());

            if (allPlugins.isEmpty()) {
                page = createNoPluginsPage();
            } else {
                // create the new plugins page and pass in a runnable that can be used to
                // refresh the page as soon as some plugin state is changed
                page = createPluginsPage(allPlugins, enabledPlugins, new Runnable() {

                    @Override
                    public void run() {
                        populatePluginMenuPage();
                    }
                });
            }

            // update others about current list of plugins
            updatePluginEnabledState(enabledPlugins);

        } catch (IOException e) {
        	// only display errors if we expect the process to be up and running
            if (State.OPERATING.equals(state)) {
	        	// this happens as soon as Business Backend is done, only report the error as "WARNING" and
	        	// the exception details as "FINE" to not fill up the log here unless needed...
	            LOGGER.warn(TextUtils.merge(MessageConstants.COULD_NOT_READ_PLUGIN_STATE_TITLE, LocalUriProvider.getBackendWebServiceUri(BaseConstants.CONFIGURATION_SERVICE), e.getMessage()));
	            if (LOGGER.isDebugEnabled()) {
	            	LOGGER.debug(TextUtils.merge(MessageConstants.COULD_NOT_READ_PLUGIN_STATE_TITLE, LocalUriProvider.getBackendWebServiceUri(BaseConstants.CONFIGURATION_SERVICE), e.getMessage()), e);
	            }
	            page = createErrorPluginsPage(e);
            } else {
            	// display normal "no plugins available" message if process is not available, e.g. because it was stopped manually
            	page = createNoPluginsPage();
            }

            // update others about current list of plugins
            updatePluginEnabledState(new PluginInfoList());

        }

        // replace the page, we currently expect plugins to be the last menu
        replacePluginPage(page);
    }

    private void updatePluginEnabledState(PluginInfoList pluginInfoList) {
        for (PluginEnabledListener pluginEnabledListener : pluginEnabledListeners) {
            pluginEnabledListener.notifyEnabledPlugins(pluginInfoList);
        }
    }

    private synchronized void disablePluginMenuPage() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Backend is stopping, disabling plugin menu page");
        }

        // remove plugins menu page and add "plugins disabled" page
        replacePluginPage(createNoPluginsPage());
    }

    private synchronized void replacePluginPage(MenuPage page) {
        if (menu == null) {
            return;
        }
        // replace the page, we currently expect Plugins to be the last menu!
		   menu.replacePage(menu.getPageCount()-1, page);
    }

    public synchronized void setMenu(final PageProvider menu) {
        this.menu = menu;
    }

    public synchronized void setHeaderPanel(final HeaderPanelInterface headerPanel) {
    	this.headerPanel = headerPanel;
    }

	public synchronized void setPluginEnabledListener(PluginEnabledListener headerListener) {
        this.pluginEnabledListeners.add(headerListener);
    }

	/**
     * Create a new MenuPage where plugins are not available yet, i.e. it simply states that plugins
     * cannot be displayed yet.
     *
     * This does not add the page to the menu component yet!
     *
     *
     * @return
     * @author dominik.stadler
     */
    public static MenuPage createNoPluginsPage() {
        final MenuPage page = new MenuPage(null, MessageConstants.SCENARIO_GROUP_PLUGINS, null, MenuVisibilityCallbackAdapter.VISIBLE_AND_ENABLED);

        page.addItem(new MenuItem(null, MessageConstants.NO_PLUGINS_AVAILABLE_TITLE, MessageConstants.NO_PLUGINS_AVAILABLE_DESC, null, MenuVisibilityCallbackAdapter.VISIBLE_AND_ENABLED));
        return page;
    }

    /**
     * Create a new MenuPage that provides information about the error that occurred while trying to
     * read plugin state.
     *
     * @param exception
     * @return
     * @author dominik.stadler
     */
    private static MenuPage createErrorPluginsPage(final Exception exception) {
        final MenuPage page = new MenuPage(null, MessageConstants.SCENARIO_GROUP_PLUGINS, null, MenuVisibilityCallbackAdapter.VISIBLE_AND_ENABLED);

        page.addItem(new MenuItem(null,
        		TextUtils.merge(MessageConstants.COULD_NOT_READ_PLUGIN_STATE_TITLE, LocalUriProvider.getBackendWebServiceUri(BaseConstants.CONFIGURATION_SERVICE), exception.getMessage()),
        		TextUtils.merge(MessageConstants.COULD_NOT_READ_PLUGIN_STATE_DESC, exception.getMessage()), null,
                MenuVisibilityCallbackAdapter.VISIBLE_AND_ENABLED));
        return page;
    }

    /**
     * Create a page for the provided list of plugins, displaying enabled ones with a different
     * icon.
     *
     * The runnable is used to refresh the menu page whenever the user clicks on any of the
     * resulting menu-items so that changes to plugin states become visible.
     * @param allPlugins TODO
     * @param enabledPlugins TODO
     * @param runnable
     *
     * @return A new page that can be added to the menu.
     *
     * @author dominik.stadler
     */
    private static MenuPage createPluginsPage(PluginInfoList allPlugins, PluginInfoList enabledPlugins,
    		final Runnable refreshRunnable) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Found plugins: " + allPlugins + " - Enabled: " + enabledPlugins);
        }

        MenuPage page = new MenuPage(null, MessageConstants.SCENARIO_GROUP_PLUGINS, null, MenuVisibilityCallbackAdapter.VISIBLE_AND_ENABLED);

        for (Plugin plugin : allPlugins) {
            boolean enabled = enabledPlugins.contains(plugin);
            PluginController controller = new PluginController(plugin.getName(), enabled, refreshRunnable);

            String description = plugin.getDescription();
            if (description == null || description.isEmpty()) {
            	description = MessageConstants.NO_PLUGIN_DESCRIPTION_AVAILABLE;
            }

            String image = enabled ? Constants.Images.STATE_ON : Constants.Images.STATE_OFF;
            MenuItem item = new MenuItem(image, plugin.getName(), description, controller, MenuVisibilityCallbackAdapter.VISIBLE_AND_ENABLED);
            page.addItem(item);
        }
		page.setIsFilterEnabled(true);
        return page;
    }

    /**
     * A callback which is invoked when a user clicks on a plugin-item to enable/disable it.
     *
     * @author dominik.stadler
     */
    private static final class PluginController implements MenuActionCallback {

        private final RemotePluginController pluginController = new RemotePluginController();
        private String text = null;
        private final String plugin;
        private boolean enabled;
        private final Runnable refreshRunnable;

        public PluginController(String plugin, boolean enabled, Runnable refreshRunnable) {
            this.plugin = plugin;
            this.enabled = enabled;
            this.refreshRunnable = refreshRunnable;
        }

        @Override
        public void run() {
            // toggle state
            enabled = !enabled;

            pluginController.sendEnabled(plugin, enabled, refreshRunnable);
        }

        @Override
        public void setText(String text) {
            this.text = text;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public void stop() {
        }

        @Override
        public Boolean isEnabled() {
            return enabled;
        }
    }

    public void disposePluginStateListener() {
    	this.menu = null;
    	this.headerPanel = null;
    	this.pluginEnabledListeners.clear();
    }
}
