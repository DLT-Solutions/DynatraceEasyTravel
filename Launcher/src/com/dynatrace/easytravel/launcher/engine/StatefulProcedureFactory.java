package com.dynatrace.easytravel.launcher.engine;

import com.dynatrace.easytravel.launcher.misc.Constants;


public class StatefulProcedureFactory {

	// TODO@(stefan.moschinski): rework visibility of the constructors
	public static StatefulProcedure newInstance(Procedure procedure) {
		if(Constants.Procedures.APACHE_HTTPD_ID.equals(procedure.getMapping().getId()) ||
				Constants.Procedures.APACHE_HTTPD_PHP_ID.equals(procedure.getMapping().getId())) {
			return new FailureAwareStatefulProcedure(procedure);
		}
		return new StatefulProcedure(procedure);
	}

}
