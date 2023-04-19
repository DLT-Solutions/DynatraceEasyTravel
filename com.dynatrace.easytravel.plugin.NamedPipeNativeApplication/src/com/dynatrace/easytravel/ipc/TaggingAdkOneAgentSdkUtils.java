package com.dynatrace.easytravel.ipc;

import com.dynatrace.adk.DynaTraceADKFactory;
import com.dynatrace.adk.Tagging;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.oneagent.sdk.OneAgentSDKFactory;
import com.dynatrace.oneagent.sdk.api.OneAgentSDK;

import ch.qos.logback.classic.Logger;

/**
 * 
 * @author Michal.Bakula
 *
 */
public class TaggingAdkOneAgentSdkUtils {
	
	private static final Logger log = LoggerFactory.make();
	
	private static boolean isTaggingAdkActive;
	private static boolean isOneAgentSdkActive;
	private static Tagging tagging;
	private static OneAgentSDK oneAgentSdk;
	static {
		DynaTraceADKFactory.initialize();
		tagging = DynaTraceADKFactory.createTagging();
		isTaggingAdkActive = TaggingAdkOneAgentSdkUtils.checkTaggingAdkAVailability();
		if (!isTaggingAdkActive) {
			oneAgentSdk = OneAgentSDKFactory.createInstance();
			isOneAgentSdkActive = TaggingAdkOneAgentSdkUtils.checkOneAgentSdkAvailability();
		} else {
			isOneAgentSdkActive = false;
		}
	}

	private static boolean checkTaggingAdkAVailability() {
		if (tagging instanceof com.dynatrace.adk.impl.DummyTaggingImpl) {
			log.info("Initializing Tagging ADK failed. Possible reasons: no agent, incompatible agent, OneAgent for Dynatrace.");
			return false;
		}
		return true;
	}
	
	private static boolean checkOneAgentSdkAvailability() {
		switch (oneAgentSdk.getCurrentState()) {
		case ACTIVE:
			log.info("SDK is active and capturing.");
			return true;
		case PERMANENTLY_INACTIVE:
			log.warn("SDK is PERMANENTLY_INACTIVE; Probably no agent injected or agent is incompatible with SDK.");
			return false;
		case TEMPORARILY_INACTIVE:
			log.warn("SDK is TEMPORARILY_INACTIVE; Agent has been deactived - check agent configuration.");
			return false;
		default:
			log.warn("SDK is in unknown state.");
			return false;
		}
	}
	
	public static boolean isTaggingAdkActive() {
		return isTaggingAdkActive;
	}
	
	public static boolean isOneAgentSdkActive() {
		return isOneAgentSdkActive;
	}
	
	public static Tagging getTaggingAdkInstance() {
		return tagging;
	}
	
	public static OneAgentSDK getOneAgentAdkInstance() {
		return oneAgentSdk;
	}
}
