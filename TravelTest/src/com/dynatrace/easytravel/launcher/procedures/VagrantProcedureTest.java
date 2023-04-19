package com.dynatrace.easytravel.launcher.procedures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.builder.SettingBuilder;
import com.dynatrace.easytravel.launcher.vagrant.VagrantBoxType;
import com.dynatrace.easytravel.launcher.vagrant.VagrantProcedure;
import com.dynatrace.easytravel.launcher.vagrant.VagrantWrapper;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPResponseRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.NanoHTTPD.Response;

@RunWith(MockitoJUnitRunner.class)
public class VagrantProcedureTest {
	
	final String vagrantStopped = "Current machine states: default stopped (aws) The EC2 instance is stopped. Run `vagrant up` to start it.";
	final String vagrantStopping = "Current machine states: default stopping (aws) The EC2 instance is stopping. Wait until is completely stopped to run `vagrant up` and start it.";
	final String vagrantPending = "Current machine states: default pending (aws) The EC2 instance is still being initialized. To destroy this machine, you can run `vagrant destroy`.";
	final String vagrantNotCreated = "Current machine states: default not created (aws) The EC2 instance is not created. Run `vagrant up` to create it.";
	final String vagrantRunning = "Current machine states: default running (aws) The EC2 instance is running. To stop this machine, you can run `vagrant halt`. To destroy the machine, you can run `vagrant destroy`.";
	
	@After
	public void revertConfig(){
		EasyTravelConfig.resetSingleton();
	}

	@Mock
	public VagrantWrapper vWrapper;
	
	@Test
	public void testStates() throws Exception{
		DefaultProcedureMapping mapping = new DefaultProcedureMapping("vagrant");
		mapping.addSetting(SettingBuilder.config("config.vagrantWorkingDir").value(System.getProperty("user.dir")).create());
		VagrantProcedure vagrantProc = new VagrantProcedure(mapping);
		vagrantProc.setVagrantWrapper(vWrapper);
		
		when(vWrapper.status()).thenReturn(vagrantRunning);
		when(vWrapper.halt()).thenReturn(vagrantStopping);
		
		assertTrue(vagrantProc.isOperating());
		
		when(vWrapper.status()).thenReturn(vagrantStopped);
		
		assertEquals("There were problems stopping vagrant procedure.", Feedback.Success, vagrantProc.stop());
	}
	
	@Test
	public void testStatesWithUrlCheck() throws Exception{
		DefaultProcedureMapping mapping = new DefaultProcedureMapping("vagrant");
		mapping.addSetting(SettingBuilder.config("config.vagrantWorkingDir").value(System.getProperty("user.dir")).create());
		
		MockRESTServer mockVagrantBox = new MockRESTServer(new HTTPResponseRunnable(){
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "OK.");				
			}			
		});
		
		mapping.addSetting(SettingBuilder.config("config.vagrantBoxUrl").value("http://localhost:" + mockVagrantBox.getPort()).create());
		
		VagrantProcedure vagrantProc = new VagrantProcedure(mapping);
		vagrantProc.setVagrantWrapper(vWrapper);
		
		when(vWrapper.status()).thenReturn(vagrantRunning);
		when(vWrapper.halt()).thenReturn(vagrantStopping);
		
		assertTrue(vagrantProc.isOperating());
		
		when(vWrapper.status()).thenReturn(vagrantStopped);
		
		assertEquals("There were problems stopping vagrant procedure.", Feedback.Success, vagrantProc.stop());
	}
	

	@Test
	public void testProcedureName() throws Exception{
		List<String> testNames = Arrays.asList("dummy", "/home/dummy", "c:\\mock\\dummy");
		
		for(String name : testNames){
			DefaultProcedureMapping mapping = new DefaultProcedureMapping("vagrant");
			mapping.addSetting(SettingBuilder.config("config.vagrantWorkingDir").value(name).create());
			
			VagrantProcedure vagrantProc = new VagrantProcedure(mapping);
			assertEquals("Vagrant (dummy)",vagrantProc.getName());		
		}	
		
		/*
		 * Test real vagrantProcedureName config
		 */
		
		DefaultProcedureMapping mapping = new DefaultProcedureMapping("vagrant");
		mapping.addSetting(SettingBuilder.config("config.vagrantProcedureName").value("dummy").create());
		
		VagrantProcedure vagrantProc = new VagrantProcedure(mapping);
		assertEquals("Vagrant (dummy)",vagrantProc.getName());		
	}
	
	@Test
	public void testAddingToDetails() throws Exception{
		DefaultProcedureMapping mapping = new DefaultProcedureMapping("vagrant");
		
		VagrantProcedure vagrantProc = new VagrantProcedure(mapping);
		
		vagrantProc.setVagrantWrapper(vWrapper);
		when(vWrapper.up()).thenReturn("{CLASSPATH}OK");
		when(vWrapper.sshConfig()).thenReturn("{CLASSPATH}OK");
		
		vagrantProc.runVagrant();
		
		System.out.println(vagrantProc.getDetails());
		assertEquals("There were problems getting details.", "OK\nOK\n",vagrantProc.getDetails());
		
	}
	

	@Test
	public void testVagrantProcedureType() throws Exception{
		
		List<String> types = Arrays.asList("frontend", "backend", "", "none", "othertext");
		for(String type : types){
			DefaultProcedureMapping mapping = new DefaultProcedureMapping("vagrant");
			mapping.addSetting(SettingBuilder.config("config.vagrantBoxType").value(type).create());
			
			VagrantProcedure vagrantProc = new VagrantProcedure(mapping);
			
			VagrantBoxType vagrantBoxType = vagrantProc.getVagrantBoxType();
			
			if(type.equals("frontend")){
				assertVagrantProcedureType(vagrantBoxType,VagrantBoxType.CUSTOMER_FRONTEND);
			} else if(type.equals("backend")){
				assertVagrantProcedureType(vagrantBoxType,VagrantBoxType.BUSINESS_BACKEND);
			} else {
				assertVagrantProcedureType(vagrantBoxType,VagrantBoxType.BUSINESS_BACKEND);
			}	
		}

	}
	
	private void assertVagrantProcedureType(VagrantBoxType actualType, VagrantBoxType expectedType){
		assertTrue("Vagrant Box Type mismatch! Type was: "+actualType, actualType.equals(expectedType));
	}

	@Test
	public void testWrapper() throws Exception {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping("vagrant");
		vWrapper = new VagrantWrapper("vagrant", "C:\\Users\\kasper.kulikowski\\pluginsenvironments\\easytravel_elastic", mapping);
		System.out.println(parseIpFromOutput(vWrapper.sshConfig()));
		
	}
	
	private String parseIpFromOutput(String output){
		String outputLines[] = output.split("\\r?\\n");
		
		for(String line : outputLines){
			if(StringUtils.containsIgnoreCase(line,"hostname")){
				String[] splitedLine = line.split("\\s+");
				int lastIndex = splitedLine.length-1;
				
				return splitedLine[lastIndex]; 
			}
		}
		return StringUtils.EMPTY;
	}
	
}
