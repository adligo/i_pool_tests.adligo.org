package org.adligo.i.pool.ldap;

import java.util.List;

import org.adligo.i.log.client.Log;
import org.adligo.i.log.client.LogFactory;
import org.adligo.i.pool.Pool;
import org.adligo.i.pool.PoolConfiguration;
import org.adligo.i.pool.ldap.models.CommonAttributes;
import org.adligo.i.pool.ldap.models.I_LdapEntry;
import org.adligo.i.pool.ldap.models.LdapConnectionFactoryConfig;
import org.adligo.i.pool.ldap.models.LdapEntryMutant;
import org.adligo.tests.ATest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LdapConnectionsTest extends ATest {
	private static final Log log = LogFactory.getLog(LdapConnectionsTest.class);
	private static int tests = 1;
	private static int which_test = 0;
	public LdapConnectionsTest() {}
	static {
		try {
			InMemoryApacheDs.startApacheDs();
		} catch (Exception x) {
			log.error(x.getMessage(), x);
		}
	}
	
	@Override
	public void tearDown() throws Exception {
		if (which_test == tests) {
			InMemoryApacheDs.stopApacheDs();
		} else {
			tests++;
		}
	}
	
	public void testGet() throws Exception {
			ReadWriteLdapConnection ldapCon = MockPool.POOL.getConnection();
	      
	      I_LdapEntry entry = ldapCon.get(InMemoryApacheDs.TEST_BASE_DN);

	      assertNotNull(InMemoryApacheDs.TEST_BASE_DN);
	      assertEquals(InMemoryApacheDs.TEST_BASE_DN, entry.getDistinguishedName());
	      assertEquals("test", entry.getAttribute(InMemoryApacheDs.DC));
	      List<String> attribs = entry.getStringAttributes("objectClass");
	      assertTrue(attribs.contains("top"));
	      assertTrue(attribs.contains("domain"));
	      
	      ldapCon.returnToPool();
	}
	
	public void testCreate() throws Exception {
	      ReadWriteLdapConnection ldapCon = MockPool.POOL.getConnection();
	      
	      
	      String dn = "dc=testCreate," + InMemoryApacheDs.TEST_BASE_DN;
	      
	      LdapEntryMutant lem = new LdapEntryMutant();
	      lem.setDistinguishedName(dn);
	      lem.setAttribute("dc", "testCreate");
	      lem.setAttribute(CommonAttributes.OBJECT_CLASS, "domain");
	      ldapCon.create(lem);
	      
	      I_LdapEntry entry = ldapCon.get(dn);

	      assertNotNull(InMemoryApacheDs.TEST_BASE_DN);
	      assertEquals(dn, entry.getDistinguishedName());
	      assertEquals("testCreate", entry.getAttribute(InMemoryApacheDs.DC));
	      List<String> attribs = entry.getStringAttributes("objectClass");
	      assertTrue(attribs.contains("top"));
	      assertTrue(attribs.contains("domain"));
	      
	      ldapCon.returnToPool();
	}
}
