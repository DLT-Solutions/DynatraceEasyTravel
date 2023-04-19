package com.dynatrace.easytravel.thirdparty;

import java.net.URI;

public class ThirdPartySocialMediaService {

	private static final String SCRIPT_DIR = "script";
	private static final String SCRIPT = "social_media_script.js";


	private String htmlSnippet = "";

	public ThirdPartySocialMediaService(String url, boolean outage, boolean limited, boolean caching) {

		URI scriptUri = UriUtil.buildUri(url, SCRIPT_DIR, SCRIPT, caching, outage, limited);
		URI domainUri = UriUtil.buildUri(url, null, null, caching, outage, limited);

		this.htmlSnippet = buildHtmlSnippet(scriptUri, domainUri);
	}

	/**
	 * html snippet which loads separate script file and attaches iframe to website
	 *
	 * <div>
	 * <script>
	 * (function(d, s, id) {
	 * var js, fjs = d.getElementsByTagName(s)[0];
	 * if (d.getElementById(id)) return;
	 * js = d.createElement(s);
	 * js.id = id;
	 * js.src = 'URI';
	 * fjs.parentNode.insertBefore(js, fjs);
	 * }(document, 'script', 'third-party-social-media'));
	 * </script>
	 * <div id="social-media-share"></div>
	 * </div>
	 *
	 * @param uri
	 * @return
	 */
	private String buildHtmlSnippet(URI scriptUri, URI domainUri) {

		String html = String.format(
				"<div>" +
						"<script>" +
						"(function(d, s, id) {" +
						"var js, fjs = d.getElementsByTagName(s)[0];" +
						"if (d.getElementById(id)) return;" +
						"js = d.createElement(s);" +
						"js.id = id;" +
						"js.src = '%s';" +
						"fjs.parentNode.insertBefore(js, fjs);" +
						"}(document, 'script', 'third-party-social-media'));" +
						"</script>" +
						"<div id=\"social-media-share\" url=\"%s\"></div>" +
						"</div>",
				scriptUri.toString(),
				domainUri.toString());

		return html;
	}

	public String getHtmlSnippet() {
		return htmlSnippet;
	}

}
