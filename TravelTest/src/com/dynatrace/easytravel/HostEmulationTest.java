package com.dynatrace.easytravel;

import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.dynatrace.easytravel.launcher.agent.OperatingSystem;

/**
 * @author stefan.moschinski
 */
public class HostEmulationTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Test
	public void test32BitHostAgentIsReturned() throws Exception {
		tempFolder.newFolder("lib64");
		tempFolder.newFile("lib64/" + getHostAgentName());
		File agent64 = tempFolder.newFile("lib64/dtagent.dll");
		
		tempFolder.newFolder("lib");
		File host32bitAgent = tempFolder.newFile("lib/" + getHostAgentName());
		
		
		HostEmulation hostEmulation = new HostEmulation();
		assertThat(hostEmulation.lookUpHostAgent(agent64.getAbsolutePath()), Matchers.is(host32bitAgent));
	}
	
	@Test
	public void test32BitOnly() throws Exception {
		tempFolder.newFolder("lib");
		File host32 = tempFolder.newFile("lib/" + getHostAgentName());
		File agent32 = tempFolder.newFile("lib/dtagent.dll");
		
		HostEmulation hostEmulation = new HostEmulation();
		assertThat(hostEmulation.lookUpHostAgent(agent32.getAbsolutePath()), Matchers.is(host32));
	}
	
	@Test
	public void test64BitHostAgentIsReturnedIf32BitAgentCannotBeFound() throws Exception {
		tempFolder.newFolder("lib64");
		File hostAgent64Bit = tempFolder.newFile("lib64/" + getHostAgentName());
		File agent64 = tempFolder.newFile("lib64/dtagent.dll");
		
		tempFolder.newFolder("lib");
				
		HostEmulation hostEmulation = new HostEmulation();
		assertThat(hostEmulation.lookUpHostAgent(agent64.getAbsolutePath()), Matchers.is(hostAgent64Bit));
	}
	
	@Test
	public void test64BitHostAgentIsReturnedIf32BitDirectoryCannotBeFound() throws Exception {
		tempFolder.newFolder("lib64");
		File hostAgent64Bit = tempFolder.newFile("lib64/" + getHostAgentName());
		File agent64 = tempFolder.newFile("lib64/dtagent.dll");
				
		HostEmulation hostEmulation = new HostEmulation();
		assertThat(hostEmulation.lookUpHostAgent(agent64.getAbsolutePath()), Matchers.is(hostAgent64Bit));
	}

	@Test
	public void testGetHostNameForMultiplePatterns() throws Exception {
		List<String> hostNameTemplates = Arrays.asList("windows", "linux");
		
		assertThat(HostEmulation.getHostName(hostNameTemplates, 0), Matchers.is("windows.001"));
		assertThat(HostEmulation.getHostName(hostNameTemplates, 1), Matchers.is("linux.002"));
		assertThat(HostEmulation.getHostName(hostNameTemplates, 2), Matchers.is("windows.003"));
		assertThat(HostEmulation.getHostName(hostNameTemplates, 3), Matchers.is("linux.004"));
	}
	
	@Test
	public void testGetHostNameForSinglePattern() throws Exception {
		List<String> hostNameTemplates = Arrays.asList("windows");
		
		assertThat(HostEmulation.getHostName(hostNameTemplates, 0), Matchers.is("windows.001"));
		assertThat(HostEmulation.getHostName(hostNameTemplates, 1), Matchers.is("windows.002"));
		assertThat(HostEmulation.getHostName(hostNameTemplates, 2), Matchers.is("windows.003"));
	}
	
	private static final String getHostAgentName() {
		return OperatingSystem.IS_WINDOWS ? "dthostagent.exe" : "dthostagent"; 
	}
	
}
