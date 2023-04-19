package com.dynatrace.easytravel.cassandra.tables;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import com.dynatrace.easytravel.cassandra.base.EtKeySpace;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;

@Ignore
public class EtClusterTest extends SimpleTestUtil {
    CassandraConnection casConnection;

    @Before
    public void setup() throws IOException, CorruptInstallationException, InterruptedException {
        casConnection = new CassandraConnection();
        casConnection.connect("127.0.0.1");
    }

    @After
    public void tearDown() {
        casConnection.close();
    }

    //TODO add test for cluster
    @Test
    public void testCreate() {
        Session session = casConnection.getSession();
        assertNotNull(session);

        EtKeySpace keySpace = new EtKeySpace(CassandraTableTestUtil.TEST_KEYSPACE_NAME, casConnection.getCluster());
        keySpace.create();

        Metadata metadata = session.getCluster().getMetadata();
        System.out.println("cluster hosts: " + metadata.getAllHosts());
        System.out.println("keyspaces: " + metadata.getKeyspaces());

        assertNotNull(CassandraTableTestUtil.TEST_KEYSPACE_NAME + " keyspace not found",
                metadata.getKeyspace(CassandraTableTestUtil.TEST_KEYSPACE_NAME));

        keySpace.delete();
        assertNull(CassandraTableTestUtil.TEST_KEYSPACE_NAME + " easyTravelBusiness keyspace found",
                metadata.getKeyspace(CassandraTableTestUtil.TEST_KEYSPACE_NAME));
    }
}
