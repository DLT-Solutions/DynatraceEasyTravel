package com.dynatrace.easytravel.launcher.sync;

public interface Predicate<T> {

	final class AllMatcher<T> implements Predicate<T> {
		@Override
		public boolean eval(T obj) {
			return true;
		}

		@Override
		public boolean shouldStop() {
			return false;
		}
	}

	boolean shouldStop();

	boolean eval(T obj);
}
