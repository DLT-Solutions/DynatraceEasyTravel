package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dynatrace.diagnostics.uemload.Browser;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.constants.BaseConstants;



public class B2BPage extends EasyTravelPage {

	private static final boolean NOT_LOAD_DYNATRACE_RESOURCES = false;
	private B2BSession session;
	//private EtPageType page;

	//private boolean logOnNeeded;

	public B2BPage(EtPageType page, B2BSession session) {
		super(page, session, NOT_LOAD_DYNATRACE_RESOURCES);
		this.session = session;
		//this.page = page;
	}

	protected void loadPage(Browser browser, UEMLoadCallback pageLoadCallback) throws IOException {
		super.loadPage(browser, getUrl(), pageLoadCallback);
	}


	public String getRandomLink(String regex) {
		String html = session.getHtml();
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(html);
		List<String> links = new ArrayList<String>();
		while(matcher.find()) {
			links.add(matcher.group());
		}
		String randomLink = links.get(UemLoadUtils.randomInt(links.size()));
		while(randomLink.startsWith(BaseConstants.FSLASH)) {
			randomLink = randomLink.substring(1);
		}
		String absolutLink = session.getHost() + randomLink;
		return  absolutLink;
	}

}
