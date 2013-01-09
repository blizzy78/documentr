/*
documentr - Edit, maintain, and present software documentation on the web.
Copyright (C) 2012-2013 Maik Schreiber

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package de.blizzy.documentr.repository;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class LockManagerTest {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private LockManager lockManager;

	@Before
	public void setUp() {
		lockManager = new LockManager();
	}

	@Test
	public void lockAndUnlock() {
		ILock lock = lockManager.lockProjectBranch("project", "branch"); //$NON-NLS-1$ //$NON-NLS-2$
		assertNotNull(lock);
		lockManager.unlock(lock);
	}

	@Test
	public void lockAndUnlockReentrant() {
		ILock lock = lockManager.lockProjectBranch("project", "branch"); //$NON-NLS-1$ //$NON-NLS-2$
		assertNotNull(lock);
		ILock lock2 = lockManager.lockProjectBranch("project", "branch"); //$NON-NLS-1$ //$NON-NLS-2$
		assertNotNull(lock2);
		lockManager.unlock(lock2);
		lockManager.unlock(lock);
	}

	@Test
	public void lockWhileHoldingAllLock() {
		lockManager.lockAll();
		lockManager.lockProjectBranch("project", "branch"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void lockAllWhileHoldingLock() {
		lockManager.lockProjectBranch("project", "branch"); //$NON-NLS-1$ //$NON-NLS-2$
		lockManager.lockAll();
	}

	@Test
	public void unlockWithUnknownLock() {
		Lock lock = new Lock(Thread.currentThread());

		expectedException.expect(IllegalStateException.class);
		lockManager.unlock(lock);
	}

	@Test
	public void unlockTooOften() {
		ILock lock = lockManager.lockProjectBranch("project", "branch"); //$NON-NLS-1$ //$NON-NLS-2$
		lockManager.unlock(lock);

		expectedException.expect(IllegalStateException.class);
		lockManager.unlock(lock);
	}
}
