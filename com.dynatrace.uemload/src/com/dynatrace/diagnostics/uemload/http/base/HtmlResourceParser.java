package com.dynatrace.diagnostics.uemload.http.base;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.html.dom.HTMLDocumentImpl;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.dynatrace.diagnostics.uemload.thirdpartycontent.FacebookInlineScriptParser;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.google.common.base.Strings;

import ch.qos.logback.classic.Logger;


/**
 * Parser that reads in a HTML document as {@link String} and collects the referenced resources.
 * {@link #listResourceReferences(String, String)} lists all <code>img</code>, <code>link</code> and <code>script</code>
 * resources.
 *
 * @author martin.wurzinger
 */
public class HtmlResourceParser {

	private static final Logger logger = LoggerFactory.make();
	private static final String ABSOLUTE_URL = "\\b(https?|ftp|file)";
	
	private ArrayList<String> resources;
	private Map<String, ResourceExclusion> excludedResources;
	private String htmlRootUrl;
	private String contextRoot;
	private boolean loadDynaTraceResources;
	private String protocol;

	public HtmlResourceParser(boolean loadDynaTraceResources) {
		this.loadDynaTraceResources = loadDynaTraceResources;
	}

	public HtmlResourceParser() {
		this(true);
	}

	/**
	 * Lists URL strings of <code>img</code>, <code>link</code> and <code>script</code> resources
	 * that are referenced within the HTML document.
	 *
	 * @param htmlRootUrl the root URL from where the HTML document was downloaded from
	 * @param html the HTML document as {@link String}
	 * @return a collection of URL strings
	 * @author martin.wurzinger
	 */
	public Collection<String> listResourceReferences(String htmlRootUrl, String html) {
		resources = new ArrayList<String>();
		excludedResources = new HashMap<>();
		this.htmlRootUrl = htmlRootUrl;
		setContextRoot();
		setProtocol();

		Document document = null;

		if (!html.contains("<partial-response>")) {

			DOMParser parser = new DOMParser();

			try {
				parser.parse(new InputSource(new StringReader(html)));
			} catch (Exception e) {
				logger.warn("Unable to parse HTML document", e);
				return Collections.emptyList();
			}

			document = parser.getDocument();
			setResourceExclusion("link", "rel", "pingback", "dns-prefetch");

			addResources(document, "link", "href");
			addResources(document, "script", "src");
			addResources(document, "img", "src");

			addResourceFacebookLikeButton(html);

		} else {
			try {
				DOMFragmentParser fragmentParser = newFragmentParser();
				DocumentFragment fragment = new HTMLDocumentImpl().createDocumentFragment();
				fragmentParser.parse(new InputSource(new StringReader(html)), fragment);
				getPartialResources(fragment);
			} catch (SAXException e) {
				logger.warn("Could not parse resources", e);
			} catch (IOException e) {
				logger.warn("Could not parse resources", e);
			}
		}
		return Collections.unmodifiableCollection(resources);
	}

	/**
	 * Lists URL strings of <code>iframe</code> resources
	 * that are referenced within the HTML document.
	 *
	 * @param htmlRootUrl the root URL from where the HTML document was downloaded from
	 * @param html the HTML document as {@link String}
	 * @return a collection of URL strings
	 * @author cwat-moehler
	 */
	public Collection<String> listIframeReferences(String html) {
		List<String> iframeUrls = new ArrayList<String>();
		String nodeJSWeatherApp = parseNodeJSWeatherApplicationUrl(html);
		if (nodeJSWeatherApp != null) {
			iframeUrls.add(parseNodeJSWeatherApplicationUrl(html));
		}

		return iframeUrls;
	}


	private DOMFragmentParser newFragmentParser() throws SAXNotRecognizedException, SAXNotSupportedException {
		DOMFragmentParser fragmentParser = new DOMFragmentParser();
		fragmentParser.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment", true);
		fragmentParser.setFeature("http://cyberneko.org/html/features/scanner/cdata-sections", true);
		return fragmentParser;
	}

	private void getPartialResources(Node node) {

		if (node.getNodeName().equals("#cdata-section")) {
			try {
				DOMFragmentParser fragmentParser = newFragmentParser();
				DocumentFragment fragment = new HTMLDocumentImpl().createDocumentFragment();
				fragmentParser.parse(new InputSource(new StringReader(node.getNodeValue())), fragment);
//				print(fragment.getFirstChild(), "");
				getPartialResources(fragment.getFirstChild());
			} catch (DOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if ("img".equalsIgnoreCase(node.getNodeName())) {
			addResource(node, "src");
			return;
		} else if ("link".equalsIgnoreCase(node.getNodeName())) {
			addResource(node, "link");
			return;
		} else if ("script".equalsIgnoreCase(node.getNodeName())) {
			addResource(node, "src");
			return;
		}

		Node child = node.getFirstChild();

		while (child != null) {
			getPartialResources(child);
			child = child.getNextSibling();
		}
	}
	
	private void setResourceExclusion(String tagName, String attributeName, String... values) {
        ResourceExclusion re = excludedResources.get(tagName);
        if(re == null) {
            re = new ResourceExclusion();
        }
        re.putAtributeExclusion(attributeName, values);
        excludedResources.put(tagName, re);
    }

	private void addResources(Document document, String tagName, String attributeName) {
		NodeList elements = document.getElementsByTagName(tagName);
		for (int i = 0; i < elements.getLength(); i++) {
			Node element = elements.item(i);
			addResourceIfNotExcluded(element, tagName, attributeName);
		}

	}
	
	private void addResourceIfNotExcluded(Node element, String tagName, String attributeName) {
		if(excludedResources.containsKey(tagName)) {
            if(!excludedResources.get(tagName).isResourceExcluded(element)) {
                addResource(element, attributeName);
            }
        } else {
            addResource(element, attributeName);
        }
	}

	/**
	 *
	 * @param document
	 * @author peter.lang
	 */
	private void addResourceFacebookLikeButton(String htmlSource) {
		String fbRootTag = "<div id=\"fb-root\"></div>";
		int indexOfFbRootTag = htmlSource.indexOf(fbRootTag);
		if (indexOfFbRootTag >= 0) {
			int scriptStartIndex = htmlSource.indexOf("<script>", indexOfFbRootTag);
			if (scriptStartIndex >= 0) {
				int scriptEndIndex = htmlSource.indexOf("</script>", scriptStartIndex);
				if (scriptEndIndex>=0) {
					String fbInlineScript = htmlSource.substring(scriptStartIndex, scriptEndIndex);
					String fbUrl = FacebookInlineScriptParser.parseInlineScript(fbInlineScript);
					if (fbUrl!=null) {
						resources.add(fbUrl);
					}
				}
			}

		}
	}

	private String parseNodeJSWeatherApplicationUrl(String htmlSource) {
		String weatherApplicationIframe = "<iframe id=\"weather-app\"";

		int indexOfWeatherApp = htmlSource.indexOf(weatherApplicationIframe);
		if (indexOfWeatherApp >= 0) {
			int urlStartIndex = htmlSource.indexOf("src=\"", indexOfWeatherApp) + 5;
			if (urlStartIndex >= 0) {
				int urlEndIndex = htmlSource.indexOf('\"', urlStartIndex + 1);
				if (urlEndIndex >= 0) {
					String url = htmlSource.substring(urlStartIndex, urlEndIndex);
					if (url != null) {
						return url;
					}
				}
			}
		}
		return null;
	}


	private void addResource(Node element, String attributeName) {
		NamedNodeMap attributes = element.getAttributes();

		if (attributes == null) {
			return;
		}
		Node attributeNode = attributes.getNamedItem(attributeName);

		if (attributeNode == null) {
			return;
		}

		String resource = attributeNode.getNodeValue();
		if(loadDynaTraceResources == false && isDynatraceResource(resource)) {
			return;
		}
		if(isVirtualResource(resource)) {
			return;
		}

		String absoluteResourceUrl = getAbsoluteResourceUrl(resource);
		resources.add(absoluteResourceUrl);
	}

	private boolean isVirtualResource(String resource) {
		if(resource.contains("javax.faces.resource")) {
			return true;
		}
		return false;
	}

	private static boolean isDynatraceResource(String resource) {
		Pattern dtPattern = Pattern.compile("dtagent_\\d+.js");
		Matcher dtMatcher = dtPattern.matcher(resource);
		if(dtMatcher.find()) {
			return true;
		}
		return false;
	}

	private String getAbsoluteResourceUrl(String resource) {
		if (resource.split("://")[0].matches(ABSOLUTE_URL)) {
			return resource;
		}
		
		//handle protocol relative URLs correctly
		if (resource.startsWith("//") && !Strings.isNullOrEmpty(protocol)) {
			return protocol + ":" + resource;
		}

		resource = removeRedundantSubpath(resource);
		String htmlRootUrlTemp = htmlRootUrl +
				(!resource.startsWith("/") && !htmlRootUrl.endsWith("/")
						? "/" + resource :
				(resource.startsWith("/") && htmlRootUrl.endsWith("/")
						? resource.substring(1)
						: resource));
		return htmlRootUrlTemp;
	}

	private String removeRedundantSubpath(String resource) {
		if (resource.startsWith("/")) {
			Pattern pattern = Pattern.compile("^(/[-A-Za-z0-9+&@#%?=~_|!:,.;]+)");
			Matcher matcher = pattern.matcher(resource);
			if (matcher.find()) {
				if (matcher.group(1).equals(contextRoot)) {
					return resource.substring(matcher.group(1).length());
				}
			}
		}
		return resource;
	}


	public void setContextRoot() {
		Pattern pattern = Pattern.compile("(/[-A-Za-z0-9+&@#%?=~_|!,.;]*)/*$");
		Matcher matcher = pattern.matcher(htmlRootUrl);
		if (matcher.find()) {
			contextRoot = matcher.group(1);
		} else {
			contextRoot = "/";
		}
	}
		
	private void setProtocol() {
		String s = htmlRootUrl.split("://")[0];
		if (s.matches(ABSOLUTE_URL)) {
			protocol = s; 
		}		
	}
	
	private class ResourceExclusion {

        private Map<String, List<String>> attributes = new HashMap<>();

        public void putAtributeExclusion(String attributeName, String... values) {
            attributes.merge(attributeName, Arrays.asList(values), (l1, l2) -> Stream.of(l1, l2).flatMap(Collection::stream).collect(Collectors.toList()));
        }

        public boolean isResourceExcluded(Node node) {
            NamedNodeMap map = node.getAttributes();
            if(map != null) {
	            for(int i=0; i<map.getLength();i++) {
	                Node n = map.item(i);
	                String attributeName = n.getNodeName();
	                List<String> values = attributes.get(attributeName);
	                if(values != null) {
	                    if(values.contains(node.getAttributes().getNamedItem(attributeName).getNodeValue()))
	                        return true;
	                }
	
	            }
            }
            return false;
        }
    }
}
