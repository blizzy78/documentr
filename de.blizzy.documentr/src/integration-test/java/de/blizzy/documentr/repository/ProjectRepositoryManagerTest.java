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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.gitective.core.CommitUtils;
import org.gitective.core.RepositoryUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

import com.google.common.eventbus.EventBus;
import com.google.common.io.Closeables;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.access.User;

public class ProjectRepositoryManagerTest extends AbstractDocumentrTest {
	private static final User USER = new User("currentUser", "pw", "admin@example.com", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Mock
	private LockManager lockManager;
	@Mock
	private EventBus eventBus;

	@Test
	public void createCentralRepository() throws IOException, GitAPIException {
		File reposDir = tempDir.getRoot();
		ProjectRepositoryManager repoManager = new ProjectRepositoryManager("project", reposDir, lockManager, eventBus); //$NON-NLS-1$
		ILockedRepository lockedRepo = null;
		try {
			lockedRepo = repoManager.createCentralRepository(USER);
		} finally {
			Closeables.closeQuietly(lockedRepo);
			lockedRepo = null;
		}

		Repository repo = null;
		try {
			repo = new RepositoryBuilder().findGitDir(new File(reposDir, "_central")).build(); //$NON-NLS-1$
			assertEquals(new File(new File(reposDir, "_central"), ".git"), repo.getDirectory()); //$NON-NLS-1$ //$NON-NLS-2$
			assertNotNull(CommitUtils.getCommit(repo, Constants.MASTER));
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}

	@Test
	public void createCentralRepositoryMustThrowIllegalStateExceptionIfExists() throws IOException, GitAPIException {
		File reposDir = tempDir.getRoot();
		ProjectRepositoryManager repoManager = new ProjectRepositoryManager("project", reposDir, lockManager, eventBus); //$NON-NLS-1$
		ILockedRepository lockedRepo = null;
		try {
			lockedRepo = repoManager.createCentralRepository(USER);
		} finally {
			Closeables.closeQuietly(lockedRepo);
			lockedRepo = null;
		}

		expectedException.expect(IllegalStateException.class);
		register(repoManager.createCentralRepository(USER));
	}

	@Test
	public void getCentralRepository() throws IOException, GitAPIException {
		File reposDir = tempDir.getRoot();
		ProjectRepositoryManager repoManager = new ProjectRepositoryManager("project", reposDir, lockManager, eventBus); //$NON-NLS-1$
		ILockedRepository repo = null;
		File gitDir;
		try {
			repo = repoManager.createCentralRepository(USER);
			gitDir = repo.r().getDirectory();
		} finally {
			Closeables.closeQuietly(repo);
			repo = null;
		}

		try {
			repo = repoManager.getCentralRepository();
			assertEquals(gitDir, repo.r().getDirectory());
			assertTrue(repo.r().isBare());
		} finally {
			Closeables.closeQuietly(repo);
		}
	}

	@Test
	public void getCentralRepositoryMustThrowRepositoryNotFoundExceptionIfNonexistent() throws IOException {
		File reposDir = tempDir.getRoot();
		ProjectRepositoryManager repoManager = new ProjectRepositoryManager("project", reposDir, lockManager, eventBus); //$NON-NLS-1$

		expectedException.expect(RepositoryNotFoundException.class);
		register(repoManager.getCentralRepository());
	}

	@Test
	public void createBranchRepositoryWithoutStartingBranch() throws IOException, GitAPIException {
		File reposDir = tempDir.getRoot();
		ProjectRepositoryManager repoManager = new ProjectRepositoryManager("project", reposDir, lockManager, eventBus); //$NON-NLS-1$
		ILockedRepository repo = null;
		try {
			repo = repoManager.createCentralRepository(USER);
		} finally {
			Closeables.closeQuietly(repo);
			repo = null;
		}

		try {
			repo = repoManager.createBranchRepository("branch", null); //$NON-NLS-1$
			assertEquals(new File(new File(reposDir, "branch"), ".git"), repo.r().getDirectory()); //$NON-NLS-1$ //$NON-NLS-2$
			assertTrue(RepositoryUtils.getBranches(repo.r()).contains("refs/heads/branch")); //$NON-NLS-1$
		} finally {
			Closeables.closeQuietly(repo);
			repo = null;
		}

		try {
			repo = repoManager.getCentralRepository();
			assertTrue(RepositoryUtils.getBranches(repo.r()).contains("refs/heads/branch")); //$NON-NLS-1$
		} finally {
			Closeables.closeQuietly(repo);
		}
	}

	@Test
	public void createBranchRepositoryMustThrowIllegalStateExceptionIfExists() throws IOException, GitAPIException {
		File reposDir = tempDir.getRoot();
		ProjectRepositoryManager repoManager = new ProjectRepositoryManager("project", reposDir, lockManager, eventBus); //$NON-NLS-1$
		ILockedRepository repo = null;
		try {
			repo = repoManager.createCentralRepository(USER);
		} finally {
			Closeables.closeQuietly(repo);
			repo = null;
		}

		try {
			repo = repoManager.createBranchRepository("branch", null); //$NON-NLS-1$
		} finally {
			Closeables.closeQuietly(repo);
			repo = null;
		}

		expectedException.expect(IllegalStateException.class);
		register(repoManager.createBranchRepository("branch", null)); //$NON-NLS-1$
	}

	@Test
	public void createBranchRepositoryWithoutStartingBranchAndExistingBranch() throws IOException, GitAPIException {
		File reposDir = tempDir.getRoot();
		ProjectRepositoryManager repoManager = new ProjectRepositoryManager("project", reposDir, lockManager, eventBus); //$NON-NLS-1$
		ILockedRepository repo = null;
		try {
			repo = repoManager.createCentralRepository(USER);
		} finally {
			Closeables.closeQuietly(repo);
			repo = null;
		}

		try {
			repo = repoManager.createBranchRepository("branch1", null); //$NON-NLS-1$
		} finally {
			Closeables.closeQuietly(repo);
			repo = null;
		}

		try {
			expectedException.expect(IllegalArgumentException.class);
			repo = repoManager.createBranchRepository("branch2", null); //$NON-NLS-1$
		} finally {
			Closeables.closeQuietly(repo);
			repo = null;
		}
	}

	@Test
	public void createBranchRepository() throws IOException, GitAPIException {
		File reposDir = tempDir.getRoot();
		ProjectRepositoryManager repoManager = new ProjectRepositoryManager("project", reposDir, lockManager, eventBus); //$NON-NLS-1$
		ILockedRepository repo = null;
		try {
			repo = repoManager.createCentralRepository(USER);
			Git.wrap(repo.r()).branchCreate().setName("startingBranch").call(); //$NON-NLS-1$
		} finally {
			Closeables.closeQuietly(repo);
			repo = null;
		}

		try {
			repo = repoManager.createBranchRepository("branch", "startingBranch"); //$NON-NLS-1$ //$NON-NLS-2$
			assertEquals(new File(new File(reposDir, "branch"), ".git"), repo.r().getDirectory()); //$NON-NLS-1$ //$NON-NLS-2$
			assertTrue(RepositoryUtils.getBranches(repo.r()).contains("refs/heads/branch")); //$NON-NLS-1$
		} finally {
			Closeables.closeQuietly(repo);
			repo = null;
		}

		try {
			repo = repoManager.getCentralRepository();
			assertTrue(RepositoryUtils.getBranches(repo.r()).contains("refs/heads/branch")); //$NON-NLS-1$
		} finally {
			Closeables.closeQuietly(repo);
		}
	}

	@Test
	public void getBranchRepository() throws IOException, GitAPIException {
		File reposDir = tempDir.getRoot();
		ProjectRepositoryManager repoManager = new ProjectRepositoryManager("project", reposDir, lockManager, eventBus); //$NON-NLS-1$
		ILockedRepository repo = null;
		try {
			repo = repoManager.createCentralRepository(USER);
		} finally {
			Closeables.closeQuietly(repo);
			repo = null;
		}

		try {
			repo = repoManager.createBranchRepository("branch", null); //$NON-NLS-1$
		} finally {
			Closeables.closeQuietly(repo);
			repo = null;
		}

		try {
			repo = repoManager.getBranchRepository("branch"); //$NON-NLS-1$
			assertTrue(RepositoryUtils.getBranches(repo.r()).contains("refs/heads/branch")); //$NON-NLS-1$
		} finally {
			Closeables.closeQuietly(repo);
		}
	}

	@Test
	public void getBranchRepositoryMustThrowRepositoryNotFoundExceptionIfNonexistent() throws IOException, GitAPIException {
		File reposDir = tempDir.getRoot();
		ProjectRepositoryManager repoManager = new ProjectRepositoryManager("project", reposDir, lockManager, eventBus); //$NON-NLS-1$

		expectedException.expect(RepositoryNotFoundException.class);
		register(repoManager.getBranchRepository("branch")); //$NON-NLS-1$
	}

	@Test
	public void listBranches() throws IOException, GitAPIException {
		File reposDir = tempDir.getRoot();
		ProjectRepositoryManager repoManager = new ProjectRepositoryManager("project", reposDir, lockManager, eventBus); //$NON-NLS-1$
		ILockedRepository repo = null;
		try {
			repo = repoManager.createCentralRepository(USER);
			Git.wrap(repo.r()).branchCreate().setName("branch1").call(); //$NON-NLS-1$
			Git.wrap(repo.r()).branchCreate().setName("branch2").call(); //$NON-NLS-1$
		} finally {
			Closeables.closeQuietly(repo);
			repo = null;
		}

		List<String> branches = repoManager.listBranches();
		assertEquals(2, branches.size());
		assertTrue(branches.contains("branch1")); //$NON-NLS-1$
		assertTrue(branches.contains("branch2")); //$NON-NLS-1$
	}
}
