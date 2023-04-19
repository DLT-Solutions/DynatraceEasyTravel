package com.dynatrace.easytravel.launcher.iis;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.easytravel.config.Directories;

public class IISExpressSetupTest {
	
	private static final String EASYTRAVEL_INSTALL_PATH = Directories.getInstallDir().getAbsolutePath();
	private static final String EASYTRAVEL_CONFIG_PATH = Directories.getConfigDir().getAbsolutePath();
	
	@BeforeClass
	public static void setup() throws IOException {		
		File src = new File(EASYTRAVEL_INSTALL_PATH, "../commons-demo/resources/iisexpresstemplate.config");
		File dest = new File(EASYTRAVEL_INSTALL_PATH, "/resources");
		FileUtils.copyFileToDirectory(src, dest);
	}
	
	@AfterClass
	public static void teardown() throws IOException {
		FileUtils.deleteDirectory(new File(EASYTRAVEL_INSTALL_PATH, "/resources"));
		FileUtils.deleteQuietly(new File(EASYTRAVEL_CONFIG_PATH, IISExpressConfigs.PAYMENT_BACKEND_CONFIG.getName()));
		FileUtils.deleteQuietly(new File(EASYTRAVEL_CONFIG_PATH, IISExpressConfigs.B2B_FRONTEND_CONFIG.getName()));
	}
	
	@Test
	public void checkPaymentBackendConfig() throws IOException {
		new IISExpressSetup(IISExpressConfigs.PAYMENT_BACKEND_CONFIG, 9010).generateConfig();
		File result = new File(Directories.getConfigDir(), IISExpressConfigs.PAYMENT_BACKEND_CONFIG.getName());
		List<String> resultOutput = FileUtils.readLines(result, "UTF-8");
		Assert.assertTrue(resultOutput.contains("            <site name=\"PaymentService Web Service\" id=\"1\" serverAutoStart=\"true\">"));
		int index = resultOutput.indexOf("            <site name=\"PaymentService Web Service\" id=\"1\" serverAutoStart=\"true\">");
		String path = resultOutput.get(index+2);
		Assert.assertTrue(path.matches("                    <virtualDirectory path=\"/\" physicalPath=\"(.*)dotNET(.*)dotNetPaymentBackend\" />"));
		Assert.assertTrue(resultOutput.contains("                    <binding protocol=\"http\" bindingInformation=\":9010:localhost\" />"));
		Assert.assertTrue(resultOutput.contains("                <add name=\"Server-Details\" value=\"IIS-Express\" />"));
	}
	
	@Test
	public void checkB2BFrontendConfig() throws IOException {
		new IISExpressSetup(IISExpressConfigs.B2B_FRONTEND_CONFIG, 9000).generateConfig();
		File result = new File(Directories.getConfigDir(), IISExpressConfigs.B2B_FRONTEND_CONFIG.getName());
		List<String> resultOutput = FileUtils.readLines(result, "UTF-8");
		Assert.assertTrue(resultOutput.contains("            <site name=\"easyTravel B2B Site\" id=\"1\" serverAutoStart=\"true\">"));
		int index = resultOutput.indexOf("            <site name=\"easyTravel B2B Site\" id=\"1\" serverAutoStart=\"true\">");
		String path = resultOutput.get(index+2);
		Assert.assertTrue(path.matches("                    <virtualDirectory path=\"/\" physicalPath=\"(.*)dotNET(.*)dotNetB2BFrontend\" />"));
		Assert.assertTrue(resultOutput.contains("                    <binding protocol=\"http\" bindingInformation=\":9000:localhost\" />"));
		Assert.assertTrue(resultOutput.contains("                <add name=\"Server-Details\" value=\"IIS-Express\" />"));
	}
	
	

}
