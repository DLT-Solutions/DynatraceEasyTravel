package com.dynatrace.easytravel.frontend.beans;


/**
 * Runnable called from the asynchronous servlet AdsForBlog returning randomly
 * selected journeys.
 *
 * cwpl-mpankows
 */

public class AdsBean extends RecommendationBean implements Runnable {

	// This method defines the URL prefix, which is context dependent.
	@Override
	protected String urlPrefix() {
		return "/img/"; // for the blog page context
	}
}
