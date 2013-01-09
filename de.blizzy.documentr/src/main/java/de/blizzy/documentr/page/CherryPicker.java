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
package de.blizzy.documentr.page;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.CherryPickResult;
import org.eclipse.jgit.api.CherryPickResult.CherryPickStatus;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.gitective.core.CommitUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Closeables;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.ILockedRepository;
import de.blizzy.documentr.repository.RepositoryUtil;
import de.blizzy.documentr.util.Util;

@Component
class CherryPicker implements ICherryPicker {
	private static final Pattern CONFLICT_MARKERS_RE = Pattern.compile(
			"^.*?[\\r\\n]<<<<<<< .*?[\\r\\n]=======.*?[\\r\\n]>>>>>>> .*$", Pattern.DOTALL + Pattern.MULTILINE); //$NON-NLS-1$

	@Autowired
	private GlobalRepositoryManager globalRepositoryManager;
	@Autowired
	private EventBus eventBus;
	@Autowired
	private IPageStore pageStore;
	@Autowired
	private MessageSource messageSource;

	@Override
	public SortedMap<String, List<CommitCherryPickResult>> cherryPick(String projectName, String branchName, String path,
			List<String> commits, Set<String> targetBranches, Set<CommitCherryPickConflictResolve> conflictResolves,
			boolean dryRun, User user, Locale locale) throws IOException {

		Assert.hasLength(projectName);
		Assert.hasLength(path);
		Assert.notEmpty(commits);
		Assert.notEmpty(targetBranches);
		Assert.notNull(conflictResolves);
		Assert.notNull(user);

		// always do a dry run first and return early if it fails
		if (!dryRun) {
			SortedMap<String, List<CommitCherryPickResult>> results = cherryPick(
					projectName, branchName, path, commits, targetBranches, conflictResolves, true, user, locale);
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
						projectName, branchName, path, commits, targetBranch, conflictResolves, dryRun, user, locale);
				results.put(targetBranch, branchResults);
			}
			return results;
		} catch (GitAPIException e) {
			throw new IOException(e);
		}
	}

	private List<CommitCherryPickResult> cherryPick(String projectName, String branchName, String path, List<String> commits,
			String targetBranch, Set<CommitCherryPickConflictResolve> conflictResolves, boolean dryRun, User user,
			Locale locale) throws IOException, GitAPIException {

		ILockedRepository repo = null;
		List<CommitCherryPickResult> cherryPickResults = Lists.newArrayList();
		boolean hadConflicts = false;
		boolean failed = false;
		try {
			repo = globalRepositoryManager.getProjectBranchRepository(projectName, targetBranch);

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
							cherryPick(repo, branchName, path, pageVersion, targetBranch, conflictResolves, user, locale);
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
			Closeables.closeQuietly(repo);
		}

		if (!dryRun && !hadConflicts && !failed) {
			eventBus.post(new PageChangedEvent(projectName, targetBranch, path));
		}

		return cherryPickResults;
	}

	private CommitCherryPickResult cherryPick(ILockedRepository repo, String branchName, String path, PageVersion pageVersion,
			String targetBranch, Set<CommitCherryPickConflictResolve> conflictResolves, User user, Locale locale)
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
				cherryPickResult = tryResolveConflict(repo, branchName, path, pageVersion, targetBranch, conflictResolves,
						user, locale);
				break;
			default:
				cherryPickResult = null;
				break;
		}
		return cherryPickResult;
	}

	private CommitCherryPickResult tryResolveConflict(ILockedRepository repo, String branchName, String path,
			PageVersion pageVersion, String targetBranch, Set<CommitCherryPickConflictResolve> conflictResolves,
			User user, Locale locale) throws IOException, GitAPIException {

		File workingDir = RepositoryUtil.getWorkingDir(repo.r());
		File pagesDir = new File(workingDir, DocumentrConstants.PAGES_DIR_NAME);
		File workingFile = Util.toFile(pagesDir, path + DocumentrConstants.PAGE_SUFFIX);

		String resolveText = getCherryPickConflictResolveText(conflictResolves, targetBranch, pageVersion.getCommitName());
		CommitCherryPickResult result;
		if (resolveText != null) {
			if (!CONFLICT_MARKERS_RE.matcher("\n" + resolveText).matches()) { //$NON-NLS-1$
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
				result = new CommitCherryPickResult(pageVersion, resolveText);
			}
		} else {
			String text = FileUtils.readFileToString(workingFile, Charsets.UTF_8);
			text = StringUtils.replace(text, "<<<<<<< OURS", //$NON-NLS-1$
					"<<<<<<< " + messageSource.getMessage("targetBranchX", new Object[] { targetBranch }, locale)); //$NON-NLS-1$ //$NON-NLS-2$
			text = StringUtils.replace(text, ">>>>>>> THEIRS", //$NON-NLS-1$
					">>>>>>> " + messageSource.getMessage("sourceBranchX", new Object[] { branchName }, locale)); //$NON-NLS-1$ //$NON-NLS-2$
			result = new CommitCherryPickResult(pageVersion, text);
		}
		return result;
	}

	private String getCherryPickConflictResolveText(Set<CommitCherryPickConflictResolve> conflictResolves,
			String targetBranch, String commit) {

		for (CommitCherryPickConflictResolve resolve : conflictResolves) {
			if (resolve.isApplicable(targetBranch, commit)) {
				return resolve.getText();
			}
		}
		return null;
	}

	@Override
	public List<String> getCommitsList(String projectName, String branchName, String path,
			String version1, String version2) throws IOException {

		List<PageVersion> pageVersions = Lists.newArrayList(pageStore.listPageVersions(projectName, branchName, path));
		boolean foundVersion1 = false;
		boolean foundVersion2 = false;
		for (PageVersion pageVersion : pageVersions) {
			String commit = pageVersion.getCommitName();
			if (!foundVersion1 && commit.equals(version1)) {
				foundVersion1 = true;
			}
			if (!foundVersion2 && commit.equals(version2)) {
				foundVersion2 = true;
			}
			if (foundVersion1 && foundVersion2) {
				break;
			}
		}
		if (!foundVersion1 || !foundVersion2) {
			throw new IllegalArgumentException("one of version1 or version2 not found in version history of page"); //$NON-NLS-1$
		}

		Collections.reverse(pageVersions);
		boolean include = false;
		for (Iterator<PageVersion> iter = pageVersions.iterator(); iter.hasNext();) {
			PageVersion version = iter.next();
			if (!include) {
				iter.remove();
			}

			String commit = version.getCommitName();
			if (commit.equals(version1)) {
				include = true;
			} else if (commit.equals(version2)) {
				include = false;
			}
		}
		Function<PageVersion, String> function = new Function<PageVersion, String>() {
			@Override
			public String apply(PageVersion version) {
				return version.getCommitName();
			}
		};
		return Lists.transform(pageVersions, function);
	}
}
