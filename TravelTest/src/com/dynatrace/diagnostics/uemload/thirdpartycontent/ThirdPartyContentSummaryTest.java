/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: ThirdPartyContentSummaryTest.java
 * @date: 11.01.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload.thirdpartycontent;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import org.junit.Test;


/**
 *
 * @author peter.lang
 */
public class ThirdPartyContentSummaryTest {

	@Test
	public void testSimplePageLoadDetails() {

		List<ResourceRequestSummary> resourseList = new ArrayList<ResourceRequestSummary>();
		resourseList.add(new ResourceRequestSummary("http://localhost:8080/img/facebookbutton.png", 1326288096730L,1326288096738L, 200, "[image/png]"));
		resourseList.add(new ResourceRequestSummary("http://localhost:8080/img/twitterbutton.png", 1326288096738L,1326288096739L, 200, "[image/png]"));
		resourseList.add(new ResourceRequestSummary("http://localhost:8080/img/rssbutton.png", 1326288096740L,1326288096741L, 200, "[image/png]"));
		resourseList.add(new ResourceRequestSummary("http://localhost:8080/img/easyTravel_banner.png", 1326288096741L,1326288096744L, 200, "[image/png]"));
		resourseList.add(new ResourceRequestSummary("http://localhost:8080/img/favicon_orange_plane.ico", 1326288096744L,1326288096747L, 200, ""));
		resourseList.add(new ResourceRequestSummary("http://localhost:8080/img/favicon_orange_plane.png", 1326288096747L,1326288096749L, 200, "[image/png]"));
		resourseList.add(new ResourceRequestSummary("http://localhost:8080/css/BaseProd.css", 1326288096749L,1326288096751L, 200, "[text/css]"));
		resourseList.add(new ResourceRequestSummary("http://localhost:8080/css/footer.css", 1326288096751L,1326288096753L, 200, "[text/css]"));
		resourseList.add(new ResourceRequestSummary("http://localhost:8080/css/rime.css", 1326288096753L,1326288096755L, 200, "[text/css]"));
		resourseList.add(new ResourceRequestSummary("http://localhost:8080/css/orange.css", 1326288096755L,1326288096756L, 200, "[text/css]"));
		resourseList.add(new ResourceRequestSummary("http://localhost:8080/dtagent_410.js", 1326288096757L,1326288096758L, 200, "[text/javascript;charset=utf-8]"));
		resourseList.add(new ResourceRequestSummary("http://localhost:8080/js/jquery-1.5.2.js", 1326288096758L,1326288096761L, 200, "[text/javascript]"));
		resourseList.add(new ResourceRequestSummary("http://localhost:8080/js/jquery-ui-1.8.2.min.js", 1326288096761L,1326288096766L, 200, "[text/javascript]"));
		resourseList.add(new ResourceRequestSummary("http://localhost:8080/js/FrameworkProd.js", 1326288096767L,1326288096769L, 200, "[text/javascript]"));
		resourseList.add(new ResourceRequestSummary("http://localhost:8080/js/jquery.formLabels1.0.js", 1326288096769L,1326288096770L, 200, "[text/javascript]"));
		resourseList.add(new ResourceRequestSummary("http://localhost:8080/js/headerRotation.js", 1326288096771L,1326288096772L, 200, "[text/javascript]"));
		resourseList.add(new ResourceRequestSummary("https://apis.google.com/js/plusone.js", 1326288096772L,1326288097141L, 200, "[text/javascript; charset=utf-8]"));
		resourseList.add(new ResourceRequestSummary("http://platform.twitter.com/widgets.js", 1326288097141L,1326288097409L, 200, "[application/javascript]"));
		resourseList.add(new ResourceRequestSummary("http://localhost:8080/js/GlobalProd.js", 1326288097409L,1326288097414L, 200, "[text/javascript]"));
		resourseList.add(new ResourceRequestSummary("http://localhost:8080/js/jquery.js", 1326288097415L,1326288097419L, 200, "[text/javascript]"));
		resourseList.add(new ResourceRequestSummary("http://connect.facebook.net/en_US/all.js", 1326288097419L,1326288097780L, 200, "[application/x-javascript; charset=utf-8]"));

		ThirdPartyContentSummary summary = new ThirdPartyContentSummary();
		summary.setPageUrl("http://localhost:8080/legal-orange.jsf");
		summary.setPageloadStart(1326288096607L);
		summary.setPageloadFinished(1326288097882L);
		summary.setLoadedResources(resourseList);

		List<ResourceRequestSummary> thirdPartyResources = summary.getLoadedThirdPartyResources();
		Assert.assertEquals(3, thirdPartyResources.size());

		Assert.assertEquals(
				"apis.google.com|4|1|0|0|0|1|0|165_534|369|369|369||0|0|0;" +
				"platform.twitter.com|4|1|0|0|0|1|0|534_802|268|268|268||0|0|0;" +
				"connect.facebook.net|4|1|0|0|0|1|0|812_1173|361|361|361||0|0|0",
				summary.createJavaAgentSignal(false));
		
		// test string with own resources enabled
		Assert.assertEquals(
				"localhost|u|5|0|0|0|5|0|123_132_133_137_140_142|3|1|8||0|0|0|8|0|0|0|8|0|150_159_160_163_164_165_802_807_808_812|2|1|5||0|0|0|4|0|0|0|142_149|1|1|2|1|0|0|0|137_140|3|3|3;" +
				"apis.google.com|4|1|0|0|0|1|0|165_534|369|369|369||0|0|0;" +
				"platform.twitter.com|4|1|0|0|0|1|0|534_802|268|268|268||0|0|0;" +
				"connect.facebook.net|4|1|0|0|0|1|0|812_1173|361|361|361||0|0|0",
				summary.createJavaAgentSignal(true));

		//test XHR 3rd party resources beacon signal
		summary.setXhrActionId(1337);
		Assert.assertEquals(
				"1337-1326288096607;"
				+ "localhost|u|5|0|0|0|5|0|123_132_133_137_140_142|3|1|8||0|0|0|8|0|0|0|8|0|150_159_160_163_164_165_802_807_808_812|2|1|5||0|0|0|4|0|0|0|142_149|1|1|2|1|0|0|0|137_140|3|3|3;"
				+ "apis.google.com|4|1|0|0|0|1|0|165_534|369|369|369||0|0|0;platform.twitter.com|4|1|0|0|0|1|0|534_802|268|268|268||0|0|0;"
				+ "connect.facebook.net|4|1|0|0|0|1|0|812_1173|361|361|361||0|0|0",
				summary.createJavaAgentSignal(true));
		
		Assert.assertEquals(
				"1337-1326288096607;"				
				+ "apis.google.com|4|1|0|0|0|1|0|165_534|369|369|369||0|0|0;platform.twitter.com|4|1|0|0|0|1|0|534_802|268|268|268||0|0|0;"
				+ "connect.facebook.net|4|1|0|0|0|1|0|812_1173|361|361|361||0|0|0",
				summary.createJavaAgentSignal(false));
	}
}
