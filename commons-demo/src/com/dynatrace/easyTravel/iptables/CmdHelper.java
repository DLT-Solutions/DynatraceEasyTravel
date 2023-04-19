package com.dynatrace.easytravel.iptables;

import java.io.File;

import org.apache.commons.lang3.ArrayUtils;

import com.dynatrace.easytravel.config.EasyTravelConfig;

/**
 * Class responsible for creating low level commands. Ready for usage in {@link CmdExecutor}
 * Commands are ready to execute by Runtime.getRuntime().exec(String[] cmd)
 *
 * cwpl-rorzecho
 */
public class CmdHelper {

    private static final String[] BASH = {"/bin/bash", "-c"};
    private static final String[] SH = {"/bin/sh", "-c"};
    private static final String ECHO_PASSWORD = "echo \"" + EasyTravelConfig.read().sudoPassword + "\"";
    private static final String SUDO = "sudo -S ";

    private static String[] cmdBash(String cmdString) {
        return ArrayUtils.add(BASH, cmdString);
    }

    @SuppressWarnings("unused")
	private static String[] cmdSh(String cmdString) {
        return ArrayUtils.add(SH, cmdString);
    }

    /**
     * Create general purpose cmd
     *
     * @param cmd Linux command for instance: ls -l
     * @return
     */
    public static String[] createCmd(String cmd) {
        StringBuilder cmdString = new StringBuilder();
            cmdString.append(ECHO_PASSWORD).append("|").append(SUDO).append(cmd);

        return cmdBash(cmdString.toString());
    }

    /**
     * Create entry in the iptabless
     *
     * Rule string example:
     *  iptables -A INPUT -p tcp --source-port 8091 -j DROP -m statistic --mode random --probability 0.4
     *
     * @param rule iptable rule with no comment parameters
     * @param comment predefined comment
     * @return
     */
    public static String[] createIptablesEntryCmd(String rule, String comment) {
        StringBuilder cmdString = new StringBuilder();
            cmdString.append(ECHO_PASSWORD).append("|").append(SUDO).append(rule);
            cmdString.append(" -m comment --comment \"").append(comment).append("\"");

        return cmdBash(cmdString.toString());
    }

    /**
     * Remove iptables entry for given chainName and ruleNum
     *
     * Command is based on Linux clause: iptables -D INPUT 1
     *
     * @param chainName PREROUTING, INPUT, FORWARD, OUTPUT, POSTROUTING
     * @param ruleNum iptables rule number
     * @return
     */
    public static String[] removeIptablesEntryCmd(String chainName, String ruleNum ) {
        String cmdString = (new StringBuilder()).append(ECHO_PASSWORD).append("|").append(SUDO)
                .append("iptables -D ").append(chainName).append(" ").append(ruleNum).toString();

        return cmdBash(cmdString);
    }

    /**
     * Save iptables to external file
     * @param filePath
     * @return
     */
    public static String[] saveIptables(File file) {
        String cmdString = (new StringBuilder()).append(ECHO_PASSWORD).append("|").append(SUDO)
                .append("iptables-save > ").append("\"").append(file.getPath()).append("\"").toString();

        return cmdBash(cmdString);
    }

}
