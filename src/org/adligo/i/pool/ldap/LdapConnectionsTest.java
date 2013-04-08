package org.adligo.i.pool.ldap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.adligo.i.log.client.Log;
import org.adligo.i.log.client.LogFactory;
import org.adligo.i.pool.ldap.models.CommonAttributes;
import org.adligo.i.pool.ldap.models.I_LdapEntry;
import org.adligo.i.pool.ldap.models.LargeFileCreationToken;
import org.adligo.i.pool.ldap.models.LdapEntryMutant;
import org.adligo.tests.ATest;
import org.apache.directory.api.ldap.model.schema.registries.Schema;
import org.apache.directory.api.ldap.schemaloader.JarLdifSchemaLoader;
import org.apache.directory.api.ldap.schemamanager.impl.DefaultSchemaManager;

import com.sun.jndi.ldap.pool.Pool;

public class LdapConnectionsTest extends ATest {
	private static final Log log = LogFactory.getLog(LdapConnectionsTest.class);
	private static int tests = 3;
	private static int which_test = 0;
	ReadWriteLdapConnection ldapCon;
    
	static {
		try {
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
			
	      I_LdapEntry entry = ldapCon.get(InMemoryApacheDs.TEST_BASE_DN);

	      assertNotNull(InMemoryApacheDs.TEST_BASE_DN);
	      assertEquals(InMemoryApacheDs.TEST_BASE_DN, entry.getDistinguishedName());
	      assertEquals("test", entry.getAttribute(CommonAttributes.DC));
	      List<String> attribs = entry.getStringAttributes(CommonAttributes.OBJECT_CLASS);
	      assertTrue(attribs.contains("top"));
	      assertTrue(attribs.contains("domain"));
	}
	
	public void testCreate() throws Exception {
	      String dn = "dc=testCreate," + InMemoryApacheDs.TEST_BASE_DN;
	      
	      LdapEntryMutant lem = new LdapEntryMutant();
	      lem.setDistinguishedName(dn);
	      lem.setAttribute(CommonAttributes.DC, "testCreate");
	      lem.setAttribute(CommonAttributes.OBJECT_CLASS, "domain");
	      ldapCon.create(lem);
	      
	      I_LdapEntry entry = ldapCon.get(dn);

	      assertNotNull(InMemoryApacheDs.TEST_BASE_DN);
	      assertEquals(dn, entry.getDistinguishedName());
	      assertEquals("testCreate", entry.getAttribute(CommonAttributes.DC));
	      List<String> attribs = entry.getStringAttributes(CommonAttributes.OBJECT_CLASS);
	      assertTrue(attribs.contains("top"));
	      assertTrue(attribs.contains("domain"));
	      
	}
	
	public void testCreateLargeFile() throws Exception {
	      InputStream in = getClass().getResourceAsStream("/org/adligo/i/pool/ldap/test.file");
	      
	      LargeFileCreationToken token = new LargeFileCreationToken();
	      token.setBaseDn(InMemoryApacheDs.TEST_BASE_DN);
	      token.setFileName("test.file");
	      token.setContentStream(in);
	      token.setSize(37);
	      token.setServerCheckedOn("inMemoryServer");
	     assertTrue(ldapCon.createLargeFile(token));

	      assertNotNull(InMemoryApacheDs.TEST_BASE_DN);
	      /*
	      assertEquals(dn, entry.getDistinguishedName());
	      assertEquals("testCreate", entry.getAttribute(InMemoryApacheDs.DC));
	      List<String> attribs = entry.getStringAttributes("objectClass");
	      assertTrue(attribs.contains("top"));
	      assertTrue(attribs.contains("domain"));
	      */
	}
}
