package com.dynatrace.easytravel;

import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

/**
 * @author Rafal.Psciuk
 *
 */
public class LoadChangePlugin extends AbstractGenericPlugin {

	@Override
	protected Object doExecute(String location, Object... context) {
		// All logic is in uemload project class: com.dynatrace.diagnostics.uemload.LoadChange
		return null;
	}

}
