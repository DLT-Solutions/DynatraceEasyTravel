package com.dynatrace.diagnostics.uemload.thirdpartycontent;


/**
 * copy of com.dynatrace.diagnostics.global.StatCounter from dynaTrace - trunk
 */
public class StatCounter  {
	public double last;
	public double sum;
	public double min = Double.MAX_VALUE;
	public double max;
    public int count;
    public double first;

    public StatCounter() {
    }

    public StatCounter(double value) {
        add(value);
    }

    /**
     * value<0 the value will be ignored.
     */
    public void add(double value) {
    	// ignore negative values
    	if (value < 0d) {
    		return;
    	}
    	if (count == 0) {
    		first = value;
    	}
        count++;
        last = value;
        sum += value;
        if (last > max) {
        	max = last;
        }
        if (last < min) {
        	min = last;
        }
    }

    public double getAvg() {
        if (count == 0)
            return -1.0;
        return sum / count;
    }

	public int getCount() {
		return count;
	}

	public double getLast() {
		return count == 0 ? -1 :last;
	}

	public double getFirst() {
		return count == 0 ? -1 : first;
	}

	public double getMax() {
		return count == 0 ? -1 :max;
	}

	public double getMin() {
		return count == 0 ? -1 :min;
	}

	public double getSum() {
		return count == 0 ? -1 :sum;
	}

	public void reset() {
		last = 0;
		sum = 0;
		min = Double.MAX_VALUE;
		max = 0;
	    count = 0;
	    first = 0;
	}

	public void update(double last, double sum, double min, double max, int count) {
		update(last, sum, min, max, count, 0);
	}

	public void update(double last, double sum, double min, double max, int count, double first) {
		this.last = last;
		this.sum = sum;
		this.min = min;
		this.max = max;
		this.count = count;
		this.first = first;
	}

	public void add(double last0, double sum0, double min0, double max0, int count0) {
        last = (last0 != -1) ? last0 : last;
        sum = (sum0 != -1) ? sum + sum0 : sum;
        min = min < min0 ? min : min0;
        max = max > max0 ? max : max0;
        count = count + count0;
	}

	@Override
	public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("StatCounter values: count=").append(count).append(", sum=").append(sum);
    	sb.append(", avg=").append(getAvg()).append(", min=").append(min);
    	sb.append(", max=").append(max).append(" first=").append(first);
    	return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StatCounter other = (StatCounter) obj;
        if (count != other.count) {
            return false;
        }
        if (Double.doubleToLongBits(last) != Double.doubleToLongBits(other.last)) {
            return false;
        }
        if (Double.doubleToLongBits(first) != Double.doubleToLongBits(other.first)) {
            return false;
        }
        if (Double.doubleToLongBits(max) != Double.doubleToLongBits(other.max)) {
            return false;
        }
        if (Double.doubleToLongBits(min) != Double.doubleToLongBits(other.min)) {
            return false;
        }
        if (Double.doubleToLongBits(sum) != Double.doubleToLongBits(other.sum)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + count;
        long temp;
        temp = Double.doubleToLongBits(last);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(first);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(max);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(min);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(sum);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

}
