package org.adligo.i.pool.ldap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.adligo.i.adi.client.InvokerNames;
import org.adligo.i.adig.MockGClock;
import org.adligo.i.adig.client.GRegistry;
import org.adligo.i.log.client.Log;
import org.adligo.i.log.client.LogFactory;
import org.adligo.i.pool.ldap.models.DomainAttributes;
import org.adligo.i.pool.ldap.models.I_LdapEntry;
import org.adligo.i.pool.ldap.models.LargeFileAttributes;
import org.adligo.i.pool.ldap.models.LargeFileCreationToken;
import org.adligo.i.pool.ldap.models.LdapEntryMutant;
import org.adligo.i.pool.ldap.models.TopAttributes;
import org.adligo.tests.ATest;
import org.apache.directory.api.ldap.model.schema.registries.Schema;
import org.apache.directory.api.ldap.schemamanager.impl.DefaultSchemaManager;

import com.sun.jndi.ldap.pool.Pool;

public class LdapConnectionsTest extends ATest {
	private static final Log log = LogFactory.getLog(LdapConnectionsTest.class);
	private static int tests = 3;
	private static int which_test = 0;
	ReadWriteLdapConnection ldapCon;
    
	static {
		try {
			GRegistry.addOrReplaceInvoker(InvokerNames.CLOCK,  MockGClock.INSTANCE);
			List<Schema> schemaFiles = new ArrayList<Schema>();
			
			
			DefaultSchemaManager dsm = InMemoryApacheDs.createDefaultSchemaManager();
			OpenDsToApacheDsSchemaLoader loader = new OpenDsToApacheDsSchemaLoader();
			Schema schema = loader.loadFromClasspath("/ldap_schemas/adligo_large_file.ldif", Pool.class, dsm);
			schemaFiles.add(schema);
			InMemoryApacheDs.addSchemas(schemaFiles, dsm);
			InMemoryApacheDs.startApacheDs(dsm);
		} catch (Exception x) {
			log.error(x.getMessage(), x);
		}
	}
	
	public LdapConnectionsTest() {}
	
	
	@Override
	public void setUp() throws Exception {
		ldapCon = MockPool.POOL.getConnection();
	}
	@Override
	public void tearDown() throws Exception {
		ldapCon.returnToPool();
		if (which_test == tests) {
			InMemoryApacheDs.stopApacheDs();
		} else {
			tests++;
		}
	}
	
	public void testGet() throws Exception {
			
	      I_LdapEntry entry = ldapCon.get(InMemoryApacheDs.BASE_TEST_DN);

	      assertNotNull(InMemoryApacheDs.BASE_TEST_DN);
	      assertEquals(InMemoryApacheDs.BASE_TEST_DN, entry.getDistinguishedName());
	      assertEquals("test", entry.getAttribute(DomainAttributes.DOMAIN_COMPONENT));
	      List<String> attribs = entry.getStringAttributes(DomainAttributes.OBJECT_CLASS);
	      assertTrue(attribs.contains("top"));
	      assertTrue(attribs.contains("domain"));
	}
	
	public void testCreate() throws Exception {
	      String dn = "dc=testCreate," + InMemoryApacheDs.BASE_TEST_DN;
	      
	      LdapEntryMutant lem = new LdapEntryMutant();
	      lem.setDistinguishedName(dn);
	      lem.setAttribute(DomainAttributes.DOMAIN_COMPONENT, "testCreate");
	      lem.setAttribute(DomainAttributes.OBJECT_CLASS, "domain");
	      ldapCon.create(lem);
	      
	      I_LdapEntry entry = ldapCon.get(dn);

	      assertNotNull(InMemoryApacheDs.BASE_TEST_DN);
	      assertEquals(dn, entry.getDistinguishedName());
	      assertEquals("testCreate", entry.getAttribute(DomainAttributes.DOMAIN_COMPONENT));
	      List<String> attribs = entry.getStringAttributes(DomainAttributes.OBJECT_CLASS);
	      assertTrue(attribs.contains("top"));
	      assertTrue(attribs.contains("domain"));
	      
	}
	
	public void testCreateLargeFile() throws Exception {
	      InputStream in = getClass().getResourceAsStream("/org/adligo/i/pool/ldap/test.file");
	      
	      MockGClock.INSTANCE.setTime(111);
	      LargeFileCreationToken token = new LargeFileCreationToken();
	      token.setBaseDn(InMemoryApacheDs.BASE_TEST_DN);
	      token.setFileName("test.file");
	      token.setContentStream(in);
	      token.setSize(37);
	      token.setServerCheckedOn("inMemoryServer");
	     assertTrue(ldapCon.createLargeFile(token));

	      I_LdapEntry largeFile = ldapCon.get("fn=test.file," + InMemoryApacheDs.BASE_TEST_DN);
	      assertNotNull(largeFile);
	      
	      assertEquals( "fn=test.file," + InMemoryApacheDs.BASE_TEST_DN, largeFile.getDistinguishedName());
	      assertEquals("test.file", largeFile.getAttribute(LargeFileAttributes.FILE_NAME));
	      assertNull(largeFile.getLongAttribute(LargeFileAttributes.DELETING));
	      assertEquals(Boolean.FALSE, largeFile.getBooleanAttribute(LargeFileAttributes.WRITING));
	      assertEquals("inMemoryServer", largeFile.getStringAttribute(LargeFileAttributes.CHECKED_ON_SERVER));
	      assertEquals(new Long(37), largeFile.getLongAttribute(LargeFileAttributes.SIZE));
	      List<String> classes = largeFile.getStringAttributes(LargeFileAttributes.OBJECT_CLASS);
	      assertNotNull(classes);
	      assertTrue( classes.contains(TopAttributes.OBJECT_CLASS_NAME));
	      assertTrue( classes.contains(LargeFileAttributes.OBJECT_CLASS_NAME));

	}
}
