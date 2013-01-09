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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.RefSpec;
import org.gitective.core.RepositoryUtils;
import org.springframework.util.Assert;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Closeables;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.User;

class ProjectRepositoryManager {
	private static final String CENTRAL_REPO_NAME = "_central"; //$NON-NLS-1$

	private String projectName;
	private File reposDir;
	private LockManager lockManager;
	private EventBus eventBus;
	private File centralRepoDir;

	ProjectRepositoryManager(String projectName, File reposDir, LockManager lockManager, EventBus eventBus) {
		this.projectName = projectName;
		this.reposDir = reposDir;
		this.lockManager = lockManager;
		this.eventBus = eventBus;
		centralRepoDir = new File(reposDir, CENTRAL_REPO_NAME);
	}

	ILockedRepository createCentralRepository(User user) throws IOException, GitAPIException {
		return createCentralRepository(true, user);
	}

	ILockedRepository createCentralRepository(boolean bare, User user) throws IOException, GitAPIException {
		if (centralRepoDir.isDirectory()) {
			throw new IllegalStateException("repository already exists: " + centralRepoDir.getAbsolutePath()); //$NON-NLS-1$
		}

		ILock lock = lockManager.lockAll();
		try {
			Repository repo = null;
			File gitDir = new File(centralRepoDir, ".git"); //$NON-NLS-1$
			try {
				RepositoryBuilder builder = new RepositoryBuilder().setGitDir(gitDir);
				if (bare) {
					builder.setBare();
				}
				repo = builder.build();
				repo.create();
			} finally {
				RepositoryUtil.closeQuietly(repo);
			}

			File tempGitDir = new File(new File(reposDir, CENTRAL_REPO_NAME + "_temp"), ".git"); //$NON-NLS-1$ //$NON-NLS-2$
			Repository tempRepo = null;
			try {
				tempRepo = Git.cloneRepository()
					.setURI(gitDir.toURI().toString())
					.setDirectory(tempGitDir)
					.call()
					.getRepository();
				Git git = Git.wrap(tempRepo);
				PersonIdent ident = new PersonIdent(user.getLoginName(), user.getEmail());
				git.commit()
					.setAuthor(ident)
					.setCommitter(ident)
					.setMessage("init").call(); //$NON-NLS-1$
				git.push().call();
			} finally {
				RepositoryUtil.closeQuietly(tempRepo);
			}
			FileUtils.forceDelete(tempGitDir.getParentFile());
		} finally {
			lockManager.unlock(lock);
		}

		return getCentralRepository(bare);
	}

	ILockedRepository getCentralRepository() throws IOException {
		return getCentralRepository(true);
	}

	ILockedRepository getCentralRepository(boolean bare) throws IOException {
		if (!centralRepoDir.isDirectory()) {
			throw RepositoryNotFoundException.forCentralRepository(projectName);
		}

		LockedRepository lockedRepo = LockedRepository.lockProjectCentral(projectName, lockManager);
		Repository repo = getCentralRepositoryInternal(bare);
		lockedRepo.setRepository(repo);
		return lockedRepo;
	}

	private Repository getCentralRepositoryInternal(boolean bare) throws IOException {
		RepositoryBuilder builder = new RepositoryBuilder().findGitDir(centralRepoDir);
		if (bare) {
			builder.setBare();
		}
		return builder.build();
	}

	ILockedRepository createBranchRepository(String branchName, String startingBranch) throws IOException, GitAPIException {
		Assert.hasLength(branchName);
		if (startingBranch != null) {
			Assert.hasLength(startingBranch);
		}

		File repoDir = new File(reposDir, branchName);
		if (repoDir.isDirectory()) {
			throw new IllegalStateException("repository already exists: " + repoDir.getAbsolutePath()); //$NON-NLS-1$
		}

		List<String> branches = listBranches();
		if (branches.contains(branchName)) {
			throw new IllegalArgumentException("branch already exists: " + branchName); //$NON-NLS-1$
		}

		if ((startingBranch == null) && !branches.isEmpty()) {
			throw new IllegalArgumentException("must specify a starting branch"); //$NON-NLS-1$
		}

		ILock lock = lockManager.lockAll();
		try {
			Repository centralRepo = null;
			File centralRepoGitDir;
			try {
				centralRepo = getCentralRepositoryInternal(true);
				centralRepoGitDir = centralRepo.getDirectory();
			} finally {
				RepositoryUtil.closeQuietly(centralRepo);
				centralRepo = null;
			}

			Repository repo = null;
			try {
				repo = Git.cloneRepository()
						.setURI(centralRepoGitDir.toURI().toString())
						.setDirectory(repoDir)
						.call()
						.getRepository();

				try {
					centralRepo = getCentralRepositoryInternal(true);
					if (!RepositoryUtils.getBranches(centralRepo).contains(branchName)) {
						CreateBranchCommand createBranchCommand = Git.wrap(centralRepo).branchCreate();
						if (startingBranch != null) {
							createBranchCommand.setStartPoint(startingBranch);
						}
						createBranchCommand.setName(branchName).call();
					}
				} finally {
					RepositoryUtil.closeQuietly(centralRepo);
				}

				Git git = Git.wrap(repo);
				RefSpec refSpec = new RefSpec("refs/heads/" + branchName + ":refs/remotes/origin/" + branchName); //$NON-NLS-1$ //$NON-NLS-2$
				git.fetch().setRemote("origin").setRefSpecs(refSpec).call();  //$NON-NLS-1$
				git.branchCreate().setName(branchName).setStartPoint("origin/" + branchName).call(); //$NON-NLS-1$
				git.checkout().setName(branchName).call();
			} finally {
				RepositoryUtil.closeQuietly(repo);
			}
		} finally {
			lockManager.unlock(lock);
		}

		return getBranchRepository(branchName);
	}

	ILockedRepository getBranchRepository(String branchName) throws IOException, GitAPIException {
		Assert.hasLength(branchName);

		File repoDir = new File(reposDir, branchName);
		if (!repoDir.isDirectory()) {
			throw new RepositoryNotFoundException(projectName, branchName);
		}

		LockedRepository lockedRepo = LockedRepository.lockProjectBranch(projectName, branchName, lockManager);
		Repository repo = new RepositoryBuilder().findGitDir(repoDir).build();
		Git.wrap(repo).pull().call();
		lockedRepo.setRepository(repo);
		return lockedRepo;
	}

	public List<String> listBranches() throws IOException {
		ILockedRepository repo = null;
		try {
			repo = getCentralRepository();
			List<String> result = Lists.newArrayList(RepositoryUtils.getBranches(repo.r()));
			final int prefixLen = "refs/heads/".length(); //$NON-NLS-1$
			Function<String, String> function = new Function<String, String>() {
				@Override
				public String apply(String branch) {
					return branch.substring(prefixLen);
				}
			};
			result = Lists.newArrayList(Lists.transform(result, function));
			result.remove(Constants.MASTER);
			Collections.sort(result);
			return result;
		} finally {
			Closeables.closeQuietly(repo);
		}
	}

	public void importSampleContents() throws IOException, GitAPIException {
		// TODO: synchronization is not quite correct here, but should be okay in this edge case
		if (listBranches().isEmpty()) {
			ILock lock = lockManager.lockAll();
			List<String> branches;
			try {
				File gitDir = new File(centralRepoDir, ".git"); //$NON-NLS-1$
				FileUtils.forceDelete(gitDir);

				Git.cloneRepository()
					.setURI(DocumentrConstants.SAMPLE_REPO_URL)
					.setDirectory(gitDir)
					.setBare(true)
					.call();

				Repository centralRepo = null;
				File centralRepoGitDir;
				try {
					centralRepo = getCentralRepositoryInternal(true);
					centralRepoGitDir = centralRepo.getDirectory();
					StoredConfig config = centralRepo.getConfig();
					config.unsetSection("remote", "origin"); //$NON-NLS-1$ //$NON-NLS-2$
					config.unsetSection("branch", "master"); //$NON-NLS-1$ //$NON-NLS-2$
					config.save();
				} finally {
					RepositoryUtil.closeQuietly(centralRepo);
				}

				branches = listBranches();
				for (String branchName : branches) {
					File repoDir = new File(reposDir, branchName);
					Repository repo = null;
					try {
						repo = Git.cloneRepository()
								.setURI(centralRepoGitDir.toURI().toString())
								.setDirectory(repoDir)
								.call()
								.getRepository();

						Git git = Git.wrap(repo);
						RefSpec refSpec = new RefSpec("refs/heads/" + branchName + ":refs/remotes/origin/" + branchName); //$NON-NLS-1$ //$NON-NLS-2$
						git.fetch().setRemote("origin").setRefSpecs(refSpec).call();  //$NON-NLS-1$
						git.branchCreate().setName(branchName).setStartPoint("origin/" + branchName).call(); //$NON-NLS-1$
						git.checkout().setName(branchName).call();
					} finally {
						RepositoryUtil.closeQuietly(repo);
					}
				}
			} finally {
				lockManager.unlock(lock);
			}

			for (String branch : branches) {
				eventBus.post(new BranchCreatedEvent(projectName, branch));
			}
		}
	}
}
