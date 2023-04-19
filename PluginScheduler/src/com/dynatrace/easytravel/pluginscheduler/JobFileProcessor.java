package com.dynatrace.easytravel.pluginscheduler;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for parsing file with Quartz jobs definitions.
 *
 * {@link JobSchedulingDataProcessorPlugin.JobFile} should contain quartz-scheduling-data elements compatible with
 * Quartz Scheduler XML Schema http://www.quartz-scheduler.org/xml/job_scheduling_data_2_0.xsd
 *
 * cwpl-rorzecho
 */
public class JobFileProcessor {
	private static final Logger LOGGER = LoggerFactory.make();

	private static final String QUARTZ_ROOT_ELEMENT = "job-scheduling-data";

	private File jobFile;
	private Document jobFileDOM = null;

	private DocumentBuilder documentBuilder = null;

	private NodeList jobSchedulingDataNodeList;

	private final List<JobSchedulingData> jobSchedulingData = new ArrayList<JobSchedulingData>();

	public JobFileProcessor(File jobFile) {
		this.jobFile = jobFile;
		initJobFileParser();
		createJobFileDOM(jobFile);
		createJobSchedulingData();
	}

	/**
	 * Ceate DOM Document for specified JobFile
	 */
	private void createJobFileDOM(File jobFile) {
		try {
			jobFileDOM = documentBuilder.parse(jobFile);
		} catch (Exception e) {
			LOGGER.error(TextUtils.merge("Cannot create DOM document for file {0}", jobFile.getName()), e);
		}
	}

	/**
	 * Initialize JobFileParser
	 */
	private void initJobFileParser() {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setNamespaceAware(false);
		try {
			documentBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			LOGGER.error("Cannot create document builder", e);
		}
	}

	/**
	 * Get JobFile DOM Document
	 * @return Document
	 */
	public Document getJobFileDOM() {
		return jobFileDOM;
	}

	/**
	 * Get all JobSchedulingData Nodes based on job-scheduling-data tag name
	 * @return NodeList with JobSchedulingData Nodes
	 */
	public NodeList getJobSchedulingDataNodeList() {
		return jobSchedulingDataNodeList;
	}

	/**
	 * Create List of JobSchedulingData objects
	 */
	private void createJobSchedulingData () {
		jobSchedulingDataNodeList = jobFileDOM.getElementsByTagName(QUARTZ_ROOT_ELEMENT);
		if (jobSchedulingDataNodeList.getLength() != 0) {
			for (int i = 0; i < jobSchedulingDataNodeList.getLength(); i++) {
				final Node jobSchedulingDataNode = jobSchedulingDataNodeList.item(i);
				jobSchedulingData.add(new JobSchedulingData(getJobFile(), jobSchedulingDataNode, documentBuilder));
			}
		}
	}

	/**
	 * Get JobSchedulingData List
	 *
	 * @return List<JobSchedulingData>
	 */
	public List<JobSchedulingData> getJobSchedulingData() {
		return this.jobSchedulingData;
	}

	/**
	 * Check if there are JobSchedulingData
	 * @return
	 */
	public boolean isJobSchedulingData() {
		return (jobSchedulingData.size() != 0) ? true : false;
	}

	public String getFileName() {
		return jobFile.getName();
	}

	public File getJobFile() {
		return jobFile;
	}

	public void setJobFile(File jobFile) {
		this.jobFile = jobFile;
	}
}
