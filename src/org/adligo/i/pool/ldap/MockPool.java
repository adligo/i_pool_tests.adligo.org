package org.adligo.i.pool.ldap;

import org.adligo.i.pool.Pool;
import org.adligo.i.pool.PoolConfiguration;
import org.adligo.i.pool.ldap.models.LdapConnectionFactoryConfig;

public class MockPool {
	public static final Pool<ReadWriteLdapConnection> POOL = getPool();
	
	public static final Pool<ReadWriteLdapConnection> getPool() {
		 LdapConnectionFactoryConfig config = new LdapConnectionFactoryConfig();
	      config.setPort(InMemoryApacheDs.PORT);
	      config.setHost("localhost");
	      config.setDefaultChunkSize(10);
	      PoolConfiguration<ReadWriteLdapConnection> poolConfig = new PoolConfiguration<ReadWriteLdapConnection>();
	      poolConfig.setFactory(new ReadWriteLdapConnectionFactory(config));
	      poolConfig.setMax(2);
	      poolConfig.setName("ldapPool");
	      
	      Pool<ReadWriteLdapConnection> pool = new Pool<ReadWriteLdapConnection>(poolConfig);
	      return pool;
	}
}
