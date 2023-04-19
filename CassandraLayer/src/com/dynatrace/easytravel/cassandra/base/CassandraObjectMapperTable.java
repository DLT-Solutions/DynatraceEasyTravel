package com.dynatrace.easytravel.cassandra.base;

import com.datastax.driver.core.IndexMetadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.annotations.Table;
import com.dynatrace.easytravel.cassandra.entities.CassandraEntity;
import com.dynatrace.easytravel.util.TextUtils;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

public abstract class CassandraObjectMapperTable<E extends CassandraEntity> extends CassandraObject {

    private final MappingManager manager;
    private final String tableName;

    protected Mapper<E> mapper;
    private Class<E> entityClass;

    public CassandraObjectMapperTable(EtCluster cluster, Class<E> entityClass) {
        super(cluster);
        this.manager = cluster.getManager();
        this.tableName = getTableName(entityClass);
        this.entityClass = entityClass;
    }

    public void init() {
        this.mapper = manager.mapper(entityClass);
    }

    private String getTableName(Class<E> entityClass){
        if(entityClass.isAnnotationPresent(Table.class)){
            Table tableAnnotation = entityClass.getAnnotation(Table.class);
            return tableAnnotation.name();
        }
        return "";
    }

    @Override
    public void delete() {
        deleteIndexes();
        dropTable(tableName);
    }

    public void deleteIndexes() {
        String loggedKeyspace = getSession().getLoggedKeyspace();
        if(isPresent()){
            Collection<IndexMetadata> indexes = getSession().getCluster().getMetadata().getKeyspace(loggedKeyspace).getTable(tableName).getIndexes();
            indexes.stream().forEach(idx -> executeQuery(TextUtils.merge("DROP INDEX {0}", idx.getName())));
        }
    }

    @Override
    public boolean isPresent() {
        String loggedKeyspace = getSession().getLoggedKeyspace();
        return getSession().getCluster().getMetadata().getKeyspace(loggedKeyspace).getTable(tableName) != null;
    }

    public void addEntity(E entity) {
        mapper.save(entity);
    }

    public void updateEntity(E entity) {
        addEntity(entity);
    }

    public void deleteEntity(E entity) {
        mapper.delete(entity);
    }

    public <K> E getEntity(K key) {
        return mapper.get(key);
    }

    public List<E> getAllEntities() {
        String cql = TextUtils.merge("SELECT * FROM {0}", tableName);
        return runQueryAndMapResults(cql);
    }

    protected List<E> runQueryAndMapResults(String cql) {
        ResultSet rows = executeQuery(cql);
        return mapper.map(rows).all();
    }

    public List<E> getEntitiesWithLimit(int limit) {
        String cql = TextUtils.merge("SELECT * FROM {0} LIMIT {1}", tableName, limit);
        return runQueryAndMapResults(cql);
    }

    public int getCount() {
        String cql = MessageFormat.format("SELECT COUNT(*) FROM {0}", tableName);
        ResultSet rows = executeQuery(cql);
        long cnt = rows.one().getLong(0);
        return Long.valueOf(cnt).intValue();
    }
}