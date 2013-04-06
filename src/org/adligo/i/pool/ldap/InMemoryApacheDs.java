package org.adligo.i.pool.ldap;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.adligo.tests.ATest;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.registries.Schema;
import org.apache.directory.api.ldap.schemamanager.impl.DefaultSchemaManager;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.api.CacheService;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.InstanceLayout;
import org.apache.directory.server.core.api.schema.SchemaPartition;
import org.apache.directory.server.core.partition.impl.avl.AvlPartition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * using log4j which seemed to be used by apacheds in production
 * 
 * @author scott
 *
 * Note ATest extension just does the i_log/jse_util init for me
 */
public class InMemoryApacheDs extends ATest {
		private static final Logger log = Logger.getLogger(InMemoryApacheDs.class);
	  private static DirectoryService directoryService;
	  private static LdapServer ldapServer;
	  private static int TEST_COUNT = 0;
	  
	  @BeforeClass
	  public static void startApacheDs() throws Exception {
	    String buildDirectory = System.getProperty("buildDirectory");
	    File workingDirectory = new File(buildDirectory, "apacheds-work");
	    workingDirectory.mkdir();

	    
	    directoryService = new DefaultDirectoryService();

	    File running = new File(".");
	    String path = running.getAbsolutePath();
	    path = path.substring(0, path.length() - 1) + "eldap_dir";
	    InstanceLayout il = new InstanceLayout(path);
	    directoryService.setInstanceLayout(il);

	    DefaultSchemaManager schemaManager = new DefaultSchemaManager();
	    directoryService.setSchemaManager(schemaManager);
	    
	    List<Schema> schemas = schemaManager.getEnabled();
	    log.info("got " + schemas.size() + " schemas");
	    for (Schema schema: schemas) {
	    	log.debug("got schema " + schema);
	    }


	    schemaManager.loadAllEnabled();

	    //schemaPartition.setSchemaManager(schemaManager);

	    List<Throwable> errors = schemaManager.getErrors();

	    if (!errors.isEmpty())
	      throw new Exception("Schema load failed : " + errors);

	    AvlPartition systemPartition = new AvlPartition(schemaManager);
	    systemPartition.setId("system");
	    systemPartition.setSuffixDn(new Dn(ServerDNConstants.SYSTEM_DN));
	    directoryService.setSystemPartition(systemPartition);

	    directoryService.setShutdownHookEnabled(false);
	    directoryService.getChangeLog().setEnabled(false);

	    ldapServer = new LdapServer();
	    ldapServer.setTransports(new TcpTransport(11389));
	    ldapServer.setDirectoryService(directoryService);

	    
	    String schemaPartitionPath = path + "/schema";
	    File schemaPartDir = new File(schemaPartitionPath);
	    if (!schemaPartDir.exists()) {
	    	schemaPartDir.mkdir();
	
	    }
	   
	    AvlPartition scheamPartition = new AvlPartition(schemaManager);
	    scheamPartition.setId("schema");
	    scheamPartition.setSchemaManager(schemaManager);
	    scheamPartition.setPartitionPath(new URI("schema"));
	   
	    SchemaPartition sp = new SchemaPartition(schemaManager);
	    sp.setWrappedPartition(scheamPartition);
	    directoryService.setSchemaPartition(sp);
	   
	    CacheService cs = new CacheService();
	    cs.initialize(il);
	    directoryService.setCacheService(cs);
	    directoryService.startup();
	    ldapServer.start();
	  }

	  @AfterClass
	  public static void stopApacheDs() throws Exception {
	    ldapServer.stop();
	    directoryService.shutdown();
	  }

}
