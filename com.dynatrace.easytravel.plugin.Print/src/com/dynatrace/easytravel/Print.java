package com.dynatrace.easytravel;

import java.util.Arrays;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

import ch.qos.logback.classic.Logger;

public class Print extends AbstractGenericPlugin  {

    private static Logger log = LoggerFactory.make();

	@Override
	public Object doExecute(String location, Object... context) {
		log.info("-------------- PrintPlugin: Had extension point: " + location + ", context: " + Arrays.toString(context));
		return null;
	}
}
