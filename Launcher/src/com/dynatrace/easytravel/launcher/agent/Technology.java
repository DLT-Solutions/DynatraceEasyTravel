package com.dynatrace.easytravel.launcher.agent;

import static com.dynatrace.easytravel.launcher.misc.Constants.Procedures.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.dynatrace.easytravel.constants.BaseConstants;


public enum Technology {

    JAVA("Java", "dtagent", "libdtagent", CUSTOMER_FRONTEND_ID, BUSINESS_BACKEND_ID),
    DOTNET_20("DotNet", "dtnetagent20", "", PAYMENT_BACKEND_ID, B2B_FRONTEND_ID),
    ADK("ADK", JAVA.agentNameWindows, JAVA.agentNameLinux, CREDIT_CARD_AUTH_UNIT_ID),
    WEBSERVER("WEBSERVER", "dtagent", "libdtagent", APACHE_HTTPD_ID),
    NGINX("NGINX", "dtagent", "libdtagent", NGINX_WEBSERVER_ID),
    WEBPHPSERVER("WEBPHPSERVER", "dtagent", "libdtagent", APACHE_HTTPD_PHP_ID),
    WEBPHPSERVERWIN("WEBPHPSERVER", "dtagent", "libdtagent", APACHE_HTTPD_PHP_ID),
	MYSQL("MySQL", BaseConstants.NONE, BaseConstants.NONE, INPROCESS_MYSQL_ID),
	MONGODB("MongoDB", BaseConstants.NONE, BaseConstants.NONE, MONGO_DB_ID),
	COUCHDB("CouchDB", BaseConstants.NONE, BaseConstants.NONE, COUCHDB_ID),
	HBASE("HBase", BaseConstants.NONE, BaseConstants.NONE, HBASE_ID),
	VAGRANT("Vagrant",BaseConstants.NONE, BaseConstants.NONE, VAGRANT_ID);

    private final String agentNameWindows;
    private final String agentNameLinux;
    private final Set<String> procedureIds;
	private String name;


	private Technology(String name, String agentNameWindows, String agentNameLinux, String...procedureIds) {
    	this.name = name;
        this.agentNameWindows = agentNameWindows;
        this.agentNameLinux = agentNameLinux;
        if (procedureIds == null || procedureIds.length == 0) {
        	this.procedureIds = Collections.emptySet();
        } else {
        	this.procedureIds = new HashSet<String>(Arrays.asList(procedureIds));
        }
    }

    public String getAgentName(OperatingSystem operatingSystem) {
        switch (operatingSystem) { // NOPMD
            case LINUX:
            case MAC_OS:
            case AIX:
            case FREE_BSD:
            case HP_UX:
            case IRIX:
            case SOLARIS:
            case UNIX:
                return agentNameLinux;
            case WINDOWS:
            case UNKNOWN:
            default:
                return agentNameWindows;
        }
    }

    public boolean belongToProcedureId(String procedureId) {
    	return procedureIds.contains(procedureId);
    }

	public String getName() {
		return name;
	}

}
