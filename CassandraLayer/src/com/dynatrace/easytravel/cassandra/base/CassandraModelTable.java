package com.dynatrace.easytravel.cassandra.base;

import com.dynatrace.easytravel.cassandra.entities.CassandraEntity;
import com.dynatrace.easytravel.jpa.Base;

import java.util.List;
import java.util.stream.Collectors;

import static com.datastax.driver.mapping.Mapper.Option.*;


public abstract class CassandraModelTable<E extends CassandraEntity<M>, M extends Base> extends CassandraObjectMapperTable<E>  {
    public CassandraModelTable(EtCluster cluster, Class entityClass) {
        super(cluster, entityClass);
    }

    public void addModel(M model) {
        E entity = getEntityFromModel(model);
        addEntity(entity);
    }

    public void deleteModel(M model) {
        E entity = getEntityFromModel(model);
        deleteEntity(entity);
    }

    protected E getEntityFromModel(M model) {
        E entity = getEntity(model);
        return entity;
    }

    protected abstract E getEntity(M model);

    public void updateModel(M model) {
        E entity = getEntityFromModel(model);
        updateEntity(entity);
    }

    public <K> M getModel(K key) {
        //TODO remove tracing
        //TODO check for null values
        E entitiy = mapper.get(key, tracing(true));
        return (entitiy != null ? entitiy.createModel() : null);
    }

    public List<M> getAllModels() {
        List<E> entities = getAllEntities();
        return entitiesToModels(entities);
    }
    
    public List<M> getModelsWithLimit(int limit) {
    	List<E> entities = getEntitiesWithLimit(limit);
    	return entitiesToModels(entities);
    }

    protected List<M> entitiesToModels(List<E> entities) {
        return entities.stream().map(CassandraEntity<M>::createModel).collect(Collectors.toList());
    }

}
