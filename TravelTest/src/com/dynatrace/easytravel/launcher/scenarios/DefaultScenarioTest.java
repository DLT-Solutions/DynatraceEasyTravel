/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: DefaultScenarioTest.java
 * @date: 01.07.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.launcher.scenarios;

import static com.dynatrace.easytravel.launcher.misc.Constants.Misc.AUTOMATIC_PLUGIN_ON_OFF_DISABLED;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.tree.ConfigurationNode;
import org.easymock.EasyMock;
import org.junit.Test;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.launcher.config.ConfigurationReader;
import com.dynatrace.easytravel.launcher.config.NodeFactory;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.Constants.ConfigurationXml;
import com.dynatrace.easytravel.utils.TestHelpers;


/**
 *
 * @author dominik.stadler
 */
public class DefaultScenarioTest {

	/**
	 * Test method for {@link com.dynatrace.easytravel.launcher.scenarios.DefaultScenario#DefaultScenario()}.
	 */
	@Test
	public void testDefaultScenario() {
		DefaultScenario obj = new DefaultScenario();
		assertTrue(obj.isEnabled());

		obj = new DefaultScenario("title", "desc", false, InstallationType.Both);
		assertFalse(obj.isEnabled());

		obj.setTitle("sometitle");
		assertEquals("sometitle", obj.getTitle());
		obj.setDescription("somedescription");
		assertEquals("somedescription", obj.getDescription());
		assertNull(obj.getGroup());

		obj.setGroup("somegroup");
		assertEquals("somegroup", obj.getGroup());

		assertFalse(obj.isEnabled());
		obj.setEnabled(true);
		assertTrue(obj.isEnabled());
	}

	@Test
	public void testNoScenarioSettings() throws ConfigurationException {
		DefaultScenario obj = prepareSettingsTest("mytype", false, InstallationType.Both.toString());
		assertEquals(1, obj.getProcedureMappings(InstallationType.Classic).size());
		assertEquals(0, obj.getProcedureMappings(InstallationType.Classic).get(0).getCustomSettings().size());

		assertEquals("Had: " + obj.getCustomSettings(),
				0, obj.getCustomSettings().size());

		DefaultScenario copy = obj.copy();
		assertEquals("Had: " + copy.getCustomSettings(),
				0, copy.getCustomSettings().size());
	}

	@Test
	public void testScenarioSettings() throws ConfigurationException {
		DefaultScenario obj = prepareSettingsTest("mytype", true, InstallationType.Both.toString());
		assertEquals(1, obj.getProcedureMappings(InstallationType.Classic).size());
		assertEquals(0, obj.getProcedureMappings(InstallationType.Classic).get(0).getCustomSettings().size());

		assertEquals("Had: " + obj.getCustomSettings(),
				1, obj.getCustomSettings().size());

		DefaultScenario copy = obj.copy();
		assertEquals("Had: " + copy.getCustomSettings(),
				1, copy.getCustomSettings().size());
	}

	@Test
	public void testCustomSettings() throws ConfigurationException {
		DefaultScenario obj = prepareSettingsTest(Scenario.TYPE_SCENARIO_PROCEDURE_CONFIG, false, InstallationType.Both.toString());
		assertEquals(1, obj.getProcedureMappings(InstallationType.Classic).size());
		assertEquals(1, obj.getProcedureMappings(InstallationType.Classic).get(0).getCustomSettings().size());

		assertEquals("Had: " + obj.getCustomSettings(),
				1, obj.getCustomSettings().size());

		DefaultScenario copy = obj.copy();
		assertEquals("Had: " + copy.getCustomSettings(),
				1, copy.getCustomSettings().size());
	}

	@Test
	public void testScenarioSettingsAndCustomSettings() throws ConfigurationException {
		DefaultScenario obj = prepareSettingsTest(Scenario.TYPE_SCENARIO_PROCEDURE_CONFIG, true, InstallationType.Both.toString());
		assertEquals(1, obj.getProcedureMappings(InstallationType.Classic).size());
		assertEquals(1, obj.getProcedureMappings(InstallationType.Classic).get(0).getCustomSettings().size());

		assertEquals("Had: " + obj.getCustomSettings(),
				2, obj.getCustomSettings().size());

		DefaultScenario copy = obj.copy();
		assertEquals("Had: " + copy.getCustomSettings(),
				2, copy.getCustomSettings().size());
	}

	private DefaultScenario prepareSettingsTest(String type, boolean scenarioSettings, String compatibility) throws ConfigurationException {
		DefaultScenario obj = new DefaultScenario("title", "desc", false, InstallationType.Both);
		assertFalse(obj.isEnabled());


		ConfigurationNode node = EasyMock.createStrictMock(ConfigurationNode.class);
		ConfigurationReader reader = EasyMock.createStrictMock(ConfigurationReader.class);

		// read of Scenario
		EasyMock.expect(reader.readStringAttribute(node, ConfigurationXml.ATTRIBUTE_TITLE)).andReturn("test");
		EasyMock.expect(reader.readTextChild(node, "description")).andReturn("desc");
		EasyMock.expect(reader.readBooleanAttribute(node, ConfigurationXml.ATTRIBUTE_ENABLED)).andReturn(true);
		EasyMock.expect(reader.readOptionalStringAttribute(node, ConfigurationXml.ATTRIBUTE_COMPATIBILITY)).andReturn(compatibility);
		EasyMock.expect(reader.getChildren(node, "procedure")).andReturn(Collections.singletonList(node));

		EasyMock.expect(reader.readStringAttribute(node, ConfigurationXml.ATTRIBUTE_ID)).andReturn("newid");
		EasyMock.expect(reader.readOptionalStringAttribute(node, ConfigurationXml.ATTRIBUTE_HOST)).andReturn(null);
		EasyMock.expect(reader.readOptionalStringAttribute(node, ConfigurationXml.ATTRIBUTE_APM_TENANT)).andReturn(null);
		EasyMock.expect(reader.readOptionalStringAttribute(node, ConfigurationXml.ATTRIBUTE_COMPATIBILITY)).andReturn(null);

		List<ConfigurationNode> list = new ArrayList<ConfigurationNode>();
		list.add(node);

		// list of settings
		EasyMock.expect(reader.getChildren(node, "setting")).andReturn(list );

		// read of procedure
		EasyMock.expect(reader.readOptionalStringAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_TYPE)).andReturn(type);
		EasyMock.expect(reader.readStringAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_NAME)).andReturn("myname");
		EasyMock.expect(reader.readStringAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_VALUE)).andReturn("myvalue");
		EasyMock.expect(reader.readOptionalIntAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_STAY_OFF_DURATION)).andReturn(AUTOMATIC_PLUGIN_ON_OFF_DISABLED);
		EasyMock.expect(reader.readOptionalIntAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_STAY_ON_DURATION)).andReturn(AUTOMATIC_PLUGIN_ON_OFF_DISABLED);

		// read of settings for scenario
		if(scenarioSettings) {
			EasyMock.expect(reader.getChildren(node, "setting")).andReturn(Collections.singletonList(node));

			EasyMock.expect(reader.readOptionalStringAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_TYPE)).andReturn(type);
			EasyMock.expect(reader.readStringAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_NAME)).andReturn("myname1");
			EasyMock.expect(reader.readStringAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_VALUE)).andReturn("myvalue2");
			EasyMock.expect(reader.readOptionalIntAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_STAY_OFF_DURATION)).andReturn(AUTOMATIC_PLUGIN_ON_OFF_DISABLED);
			EasyMock.expect(reader.readOptionalIntAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_STAY_ON_DURATION)).andReturn(AUTOMATIC_PLUGIN_ON_OFF_DISABLED);
		} else {
			EasyMock.expect(reader.getChildren(node, "setting")).andReturn(Collections.<ConfigurationNode> emptyList());
		}

		EasyMock.replay(node, reader);

		obj.read(node, reader);
		//mapping.read(node, reader);

		assertEquals("newid", obj.getProcedureMappings(InstallationType.Classic).get(0).getId());
		assertEquals(1, obj.getProcedureMappings(InstallationType.Classic).get(0).getSettings().size());
		assertEquals("myvalue", obj.getProcedureMappings(InstallationType.Classic).get(0).getSettingValue(type, "myname"));

		EasyMock.verify(node, reader);

		return obj;
	}


	@Test
	public void testHashCode() {
		DefaultScenario obj = new DefaultScenario();
		DefaultScenario equ = new DefaultScenario();

		TestHelpers.HashCodeTest(obj, equ);

		obj = new DefaultScenario("title", "description");
		equ = new DefaultScenario("title", "description");

		TestHelpers.HashCodeTest(obj, equ);
	}

	@Test
	public void testEquals() {
		DefaultScenario obj = new DefaultScenario();
		DefaultScenario equ = new DefaultScenario();
		DefaultScenario notequ = new DefaultScenario("title", null);

		TestHelpers.EqualsTest(obj, equ, notequ);

		obj = new DefaultScenario("title", "desc");
		equ = new DefaultScenario("title", "desc");
		notequ = new DefaultScenario("title1", null);

		TestHelpers.EqualsTest(obj, equ, notequ);

		obj = new DefaultScenario("title", "desc");
		equ = new DefaultScenario("title", "desc");
		notequ = new DefaultScenario("title", "desc");
		notequ.setGroup("group");

		TestHelpers.EqualsTest(obj, equ, notequ);

		obj = new DefaultScenario("title", "desc");
		obj.setGroup("group");
		equ = new DefaultScenario("title", "desc");
		equ.setGroup("group");
		notequ = new DefaultScenario("title", "desc");

		TestHelpers.EqualsTest(obj, equ, notequ);
	}

	@Test
	public void testWrite() {
		DefaultScenario obj = new DefaultScenario("title", "desc", true, InstallationType.Both);

		final ConfigurationNode node = EasyMock.createStrictMock(ConfigurationNode.class);

		// return the same node always to ease testing
		NodeFactory factory = new NodeFactory() {
			@Override
			public ConfigurationNode createNode(String name) {
				return node;
			}
		};

        node.addAttribute(factory.createNode(ConfigurationXml.ATTRIBUTE_TITLE, "title"));
        node.addAttribute(factory.createNode(ConfigurationXml.ATTRIBUTE_ENABLED, true));
        node.addAttribute(factory.createNode(ConfigurationXml.ATTRIBUTE_COMPATIBILITY, InstallationType.Both.toString()));
        node.addChild(factory.createNode("description", "desc"));

        EasyMock.replay(node);

		obj.write(node, factory);

		EasyMock.verify(node);
	}

	@Test
	public void testWriteNullCompatibility() {
		DefaultScenario obj = new DefaultScenario("title", "desc", true, null);

		final ConfigurationNode node = EasyMock.createStrictMock(ConfigurationNode.class);

		// return the same node always to ease testing
		NodeFactory factory = new NodeFactory() {
			@Override
			public ConfigurationNode createNode(String name) {
				return node;
			}
		};

        node.addAttribute(factory.createNode(ConfigurationXml.ATTRIBUTE_TITLE, "title"));
        node.addAttribute(factory.createNode(ConfigurationXml.ATTRIBUTE_ENABLED, true));
        //node.addAttribute(factory.createNode(ConfigurationXml.ATTRIBUTE_COMPATIBILITY, InstallationType.Both.toString()));
        node.addChild(factory.createNode("description", "desc"));

        EasyMock.replay(node);

		obj.write(node, factory);

		EasyMock.verify(node);
	}

	@Test
	public void testWriteWithContent() {
		DefaultScenario obj = new DefaultScenario("title", "desc", true, InstallationType.Both);

		final ConfigurationNode node = EasyMock.createMock(ConfigurationNode.class);

		// return the same node always to ease testing
		NodeFactory factory = new NodeFactory() {
			@Override
			public ConfigurationNode createNode(String name) {
				return node;
			}
		};

		node.setValue("title");
        node.addAttribute(node);
		node.setValue(true);
        node.addAttribute(node);
        node.setValue(InstallationType.Both.toString());
        node.addAttribute(node);
		node.setValue("desc");
        node.addChild(node);

        // procedure settings
		node.setValue(Constants.Procedures.BUSINESS_BACKEND_ID);
        node.addAttribute(node);
        node.addChild(node);

        // scenario settings
        node.setValue("setname");
        node.addAttribute(node);
        node.setValue("setvalue");
        node.addAttribute(node);
        node.addChild(node);
        /*node.addAttribute(factory.createNode(Constants.ConfigurationXml.ATTRIBUTE_NAME, "setname"));
		node.addChild(node);*/

        EasyMock.replay(node);

		obj.addProcedureMapping(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID));
		obj.addSetting(new DefaultProcedureSetting("setname", "setvalue"));

		obj.write(node, factory);

		EasyMock.verify(node);
	}

	@Test
	public void testAddSetting() {
		DefaultScenario obj = new DefaultScenario();
		assertEquals(0, obj.getCustomSettings().size());

		obj.addSetting(new DefaultProcedureSetting("setname", "setvalue"));

		assertEquals(1, obj.getCustomSettings().size());
		assertTrue(obj.getCustomSettings().containsKey("setname"));
	}

	@Test
	public void testGetProcedureMappings() throws ConfigurationException {
		DefaultScenario obj = new DefaultScenario();
		obj.addProcedureMapping(new DefaultProcedureMapping("idBoth", InstallationType.Both));
		obj.addProcedureMapping(new DefaultProcedureMapping("idAPM", InstallationType.APM));
		obj.addProcedureMapping(new DefaultProcedureMapping("idClassic", InstallationType.Classic));
		obj.addProcedureMapping(new DefaultProcedureMapping("idUnknown", InstallationType.Unknown));
		obj.addProcedureMapping(null);

		List<ProcedureMapping> mappings = obj.getProcedureMappings(InstallationType.APM);
		assertEquals("idBoth", mappings.get(0).getId());
		assertEquals("idAPM", mappings.get(1).getId());
		assertNull(mappings.get(2));
		assertEquals(3, mappings.size());

		mappings = obj.getProcedureMappings(InstallationType.Classic);
		assertEquals("idBoth", mappings.get(0).getId());
		assertEquals("idClassic", mappings.get(1).getId());
		assertNull(mappings.get(2));
		assertEquals(3, mappings.size());

		mappings = obj.getProcedureMappings(InstallationType.Both);
		assertEquals("idBoth", mappings.get(0).getId());
		assertEquals("idAPM", mappings.get(1).getId());
		assertEquals("idClassic", mappings.get(2).getId());
		assertNull(mappings.get(3));
		assertEquals(4, mappings.size());

		mappings = obj.getProcedureMappings(InstallationType.Unknown);
		assertNull(mappings.get(0));
		assertEquals(1, mappings.size());
	}

	@Test
	public void testGetCompatibility() throws ConfigurationException {
		DefaultScenario obj = new DefaultScenario();
		assertEquals(InstallationType.Both, obj.getCompatibility());

		obj = new DefaultScenario("title", "desc", true, InstallationType.APM);
		assertEquals(InstallationType.APM, obj.getCompatibility());
	}

	@Test
	public void testReadCompatiblity() throws ConfigurationException {
		DefaultScenario obj = prepareSettingsTest("mytype", true, InstallationType.Both.toString());
		assertEquals(InstallationType.Both, obj.getCompatibility());

		obj = prepareSettingsTest("mytype", true, InstallationType.APM.toString());
		assertEquals(InstallationType.APM, obj.getCompatibility());

		obj = prepareSettingsTest("mytype", true, InstallationType.Classic.toString());
		assertEquals(InstallationType.Classic, obj.getCompatibility());

		// unknown strings are handled as Both
		obj = prepareSettingsTest("mytype", true, InstallationType.Unknown.toString());
		assertEquals(InstallationType.Both, obj.getCompatibility());

		obj = prepareSettingsTest("mytype", true, "somethingunknown");
		assertEquals(InstallationType.Both, obj.getCompatibility());

		obj = prepareSettingsTest("mytype", true, null);
		assertEquals(InstallationType.Both, obj.getCompatibility());
	}
}
