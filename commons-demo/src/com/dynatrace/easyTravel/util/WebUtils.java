package com.dynatrace.easytravel.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility collection for web.
 */
public class WebUtils {

	// the supported content-types of the PluginServlet / WebLauncher helper Servlets
	public static String getContentType(String path) {
		if (path.endsWith(".dtp")) {
			return "application/octet-stream";
		} else if (path.endsWith(".html")) {
			return "text/html;charset=UTF-8";
		} else if (path.endsWith(".txt")) {
			return "text/plain;charset=UTF-8";
		} else if (path.endsWith(".css")) {
			return "text/css;charset=UTF-8";
		} else if (path.endsWith(".js")) {
			return "application/javascript;charset=UTF-8";
		} else if (path.endsWith(".png")) {
			return "image/png";
		} else if (path.endsWith(".gif")) {
			return "image/gif";
		} else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
			return "image/jpeg";
		} else if (path.endsWith(".zip")) {
			return "application/zip";
		}
		return "application/octet-stream";
	}

	private static String buildHtmlRegex(String[] elements, String content) {
		if (elements.length < 1) {
			throw new IllegalArgumentException("Excepting at least 1 element");
		}
		StringBuilder buf = new StringBuilder("(?s)"); // DOT_ALL
		String sep = "";
		for (String element : elements) {
			buf.append(sep).append("<").append(element).append("[^>]*>");
			sep = ".*"; // capture any text in-between
		}
		buf.append("\\s*(").append(content != null ? content : ".*?").append(")\\s*");
		buf.append("</").append(elements[elements.length - 1]).append(">");
		return buf.toString();
	}

	/**
	 * Scan html content for the contents of the element denoted by elements
	 * matching a specific content regex
	 *
	 * @param html          the HTML content to scan
	 * @param elements      the nested HTML elements, e.g. {"html","head","title"} to get the title
	 *                      elements in hiararchy may be skipped, e.g. {"html","title"} will work
	 *                      as well.
	 * @param content       the text content (regex), e.g. "My Page.*?" for a title
	 * @return              the trimmed contents of the desired element, or null, if not found
	 *                      or not matched, e.g. if the <title> content didn't match "My Page.*?"
	 *
	 * @author philipp.grasboeck
	 */
	public static String getHtmlElementText(String html, String[] elements, String content) {
		return getHtmlText(html, buildHtmlRegex(elements, content));
	}

	/**
	 * Scan html content for the contents of the element denoted by elements.
	 *
	 * @param html          the HTML content to scan
	 * @param elements      the nested HTML elements, e.g. {"html","head","title"} to get the title
	 * @return              the trimmed contents of the desired element, or null, if not found.
	 *
	 * @author philipp.grasboeck
	 */
	public static String getHtmlElementText(String html, String[] elements) {
		return getHtmlText(html, buildHtmlRegex(elements, null));
	}

	// helper to extract the matching group
	private static String getHtmlText(String html, String regex) {
		Matcher matcher = Pattern.compile(regex).matcher(html);
		return matcher.find() ? matcher.group(1) : null;
	}
}
