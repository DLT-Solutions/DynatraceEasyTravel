package com.dynatrace.easytravel.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.utils.TestEnvironment;
import com.dynatrace.easytravel.utils.TestHelpers;


public class CommandlineArgumentsTest {
	private static final File ABS_TEMP_DIR = new File(TestEnvironment.ABS_RUNTIME_DATA_PATH);
	private static final File TEMP_DIR = new File(TestEnvironment.RUNTIME_DATA_PATH);

	@Before
	public void setUp() throws IOException {
		TestEnvironment.createOrClearRuntimeData();
		
		assertTrue(ABS_TEMP_DIR.exists());
		assertTrue(ABS_TEMP_DIR.isDirectory());
		
		assertTrue(TEMP_DIR.exists());
		assertTrue(TEMP_DIR.isDirectory());
	}

	@Test
	public void testCommandlineArgumentsEmpty() throws Exception {
		CommandlineArguments arguments = new CommandlineArguments(new String[] {});
		assertNull(arguments.getPropertyFile());
		assertNull(arguments.getScenarioFile());
		assertNull(arguments.getStartGroup());
		assertNull(arguments.getStartScenario());
		assertFalse(arguments.isNoAutostart());
	}
	
	@Test
	public void testCommandlineArgumentsNull() throws Exception {
		CommandlineArguments arguments = new CommandlineArguments(null);
		assertNull(arguments.getPropertyFile());
		assertNull(arguments.getScenarioFile());
		assertNull(arguments.getStartGroup());
		assertNull(arguments.getStartScenario());
		assertFalse(arguments.isNoAutostart());
	}
	
	@Test
	public void testCommandlineArguments() throws Exception {
		File property = File.createTempFile("test", ".property", ABS_TEMP_DIR);
		File scenario = File.createTempFile("test", ".xml", ABS_TEMP_DIR);
		
		CommandlineArguments arguments = new CommandlineArguments(new String[] {
				BaseConstants.MINUS + Constants.CmdArguments.PROPERTY_FILE,
				property.getAbsolutePath(),
				BaseConstants.MINUS + Constants.CmdArguments.SCENARIO_FILE,
				scenario.getAbsolutePath(),
				BaseConstants.MINUS + Constants.CmdArguments.START_SCENARIO,
				"scenariotostart",
				BaseConstants.MINUS + Constants.CmdArguments.START_GROUP,
				"grouptostart",				
				BaseConstants.MINUS + Constants.CmdArguments.NO_AUTOSTART
		});

		assertEquals(property, arguments.getPropertyFile());
		assertEquals(scenario, arguments.getScenarioFile());
		assertEquals("scenariotostart", arguments.getStartScenario());
		assertEquals("grouptostart", arguments.getStartGroup());
		assertTrue(arguments.isNoAutostart());
	}

	@Test
	public void testCommandlineArgumentsRelativeDirs() throws Exception {
		File property = File.createTempFile("test", ".property", TEMP_DIR);
		File scenario = File.createTempFile("test", ".xml", TEMP_DIR);
		
		CommandlineArguments arguments = new CommandlineArguments(new String[] {
				BaseConstants.MINUS + Constants.CmdArguments.PROPERTY_FILE,
				property.getPath(),
				BaseConstants.MINUS + Constants.CmdArguments.SCENARIO_FILE,
				scenario.getPath(),
				BaseConstants.MINUS + Constants.CmdArguments.START_SCENARIO,
				"scenariotostart",
				BaseConstants.MINUS + Constants.CmdArguments.START_GROUP,
				"grouptostart",				
				BaseConstants.MINUS + Constants.CmdArguments.NO_AUTOSTART
		});

		assertEquals(property.getAbsoluteFile(), arguments.getPropertyFile());
		assertEquals(scenario.getAbsoluteFile(), arguments.getScenarioFile());
		assertEquals("scenariotostart", arguments.getStartScenario());
		assertEquals("grouptostart", arguments.getStartGroup());
		assertTrue(arguments.isNoAutostart());
	}

	@Test
	public void testCommandlineArgumentsInvalidFile() throws Exception {
		try {
			new CommandlineArguments(new String[] {
					BaseConstants.MINUS + Constants.CmdArguments.PROPERTY_FILE,
					"somenonexistingfile"
			});
		} catch (FileNotFoundException e) {
			TestHelpers.assertContains(e, "somenonexistingfile", "Unable to find file");
		}
	}
}
