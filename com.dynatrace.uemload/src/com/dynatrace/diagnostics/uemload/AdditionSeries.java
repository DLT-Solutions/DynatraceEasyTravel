package com.dynatrace.diagnostics.uemload;

import java.text.NumberFormat;
import java.util.Locale;


public class AdditionSeries implements Series {

	private final Series[] series;

	public AdditionSeries(Series... series) {
		this.series = series;
	}
	
	@Override
	public double next() {
		double res = 0;
		for(int i = 0; i < series.length; i++) {
			res += series[i].next();
		}
		return res;
	}
	
	public static void main(String[] args) {
		Series s = 
			new AdditionSeries(
				new SinusSeries(0, 100, 60),
				new LinearSeries(0, 1)
			);
		NumberFormat format = NumberFormat.getInstance(Locale.GERMAN);
		for(int i = 0; i < 360; i++) {
			System.err.println(format.format(s.next()));
		}
	}

}
