package com.dynatrace.diagnostics.uemload.mobileopenkit.parameters;

import com.dynatrace.diagnostics.uemload.RandomSet;

import java.util.Arrays;

public class MobileParamDistribution {
	@SafeVarargs
	protected static <T> RandomSet<T> create(RandomSet.RandomSetEntry<T>... entries) {
		RandomSet<T> distribution = new RandomSet<>();
		Arrays.stream(entries).forEach(distribution::add);
		return distribution;
	}
}
