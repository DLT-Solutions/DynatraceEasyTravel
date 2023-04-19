package com.dynatrace.easytravel.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.dynatrace.easytravel.config.Directories;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests for PluginNotificationConfigFileGenerator
 *
 * @author tomasz.wieremjewicz
 * @date 20 lis 2017
 *
 */
public class PluginNotificationConfigFileGeneratorTest {
	private final static String FILE_NAME="templateFileForTests.json";
	private final static String ENTITY="testEntity";
	private final static String SOURCE="easyTravel";
	private final static String TITLE="_pluginName_";
	private final static String VERSION="version";
	private final static String DEPLOYMENTPROJECT="testDeploymentProject";
	private final static String CIBACKLINK="testCiBackLink";
	private final static String REMEDIATIONACTION="testRemediationAction";

	@Test
	public void checkFileCreation() {
		PluginNotificationTemplate[] templates = new PluginNotificationTemplate[2];
		templates[0] = new PluginNotificationTemplate();
		templates[0].pluginNames = new String[] {"PluginName1"};
		templates[0].entityIds = new String[] {ENTITY};

		templates[1] = new PluginNotificationTemplate();
		templates[1].pluginNames = new String[] {"PluginName2"};
		templates[1].entityIds = new String[] {ENTITY};

		templates[0].title = templates[1].title = TITLE;
		templates[0].source = templates[1].source = SOURCE;
		templates[0].version = templates[1].version = VERSION;
		templates[0].deploymentProject = templates[1].deploymentProject = DEPLOYMENTPROJECT;
		templates[0].ciBackLink = templates[1].ciBackLink = CIBACKLINK;
		templates[0].remediationAction = templates[1].remediationAction = REMEDIATIONACTION;

		PluginNotificationConfigFileGenerator generator = new PluginNotificationConfigFileGenerator();
		generator.generateConfigFile(templates, FILE_NAME);

		File f = new File(Directories.getConfigDir(), FILE_NAME);
		Assert.assertTrue(f.exists());

		ObjectMapper mapper = new ObjectMapper();
		try {
			templates = mapper.readValue(f, PluginNotificationTemplate[].class);
			assertTrue(templates != null);
			assertTrue(templates.length == 2);

			for(PluginNotificationTemplate element : templates) {
				assertTrue(element.isTemplateDefinitionComplete());
				assertEquals(ENTITY, element.entityIds[0]);
				assertEquals(TITLE, element.title);
				assertEquals(SOURCE, element.source);
				assertEquals(VERSION, element.version);
				assertEquals(DEPLOYMENTPROJECT, element.deploymentProject);
				assertEquals(CIBACKLINK, element.ciBackLink);
				assertEquals(REMEDIATIONACTION, element.remediationAction);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void createSituationWhereFileExists() {
		PluginNotificationTemplate[] templates = new PluginNotificationTemplate[1];
		templates[0] = new PluginNotificationTemplate();
		PluginNotificationConfigFileGenerator generator = new PluginNotificationConfigFileGenerator();
		generator.generateConfigFile(templates, FILE_NAME);
		generator.generateConfigFile(templates, FILE_NAME);
	}

	@After
	public void cleanFileAfterTest() {
		File f = new File(Directories.getConfigDir(), FILE_NAME);
		if (f.exists()) {
			f.delete();
			Assert.assertFalse(f.exists());
		}
	}
}
