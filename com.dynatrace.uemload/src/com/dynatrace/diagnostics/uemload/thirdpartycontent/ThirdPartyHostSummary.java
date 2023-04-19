/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: ThirdPartyHostSummary.java
 * @date: 11.01.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload.thirdpartycontent;




/**
 *
 *
 * @author peter.lang
 */
public class ThirdPartyHostSummary {

	private String hostname;

	private int imagesSuccessul = 0;
	private int imagesFailed = 0;
	private int imagesInterrupted = 0;

	private IntervalSet imageDuration = new IntervalSet();        // format of interval string in monitor signal: 150_249_1764_1764
	private StatCounter imageLoadStatistics = new StatCounter();

	private IntervalSet scriptDuration  = new IntervalSet();
	private StatCounter scriptLoadStatistics = new StatCounter();

	private IntervalSet customDuration = new IntervalSet();
	private StatCounter customLoadStatistics = new StatCounter();
	
	private IntervalSet cssDuration = new IntervalSet();
	private StatCounter cssLoadStatistics = new StatCounter();
	
	private IntervalSet otherDuration = new IntervalSet();
	private StatCounter otherLoadStatistics = new StatCounter();

	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * @param hostname the hostname to set
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}


	/**
	 * @return the imagesSuccessul
	 */
	public int getImagesSuccessul() {
		return imagesSuccessul;
	}


	/**
	 * @param imagesSuccessul the imagesSuccessul to set
	 */
	public void setImagesSuccessul(int imagesSuccessul) {
		this.imagesSuccessul = imagesSuccessul;
	}


	/**
	 * @return the imagesFailed
	 */
	public int getImagesFailed() {
		return imagesFailed;
	}


	/**
	 * @param imagesFailed the imagesFailed to set
	 */
	public void setImagesFailed(int imagesFailed) {
		this.imagesFailed = imagesFailed;
	}


	/**
	 * @return the imagesInterrupted
	 */
	public int getImagesInterrupted() {
		return imagesInterrupted;
	}


	/**
	 * @param imagesInterrupted the imagesInterrupted to set
	 */
	public void setImagesInterrupted(int imagesInterrupted) {
		this.imagesInterrupted = imagesInterrupted;
	}


	/**
	 * @return the imageDuration
	 */
	public IntervalSet getImageDuration() {
		return imageDuration;
	}


	/**
	 * @param imageDuration the imageDuration to set
	 */
	public void setImageDuration(IntervalSet imageDuration) {
		this.imageDuration = imageDuration;
	}


	/**
	 * @return the imageLoadStatistics
	 */
	public StatCounter getImageLoadStatistics() {
		return imageLoadStatistics;
	}


	/**
	 * @param imageLoadStatistics the imageLoadStatistics to set
	 */
	public void setImageLoadStatistics(StatCounter imageLoadStatistics) {
		this.imageLoadStatistics = imageLoadStatistics;
	}


	/**
	 * @return the scriptDuration
	 */
	public IntervalSet getScriptDuration() {
		return scriptDuration;
	}


	/**
	 * @param scriptDuration the scriptDuration to set
	 */
	public void setScriptDuration(IntervalSet scriptDuration) {
		this.scriptDuration = scriptDuration;
	}


	/**
	 * @return the scriptLoadStatistics
	 */
	public StatCounter getScriptLoadStatistics() {
		return scriptLoadStatistics;
	}


	/**
	 * @param scriptLoadStatistics the scriptLoadStatistics to set
	 */
	public void setScriptLoadStatistics(StatCounter scriptLoadStatistics) {
		this.scriptLoadStatistics = scriptLoadStatistics;
	}


	/**
	 * @return the customDuration
	 */
	public IntervalSet getCustomDuration() {
		return customDuration;
	}


	/**
	 * @param customDuration the customDuration to set
	 */
	public void setCustomDuration(IntervalSet customDuration) {
		this.customDuration = customDuration;
	}


	/**
	 * @return the customLoadStatistics
	 */
	public StatCounter getCustomLoadStatistics() {
		return customLoadStatistics;
	}
	
	/**
	 * @return the cssLoadStatistics
	 */
	public StatCounter getCssLoadStatistics() {
		return cssLoadStatistics;
	}
		
	/**
	 * 
	 * @return
	 */
	public IntervalSet getCssDuration() {
		return cssDuration;
	}
	
	/**
	 * 
	 * @param cssDuration
	 */
	public void setCssDuration(IntervalSet cssDuration) {
		this.cssDuration = cssDuration;
	}
	
	/**
	 * 
	 * @param cssLoadStatistics
	 */
	public void setCssLoadStatistics(StatCounter cssLoadStatistics) {
		this.cssLoadStatistics = cssLoadStatistics;
	}
	
	/**
	 * 
	 * @param otherLoadStatistics
	 */
	public void setOtherLoadStatistics(StatCounter otherLoadStatistics) {
		this.otherLoadStatistics = otherLoadStatistics;
	}

	/**
	 * 
	 * @return
	 */
	public IntervalSet getOtherDuration() {
		return otherDuration;
	}
	
	/**
	 * 
	 * @param otherDuration
	 */
	public void setOtherDuration(IntervalSet otherDuration) {
		this.otherDuration = otherDuration;
	}
	
	/**
	 * @return the otherLoadStatistics
	 */
	public StatCounter getOtherLoadStatistics() {
		return otherLoadStatistics;
	}


	/**
	 * @param customLoadStatistics the customLoadStatistics to set
	 */
	public void setCustomLoadStatistics(StatCounter customLoadStatistics) {
		this.customLoadStatistics = customLoadStatistics;
	}

	/**
	 * counts successful or failed images depending on the statusCode passed.
	 *
	 * @param statusCode http response code for loading of image
	 * @param startOffset start time of interval for loaded custom resource
	 * @param endOffset end time of interval for loaded resource
	 * @author peter.lang
	 */
	public void addImageSample(int statusCode, long startOffset, long endOffset) {
		imageDuration.add(startOffset, endOffset);
		imageLoadStatistics.add(endOffset-startOffset);

		if (statusCode == 404 || (statusCode >= 500 && statusCode<=599)) {
			imagesFailed++;
		} else {
			imagesSuccessul++;
		}

	}

	/**
	 * Add a loaded script resource interval to summary information.
	 *
	 * @param startOffset start time of interval for loaded custom resource
	 * @param endOffset end time of interval for loaded resource
	 * @author peter.lang
	 */
	public void addScriptSample(long startOffset, long endOffset) {
		scriptLoadStatistics.add(endOffset-startOffset);
		scriptDuration.add(startOffset, endOffset);
	}


	/**
	 * Add a loaded custom resource interval to summary information.
	 *
	 * @param startOffset start time of interval for loaded custom resource
	 * @param endOffset end time of interval for loaded resource
	 * @author peter.lang
	 */
	public void addCustomSample(long startOffset, long endOffset) {
		customLoadStatistics.add(endOffset-startOffset);
		customDuration.add(startOffset, endOffset);
	}
	
	/**
	 * Add a loaded css resource interval to summary information.
	 *
	 * @param startOffset start time of interval for loaded css resource
	 * @param endOffset end time of interval for loaded resource
	 * @author peter.lang
	 */
	public void addCssSample(long startOffset, long endOffset) {
		cssLoadStatistics.add(endOffset-startOffset);
		cssDuration.add(startOffset, endOffset);
	}
	
	/**
	 * Add a loaded other resource interval to summary information.
	 *
	 * @param startOffset start time of interval for loaded other resource
	 * @param endOffset end time of interval for loaded resource
	 * @author peter.lang
	 */
	public void addOtherSample(long startOffset, long endOffset) {
		otherLoadStatistics.add(endOffset-startOffset);
		otherDuration.add(startOffset, endOffset);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ThirdPartyHostSummary [hostname=");
		builder.append(hostname);
		builder.append(", imagesSuccessul=");
		builder.append(imagesSuccessul);
		builder.append(", imagesFailed=");
		builder.append(imagesFailed);
		builder.append(", imagesInterrupted=");
		builder.append(imagesInterrupted);
		builder.append(", imageDuration=");
		builder.append(imageDuration);
		builder.append(", imageLoadStatistics=");
		builder.append(imageLoadStatistics);
		builder.append(", scriptDuration=");
		builder.append(scriptDuration);
		builder.append(", scriptLoadStatistics=");
		builder.append(scriptLoadStatistics);
		builder.append(", cssDuration=");
		builder.append(cssDuration);
		builder.append(", cssLoadStatistics=");
		builder.append(cssLoadStatistics);
		builder.append(", otherDuration=");
		builder.append(otherDuration);
		builder.append(", otherLoadStatistics=");
		builder.append(otherLoadStatistics);
		builder.append(", customDuration=");
		builder.append(customDuration);
		builder.append(", customLoadStatistics=");
		builder.append(customLoadStatistics);
		builder.append("]");
		return builder.toString();
	}


	public long getFirstStartOffset() {
		double result = Long.MAX_VALUE;
		if (imageLoadStatistics.getCount()>0) {
			result = Math.min(result, imageDuration.getMinStart());
		}
		if (scriptLoadStatistics.getCount()>0) {
			result = Math.min(result, scriptDuration.getMinStart());
		}
		if (customLoadStatistics.getCount()>0) {
			result = Math.min(result, customDuration.getMinStart());
		}
		return (long) result;
	}

}
