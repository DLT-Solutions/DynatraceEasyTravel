package com.dynatrace.easytravel.iptables;

import java.io.File;
import java.io.IOException;

/**
 * Iptables interface
 *
 * cwpl-rorzecho
 */
public interface IptablesOperations {

    /**
     * Get iptables rules
     *
     * @return
     * @throws IOException
     */
    String getIptables() throws IOException;

    /**
     * Add rule to iptables with specified comment
     * @param rule
     * @param comment
     */
    void addRule(String rule, String comment);

    /**
     * Remove all rules for comment
     * @param comment
     */
    void removeRules(String comment);

    /**
     * Remove particular rule
     * @param chainName
     * @param chainNum
     */
    void removeRule(String chainName, String chainNum);

    /**
     * Save iptables to external file
     * @param file
     */
    public void saveIptables(File file);

}
