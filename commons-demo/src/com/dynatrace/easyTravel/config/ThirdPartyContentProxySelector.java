package com.dynatrace.easytravel.config;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import ch.qos.logback.classic.Logger;
import org.apache.commons.lang3.ArrayUtils;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;

/**
 * A ProxySelector is a global intercepter of Socket connections, after it is
 * installed it is called for every connection that is opened.
 *
 * We use this proxy in tests that need to access resources on the Internet.
 *
 * Note: Part of this code is taken from class ClientProxySelector, see there
 * 			for more details!
 *
 * @author dominik.stadler
 */
public class ThirdPartyContentProxySelector extends ProxySelector {

	private static final Logger LOGGER = LoggerFactory.make();
	static final String PROXY_AUTHORITY_PATTERN_TEMPLATE = "([a-zA-Z0-9\\-\\.]+\\.)?(%s)\\.[a-zA-Z]{2,3}";

	final ProxySelector defsel;
	private final Proxy proxy;

	private final Optional<Pattern> proxyPattern;

	/**
	 * Configure a global proxy server to allow access to resources on the Internet.
	 * 
	 * @author dominik.stadler
	 */
	public static void applyProxy() {
		EasyTravelConfig config = EasyTravelConfig.read();

		// only enable if proxy host is set
		if (StringUtils.isNotEmpty(config.proxyHost)) {
			if (UrlUtils.checkServiceAvailability(config.proxyHost, config.proxyPort)) {
				ThirdPartyContentProxySelector ps =
						new ThirdPartyContentProxySelector(ProxySelector.getDefault(),
								config.proxyHost, config.proxyPort, config.proxiedSites);

				ProxySelector.setDefault(ps);
			} else {
				LOGGER.info("Proxy service \"" + config.proxyHost + ":" + config.proxyPort +
						"\" not available. Continue without proxy");
			}
		}
	}

	/**
	 * Undo the proxy server changes to prevent access to the Internet again.
	 * 
	 * @author dominik.stadler
	 */
	public static void clearProxy() {
		ProxySelector def = ProxySelector.getDefault();
		if (def instanceof ThirdPartyContentProxySelector) {
			LOGGER.info("Removing proxy selector");
			ProxySelector.setDefault(((ThirdPartyContentProxySelector) def).defsel);
		}
	}


	public ThirdPartyContentProxySelector(ProxySelector def, String host, int port, String[] proxiedSites) {
		this.defsel = def;

		SocketAddress addr = new InetSocketAddress(host, port);
		proxy = new Proxy(Proxy.Type.HTTP, addr);

		proxyPattern = createProxyRegexPatternFor(proxiedSites);
		if (proxyPattern.isPresent()) {
			LOGGER.info(String.format("Setting proxy selector for proxy at '%s:%s' for pattern '%s'", host, port,
					proxyPattern.get()));
		} else {
			LOGGER.warn("No proxied sites are given, connections to external sites such as facebook.com and google.com are most likely to fail from UEMLoad");
		}

		// we currently only support HTTP proxy type
		/*
		 * SocketAddress addr = new InetSocketAddress("socks.mydomain.com", 1080);
		 * Proxy proxy = new Proxy(Proxy.Type.SOCKS, addr);
		 */
	}

	static Optional<Pattern> createProxyRegexPatternFor(String[] proxiedSites) {
		if (ArrayUtils.isEmpty(proxiedSites)) {
			return Optional.absent();
		}

		String regexStr =
				String.format(PROXY_AUTHORITY_PATTERN_TEMPLATE,
						Joiner.on(BaseConstants.PIPE)
								.join(proxiedSites));
		return Optional.of(Pattern.compile(regexStr));
	}

	@Override
	public java.util.List<Proxy> select(URI uri) {
		if (uri == null) {
			throw new IllegalArgumentException("Need an URI in the Proxy Selector");
		}

		// check if we should proxy this url
		if (isIncluded(uri)) {
			LOGGER.debug("Using Proxy for URL: " + uri);

			return Collections.singletonList(proxy);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Not using Proxy for URL: " + uri);
		}

		if (defsel != null) {
			return defsel.select(uri);
		}

		return Collections.singletonList(Proxy.NO_PROXY);
	}

	private boolean isIncluded(URI uri) {
		if (proxyPattern.isPresent()) {
			String address = uri.getAuthority();
			return proxyPattern.get().matcher(address).matches();
		}

		return false;
	}

	@Override
	public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
		if (uri == null || sa == null || ioe == null) {
			throw new IllegalArgumentException("Arguments can't be null.");
		}

		if (defsel != null) {
			defsel.connectFailed(uri, sa, ioe);
		}
	}
}
