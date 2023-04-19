/**
 *
 */
package com.dynatrace.easytravel.launcher.engine;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Strings;

/**
 * @author tomasz.wieremjewicz
 * @date 25 sty 2018
 *
 */
public class DatabaseEnvArgs {
	public static final String ET_CCA_DB_NAME = "ET_CCA_DB_NAME";
	public static final String ET_CCA_DB_VENDOR = "ET_CCA_DB_VENDOR";
	public static final String ET_CCA_DB_CHANNEL_TYPE = "ET_CCA_DB_CHANNEL_TYPE";
	public static final String ET_CCA_DB_CHANNEL_ENDPOINT = "ET_CCA_DB_CHANNEL_ENDPOINT";

	public enum ChannelType {
		OTHER,
		TCP_IP,
		UNIX_DOMAIN_SOCKET,
		NAMED_PIPE,
		IN_PROCESS;
    }

	public enum Vendor {
		DB2("DB2"),
		ORACLE("Oracle"),
		SQLSERVER("SQL Server"),
		MYSQL("MySQL"),
		DERBY_CLIENT("Derby Client"),
		POSTGRESQL("PostgreSQL");

		private final String preciseName;

		private Vendor(String value) {
			preciseName = value;
		}

		public String getPreciseName() {
			return preciseName;
		};
	}

	public String name;
	public String vendor;
	public String channelType;
	public String channelEndpoint;

	public DatabaseEnvArgs(String databaseUrl) {
		if (Strings.isNullOrEmpty(databaseUrl)) {
			return;
		}
		else if (StringUtils.containsIgnoreCase(databaseUrl, "jdbc:db2")) {
			name = databaseUrl.substring(databaseUrl.lastIndexOf('/') + 1);
			vendor = Vendor.DB2.getPreciseName();
			channelType = ChannelType.TCP_IP.name();
			channelEndpoint = databaseUrl.substring(databaseUrl.indexOf("//") + 2, databaseUrl.lastIndexOf('/'));
		}
		else if (StringUtils.containsIgnoreCase(databaseUrl, "jdbc:oracle")) {
			if (databaseUrl.lastIndexOf(':') > databaseUrl.lastIndexOf('/')) {
				name = databaseUrl.substring(databaseUrl.lastIndexOf(':') + 1);
				channelEndpoint = databaseUrl.substring(databaseUrl.indexOf('@') + 1, databaseUrl.lastIndexOf(':'));
			}
			else {
				name = databaseUrl.substring(databaseUrl.lastIndexOf('/') + 1);
				channelEndpoint = databaseUrl.substring(databaseUrl.indexOf("@//") + 3, databaseUrl.lastIndexOf('/'));
			}
			vendor = Vendor.ORACLE.getPreciseName();
			channelType = ChannelType.TCP_IP.name();
		}
		else if (StringUtils.containsIgnoreCase(databaseUrl, "jdbc:sqlserver") || StringUtils.containsIgnoreCase(databaseUrl, "jdbc:jtds:sqlserver")) {
			if (databaseUrl.lastIndexOf("databaseName=") >= 0) {
				name = databaseUrl.substring(databaseUrl.lastIndexOf('=') + 1);
				channelEndpoint = databaseUrl.substring(databaseUrl.indexOf("//") + 2, databaseUrl.lastIndexOf(';'));
			}
			else if (databaseUrl.lastIndexOf("database=") >= 0) {
				name = databaseUrl.substring(databaseUrl.lastIndexOf("database=") + 9);
				name = name.substring(0, name.indexOf(';'));
				channelEndpoint = databaseUrl.substring(databaseUrl.indexOf("//") + 2);
				channelEndpoint = channelEndpoint.substring(0,  channelEndpoint.indexOf(';'));
			}
			else {
				int tmp = databaseUrl.lastIndexOf(';');
				name = tmp == -1 ? databaseUrl.substring(databaseUrl.lastIndexOf('/') + 1) : databaseUrl.substring(databaseUrl.lastIndexOf('/') + 1, tmp);
				channelEndpoint = databaseUrl.substring(databaseUrl.indexOf("//") + 2, databaseUrl.lastIndexOf('/'));
			}
			vendor = Vendor.SQLSERVER.getPreciseName();
			channelType = ChannelType.TCP_IP.name();
		}
		else if (StringUtils.containsIgnoreCase(databaseUrl, "jdbc:mysql")) {
			name = databaseUrl.substring(databaseUrl.lastIndexOf('/') + 1);
			vendor = Vendor.MYSQL.getPreciseName();
			channelType = ChannelType.TCP_IP.name();
			channelEndpoint = databaseUrl.substring(databaseUrl.indexOf("//") + 2, databaseUrl.lastIndexOf('/'));
		}
		else if (StringUtils.containsIgnoreCase(databaseUrl, "jdbc:derby")) {
			int tmp = databaseUrl.lastIndexOf(';');
			name = tmp == -1 ? databaseUrl.substring(databaseUrl.lastIndexOf('/') + 1) : databaseUrl.substring(databaseUrl.lastIndexOf('/') + 1, tmp);
			vendor = Vendor.DERBY_CLIENT.getPreciseName();
			channelType = ChannelType.TCP_IP.name();
			channelEndpoint = databaseUrl.substring(databaseUrl.indexOf("//") + 2, databaseUrl.lastIndexOf('/'));
		}
		else if (StringUtils.containsIgnoreCase(databaseUrl, "jdbc:postgresql")) {
			name = databaseUrl.substring(databaseUrl.lastIndexOf('/') + 1);
			vendor = Vendor.POSTGRESQL.getPreciseName();
			channelType = ChannelType.TCP_IP.name();
			channelEndpoint = databaseUrl.substring(databaseUrl.indexOf("//") + 2, databaseUrl.lastIndexOf('/'));
		}
		else {
			return;
		}

		channelEndpoint = channelEndpoint.replace("\\", "");
	}

	public boolean isEmptyOrNull() {
		return Strings.isNullOrEmpty(name) && Strings.isNullOrEmpty(vendor) && Strings.isNullOrEmpty(channelEndpoint) && Strings.isNullOrEmpty(channelType);
	}
}
