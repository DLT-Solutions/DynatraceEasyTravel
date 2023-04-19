package com.dynatrace.easytravel;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

import ch.qos.logback.classic.Logger;

public class AlternativeDeadlockInFrontend extends AbstractGenericPlugin  {
	
	private static final Logger log = LoggerFactory.make();

	private static final ReadWriteLock lock0 = new ReentrantReadWriteLock();
	private static final Object lock1 = new Object();
	private static final Lock lock2 = new ReentrantLock(true);

	@Override
	public Object doExecute(String location, Object... context) {
		// Create a cycle with three threads:
		//   thread 0 (current thread) holds lock0, wants lock1
		//   ... that is held by thread 1, which wants lock2.writeLock()
		//   ... that is held by thread 2, which wants lock0
		//   ... which is held by thread 0.
		//
		// lock0 is a class Java monitor, lock1 and lock2 are lock implementations
		// from the java.util.concurrent package using ownable synchronizers.
		//
		// When the deadlock has already been triggered earlier, the current thread
		// becomes stuck waiting for lock0 right away, launching no other threads.
		lock0.writeLock().lock();
		try {
			log.info("Thread {0}: acquired write permit for lock 0", Thread.currentThread().getName());

			// Thread 1
			new Thread() {
				public void run() {
					synchronized(lock1) {
						log.info("Thread {0}: acquired lock 1", Thread.currentThread().getName());

						// Thread 2
						new Thread() {
							public void run() {
								lock2.lock();
								try {
									log.info("Thread {0}: acquired lock 2",
											Thread.currentThread().getName());
									lock0.readLock().lock();
									try {
										log.info("Thread {0}: acquired read permit for lock 0 (impossible)",
												Thread.currentThread().getName());
									} finally {
										lock0.readLock().unlock();
									}
								} finally {
									lock2.unlock();
								}
							}
						}.start();

						// Wait for thread 2 to acquire write permit from lock2
						try {
							Thread.sleep(100);
						} catch (InterruptedException ie) {
							log.info(ie.getMessage());
						}

						lock2.lock();
						try {
							log.info("Thread {0}: acquired lock 2 (impossible)",
									Thread.currentThread().getName());
						} finally {
							lock2.unlock();
						}
					}
				}
			}.start();

			// Wait for thread 1 to acquire lock 1
			try {
				Thread.sleep(100);
			} catch (InterruptedException ie) {
				log.info(ie.getMessage());
			}

			synchronized(lock1) {
				log.info("Thread {0}: acquired lock 1 (impossible)", Thread.currentThread().getName());
			}
		} finally {
			lock0.writeLock().unlock();
		}
		
		return null;
	}
}
