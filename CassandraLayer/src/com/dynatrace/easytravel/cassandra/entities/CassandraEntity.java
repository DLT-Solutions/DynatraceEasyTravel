package com.dynatrace.easytravel.cassandra.entities;

/**
 * 
 * @author Rafal.Psciuk
 *
 */
public interface CassandraEntity<T> {
    T createModel();
}
