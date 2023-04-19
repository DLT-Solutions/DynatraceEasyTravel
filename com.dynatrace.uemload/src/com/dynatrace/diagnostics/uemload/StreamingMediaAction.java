package com.dynatrace.diagnostics.uemload;



/**
 * This class holds the information measured from a streaming media
 * object (HTML5, Flash, ...)
 * Similar to  production EuStreamingMediaNodeAttachment
 *
 * @author cwat-moehler
 */
public class StreamingMediaAction {

	private static final String SEPERATOR = "|";
	private String mediaType;
	private String source;
	private long duration;
	private boolean userTriggered;
	private boolean watchedUntilEnd;
	private long maxPlayTime;
	private long playTime;
	private long bufferingCount;
	private long bufferingTime;
	private long eventTime;

	public static final String MEDIATYPE_AUDIO = "_audio_";
	public static final String MEDIATYPE_VIDEO = "_video_";

	public String getMediaType() {
		return mediaType;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public boolean isUserTriggered() {
		return userTriggered;
	}

	public void setUserTriggered(boolean userTriggered) {
		this.userTriggered = userTriggered;
	}

	public boolean isWatchedUntilEnd() {
		return watchedUntilEnd;
	}

	public void setWatchedUntilEnd(boolean watchedUntilEnd) {
		this.watchedUntilEnd = watchedUntilEnd;
	}

	public long getMaxPlayTime() {
		return maxPlayTime;
	}

	public void setMaxPlayTime(long maxPlayTime) {
		this.maxPlayTime = maxPlayTime;
	}

	public long getPlayTime() {
		return playTime;
	}

	public void setPlayTime(long playTime) {
		this.playTime = playTime;
	}

	public long getBufferingCount() {
		return bufferingCount;
	}

	public void setBufferingCount(long bufferingCount) {
		this.bufferingCount = bufferingCount;
	}

	public long getBufferingTime() {
		return bufferingTime;
	}

	public void setBufferingTime(long bufferingTime) {
		this.bufferingTime = bufferingTime;
	}

	public long getEventTime() {
		return eventTime;
	}

	public void setEventTime(long eventTime) {
		this.eventTime = eventTime;
	}

	public boolean isAudioAction() {
		return MEDIATYPE_AUDIO.equals(getMediaType());
	}

	public boolean isVideoAction() {
		return MEDIATYPE_VIDEO.equals(getMediaType());
	}

	private String getActionName() {

		StringBuilder builder = new StringBuilder();
		builder.append(source);
		builder.append(SEPERATOR);
		builder.append(duration);
		builder.append(SEPERATOR);
		builder.append(userTriggered ? "1" : "0");
		builder.append(SEPERATOR);
		builder.append(watchedUntilEnd ? "1" : "0");
		builder.append(SEPERATOR);
		builder.append(maxPlayTime);
		builder.append(SEPERATOR);
		builder.append(playTime);
		builder.append(SEPERATOR);
		builder.append(bufferingCount);
		builder.append(SEPERATOR);
		builder.append(bufferingTime);

		return builder.toString();
	}

	public String toString(int actionId) {

		StringBuilder builder = new StringBuilder();
		builder.append("1"); // depth
		if (JavaScriptAgent.shouldSendActionWithActionId()) {
			builder.append(SEPERATOR);
			builder.append(actionId);
		} // actionId
		builder.append(SEPERATOR);
		builder.append(JavaScriptAgent.actionEscape(getActionName())); // name
		if (!JavaScriptAgent.shouldSendActionWithActionId()) {
			builder.append(SEPERATOR + "-"); //
		}
		builder.append(SEPERATOR);
		builder.append(mediaType); // type
		if (JavaScriptAgent.shouldSendActionWithActionInfo()) {
			builder.append(SEPERATOR + "-");
		}
		builder.append(SEPERATOR);
		builder.append(eventTime); // start
		builder.append(SEPERATOR);
		builder.append(eventTime); // stop
		builder.append(SEPERATOR + "19"); // dom nodes

		return builder.toString();
	}
}
