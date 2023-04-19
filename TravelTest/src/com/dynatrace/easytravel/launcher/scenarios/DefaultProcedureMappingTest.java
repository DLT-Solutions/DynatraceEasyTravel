package com.dynatrace.easytravel.launcher.scenarios;

import static com.dynatrace.easytravel.launcher.misc.Constants.Misc.AUTOMATIC_PLUGIN_ON_OFF_DISABLED;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.tree.ConfigurationNode;
import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.launcher.config.ConfigurationReader;
import com.dynatrace.easytravel.launcher.config.NodeFactory;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.Constants.ConfigurationXml;
import com.dynatrace.easytravel.utils.TestHelpers;


@RunWith(MockitoJUnitRunner.class)
public class DefaultProcedureMappingTest {

	@Test
	public void testDefaultProcedureMapping() {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping();
		assertNull(mapping.getId());
	}

	@Test
	public void testDefaultProcedureMappingString() {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping("someid");
		assertEquals("someid", mapping.getId());
	}

	@Test
	public void testRead() throws ConfigurationException {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping("someid");

		ConfigurationNode node = EasyMock.createStrictMock(ConfigurationNode.class);
		ConfigurationReader reader = EasyMock.createStrictMock(ConfigurationReader.class);

		expectAttributes(node, reader);

		List<ConfigurationNode> list = Collections.emptyList();
		EasyMock.expect(reader.getChildren(node, "setting")).andReturn(list );

		EasyMock.replay(node, reader);

		mapping.read(node, reader);

		assertEquals("newid", mapping.getId());
		assertEquals(0, mapping.getSettings().size());

		EasyMock.verify(node, reader);
	}

	@Test
	public void testReadWithSettings() throws ConfigurationException {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping("someid");

		ConfigurationNode node = EasyMock.createStrictMock(ConfigurationNode.class);
		ConfigurationReader reader = EasyMock.createStrictMock(ConfigurationReader.class);

		expectAttributes(node, reader);

		List<ConfigurationNode> list = new ArrayList<ConfigurationNode>();
		list.add(node);

		// list of settings
		EasyMock.expect(reader.getChildren(node, "setting")).andReturn(list );

		// read first setting

		EasyMock.expect(reader.readOptionalStringAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_TYPE)).andReturn("mytype");
		EasyMock.expect(reader.readStringAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_NAME)).andReturn("myname");
		EasyMock.expect(reader.readStringAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_VALUE)).andReturn("myvalue");
		EasyMock.expect(reader.readOptionalIntAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_STAY_OFF_DURATION)).andReturn(AUTOMATIC_PLUGIN_ON_OFF_DISABLED);
		EasyMock.expect(reader.readOptionalIntAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_STAY_ON_DURATION)).andReturn(AUTOMATIC_PLUGIN_ON_OFF_DISABLED);
		EasyMock.replay(node, reader);

		mapping.read(node, reader);

		assertEquals("newid", mapping.getId());
		assertEquals(1, mapping.getSettings().size());
		assertEquals("myvalue", mapping.getSettingValue("mytype", "myname"));

		// verify copy()
		ProcedureMapping copy = mapping.copy();
		assertEquals("newid", copy.getId());
		assertEquals(1, copy.getSettings().size());
		assertEquals("myvalue", copy.getSettingValue("mytype", "myname"));

		EasyMock.verify(node, reader);
	}

	@Test
	public void testReadWithSettingsStayOnOff() throws ConfigurationException {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping("someid");

		ConfigurationNode node = EasyMock.createStrictMock(ConfigurationNode.class);
		ConfigurationReader reader = EasyMock.createStrictMock(ConfigurationReader.class);

		expectAttributes(node, reader);

		List<ConfigurationNode> list = new ArrayList<ConfigurationNode>();
		list.add(node);

		// list of settings
		EasyMock.expect(reader.getChildren(node, "setting")).andReturn(list );

		// read first setting

		EasyMock.expect(reader.readOptionalStringAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_TYPE)).andReturn("mytype");
		EasyMock.expect(reader.readStringAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_NAME)).andReturn("myname");
		EasyMock.expect(reader.readStringAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_VALUE)).andReturn("myvalue");
		EasyMock.expect(reader.readOptionalIntAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_STAY_OFF_DURATION)).andReturn(23);
		EasyMock.expect(reader.readOptionalIntAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_STAY_ON_DURATION)).andReturn(24);
		EasyMock.replay(node, reader);

		mapping.read(node, reader);

		assertEquals("newid", mapping.getId());
		assertEquals(1, mapping.getSettings().size());
		assertEquals("myvalue", mapping.getSettingValue("mytype", "myname"));
		assertEquals(1, mapping.getScheduledOnOffSettings().size());
		assertEquals(23, mapping.getScheduledOnOffSettings().iterator().next().getStayOffDuration());
		assertEquals(24, mapping.getScheduledOnOffSettings().iterator().next().getStayOnDuration());

		// verify copy()
		ProcedureMapping copy = mapping.copy();
		assertEquals("newid", copy.getId());
		assertEquals(1, copy.getSettings().size());
		assertEquals("myvalue", copy.getSettingValue("mytype", "myname"));
		assertEquals(23, copy.getScheduledOnOffSettings().iterator().next().getStayOffDuration());
		assertEquals(24, copy.getScheduledOnOffSettings().iterator().next().getStayOnDuration());

		EasyMock.verify(node, reader);
	}
	@Test
	public void testReadWithCustomSettings() throws ConfigurationException {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping("someid");
		assertFalse(mapping.hasCustomSettings());

		ConfigurationNode node = EasyMock.createStrictMock(ConfigurationNode.class);
		ConfigurationReader reader = EasyMock.createStrictMock(ConfigurationReader.class);

		expectAttributes(node, reader);

		List<ConfigurationNode> list = new ArrayList<ConfigurationNode>();
		list.add(node);

		// list of settings
		EasyMock.expect(reader.getChildren(node, "setting")).andReturn(list );

		// read first setting

		EasyMock.expect(reader.readOptionalStringAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_TYPE)).andReturn(Scenario.TYPE_SCENARIO_PROCEDURE_CONFIG);
		EasyMock.expect(reader.readStringAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_NAME)).andReturn("myname");
		EasyMock.expect(reader.readStringAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_VALUE)).andReturn("myvalue");
		EasyMock.expect(reader.readOptionalIntAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_STAY_OFF_DURATION)).andReturn(AUTOMATIC_PLUGIN_ON_OFF_DISABLED);
		EasyMock.expect(reader.readOptionalIntAttribute(node, Constants.ConfigurationXml.ATTRIBUTE_STAY_ON_DURATION)).andReturn(AUTOMATIC_PLUGIN_ON_OFF_DISABLED);
		EasyMock.replay(node, reader);

		mapping.read(node, reader);

		assertEquals("newid", mapping.getId());
		assertEquals(1, mapping.getSettings().size());
		assertEquals("myvalue", mapping.getSettingValue(Scenario.TYPE_SCENARIO_PROCEDURE_CONFIG, "myname"));
		assertEquals(1, mapping.getCustomSettings().size());
		assertTrue(mapping.hasCustomSettings());

		// verify copy()
		ProcedureMapping copy = mapping.copy();
		assertEquals("newid", copy.getId());
		assertEquals(1, copy.getSettings().size());
		assertEquals("myvalue", copy.getSettingValue(Scenario.TYPE_SCENARIO_PROCEDURE_CONFIG, "myname"));
		assertEquals(1, copy.getCustomSettings().size());

		EasyMock.verify(node, reader);
	}

	private void expectAttributes(ConfigurationNode node, ConfigurationReader reader) throws ConfigurationException {
		EasyMock.expect(reader.readStringAttribute(node, ConfigurationXml.ATTRIBUTE_ID)).andReturn("newid");
		EasyMock.expect(reader.readOptionalStringAttribute(node, ConfigurationXml.ATTRIBUTE_HOST)).andReturn(null);
		EasyMock.expect(reader.readOptionalStringAttribute(node, ConfigurationXml.ATTRIBUTE_APM_TENANT)).andReturn(null);
		EasyMock.expect(reader.readOptionalStringAttribute(node, ConfigurationXml.ATTRIBUTE_COMPATIBILITY)).andReturn(null);
	}

	@Test
	public void testWrite() {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);

		writeMapping(mapping);

		mapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID, null);

		writeMapping(mapping);

		mapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID, InstallationType.APM);

		writeMapping(mapping);
	}

	@Test
	public void testWriteWithHostAndSetting() {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);
		mapping.setHost("somehost");
		mapping.addSetting(new DefaultProcedureSetting("somename", "somevalue"));

		final ConfigurationNode node = EasyMock.createStrictMock(ConfigurationNode.class);

		// return the same node always to ease testing
		NodeFactory factory = new NodeFactory() {
			@Override
			public ConfigurationNode createNode(String name) {
				return node;
			}
		};

        // procedure settings
		node.setValue(Constants.Procedures.BUSINESS_BACKEND_ID);
		node.addAttribute(node);
		node.setValue("somehost");
		node.addAttribute(node);
        node.setValue("somename");
		node.addAttribute(node);
        node.setValue("somevalue");
		node.addAttribute(node);
        node.addChild(node);

        EasyMock.replay(node);

		mapping.write(node, factory);

		EasyMock.verify(node);
	}

	@Test
	public void testAddAndRemoveSetting() {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping();
		assertNull(mapping.getId());

		assertNull("setting not yet available", mapping.getSetting("mytype", "myname"));

		// add a setting
		mapping.addSetting(new DefaultProcedureSetting("mytype", "myname", "myvalue"));
		assertEquals("setting available now", "myvalue", mapping.getSetting("mytype", "myname").getValue());

		// add a different setting
		mapping.addSetting(new DefaultProcedureSetting("mytype", "myname2", "myvalue2"));
		assertEquals("setting available now", "myvalue", mapping.getSetting("mytype", "myname").getValue());
		assertEquals("setting available now", "myvalue2", mapping.getSetting("mytype", "myname2").getValue());

		// add a previously set setting with a new value
		mapping.addSetting(new DefaultProcedureSetting("mytype", "myname", "myvalue3"));
		assertEquals("setting available now", "myvalue3", mapping.getSetting("mytype", "myname").getValue());

		mapping.removeSetting(mapping.getSetting("mytype", "myname"));

		assertNull("setting gone again", mapping.getSetting("mytype", "myname"));

		assertNull(mapping.getSetting(null, "somename"));
		mapping.addSetting(new DefaultProcedureSetting(null, "somename", "somevalue"));
		assertEquals("somevalue", mapping.getSetting(null, "somename").getValue());
		mapping.addSetting(new DefaultProcedureSetting(null, "somename", "somevalue2"));
		assertEquals("somevalue2", mapping.getSetting(null, "somename").getValue());

		assertNull(mapping.getSetting("type1", "somename"));
		mapping.addSetting(new DefaultProcedureSetting("type1", "somename", "somevalue"));
		assertEquals("somevalue", mapping.getSetting("type1", "somename").getValue());
		mapping.addSetting(new DefaultProcedureSetting("type1", "somename", "somevalue2"));
		assertEquals("somevalue2", mapping.getSetting("type1", "somename").getValue());


	}

	@Test
	public void testGetSettings() {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping();
		assertEquals(0, mapping.getSettings().size());
		assertEquals(0, mapping.getSettings("type").size());
		assertEquals(0, mapping.getSettings(null).size());

		mapping.addSetting(new DefaultProcedureSetting("name", "value"));
		assertEquals(1, mapping.getSettings().size());
		assertEquals(1, mapping.getSettings(null).size());
		assertEquals(0, mapping.getSettings("type").size());

		try {
			mapping.getSetting(null, null);
			fail("Should catch exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "Invalid setting name argument");
		}

		mapping.addSetting(new DefaultProcedureSetting("type1", "name", "value1"));
		mapping.addSetting(new DefaultProcedureSetting("type2", "name", "value2"));
		assertEquals(3, mapping.getSettings().size());
		assertEquals(1, mapping.getSettings(null).size());
		assertEquals(0, mapping.getSettings("type").size());
		assertEquals(1, mapping.getSettings("type1").size());

		assertEquals("value1", mapping.getSetting("type1", "name").getValue());
		assertEquals("value2", mapping.getSetting("type2", "name").getValue());
		assertEquals("value", mapping.getSetting(null, "name").getValue());

		assertEquals("value1", mapping.getSettingValue("type1", "name"));
		assertEquals("value2", mapping.getSettingValue("type2", "name"));
		assertEquals("value", mapping.getSettingValue(null, "name"));
		assertEquals("value", mapping.getSettingValue("name"));

		assertNull(mapping.getSetting(null, "notexist"));
		assertNull(mapping.getSettingValue("notexist"));
		assertNull(mapping.getSettingValue(null, "notexist"));
	}

	@Mock ConfigurationReader reader;

	@Test
	public void testFindOneOverriddenSetting() throws ConfigurationException {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping();
		ArrayList<ConfigurationNode> returnList = new ArrayList<ConfigurationNode>();
		returnList.add(null);
		returnList.add(null);

		when(reader.getChildren(any(ConfigurationNode.class), anyString())).thenReturn(returnList);


		when(reader.readOptionalStringAttribute(any(ConfigurationNode.class), eq(Constants.ConfigurationXml.ATTRIBUTE_TYPE))).thenReturn("property", Scenario.TYPE_SCENARIO_PROCEDURE_CONFIG);
		when(reader.readStringAttribute(any(ConfigurationNode.class), eq(Constants.ConfigurationXml.ATTRIBUTE_NAME))).thenReturn("XXX", "config.dtServer");
		when(reader.readStringAttribute(any(ConfigurationNode.class), eq(Constants.ConfigurationXml.ATTRIBUTE_VALUE))).thenReturn("blub", "localhost");

		mapping.read(null, reader);

		assertTrue(mapping.hasCustomSettings());
		Collection<Setting> overriddenSetting = mapping.getCustomSettings();
		assertEquals(1, overriddenSetting.size());
		Setting item = overriddenSetting.iterator().next();
		assertEquals(item.getName(), "config.dtServer");
		assertEquals(item.getValue(), "localhost");
	}

	@Test
	public void testFindMultipleOverriddenSettings() throws ConfigurationException {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping();
		ArrayList<ConfigurationNode> returnList = new ArrayList<ConfigurationNode>();
		returnList.add(null);
		returnList.add(null);
		returnList.add(null);

		when(reader.getChildren(any(ConfigurationNode.class), anyString())).thenReturn(returnList);

		when(reader.readOptionalStringAttribute(any(ConfigurationNode.class), eq(Constants.ConfigurationXml.ATTRIBUTE_TYPE))).thenReturn(Scenario.TYPE_SCENARIO_PROCEDURE_CONFIG, "property", Scenario.TYPE_SCENARIO_PROCEDURE_CONFIG);
		when(reader.readStringAttribute(any(ConfigurationNode.class), eq(Constants.ConfigurationXml.ATTRIBUTE_NAME))).thenReturn("config.backendPort", "XXX", "config.dtServer");
		when(reader.readStringAttribute(any(ConfigurationNode.class), eq(Constants.ConfigurationXml.ATTRIBUTE_VALUE))).thenReturn("80", "blub", "localhost");

		mapping.read(null, reader);

		assertTrue(mapping.hasCustomSettings());
		Collection<Setting> overriddenSetting = mapping.getCustomSettings();

		assertEquals(2, overriddenSetting.size());
	}

	@Test
	public void testHost() throws ConfigurationException {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping();
		assertNull(mapping.getHost());
		mapping.setHost("somehost");
		assertEquals("somehost", mapping.getHost());
	}

	@Test
	public void testAPMTenant() throws ConfigurationException {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping();
		assertNull(mapping.getAPMTenantUUID());
		mapping.setAPMTenantUUID("sometenant");
		assertEquals("sometenant", mapping.getAPMTenantUUID());

		writeMapping(mapping);
	}

	private void writeMapping(DefaultProcedureMapping mapping) {
		final ConfigurationNode node = EasyMock.createStrictMock(ConfigurationNode.class);

		// return the same node always to ease testing
		NodeFactory factory = new NodeFactory() {
			@Override
			public ConfigurationNode createNode(String name) {
				return node;
			}
		};

        // procedure settings
		node.setValue(mapping.getId());
        node.addAttribute(node);
        if(mapping.getAPMTenantUUID() != null) {
        	node.setValue(mapping.getAPMTenantUUID());
            node.addAttribute(node);
        }
        if(mapping.getCompatibility() != null && !InstallationType.Both.equals(mapping.getCompatibility())) {
        	node.setValue(mapping.getCompatibility().toString());
            node.addAttribute(node);
        }

        EasyMock.replay(node);

		mapping.write(node, factory);

		EasyMock.verify(node);
	}

	@Test
	public void testOnOffSettings() {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping();
		DefaultProcedureSetting setting = new DefaultProcedureSetting(null, "sname1", "val", 100, 200);
		mapping.addSetting(setting);
		mapping.addSetting(new DefaultProcedureSetting(null, "sname2", "val"));
		//mapping.addSetting(new DefaultProcedureSetting(null, "sname1", "val", 0, 0));

		// currently we check only on offduration, so this is not detected as onoffduration!
		//mapping.addSetting(new DefaultProcedureSetting(null, "sname1", "val", 100, 0));

		assertEquals(1, mapping.getScheduledOnOffSettings().size());
		assertEquals(setting, mapping.getScheduledOnOffSettings().iterator().next());
	}

	@Test
	public void testInstallationType() {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping();
		assertEquals(InstallationType.Both, mapping.getCompatibility());

		mapping = new DefaultProcedureMapping("somemapping");
		assertEquals(InstallationType.Both, mapping.getCompatibility());

		mapping = new DefaultProcedureMapping("somemapping", InstallationType.APM);
		assertEquals(InstallationType.APM, mapping.getCompatibility());
	}

    @Test
    public void testEquality() {
        DefaultProcedureMapping mapping1 = new DefaultProcedureMapping();
        assertEquals(InstallationType.Both, mapping1.getCompatibility());

        DefaultProcedureMapping mapping2 = new DefaultProcedureMapping();
        assertEquals(InstallationType.Both, mapping2.getCompatibility());

        DefaultProcedureMapping mapping3 = new DefaultProcedureMapping(null, InstallationType.APM);
        assertEquals(InstallationType.APM, mapping3.getCompatibility());

        DefaultProcedureMapping mapping4 = new DefaultProcedureMapping("somemapping");

        DefaultProcedureMapping mapping5 = new DefaultProcedureMapping("somemapping");
        mapping5.setHost("otherhost");
        assertEquals(mapping5.getHost(), "otherhost");

        assertTrue(mapping1.equals(mapping2));

        assertFalse(mapping1.equals(mapping3));

        assertFalse(mapping1.equals(mapping4));

        assertFalse(mapping1.equals(mapping5));

    }
}
