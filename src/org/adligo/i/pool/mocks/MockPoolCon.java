package org.adligo.i.pool.mocks;

import org.adligo.i.pool.I_PooledConnection;

public class MockPoolCon implements I_PooledConnection {
	private boolean returnToPoolCalled = false;
	private boolean isReadWriteCalled = false;
	
	private boolean isOkCalled = false;
	private boolean disposeCalled = false;
	
	public boolean isReturnToPoolCalled() {
		return returnToPoolCalled;
	}

	public boolean isReadWriteCalled() {
		return isReadWriteCalled;
	}

	public boolean isOkCalled() {
		return isOkCalled;
	}

	public boolean isDisposeCalled() {
		return disposeCalled;
	}

	
	public void reset() {
		returnToPoolCalled = false;
		isReadWriteCalled = false;
		isOkCalled = false;
		disposeCalled = false;
	}
	
	@Override
	public void returnToPool() {
		returnToPoolCalled = true;
	}

	@Override
	public boolean isReadWrite() {
		isReadWriteCalled = true;
		return false;
	}

	@Override
	public boolean isOK() {
		isOkCalled = true;
		return false;
	}

	@Override
	public void dispose() {
		disposeCalled = true;	
	}

}
