package com.dynatrace.easytravel.frontend.beans;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class AmpWebsiteBeanTest {
	
	private static final String SAAS_JAVASCRIPTAG = "	<!--Dynatrace amp-analytics-->" + BaseConstants.CRLF
			+ "	<amp-analytics type=\"dynatrace\">" + BaseConstants.CRLF
			+ "		<script type=\"application/json\">" + BaseConstants.CRLF
			+ "			{" + BaseConstants.CRLF
			+ "				\"vars\": {" + BaseConstants.CRLF
			+ "					\"app\": \"ampApplicationID\"," + BaseConstants.CRLF
			+ "					\"protocol\": \"https\"," + BaseConstants.CRLF
			+ "					\"environment\": \"bf-sprint.dynatracelabs.com\"," + BaseConstants.CRLF
			+ "					\"port\": \"443\"," + BaseConstants.CRLF
			+ "					\"tenant\": \"bf-tenant\"" + BaseConstants.CRLF
			+ "				}" + BaseConstants.CRLF
			+ "			}" + BaseConstants.CRLF
			+ "		</script>" + BaseConstants.CRLF
			+ "	</amp-analytics>"+BaseConstants.CRLF;
	
	private static final String MANAGED_JAVASCRIPTAG = "	<!--Dynatrace amp-analytics-->" + BaseConstants.CRLF
			+ "	<amp-analytics type=\"dynatrace\">" + BaseConstants.CRLF
			+ "		<script type=\"application/json\">" + BaseConstants.CRLF
			+ "			{" + BaseConstants.CRLF
			+ "				\"vars\": {" + BaseConstants.CRLF
			+ "					\"app\": \"ampApplicationID\"," + BaseConstants.CRLF
			+ "					\"protocol\": \"https\"," + BaseConstants.CRLF
			+ "					\"environment\": \"server.dynatrace-managed.com\"," + BaseConstants.CRLF
			+ "					\"port\": \"8443\"," + BaseConstants.CRLF
			+ "					\"separator\": \"\","+BaseConstants.CRLF
			+ "					\"tenantpath\": \"9d604a71-31cd-43c2-93bb-76cbf66b2e8c\"" + BaseConstants.CRLF
			+ "				}" + BaseConstants.CRLF
			+ "			}" + BaseConstants.CRLF
			+ "		</script>" + BaseConstants.CRLF
			+ "	</amp-analytics>"+BaseConstants.CRLF;
	
	@Test
	public void getJavaScriptTagTest() {
		EasyTravelConfig config = EasyTravelConfig.read();
		try {
			config.ampApplicationID="";
			config.ampBfTenant="";
			AmpWebsiteBean bean = new AmpWebsiteBean();
			assertTrue("Unexpected JavaScriptTag. Information messege expected.", bean.getJavaScriptTag().equals(AmpWebsiteBean.INFO));
		
			config.ampApplicationID = "ampApplicationID";
			config.ampBfProtocol = "https";
			config.ampBfEnvironment = "bf-sprint.dynatracelabs.com";
			config.ampBfPort = "443";
			config.ampBfTenant = "bf-tenant";
			bean = new AmpWebsiteBean();
			assertTrue("Unexpected JavaScriptTag for SaaS Tenant.", bean.getJavaScriptTag().equals(SAAS_JAVASCRIPTAG));
			
			config.ampBfEnvironment = "server.dynatrace-managed.com";
			config.ampBfPort = "8443";
			config.ampBfTenant = "9d604a71-31cd-43c2-93bb-76cbf66b2e8c";
			bean = new AmpWebsiteBean();
			assertTrue("Unexpected JavaScriptTag for Managed Cluster.", bean.getJavaScriptTag().equals(MANAGED_JAVASCRIPTAG));
			
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}
}
