package com.dynatrace.easytravel.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;

import org.junit.Test;

import com.dynatrace.easytravel.util.ConfigurationProvider;

public class ReplaceResurivePropertyTest {

	@Test
	public void testReplaceWithForwardRef() {
        Properties map = new Properties();
        map.put("location", "${whereIWork}");
        map.put("name", "${whatImCalled}");
        map.put("whereIWork", "dynaTrace");
        map.put("whatImCalled", "Philipp");

        ConfigurationProvider.replaceRecursiveProperties(map);

        Assert.assertEquals("dynaTrace", map.get("location"));
        Assert.assertEquals("Philipp", map.get("name"));
	}

	@Test
	public void testReplaceWithForwardRefAndConcat() {
		Map<Object, Object> map = new LinkedHashMap<Object, Object>();
        map.put("newLocation", "New Location is: ${location}"); // a real recursion + concat
        map.put("location", "${whereIWork}");
        map.put("name", "${whatImCalled}");
        map.put("whereIWork", "dynaTrace");
        map.put("whatImCalled", "Philipp");

        ConfigurationProvider.replaceRecursiveProperties(map);

        Assert.assertEquals("dynaTrace", map.get("location"));
        Assert.assertEquals("Philipp", map.get("name"));
        Assert.assertEquals("New Location is: dynaTrace", map.get("newLocation"));
	}

	@Test
	public void testReplaceWithForwardRefAndSystemPropertyOverride() {
		Map<Object, Object> map = new LinkedHashMap<Object, Object>();
        map.put("newLocation", "New Location is: ${location}"); // a real recursion + concat
        map.put("location", "${whereIWork}");
        map.put("name", "${whatImCalled}");
        map.put("whereIWork", "dynaTrace");
        map.put("whatImCalled", "Philipp");

        System.setProperty("whatImCalled", "TheMexican");
        System.setProperty("whereIWork", "Tijuana");

        ConfigurationProvider.replaceRecursiveProperties(map, System.getProperties());
        ConfigurationProvider.replaceRecursiveProperties(map);

        Assert.assertEquals("Tijuana", map.get("location"));
        Assert.assertEquals("TheMexican", map.get("name"));
        Assert.assertEquals("New Location is: Tijuana", map.get("newLocation"));
	}

	@Test
	public void testReplaceWithForwardRefAndNoSystemPropertyOverride() {
		Map<Object, Object> map = new LinkedHashMap<Object, Object>();
        map.put("newLocation", "New Location is: ${location}"); // a real recursion + concat
        map.put("location", "${whereIWork}");
        map.put("name", "${whatImCalled}");
        map.put("whereIWork", "dynaTrace");
        map.put("whatImCalled", "Philipp");

        System.setProperty("whatImCalled", "TheMexican");
        System.setProperty("whereIWork", "Tijuana");

//        don't replace with sysprops
//        ConfigurationProvider.replaceRecursiveProperties(map, System.getProperties());
        ConfigurationProvider.replaceRecursiveProperties(map);

        Assert.assertEquals("dynaTrace", map.get("location"));
        Assert.assertEquals("Philipp", map.get("name"));
        Assert.assertEquals("New Location is: dynaTrace", map.get("newLocation"));
	}

	@Test
	public void testReplacePropertyMissing() {
        Properties map = new Properties();
        map.put("location", "${whereIWork}");
        map.put("name", "${whatImCalled}");
//        map.put("whereIWork", "dynaTrace"); // missing
        map.put("whatImCalled", "Philipp");

        ConfigurationProvider.replaceRecursiveProperties(map);

        Assert.assertEquals("${whereIWork}", map.get("location"));
        Assert.assertEquals("Philipp", map.get("name"));
	}

	@Test
	public void testPropertyIndirection1() {
		Map<Object, Object> map = new LinkedHashMap<Object, Object>();
        map.put("remote.os", "linux");
        map.put("component", "easytravel");
        map.put("install.file", "${${component}-${remote.os}}");

        ConfigurationProvider.replaceRecursiveProperties(map);

        Assert.assertEquals("${easytravel-linux}", map.get("install.file"));
	}

	@Test
	public void testPropertyIndirection2() {
		Map<Object, Object> map = new LinkedHashMap<Object, Object>();
        map.put("remote.os", "linux");
        map.put("component", "easytravel");
        map.put("install.file", "${${component}-${remote.os}}");
        map.put("easytravel-linux", "easytravel-2.0.0.250-linux.jar");

        ConfigurationProvider.replaceRecursiveProperties(map);

        Assert.assertEquals("easytravel-2.0.0.250-linux.jar", map.get("install.file"));
	}

	@Test
	public void testPropertyIndirection3() {
		Map<Object, Object> map = new LinkedHashMap<Object, Object>();
        map.put("remote.os", "linux");
        map.put("component", "easytravel");
        map.put("install.file", "${dynaTrace-${component}-os:${remote.os}-software}");

        ConfigurationProvider.replaceRecursiveProperties(map);

        Assert.assertEquals("${dynaTrace-easytravel-os:linux-software}", map.get("install.file"));
	}

	@Test
	public void testPropertyIndirection4() {
		Map<Object, Object> map = new LinkedHashMap<Object, Object>();
        map.put("remote.os", "linux");
        map.put("component", "easytravel");
        map.put("install.file", "${dynaTrace-${component}-os:${remote.os}-software}");
        map.put("dynaTrace-easytravel-os:linux-software", "easytravel-2.0.0.250-linux.jar");

        ConfigurationProvider.replaceRecursiveProperties(map);

        Assert.assertEquals("easytravel-2.0.0.250-linux.jar", map.get("install.file"));
	}

	@Test
	public void testSubstring() {
		Map<Object, Object> map = new LinkedHashMap<Object, Object>();
        map.put("component", "easytravel");
        map.put("easy", "${component:0:4}");
        map.put("travel", "${component:4}");
        map.put("ytr", "${component:3:6}");
        map.put("empty1", "${component:3:3}");
        map.put("empty2", "${component:0:0}");
        map.put("empty3", "${component:9:9}");
        map.put("empty4", "${component:10:10}");
        map.put("empty5", "${component:10:9}");
        map.put("empty6", "${component:10:0}");
        map.put("empty7", "${component:10}");
        map.put("error1", "${component:20}");
        map.put("error2", "${component:0:20}");
        map.put("error3", "${component:4:2}");
        map.put("error4", "${component:4:20}");
        map.put("easytravel", "EASYTRAVEL");
        map.put("x-easy", "EASY");
        map.put("indirection1", "${${component}}");
        map.put("indirection2", "${${component:0:10}}");
        map.put("indirection3", "${${component:0:55}}");
        map.put("indirection4", "${x-${component:0:4}}");
        map.put("indirection5", "${x-${component:0:4}:0:2}");

        ConfigurationProvider.replaceRecursiveProperties(map);

        Assert.assertEquals("easy", map.get("easy"));
        Assert.assertEquals("travel", map.get("travel"));
        Assert.assertEquals("ytr", map.get("ytr"));
        Assert.assertEquals("", map.get("empty1"));
        Assert.assertEquals("", map.get("empty2"));
        Assert.assertEquals("", map.get("empty3"));
        Assert.assertEquals("", map.get("empty4"));
        Assert.assertEquals("", map.get("empty5"));
        Assert.assertEquals("", map.get("empty6"));
        Assert.assertEquals("", map.get("empty7"));
        Assert.assertEquals("easytravel", map.get("error1"));
        Assert.assertEquals("easytravel", map.get("error2"));
        Assert.assertEquals("", map.get("error3"));
        Assert.assertEquals("travel", map.get("error4"));
        Assert.assertEquals("EASYTRAVEL", map.get("indirection1"));
        Assert.assertEquals("EASYTRAVEL", map.get("indirection2"));
        Assert.assertEquals("EASYTRAVEL", map.get("indirection3"));
        Assert.assertEquals("EASY", map.get("indirection4"));
        Assert.assertEquals("EA", map.get("indirection5"));
	}

	@Test
	public void testPropertyIndirection5() {
		Map<Object, Object> map = new LinkedHashMap<Object, Object>();
        map.put("remote.os", "linux");
        map.put("component", "easytravel");
        map.put("install.file", "${${component}-${remote.${os}}}");

        ConfigurationProvider.replaceRecursiveProperties(map);

        Assert.assertEquals("${easytravel-${remote.${os}}}", map.get("install.file"));
	}

	@Test
	public void testPropertyIndirection6() {
		Map<Object, Object> map = new LinkedHashMap<Object, Object>();
        map.put("remote.os", "linux");
        map.put("os", "os");
        map.put("component", "easytravel");
        map.put("install.file", "${${component}-${remote.${os}}}");

        ConfigurationProvider.replaceRecursiveProperties(map);

        Assert.assertEquals("${easytravel-linux}", map.get("install.file"));
	}

	@Test
	public void testPropertyIndirection7() {
		Map<Object, Object> map = new LinkedHashMap<Object, Object>();
        map.put("remote.os", "linux");
        map.put("os", "os");
        map.put("component", "easytravel");
        map.put("install.file", "${${component}-${remote.${os}}}");
        map.put("easytravel-linux", "easytravel-2.0.0.250-linux.jar");

        ConfigurationProvider.replaceRecursiveProperties(map);

        Assert.assertEquals("easytravel-2.0.0.250-linux.jar", map.get("install.file"));
	}

	@Test
	public void testPropertyIndirection8() {
		Map<Object, Object> map = new LinkedHashMap<Object, Object>();
        map.put("remote.os", "linux");
        map.put("os", "os");
        map.put("suffix", "jar");
        map.put("component", "easytravel");
        map.put("install.file", "${${component}-${remote.${os}}}");
        map.put("easytravel-linux", "easytravel-2.0.0.250-linux.${suffix}");

        ConfigurationProvider.replaceRecursiveProperties(map);

        Assert.assertEquals("easytravel-2.0.0.250-linux.jar", map.get("install.file"));
	}

}
