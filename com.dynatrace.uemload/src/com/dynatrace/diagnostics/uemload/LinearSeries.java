package com.dynatrace.diagnostics.uemload;



public class LinearSeries implements Series {
	
	private final double step;
	private double cur;
	
	public LinearSeries(double start, double step) {
		this.cur = start;
		this.step = step;
	}
	
	@Override
	public double next() {
		double res = cur;
		cur += step;
		return res;
	}
	
}
