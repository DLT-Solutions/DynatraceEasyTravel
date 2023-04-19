package com.dynatrace.easytravel.pluginscheduler;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.dynatrace.easytravel.utils.TestEnvironment;

/**
 * cwpl-rorzecho
 */
public class JobFileProcessorTest {

	private static String JOB_FILE_NAME = "scenarios.xml";
	private static String TMP_JOB_FILE_NAME = "jobFile.xml";
	private JobFileProcessor jobFileProcessor;

	String testJobFileNamePath = new File(TestEnvironment.TEST_DATA_PATH, JOB_FILE_NAME).getPath();

	@Before
	public void testJobSchedulingDataCount() {
		File jobFile = new File(testJobFileNamePath);
		jobFileProcessor = new JobFileProcessor(jobFile);

		assertTrue("The <schedule> element if defined", jobFileProcessor.isJobSchedulingData());

		assertEquals("Only one Node should be available in NodeList", 1, jobFileProcessor.getJobSchedulingDataNodeList().getLength());

		assertEquals("There should be only one job-scheduling-data in scenarios.xml", jobFileProcessor.getJobSchedulingData().size(), 1);

		assertEquals("There should be for name elements", 4, jobFileProcessor.getJobFileDOM().getElementsByTagName("name").getLength());

	}

	@Test
	public void testGetJobSchedulingData() {
		assertTrue(jobFileProcessor.getJobFileDOM() instanceof Document);

		for (JobSchedulingData jobSchedulingData : jobFileProcessor.getJobSchedulingData()) {
			assertEquals("scanrios.xml file should be processed", jobSchedulingData.getFileName(), "scenarios.xml");
			assertEquals("There should not be NameSpacePrefix defined",jobSchedulingData.getNameSpacePrefix(), null);
		}
	}

	@Test
	public void testJobSchedulingDataWithNameSpace() throws IOException {
		String xmlString = "<job-scheduling-data xmlns=\"http://www.quartz-scheduler.org/xml/JobSchedulingData\">\n"+
				"        <schedule>\n" +
				"            <job>\n" +
				"                <name>DummyJob</name>\n" +
				"                <group>GROUP_1</group>\n" +
				"                <description>Run dummy job</description>\n" +
				"                <job-class>com.dynatrace.easytravel.pluginscheduler.jobs.DummyJob</job-class>\n" +
				"                <durability>true</durability>\n" +
				"                <recover>false</recover>\n" +
				"            </job>\n" +
				"            <trigger>\n" +
				"                <cron>\n" +
				"                    <name>DummyJobTrigger</name>\n" +
				"                    <job-name>DummyJob</job-name>\n" +
				"                    <job-group>GROUP_1</job-group>\n" +
				"                    <cron-expression>0/1 * * * * ?</cron-expression>\n" +
				"                </cron>\n" +
				"            </trigger>\n" +
				"         </schedule>\n" +
				"    </job-scheduling-data>";

		File jobFile = new File(TestEnvironment.TEST_DATA_PATH, TMP_JOB_FILE_NAME);

		FileUtils.writeStringToFile(jobFile, xmlString);

		jobFileProcessor = new JobFileProcessor(jobFile);

		assertEquals("ParentName schould be none", "none", jobFileProcessor.getJobSchedulingData().get(0).getParentName());

		FileUtils.deleteQuietly(jobFile);
	}

	@Test
	public void testJobSchedulingDataWithNoNameSpace() throws IOException {
		String xmlString = "<job-scheduling-data>\n"+
				"        <schedule>\n" +
				"            <job>\n" +
				"                <name>DummyJob</name>\n" +
				"                <group>GROUP_1</group>\n" +
				"                <description>Run dummy job</description>\n" +
				"                <job-class>com.dynatrace.easytravel.pluginscheduler.DummyJob</job-class>\n" +
				"                <durability>true</durability>\n" +
				"                <recover>false</recover>\n" +
				"            </job>\n" +
				"            <trigger>\n" +
				"                <cron>\n" +
				"                    <name>DummyJobTrigger</name>\n" +
				"                    <job-name>DummyJob</job-name>\n" +
				"                    <job-group>GROUP_1</job-group>\n" +
				"                    <cron-expression>0/1 * * * * ?</cron-expression>\n" +
				"                </cron>\n" +
				"            </trigger>\n" +
				"         </schedule>\n" +
				"    </job-scheduling-data>";

		File jobFile = new File(TestEnvironment.TEST_DATA_PATH, TMP_JOB_FILE_NAME);

		FileUtils.writeStringToFile(jobFile, xmlString);

		jobFileProcessor = new JobFileProcessor(jobFile);

		assertEquals("ParentName schould be none", "none", jobFileProcessor.getJobSchedulingData().get(0).getParentName());

		FileUtils.deleteQuietly(jobFile);
	}


	@Test
	public void testJobSchedulingDataScenarioParentName() throws IOException, SAXException {
		String xmlString = "<scenario compatibility=\"Both\" enabled=\"true\" title=\"Standard\">\n" +
				"    <job-scheduling-data>\n" +
				"        <schedule>\n" +
				"            <job>\n" +
				"                <name>DummyJob</name>\n" +
				"                <group>GROUP_1</group>\n" +
				"                <description>Run dummy job</description>\n" +
				"                <job-class>com.dynatrace.easytravel.pluginscheduler.DummyJob</job-class>\n" +
				"                <durability>true</durability>\n" +
				"                <recover>false</recover>\n" +
				"            </job>\n" +
				"            <trigger>\n" +
				"                <cron>\n" +
				"                    <name>DummyJobTrigger</name>\n" +
				"                    <job-name>DummyJob</job-name>\n" +
				"                    <job-group>GROUP_1</job-group>\n" +
				"                    <cron-expression>0/1 * * * * ?</cron-expression>\n" +
				"                </cron>\n" +
				"            </trigger>\n" +
				"         </schedule>\n" +
				"    </job-scheduling-data>\n" +
				"</scenario>";

		File jobFile = new File(TestEnvironment.TEST_DATA_PATH, TMP_JOB_FILE_NAME);

		FileUtils.writeStringToFile(jobFile, xmlString);

		JobFileProcessor jobFileProcessor = new JobFileProcessor(jobFile);

		assertEquals("ParentName schould be Standard", "Standard", jobFileProcessor.getJobSchedulingData().get(0).getParentName());

		FileUtils.deleteQuietly(jobFile);
	}

	@Test
	public void testJobSchedulingDataGroupParentName() throws IOException {
		String xmlString = "<group title=\"UEM\">\n" +
				"    <job-scheduling-data>\n" +
				"        <schedule>\n" +
				"            <job>\n" +
				"                <name>DummyJob</name>\n" +
				"                <group>GROUP_1</group>\n" +
				"                <description>Run dummy job</description>\n" +
				"                <job-class>com.dynatrace.easytravel.pluginscheduler.DummyJob</job-class>\n" +
				"                <durability>true</durability>\n" +
				"                <recover>false</recover>\n" +
				"            </job>\n" +
				"            <trigger>\n" +
				"                <cron>\n" +
				"                    <name>DummyJobTrigger</name>\n" +
				"                    <job-name>DummyJob</job-name>\n" +
				"                    <job-group>GROUP_1</job-group>\n" +
				"                    <cron-expression>0/1 * * * * ?</cron-expression>\n" +
				"                </cron>\n" +
				"            </trigger>\n" +
				"         </schedule>\n" +
				"    </job-scheduling-data>\n" +
				"</group>";

		File jobFile = new File(TestEnvironment.TEST_DATA_PATH, TMP_JOB_FILE_NAME);

		FileUtils.writeStringToFile(jobFile, xmlString);

		jobFileProcessor = new JobFileProcessor(jobFile);

		assertEquals("ParentName should be UEM", "UEM", jobFileProcessor.getJobSchedulingData().get(0).getParentName());

		FileUtils.deleteQuietly(jobFile);
	}

	@Test
	public void testJobSchedulingDataConfigurationParent() throws IOException {
		String xmlString = "<configuration defaulthash=\"a294d5c9001ad1e16c15ecdf2499ff71\">\n" +
				"    <job-scheduling-data>\n" +
				"        <schedule>\n" +
				"            <job>\n" +
				"                <name>DummyJob</name>\n" +
				"                <group>GROUP_1</group>\n" +
				"                <description>Run dummy job</description>\n" +
				"                <job-class>com.dynatrace.easytravel.pluginscheduler.DummyJob</job-class>\n" +
				"                <durability>true</durability>\n" +
				"                <recover>false</recover>\n" +
				"            </job>\n" +
				"            <trigger>\n" +
				"                <cron>\n" +
				"                    <name>DummyJobTrigger</name>\n" +
				"                    <job-name>DummyJob</job-name>\n" +
				"                    <job-group>GROUP_1</job-group>\n" +
				"                    <cron-expression>0/1 * * * * ?</cron-expression>\n" +
				"                </cron>\n" +
				"            </trigger>\n" +
				"         </schedule>\n" +
				"    </job-scheduling-data>\n" +
				"</configuration>";

		File jobFile = new File(TestEnvironment.TEST_DATA_PATH, TMP_JOB_FILE_NAME);

		FileUtils.writeStringToFile(jobFile, xmlString);

		jobFileProcessor = new JobFileProcessor(jobFile);

		assertEquals("ParentName should be configuration", "configuration", jobFileProcessor.getJobSchedulingData().get(0).getParentName());

		FileUtils.deleteQuietly(jobFile);
	}

	@Test
	public void testJobSchedulingDataManyParents() throws IOException {
		String xmlString = "<configuration defaulthash=\"a294d5c9001ad1e16c15ecdf2499ff71\">\n" +
				"    <job-scheduling-data>\n" +
				"        <schedule>\n" +
				"            <job>\n" +
				"                <name>DummyJob</name>\n" +
				"                <group>GROUP_1</group>\n" +
				"                <description>Run dummy job</description>\n" +
				"                <job-class>com.dynatrace.easytravel.pluginscheduler.DummyJob</job-class>\n" +
				"                <durability>true</durability>\n" +
				"                <recover>false</recover>\n" +
				"            </job>\n" +
				"            <trigger>\n" +
				"                <cron>\n" +
				"                    <name>DummyJobTrigger</name>\n" +
				"                    <job-name>DummyJob</job-name>\n" +
				"                    <job-group>GROUP_1</job-group>\n" +
				"                    <cron-expression>0/1 * * * * ?</cron-expression>\n" +
				"                </cron>\n" +
				"            </trigger>\n" +
				"         </schedule>\n" +
				"    </job-scheduling-data>\n" +
				"<group title=\"UEM\">\n" +
				"    <job-scheduling-data>\n" +
				"        <schedule>\n" +
				"            <job>\n" +
				"                <name>DummyJob</name>\n" +
				"                <group>GROUP_1</group>\n" +
				"                <description>Run dummy job</description>\n" +
				"                <job-class>com.dynatrace.easytravel.pluginscheduler.DummyJob</job-class>\n" +
				"                <durability>true</durability>\n" +
				"                <recover>false</recover>\n" +
				"            </job>\n" +
				"            <trigger>\n" +
				"                <cron>\n" +
				"                    <name>DummyJobTrigger</name>\n" +
				"                    <job-name>DummyJob</job-name>\n" +
				"                    <job-group>GROUP_1</job-group>\n" +
				"                    <cron-expression>0/1 * * * * ?</cron-expression>\n" +
				"                </cron>\n" +
				"            </trigger>\n" +
				"         </schedule>\n" +
				"    </job-scheduling-data>\n" +
				"<scenario compatibility=\"Both\" enabled=\"true\" title=\"Standard\">\n" +
				"    <job-scheduling-data>\n" +
				"        <schedule>\n" +
				"            <job>\n" +
				"                <name>DummyJob</name>\n" +
				"                <group>GROUP_1</group>\n" +
				"                <description>Run dummy job</description>\n" +
				"                <job-class>com.dynatrace.easytravel.pluginscheduler.DummyJob</job-class>\n" +
				"                <durability>true</durability>\n" +
				"                <recover>false</recover>\n" +
				"            </job>\n" +
				"            <trigger>\n" +
				"                <cron>\n" +
				"                    <name>DummyJobTrigger</name>\n" +
				"                    <job-name>DummyJob</job-name>\n" +
				"                    <job-group>GROUP_1</job-group>\n" +
				"                    <cron-expression>0/1 * * * * ?</cron-expression>\n" +
				"                </cron>\n" +
				"            </trigger>\n" +
				"         </schedule>\n" +
				"    </job-scheduling-data>\n" +
				"</scenario>\n" +
				"</group>" +
				"</configuration>";

		File jobFile = new File(TestEnvironment.TEST_DATA_PATH, TMP_JOB_FILE_NAME);

		FileUtils.writeStringToFile(jobFile, xmlString);

		jobFileProcessor = new JobFileProcessor(jobFile);

		assertEquals("There should be two job-scheduling-data definitions", 3, jobFileProcessor.getJobSchedulingData().size());

		assertEquals("configuration", jobFileProcessor.getJobSchedulingData().get(0).getParentName());
		assertEquals("UEM", jobFileProcessor.getJobSchedulingData().get(1).getParentName());
		assertEquals("Standard", jobFileProcessor.getJobSchedulingData().get(2).getParentName());

		FileUtils.deleteQuietly(jobFile);
	}

}
