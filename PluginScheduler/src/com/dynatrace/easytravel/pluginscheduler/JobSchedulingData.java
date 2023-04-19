package com.dynatrace.easytravel.pluginscheduler;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * Main class for parsing job-scheduling-data from {@link JobSchedulingDataProcessorPlugin.JobFile}
 * JobFile can contain multiple <job-scheduling-data> definitions.
 *
 * cwpl-rorzecho
 */
public class JobSchedulingData {

	private final DocumentBuilder documentBuilder;
	private Document jobSchedulingDataDOM;
	private Node jobSchedulingDataNode;
	private final File jobFile;

	public JobSchedulingData(File jobFile, Node jobSchedulingDataNode, DocumentBuilder documentBuilder) {
		this.jobFile = jobFile;
		this.jobSchedulingDataNode = jobSchedulingDataNode;
		this.documentBuilder = documentBuilder;

		createJobSchedulingDataDOM();
	}

	public JobSchedulingData(InputStream is, DocumentBuilder documentBuilder) throws IOException, SAXException {
		this.documentBuilder = documentBuilder;
		createJobSchedulingDataDOM(is);
		createJobSchedulingDataNode();
		jobFile = null;
	}

	/**
	 * Get parent name for job-scheduling-data.
	 * Options for parentName:
	 * - parent node name if there is no attribute named "title"
	 * - if parent node contains attribute "title" - title name
	 * - otherwise none
	 *
	 * @return job-scheduling-data parent name
	 */
	public String getParentName() {
		Node parentNode = jobSchedulingDataNode.getParentNode();
		if (parentNode != null && parentNode.hasAttributes()) {
			Node attrNode = parentNode.getAttributes().getNamedItem("title");
			return attrNode == null ? parentNode.getNodeName() : attrNode.getNodeValue();
		}
		return "none";
	}

	/**
	 * Get JobSchedulingData as XML
	 * @return
	 */
	public String getJobSchedulingXMLData() {
		setJobSchedulingDataNS(jobSchedulingDataDOM);
		try {
			return documentToXML(jobSchedulingDataDOM);
		} catch (TransformerException e) {
			throw new RuntimeException("Cannot transform DOM to string", e);
		}
	}

	/**
	 * Create DOM Document from Node
	 */
	private void createJobSchedulingDataDOM() {
		jobSchedulingDataDOM = nodeToDOM(jobSchedulingDataNode);
	}

	/**
	 * Create DOM Document from InputStream
	 * @param InputStream
	 * @throws IOException
	 * @throws SAXException
	 */
	private void createJobSchedulingDataDOM(InputStream is) throws IOException, SAXException {
		jobSchedulingDataDOM = inputStreamToDOM(is);
	}

	/**
	 * Create Node from DOM Document
	 */
	private void createJobSchedulingDataNode() {
		jobSchedulingDataNode = (Node) jobSchedulingDataDOM;
	}

	/**
	 * Get DOM Document with job-scheduling-data
	 * @return
	 */
	public Document getJobSchedulingDataDOM() {
		return jobSchedulingDataDOM;
	}

	/**
	 * Get job-scheduling-data Node
	 * @return
	 */
	public Node getJobSchedulingDataNode() {
		return jobSchedulingDataNode;
	}

	/**
	 * Convert JobSchedulingData to ByteArrayInputStream
	 * @return ByteArrayInputStream
	 */
	public ByteArrayInputStream getJobSchedulingDataStream() {
		return new ByteArrayInputStream(getJobSchedulingXMLData().getBytes());
	}

	/**
	 * Transform DOM document to string
	 * @param document
	 * @return
	 * @throws javax.xml.transform.TransformerConfigurationException
	 */
	private String documentToXML(Document document) throws TransformerException {
		Transformer tf = TransformerFactory.newInstance().newTransformer();
		tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		tf.setOutputProperty(OutputKeys.METHOD, "xml");
		Writer out = new StringWriter();
		tf.transform(new DOMSource(document), new StreamResult(out));
		return out.toString();
	}

	/**
	 * Set xmlns attribute to job-scheduling-data element if is not specified
	 * @param document
	 */
	private void setJobSchedulingDataNS (Document document) {
		Element jobSchedulingData = document.getDocumentElement();
		String attrName = "xmlns";

		if (!attrExists(jobSchedulingData, attrName)) {
			Attr attr = document.createAttribute(attrName);
			attr.setNodeValue("http://www.quartz-scheduler.org/xml/JobSchedulingData");
			jobSchedulingData.setAttributeNode(attr);
		}
	}

	/**
	 * Check if specified attribute name exists in Element
	 * @param element
	 * @param attrName
	 * @return
	 */
	private boolean attrExists(Element element, String attrName) {
		Node attrNode = null;
		if (element.hasAttributes()) {
			NamedNodeMap attr = element.getAttributes();
			attrNode = attr.getNamedItem(attrName);
		}
		return attrNode != null ? true : false;
	}

	/**
	 * Convert Node to DOM Document, used to change JobSchedulingData Node to Document
	 *
	 * @param node
	 * @return Document
	 */
	private Document nodeToDOM(Node node) {
		Document document = documentBuilder.newDocument();
		Node importedNode = document.importNode(node, true);
		document.appendChild(importedNode);
		return document;
	}

	/**
	 * Convert InputStream to DOM Document
	 *
	 * @param InputStream
	 * @return Document
	 * @throws IOException
	 * @throws SAXException
	 */
	private Document inputStreamToDOM(InputStream is) throws IOException, SAXException {
		return documentBuilder.parse(is);
	}

	/**
	 * Return NameSpace prefix
	 * @return prefix
	 */
	public String getNameSpacePrefix() {
		return jobSchedulingDataDOM.getDocumentElement().getPrefix();
	}

	/**
	 * The main criteria to determine if there are any jobs definitions to
	 * execute processJobSchedulingData method from {@link JobSchedulingDataProcessorPlugin}
	 *
	 * If the JobFile contains <schedule></schedule> elements
	 *
	 * @return
	 */
	public boolean containsScheduleElement() {
		NodeList schedule = jobSchedulingDataDOM.getDocumentElement().getElementsByTagName("schedule");
		return schedule.getLength() != 0 ? true : false;
	}

	public String getFileName() {
		return jobFile.getName();
	}

	public File getJobFile() {
		return jobFile;
	}
}

