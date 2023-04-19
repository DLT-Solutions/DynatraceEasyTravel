package com.dynatrace.easytravel.launcher.plugin.restore;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import java.util.*;

/**
 * Base data structure for restoring default plugins state
 *
 * cwpl-rorzecho
 */
public class RestoreDataContainer {

    private Table<Enum<?>, Long, String> table;

    public RestoreDataContainer() {
        table = TreeBasedTable.create();
    }

    public void add(Enum<?> type, String value) {
        table.put(type, System.nanoTime(), value);
    }

    public void addAll(Enum<?> type, Collection<String> values) {
        for (String value : values) {
            add(type, value);
        }
    }

    public void addAll(Enum<?> type, String[] values) {
        for (String value : values) {
            add(type, value);
        }
    }

    public Collection<String> getAll(Enum<?> type) {
        return row(type).values();
    }

    public Map<Long, String> getAsMap(Enum<?> type) {
        return row(type);
    }

    public void remove(Enum<?> type, String value) {
        row(type).values().remove(value);
    }

    public void removeAll(Enum<?> type, Collection<String> values) {
        for (String value : values) {
            remove(type, value);
        }
    }

    public boolean contains(Enum<?> type, String value) {
        return row(type).values().contains(value);
    }

    public boolean containsAll(Enum<?> type, Collection<String> values) {
        return row(type).values().containsAll(values);
    }

    public boolean containsAll(Enum<?> type, String[] values) {
        return row(type).values().containsAll(Arrays.asList(values));
    }

    public void clear(Enum<?> type) {
        row(type).clear();
    }

    public boolean isEmpty(Enum<?> type) {
        return row(type).isEmpty();
    }

    public int size(Enum<?> type) {
        return row(type).size();
    }

    public Set<Enum<?>> rowKey() {
        synchronized (table) {
            return table.rowKeySet();
        }
    }

    public Map<Long, String> row(Enum<?> type) {
        synchronized (table) {
            return table.row(type);
        }
    }
}
