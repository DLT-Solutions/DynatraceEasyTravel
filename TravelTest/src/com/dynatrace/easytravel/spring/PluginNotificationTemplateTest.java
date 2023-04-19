package com.dynatrace.easytravel.spring;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for PluginNotificationTemplate
 *
 * @author tomasz.wieremjewicz
 * @date 20 lis 2017
 *
 */
public class PluginNotificationTemplateTest {

	@Test
	public void isTemplateDefinitionCompleteTest() {
		PluginNotificationTemplate template = new PluginNotificationTemplate();
		assertFalse(template.isTemplateDefinitionComplete());

		template.entityIds = new String[] {"Test"};
		template.pluginNames = new String[] {"Test"};
		template.source = template.title = template.version = template.deploymentProject = template.ciBackLink = template.remediationAction = "test";
		assertTrue(template.isTemplateDefinitionComplete());

		//entityIds
		template.entityIds = new String[] {};
		assertFalse(template.isTemplateDefinitionComplete());
		template.entityIds = null;
		assertFalse(template.isTemplateDefinitionComplete());
		template.entityIds = new String[] {"Test"};
		assertTrue(template.isTemplateDefinitionComplete());

		//pluginNames
		template.pluginNames = new String[] {};
		assertFalse(template.isTemplateDefinitionComplete());
		template.pluginNames = null;
		assertFalse(template.isTemplateDefinitionComplete());
		template.pluginNames = new String[] {"Test"};
		assertTrue(template.isTemplateDefinitionComplete());

		//source
		template.source = null;
		assertFalse(template.isTemplateDefinitionComplete());
		template.source = "test";
		assertTrue(template.isTemplateDefinitionComplete());

		//title
		template.title = null;
		assertFalse(template.isTemplateDefinitionComplete());
		template.title = "test";
		assertTrue(template.isTemplateDefinitionComplete());

		//version
		template.version = null;
		assertFalse(template.isTemplateDefinitionComplete());
		template.version = "test";
		assertTrue(template.isTemplateDefinitionComplete());
	}
}
