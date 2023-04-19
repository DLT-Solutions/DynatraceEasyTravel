package com.dynatrace.diagnostics.uemload.scenarios.easytravel;

/**
 * <p>This class provides a rolling set of monitor and stepIds.</p>
 * <p>Each time next() is called the next test step is set, if the current monitor has no more steps the next monitor is set and so on.</p>
 * <p>Additionally a fixed application is provided.</p>
 *
 * <p>This class is needed for the convergence project to provide some pseudo-realistic headers.</p>
 *
 * <p>It's possible to set the number of steps for each monitor by using the last character, eg. 'FOOMON_3' means 3 steps.</p>
 *
 * <p>Usage: call next() before each page, and then call getMonitorId() and getStepId()</p>
 *
 * @author cwat-hgrining
 *
 */
public class SyntheticTestHeadersGenerator {

	// The app id (encrypted)
	private static final String APP_ID = "ifNt6CLA1zXeToK5kzz2bI0Pl0jnbUkAwvYOrx9gHI/vJEGNWe02b9ZRP4/MJivqyaVJ4Hoz68WpwkKpy1HE+1Chste+qfLHnGmnFgSrccIgrrZv1FXbVcvA/M1qtN2toCWsc95sqfB5wCVZC1FRtZba8/dqAUjFF25j37NlkdCkt6tXxszHpbwZ/F80Zje1BvSS2i5MrqLCKHzmV0j+GgxuchbzMKikhKUmm3+7kfZKelkNP9i4i27Kn4X4UmMGVk35oXrTEiW0gCN7b9yUHnxEAluEFKXDKPDhH1eJWmQsqoZbx4uEwysLs36QHIDkHx+QQ6QYHL0jnXcJoTKBZxkE/vrmJJObiwyt5YWlKMn98+HQfYSFtP4sm2+NhcMJgFXV6wu5Zokng8VtrXkpP2pCHaJMf4dwtVye2G6yLQfs9a6U5Jr4C+BmDcrMxeyuqcNARagYRSL6u2J/CKNmNgM/M9LvRloyYpe2nbY6dFg/erIFDHbJzqIWUQfeIvj94ZMOTOlVvb2sBpCmV9fjz0NtMgFewLtLm5o2v9TPeLOGKeXVuHw1QCkjr5+idaqj8TnTYYGgLQeH0/jMvEXtC6XvfjGsVvQKKREfv61OlVkv0ZVqXKlO12A1QyuWSregD13xyz9b/BYwDpqFQVLLMoRnXtRNaKpGi8245k2GGqI=";

	// The monitor Ids (encrypted)
	private static final String[] monitorIds = new String[] {
	"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"};

	// The id for each step (goes into the PI header value)
	private static final String[] stepIds = new String[] {"1", "2", "3", "4", "5", "6", "7"};

	// The number of steps for each monitor
	private static final int[] stepCountsForMonitors = new int[] {3, 4, 7};

	public static int monitorCount = -1;
	public static int stepCount = -1;

	public String getApplicationId() {
		return APP_ID;
	}

	public String getMonitorId() {
		return monitorIds[monitorCount];
	}

	public String getStepId() {
		return stepIds[stepCount];
	}

	public void next() {
		if (monitorCount < 0) monitorCount = 0;

		int stepsForMonitor = stepCountsForMonitors[monitorCount];

		if (stepCount < stepsForMonitor-1) {
			stepCount++;
		} else {
			monitorCount++;
			if (monitorCount >= monitorIds.length) {
				monitorCount = 0;
			}
			stepCount = 0;
		}
	}

}
