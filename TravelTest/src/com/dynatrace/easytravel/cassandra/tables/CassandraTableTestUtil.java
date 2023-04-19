package com.dynatrace.easytravel.cassandra.tables;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;
import com.dynatrace.easytravel.cassandra.base.CassandraModelTable;
import com.dynatrace.easytravel.cassandra.base.CassandraObject;
import com.dynatrace.easytravel.cassandra.base.EtCluster;
import com.dynatrace.easytravel.cassandra.base.EtKeySpace;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.integration.persistence.cassandra.CassandraProcedureRunner;
import com.dynatrace.easytravel.launcher.procedures.CassandraProcedure;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.junit.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;

public class CassandraTableTestUtil {
	
	private static final Logger LOGGER = LoggerFactory.make();

    public static final String TEST_KEYSPACE_NAME = "easyTravelBusinessTest";
    
    public static final String HOME_DIRECTORY = System.getProperty(BaseConstants.SystemProperties.USER_HOME);
	public static final String DIRECTORY1 = ".dynaTrace\\easyTravel 1.0.0\\easyTravelTest\\database";
	public static final String DIRECTORY2 = ".dynaTrace\\easyTravel 2.0.0\\easyTravelTest\\database";
	public static final String PREFIX = "node";

    protected static CassandraConnection casConnection;
    private static EtKeySpace keySpace;
    private CassandraObject[] cassandraObjects = {};
    private static Iterable<CassandraProcedure> cassandraNodes;

    protected void setCassandraObjects(CassandraObject... objects) {
        this.cassandraObjects = objects;
    }

    @Before
    public void createObjects() {
        Arrays.stream(cassandraObjects).forEach( CassandraObject::reset );
        Arrays.stream(cassandraObjects)
                .filter(CassandraModelTable.class::isInstance)
                .map(CassandraModelTable.class::cast)
                .forEach(CassandraModelTable::init);
    }

    @After
    public void dropTables() {
        Arrays.stream(cassandraObjects)
                .forEach( CassandraObject::delete );
    }

    @BeforeClass
    public static void setupClass() throws Exception {
    	deleteDirectoryWithPrefix(HOME_DIRECTORY, DIRECTORY1, PREFIX);
    	deleteDirectoryWithPrefix(HOME_DIRECTORY, DIRECTORY2, PREFIX);
    	cassandraNodes = CassandraProcedureRunner.startCassandraNode(1);
        casConnection = new CassandraConnection();
        casConnection.connect("127.0.0.1");
        keySpace = new EtKeySpace(TEST_KEYSPACE_NAME, casConnection.getCluster());
        keySpace.delete();
        Assert.assertFalse(keySpace.isPresent());
        keySpace.create();
        keySpace.useKeyspace();
    }

    @AfterClass
    public static void tearDownClass() {
    	keySpace.delete();
        casConnection.close();
        for (CassandraProcedure node : cassandraNodes) {
			node.stop();
		}
    }

    protected static Session getSession() {
        return casConnection.getSession();
    }

    protected static EtCluster getCluster() {
        return casConnection.getCluster();
    }

    public boolean isTableExists(String table) {
        TableMetadata tableMetadata = casConnection.getSession().getCluster().getMetadata().getKeyspace(TEST_KEYSPACE_NAME).getTable(table);
        return tableMetadata != null;
    }

    public void reset(CassandraObject casObject) {
        casObject.reset();
    }
    
    public static void deleteDirectoryWithPrefix(String homeDirectory, String childDirectory, String directoryPrefix) throws IOException {
    	String path = childDirectory;
    	if(isLinux()) {
    		path = childDirectory.replace("\\", "/");
    	}
    	File parentDirectory = new File(homeDirectory, path);
    	LOGGER.trace("Directory: "+parentDirectory.getAbsolutePath());
		if(parentDirectory.exists()) {
			File[] subdirs = parentDirectory.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
			for (File dir : subdirs) {
				if(dir.getName().startsWith(directoryPrefix)) {
					LOGGER.info(TextUtils.merge("Removing folder: {0}, from directory: {1}", dir.getName(), parentDirectory.getPath()));
					FileUtils.deleteDirectory(dir);
				}
			}
		}
	}
    
	private static boolean isLinux() throws IOException {
		String osName = System.getProperty(BaseConstants.SystemProperties.OS_NAME);
		if (osName == null) {
			throw new IOException("os.name not found");
		}
		osName = osName.toLowerCase();

		if (osName.contains("linux") || osName.contains("mpe/ix") || osName.contains("freebsd")
				|| osName.contains("irix") || osName.contains("digital unix") || osName.contains("unix")) {
			return true;
		}
		return false;
	}
}
