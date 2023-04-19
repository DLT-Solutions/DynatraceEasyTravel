package com.dynatrace.easytravel;

import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.dynatrace.easytravel.spring.AbstractPagePlugin;

public class PrintClusterNode extends AbstractPagePlugin {

	@Override
	public String getFooter() {
		return getClusterNode((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
	}

	private static String getClusterNode(ServletRequest request)
	{
		HttpSession session = (request instanceof HttpServletRequest) ? ((HttpServletRequest) request).getSession() : null;
		return (session != null) ? session.getId().replaceFirst(".+\\.", "") : "no-session"; // should return something like "jvmRoute-8280"
	}
}
