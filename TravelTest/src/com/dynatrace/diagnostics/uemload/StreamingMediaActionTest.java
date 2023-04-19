/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: JavaScriptAgentTest.java
 * @date: 01.03.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.util.DtVersionDetector;


/**
 *
 * @author cwat-moehler
 */
public class StreamingMediaActionTest {

	@Test
	public void testToString(){
		StreamingMediaAction action = new StreamingMediaAction();
		DtVersionDetector.enforceInstallationType(InstallationType.Classic);


		long currentTime = System.currentTimeMillis();
		String mediaType = StreamingMediaAction.MEDIATYPE_AUDIO;
		String source = "http://www.dynatrace.com/audio.mp3";
		int duration = 50;
		int maxPlayTime = 50;
		int playTime = 100;
		int bufferingCount = 1;
		int bufferingTime = 100;
		boolean userTriggered = true;
		boolean watchedCompletely = false;

		action.setMediaType(mediaType);
		action.setSource(source);
		action.setUserTriggered(userTriggered);
		action.setWatchedUntilEnd(watchedCompletely);
		action.setDuration(duration);
		action.setEventTime(currentTime);
		action.setMaxPlayTime(maxPlayTime);
		action.setPlayTime(playTime);
		action.setBufferingCount(bufferingCount);
		action.setBufferingTime(bufferingTime);

		String actionName = source + "|" + duration + "|" + (userTriggered ? "1" : "0") + "|" + (watchedCompletely ? "1" : "0") + "|" + maxPlayTime + "|" + playTime + "|" + bufferingCount + "|" + bufferingTime;

		assertEquals("1|0|" + encodeAction(actionName) + "|" + mediaType + "|-|" + currentTime + "|" + currentTime + "|19",
				action.toString(0));

		currentTime = System.currentTimeMillis();
		mediaType = StreamingMediaAction.MEDIATYPE_VIDEO;
		source = "http://www.dynatrace.com/video.webm";
		duration = 55;
		maxPlayTime = 55;
		playTime = 10;
		bufferingCount = 5;
		bufferingTime = 100;
		userTriggered = false;
		watchedCompletely = true;

		action = new StreamingMediaAction();
		action.setMediaType(mediaType);
		action.setSource(source);
		action.setUserTriggered(userTriggered);
		action.setWatchedUntilEnd(watchedCompletely);
		action.setDuration(duration);
		action.setEventTime(currentTime);
		action.setMaxPlayTime(maxPlayTime);
		action.setPlayTime(playTime);
		action.setBufferingCount(bufferingCount);
		action.setBufferingTime(bufferingTime);

		actionName = source + "|" + duration + "|" + (userTriggered ? "1" : "0") + "|" + (watchedCompletely ? "1" : "0") + "|" + maxPlayTime + "|" + playTime + "|" + bufferingCount + "|" + bufferingTime;

		assertEquals("1|0|" + encodeAction(actionName) + "|" + mediaType + "|-|" + currentTime + "|" + currentTime + "|19",
					action.toString(0));
	}

	private String encodeAction(String str){
		return str.replace("^", "^^").replace("|","^p").replace(",","^c");
	}

	/**
	 * Test that the Streaming media helper returns an action object and
	 * that the host parameter is set correctly.
	 */
	@Test
	public void testStreamingMediaActionHelper() {

		StreamingMediaAction action = StreamingMediaActionHelper.generateRandomMediaAction("http://www.dynatrace.com");

		assertNotNull(action);
		assertTrue(action.getSource().contains("http://www.dynatrace.com"));

	}


}
