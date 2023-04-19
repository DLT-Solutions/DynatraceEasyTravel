package com.dynatrace.easytravel.tomcat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.Callable;


public class CleaningTestUtil {

	MemoryMXBean memoryBean;
	MemoryUsage memoryUsage;


	public CleaningTestUtil() {
		memoryBean = mock(MemoryMXBean.class);
		memoryUsage = mock(MemoryUsage.class);
	}

	public void setUp() {
		when(memoryBean.getHeapMemoryUsage()).thenReturn(memoryUsage);
		setStandardHeap();
	}

	public void setStandardHeap() {
		reset(memoryUsage);
		when(memoryUsage.getUsed()).thenReturn(50l);
		when(memoryUsage.getMax()).thenReturn(100l);
	}

	public void setLowHeap() {
		reset(memoryUsage);
		when(memoryUsage.getUsed()).thenReturn(85l);
		when(memoryUsage.getMax()).thenReturn(100l);
	}

	public void setExtremeLowHeap() {
		reset(memoryUsage);
		when(memoryUsage.getUsed()).thenReturn(99l);
		when(memoryUsage.getMax()).thenReturn(100l);
	}

	public Callable<Void> setStandardHeapCallable() {
		return new Callable<Void>(){
			@Override
			public Void call() throws Exception {
				setStandardHeap();
				return null;
			}
		};
	}

	public Callable<Void> setLowHeapCallable() {
		return new Callable<Void>(){
			@Override
			public Void call() throws Exception {
				setLowHeap();
				return null;
			}
		};
	}

	public Callable<Void> setExtremeLowHeapCallable() {
		return new Callable<Void>(){
			@Override
			public Void call() throws Exception {
				setExtremeLowHeap();
				return null;
			}
		};
	}

	public MemoryMXBean getMemoryBean() {
		return memoryBean;
	}

	public void nextMillisecond() throws InterruptedException {
		long start = System.currentTimeMillis();
		while(start == System.currentTimeMillis()) {
			Thread.sleep(10);
		}
		// give it a little extra sleep to see whether ExpiryLimitManagerTest work now in CI
		Thread.sleep(10);
	}

	public void waitFor(long waitInMillis) throws InterruptedException {
		Thread.sleep(waitInMillis);
	}

	public int getExpectSessionNo(AutomaticMemoryManager manager, double clearRate) {
		return (int) (manager.getActiveSessions() - manager.getSessionsToClear(manager.getActiveSessions(), clearRate));
	}

}
