package com.dynatrace.easytravel.business.webservice.transferobj;

import org.apache.commons.lang3.ArrayUtils;


public abstract class Page<T> {

    private int fromIdx;
    private int count;
    private int total;
    protected T[] objects;


    public Page(T[] objects, int fromIdx, int count, int total) {
        this.objects = ArrayUtils.clone(objects);
        this.fromIdx = fromIdx;
        this.count = count;
        this.total = total;
    }


    public int getFromIdx() {
        return fromIdx;
    }


    public int getCount() {
        return count;
    }


    public int getTotal() {
        return total;
    }


}