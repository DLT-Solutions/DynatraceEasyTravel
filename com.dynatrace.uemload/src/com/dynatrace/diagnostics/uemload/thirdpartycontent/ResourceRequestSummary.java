/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: ResourceRequestSummary.java
 * @date: 11.01.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload.thirdpartycontent;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.logging.Logger;


/**
 * Summary Object to hold timing information about loaded resources.
 *
 * @author peter.lang
 */
public class ResourceRequestSummary {
	private static final Logger LOGGER = Logger.getLogger(ResourceRequestSummary.class.getName());
	private static final Random random = new Random(System.currentTimeMillis());

	private String resourceUrl = "";
	private long loadstart = -1;
	private long loadfinished = -1;
	private int statusCode;
	private String contentType = "";
	private boolean loadedFromCache = false;
	private int responseSize = 0;
	private int responseHeadersSize = 0;

	public ResourceRequestSummary() {
		// intentionally left empty
	}

	public ResourceRequestSummary(String resourceUrl, long loadStart, long loadfinished, int statuscode, String contentType) {
		this.resourceUrl = resourceUrl;
		this.loadstart = loadStart;
		this.loadfinished = loadfinished;
		this.statusCode = statuscode;
		this.contentType = contentType;
	}

	/**
	 * @return the resourceUrl
	 */
	public String getResourceUrl() {
		return resourceUrl;
	}

	/**
	 * @param resourceUrl the resourceUrl to set
	 */
	public void setResourceUrl(String resourceUrl) {
		this.resourceUrl = resourceUrl;
	}

	/**
	 * @return the loadstart
	 */
	public long getLoadstart() {
		return loadstart;
	}

	/**
	 * @param loadstart the loadstart to set
	 */
	public void setLoadstart(long loadstart) {
		this.loadstart = loadstart;
	}

	/**
	 * @return the loadfinished, actual timing of resource loaded or a duration of 10-50ms (randomly chosen)
	 */
	public long getLoadfinished() {
		if (loadfinished < loadstart) {
			return loadstart + (10 + (int)(40*random.nextFloat()));
		}
		return loadfinished;
	}

	/**
	 * @param loadfinished the loadfinished to set
	 */
	public void setLoadfinished(long loadfinished) {
		this.loadfinished = loadfinished;
	}
	
	public int getResponseSize() {
		return responseSize;
	}

	public void setResponseSize(int responseSize) {
		this.responseSize = responseSize;
	}
	
	public int getResponseHeadersSize() {
		return responseHeadersSize;
	}

	public void setResponseHeadersSize(int responseHeadersSize) {
		this.responseHeadersSize = responseHeadersSize;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ResourceRequestSummary [resourceUrl=");
		builder.append(resourceUrl);
		builder.append(", loadstart=");
		builder.append(loadstart);
		builder.append(", loadfinished=");
		builder.append(loadfinished);
		builder.append(", statuscode=");
		builder.append(statusCode);
		builder.append(", mimetype=");
		builder.append(contentType);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return the mimeType
	 */
	public String getMimeType() {
		return contentType;
	}

	/**
	 * @param mimeType the mimeType to set
	 */
	public void setMimeType(String mimeType) {
		this.contentType = mimeType;
	}

	/**
	 *
	 * @param statusCode
	 * @author peter.lang
	 */
	public void setStatusCode(int httpResponseStatusCode) {
		this.statusCode = httpResponseStatusCode;
	}

	/**
	 * @return the statusCode
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 *
	 * @return hostname of {@link #resourceUrl} or empty string.
	 * @author peter.lang
	 */
	public String getHostname() {
		if (resourceUrl == null) {
			return "";
		}
		try {
			URL url = new URL(resourceUrl);
			return url.getHost();
		} catch (MalformedURLException e) {
			return "";
		}
	}

	/**
	 * @return the loadedFromCache
	 */
	public boolean isLoadedFromCache() {
		return loadedFromCache;
	}


	/**
	 * @param loadedFromCache the loadedFromCache to set
	 */
	public void setLoadedFromCache(boolean loadedFromCache) {
		this.loadedFromCache = loadedFromCache;
	}

	/**
	 *
	 * @param currentPage
	 * @return
	 * @author peter.lang
	 */
	public boolean isThirdPartyResource(String pageUrl) {
		try {
			URL page = new URL(pageUrl);
			return !page.getHost().equals(getHostname());
		} catch (MalformedURLException e) {
			LOGGER.warning("MalformedURLException catched: " + e.getMessage());
			return false;
		}
	}
	
	public long getLoadDuration(){
		return this.getLoadfinished() - this.getLoadstart();
	}
	
	/**
	 * determines if the resource is an image element
	 * 
	 * @return boolean true if image
	 * @author cwat-shauser
	 */
	public boolean isImage(){
		if(this.getMimeType() == null) return false;
		return this.getMimeType().toLowerCase().contains("image");
	}

}
