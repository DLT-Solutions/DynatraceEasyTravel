package com.dynatrace.diagnostics.uemload;

import java.util.Random;


public class RandomWalk implements Series {

	private final Random random;

	private final double min;
	private final double max;
	private final double maxStep;
	private final double avg;
	private final double tension;

	private double cur;

	/**
	 * @param runs number of runs that should be performed in the 
	 * given time interval
	 * @author stefan.moschinski
	 */
	public RandomWalk(double runs) {
		this(runs, runs);
	}

	public RandomWalk(double min, double max) {
		this(min, max, (max - min) / 20, (max + min) / 2, (max + min) / 2, (max - min) / 4000);
	}

	public RandomWalk(double min, double max, double maxStep, double start, double avg, double tension) {
		this.random = new Random();
		this.min = min;
		this.max = max;
		this.maxStep = maxStep;
		this.avg = avg;
		this.tension = tension;
		this.cur = start;
	}

	// =MAX(@min;MIN(@max;@cur+RAND()/10-0,05)+IF(@cur<@avg;@tension;-@tension))
	@Override
	public double next() {
		double res = cur;
		double correction = cur < avg ? tension : -tension;
		double next = cur + random.nextDouble() * maxStep * 2 - maxStep + correction;
		cur = Math.max(min, Math.min(max, next));
		return res;
	}
}
