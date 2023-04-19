package com.dynatrace.easytravel.spring;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.DummyNativeApplication;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.ipc.SocketNativeApplication;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.utils.TestHelpers;


public class PluginInfoListTest {

    private String installationType = InstallationType.Both.name();

    @Before
    public void setUp() {
		DtVersionDetector.enforceInstallationType(InstallationType.Classic);
    }

	@Test
	public void test() {
		PluginInfoList list = new PluginInfoList();
		assertArrayEquals(new String[] {}, list.getData());
		TestHelpers.ToStringTest(list);
		assertTrue(list.isEmpty());

		list = new PluginInfoList((String[])null);
		assertArrayEquals(new String[] {}, list.getData());
		TestHelpers.ToStringTest(list);
		assertTrue(list.isEmpty());

		list = new PluginInfoList((Plugin)null);
		assertArrayEquals(new String[] {}, list.getData());
		TestHelpers.ToStringTest(list);
		assertTrue(list.isEmpty());

		list = new PluginInfoList(new String[] {"plugin1"});
		assertArrayEquals(new String[] {"plugin1::Both"}, list.getData());
		TestHelpers.ToStringTest(list);
		assertTrue(list.contains("plugin1"));
		assertFalse(list.contains("plugin2"));
		assertFalse(list.isEmpty());

		list = new PluginInfoList(new String[] {"plugin1", "plugin2:group1"});
		assertArrayEquals(new String[] {"plugin1::Both", "plugin2:group1:Both"}, list.getData());
		assertArrayEquals(new String[] {"plugin1", "plugin2"}, list.getNames());
		TestHelpers.ToStringTest(list);
		assertTrue(list.contains("plugin1"));
		assertTrue(list.contains("plugin2"));
		assertFalse(list.isEmpty());

		Plugin plugin = new DummyNativeApplication();
		Plugin plugin2 = new SocketNativeApplication();
		assertFalse(list.contains(plugin));
		assertNull(list.get("DummyNativeApplication"));
		assertFalse(list.isEmpty());

		list = new PluginInfoList(plugin);
		assertArrayEquals(new String[] {"DummyNativeApplication::Both"}, list.getData());
		TestHelpers.ToStringTest(list);
		assertTrue(list.contains(plugin));
		assertFalse(list.contains(plugin2));
		assertNotNull(list.get("DummyNativeApplication"));
		assertFalse(list.isEmpty());

		list = new PluginInfoList();
		list.add(plugin);
		assertArrayEquals(new String[] {"DummyNativeApplication::Both"}, list.getData());
		TestHelpers.ToStringTest(list);
		assertFalse(list.isEmpty());

		list = new PluginInfoList(Arrays.asList(new Plugin[] {plugin, plugin2}));
		assertArrayEquals(new String[] {"DummyNativeApplication::Both", "SocketNativeApplication::Both"}, list.getData());
		assertArrayEquals(new String[] {"DummyNativeApplication", "SocketNativeApplication"}, list.getNames());
		TestHelpers.ToStringTest(list);
		assertTrue(list.contains(plugin));
		assertTrue(list.contains(plugin2));
		assertFalse(list.isEmpty());

		list = new PluginInfoList();
		list.addAll(Arrays.asList(new Plugin[] {plugin, plugin2 }));
		assertArrayEquals(new String[] {"DummyNativeApplication::Both", "SocketNativeApplication::Both"}, list.getData());
		assertArrayEquals(new String[] {"DummyNativeApplication", "SocketNativeApplication"}, list.getNames());
		TestHelpers.ToStringTest(list);
		assertTrue(list.contains(plugin));
		assertTrue(list.contains(plugin2));
		assertFalse(list.isEmpty());

		list = new PluginInfoList();
		list.addData(new String[] {});
		assertArrayEquals(new String[] {}, list.getData());
		TestHelpers.ToStringTest(list);
		assertTrue(list.isEmpty());

		list.addData(new String[] {"plugin3"});
		assertArrayEquals(new String[] {"plugin3::Both"}, list.getData());
		TestHelpers.ToStringTest(list);
		assertFalse(list.isEmpty());

		list.addData(new String[] {"plugin4:group3"});
		assertArrayEquals(new String[] {"plugin3::Both", "plugin4:group3:Both"}, list.getData());
		assertArrayEquals(new String[] {"plugin3", "plugin4"}, list.getNames());
		TestHelpers.ToStringTest(list);
		assertFalse(list.isEmpty());

		list.addData(new String[] {"plugin4:group3:" + installationType + ":descr"});
		assertArrayEquals(new String[] {"plugin3::Both", "plugin4:group3:" + installationType + ":descr"}, list.getData());
		assertArrayEquals(new String[] {"plugin3", "plugin4"}, list.getNames());
		TestHelpers.ToStringTest(list);
		assertFalse(list.isEmpty());

		// re-add of plugin without group information does not overwrite
		list.addData(new String[] {"plugin4"});
		assertArrayEquals(new String[] {"plugin3::Both", "plugin4:group3:"+installationType+":descr"}, list.getData());
		assertArrayEquals(new String[] {"plugin3", "plugin4"}, list.getNames());
		TestHelpers.ToStringTest(list);
		assertFalse(list.isEmpty());

		// a different group is updated and a new plugin in update is not updated
		list.updateData(new String[] {"plugin4:group4", "plugin5"});
		assertArrayEquals(new String[] {"plugin3::Both", "plugin4:group4:Both"}, list.getData());
		assertArrayEquals(new String[] {"plugin3", "plugin4"}, list.getNames());
		TestHelpers.ToStringTest(list);
		assertFalse(list.isEmpty());

		list.add(plugin);
		assertArrayEquals(new String[] {"DummyNativeApplication::Both", "plugin3::Both", "plugin4:group4:Both"}, list.getData());
		assertArrayEquals(new String[] {"DummyNativeApplication", "plugin3", "plugin4"}, list.getNames());
		TestHelpers.ToStringTest(list);
		assertFalse(list.isEmpty());

		list.remove(plugin);
		assertArrayEquals(new String[] {"plugin3::Both", "plugin4:group4:Both"}, list.getData());
		assertArrayEquals(new String[] {"plugin3", "plugin4"}, list.getNames());
		TestHelpers.ToStringTest(list);
		assertFalse(list.isEmpty());

		for(Plugin plug : list) {
			assertNotNull(plug);
		}
	}

	@Test
	public void testNull1() {
		Plugin plugin = createStrictMock(Plugin.class);

		// add
		expect(plugin.getGroupName()).andReturn(null);
		expect(plugin.getName()).andReturn("name").times(2);
        expect(plugin.getCompatibility()).andReturn(installationType).anyTimes();

		// getData()
		expect(plugin.getName()).andReturn(null);

		replay(plugin);

		PluginInfoList list = new PluginInfoList(plugin);

		try {
			list.getData();
			fail("Should throw exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "name must not be null");
		}

		verify(plugin);
	}

	@Test
	public void testNull2() {
		PluginInfoList list = new PluginInfoList((List<Plugin>)null);
		assertEquals("Had: " + list.getNames(),
				0, list.getNames().length);

		assertEquals(0, list.getNames(null).length);
		assertEquals(0, list.getData(null).length);
	}

	@Test
	public void testSeparator2() {
		Plugin plugin = createStrictMock(Plugin.class);

		// add
		expect(plugin.getGroupName()).andReturn(null);
		expect(plugin.getName()).andReturn("somename : some").times(2);
        expect(plugin.getCompatibility()).andReturn(installationType).anyTimes();

		// second getData()
		expect(plugin.getName()).andReturn("somename : some").times(2);

		replay(plugin);

		PluginInfoList list = new PluginInfoList(plugin);

		try {
			list.getData();
			fail("Should throw exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, " name must not contain ':'");
		}

		verify(plugin);
	}

	@Test
	public void testSeparator3() {
		Plugin plugin = createStrictMock(Plugin.class);

		// add
		expect(plugin.getGroupName()).andReturn("name");
		expect(plugin.getName()).andReturn("name");

        // getData()
		expect(plugin.getCompatibility()).andReturn("Both").anyTimes();
		expect(plugin.getName()).andReturn("somename").times(3);
        expect(plugin.getGroupName()).andReturn("some : some").times(2);

        replay(plugin);

		PluginInfoList list = new PluginInfoList(plugin);

		try {
			list.getData();
			fail("Should throw exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "groupName must not contain ':'");
		}

		verify(plugin);
	}

	@Test
	public void testAddDataFormats() {
		// full data
		checkAddData("TestPlugin1:Group:" + InstallationType.APM.name() + ":Desc", "TestPlugin1", "Group", "APM", "Desc");

		// minimal data
		checkAddData("TestPlugin2", "TestPlugin2", null, "Both", null);

		// only group
		checkAddData("TestPlugin3:Group", "TestPlugin3", "Group", "Both", null);

		// null group
		checkAddData("TestPlugin4::Classic", "TestPlugin4", "", "Classic", null);

		// no description
		checkAddData("TestPlugin4:Group:APM", "TestPlugin4", "Group", "APM", null);

	}

	protected void checkAddData(String data, String name, String group, String compatibility, String desc) {
		PluginInfoList list = new PluginInfoList();
		list.addData(new String[] { data });

		Plugin plugin = list.get(name);
		assertNotNull("Could not find plugin with name: " + name, plugin);
		assertEquals(name, plugin.getName());
		assertEquals(group, plugin.getGroupName());
		assertEquals(compatibility, plugin.getCompatibility());
		assertEquals(desc, plugin.getDescription());
	}

	@Test
	public void testCompatibility() {
		PluginInfoList list = new PluginInfoList(new String[] {
				"TestPlugin1:Group:APM",
				"TestPlugin2:Group:Classic",
				"TestPlugin3:Group:wrong",
				"TestPlugin4:Group",
				"TestPlugin5:Group:Both",
		});

		assertEquals("APM", list.get("TestPlugin1").getCompatibility());
		assertEquals("Classic", list.get("TestPlugin2").getCompatibility());
		assertEquals("wrong", list.get("TestPlugin3").getCompatibility());
		assertEquals("Both", list.get("TestPlugin4").getCompatibility());
		assertEquals("Both", list.get("TestPlugin5").getCompatibility());

		DtVersionDetector.enforceInstallationType(InstallationType.Classic);
		assertArrayEquals(new String[] { "TestPlugin2", "TestPlugin3", "TestPlugin4", "TestPlugin5" }, list.getNames());

		DtVersionDetector.enforceInstallationType(InstallationType.APM);
		assertArrayEquals(new String[] { "TestPlugin1", "TestPlugin3", "TestPlugin4", "TestPlugin5" }, list.getNames());

		list.get("TestPlugin4").setCompatibility(null);

		DtVersionDetector.enforceInstallationType(InstallationType.Classic);
		assertArrayEquals(new String[] { "TestPlugin2", "TestPlugin3", "TestPlugin4", "TestPlugin5" }, list.getNames());

		DtVersionDetector.enforceInstallationType(InstallationType.APM);
		assertArrayEquals(new String[] { "TestPlugin1", "TestPlugin3", "TestPlugin4", "TestPlugin5" }, list.getNames());

		list.get("TestPlugin3").setCompatibility("APM");

		DtVersionDetector.enforceInstallationType(InstallationType.Classic);
		assertArrayEquals(new String[] { "TestPlugin2", "TestPlugin4", "TestPlugin5" }, list.getNames());

		DtVersionDetector.enforceInstallationType(InstallationType.APM);
		assertArrayEquals(new String[] { "TestPlugin1", "TestPlugin3", "TestPlugin4", "TestPlugin5" }, list.getNames());
	}

	@Test
	public void testWithHosts() {
		PluginInfoList list = new PluginInfoList(new String[] {
				"TestPlugin1:Group",
				"TestPlugin2:Group",
				"TestPlugin3:Group",
				"TestPlugin4:Group",
				"TestPlugin5:Group",
		});

		list.get("TestPlugin1").setHosts(new String[] { "host1", "host2" });
		list.get("TestPlugin2").setHosts(new String[] { });
		list.get("TestPlugin3").setHosts(null);
		list.get("TestPlugin4").setHosts(new String[] { "host1" });

		assertArrayEquals(new String[] { }, list.getNames(null));
		assertArrayEquals(new String[] { "TestPlugin1", "TestPlugin3", "TestPlugin4", "TestPlugin5" }, list.getNames("host1"));
		assertArrayEquals(new String[] { "TestPlugin1", "TestPlugin3", "TestPlugin5" }, list.getNames("host2"));
		assertArrayEquals(new String[] { "TestPlugin3", "TestPlugin5"}, list.getNames("somehost"));

		assertArrayEquals(new String[] { }, list.getData(null));
		assertArrayEquals(new String[] { "TestPlugin1:Group:Both", "TestPlugin3:Group:Both", "TestPlugin4:Group:Both", "TestPlugin5:Group:Both" }, list.getData("host1"));
		assertArrayEquals(new String[] { "TestPlugin1:Group:Both", "TestPlugin3:Group:Both", "TestPlugin5:Group:Both" }, list.getData("host2"));
		assertArrayEquals(new String[] { "TestPlugin3:Group:Both", "TestPlugin5:Group:Both"}, list.getData("somehost"));
	}
}
