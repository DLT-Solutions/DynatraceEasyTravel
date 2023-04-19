package com.dynatrace.easytravel.launcher.war;

import org.apache.commons.lang3.StringUtils;


import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.procedures.DatabaseContentCreationProcedure;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureSetting;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.persistence.PersistenceMode;


public class DatabaseContentCreator {
	
	static private String persistenceMode;
	
	public static void main(String[] args) {
		
		if(StringUtils.isNotBlank(args[0]) && PersistenceMode.isAllowedPersistenceMode(args[0])){
			persistenceMode=args[0];
		}
		else{
			/*
			 * Use default configuration
			 */
			persistenceMode="mongodb";
		}
		ProcedureMapping mapping = new DefaultProcedureMapping(
				Constants.Procedures.DATABASE_CONTENT_CREATOR_ID);
		mapping.addSetting(new DefaultProcedureSetting(Constants.Misc.SETTING_PERSISTENCE_MODE, persistenceMode));
		
		DatabaseContentCreationProcedure contentCreator = new DatabaseContentCreationProcedure(mapping);
		contentCreator.run();
	}
	

}
