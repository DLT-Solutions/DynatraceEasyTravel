package com.dynatrace.diagnostics.uemload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


// Random set implemented by 2 lists and a total weight
// adding (a, 1), (b, 2), (c, 3) results in
// items = (a, b, c)
// weight = (1, 3, 6)
// totalWeight = 6
// binary search in weight with random number between 1 and 6 results in
// slot 0 with probability 1 / 6, slot 1 with 2 / 6, slot 3 with 3 / 6.
public class RandomSet<T> implements IterableSet<T>{

	private final Random random = new Random();

	private final List<T> items = new ArrayList<T>();
	private final List<Integer> weights = new ArrayList<Integer>();

	private int totalWeight;

	public void add(T data, int weight) {
		totalWeight += weight;
		items.add(data);
		weights.add(totalWeight);
	}

	public void add(RandomSetEntry<T> entry) {
		add(entry.value, entry.weight);
	}

	public T getRandom() {
		int key = random.nextInt(totalWeight) + 1;
		int index = Collections.binarySearch(weights, key);

		// (-(insertion point) - 1
		if(index < 0) {
			index = - (index + 1);
		}

		return items.get(index);
	}

	public void setWeightInPercent(T value, int percent) {
		int prevSum = 0;
		for(int i = 0; i < items.size(); i++) {
			Integer sum = weights.get(i);
			int curWeight = sum - prevSum;
			prevSum = sum;
			if(value.equals(items.get(i))) {
				int restWeight = totalWeight - curWeight;
				double p = percent / 100.0;
				double newWeightDouble = (p * restWeight) / (1 - p);
				int newWeight = (int) Math.round(newWeightDouble);
				int diff = newWeight - curWeight;

				totalWeight += diff;
				for(int j = i; j < items.size(); j++) {
					weights.set(j, weights.get(j) + diff);
				}
				break;
			}
		}
	}

	@Override
	public T getNext() {
		return getRandom();
	}
	
	public boolean isEmpty() {
		return totalWeight == 0;
	}

	public static class RandomSetEntry<T> {
		public T value;
		public int weight;

		public RandomSetEntry(T value, int weight) {
			this.value = value;
			this.weight = weight;
		}
	}
}
