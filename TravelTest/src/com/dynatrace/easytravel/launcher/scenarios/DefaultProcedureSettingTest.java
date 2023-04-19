package com.dynatrace.easytravel.launcher.scenarios;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.DefaultConfigurationNode;
import org.junit.Test;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.launcher.config.ConfigurationReader;
import com.dynatrace.easytravel.launcher.config.NodeFactory;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.utils.TestHelpers;


public class DefaultProcedureSettingTest {
	private static final String DEL = DefaultProcedureSetting.DELIMITER;

	@Test
	public void testDefaultProcedureSettingString() throws ConfigurationException {
		DefaultProcedureSetting setting = new DefaultProcedureSetting("");
		assertNull(setting.getType());
		assertNull(setting.getName());
		assertNull(setting.getValue());
		assertEquals("null" + DEL + "null", setting.toREST());

		// read does not allow empty name.... readAndWrite(setting);

		setting = new DefaultProcedureSetting("onlyonestringwithoutdelimiter");
		assertNull(setting.getType());
		assertNull(setting.getName());
		assertNull(setting.getValue());
		assertEquals("null" + DEL + "null", setting.toREST());

		// read does not allow empty name.... readAndWrite(setting);

		setting = new DefaultProcedureSetting("two" + DEL + "items");
		assertNull(setting.getType());
		assertEquals("two", setting.getName());
		assertEquals("items", setting.getValue());
		assertEquals("two" + DEL + "items", setting.toREST());

		readAndWrite(setting);

		setting = new DefaultProcedureSetting("now" + DEL + "three" + DEL + "items");
		assertEquals("now", setting.getType());
		assertEquals("three", setting.getName());
		assertEquals("items", setting.getValue());
		assertEquals("now" + DEL + "three" + DEL + "items", setting.toREST());

		readAndWrite(setting);

		setting = new DefaultProcedureSetting("now" + DEL + "with" + DEL + "five" + DEL + "10" + DEL + "5");
		assertEquals("now", setting.getType());
		assertEquals("with", setting.getName());
		assertEquals("five", setting.getValue());
		assertEquals(10, setting.getStayOnDuration());
		assertEquals(5, setting.getStayOffDuration());

		readAndWrite(setting);

		setting = new DefaultProcedureSetting("with" + DEL + "four" + DEL + "10" + DEL + "5");
		assertEquals(null, setting.getType());
		assertEquals("with", setting.getName());
		assertEquals("four", setting.getValue());
		assertEquals(10, setting.getStayOnDuration());
		assertEquals(5, setting.getStayOffDuration());

		readAndWrite(setting);

		setting = new DefaultProcedureSetting("with" + DEL + "four" + DEL + "noint" + DEL + "5");
		assertEquals(null, setting.getType());
		assertEquals("with", setting.getName());
		assertEquals("four", setting.getValue());
		assertEquals(-1, setting.getStayOnDuration());
		assertEquals(-1, setting.getStayOffDuration());

		readAndWrite(setting);

		setting = new DefaultProcedureSetting("with" + DEL + "four" + DEL + "noint" + DEL + "noint");
		assertEquals(null, setting.getType());
		assertEquals("with", setting.getName());
		assertEquals("four", setting.getValue());
		assertEquals(-1, setting.getStayOnDuration());
		assertEquals(-1, setting.getStayOffDuration());

		readAndWrite(setting);

		setting = new DefaultProcedureSetting("now" + DEL + "with" + DEL + "two" + DEL + "error" + DEL + "fields");
		assertEquals("now", setting.getType());
		assertEquals("with", setting.getName());
		assertEquals("two", setting.getValue());
		assertEquals(-1, setting.getStayOnDuration());
		assertEquals(-1, setting.getStayOffDuration());

		readAndWrite(setting);

		setting = new DefaultProcedureSetting();
		assertNull(setting.getType());
		assertNull(setting.getName());
		assertNull(setting.getValue());

		// read does not allow empty name.... readAndWrite(setting);

		setting = new DefaultProcedureSetting("now" + DEL + "with" + DEL + "too" + DEL + "many" + DEL + "delim" + DEL + "iters");
		assertNull(setting.getType());
		assertNull(setting.getName());
		assertNull(setting.getValue());
		assertEquals("null" + DEL + "null", setting.toREST());
		setting.setValue("val");
		assertEquals("val", setting.getValue());

		// read does not allow empty name.... readAndWrite(setting);

		setting.setValue("val");
		assertEquals("val", setting.getValue());

		// read does not allow empty name.... readAndWrite(setting);

		setting = new DefaultProcedureSetting("name1", "now" + DEL + "with" + DEL + "too" + DEL + "many" + DEL + "delim" + DEL + "iters");
		assertNull(setting.getType());
		assertEquals("name1", setting.getName());
		assertEquals("Had" + DEL + " " + setting.getValue(),
				"now" + DEL + "with" + DEL + "too" + DEL + "many" + DEL + "delim" + DEL + "iters", setting.getValue());
		assertEquals("name1" + DEL + "now" + DEL + "with" + DEL + "too" + DEL + "many" + DEL + "delim" + DEL + "iters", setting.toREST());
		setting.setValue("val");
		assertEquals("val", setting.getValue());

		readAndWrite(setting);

		setting.setStayOnDuration(1);
		assertEquals(1, setting.getStayOnDuration());
		setting.setStayOffDuration(2);
		assertEquals(2, setting.getStayOffDuration());

		readAndWrite(setting);

		try {
			setting = new DefaultProcedureSetting("", "", "", 0, 0);
			fail("Should catch exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "must be greater than 0");
		}

		try {
			setting = new DefaultProcedureSetting("", "", "", -2, 0);
			fail("Should catch exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "must be greater than 0");
		}

		try {
			setting = new DefaultProcedureSetting("", "", "", 1, 0);
			fail("Should catch exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "must be greater than 0");
		}

		try {
			setting = new DefaultProcedureSetting("", "", "", 1, -2);
			fail("Should catch exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "must be greater than 0");
		}
	}

	@Test
	public void testCopy() throws ConfigurationException {
		DefaultProcedureSetting setting = new DefaultProcedureSetting("name1", "now" + DEL + "with" + DEL + "too" + DEL + "many" + DEL + "delim" + DEL + "iters");
		setting.setValue("val");
		setting.setStayOnDuration(1);
		setting.setStayOffDuration(2);

		DefaultProcedureSetting copy = setting.copy();
		assertNotNull(copy);
		assertNull(copy.getType());
		assertEquals("name1", copy.getName());
		assertEquals("name1" + DEL + "val" + DEL + "1" + DEL + "2", copy.toREST());
		assertEquals("val", copy.getValue());
		assertEquals(1, copy.getStayOnDuration());
		assertEquals(2, copy.getStayOffDuration());

		readAndWrite(setting);
	}

	@Test
	public void testWithNull() {
		try {
			new DefaultProcedureSetting(null, null, null);
			fail("should catch exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "must not be null");
		}

		try {
			new DefaultProcedureSetting(null, "name", null);
			fail("should catch exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "must not be null");
		}
	}

	@Test
	public void testToREST() throws ConfigurationException {
		DefaultProcedureSetting setting = new DefaultProcedureSetting("name", "value");
		assertEquals("name" + DEL + "value", setting.toREST());
		setting.setStayOnDuration(5);
		assertEquals("name" + DEL + "value" + DEL + "5" + DEL + "-1", setting.toREST());
		setting.setStayOnDuration(-1);
		setting.setStayOffDuration(51);
		assertEquals("name" + DEL + "value" + DEL + "-1" + DEL + "51", setting.toREST());

		readAndWrite(setting);
	}

	@Test
	public void testToString() throws ConfigurationException {
		DefaultProcedureSetting setting = new DefaultProcedureSetting("");
		TestHelpers.ToStringTest(setting);

		// read does not allow empty name.... readAndWrite(setting);

		setting = new DefaultProcedureSetting(null, "name", "value");
		TestHelpers.ToStringTest(setting);

		readAndWrite(setting);

		setting = new DefaultProcedureSetting("type", "name", "value");
		TestHelpers.ToStringTest(setting);

		readAndWrite(setting);
	}

	@Test
	public void testReadEmpty() throws ConfigurationException {
		ConfigurationNode node = createStrictMock(ConfigurationNode.class);

		List<ConfigurationNode> emptyList = Collections.emptyList();
		expect(node.getAttributes(Constants.ConfigurationXml.ATTRIBUTE_TYPE)).andReturn(emptyList);
		expect(node.getAttributes(Constants.ConfigurationXml.ATTRIBUTE_NAME)).andReturn(emptyList);
		expect(node.getName()).andReturn("nodename").atLeastOnce();
		//expect(node.getAttributes(Constants.ConfigurationXml.ATTRIBUTE_VALUE)).andReturn(Collections.emptyList());

		replay(node);

		DefaultProcedureSetting setting = new DefaultProcedureSetting("");
		try {
			setting.read(node, new ConfigurationReader());
			fail("Should catch exception here");
		} catch (ConfigurationException e) {
			TestHelpers.assertContains(e, "nodename");
		}

		verify(node);
	}

	@Test
	public void testEquals() throws ConfigurationException {
		DefaultProcedureSetting obj = new DefaultProcedureSetting();
		DefaultProcedureSetting equal = new DefaultProcedureSetting();
		DefaultProcedureSetting notequal = new DefaultProcedureSetting("name1", "123");

		//readAndWrite(obj);
		readAndWrite(notequal);

		TestHelpers.EqualsTest(obj, equal, notequal);

		// not equal with null name
		notequal = new DefaultProcedureSetting("", "123");
		TestHelpers.EqualsTest(obj, equal, notequal);

		//readAndWrite(obj);
		readAndWrite(notequal);

		// not equal with same value
		obj = new DefaultProcedureSetting("123");
		equal = new DefaultProcedureSetting("123");
		TestHelpers.EqualsTest(obj, equal, notequal);

		//readAndWrite(obj);
		readAndWrite(notequal);

		// not equal with name
		obj = new DefaultProcedureSetting("name", "123");
		equal = new DefaultProcedureSetting("name", "123");
		TestHelpers.EqualsTest(obj, equal, notequal);

		readAndWrite(obj);
		readAndWrite(notequal);

		// not equal with type
		obj = new DefaultProcedureSetting("type", "name", "123");
		equal = new DefaultProcedureSetting("type", "name", "123");
		TestHelpers.EqualsTest(obj, equal, notequal);

		readAndWrite(obj);
		readAndWrite(notequal);

		// not equal with different type, but same name
		notequal = new DefaultProcedureSetting("type2", "name", "123");
		TestHelpers.EqualsTest(obj, equal, notequal);

		readAndWrite(obj);
		readAndWrite(notequal);

		// not equal with empty type
		notequal = new DefaultProcedureSetting(null, "name", "123");
		TestHelpers.EqualsTest(obj, equal, notequal);

		readAndWrite(obj);
		readAndWrite(notequal);
	}

	@Test
	public void testHashCode() throws ConfigurationException {
		DefaultProcedureSetting obj = new DefaultProcedureSetting();
		DefaultProcedureSetting equal = new DefaultProcedureSetting();

		TestHelpers.HashCodeTest(obj, equal);

		obj = new DefaultProcedureSetting("123");
		equal = new DefaultProcedureSetting("123");

		TestHelpers.HashCodeTest(obj, equal);

		obj = new DefaultProcedureSetting("name", "123");
		equal = new DefaultProcedureSetting("name", "123");

		TestHelpers.HashCodeTest(obj, equal);

		obj = new DefaultProcedureSetting("t", "name", "123");
		equal = new DefaultProcedureSetting("t", "name", "123");

		TestHelpers.HashCodeTest(obj, equal);

		readAndWrite(obj);
	}

	private void readAndWrite(DefaultProcedureSetting setting) throws ConfigurationException {
		NodeFactory factory = new NodeFactory();
		DefaultConfigurationNode node = new DefaultConfigurationNode();
		setting.write(node, factory);

		DefaultProcedureSetting newSetting = new DefaultProcedureSetting();
		newSetting.read(node, new ConfigurationReader());

		assertEquals(setting, newSetting);
		assertEquals(setting.getStayOffDuration(), newSetting.getStayOffDuration());
		assertEquals(setting.getStayOnDuration(), newSetting.getStayOnDuration());
		assertEquals(setting.getValue(), newSetting.getValue());
	}
}
