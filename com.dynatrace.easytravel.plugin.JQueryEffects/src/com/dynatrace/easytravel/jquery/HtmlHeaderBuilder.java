package com.dynatrace.easytravel.jquery;

import java.util.Arrays;
import java.util.List;


public class HtmlHeaderBuilder {

	enum HtmlEntryType {
		SCRIPT("text/javascript", "src"),
		LINK("text/css", "href", "rel='stylesheet'");


		private List<String> attributes;
		private String reference;
		private String type;

		private HtmlEntryType(String type, String reference, String... attributes) {
			this.type = type;
			this.reference = reference;
			this.attributes = Arrays.asList(attributes);
		}

		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}


		protected List<String> getAttributes() {
			return attributes;
		}


		protected String getReference() {
			return reference;
		}


		protected String getType() {
			return type;
		}


	}

	private final StringBuilder StringBuilder;
	private String pluginName;

	public HtmlHeaderBuilder(String pluginName) {
		this.StringBuilder = new StringBuilder();
		this.pluginName = pluginName;
	}

	public HtmlHeaderBuilder appendScript(String scriptPath){
		String entry = getEntry(HtmlEntryType.SCRIPT, scriptPath);
		this.StringBuilder.append(entry);
		return this;
	}

	public HtmlHeaderBuilder appendRemoteScript(String scriptPath){
		String entry = getRemoteEntry(HtmlEntryType.SCRIPT, scriptPath);
		this.StringBuilder.append(entry);
		return this;
	}

	public HtmlHeaderBuilder appendLink(String linkPath){
		String entry = getEntry(HtmlEntryType.LINK, linkPath);
		this.StringBuilder.append(entry);
		return this;
	}

	public String generate() {
		return StringBuilder.toString();
	}

	private String getEntry(HtmlEntryType entryType, String path) {
		String startTag = entryType.toString();
		String fullPath = "plugins/" + pluginName + path;
		String htmlEntry = "<" + startTag + " type='" + entryType.getType() + "' "
					+ entryType.reference + "='" + fullPath + "' "
					+ getAttributes(entryType.getAttributes()) + getEndTag(entryType);
		return htmlEntry;
	}

	private String getRemoteEntry(HtmlEntryType entryType, String path) {
		String startTag = entryType.toString();
		String fullPath = path;
		String htmlEntry = "<" + startTag + " type='" + entryType.getType() + "' "
					+ entryType.reference + "='" + fullPath + "' "
					+ getAttributes(entryType.getAttributes()) + getEndTag(entryType);
		return htmlEntry;
	}
	
	private String getEndTag(HtmlEntryType entryType) {
		if(entryType == HtmlEntryType.SCRIPT)
			return "></" + HtmlEntryType.SCRIPT + ">";

		return "/>";
	}

	private String getAttributes(List<String> attributes) {
		StringBuilder entry = new StringBuilder();
		for (String attribute : attributes) {
			entry.append(attribute).append(" ");
		}
		return entry.toString();
	}
}
