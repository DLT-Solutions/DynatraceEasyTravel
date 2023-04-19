package com.dynatrace.diagnostics.uemload;

import java.util.Random;

import com.dynatrace.diagnostics.uemload.utils.UemLoadUrlUtils;

/**
 * Helper class to generate actions for streaming medias.
 *
 * While there is a certain degree of randomness to the generated actions
 * the actual distribution will mimick some commonly seen patterns of
 * streaming media ussage on websites.
 *
 * @author cwat-moehler
 */
public class StreamingMediaActionHelper {

	private static final Random randomNumberGenerator = new Random();

	private static final int percentageOfVideoActions = 78;
	private static final int percentageOfWatchedCompletely = 62;

	private static final int[] distributionOfVideoFilesInPercent = { 45, 10, 10, 25, 10 };
	private static final int[] distributionOfAudioFilesInPercent = { 35, 25, 40 };

	// [0] => no buffering
	// [1] => a little buffering
	// [2] => some buffering
	// [3] => a lot of buffering
	private static final int[] distributionOfBufferingForVideo = {40, 30, 20, 10};
	private static final int[] distributionOfBufferingForAudio = {90, 8, 2, 0};

	private static final String[] videoFilenames = {
		"video/autoplayvid.ogg",
		"video/popularvid1.webm",
		"video/popularvid2.webm",
		"video/interestingvid%n.mp4",
		"video/unpopularvid%n.flv"
	};

	private static final int[][] videoFileDuration = {
		{ 169 },
		{ 251 },
		{ 98 },
		{ 126, 163, 147, 40, 194},
		{ 203, 93, 88, 65, 250, 50, 36, 103, 121, 143, 106, 178, 162, 287, 260, 225, 230, 284, 252,	47 }
	};

	private static final String[] audioFilenames = {
		"audio/autoplayaudio1.oga",
		"audio/autoplayaudio2.mp3",
		"audio/audioclip%n.webm"
	};

	private static final int[][] audioFileDuration = {
		{ 38 },
		{ 317 },
		{ 432, 130, 209, 344, 405, 320, 192, 85, 30, 18, 162, 488, 493, 299, 188, 428,	183, 60, 485, 498 }
	};

	public static StreamingMediaAction generateRandomMediaAction(String host)
	{
		// NB - the order of the methods is important, don't just rearrange them
		StreamingMediaAction action = new StreamingMediaAction();
		action.setEventTime(System.currentTimeMillis());
		setMediaType(action);
		setMediaUrl(action, host);
		setPlayTime(action);
		setWatchedCompletely(action);
		setAutoPlay(action);
		setBufferingInformation(action);
		return action;
	}

	private static void setMediaType(StreamingMediaAction action) {
		if (getNextRandom() >= percentageOfVideoActions) {
			action.setMediaType(StreamingMediaAction.MEDIATYPE_AUDIO);
		} else {
			action.setMediaType(StreamingMediaAction.MEDIATYPE_VIDEO);
		}
	}

	private static void setMediaUrl(StreamingMediaAction action, String host) {
		int[] distributionOfFilesInPercent = distributionOfVideoFilesInPercent;
		String[] filenames = videoFilenames;
		int[][] durations = videoFileDuration;
		if (StreamingMediaAction.MEDIATYPE_AUDIO.equals((action.getMediaType()))) {
			distributionOfFilesInPercent = distributionOfAudioFilesInPercent;
			filenames = audioFilenames;
			durations = audioFileDuration;
		}

		int accumulatedProbability = 0;
		int random = getNextRandom();
		for (int i = 0; i < distributionOfFilesInPercent.length; i++) {
			accumulatedProbability += distributionOfFilesInPercent[i];
			if (random <= accumulatedProbability) {
				String filename = filenames[i];
				int duration = durations[i][0];
				if (filename.contains("%n")) {
					if (i == 3) {
						int randomNumberForFileName = getNextRandom(1, durations[i].length);
						filename = filename.replace("%n", Integer.toString(randomNumberForFileName));
						duration = durations[i][randomNumberForFileName];
					} else {
						int randomNumberForFileName = getNextRandom(1, durations[i].length);
						filename = filename.replace("%n", Integer.toString(randomNumberForFileName));
						duration = durations[i][randomNumberForFileName];
					}
				}
				action.setSource(UemLoadUrlUtils.getUrl(host, filename));
				action.setDuration(duration);
				break;
			}
		}
	}

	private static void setPlayTime(StreamingMediaAction action) {
		float ratio = (float) getNextRandom(10, 200) / 100;
		action.setPlayTime((long) (action.getDuration() * ratio));
		action.setMaxPlayTime(getNextRandom((int) action.getDuration() / 3, (int) action.getDuration()));
	}

	private static void setAutoPlay(StreamingMediaAction action) {
		if (action.getSource().contains("autoplay")) {
			action.setUserTriggered(false);
		} else {
			action.setUserTriggered(true);
		}
	}

	private static void setWatchedCompletely(StreamingMediaAction action) {
		if (getNextRandom(100) <= percentageOfWatchedCompletely) {
			action.setWatchedUntilEnd(true);
			action.setMaxPlayTime(action.getDuration());
		} else {
			action.setWatchedUntilEnd(false);
		}
	}

	private static void setBufferingInformation(StreamingMediaAction action) {
		int[] distributionOfBuffering = distributionOfBufferingForVideo;
		if (action.isAudioAction()) {
			distributionOfBuffering = distributionOfBufferingForAudio;
		}

		int random = getNextRandom();
		if (random < distributionOfBuffering[0]) {
			// no buffering
			action.setBufferingCount(0);
			action.setBufferingTime(0);
		} else if (random < (distributionOfBuffering[0] + distributionOfBuffering[1])) {
			// a little buffering
			action.setBufferingCount(getNextRandom(1, 3));
			action.setBufferingTime(action.getBufferingCount() * getNextRandom(2, 5));
		} else if (random < (distributionOfBuffering[0] + distributionOfBuffering[1] + distributionOfBuffering[2])) {
			// some buffering
			action.setBufferingCount(getNextRandom(3, 6));
			action.setBufferingTime(action.getBufferingCount() * getNextRandom(3, 10));
		} else {
			// a lot of buffering
			action.setBufferingCount(getNextRandom(6, 15));
			action.setBufferingTime(action.getBufferingCount() * getNextRandom(10, 30));
	    }
	}

	private static int getNextRandom() {
		return getNextRandom(100);
	}

	private static int getNextRandom(int min, int max) {
		return randomNumberGenerator.nextInt(max - min) + min;
	}

	private static int getNextRandom(int max) {
		return getNextRandom(1, max);
	}

	/**
	 * returns true at approximately every third call
	 * @return
	 */
	public static boolean shouldStreamingMediaActionBeAdded() {
		return getNextRandom(1, 3) == 1;
	}

}
