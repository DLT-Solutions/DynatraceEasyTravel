package com.dynatrace.easytravel.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class MySqlVcapService extends AbstractVcapService {
	
	private final Map<String, String> requiredProperties = new HashMap<>();
	
	public MySqlVcapService(String namePattern) {
		Objects.requireNonNull(namePattern);
		this.requiredProperties.put(SERVICE_PROPERTY_NAME, namePattern);
		this.requiredProperties.put(SERVICE_PROPERTY_LABEL, "p.mysql");
	}

	@Override
	public String getJDBCDriverClass() {
		return "com.mysql.jdbc.Driver";
	}
	
	@Override
	public Map<String, String> getRequiredProperties() {
		return requiredProperties;
	}
	
	public static void main(String[] args) {
		parseEnv("easyTravel-Business*");
	}
	
	public static boolean parseEnv(String namePattern) {
		Objects.requireNonNull(namePattern);
		MySqlVcapService main = new MySqlVcapService(namePattern);
		return main.parse();
	}

}
