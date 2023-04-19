package com.dynatrace.easytravel.launcher.process;

import static com.dynatrace.easytravel.util.TextUtils.merge;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.procedures.CassandraProcedure;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.utils.TestEnvironment;
import com.google.common.base.Joiner;


public class JavaProcessTest {

	@Test
	public void testCreateAgentStringWithDefaultServer() throws ConfigurationException {
    	DtVersionDetector.enforceInstallationType(InstallationType.Classic);
    	assertTrue(DtVersionDetector.isClassic());

    	EasyTravelConfig config = EasyTravelConfig.read();
        config.dtServer = "defaultHost";
        config.dtServerPort = "1111";

        DtAgentConfig dtAgentConfig = new DtAgentConfig("agentName", Joiner.on(File.separator).join(
				TestEnvironment.TEST_DATA_PATH, "agent", "dtagent.dll"), new String[] {}, null);
		String createAgentString = JavaProcess.createAgentString(dtAgentConfig);

		assertThat("Agent string should contain the default server address", createAgentString,
				containsString("server=defaultHost:1111"));
	}

	@Test
	public void testCreateAgentStringWithCustomServer() throws ConfigurationException {
    	DtVersionDetector.enforceInstallationType(InstallationType.Classic);
    	assertTrue(DtVersionDetector.isClassic());

    	EasyTravelConfig config = EasyTravelConfig.read();
        config.dtServer = "defaultHost";
        config.dtServerPort = "1111";

		DtAgentConfig dtAgentConfig = new DtAgentConfig("agentName", Joiner.on(File.separator).join(
				TestEnvironment.TEST_DATA_PATH, "agent", "dtagent.dll"), new String[] { "server=customHost:7777" }, null);
		String createAgentString = JavaProcess.createAgentString(dtAgentConfig);

		assertThat(merge("Agent string should contain the custom server address: ''{0}''", createAgentString), createAgentString,
				containsString("server=customHost:7777"));
		assertThat(merge("Agent string should not contain the default server address", createAgentString), createAgentString,
				not(containsString("server=defaultHost:1111")));
	}


	private static boolean runJavaProcess = false;

	@Ignore("Just used locally")
	@Test
	public void testRunCassandra() throws Exception {
		if(runJavaProcess) {
			JavaProcess process = new JavaProcess(new File("../Distribution/dist/com.dynatrace.easytravel.cassandra.jar"));
			process.setJavaArguments(new String[] {"-Xms234m", "-Dcassandra.config=file:////home/dominikstadler/.dynaTrace/easyTravel 2.0.0/easyTravel/somethingelse"});
			process.start();

			while(!process.hasResult()) {
				System.out.println("Waiting for process");
				Thread.sleep(10000);
			}
		} else {
			CassandraProcedure procedure = new CassandraProcedure(new DefaultProcedureMapping(Constants.Procedures.CASSANDRA_ID));
			procedure.run();

			while(procedure.isRunning()) {
				System.out.println("Waiting for process");
				Thread.sleep(10000);
			}

			procedure.stop();
		}
	}
}
