/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: LimitCleaningSessionManager.java
 * @date: 01.07.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.tomcat;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpSession;

import org.apache.catalina.Session;
import org.apache.catalina.session.StandardManager;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.MemoryUtils;
import com.dynatrace.easytravel.util.TextUtils;


/**
 * A enhanced Tomcat Session Manager which can do two things:
 *
 * * Clean session attributes from Sessions
 * * Expire sessions if there are too many active ones
 *
 * This is used in easyTravel to prevent OOM in all cases, eve, when high load scenarios are run
 * and even with icefaces which seems to keep sessions endlessly.
 *
 * @author dominik.stadler
 */
public class AutomaticMemoryManager extends StandardManager {
	private static final Logger log = LoggerFactory.make();

	private static final String[] ATTRIBUTES = {
			"bookingBean",
			"facebookBean",
			"searchBean",
			"loginBean",
			"adBean",
			"searchDelayStateBean",
			"com.sun.faces.renderkit.ServerSideStateHelper.LogicalViewMap",
			"com.icesoft.faces.component.panelpositioned.PanelPositionedModel:iceform:dataList",
			"org.icefaces.impl.application.WindowScopeManager",
			"Icesoft_DnDCache_Key",
			"journeyBean",
			"org.icefaces.impl.application.LazyPushManager",
			"javax.faces.request.charset",
			"org.icefaces.impl.push.SessionViewManager"
	};

	/**
	 * The descriptive information about this implementation.
	 */
	protected static final String info = "AutomaticMemoryManager/1.0";


	/**
	 * The descriptive name of this Manager implementation (for logging).
	 */
	protected static String name = "AutomaticMemoryManager";

	private static Comparator<? super Session> sessionComparator = new SessionComparator();

	private long lastCheckAttributes = System.currentTimeMillis();
	private long lastCheckSessions = System.currentTimeMillis();

	private long minSessionLifeTime;

	private final String serverName;

	private MemoryUtils memUtils;
	private AtomicBoolean sessionCleanUpRunning;

	public static final double STANDARD_SESSION_CLEAR_RATE = 0.2;
	public static final double LOW_HEAP_SESSION_CLEAR_RATE = 0.4;
	public static final double EXTREME_LOW_HEAP_SESSION_CLEAR_RATE = 0.5;

	public static final double STANDARD_ATTRIBUTE_CLEAR_RATE = 0.49;
	public static final double LOW_HEAP_ATTRIBUTE_CLEAR_RATE = 0.7;
	public static final double EXTREME_LOW_HEAP_ATTRIBUTE_CLEAR_RATE = 1.0;


	public static final double CLEAR_SESSION_HEAP_THRESHOLD = Double.parseDouble(System.getProperty(
			"com.dynatrace.easytravel.tomcat.AutomaticMemoryManager.clearSessionHeapThreshold", "0.75"));
	public static final double CLEAR_ATTRIBUTE_HEAP_THRESHOLD = Double.parseDouble(System.getProperty(
			"com.dynatrace.easytravel.tomcat.AutomaticMemoryManager.clearAttributeHeapThreshold", "0.75"));


	// ------------------------------------------------------------- Properties


	/**
	 * Creates an instance that automatically manage the sessions with respect to the maximum heap memory.
	 *
	 * @return
	 * @author stefan.moschinski
	 */
	public AutomaticMemoryManager(String serverName) {
		this(serverName, new MemoryUtils(ManagementFactory.getMemoryMXBean()));
	}

	AutomaticMemoryManager(String serverName, MemoryUtils memUtils) {
		this.memUtils = memUtils;
		this.serverName = serverName;
		this.sessionCleanUpRunning = new AtomicBoolean(false);
	}

	/**
	 * Return descriptive information about this Manager implementation and
	 * the corresponding version number, in the format <code>&lt;description&gt;/&lt;version&gt;</code>.
	 */
	@Override
	public String getInfo() {
		return info;
	}

	/**
	 * Return the descriptive short name of this Manager implementation.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Check and expire if there are too many sessions.
	 */
	@Override
	public Session createSession(String sessionId) {
		clearSessions();
		// do normal creation now
		return super.createSession(sessionId);
	}


	public long getMinSessionLifeTime() {
		return minSessionLifeTime;
	}


	public void setMinSessionLifetime(long minSessionLifeTime) {
		this.minSessionLifeTime = minSessionLifeTime;
	}

	void clearSessions() {
		if (sessionCleanUpRunning.get()) {
			if (log.isDebugEnabled()) {
				log.debug("A session clean up is already running, not starting another one");
			}
			return;
		}

		if (memUtils.getHeapUsage() >= CLEAR_SESSION_HEAP_THRESHOLD) {
			// invoke GC manually and see whether clearing of sessions is necessary afterwards
			System.gc();
			System.gc();

			double heapUsage;
			if ((heapUsage = memUtils.getHeapUsage()) >= CLEAR_SESSION_HEAP_THRESHOLD) {
				clearSessionLowHeap(heapUsage);
			}
		} else if (System.currentTimeMillis() - lastCheckSessions > minSessionLifeTime) {
			clearSessions(STANDARD_SESSION_CLEAR_RATE, minSessionLifeTime);
			lastCheckSessions = System.currentTimeMillis();
		}
	}

	private void clearSessionLowHeap(double heapUsage) {
		log.info(TextUtils.merge(
				"{0}: Too much heap is used ({1} % of maximum heap), enforcing the cleaning of old sessions", serverName,
				heapUsage * 100));

		if (heapUsage < 0.9) {
			clearSessions(LOW_HEAP_SESSION_CLEAR_RATE, (long) (minSessionLifeTime / 2.0));
		} else {
			clearSessions(EXTREME_LOW_HEAP_SESSION_CLEAR_RATE, 0);
		}
	}

	public void clearRandomSessions() {
		if (memUtils.getHeapUsage() >= 0.8) {
			double clearRate = (1 - memUtils.getHeapUsage()) / 2.0;
			clearSessionsInternal(clearRate, 0, new HashSet<Session>(sessions.values()));
		}
	}

	/**
	 * Expires sessions
	 *
	 * @param clearRate
	 * @param respectTimeThreshold
	 * @author stefan.moschinski
	 */
	private void clearSessions(double clearRate, long minSessionLifeTime) {
		if (sessionCleanUpRunning.compareAndSet(false, true)) {
			try {
				Set<Session> sessionsSorted = getSortedSessions(clearRate);
				clearSessionsInternal(clearRate, minSessionLifeTime, sessionsSorted);
			} finally {
				sessionCleanUpRunning.set(false);
			}
		}
	}

	private void clearSessionsInternal(double clearRate, long minSessionLifeTime, Collection<Session> sessions) {
		int activeSessions = getActiveSessions();
		int count = 0;
		for (int i = 0; i < getSessionsToClear(activeSessions, clearRate) && sessions.iterator().hasNext(); i++) {
			Session session = sessions.iterator().next();
			// remove the Session from the sorted list to look at the next one in this loop
			sessions.remove(session);

			// calculate the session life time
			long sessionLifeTime = System.currentTimeMillis() - session.getCreationTimeInternal();
			if (sessionLifeTime < minSessionLifeTime) {
				break;
			}
			if (log.isDebugEnabled()) {
				log.debug("Expiring session '" + session.getId() + "' after " + sessionLifeTime + " ms.");
			}
			// do not expire the sessions, rather simply remove them in order
			// to not have icefaces log ugly warnings
			remove(session);
			// expireSession(session.getId());

			count++;
		}

		log.info("Removed " + count + " sessions out of " + activeSessions + " sessions (Rate: " + clearRate + "/MinLifetime: " + minSessionLifeTime + ") to free excessive memory kept in sessions by icefaces");
	}

	long getSessionsToClear(int sessions, double clearRate) {
		return Math.round(sessions * clearRate);
	}


	/**
	 * Before doing the normal expire, clean out session attributes if there
	 * are too many sessions active to avoid OOM.
	 */
	@Override
	public void processExpires() {
		if (memUtils.getHeapUsage() >= CLEAR_ATTRIBUTE_HEAP_THRESHOLD) {
			clearAttributesLowHeap();
		} else if (System.currentTimeMillis() - lastCheckAttributes > minSessionLifeTime) {
			clearAttributes(STANDARD_ATTRIBUTE_CLEAR_RATE);
			lastCheckAttributes = System.currentTimeMillis();
		}
		super.processExpires();
	}

	private void clearAttributesLowHeap() {
		double heapUsage = memUtils.getHeapUsage();
		log.info(TextUtils.merge("{0}: Too much heap is used ({1} % of maximum heap), enforcing attribute cleaning",
				serverName, heapUsage * 100));
		if (heapUsage < 0.9) {
			clearAttributes(LOW_HEAP_ATTRIBUTE_CLEAR_RATE);
		} else {
			clearAttributes(EXTREME_LOW_HEAP_ATTRIBUTE_CLEAR_RATE);
		}
	}

	protected void clearAttributes(double clearRate) {
		// clean sessions after the number of sessions that we keep intact
		Set<Session> sessionsSorted = getSortedSessions(clearRate);

		int removedAttributes = 0;
		Iterator<Session> it = sessionsSorted.iterator();
		for (int i = 0; i < Math.round(clearRate * sessionsSorted.size()) && it.hasNext(); i++) {
			// we remove attributes which keep most of of the memory in icefaces/easyTravel from older sessions in order to
			// prevent OOM
			Session session = it.next();
			if (session.isValid()) {
				try {
					HttpSession httpSession = session.getSession();

					/*Enumeration<String> atts = httpSession.getAttributeNames();
					while(atts.hasMoreElements()) {
						String att = atts.nextElement();
						log.info("Had attribute: " + att);
					}*/

					for (String att : ATTRIBUTES) {
						removedAttributes += removeAttrbute(att, httpSession);
					}
				} catch (IllegalStateException e) {
					log.info(TextUtils.merge(
							"{0}: Had exception when cleaning session, probably session got invalidated in-between: {1}",
							serverName, e.getMessage()));
				}
			}
		}

		log.info(serverName + ": Cleared " + removedAttributes + " attributes in " +
				Math.round(clearRate * sessionsSorted.size()) +
				" out of " + sessionsSorted.size() + " sessions (Rate: " + clearRate + ") to free excessive memory kept in sessions by icefaces");
	}

	private Set<Session> getSortedSessions(double clearRate) {
		Comparator<? super Session> comparator = getSessionComparator(clearRate);
		Set<Session> sessionsSorted;
		if (comparator == null) {
			sessionsSorted = new HashSet<Session>(sessions.values().size());
		} else {
			sessionsSorted = new TreeSet<Session>(comparator);
		}
		sessionsSorted.addAll(sessions.values());
		return sessionsSorted;
	}

	private Comparator<? super Session> getSessionComparator(double clearRate) {
		if (clearRate >= 1.0) {
			if (log.isDebugEnabled()) {
				log.debug(TextUtils.merge("Clear rate: {0}, no need to sort collection", clearRate));
			}
			return null;
		}
		return sessionComparator;
	}

	private int removeAttrbute(String att, HttpSession httpSession) {
		if (httpSession.getAttribute(att) == null) {
			return 0;
		}

		httpSession.removeAttribute(att);
		return 1;
	}

	private static class SessionComparator implements Comparator<Session> {

		@Override
		public int compare(Session o1, Session o2) {
			// ignore possible overflow here as we expect to have creation time not too far away
			long diff = o1.getCreationTimeInternal() - o2.getCreationTimeInternal();

			// sort oldest first
			return diff > 0 ? 1 : (diff < 0 ? -1 : 0);
		}
	}
}
