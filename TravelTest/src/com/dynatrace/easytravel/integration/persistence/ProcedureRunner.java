/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: ProcedureRunner.java
 * @date: 21.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.integration.persistence;

import java.io.File;

import com.dynatrace.easytravel.constants.BaseConstants;


/**
 *
 * @author stefan.moschinski
 */
public final class ProcedureRunner {

	private static final String DIST_DIR = "../Distribution/dist";

	public static void configure() {
		System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, new File(DIST_DIR).getAbsolutePath());
	}

	private ProcedureRunner() {
		// not intended for instantiation
	}
}
