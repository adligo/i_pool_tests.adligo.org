package org.adligo.i.pool;

import org.adligo.i.pool.mocks.MockPoolCon;
import org.adligo.tests.ATest;

public class PoolConfigurationTests extends ATest {

	public void testBean() throws Exception {
		PoolConfigurationMutant<MockPoolCon> config = new PoolConfigurationMutant<MockPoolCon>();
		assertEquals(0, config.getMin());
		assertEquals(1, config.getMax());
		assertNull(config.getName());
		
		config.setMax(10);
		config.setMin(4);
		assertEquals(4, config.getMin());
		assertEquals(10, config.getMax());
		config.setName("config name");
		assertEquals("config name", config.getName());
	}
}
