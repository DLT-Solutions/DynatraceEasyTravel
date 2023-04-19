package com.dynatrace.diagnostics.uemload.thirdpartycontent;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * copy of com.dynatrace.diagnostics.util.IntervalSet
 *
 * @author peter.lang
 */
public class IntervalSet implements Iterable<IntervalSet.Interval> {

	public interface Interval {
		public double getStart();
		public double getEnd();
		public double getDuration();
	}

	private static class Node implements Interval {
		private Node previous;
		private Node next;
		private double start;
		private double end;

		public Node() {
			this(null, null);
		}

		public Node(Node next, Node previous) {
			this.next = next;
			this.previous = previous;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			sb.append(start);
			sb.append(",");
			sb.append(end);
			sb.append("]");
			return sb.toString();
		}

		@Override
		public double getStart() {
			return start;
		}

		@Override
		public double getEnd() {
			return end;
		}

		@Override
		public double getDuration() {
			return end - start;
		}

	}

	// Inv: head != null
	private Node head;

	public IntervalSet() {
		head = new Node();
		head.next = head.previous = head;
	}

	/**
	 * creates an IntervalSet with the values given in the array
	 *
	 *
	 * @param values contains start and stop values and must have even size. format: start1,end1,start2,end2,...startn,endn. if null, an empty IntervalSet is created
	 * @author bernhard.lackner
	 */
	public IntervalSet(double[] values) {
		this();
		if(values!=null){
			if(values.length%2==1)
				throw new IllegalArgumentException("Array of values must have even size (must be a list of start and stop times for intervals)!");

			for(int i=0;i<values.length;i+=2){
				add(values[i],values[i+1]);
			}
		}

	}

	private Node addBefore(Node node) {
		Node newNode = new Node(node, node.previous);
		newNode.previous.next = newNode;
		newNode.next.previous = newNode;
		return newNode;
	}

	private void remove(Node e) {
		if (e == head) {
			throw new NoSuchElementException();
		}
		e.previous.next = e.next;
		e.next.previous = e.previous;
		e.next = e.previous = null;
	}

	public void add(double start, double end) {
		Node cur = head.next;
		while (cur != head && cur.end < start) {
			cur = cur.next;
		}

		// Inv: cur == head || cur.end >= start
		if(cur == head) {
			Node node = addBefore(head);
			node.start = start;
			node.end = end;
		} else {
			// Inv: cur != head && cur.end >= start
			if(cur.start <= end) {
				// Intervals overlap, so merge them
				cur.start = Math.min(cur.start, start);
				cur.end = Math.max(cur.end, end);

				Node n = cur.next;
				while(n != head && n.start <= cur.end && n.end >= cur.start) {
					// cur and n now overlap, so merge them
					cur.start = Math.min(cur.start, n.start);
					cur.end = Math.max(cur.end, n.end);
					Node r = n;
					n = n.next;
					remove(r);
				}
			} else {
				// Interval does not overlap, add new interval
				Node node = addBefore(cur);
				node.start = start;
				node.end = end;
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Node cur = head.next;
		String sep = "";
		while (cur != head) {
			sb.append(sep);
			sep = ";";
			sb.append(cur);
			cur = cur.next;
		}
		return sb.toString();
	}

	public double getSize() {
		Node cur = head.next;
		double res = 0;
		while (cur != head) {
			res += cur.end - cur.start;
			cur = cur.next;
		}
		return res;
	}

	/**
	 *
	 * calculates the first start time
	 * @return min start time or Double.MAX_VALUE if empty
	 * @author bernhard.lackner
	 */
	public double getMinStart(){
		Node cur = head.next;
		double min = Double.MAX_VALUE;
		while (cur != head) {
			min=Math.min(min, cur.start);
			cur = cur.next;
		}
		return min;

	}

	/**
	 * calculates the last end time
	 *
	 * @return max end time or 0 if empty
	 * @author bernhard.lackner
	 */
	public double getMaxEnd(){
		Node cur = head.next;
		double max = 0;
		while (cur != head) {
			max=Math.max(max, cur.end);
			cur = cur.next;
		}
		return max;
	}

	@Override
	public Iterator<Interval> iterator() {
		return new Iterator<Interval>() {

			private Node cur = head.next;

			@Override
			public boolean hasNext() {
				return cur != head;
			}

			@Override
			public Interval next() {
				Interval res = cur;
				cur = cur.next;
				return res;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public void addAll(IntervalSet intervalSet) {
		Iterator<Interval> it = intervalSet.iterator();
		while (it.hasNext()) {
			Interval interval = it.next();
			this.add(interval.getStart(), interval.getEnd());
		}
	}



	/**
	 *
	 *
	 * @return array of double values in following format: start1,end1,start2,start2,...,startn,endn. if empty, null is returned
	 * @author bernhard.lackner
	 */
	public double[] getAsArray(){

		//calculate size
		Node cur = head.next;
		int size=0;
		while (cur != head) {
			size++;
			cur = cur.next;
		}

		double[] res=null;
		if(size>0){
			//create array and set values
			res=new double[size*2];
			cur = head.next;
			int i=0;
			while (cur != head) {
				res[i]=cur.start;
				res[i+1]=cur.end;
				cur = cur.next;
				i+=2;
			}
		}


		return res;

	}
}
