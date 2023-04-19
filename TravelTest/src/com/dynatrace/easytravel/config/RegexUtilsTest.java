package com.dynatrace.easytravel.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import org.junit.Assert;

import org.junit.Test;

import com.dynatrace.easytravel.util.RegexUtils;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;

/**
 * Some tests of the RegexUtils scanning methods
 *
 * @author philipp.grasboeck
 */
public class RegexUtilsTest {

	@Test
	public void testStaticReplace() {
		String regex = "\\#\\{(\\w+)\\}";
		String input = "Hello World from #{location}! My name is #{name}";
		final String replacement = "MyReplacement";
		String expected = "Hello World from MyReplacement! My name is MyReplacement";

		String result1 = input.replaceAll(regex, replacement);
		final StringBuilder buf = new StringBuilder(input);
		int count = RegexUtils.dynamicScan(buf, Pattern.compile(regex), new RegexUtils.ScanVisitor() {
            @Override
            public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
                Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
                return replacement;
            }
        });
		String result2 = buf.toString();

		Assert.assertEquals(expected, result1);
		Assert.assertEquals(result2, result1);
		Assert.assertEquals(2, count);
	}

    @Test
    public void testGroupReplace() {
        String regex = "\\#\\{(\\w+)\\}";
        String input = "Hello World from #{location}! My name is #{name}";
        String expected = "Hello World from location! My name is name";

        String result1 = input.replaceAll(regex, "$1");
        final StringBuilder buf = new StringBuilder(input);
        int count = RegexUtils.dynamicScan(buf, Pattern.compile(regex), new RegexUtils.ScanVisitor() {
            @Override
            public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
                Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
                return match.group(1);
            }
        });
        String result2 = buf.toString();

        Assert.assertEquals(2, count);
        Assert.assertEquals(expected, result1);
        Assert.assertEquals(result1, result2);
    }

	@Test
	public void testDynamicReplace() {
		String regex = "\\#\\{(\\w+)\\}";
		String input = "Hello World from #{location}! My name is #{name}";
		String expected = "Hello World from dynaTrace! My name is Philipp";

		final Map<String, String> map = new HashMap<String, String>();
		map.put("location", "dynaTrace");
		map.put("name", "Philipp");

		final StringBuilder buf = new StringBuilder(input);
		int count = RegexUtils.dynamicScan(buf, Pattern.compile(regex), new RegexUtils.ScanVisitor() {
            @Override
            public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
                Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
                return map.get(match.group(1));
            }
        });
		String result = buf.toString();

		Assert.assertEquals(2, count);
		Assert.assertEquals(expected, result);
	}

    @Test
    public void testRecursiveReplace() {
        String regex = "\\#\\{(\\w+)\\}";
        String input = "Hello World from #{location}! My name is #{name}";
        String expected = "Hello World from dynaTrace! My name is Philipp";

        final Map<String, String> map = new HashMap<String, String>();
        map.put("location", "#{whereIWork}");
        map.put("name", "#{whatImCalled}");
        map.put("whereIWork", "dynaTrace");
        map.put("whatImCalled", "Philipp");

        final StringBuilder buf = new StringBuilder(input);
        int count = RegexUtils.recursiveScan(buf, Pattern.compile(regex), new RegexUtils.ScanVisitor() {
            @Override
            public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
                Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
                return map.get(match.group(1));
            }
        });
        String result = buf.toString();

        Assert.assertEquals(4, count);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testRecursiveReplaceNoVar() {
        String regex = "\\#\\{(\\w+)\\}";
        String input = "Hello World from #{location}! My name is #{name}";
        String expected = "Hello World from dynaTrace! My name is #{Philipp}";

        final Map<String, String> map = new HashMap<String, String>();
        map.put("location", "#{whereIWork}");
        map.put("name", "#{whatImCalled}");
        map.put("whereIWork", "dynaTrace");
        map.put("whatImCalled", "#{Philipp}"); // Philipp is missing

        final StringBuilder buf = new StringBuilder(input);
        int count = RegexUtils.recursiveScan(buf, Pattern.compile(regex), new RegexUtils.ScanVisitor() {
            @Override
            public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
                Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
                return map.get(match.group(1));
            }
        });
        String result = buf.toString();

        Assert.assertEquals(4, count);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testRecursiveNotReplace() {
        String regex = "\\#\\{(\\w+)\\}";
        String input = "Hello World from #{location}! My name is #{name}";
        String expected = "Hello World from #{whereIWork}! My name is #{whatImCalled}";

        final Map<String, String> map = new HashMap<String, String>();
        map.put("location", "#{whereIWork}");
        map.put("name", "#{whatImCalled}");
        map.put("whereIWork", "dynaTrace"); // they should NOT be used
        map.put("whatImCalled", "Philipp");

        final StringBuilder buf = new StringBuilder(input);
        int count = RegexUtils.recursiveScan(buf, Pattern.compile(regex), new RegexUtils.ScanVisitor() {
            @Override
            public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
                Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
                return depth == 0 ? map.get(match.group(1)) : null;
            }
        });
        String result = buf.toString();

        Assert.assertEquals(2, count);
        Assert.assertEquals(expected, result);
    }


    @Test
    public void testDirectInfiniteRecursiveReplace() {
        String regex = "\\#\\{(\\w+)\\}";
        String input = "Hello World from #{location}! My name is #{name}";
        String expected = "Hello World from dynaTrace! My name is #{name}"; // name not replaced - recursion

        final Map<String, String> map = new HashMap<String, String>();
        map.put("location", "dynaTrace");
        map.put("name", "#{name}"); // direct infinte recursion

        final StringBuilder buf = new StringBuilder(input);
		int count = RegexUtils.recursiveScan(buf, Pattern.compile(regex) , new RegexUtils.ScanVisitor() {
		    @Override
		    public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
		        Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
		        return map.get(match.group(1));
		    }
		});
        String result = buf.toString();

        Assert.assertEquals(1, count);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testDirectInfiniteRecursiveReplaceWithProd() {
        String regex = "\\#\\{(\\w+)\\}";
        String input = "Hello World from #{location}! My name is #{name}";

        final Map<String, String> map = new HashMap<String, String>();
        map.put("location", "dynaTrace");
        map.put("name", "123#{name}456"); // direct infinte recursion in a "production"

        final StringBuilder buf = new StringBuilder(input);
		int count = RegexUtils.recursiveScan(buf, Pattern.compile(regex) , new RegexUtils.ScanVisitor() {
		    @Override
		    public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
		        Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
		        return map.get(match.group(1));
		    }
		});

        Assert.assertEquals(-1, count);
    }

    @Test
    public void testIndirectInfiniteRecursiveReplace() {
        String regex = "\\#\\{(\\w+)\\}";
        String input = "Hello World from #{location}! My name is #{name}";

        final Map<String, String> map = new HashMap<String, String>();
        map.put("location", "#{whereIWork}");
        map.put("name", "#{whatImCalled}-AKA-GARFIELD");
        map.put("whereIWork", "dynaTrace");
        map.put("whatImCalled", "#{name}"); // indirect recursion

        final StringBuilder buf = new StringBuilder(input);
		int count = RegexUtils.recursiveScan(buf, Pattern.compile(regex) , new RegexUtils.ScanVisitor() {
		    @Override
		    public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
		        Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
		        return map.get(match.group(1));
		    }
		});

        Assert.assertEquals(-1, count);
    }

    @Test
    public void testIndirectInfiniteRecursiveReplaceWithProd() {
        String regex = "\\#\\{(\\w+)\\}";
        String input = "Hello World from #{location}! My name is #{name}";

        final Map<String, String> map = new HashMap<String, String>();
        map.put("location", "#{whereIWork}");
        map.put("name", "#{whatImCalled}-AKA-GARFIELD");
        map.put("whereIWork", "dynaTrace");
        map.put("whatImCalled", "XYZ#{name}ABC"); // indirect recursion with "production"

        final StringBuilder buf = new StringBuilder(input);
		int count = RegexUtils.recursiveScan(buf, Pattern.compile(regex) , new RegexUtils.ScanVisitor() {
		    @Override
		    public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
		        Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
		        return map.get(match.group(1));
		    }
		});

        Assert.assertEquals(-1, count);
    }

    @Test
    public void testDynamicFindOnlyCount2() {
        String regex = "\\#\\{(\\w+)\\}";
        final String input = "Hello World from #{location}! My name is #{name}";
        final List<String> foundGroups = new ArrayList<String>();
        final List<String> found1Groups = new ArrayList<String>();

        int count = RegexUtils.dynamicScan(input, Pattern.compile(regex), new RegexUtils.ScanVisitor() {
            @Override
            public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
                Assert.assertEquals(match.group(), input.substring(startIndex, endIndex));
                foundGroups.add(match.group());
                found1Groups.add(match.group(1));
                return match.group(1); // count everything => count=2
            }
        });
        Assert.assertEquals(2, count);
        Assert.assertEquals(2, foundGroups.size());
        Assert.assertEquals(2, found1Groups.size());
        Assert.assertTrue(foundGroups.contains("#{name}"));
        Assert.assertTrue(foundGroups.contains("#{location}"));
        Assert.assertTrue(found1Groups.contains("name"));
        Assert.assertTrue(found1Groups.contains("location"));
    }

    @Test
    public void testDynamicFindOnlyCount1() {
        String regex = "\\#\\{(\\w+)\\}";
        final String input = "Hello World from #{location}! My name is #{name}";
        final List<String> foundGroups = new ArrayList<String>();
        final List<String> found1Groups = new ArrayList<String>();

        int count = RegexUtils.dynamicScan(input, Pattern.compile(regex), new RegexUtils.ScanVisitor() {
            @Override
            public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
                Assert.assertEquals(match.group(), input.substring(startIndex, endIndex));
                foundGroups.add(match.group());
                found1Groups.add(match.group(1));
                return match.group(1).equals("name") ? "name" : null; // only count "name" => count=1
            }
        });
        Assert.assertEquals(1, count);
        Assert.assertEquals(2, foundGroups.size());
        Assert.assertEquals(2, found1Groups.size());
        Assert.assertTrue(foundGroups.contains("#{name}"));
        Assert.assertTrue(foundGroups.contains("#{location}"));
        Assert.assertTrue(found1Groups.contains("name"));
        Assert.assertTrue(found1Groups.contains("location"));
    }

    @Test
    public void testDynamicFindOnlyCount0() {
        String regex = "\\#\\{(\\w+)\\}";
        final String input = "Hello World from #{location}! My name is #{name}";
        final List<String> foundGroups = new ArrayList<String>();
        final List<String> found1Groups = new ArrayList<String>();

        int count = RegexUtils.dynamicScan(input, Pattern.compile(regex), new RegexUtils.ScanVisitor() {
            @Override
            public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
                Assert.assertEquals(match.group(), input.substring(startIndex, endIndex));
                foundGroups.add(match.group());
                found1Groups.add(match.group(1));
                return null; // nothing will be counted => count=0
            }
        });
        Assert.assertEquals(0, count);
        Assert.assertEquals(2, foundGroups.size());
        Assert.assertEquals(2, found1Groups.size());
        Assert.assertTrue(foundGroups.contains("#{name}"));
        Assert.assertTrue(foundGroups.contains("#{location}"));
        Assert.assertTrue(found1Groups.contains("name"));
        Assert.assertTrue(found1Groups.contains("location"));
    }

    @Test
    public void testDynamicFindOnlyVisitorNullCount2() {
        String regex = "\\#\\{(\\w+)\\}";
        final String input = "Hello World from #{location}! My name is #{name}";

        int count = RegexUtils.dynamicScan(input, Pattern.compile(regex), /*visitor*/ null);
        Assert.assertEquals(2, count);
    }

	 // helper method to get coverage of the unused constructor
	 @Test
	 public void testPrivateConstructor() throws Exception {
	 	PrivateConstructorCoverage.executePrivateConstructor(RegexUtils.class);
	 }

    @Test
    public void testRecursiveReplaceEmptyValue() {
        String regex = "\\$\\{([^\\}]+)\\}";
        String input = "pscp -i ${key.file} ${addon.dir}/* ${install.file} ${setup.file} ${remote.user}@${remote.host}:${upload.path}";
        String expected = "pscp -i key123.pem addon-files/*  setup.sh ec2-user@:";

        final Map<String, String> map = new HashMap<String, String>();
        map.put("key.file", "${key.name}.pem");
        map.put("key.name", "key123");
        map.put("addon.dir", "addon-files");
        map.put("install.file", "");
        map.put("setup.file", "setup.sh");
        map.put("remote.user", "ec2-user");
        map.put("remote.host", "");
        map.put("upload.path", "");

        final StringBuilder buf = new StringBuilder(input);
        int count = RegexUtils.recursiveScan(buf, Pattern.compile(regex), new RegexUtils.ScanVisitor() {
            @Override
            public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
                Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
                return map.get(match.group(1));
            }
        });
        String result = buf.toString();

        Assert.assertEquals(8, count);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testRecursiveReplaceWhitespaceValue() {
        String regex = "\\$\\{([^\\}]+)\\}";
        String input = "pscp -i ${key.file} ${addon.dir}/* ${install.file} ${setup.file} ${remote.user}@${remote.host}:${upload.path}";
        String expected = "pscp -i key123.pem addon-files/*   setup.sh ec2-user@ : ";

        final Map<String, String> map = new HashMap<String, String>();
        map.put("key.file", "${key.name}.pem");
        map.put("key.name", "key123");
        map.put("addon.dir", "addon-files");
        map.put("install.file", " ");
        map.put("setup.file", "setup.sh");
        map.put("remote.user", "ec2-user");
        map.put("remote.host", " ");
        map.put("upload.path", " ");

        final StringBuilder buf = new StringBuilder(input);
        int count = RegexUtils.recursiveScan(buf, Pattern.compile(regex), new RegexUtils.ScanVisitor() {
            @Override
            public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
                Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
                return map.get(match.group(1));
            }
        });
        String result = buf.toString();

        Assert.assertEquals(8, count);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testRecursiveReplaceSameValue() {
        String regex = "\\$\\{([^\\}]+)\\}";
        String input = "pscp -i ${key.file} ${addon.dir}/* ${install.file} ${setup.file} ${remote.user}@${remote.host}:${upload.path}";
        String expected = "pscp -i key123.pem addon-files/* AAA setup.sh ec2-user@AAA:AAA";

        final Map<String, String> map = new HashMap<String, String>();
        map.put("key.file", "${key.name}.pem");
        map.put("key.name", "key123");
        map.put("addon.dir", "addon-files");
        map.put("install.file", "AAA");
        map.put("setup.file", "setup.sh");
        map.put("remote.user", "ec2-user");
        map.put("remote.host", "AAA");
        map.put("upload.path", "AAA");

        final StringBuilder buf = new StringBuilder(input);
        int count = RegexUtils.recursiveScan(buf, Pattern.compile(regex), new RegexUtils.ScanVisitor() {
            @Override
            public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
                Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
                return map.get(match.group(1));
            }
        });
        String result = buf.toString();

        Assert.assertEquals(8, count);
        Assert.assertEquals(expected, result);
    }

    // this also tests the endless recursion detection
    @Test
    public void testRecursiveReplaceRepeatValues() {
        String regex = "\\$\\{([^\\}]+)\\}";
        String input = "pscp -i ${key.file} ${addon.dir}/* ${install.file}${install.file} ${setup.file} ${install.file}${install.file} ${remote.user}@${remote.host}:${upload.path}";
        String expected = "pscp -i key123.pem addon-files/* AAAAAA setup.sh AAAAAA ec2-user@BBB:CCC";

        final Map<String, String> map = new HashMap<String, String>();
        map.put("key.file", "${key.name}.pem");
        map.put("key.name", "key123");
        map.put("addon.dir", "addon-files");
        map.put("install.file", "AAA");
        map.put("setup.file", "setup.sh");
        map.put("remote.user", "ec2-user");
        map.put("remote.host", "BBB");
        map.put("upload.path", "CCC");

        final StringBuilder buf = new StringBuilder(input);
        int count = RegexUtils.recursiveScan(buf, Pattern.compile(regex), new RegexUtils.ScanVisitor() {
            @Override
            public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
                Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
                return map.get(match.group(1));
            }
        });
        String result = buf.toString();

        Assert.assertEquals(11, count);
        Assert.assertEquals(expected, result);
    }

    // this also tests the endless recursion detection
    @Test
    public void testRecursiveReplaceRepeatValues2() {
        String regex = "\\$\\{([^\\}]+)\\}";
        String input = "chmod 700 ${key.file} && ssh -i ${key.file} -q -o StrictHostKeyChecking=no ${remote.user}@${remote.host} \"${test.cmd}\" && rm ${key.file} && echo DELETED KEY: ${key.name}";
        String expected = "chmod 700 ../dost-easytravel.pem && ssh -i ../dost-easytravel.pem -q -o StrictHostKeyChecking=no ec2-user@ \"uname -a\" && rm ../dost-easytravel.pem && echo DELETED KEY: dost-easytravel";

        final Map<String, String> map = new HashMap<String, String>();
        map.put("key.file", "../${key.name}.pem");
        map.put("key.name", "dost-easytravel");
        map.put("remote.user", "ec2-user");
        map.put("remote.host", "");
        map.put("test.cmd", "uname -a");

        final StringBuilder buf = new StringBuilder(input);
        int count = RegexUtils.recursiveScan(buf, Pattern.compile(regex), new RegexUtils.ScanVisitor() {
            @Override
            public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
                Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
                return map.get(match.group(1));
            }
        });
        String result = buf.toString();

        Assert.assertEquals(10, count);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testMaxDepthNotExceeded() {
        String regex = "\\#\\{(\\w+)\\}";
        String input = "Hello World from #{location}! My name is #{name}#{name}#{name}#{name}#{name}#{name}#{name}#{name}";
        String expected = "Hello World from dynaTrace! My name is GandalfGandalfGandalfGandalfGandalfGandalfGandalfGandalf";

        final Map<String, String> map = new HashMap<String, String>();
        map.put("location", "#{whereIWork}");
        map.put("name", "#{whatImCalled}");
        map.put("whereIWork", "dynaTrace");
        map.put("whatImCalled", "#{Gandalf}");
        map.put("Gandalf", "Gandalf");

        final StringBuilder buf = new StringBuilder(input);
        int count = RegexUtils.recursiveScan(buf, Pattern.compile(regex), new RegexUtils.ScanVisitor() {
            @Override
            public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
                Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
                return map.get(match.group(1));
            }
        });
        String result = buf.toString();

        Assert.assertEquals(26, count);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testPropertyIndirection1() {
        String regex = "\\$\\{([^\\}\\$]+)\\}";
        String input = "install.file is ${${component}-${remote.os}}";
        String expected = "install.file is ${easytravel-linux}";

        final Map<String, String> map = new HashMap<String, String>();
        map.put("remote.os", "linux");
        map.put("component", "easytravel");

        final StringBuilder buf = new StringBuilder(input);
        int count = RegexUtils.recursiveScan(buf, Pattern.compile(regex), new RegexUtils.ScanVisitor() {
            @Override
            public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
                Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
                return map.get(match.group(1));
            }
        });
        String result = buf.toString();

        Assert.assertEquals(2, count);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testPropertyIndirection2() {
        String regex = "\\$\\{([^\\}\\$]+)\\}";
        String input = "install.file is ${${component}-${remote.os}}";
        String expected = "install.file is easytravel-2.0.0.250-linux.jar";

        final Map<String, String> map = new HashMap<String, String>();
        map.put("remote.os", "linux");
        map.put("component", "easytravel");
        map.put("easytravel-linux", "easytravel-2.0.0.250-linux.jar");

        final StringBuilder buf = new StringBuilder(input);
        int count = RegexUtils.recursiveScan(buf, Pattern.compile(regex), new RegexUtils.ScanVisitor() {
            @Override
            public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
                Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
                return map.get(match.group(1));
            }
        });
        String result = buf.toString();

        Assert.assertEquals(3, count);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testPropertyIndirection3() {
        String regex = "\\$\\{([^\\}\\$]+)\\}";
        String input = "install.file is ${software.${component}-${remote.os}.name}";
        String expected = "install.file is ${software.easytravel-linux.name}";

        final Map<String, String> map = new HashMap<String, String>();
        map.put("remote.os", "linux");
        map.put("component", "easytravel");

        final StringBuilder buf = new StringBuilder(input);
        int count = RegexUtils.recursiveScan(buf, Pattern.compile(regex), new RegexUtils.ScanVisitor() {
            @Override
            public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
                Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
                return map.get(match.group(1));
            }
        });
        String result = buf.toString();

        Assert.assertEquals(2, count);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testPropertyIndirection4() {
        String regex = "\\$\\{([^\\}\\$]+)\\}";
        String input = "install.file is ${software.${component}-${remote.os}.name}";
        String expected = "install.file is easytravel-2.0.0.250-linux.jar";

        final Map<String, String> map = new HashMap<String, String>();
        map.put("remote.os", "linux");
        map.put("component", "easytravel");
        map.put("software.easytravel-linux.name", "easytravel-2.0.0.250-linux.jar");

        final StringBuilder buf = new StringBuilder(input);
        int count = RegexUtils.recursiveScan(buf, Pattern.compile(regex), new RegexUtils.ScanVisitor() {
            @Override
            public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
                Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
                return map.get(match.group(1));
            }
        });
        String result = buf.toString();

        Assert.assertEquals(3, count);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testRecursiveReplaceCounters() {
        String regex = "\\#\\{(\\w+)\\}";
        String input = "Hello World from #{location}! My name is #{name}";
        String expected = "Hello World from dynaTrace! My name is Philipp";

        final Map<String, String> map = new HashMap<String, String>();
        map.put("location", "#{whereIWork}");
        map.put("name", "#{whatImCalled}");
        map.put("whereIWork", "#{compuware}");
        map.put("whatImCalled", "#{gandalf}");
        map.put("compuware", "dynaTrace");
        map.put("gandalf", "Philipp");
        final int[] occurence = { 0 };

        final StringBuilder buf = new StringBuilder(input);
        int count = RegexUtils.recursiveScan(buf, Pattern.compile(regex), new RegexUtils.ScanVisitor() {
            @Override
            public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
                Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
                Assert.assertEquals(occurence[0]++, index);
                String group = match.group(1);
                int expectedDepth = -1;
                if (group.equals("location") || group.equals("name")) {
                	expectedDepth = 0;
                } else if (group.equals("whereIWork") || group.equals("whatImCalled")) {
                	expectedDepth = 1;
                } else if (group.equals("compuware") || group.equals("gandalf")) {
                	expectedDepth = 2;
                }
                Assert.assertEquals(expectedDepth, depth);
                return map.get(match.group(1));
            }
        });
        String result = buf.toString();

        Assert.assertEquals(6, count);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testRecursiveReplaceTreatFirstSpecial() {
        String regex = "\\#\\{(\\w+)\\}";
        String input = "Hello World from #{location}! My name is #{name}";
        String expected = "Hello World from FIRST! My name is Philipp";

        final Map<String, String> map = new HashMap<String, String>();
        map.put("location", "#{whereIWork}");
        map.put("name", "#{whatImCalled}");
        map.put("whereIWork", "dynaTrace");
        map.put("whatImCalled", "Philipp");

        final StringBuilder buf = new StringBuilder(input);
        int count = RegexUtils.recursiveScan(buf, Pattern.compile(regex), new RegexUtils.ScanVisitor() {
            @Override
            public CharSequence visit(MatchResult match, int depth, int index, int startIndex, int endIndex) {
                Assert.assertEquals(match.group(), buf.substring(startIndex, endIndex));
                return index == 0 ? "FIRST" : map.get(match.group(1));
            }
        });
        String result = buf.toString();

        Assert.assertEquals(3, count);
        Assert.assertEquals(expected, result);
    }
}
