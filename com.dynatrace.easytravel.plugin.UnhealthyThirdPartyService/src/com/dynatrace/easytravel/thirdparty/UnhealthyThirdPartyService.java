package com.dynatrace.easytravel.thirdparty;

import java.net.URI;
import java.util.List;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractPagePlugin;

/**
 * This plugin manages loading third party services which can either be healthy (delivers
 * resources within 'normal' time), unhealthy (delivers resources with delay) or completely
 * unavailable (does not deliver resources at all)
 *
 * The following services are available: - CDN - Social Media
 *
 * @author cwat-sreiting
 */
public class UnhealthyThirdPartyService extends AbstractPagePlugin {

	private static final EasyTravelConfig CONFIG = EasyTravelConfig.read();
	private static final Logger log = LoggerFactory.make();

	private static final String HTML_IMAGE_TAG = "<img src=\"%s\" style=\"margin-right: 9px;\"/>";
	private static final String HTML_SCRIPT_TAG = "<script src=\"%s\" type=\"text/javascript\"></script>";

	private boolean cachingEnabled;

	private boolean cdnEnabled = false;
	private boolean cdnOutage = false;
	private boolean cdnLimited = false;
	private boolean cdnPropertiesDirty = false;
	private ThirdPartyCdnService cdnService;

	private boolean socialMediaEnabled = false;
	private boolean socialMediaOutage = false;
	private boolean socialMediaLimited = false;
	private boolean socialMediaPropertiesDirty = false;
	private ThirdPartySocialMediaService socialMediaService;

	public UnhealthyThirdPartyService() {
		this.cachingEnabled = CONFIG.thirdpartyCaching;
	}

	@Override
	public String getHeader() {
		StringBuilder sb = new StringBuilder();

		if (this.cdnEnabled) {
			if (cdnService == null || this.cdnPropertiesDirty) {
				this.cdnService = new ThirdPartyCdnService(CONFIG.thirdpartyCdnUrl, this.cdnOutage, this.cdnLimited, this.cachingEnabled);
				this.cdnPropertiesDirty = false;
			}

			sb.append(buildHtmlScriptSnippet(this.cdnService.getScriptUris()));
		}

		return sb.toString();
	}


	@Override
	public String getFooter() {
		StringBuilder sb = new StringBuilder();

		if (this.socialMediaEnabled) {
			if (socialMediaService == null || this.socialMediaPropertiesDirty) {
				this.socialMediaService = new ThirdPartySocialMediaService(CONFIG.thirdpartySocialMediaUrl,
						this.socialMediaOutage, this.socialMediaLimited, this.cachingEnabled);
				this.socialMediaPropertiesDirty = false;
			}
			sb.append(this.socialMediaService.getHtmlSnippet());
		}

		if (this.cdnEnabled) {
			if (this.cdnService == null || this.cdnPropertiesDirty) {
				this.cdnService = new ThirdPartyCdnService(CONFIG.thirdpartyCdnUrl, this.cdnOutage, this.cdnLimited, this.cachingEnabled);
				this.cdnPropertiesDirty = false;
			}
			sb.append(buildHtmlImageSnippet(this.cdnService.getImageUris()));
		}

		return sb.toString();
	}


	/**
	 * create HTML snippet which creates an <image> tag for each given URI
	 *
	 * @param uriList list of URIs
	 * @return HTML snippet
	 */
	private static String buildHtmlImageSnippet(List<URI> uriList) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div style=\"width: 867px; height: 210px; margin: 10px 0; overflow: hidden;\">");
		sb.append("<div id=\"cdnSlideshow\" >");
		sb.append("<div>");


		for (int i = 0; i < uriList.size(); i++) {
			if (i != 0 && i % 3 == 0) {
				sb.append("</div>");
				sb.append("<div>");
			}

			URI uri = uriList.get(i);
			sb.append(String.format(HTML_IMAGE_TAG, uri.toString()));
		}

		sb.append("</div>");
		sb.append("</div>");
		sb.append("</div>");
		return sb.toString();
	}


	/**
	 * create HTML snippet which creates a <script> tag for each given URI
	 *
	 * @param uriList list of URIs
	 * @return HTML snippet
	 */
	private static String buildHtmlScriptSnippet(List<URI> uriList) {
		StringBuilder sb = new StringBuilder();
		for (URI uri : uriList) {
			sb.append(String.format(HTML_SCRIPT_TAG, uri.toString()));
		}
		return sb.toString();
	}


	// setters needed for IoC by ctx.xml

	public void setCdnOutage(boolean outage) {
		log.info(String.format("Third Party CDN service outage %s", outage ? "enabled" : "disabled"));
		this.cdnOutage = outage;
		this.cdnPropertiesDirty = true;
	}

	public void setCdnLimited(boolean limited) {
		log.info(String.format("Third Party CDN service limitation %s", limited ? "enabled" : "disabled"));
		this.cdnLimited = limited;
		this.cdnPropertiesDirty = true;
	}

	public void setSocialMediaOutage(boolean outage) {
		log.info(String.format("Third Party Social Media service outage %s", outage ? "enabled" : "disabled"));
		this.socialMediaOutage = outage;
		this.socialMediaPropertiesDirty = true;
	}

	public void setSocialMediaLimited(boolean limited) {
		log.info(String.format("Third Party Social Media service limitation %s", limited ? "enabled" : "disabled"));
		this.socialMediaLimited = limited;
		this.socialMediaPropertiesDirty = true;
	}

	public void setCdnEnabled(boolean enabled) {
		log.info(String.format("Third Party CDN service %s", enabled ? "enabled" : "disabled"));
		this.cdnEnabled = enabled;
		this.cdnPropertiesDirty = true;
	}

	public void setSocialMediaEnabled(boolean enabled) {
		log.info(String.format("Third Party Social Media service %s", enabled ? "enabled" : "disabled"));
		this.socialMediaEnabled = enabled;
		this.socialMediaPropertiesDirty = true;
	}

}
