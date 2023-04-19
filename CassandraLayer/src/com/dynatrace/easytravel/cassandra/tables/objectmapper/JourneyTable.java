package com.dynatrace.easytravel.cassandra.tables.objectmapper;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.datastax.driver.core.ResultSet;
import com.dynatrace.easytravel.cassandra.base.CassandraModelTable;
import com.dynatrace.easytravel.cassandra.base.EtCluster;
import com.dynatrace.easytravel.cassandra.entities.JourneyEntity;
import com.dynatrace.easytravel.jpa.business.Journey;

import com.dynatrace.easytravel.util.TextUtils;

/**
 * 
 * @author Rafal.Psciuk
 *
 */
public class JourneyTable extends CassandraModelTable<JourneyEntity, Journey> {
    public static final int JOURNEY_ID_NOT_SET = 0;
    public static final String JOURNEY_TABLE_NAME = "JourneyTable";
    public static final String JOURNEY_BY_TENANT_VIEW = "Journey_ByTenant_View";
    public static final String JOURNEY_BY_DESTINATION_VIEW = "Journey_ByDestination_View";
	private static final String JOURNEY_BY_NAME_VIEW = "Journey_ByName_View";
	private static final String JOURNEY_BY_START_VIEW = "Journey_ByStart_View";

    public JourneyTable(EtCluster cluster) {
        super(cluster, JourneyEntity.class);
    }

    @Override
    protected JourneyEntity getEntity(Journey model) {
        return new JourneyEntity(model);
    }

    @Override
    public void create() {
        executeQuery("CREATE TABLE IF NOT EXISTS " + JOURNEY_TABLE_NAME + "(" +
                "name text , " +
                "id int, " +
                "depLocation text," +
                "destLocation text," +
                "tenant text," +
                "fromDate timestamp," +
                "toDate timestamp," +
                "description text," +
                "amount double, " +
                "picture blob," +
                " PRIMARY KEY (id, name, tenant, destLocation, fromDate, toDate));");

        //TODO check if we can remove select columns
        executeQuery("CREATE MATERIALIZED VIEW IF NOT EXISTS " + JOURNEY_BY_TENANT_VIEW + " AS"
                + " SELECT id, depLocation, destLocation, fromDate, toDate, description, amount, picture FROM " + JOURNEY_TABLE_NAME
                + " WHERE tenant IS NOT NULL AND name IS NOT NULL AND destLocation IS NOT NULL and fromDate IS NOT NULL and toDate IS NOT NULL"
                + " PRIMARY KEY (tenant, name, id, destLocation, fromDate, toDate)"
                + " WITH CLUSTERING ORDER BY (tenant ASC)"
        );

        executeQuery("CREATE MATERIALIZED VIEW IF NOT EXISTS " + JOURNEY_BY_DESTINATION_VIEW + " AS"
                + " SELECT id, depLocation, destLocation, fromDate, toDate, description, amount, picture FROM " + JOURNEY_TABLE_NAME
                + " WHERE destLocation IS NOT NULL AND fromDate IS NOT NULL AND toDate IS NOT NULL AND tenant IS NOT NULL AND name IS NOT NULL"
                + " PRIMARY KEY (destLocation, fromDate, toDate, tenant, name, id)"
                + " WITH CLUSTERING ORDER BY (destLocation ASC)"
        );
        
        executeQuery("CREATE MATERIALIZED VIEW IF NOT EXISTS " + JOURNEY_BY_START_VIEW + " AS"
                + " SELECT id, depLocation, destLocation, fromDate, toDate, description, amount, picture FROM " + JOURNEY_TABLE_NAME
                + " WHERE depLocation IS NOT NULL AND destLocation IS NOT NULL AND fromDate IS NOT NULL AND toDate IS NOT NULL AND tenant IS NOT NULL AND name IS NOT NULL"
                + " PRIMARY KEY (depLocation, destLocation, fromDate, toDate, tenant, name, id)"
                + " WITH CLUSTERING ORDER BY (depLocation ASC)"
        );

        executeQuery("CREATE MATERIALIZED VIEW IF NOT EXISTS " + JOURNEY_BY_NAME_VIEW + " AS"
                + " SELECT name, depLocation, destLocation, fromDate, toDate, description, amount, picture FROM " + JOURNEY_TABLE_NAME
                + " WHERE tenant IS NOT NULL AND name IS NOT NULL AND destLocation IS NOT NULL and fromDate IS NOT NULL and toDate IS NOT NULL"
                + " PRIMARY KEY (name, id, tenant, destLocation, fromDate, toDate)"
                + " WITH CLUSTERING ORDER BY (name ASC)"
        );
    }

    @Override
    public void delete() {
        executeQuery(TextUtils.merge("DROP MATERIALIZED VIEW IF EXISTS {0}", JOURNEY_BY_TENANT_VIEW));
        executeQuery(TextUtils.merge("DROP MATERIALIZED VIEW IF EXISTS {0}", JOURNEY_BY_DESTINATION_VIEW));
        executeQuery(TextUtils.merge("DROP MATERIALIZED VIEW IF EXISTS {0}", JOURNEY_BY_NAME_VIEW));
        executeQuery(TextUtils.merge("DROP MATERIALIZED VIEW IF EXISTS {0}", JOURNEY_BY_START_VIEW));
        super.delete();
    }


    @Override
    public void addModel(Journey model) {
        if (model.getId() == JOURNEY_ID_NOT_SET) {
            model.setId(model.hashCode());
        }
        super.addModel(model);
    }

    public Collection<Journey> findJourneys(String destination, Date fromDate, Date toDate, boolean normalize){
        String cql = MessageFormat.format("SELECT * FROM {0} WHERE " +
                "destLocation = $${1}$$ AND (fromDate, toDate) >= (''{2,number,#}'', 0) " +
                "AND (fromDate, toDate) <= (''{3,number,#}'', ''{3,number,#}'')" + //NOTE this is not compatible with old Cassandra
                ";", JOURNEY_BY_DESTINATION_VIEW, destination, fromDate.getTime(), toDate.getTime());
        List<JourneyEntity> entities = runQueryAndMapResults(cql);
        return entitiesToModels(entities);
    }

    public Journey getJourneyById(Integer id){
        return getModel(id);
    }
    
    public Journey getJourneyByName(String name) {
    	String cql = MessageFormat.format("SELECT * FROM {0} WHERE " +
                "name = $${1}$$" +
                ";", JOURNEY_BY_NAME_VIEW, name);
    	Optional<Journey> first = runQueryAndMapResults(cql).stream().map(entity -> entity.createModel()).findFirst();
    	return  first.orElse(null);
    }

    public Journey getJourneyByIdNormalize(Integer id, boolean normalize){
        return getJourneyById(id);
    }

    public Collection<Journey> getJourneysByTenant(String tenantName){
        String cql = TextUtils.merge("SELECT * FROM {0} WHERE tenant = $${1}$$", JOURNEY_BY_TENANT_VIEW, tenantName);
        List<JourneyEntity> entities = runQueryAndMapResults(cql);
        return entitiesToModels(entities);
    }

    public Collection<Journey> getJourneysByTenant(String tenantName, int fromIdx, int count){
        String cql = TextUtils.merge("SELECT * FROM {0} WHERE tenant = $${1}$$ LIMIT {2}", JOURNEY_BY_TENANT_VIEW, tenantName, fromIdx+count);
        List<JourneyEntity> entities = runQueryAndMapResults(cql);
        List<JourneyEntity> limitedEntities = entities.subList(fromIdx, fromIdx + count);
        return entitiesToModels(limitedEntities);
    }

    public int getJourneyCountByTenant(String tenantName){
        String cql = TextUtils.merge("SELECT count(*) FROM {0} WHERE tenant = $${1}$$", JOURNEY_BY_TENANT_VIEW, tenantName);
        ResultSet rows = executeQuery(cql);
        long cnt = rows.one().getLong(0);
        return Long.valueOf(cnt).intValue();
    }

    public int getJourneyIndexByName(String tenantName, String journeyName){
        String cql = TextUtils.merge("SELECT id, tenant FROM {0} WHERE tenant = $${1}$$ AND name = $${2}$$;", JOURNEY_BY_TENANT_VIEW, tenantName, journeyName);

        ResultSet rows = executeQuery(cql);
        return (rows.isExhausted() ? 0 :  rows.one().getInt("id"));
    }
    
    public Collection<Integer> getAllJourneyIds() {
    	String cql = TextUtils.merge("SELECT id FROM {0}", JOURNEY_TABLE_NAME);
    	ResultSet result = executeQuery(cql);
    	return result.all().stream().map(row -> row.getInt(0)).collect(Collectors.toList());
    }
    
    public Collection<Integer> getJourneyIdsForDestination(String destinationLocation) {
    	String cql = TextUtils.merge("SELECT id FROM {0} WHERE destLocation = $${1}$$", JOURNEY_BY_DESTINATION_VIEW, destinationLocation);
    	ResultSet result = executeQuery(cql);
    	return result.all().stream().map(row -> row.getInt(0)).collect(Collectors.toList());
    }
    
    public Collection<Integer> getJourneyIdsForDeparture(String departureLocation) {
    	String cql = TextUtils.merge("SELECT id FROM {0} WHERE depLocation = $${1}$$", JOURNEY_BY_START_VIEW, departureLocation);
    	ResultSet result = executeQuery(cql);
    	return result.all().stream().map(row -> row.getInt(0)).collect(Collectors.toList());
    }
    
    public Collection<Journey> getMatchingJourneyDestinations(String name, boolean normalize) {
    	String cql = TextUtils.merge("SELECT * FROM " + JOURNEY_BY_DESTINATION_VIEW + " WHERE destLocation = $${0}$$;", name );
        List<JourneyEntity> entitities = runQueryAndMapResults(cql);
        return entitiesToModels(entitities);
	}
    
    public void removeJourneyById(int journeyId) {
        Journey journey = getModel(journeyId);
        if( journey != null) {
            deleteModel(journey);
        }
    }
}
