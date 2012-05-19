/*
documentr - Edit, maintain, and present software documentation on the web.
Copyright (C) 2012 Maik Schreiber

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

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class LockManager {
	private Set<LockKey> locks = new HashSet<LockKey>();

	ILock lockAll() {
		return lock(new LockKey(null, null, false), true);
	}
	
	ILock lockProjectCentral(String projectName) {
		Assert.notNull(projectName);
		
		return lock(new LockKey(projectName, null, true), false);
	}

	ILock lockProjectBranch(String projectName, String branchName) {
		Assert.notNull(projectName);
		Assert.notNull(branchName);
		
		return lock(new LockKey(projectName, branchName, false), false);
	}

	private ILock lock(LockKey key, boolean all) {
		synchronized (locks) {
			if (all) {
				while (!locks.isEmpty()) {
					try {
						locks.wait();
					} catch (InterruptedException e) {
						// ignore
					}
				}
			} else {
				while (locks.contains(key)) {
					try {
						locks.wait();
					} catch (InterruptedException e) {
						// ignore
					}
				}
			}
			
			locks.add(key);
		}
		return key;
	}
	
	void unlock(ILock lock) {
		Assert.notNull(lock);
		Assert.isInstanceOf(LockKey.class, lock);
		
		synchronized (locks) {
			if (!locks.remove(lock)) {
				throw new IllegalStateException("unknown lock"); //$NON-NLS-1$
			}
			
			locks.notifyAll();
		}
	}
}
