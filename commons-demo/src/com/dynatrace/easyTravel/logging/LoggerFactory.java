package com.dynatrace.easytravel.logging;

import java.io.IOException;

import org.slf4j.bridge.SLF4JBridgeHandler;

import com.dynatrace.easytravel.config.Directories;

import ch.qos.logback.classic.Logger;

/**
 * Taken with explicit allowance from Dr Heinz M. Kabutz on 04.04.2007 from
 * http://www.javaspecialists.co.za/archive/newsletter.do?issue=137:
 *
 * Dear Reinhold,
 * please go ahead - at your own risk though :-) Please acknowledge the source.
 * Be aware that a IoC framework like Spring can give you incorrect results with the loggers.
 * Thanks for asking :-) und viele Gruesse aus Kreta
 * Regards
 * Heinz
 * --
 * Dr Heinz M. Kabutz (PhD CompSci)
 * Author of "The Java(tm) Specialists' Newsletter"
 * Sun Java Champion
 * http://www.javaspecialists.eu
 * Tel: +30 69 72 850 460
 * Skype: kabutz
 */
public class LoggerFactory {

	protected LoggerFactory() {
		// Hide c'tor to avoid instantiation
	}

	public static Logger make() {
		Throwable t = new Throwable();
		StackTraceElement directCaller = t.getStackTrace()[1];
		return (Logger)org.slf4j.LoggerFactory.getLogger(directCaller.getClassName());
	}

	// call this method at the very first after main
	public static void initLogging() throws IOException {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		System.setProperty("logback.configurationFile", Directories.getResourcesDir()+"\\logback.xml");
	}
}