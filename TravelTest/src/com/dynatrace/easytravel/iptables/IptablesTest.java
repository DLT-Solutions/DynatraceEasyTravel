package com.dynatrace.easytravel.iptables;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.dynatrace.easytravel.utils.TestEnvironment;

/**
 * cwpl-rorzecho
 */
public class IptablesTest {

    private static String IPTABLES_OUTPUT = new File(TestEnvironment.TEST_DATA_PATH, "iptables-output.txt").getPath();
    private static String IPTABLE_RULE = "iptables -A INPUT -p tcp --source-port 8091 -j DROP -m statistic --mode random --probability 0.4";

    @Test
    public void iptablesAddRuleTest() {
        Iptables iptables = new Iptables();

        Assert.assertNotNull(iptables);

        iptables.setCmdExecutor(new CmdExecutor() {
            @Override
            public String execute(String[] cmd) throws IOException {
                Assert.assertEquals(3, cmd.length);

                Assert.assertTrue(cmd[0].contains("bash"));

                Assert.assertTrue(cmd[1].contains("c"));

                Assert.assertTrue(cmd[2].contains("testRule"));

                return "ok";
            }
        });

        iptables.addRule(IPTABLE_RULE, "testRule");
    }

    @Test
    public void iptablesGetRulesTest() {
        Iptables iptables = new Iptables();
        iptables.setCmdExecutor(new DummyGetRulesCmdExecutor());

        String[] rules = iptables.getIptables().split("\\r?\\n");

        Assert.assertEquals(12, rules.length);

        int easyTravelRules = 0;

        for (String rule : rules) {
            if (rule.contains("easyTravel")) {
                easyTravelRules++;
            }
        }

        Assert.assertEquals(4, easyTravelRules);

    }

    @Test
    public void iptablesRemoveEtRulesTest() {
        String iptableComment = "easyTravel";
        Iptables iptablesRules = new Iptables();
        iptablesRules.setCmdExecutor(new DummyGetRulesCmdExecutor());
        iptablesRules.removeRules(iptableComment);
    }

    /**
     * Simple class for testing recursive deleting iptables rules
     *
     * NOTE: Here we are simulating deleting iptables rule with no reindexing chain numbers
     *       after deleting! For now it is sufficient. It is mainly for checking if the
     *       recursive will not last infinitely
     */
    static class DummyGetRulesCmdExecutor implements CmdExecutor {

        private static String iptablesEntries;

        private static boolean isIptableLoaded = false;

        @Override
        public String execute(String[] cmd) throws IOException {

            if (cmd[2].contains("iptables -D")) {

                Collection<String> alteredIptables = new ArrayList<String>();
                String[] tokens = cmd[2].split(" ");
                String chainNum = tokens[6];

                for (String iptablesEntry : iptablesEntries.split("\\r?\\n")) {
                    String[] entryTokens = iptablesEntry.split(" ");

                    if (!(entryTokens[0].equals(chainNum) && iptablesEntry.contains("easyTravel"))) {
                        alteredIptables.add(iptablesEntry + "\n");
                    }
                }

                iptablesEntries = alteredIptables.toString();

            } else {
                if (!isIptableLoaded) {
                    iptablesEntries = loadIptables();
                    isIptableLoaded = true;
                }
            }

            return iptablesEntries;
        }

        private String loadIptables() throws IOException {
            FileInputStream fileInputStream = new FileInputStream(IPTABLES_OUTPUT);
            return IOUtils.toString(fileInputStream);
        }

    }

}

