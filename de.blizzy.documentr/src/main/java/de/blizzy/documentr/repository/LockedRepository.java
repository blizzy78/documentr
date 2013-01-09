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

import javax.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.Setter;

import org.eclipse.jgit.lib.Repository;

final class LockedRepository implements ILockedRepository {
	private ILock lock;
	private LockManager lockManager;
	@Setter(AccessLevel.PACKAGE)
	@NotNull
	private Repository repository;

	private LockedRepository(ILock lock, LockManager lockManager) {
		this.lock = lock;
		this.lockManager = lockManager;
	}

	static LockedRepository lockProjectCentral(String projectName, LockManager lockManager) {
		ILock lock = lockManager.lockProjectCentral(projectName);
		return new LockedRepository(lock, lockManager);
	}

	static LockedRepository lockProjectBranch(String projectName, String branchName, LockManager lockManager) {
		ILock lock = lockManager.lockProjectBranch(projectName, branchName);
		return new LockedRepository(lock, lockManager);
	}

	@Override
	public Repository r() {
		if (repository == null) {
			throw new IllegalStateException("no repository"); //$NON-NLS-1$
		}
		return repository;
	}

	@Override
	public void close() {
		try {
			RepositoryUtil.closeQuietly(repository);
		} finally {
			lockManager.unlock(lock);
		}
	}
}
