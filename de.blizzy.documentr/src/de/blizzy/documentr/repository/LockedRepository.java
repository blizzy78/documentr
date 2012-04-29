package de.blizzy.documentr.repository;

import org.eclipse.jgit.lib.Repository;
import org.springframework.util.Assert;

class LockedRepository implements ILockedRepository {
	private ILock lock;
	private LockManager lockManager;
	private Repository repo;

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

	void setRepository(Repository repo) {
		Assert.notNull(repo);
		this.repo = repo;
	}
	
	@Override
	public Repository r() {
		if (repo == null) {
			throw new IllegalStateException("no repository"); //$NON-NLS-1$
		}
		return repo;
	}
	
	@Override
	public void close() {
		try {
			RepositoryUtil.closeQuietly(repo);
		} finally {
			lockManager.unlock(lock);
		}
	}
}
