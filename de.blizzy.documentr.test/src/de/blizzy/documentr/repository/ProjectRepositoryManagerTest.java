package de.blizzy.documentr.repository;

import static de.blizzy.documentr.TestUtil.*;
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
import org.junit.Test;

public class ProjectRepositoryManagerTest {
	@Test
	public void createCentralRepository() throws IOException, GitAPIException {
		File reposDir = createTempDir();
		ProjectRepositoryManager repoManager = new ProjectRepositoryManager("project", reposDir); //$NON-NLS-1$
		Repository repo = null;
		try {
			repo = repoManager.createCentralRepository();
		} finally {
			RepositoryUtil.closeQuietly(repo);
			repo = null;
		}

		try {
			repo = new RepositoryBuilder().findGitDir(new File(reposDir, "_central")).build(); //$NON-NLS-1$
			assertEquals(new File(new File(reposDir, "_central"), ".git"), repo.getDirectory()); //$NON-NLS-1$ //$NON-NLS-2$
			assertNotNull(CommitUtils.getCommit(repo, Constants.MASTER));
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}
	
	@Test
	public void getCentralRepository() throws IOException, GitAPIException {
		File reposDir = createTempDir();
		ProjectRepositoryManager repoManager = new ProjectRepositoryManager("project", reposDir); //$NON-NLS-1$
		Repository repo = null;
		File gitDir;
		try {
			repo = repoManager.createCentralRepository();
			gitDir = repo.getDirectory();
		} finally {
			RepositoryUtil.closeQuietly(repo);
			repo = null;
		}
		
		try {
			repo = repoManager.getCentralRepository();
			assertEquals(gitDir, repo.getDirectory());
			assertTrue(repo.isBare());
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}

	@Test
	public void createBranchRepositoryWithoutStartingBranch() throws IOException, GitAPIException {
		File reposDir = createTempDir();
		ProjectRepositoryManager repoManager = new ProjectRepositoryManager("project", reposDir); //$NON-NLS-1$
		Repository repo = null;
		try {
			repo = repoManager.createCentralRepository();
		} finally {
			RepositoryUtil.closeQuietly(repo);
			repo = null;
		}

		try {
			repo = repoManager.createBranchRepository("branch", null); //$NON-NLS-1$
			assertEquals(new File(new File(reposDir, "branch"), ".git"), repo.getDirectory()); //$NON-NLS-1$ //$NON-NLS-2$
			assertTrue(RepositoryUtils.getBranches(repo).contains("refs/heads/branch")); //$NON-NLS-1$
		} finally {
			RepositoryUtil.closeQuietly(repo);
			repo = null;
		}

		try {
			repo = repoManager.getCentralRepository();
			assertTrue(RepositoryUtils.getBranches(repo).contains("refs/heads/branch")); //$NON-NLS-1$
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void createBranchRepositoryWithoutStartingBranchAndExistingBranch() throws IOException, GitAPIException {
		File reposDir = createTempDir();
		ProjectRepositoryManager repoManager = new ProjectRepositoryManager("project", reposDir); //$NON-NLS-1$
		Repository repo = null;
		try {
			repo = repoManager.createCentralRepository();
		} finally {
			RepositoryUtil.closeQuietly(repo);
			repo = null;
		}
		
		try {
			repo = repoManager.createBranchRepository("branch1", null); //$NON-NLS-1$
		} finally {
			RepositoryUtil.closeQuietly(repo);
			repo = null;
		}

		try {
			repo = repoManager.createBranchRepository("branch2", null); //$NON-NLS-1$
		} finally {
			RepositoryUtil.closeQuietly(repo);
			repo = null;
		}
	}
	
	@Test
	public void createBranchRepository() throws IOException, GitAPIException {
		File reposDir = createTempDir();
		ProjectRepositoryManager repoManager = new ProjectRepositoryManager("project", reposDir); //$NON-NLS-1$
		Repository repo = null;
		try {
			repo = repoManager.createCentralRepository();
			Git.wrap(repo).branchCreate().setName("startingBranch").call(); //$NON-NLS-1$
		} finally {
			RepositoryUtil.closeQuietly(repo);
			repo = null;
		}
		
		try {
			repo = repoManager.createBranchRepository("branch", "startingBranch"); //$NON-NLS-1$ //$NON-NLS-2$
			assertEquals(new File(new File(reposDir, "branch"), ".git"), repo.getDirectory()); //$NON-NLS-1$ //$NON-NLS-2$
			assertTrue(RepositoryUtils.getBranches(repo).contains("refs/heads/branch")); //$NON-NLS-1$
		} finally {
			RepositoryUtil.closeQuietly(repo);
			repo = null;
		}

		try {
			repo = repoManager.getCentralRepository();
			assertTrue(RepositoryUtils.getBranches(repo).contains("refs/heads/branch")); //$NON-NLS-1$
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}

	@Test
	public void getBranchRepository() throws IOException, GitAPIException {
		File reposDir = createTempDir();
		ProjectRepositoryManager repoManager = new ProjectRepositoryManager("project", reposDir); //$NON-NLS-1$
		Repository repo = null;
		try {
			repo = repoManager.createCentralRepository();
		} finally {
			RepositoryUtil.closeQuietly(repo);
			repo = null;
		}
		
		try {
			repo = repoManager.createBranchRepository("branch", null); //$NON-NLS-1$
		} finally {
			RepositoryUtil.closeQuietly(repo);
			repo = null;
		}

		try {
			repo = repoManager.getBranchRepository("branch"); //$NON-NLS-1$
			assertTrue(RepositoryUtils.getBranches(repo).contains("refs/heads/branch")); //$NON-NLS-1$
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}

	@Test
	public void listBranches() throws IOException, GitAPIException {
		File reposDir = createTempDir();
		ProjectRepositoryManager repoManager = new ProjectRepositoryManager("project", reposDir); //$NON-NLS-1$
		Repository repo = null;
		try {
			repo = repoManager.createCentralRepository();
			Git.wrap(repo).branchCreate().setName("branch1").call(); //$NON-NLS-1$
			Git.wrap(repo).branchCreate().setName("branch2").call(); //$NON-NLS-1$
		} finally {
			RepositoryUtil.closeQuietly(repo);
			repo = null;
		}

		List<String> branches = repoManager.listBranches();
		assertEquals(2, branches.size());
		assertTrue(branches.contains("branch1")); //$NON-NLS-1$
		assertTrue(branches.contains("branch2")); //$NON-NLS-1$
	}
}
