package com.dynatrace.diagnostics.uemload;

import java.text.NumberFormat;
import java.util.Locale;


public class SinusSeries implements Series {
	
	private final double min;
	private final double max;
	private final double step;
	
	private double cur;
	
	public SinusSeries(double min, double max, double period) {
		this.min = min;
		this.max = max;
		this.step = 2 * Math.PI / period;
		this.cur = 0;
	}
	
	@Override
	public double next() {
		double res = min + (Math.sin(cur) / 2 + 0.5) * (max - min);
		cur += step;
		return res;
	}
	
	public static void main(String[] args) {
		SinusSeries walk = new SinusSeries(0, 100, 60);
		NumberFormat format = NumberFormat.getInstance(Locale.GERMAN);
		for(int i = 0; i < 1000; i++) {
			System.err.println(format.format(walk.next()));
		}
	}

}
