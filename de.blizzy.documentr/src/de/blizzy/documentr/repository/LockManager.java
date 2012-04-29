package de.blizzy.documentr.repository;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class LockManager {
	private static final class LockKey implements ILock {
		private String projectName;
		private String branchName;
		private boolean central;

		private LockKey(String projectName, String branchName, boolean central) {
			this.projectName = projectName;
			this.branchName = branchName;
			this.central = central;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			} else if ((o != null) && o.getClass().equals(getClass())) {
				LockKey other = (LockKey) o;
				return new EqualsBuilder()
					.append(projectName, other.projectName)
					.append(branchName, other.branchName)
					.append(central, other.central)
					.isEquals();
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return new HashCodeBuilder()
				.append(projectName)
				.append(branchName)
				.append(central)
				.toHashCode();
		}
	}
	
	private Set<LockKey> locks = new HashSet<>();

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
			
			locks.notify();
		}
	}
}
