package com.dynatrace.easytravel;

import com.dynatrace.easytravel.spring.AbstractPagePlugin;

public class Simple404 extends AbstractPagePlugin {
	private String linkStyle;
	private String linkTarget;
	private String linkName;

	public void setLinkStyle(String linkStyle) {
		this.linkStyle = linkStyle;
	}

	public void setLinkTarget(String linkTarget) {
		this.linkTarget = linkTarget;
	}

	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}

	@Override
	public String getHeader() {
		return "<a id='MissingServletLink' " + linkStyle + " " + linkTarget + ">" + linkName + "</a>";
	}
}
