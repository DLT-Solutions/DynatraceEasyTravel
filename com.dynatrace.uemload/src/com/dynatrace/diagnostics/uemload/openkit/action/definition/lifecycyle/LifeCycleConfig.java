package com.dynatrace.diagnostics.uemload.openkit.action.definition.lifecycyle;

public class LifeCycleConfig {
	public final int minAddedDuration, maxAddedDuration;
	public final int minExtraDuration, maxExtraDuration;
	public final int minEndDelay, maxEndDelay;

	private LifeCycleConfig(int minAddedDuration, int maxAddedDuration, int minExtraDuration, int maxExtraDuration, int minEndDelay, int maxEndDelay) {
		this.minAddedDuration = minAddedDuration;
		this.maxAddedDuration = maxAddedDuration;
		this.minExtraDuration = minExtraDuration;
		this.maxExtraDuration = maxExtraDuration;
		this.minEndDelay = minEndDelay;
		this.maxEndDelay = maxEndDelay;
	}

	public static LifeCycleConfigBuilder createOrDidLoadAfter(int minDelay, int maxDelay) {
		return new LifeCycleConfigBuilder(minDelay, maxDelay);
	}

	public static class LifeCycleConfigBuilder {
		public final int minAddedDuration, maxAddedDuration;

		private LifeCycleConfigBuilder(int minAddedDuration, int maxAddedDuration) {
			this.minAddedDuration = minAddedDuration;
			this.maxAddedDuration = maxAddedDuration;
		}

		public NamedLifeCycleConfigBuilder startOrWillAppearAfter(int minDelay, int maxDelay) {
			return new NamedLifeCycleConfigBuilder(minDelay, maxDelay);
		}

		public class NamedLifeCycleConfigBuilder {
			public final int minExtraDuration, maxExtraDuration;

			private NamedLifeCycleConfigBuilder(int minExtraDuration, int maxExtraDuration) {
				this.minExtraDuration = minExtraDuration;
				this.maxExtraDuration = maxExtraDuration;
			}

			public LifeCycleConfig resumeOrDidAppearAfter(int minDelay, int maxDelay) {
				return new LifeCycleConfig(minAddedDuration, maxAddedDuration, minExtraDuration, maxExtraDuration, minDelay, maxDelay);
			}
		}
	}
}
