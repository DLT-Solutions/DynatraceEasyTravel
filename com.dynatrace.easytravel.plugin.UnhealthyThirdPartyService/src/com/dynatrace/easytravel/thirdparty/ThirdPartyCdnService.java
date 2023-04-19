package com.dynatrace.easytravel.thirdparty;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ThirdPartyCdnService {

	private static final int IMAGE_COUNT = 51;
	private static final String IMAGE_DIR = "image";
	private static final String[] AVAILABLE_IMAGES = { "cdn_image_1.png", "cdn_image_2.png", "cdn_image_3.png",
			"cdn_image_4.png", "cdn_image_5.png" };
	private static final String IMAGE_PLUGIN_SCRIPT = "jquery.cycle.lite.js";
	private static final String IMAGE_SLIDESHOW_SCRIPT = "image_slideshow.js";

	private static final int SCRIPT_COUNT = 3;
	private static final String SCRIPT_DIR = "script";
	private static final String SCRIPT_FILENAME = "cdn_script.js";

	private static final String REDIRECT_FILTER = "resource-redirect";

	private String cdnUrl;
	private boolean caching;
	private boolean outage;
	private boolean limited;

	private List<URI> imageUris;
	private List<URI> scriptUris;

	public ThirdPartyCdnService(String url, boolean outage, boolean limited, boolean caching) {
		this.cdnUrl = url;
		this.caching = caching;
		this.outage = outage;
		this.limited = limited;

		this.imageUris = prepareImageUris();
		this.scriptUris = prepareScriptUris();
	}

	private List<URI> prepareImageUris() {
		List<URI> uriList = new ArrayList<URI>();

		for (int i = 0; i < IMAGE_COUNT; i++) {
			URI uri = prepareImagePathFor(i);
			uriList.add(uri);
		}

		return uriList;
	}

	private List<URI> prepareScriptUris() {
		List<URI> uriList = new ArrayList<URI>();

		for (int i = 1; i <= SCRIPT_COUNT; i++) {
			URI uri = prepareScriptUriFor(i);
			uriList.add(uri);
		}

		URI imagePluginScriptUri = UriUtil.buildUri(this.cdnUrl, SCRIPT_DIR, IMAGE_PLUGIN_SCRIPT, this.caching, this.outage,
				this.limited);
		uriList.add(imagePluginScriptUri);

		URI imageSlideshowScript = UriUtil.buildUri(this.cdnUrl, SCRIPT_DIR, IMAGE_SLIDESHOW_SCRIPT, this.caching, this.outage,
				this.limited);
		uriList.add(imageSlideshowScript);

		return uriList;
	}


	private URI prepareImagePathFor(int idx) {
		String directory = REDIRECT_FILTER + "/" + idx + "/" + IMAGE_DIR;
		int imageIdx = idx % AVAILABLE_IMAGES.length;
		String imageFilename = AVAILABLE_IMAGES[imageIdx];
		URI uri = UriUtil.buildUri(this.cdnUrl, directory, imageFilename, this.caching, this.outage, this.limited);
		return uri;
	}

	private URI prepareScriptUriFor(int idx) {
		String directory = REDIRECT_FILTER + "/" + idx + "/" + SCRIPT_DIR;
		URI uri = UriUtil.buildUri(this.cdnUrl, directory, SCRIPT_FILENAME, this.caching, this.outage, this.limited);
		return uri;
	}

	public List<URI> getImageUris() {
		return imageUris;
	}

	public List<URI> getScriptUris() {
		return scriptUris;
	}

}
