package com.dynatrace.easytravel.frontend.beans;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.util.DynatraceUrlUtils;
import com.google.common.base.Strings;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class AmpWebsiteBean {
	
	private static String protocol;
	private static String tenant;
	private static String host;
	private static String port;
	private static String app;
	
	public static final String INFO = "	<!--Here would be placed JavaScript Tag for Dynatrace amp-analytics."
			+ BaseConstants.CRLF + "			To enable Dynatrace AMP monitoring set proper configs.-->";
	
	public String getJavaScriptTag() {
		if(isConfigSet()) {
			StringBuilder sb = new StringBuilder()
				.append("	<!--Dynatrace amp-analytics-->").append(BaseConstants.CRLF)
				.append("	<amp-analytics type=\"dynatrace\">").append(BaseConstants.CRLF)
				.append("		<script type=\"application/json\">").append(BaseConstants.CRLF)
				.append("			{").append(BaseConstants.CRLF)
				.append("				\"vars\": {").append(BaseConstants.CRLF)
				.append("					\"app\": \"").append(app).append("\",").append(BaseConstants.CRLF)
				.append("					\"protocol\": \"").append(protocol).append("\",").append(BaseConstants.CRLF)
				.append("					\"environment\": \"").append(host).append("\",").append(BaseConstants.CRLF)
				.append("					\"port\": \"").append(port).append("\",").append(BaseConstants.CRLF);
			if(DynatraceUrlUtils.isManagedOrLocalTenant(tenant)) {
				sb.append("					\"separator\": \"\",").append(BaseConstants.CRLF)
					.append("					\"tenantpath\": \"").append(tenant).append("\"").append(BaseConstants.CRLF);
			} else {
				sb.append("					\"tenant\": \"").append(tenant).append("\"").append(BaseConstants.CRLF);
			}
			sb.append("				}").append(BaseConstants.CRLF)
				.append("			}").append(BaseConstants.CRLF)
				.append("		</script>").append(BaseConstants.CRLF)
				.append("	</amp-analytics>").append(BaseConstants.CRLF);
			return sb.toString();
		} else {
			return INFO;
		}
	}
	
	private static boolean isConfigSet() {
		EasyTravelConfig config = EasyTravelConfig.read();
		protocol = StringUtils.trim(config.ampBfProtocol);
		tenant = StringUtils.trim(config.ampBfTenant);
		host = StringUtils.trim(config.ampBfEnvironment);
		port = StringUtils.trim(config.ampBfPort);
		app = StringUtils.trim(config.ampApplicationID);
		boolean isEmpty = Strings.isNullOrEmpty(protocol) || Strings.isNullOrEmpty(tenant)
				|| Strings.isNullOrEmpty(host) || Strings.isNullOrEmpty(port) || Strings.isNullOrEmpty(app);
		return !isEmpty;
	}

}
