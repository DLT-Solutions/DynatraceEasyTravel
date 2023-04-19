/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 * 
 * @file: EasyTravelCookieStore.java
 * @date: 02.07.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.diagnostics.uemload.http.base;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieIdentityComparator;
import org.apache.http.impl.client.BasicCookieStore;


/**
 * An implementation of {@link CookieStore} that allows to delete a specific cookie.
 * 
 * @author stefan.moschinski
 */
public class UemLoadCookieStore implements CookieStore {

	private static final Logger logger = Logger.getLogger(UemLoadCookieStore.class.getName());

	private final Set<Cookie> cookieBackup = new TreeSet<Cookie>(new CookieIdentityComparator());

	private BasicCookieStore cookieStore;

	public UemLoadCookieStore() {
		this.cookieStore = new BasicCookieStore();
	}

	@Override
	public List<Cookie> getCookies() {
		return cookieStore.getCookies();
	}

	@Override
	public synchronized boolean clearExpired(Date date) {
		return cookieStore.clearExpired(date);
	}

	@Override
	public synchronized void clear() {
		cookieStore.clear();
		cookieBackup.clear();
	}

	@Override
	public synchronized void addCookie(Cookie cookie) {
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("Adding cookie: " + cookie);
		}
		cookieStore.addCookie(cookie);
		cookieBackup.add(cookie);
	}

	public synchronized void removeCookie(String name) {
		// we cannot remove the cookies of BasicCookieStore directly, so we have to use this workaround
		if (removeCookieFromBackup(name)) {
			if (logger.isLoggable(Level.FINER)) {
				logger.finer("Removed cookies: " + name);
			}
			cookieStore.clear();
			cookieStore.addCookies(cookieBackup.toArray(new Cookie[cookieBackup.size()]));
		}

	}
	
	private boolean removeCookieFromBackup(String name) {		
		List<Cookie> cookieList = cookieBackup.stream()
		.filter( c -> c.getName().equals(name))
		.collect(Collectors.toList());
		
		cookieList.stream().forEach(cookie -> cookieBackup.remove(cookie));
		return cookieList.size() > 0;
	}
}