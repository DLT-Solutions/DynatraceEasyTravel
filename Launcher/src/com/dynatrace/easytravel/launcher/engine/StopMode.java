package com.dynatrace.easytravel.launcher.engine;


public enum StopMode {
	SEQUENTIAL,
	PARALLEL,
	NONE {
		@Override
		public boolean isStoppable() {
			return false;
		}
	};

	public boolean isStoppable() {
		return true;
	}
}
