package de.blizzy.documentr.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.transport.RefSpec;
import org.gitective.core.RepositoryUtils;
import org.springframework.util.Assert;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

class ProjectRepositoryManager {
	private static final String CENTRAL_REPO_NAME = "_central"; //$NON-NLS-1$
	
	private String projectName;
	private File reposDir;
	private File centralRepoDir;

	ProjectRepositoryManager(String projectName, File reposDir) {
		this.projectName = projectName;
		this.reposDir = reposDir;
		centralRepoDir = new File(reposDir, CENTRAL_REPO_NAME);
	}
	
	Repository createCentralRepository() throws IOException, GitAPIException {
		if (centralRepoDir.isDirectory()) {
			throw new IllegalStateException("repository already exists: " + centralRepoDir.getAbsolutePath()); //$NON-NLS-1$
		}

		Repository repo = null;
		File gitDir = new File(centralRepoDir, ".git"); //$NON-NLS-1$
		try {
			repo = new RepositoryBuilder().setGitDir(gitDir).setBare().build();
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
			git.commit().setMessage("init").call(); //$NON-NLS-1$
			git.push().call();
		} finally {
			RepositoryUtil.closeQuietly(tempRepo);
		}
		FileUtils.forceDelete(tempGitDir.getParentFile());

		return getCentralRepository();
	}
	
	Repository getCentralRepository() throws IOException {
		if (!centralRepoDir.isDirectory()) {
			throw RepositoryNotFoundException.forCentralRepository(projectName);
		}

		return new RepositoryBuilder().findGitDir(centralRepoDir).setBare().build();
	}
	
	Repository createBranchRepository(String branchName, String startingBranch) throws IOException, GitAPIException {
		Assert.hasLength(branchName);
		if (startingBranch != null) {
			Assert.hasLength(startingBranch);
		}
		
		File repoDir = new File(reposDir, branchName);
		if (repoDir.isDirectory()) {
			throw new IllegalStateException("repository already exists: " + repoDir.getAbsolutePath()); //$NON-NLS-1$
		}

		if ((startingBranch == null) && !listBranches().isEmpty()) {
			throw new IllegalArgumentException("must specify a starting branch"); //$NON-NLS-1$
		}
		
		Repository centralRepo = null;
		File centralRepoGitDir;
		try {
			centralRepo = getCentralRepository();
			centralRepoGitDir = centralRepo.getDirectory();
		} finally {
			RepositoryUtil.closeQuietly(centralRepo);
		}
		
		Repository repo = Git.cloneRepository()
				.setURI(centralRepoGitDir.toURI().toString())
				.setDirectory(repoDir)
				.call()
				.getRepository();
		
		try {
			centralRepo = getCentralRepository();
			if (!RepositoryUtils.getBranches(centralRepo).contains(branchName)) {
				CreateBranchCommand createCommand = Git.wrap(centralRepo).branchCreate();
				if (startingBranch != null) {
					createCommand.setStartPoint(startingBranch);
				}
				createCommand.setName(branchName).call();
			}
		} finally {
			RepositoryUtil.closeQuietly(centralRepo);
		}
		
		Git git = Git.wrap(repo);
		RefSpec refSpec = new RefSpec("refs/heads/" + branchName + ":refs/remotes/origin/" + branchName); //$NON-NLS-1$ //$NON-NLS-2$
		git.fetch().setRemote("origin").setRefSpecs(refSpec).call();  //$NON-NLS-1$
		git.branchCreate().setName(branchName).setStartPoint("origin/" + branchName).call(); //$NON-NLS-1$
		git.checkout().setName(branchName).call();
		return repo;
	}
	
	Repository getBranchRepository(String branchName) throws IOException, GitAPIException {
		Assert.hasLength(branchName);
		
		File repoDir = new File(reposDir, branchName);
		if (!repoDir.isDirectory()) {
			throw new RepositoryNotFoundException(projectName, branchName);
		}

		Repository repo = new RepositoryBuilder().findGitDir(repoDir).build();
		Git.wrap(repo).pull().call();
		return repo;
	}

	public List<String> listBranches() throws IOException {
		Repository repo = null;
		try {
			repo = getCentralRepository();
			List<String> result = new ArrayList<>(RepositoryUtils.getBranches(repo));
			final int prefixLen = "refs/heads/".length(); //$NON-NLS-1$
			Function<String, String> function = new Function<String, String>() {
				@Override
				public String apply(String branch) {
					return branch.substring(prefixLen);
				}
			};
			result = new ArrayList<>(Lists.transform(result, function));
			result.remove(Constants.MASTER);
			Collections.sort(result);
			return result;
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}
}
