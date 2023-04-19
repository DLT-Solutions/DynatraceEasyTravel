package com.dynatrace.easytravel;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.util.TextUtils;

/**
 * This plugin adds third party advertisments to easyTravel page.
 *
 * Plugin returns a url to a third party banner image to use within the easyTravel
 * customer frontend page.
 *
 * @author peter.lang
 *
 */
public class ThirdPartyAdvertisements extends AbstractGenericPlugin {

	private static final String[] availableImages = {"easyTravel_banner.png", "road1.png", "winter.jpeg"};

	private static final Random rnd = new Random(System.currentTimeMillis());

	private String imagePrefix = "";

	@Override
	protected Object doExecute(String location, Object... context) {
		if (location.equals(PluginConstants.FRONTEND_THIRDPARTYADSERVER)) {
			if (context[0] instanceof AtomicReference<?>) {
				@SuppressWarnings("unchecked")
				AtomicReference<String> urls = (AtomicReference<String>) context[0];

				final EasyTravelConfig CONFIG = EasyTravelConfig.read();

				StringBuilder sb = new StringBuilder();
				sb.append(TextUtils.appendTrailingSlash(CONFIG.thirdpartyUrl));
				sb.append(TextUtils.appendTrailingSlash(imagePrefix));
				int index = rnd.nextInt(availableImages.length);
				sb.append(availableImages[index]);

				String imgStr = TextUtils.merge(BaseConstants.Images.THIRDPARTY_ADVERTISMENT_IMAGE_TEMPLATE, sb.toString());				
				urls.set(imgStr);
			}
		}
		return null;
	}



	/**
	 * @return the imagePrefix
	 */
	public String getImagePrefix() {
		return imagePrefix;
	}

	/**
	 * @param imagePrefix the imagePrefix to set
	 */
	public void setImagePrefix(String imagePrefix) {
		this.imagePrefix = imagePrefix;
	}

}
