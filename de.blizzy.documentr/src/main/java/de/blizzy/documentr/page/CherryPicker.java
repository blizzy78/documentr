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
package de.blizzy.documentr.page;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CherryPickResult;
import org.eclipse.jgit.api.CherryPickResult.CherryPickStatus;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.gitective.core.CommitUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.ILockedRepository;
import de.blizzy.documentr.repository.RepositoryUtil;
import de.blizzy.documentr.util.Util;

@Component
class CherryPicker implements ICherryPicker {
	@Autowired
	private GlobalRepositoryManager repoManager;
	@Autowired
	private EventBus eventBus;
	
	@Override
	public SortedMap<String, List<CommitCherryPickResult>> cherryPick(String projectName, String path, List<String> commits,
			Set<String> targetBranches, Set<CommitCherryPickConflictResolve> conflictResolves, boolean dryRun,
			User user) throws IOException {

		Assert.hasLength(projectName);
		Assert.hasLength(path);
		Assert.notEmpty(commits);
		Assert.notEmpty(targetBranches);
		Assert.notNull(conflictResolves);
		Assert.notNull(user);

		// always do a dry run first and return early if it fails
		if (!dryRun) {
			SortedMap<String, List<CommitCherryPickResult>> results = cherryPick(
					projectName, path, commits, targetBranches, conflictResolves, true, user);
			for (List<CommitCherryPickResult> branchResults : results.values()) {
				for (CommitCherryPickResult result : branchResults) {
					if (result.getStatus() != CommitCherryPickResult.Status.OK) {
						return results;
					}
				}
			}
		}
		
		try {
			SortedMap<String, List<CommitCherryPickResult>> results = Maps.newTreeMap();
			for (String targetBranch : targetBranches) {
				List<CommitCherryPickResult> branchResults = cherryPick(
						projectName, path, commits, targetBranch, conflictResolves, dryRun, user);
				results.put(targetBranch, branchResults);
			}
			return results;
		} catch (GitAPIException e) {
			throw new IOException(e);
		}
	}

	private List<CommitCherryPickResult> cherryPick(String projectName, String path, List<String> commits,
			String targetBranch, Set<CommitCherryPickConflictResolve> conflictResolves, boolean dryRun, User user)
			throws IOException, GitAPIException {
		
		ILockedRepository repo = null;
		List<CommitCherryPickResult> cherryPickResults = Lists.newArrayList();
		boolean hadConflicts = false;
		boolean failed = false;
		try {
			repo = repoManager.getProjectBranchRepository(projectName, targetBranch);
			
			String tempBranchName = "_temp_" + String.valueOf((long) (Math.random() * Long.MAX_VALUE)); //$NON-NLS-1$
			Git git = Git.wrap(repo.r());
			
			git.branchCreate()
				.setName(tempBranchName)
				.setStartPoint(targetBranch)
				.call();
			
			git.checkout()
				.setName(tempBranchName)
				.call();

			for (String commit : commits) {
				PageVersion pageVersion = PageUtil.toPageVersion(CommitUtils.getCommit(repo.r(), commit));
				if (!hadConflicts) {
					CommitCherryPickResult singleCherryPickResult =
							cherryPick(repo, path, pageVersion, targetBranch, conflictResolves, user);
					if (singleCherryPickResult != null) {
						cherryPickResults.add(singleCherryPickResult);
						if (singleCherryPickResult.getStatus() == CommitCherryPickResult.Status.CONFLICT) {
							hadConflicts = true;
						}
					} else {
						failed = true;
						break;
					}
				} else {
					cherryPickResults.add(new CommitCherryPickResult(pageVersion, CommitCherryPickResult.Status.UNKNOWN));
				}
			}

			if (hadConflicts || failed) {
				git.reset()
					.setMode(ResetCommand.ResetType.HARD)
					.call();
			}
			
			git.checkout()
				.setName(targetBranch)
				.call();
			
			if (!dryRun && !hadConflicts && !failed) {
				git.merge()
					.include(repo.r().resolve(tempBranchName))
					.call();
			}
			
			git.branchDelete()
				.setBranchNames(tempBranchName)
				.setForce(true)
				.call();

			if (failed) {
				throw new IOException("cherry-picking failed"); //$NON-NLS-1$
			}
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}

		if (!dryRun && !hadConflicts && !failed) {
			eventBus.post(new PageChangedEvent(projectName, targetBranch, path));
		}

		return cherryPickResults;
	}
	
	private CommitCherryPickResult cherryPick(ILockedRepository repo, String path, PageVersion pageVersion,
			String targetBranch, Set<CommitCherryPickConflictResolve> conflictResolves, User user)
			throws IOException, GitAPIException {
		
		CommitCherryPickResult cherryPickResult;
		CherryPickResult result = Git.wrap(repo.r()).cherryPick()
				.include(repo.r().resolve(pageVersion.getCommitName()))
				.call();
		CherryPickStatus status = result.getStatus();
		switch (status) {
			case OK:
				cherryPickResult = new CommitCherryPickResult(pageVersion, CommitCherryPickResult.Status.OK);
				break;
			case CONFLICTING:
				cherryPickResult = tryResolveConflict(repo, path, pageVersion, targetBranch, conflictResolves, user);
				break;
			default:
				cherryPickResult = null;
				break;
		}
		return cherryPickResult;
	}

	private CommitCherryPickResult tryResolveConflict(ILockedRepository repo, String path, PageVersion pageVersion,
			String targetBranch, Set<CommitCherryPickConflictResolve> conflictResolves, User user)
			throws IOException, GitAPIException {
		
		File workingDir = RepositoryUtil.getWorkingDir(repo.r());
		File pagesDir = new File(workingDir, DocumentrConstants.PAGES_DIR_NAME);
		File workingFile = Util.toFile(pagesDir, path + DocumentrConstants.PAGE_SUFFIX);

		String resolveText = getCherryPickConflictResolveText(conflictResolves, targetBranch, pageVersion.getCommitName());
		CommitCherryPickResult result;
		if (resolveText != null) {
			FileUtils.writeStringToFile(workingFile, resolveText, Charsets.UTF_8.name());
			Git git = Git.wrap(repo.r());
			git.add()
				.addFilepattern(DocumentrConstants.PAGES_DIR_NAME + "/" + path + DocumentrConstants.PAGE_SUFFIX) //$NON-NLS-1$
				.call();
			PersonIdent ident = new PersonIdent(user.getLoginName(), user.getEmail());
			git.commit()
				.setAuthor(ident)
				.setCommitter(ident)
				.setMessage(DocumentrConstants.PAGES_DIR_NAME + "/" + path + DocumentrConstants.PAGE_SUFFIX) //$NON-NLS-1$
				.call();
			result = new CommitCherryPickResult(pageVersion, CommitCherryPickResult.Status.OK);
		} else {
			String text = FileUtils.readFileToString(workingFile, Charsets.UTF_8);
			result = new CommitCherryPickResult(pageVersion, text);
		}
		return result;
	}

	private String getCherryPickConflictResolveText(Set<CommitCherryPickConflictResolve> conflictResolves,
			String targetBranch, String commit) {
		
		for (CommitCherryPickConflictResolve resolve : conflictResolves) {
			if (resolve.getTargetBranch().equals(targetBranch) && resolve.getCommit().equals(commit)) {
				return resolve.getText();
			}
		}
		return null;
	}

	void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	void setGlobalRepositoryManager(GlobalRepositoryManager repoManager) {
		this.repoManager = repoManager;
	}
}
