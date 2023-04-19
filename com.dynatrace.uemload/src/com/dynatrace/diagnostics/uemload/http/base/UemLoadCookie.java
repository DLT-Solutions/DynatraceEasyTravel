package com.dynatrace.diagnostics.uemload.http.base;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.SetCookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import com.dynatrace.diagnostics.uemload.utils.UemLoadUrlUtils;
import com.google.common.base.Optional;


public class UemLoadCookie implements SetCookie, ClientCookie, Cloneable, Serializable {

	private static final long serialVersionUID = -8411486076860671300L;

	private final BasicClientCookie wrappedCookie;

	/**
	 * Constructor is only intended for easily removing of cookies out of the {@link UemLoadCookieStore}
	 * 
	 * @param name cookie name
	 */
	UemLoadCookie(String name) {
		this(name, null, Optional.<String> absent(), Collections.<String, String> emptyMap());
	}

	public UemLoadCookie(String name, String value, String url) {
		this(name, value, Optional.of(url), Collections.<String, String> emptyMap());
	}

	public UemLoadCookie(String name, String value, Optional<String> url, Map<String, String> attributes) {
		this.wrappedCookie = new BasicClientCookie(name, value);

		for (Entry<String, String> entry : attributes.entrySet()) {
			this.wrappedCookie.setAttribute(entry.getKey(), entry.getValue());
		}

		if (url.isPresent()) {
			this.wrappedCookie.setDomain(UemLoadUrlUtils.getHost(url.get()));
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((wrappedCookie.getName() == null) ? 0 : wrappedCookie.getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UemLoadCookie other = (UemLoadCookie) obj;
		if (wrappedCookie.getName() == null) {
			if (other.wrappedCookie.getName() != null)
				return false;
		} else if (!wrappedCookie.getName().equals(other.wrappedCookie.getName()))
			return false;
		return true;
	}

	/**
	 * @return
	 * @see org.apache.http.impl.cookie.BasicClientCookie#getName()
	 */
	@Override
	public String getName() {
		return wrappedCookie.getName();
	}

	/**
	 * @return
	 * @see org.apache.http.impl.cookie.BasicClientCookie#getValue()
	 */
	@Override
	public String getValue() {
		return wrappedCookie.getValue();
	}

	/**
	 * @param value
	 * @see org.apache.http.impl.cookie.BasicClientCookie#setValue(java.lang.String)
	 */
	@Override
	public void setValue(String value) {
		wrappedCookie.setValue(value);
	}

	/**
	 * @return
	 * @see org.apache.http.impl.cookie.BasicClientCookie#getComment()
	 */
	@Override
	public String getComment() {
		return wrappedCookie.getComment();
	}

	/**
	 * @param comment
	 * @see org.apache.http.impl.cookie.BasicClientCookie#setComment(java.lang.String)
	 */
	@Override
	public void setComment(String comment) {
		wrappedCookie.setComment(comment);
	}

	/**
	 * @return
	 * @see org.apache.http.impl.cookie.BasicClientCookie#getCommentURL()
	 */
	@Override
	public String getCommentURL() {
		return wrappedCookie.getCommentURL();
	}

	/**
	 * @return
	 * @see org.apache.http.impl.cookie.BasicClientCookie#getExpiryDate()
	 */
	@Override
	public Date getExpiryDate() {
		return wrappedCookie.getExpiryDate();
	}

	/**
	 * @param expiryDate
	 * @see org.apache.http.impl.cookie.BasicClientCookie#setExpiryDate(java.util.Date)
	 */
	@Override
	public void setExpiryDate(Date expiryDate) {
		wrappedCookie.setExpiryDate(expiryDate);
	}

	/**
	 * @return
	 * @see org.apache.http.impl.cookie.BasicClientCookie#isPersistent()
	 */
	@Override
	public boolean isPersistent() {
		return wrappedCookie.isPersistent();
	}

	/**
	 * @return
	 * @see org.apache.http.impl.cookie.BasicClientCookie#getDomain()
	 */
	@Override
	public String getDomain() {
		return wrappedCookie.getDomain();
	}

	/**
	 * @param domain
	 * @see org.apache.http.impl.cookie.BasicClientCookie#setDomain(java.lang.String)
	 */
	@Override
	public void setDomain(String domain) {
		wrappedCookie.setDomain(domain);
	}

	/**
	 * @return
	 * @see org.apache.http.impl.cookie.BasicClientCookie#getPath()
	 */
	@Override
	public String getPath() {
		return wrappedCookie.getPath();
	}

	/**
	 * @param path
	 * @see org.apache.http.impl.cookie.BasicClientCookie#setPath(java.lang.String)
	 */
	@Override
	public void setPath(String path) {
		wrappedCookie.setPath(path);
	}

	/**
	 * @return
	 * @see org.apache.http.impl.cookie.BasicClientCookie#isSecure()
	 */
	@Override
	public boolean isSecure() {
		return wrappedCookie.isSecure();
	}

	/**
	 * @param secure
	 * @see org.apache.http.impl.cookie.BasicClientCookie#setSecure(boolean)
	 */
	@Override
	public void setSecure(boolean secure) {
		wrappedCookie.setSecure(secure);
	}

	/**
	 * @return
	 * @see org.apache.http.impl.cookie.BasicClientCookie#getPorts()
	 */
	@Override
	public int[] getPorts() {
		return wrappedCookie.getPorts();
	}

	/**
	 * @return
	 * @see org.apache.http.impl.cookie.BasicClientCookie#getVersion()
	 */
	@Override
	public int getVersion() {
		return wrappedCookie.getVersion();
	}

	/**
	 * @param version
	 * @see org.apache.http.impl.cookie.BasicClientCookie#setVersion(int)
	 */
	@Override
	public void setVersion(int version) {
		wrappedCookie.setVersion(version);
	}

	/**
	 * @param date
	 * @return
	 * @see org.apache.http.impl.cookie.BasicClientCookie#isExpired(java.util.Date)
	 */
	@Override
	public boolean isExpired(Date date) {
		return wrappedCookie.isExpired(date);
	}

	/**
	 * @param name
	 * @param value
	 * @see org.apache.http.impl.cookie.BasicClientCookie#setAttribute(java.lang.String, java.lang.String)
	 */
	public void setAttribute(String name, String value) {
		wrappedCookie.setAttribute(name, value);
	}

	/**
	 * @param name
	 * @return
	 * @see org.apache.http.impl.cookie.BasicClientCookie#getAttribute(java.lang.String)
	 */
	@Override
	public String getAttribute(String name) {
		return wrappedCookie.getAttribute(name);
	}

	/**
	 * @param name
	 * @return
	 * @see org.apache.http.impl.cookie.BasicClientCookie#containsAttribute(java.lang.String)
	 */
	@Override
	public boolean containsAttribute(String name) {
		return wrappedCookie.containsAttribute(name);
	}

	/**
	 * @return
	 * @throws CloneNotSupportedException
	 * @see org.apache.http.impl.cookie.BasicClientCookie#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return wrappedCookie.clone();
	}

	/**
	 * @return
	 * @see org.apache.http.impl.cookie.BasicClientCookie#toString()
	 */
	@Override
	public String toString() {
		return wrappedCookie.toString();
	}



}
