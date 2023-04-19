package com.dynatrace.easytravel.iptables;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

/**
 * Class manages iptables entries
 *
 * cwpl-rorzecho
 */
public class Iptables implements IptablesOperations {
    private static final Logger LOGGER = LoggerFactory.make();

    private CmdExecutor cmdExecutor;

    public Iptables() {
        this.cmdExecutor = new IptablesCmdExecutor();
    }

    @Override
    public String getIptables() {
        String cmdOutput = null;
        String[] cmd = CmdHelper.createCmd("iptables -L -v -n --line-numbers");

        try {
            cmdOutput = cmdExecutor.execute(cmd);
        } catch (IOException e) {
            LOGGER.error(TextUtils.merge("Failed to get iptables rules: {0}", cmdOutput), e);
        }

        return cmdOutput;
    }

    @Override
    public void addRule(String rule, String comment) {
        String cmdOutput = null;
        String[] cmd = CmdHelper.createIptablesEntryCmd(rule, comment);

        try {
            cmdOutput = cmdExecutor.execute(cmd);
            LOGGER.info(TextUtils.merge("Add rule: {0}, {1}", rule, cmdOutput));
        } catch (IOException e) {
            LOGGER.error(TextUtils.merge("Failed to add rule: {0}, {1}", rule, cmdOutput), e);
        }
    }

    public void addRules(String[] rules, String comment) {
        for (String rule : rules) {
            addRule(rule, comment);
        }
    }


    @Override
    public void saveIptables(File file) {
        String[] cmd = CmdHelper.saveIptables(file);

        try {
            cmdExecutor.execute(cmd);
        } catch (IOException e) {
            LOGGER.error(TextUtils.merge("Failed to save iptables to file: {0}", file.getPath()), e);
        }
    }

    @Override
    public void removeRules(String comment) {
        String iptablesOutput = getIptables();

        Pattern rulePattern = Pattern.compile("^([0-9]+).*" + comment + "|^Chain (\\w+)", Pattern.MULTILINE);
        Matcher ruleMatcher = rulePattern.matcher(iptablesOutput);

        String chainName = null;

        while (ruleMatcher.find()) {
            String ruleNum = ruleMatcher.group(1);

            if (ruleNum == null) {
                chainName = ruleMatcher.group(2);
                continue;
            }

            removeRule(chainName, ruleNum);

            // remove rules recursively
            removeRules(comment);
        }
    }

    @Override
    public synchronized void removeRule(String chainName, String ruleNum) {
        String[] cmd = CmdHelper.removeIptablesEntryCmd(chainName, ruleNum);

        try {
            cmdExecutor.execute(cmd);
            LOGGER.info(TextUtils.merge("Removing rule: {0}:{1}", chainName, ruleNum));
        } catch (IOException e) {
            LOGGER.info(TextUtils.merge("Failed to remove rule: {0}:{1}", chainName, ruleNum), e);
        }

    }

    public CmdExecutor getCmdExecutor() {
        return cmdExecutor;
    }

    public void setCmdExecutor(CmdExecutor cmdExecutor) {
        this.cmdExecutor = cmdExecutor;
    }
}
