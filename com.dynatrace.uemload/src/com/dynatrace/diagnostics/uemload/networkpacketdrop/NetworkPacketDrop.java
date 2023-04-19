package com.dynatrace.diagnostics.uemload.networkpacketdrop;

import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeListener;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.iptables.Iptables;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

/**
 * Add iptables rules to generate network problems caused by packet drops
 *
 * cwpl-rorzecho
 */
public class NetworkPacketDrop implements PluginChangeListener {
    private static Logger LOGGER = LoggerFactory.make();

    private static boolean isEnabled;
    private Iptables iptables;

    private static String COMMENT = NetworkPacketDrop.class.getSimpleName();
    private static final String PROPERTY_OS_NAME = System.getProperty("os.name");

    public NetworkPacketDrop() {
        this.iptables = new Iptables();
    }

    @Override
    public void pluginsChanged() {
        if (PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.NETWORK_PACKET_DROP)) {
            if (!isEnabled) {
                addIptablesRules();
            }
        } else {
            removeIptablesRules();
        }
    }

    /**
     * Add iptables rules specified in easyTravelConfig.properties file
     * @param iptables
     */
    private void addIptablesRules() {
        if (isLinux()) {
            iptables.addRules(EasyTravelConfig.read().iptablesRules, COMMENT);

            printIptables(iptables);
            isEnabled = true;
        } else {
            LOGGER.warn(TextUtils.merge("The prefered platform for {0} pattern is Linux", COMMENT));
        }
    }

    private void removeIptablesRules() {
        if (isLinux()) {
            iptables.removeRules(COMMENT);

            printIptables(iptables);
            isEnabled = false;
        }
    }

    /**
     * Print entire ipables
     * @param iptables
     */
    private void printIptables(Iptables iptables) {
        LOGGER.info(TextUtils.merge("iptables \n {0}", iptables.getIptables()));
    }

    /**
     * Check valid platform
     * @return
     */
    private boolean isLinux() {
        return PROPERTY_OS_NAME != null && PROPERTY_OS_NAME.contains("Linux");
    }
}
