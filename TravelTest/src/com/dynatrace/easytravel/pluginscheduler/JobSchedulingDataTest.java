package com.dynatrace.easytravel.pluginscheduler;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.dynatrace.easytravel.utils.TestEnvironment;

/**
 * cwpl-rorzecho
 */
public class JobSchedulingDataTest {

	private static String JOB_FILE_NAME = "scenarios.xml";
	private static String testJobFileNamePath = new File(TestEnvironment.TEST_DATA_PATH, JOB_FILE_NAME).getPath();

	private DocumentBuilder documentBuilder;
	private File jobFile;

	@Before
	public void setUp() throws ParserConfigurationException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setNamespaceAware(true);
		documentBuilder = docBuilderFactory.newDocumentBuilder();
		jobFile = new File(testJobFileNamePath);
		assertNotNull(jobFile);
	}

	@Test
	public void testGetJobScheduilngData() throws IOException, SAXException {
		JobFileProcessor jobFileProcessor = new JobFileProcessor(jobFile);

		NodeList nodeList = jobFileProcessor.getJobSchedulingDataNodeList();
		assertEquals("There should be one job-scheduling-data Node", nodeList.getLength(), 1);

		Node jobSchedulingDataNode = nodeList.item(0);
		assertEquals(jobSchedulingDataNode.getNodeName(), "job-scheduling-data");

		JobSchedulingData jobSchedulingData = jobFileProcessor.getJobSchedulingData().get(0);

		assertEquals("Scheduling data defined directly under configuration Node ", "configuration", jobSchedulingData.getParentName());

		assertTrue("JobFile shoul contain <schedule> element", jobSchedulingData.containsScheduleElement());

		assertEquals("No NameSpace prefix should be defined", jobSchedulingData.getNameSpacePrefix(), null);

		assertEquals("scenarios.xml should be processed", JOB_FILE_NAME, jobSchedulingData.getFileName());

		assertTrue(jobSchedulingData.getJobFile() instanceof File);

		assertTrue(jobSchedulingData.getJobSchedulingDataDOM() instanceof Document);

		String jobSchedulingXMLData = jobSchedulingData.getJobSchedulingXMLData();

		assertTrue(jobSchedulingXMLData.contains("<job-scheduling-data xmlns=\"http://www.quartz-scheduler.org/xml/JobSchedulingData\">"));
		assertTrue(jobSchedulingXMLData.contains("<schedule>"));
		assertTrue(jobSchedulingXMLData.contains("<job>"));
		assertTrue(jobSchedulingXMLData.contains("<name>DummyJob</name>"));
		assertTrue(jobSchedulingXMLData.contains("<group>DummyGroup</group>"));
		assertTrue(jobSchedulingXMLData.contains("<description>Run dummy job</description>"));
		assertTrue(jobSchedulingXMLData.contains("<job-class>com.dynatrace.easytravel.pluginscheduler.DummyJob</job-class>"));
		assertTrue(jobSchedulingXMLData.contains("<durability>true</durability>"));
		assertTrue(jobSchedulingXMLData.contains("<recover>false</recover>"));
		assertTrue(jobSchedulingXMLData.contains("</job>"));
		assertTrue(jobSchedulingXMLData.contains("<job>"));
		assertTrue(jobSchedulingXMLData.contains("<name>ScenarioJob</name>"));
		assertTrue(jobSchedulingXMLData.contains("<group>Scenario</group>"));
		assertTrue(jobSchedulingXMLData.contains("<description>Scenario job definition</description>"));
		assertTrue(jobSchedulingXMLData.contains("<job-class>com.dynatrace.easytravel.pluginscheduler.DummyJob</job-class>"));
		assertTrue(jobSchedulingXMLData.contains("<durability>true</durability>"));
		assertTrue(jobSchedulingXMLData.contains("<recover>false</recover>"));
		assertTrue(jobSchedulingXMLData.contains("</job>"));
		assertTrue(jobSchedulingXMLData.contains("<trigger>"));
		assertTrue(jobSchedulingXMLData.contains("<cron>"));
		assertTrue(jobSchedulingXMLData.contains("<name>DummyJobTrigger</name>"));
		assertTrue(jobSchedulingXMLData.contains("<job-name>DummyJob</job-name>"));
		assertTrue(jobSchedulingXMLData.contains("<job-group>DummyGroup</job-group>"));
		assertTrue(jobSchedulingXMLData.contains("<cron-expression>0/1 * * * * ?</cron-expression>"));
		assertTrue(jobSchedulingXMLData.contains("</cron>"));
		assertTrue(jobSchedulingXMLData.contains("</trigger>"));
		assertTrue(jobSchedulingXMLData.contains("<trigger>"));
		assertTrue(jobSchedulingXMLData.contains("<cron>"));
		assertTrue(jobSchedulingXMLData.contains("<name>ScenarioJobTrigger</name>"));
		assertTrue(jobSchedulingXMLData.contains("<job-name>ScenarioJob</job-name>"));
		assertTrue(jobSchedulingXMLData.contains("<job-group>Scenario</job-group>"));
		assertTrue(jobSchedulingXMLData.contains("<cron-expression>0/2 * * * * ?</cron-expression>"));
		assertTrue(jobSchedulingXMLData.contains("</cron>"));
		assertTrue(jobSchedulingXMLData.contains("</trigger>"));
		assertTrue(jobSchedulingXMLData.contains("</schedule>"));
		assertTrue(jobSchedulingXMLData.contains("</job-scheduling-data>"));
	}

	@Test
	public void createJobSchedulingData() throws IOException, SAXException {
		String xmlString = "<job-scheduling-data xmlns=\"http://www.quartz-scheduler.org/xml/JobSchedulingData\">\n" +
				"        <schedule>\n" +
				"            <job>\n" +
				"                <name>DummyJob</name>\n" +
				"                <group>DummyGroup</group>\n" +
				"                <description>Run dummy job</description>\n" +
				"                <job-class>com.dynatrace.easytravel.pluginscheduler.DummyJob</job-class>\n" +
				"                <durability>true</durability>\n" +
				"                <recover>false</recover>\n" +
				"            </job>\n" +
				"            <job>\n" +
				"                <name>ScenarioJob</name>\n" +
				"                <group>Scenario</group>\n" +
				"                <description>Scenario job definition</description>\n" +
				"                <job-class>com.dynatrace.easytravel.pluginscheduler.DummyJob</job-class>\n" +
				"                <durability>true</durability>\n" +
				"                <recover>false</recover>\n" +
				"            </job>\n" +
				"            <trigger>\n" +
				"                <cron>\n" +
				"                    <name>DummyJobTrigger</name>\n" +
				"                    <job-name>DummyJob</job-name>\n" +
				"                    <job-group>DummyGroup</job-group>\n" +
				"                    <cron-expression>0/1 * * * * ?</cron-expression>\n" +
				"                </cron>\n" +
				"            </trigger>\n" +
				"            <trigger>\n" +
				"                <cron>\n" +
				"                    <name>ScenarioJobTrigger</name>\n" +
				"                    <job-name>ScenarioJob</job-name>\n" +
				"                    <job-group>Scenario</job-group>\n" +
				"                    <cron-expression>0/2 * * * * ?</cron-expression>\n" +
				"                </cron>\n" +
				"            </trigger>\n" +
				"        </schedule>\n" +
				"    </job-scheduling-data>";


		ByteArrayInputStream jobSchedulingDataIS = new ByteArrayInputStream(xmlString.getBytes());

		assertNotNull(jobSchedulingDataIS);

		JobSchedulingData jobSchedulingData = new JobSchedulingData(jobSchedulingDataIS, documentBuilder);

		assertNotNull(jobSchedulingData);

		assertEquals("Beceause there is no parent element parent name should be none ", jobSchedulingData.getParentName(), "none");

		String jobSchedulingXMLData = jobSchedulingData.getJobSchedulingXMLData();
		assertNotNull(jobSchedulingXMLData);

		assertEquals("XML string should be the same", adjustXMLStrings(xmlString), adjustXMLStrings(jobSchedulingData.getJobSchedulingXMLData()));

		Node jobSchedulingDataNode = jobSchedulingData.getJobSchedulingDataNode();

		assertNotNull(jobSchedulingDataNode);

		System.out.println(jobSchedulingData.getParentName());

		assertEquals("none", jobSchedulingData.getParentName());

		assertEquals(jobSchedulingDataNode.getFirstChild().getNodeName(), "job-scheduling-data");

	}

	/**
	 * Helper method to fit xml Strings
	 * @param xmlString
	 * @return
	 */
	private String adjustXMLStrings(String xmlString) {
		return xmlString.replace("\n", "").replace("\r", "");
	}
}

