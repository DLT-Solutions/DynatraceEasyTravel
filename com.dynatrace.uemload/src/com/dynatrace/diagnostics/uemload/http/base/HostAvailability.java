/***************************************************
 *
 * @file: HttpAvailability.java
 * @date: 29.06.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.diagnostics.uemload.http.base;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.diagnostics.uemload.utils.UemLoadHttpUtils;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUrlUtils;
import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;


/**
 * Class that manages the availability of the hosts connected by UEMLoad.
 * If a host is unexpected unavailable, the class checks each minute
 * whether it is available again.
 *
 * @author stefan.moschinski
 */
public class HostAvailability {

	private static final Logger logger = Logger.getLogger(HostAvailability.class.getName());
	private static final long WARNING_TIMEOUT = TimeUnit.SECONDS.toMillis(30);

	public static HostAvailability INSTANCE = new HostAvailability(true);

	private AtomicLong lastWarning = new AtomicLong(0);
	private UnavailableHosts unavailableHosts;
	private ScheduledExecutorService executor;

	HostAvailability(boolean schedule) {
		unavailableHosts = new UnavailableHosts();

		/* The thread number is 2, because the scheduled task can block one of them permanently if one of the hosts becomes unresponsive
		 * In such scenario other submitted tasks would not launch - such as setAvailable - so the pool needs to be 2 */
		executor = Executors.newScheduledThreadPool(2,
				new ThreadFactoryBuilder()
						.setDaemon(true)
						.setNameFormat(BaseConstants.UEM_LOAD_HOST_AVAILABILITY_THREAD + "-%d")
						.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

							@Override
							public void uncaughtException(Thread t, Throwable e) {
								logger.log(Level.SEVERE,
										TextUtils.merge("An uncaught exception happened in the thread ''{0}''", t.getName()), e);
							}
						})
						.build());
		/* We could set the thread pool up to close the thread when it is unused. During normal load generation it
		 * will always be used, though, and it's only one thread, so not changing the behavior for now
		((ScheduledThreadPoolExecutor)executor).setCorePoolSize(0);
		((ScheduledThreadPoolExecutor)executor).setMaximumPoolSize(1);
		((ScheduledThreadPoolExecutor)executor).setKeepAliveTime(2, TimeUnit.SECONDS);*/

		if(schedule) {
			executor.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					checkUnexpectedUnavailableHosts();
				}
			}, 60, 30, TimeUnit.SECONDS);
		}
	};


	/**
	 * Use this method to notify that a host is not available. The method verifies that the host is really unavailable and
	 * <b>then</b> adds it to the collection of unavailable hosts.
	 *
	 * @param address URL that is used to get the unavailable host
	 * @author stefan.moschinski
	 */
	public void informUnexpectedUnavailable(final String address) {
		if (isHostUnavailable(address)) {
			return;
		}

		String host = getHost(address);
		if (host == null) {
			return;
		}

		if (UemLoadHttpUtils.isConnectable(host)) {
			return;
		}
		if (System.currentTimeMillis() > lastWarning.get() + WARNING_TIMEOUT) {
			lastWarning.set(System.currentTimeMillis());
			logger.warning(TextUtils.merge("Host ''{0}'' is currently not available", host));
		}
		unavailableHosts.addUnexpectedUnavailable(host);
	}


	public void informUnexpectedUnavailable(final Collection<String> unavailableAddresses) {
		for (String address : unavailableAddresses) {
			informUnexpectedUnavailable(address);
		}
	}

	/**
	 * call to enable processing of requests to this host
	 *
	 * @param address
	 * @author peter.kaiser
	 * @return
	 */
	public Future<?> setAvailable(final String address) {
		if (address == null) {
			return null;
		}
		// do it asynchronously to do not cause an increased startup time ==> SECRET
		return executor.submit(new Runnable() {

			@Override
			public void run() {
				String host = getHost(address);
				if (unavailableHosts.contains(host)) {
					if (UemLoadHttpUtils.isConnectable(host)) {
						unavailableHosts.setAvailable(host);
					} else {
						unavailableHosts.addUnexpectedUnavailable(host);
					}
				}
			}
		});
	}

	/**
	 * requests to the specified hosts will not be processed after calling this method
	 *
	 * @param host
	 * @author peter.kaiser
	 */
	public void setExpectedUnavailable(String address) {
		if (address == null) {
			return;
		}
		String host = getHost(address);
		unavailableHosts.addExpectedUnavailable(host);
	}

	/**
	 *
	 *
	 * @param address
	 * @return true if the host of the specified url was set available
	 * @see UemLoadHttpUtils#setAvailable(String)
	 * @author peter.kaiser
	 */
	public boolean isHostAvailable(String address) {
		if (unavailableHosts.isEmpty()) {
			return true;
		}
		String host = getHost(address);
		return !unavailableHosts.contains(host);
	}

	public boolean isHostUnavailable(String address) {
		return !isHostAvailable(address);
	}

	String getHost(String address) {
		return UemLoadUrlUtils.getExtendedHostUrl(address);
	}

	public boolean isHostUnexpectedUnavailable(String address) {
		Collection<String> unexpectedUnavailable = unavailableHosts.getUnexpectedUnavailable();
		return unexpectedUnavailable.contains(address);
	}

	/**
	 * Check if there is any unexpected host unavailability
	 * @return
	 */
	public boolean isAnyUnexpectedUnavailable() {
		return unavailableHosts.isAnyUnexpected();
	}

	public Collection<String> getUnexpectedHostUvavailable() {
		return unavailableHosts.getUnexpectedUnavailable();
	}

	// not private to allow for testing
	void checkUnexpectedUnavailableHosts() {
		try {
			Set<String> availableAgain = new HashSet<String>(unavailableHosts.size(), 1F);
			for (String host : unavailableHosts.getUnexpectedUnavailable()) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine(TextUtils.merge("Checking whether host ''{0}'' is available again", host));
				}
				if (UemLoadHttpUtils.isConnectable(host)) {
					logger.info(TextUtils.merge("Host ''{0}'' is available again", host));
					availableAgain.add(host);
				}
			}
			unavailableHosts.setAvailable(availableAgain);
		} catch (RuntimeException e) {
			logger.log(Level.WARNING,
					TextUtils.merge("A RuntimeException happened in ''{0}''", Thread.currentThread().getName()), e);
		}
	}

	public void shutdown() {
		executor.shutdown();
	}

	private static class UnavailableHosts {

		private static final Boolean UNEXPECTED_UNAVAILABLE = false;
		private static final Boolean EXPECTED_UNAVAILABLE = true;

		private ConcurrentMap<String, /* expected unavailable */Boolean> hosts = new ConcurrentHashMap<String, Boolean>(8, 1F);

		Collection<String> getUnexpectedUnavailable() {
			HashSet<String> unexpected = new HashSet<String>();
			for (Entry<String, Boolean> host : hosts.entrySet()) {
				if (host.getValue() == UNEXPECTED_UNAVAILABLE) {
					unexpected.add(host.getKey());
				}
			}
			return unexpected;
		}

		void setAvailable(Collection<String> availableHosts) {
			for (String host : availableHosts) {
				if(hosts.remove(host) != null) {
					if (logger.isLoggable(Level.INFO)) {
						logger.info("Host available again: " + host);
					}
				}
			}
		}

		void setAvailable(String availableHost) {
			if(hosts.remove(availableHost) != null) {
				if (logger.isLoggable(Level.INFO)) {
					logger.info("Host available again: " + availableHost);
				}
			}
		}

		void addExpectedUnavailable(String host) {
			Boolean put = hosts.put(host, EXPECTED_UNAVAILABLE);
			if (logger.isLoggable(Level.INFO)) {
				if(put == null) {
					logger.info("Host unavailable (expected): " + host);
				} else if (put != EXPECTED_UNAVAILABLE) {
					logger.info("Host is now unavailable (expected), was unavailable (unexpected) before");
				}
			}
		}

		void addUnexpectedUnavailable(String host) {
			Boolean put = hosts.put(host, UNEXPECTED_UNAVAILABLE);
			if (logger.isLoggable(Level.INFO)) {
				if(put == null) {
					logger.info("Host unavailable (unexpected): " + host);
				} else if (put != UNEXPECTED_UNAVAILABLE) {
					logger.info("Host is now unavailable (unexpected), was unavailable (expected) before");
				}
			}
		}

		boolean contains(String host) {
			return hosts.containsKey(host);
		}

		boolean isEmpty() {
			return hosts.isEmpty();
		}

		int size() {
			return hosts.size();
		}

		boolean isAnyUnexpected() {
			return hosts.values().contains(false);
		}
	}

	@TestOnly
	public ScheduledExecutorService getExecutor() {
		return executor;
	}
}
