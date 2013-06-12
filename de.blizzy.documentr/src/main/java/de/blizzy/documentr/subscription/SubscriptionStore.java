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
package de.blizzy.documentr.subscription;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.gitective.core.BlobUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.repository.IGlobalRepositoryManager;
import de.blizzy.documentr.repository.ILockedRepository;
import de.blizzy.documentr.repository.ProjectBranchDeletedEvent;
import de.blizzy.documentr.repository.ProjectBranchRenamedEvent;
import de.blizzy.documentr.repository.ProjectDeletedEvent;
import de.blizzy.documentr.repository.ProjectRenamedEvent;
import de.blizzy.documentr.repository.RepositoryNotFoundException;
import de.blizzy.documentr.repository.RepositoryUtil;
import de.blizzy.documentr.util.Util;

@Component
@Slf4j
public class SubscriptionStore {
	private static final String REPOSITORY_NAME = "_subscriptions"; //$NON-NLS-1$
	private static final String SUBSCRIPTIONS_SUFFIX = ".subscriptions"; //$NON-NLS-1$

	@Autowired
	private IGlobalRepositoryManager globalRepositoryManager;
	@Autowired
	private UserStore userStore;

	public void subscribe(String projectName, String branchName, String path, User user) throws IOException {
		ILockedRepository repo = null;
		try {
			repo = getOrCreateRepository(user);
			Set<Page> pages = getSubscriptions(user, repo);
			Page page = new Page(projectName, branchName, path);
			if (pages.add(page)) {
				saveSubscriptions(user, pages, repo, true);
			}
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			Util.closeQuietly(repo);
		}
	}

	public void unsubscribe(String projectName, String branchName, String path, User user) throws IOException {
		ILockedRepository repo = null;
		try {
			repo = getOrCreateRepository(user);
			Set<Page> pages = getSubscriptions(user, repo);
			Page page = new Page(projectName, branchName, path);
			if (pages.remove(page)) {
				saveSubscriptions(user, pages, repo, true);
			}
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			Util.closeQuietly(repo);
		}
	}

	private Set<Page> getSubscriptions(User user, ILockedRepository repo) {
		String json = BlobUtils.getHeadContent(repo.r(), user.getLoginName() + SUBSCRIPTIONS_SUFFIX);
		Set<Page> pages = Sets.newHashSet();
		if (StringUtils.isNotBlank(json)) {
			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
			List<Page> pagesList = gson.fromJson(json, new TypeToken<List<Page>>() {}.getType());
			pages.addAll(pagesList);
		}
		return pages;
	}

	private void saveSubscriptions(User user, Set<Page> pages, ILockedRepository repo, boolean commit) throws IOException, GitAPIException {
		Git git = Git.wrap(repo.r());
		if (!pages.isEmpty()) {
			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
			String json = gson.toJson(pages);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			File file = new File(workingDir, user.getLoginName() + SUBSCRIPTIONS_SUFFIX);
			FileUtils.writeStringToFile(file, json, Charsets.UTF_8);
			git.add()
				.addFilepattern(user.getLoginName() + SUBSCRIPTIONS_SUFFIX)
				.call();
		} else {
			git.rm()
				.addFilepattern(user.getLoginName() + SUBSCRIPTIONS_SUFFIX)
				.call();
		}

		if (commit) {
			PersonIdent ident = new PersonIdent(user.getLoginName(), user.getEmail());
			git.commit()
				.setAuthor(ident)
				.setCommitter(ident)
				.setMessage(user.getLoginName() + SUBSCRIPTIONS_SUFFIX)
				.call();
		}
	}

	public boolean isSubscribed(String projectName, String branchName, String path, User user) throws IOException {
		if (!user.isDisabled()) {
			ILockedRepository repo = null;
			try {
				repo = getOrCreateRepository(user);
				return isSubscribed(projectName, branchName, path, user, repo);
			} catch (GitAPIException e) {
				throw new IOException(e);
			} finally {
				Util.closeQuietly(repo);
			}
		} else {
			return false;
		}
	}

	private boolean isSubscribed(String projectName, String branchName, String path, User user, ILockedRepository repo) {
		if (!user.isDisabled()) {
			Set<Page> pages = getSubscriptions(user, repo);
			Page page = new Page(projectName, branchName, path);
			return pages.contains(page);
		} else {
			return false;
		}
	}

	public Set<String> getSubscriberEmails(String projectName, String branchName, String path) throws IOException {
		ILockedRepository repo = null;
		try {
			repo = globalRepositoryManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			FileFilter fileFilter = new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.isFile() && file.getName().endsWith(SUBSCRIPTIONS_SUFFIX);
				}
			};
			List<File> files = Lists.newArrayList(workingDir.listFiles(fileFilter));
			Function<File, String> loginNamesFunction = new Function<File, String>() {
				@Override
				public String apply(File file) {
					return StringUtils.removeEnd(file.getName(), SUBSCRIPTIONS_SUFFIX);
				}
			};
			List<String> loginNames = Lists.transform(files, loginNamesFunction);
			Set<String> emails = Sets.newHashSet();
			for (Iterator<String> iter = loginNames.iterator(); iter.hasNext();) {
				String loginName = iter.next();
				User user = userStore.getUser(loginName);
				if (isSubscribed(projectName, branchName, path, user, repo)) {
					emails.add(user.getEmail());
				}
			}
			log.debug("emails subscribed to {}/{}/{}: {}", projectName, branchName, Util.toUrlPagePath(path), emails); //$NON-NLS-1$
			return emails;
		} finally {
			Util.closeQuietly(repo);
		}
	}

	private ILockedRepository getOrCreateRepository(User user) throws IOException, GitAPIException {
		try {
			return globalRepositoryManager.getProjectCentralRepository(REPOSITORY_NAME, false);
		} catch (RepositoryNotFoundException e) {
			return globalRepositoryManager.createProjectCentralRepository(REPOSITORY_NAME, false, user);
		}
	}

	@Subscribe
	public void renameProject(ProjectRenamedEvent event) {
		final String projectName = event.getProjectName();
		final String newProjectName = event.getNewProjectName();
		Function<Page, Page> function = new Function<Page, Page>() {
			@Override
			public Page apply(Page page) {
				return page.getProjectName().equals(projectName) ?
						new Page(newProjectName, page.getBranchName(), page.getPath()) :
						page;
			}
		};

		try {
			transformAllSubscriptions(function, "rename project " + projectName + " to " + newProjectName, //$NON-NLS-1$ //$NON-NLS-2$
					event.getCurrentUser());
		} catch (IOException e) {
			log.error(StringUtils.EMPTY, e);
		} catch (GitAPIException e) {
			log.error(StringUtils.EMPTY, e);
		}
	}

	private void transformAllSubscriptions(Function<Page, Page> function, String commitMessage, User currentUser)
			throws IOException, GitAPIException {

		ILockedRepository repo = null;
		try {
			List<String> users = userStore.listUsers();
			repo = getOrCreateRepository(currentUser);
			boolean anyChanged = false;
			for (String loginName : users) {
				User user = userStore.getUser(loginName);
				List<Page> pages = Lists.newArrayList(getSubscriptions(user, repo));
				List<Page> newPages = Lists.newArrayList(Lists.transform(pages, function));
				if (!newPages.equals(pages)) {
					saveSubscriptions(user, Sets.newHashSet(newPages), repo, false);
					anyChanged = true;
				}
			}

			if (anyChanged) {
				PersonIdent ident = new PersonIdent(currentUser.getLoginName(), currentUser.getEmail());
				Git.wrap(repo.r()).commit()
					.setAuthor(ident)
					.setCommitter(ident)
					.setMessage(commitMessage)
					.call();
			}
		} finally {
			Util.closeQuietly(repo);
		}
	}

	@Subscribe
	public void deleteProject(ProjectDeletedEvent event) {
		final String projectName = event.getProjectName();
		Predicate<Page> predicate = new Predicate<Page>() {
			@Override
			public boolean apply(Page page) {
				return !page.getProjectName().equals(projectName);
			}
		};
		try {
			deleteFromAllSubscriptions(predicate, "delete project " + projectName, event.getCurrentUser()); //$NON-NLS-1$
		} catch (IOException e) {
			log.error(StringUtils.EMPTY, e);
		} catch (GitAPIException e) {
			log.error(StringUtils.EMPTY, e);
		}
	}

	private void deleteFromAllSubscriptions(Predicate<Page> predicate, String commitMessage, User currentUser)
			throws IOException, GitAPIException {

		ILockedRepository repo = null;
		try {
			List<String> users = userStore.listUsers();
			repo = getOrCreateRepository(currentUser);
			boolean anyChanged = false;
			for (String loginName : users) {
				User user = userStore.getUser(loginName);
				Set<Page> pages = getSubscriptions(user, repo);
				Set<Page> newPages = Sets.newHashSet(Sets.filter(pages, predicate));
				if (!newPages.equals(pages)) {
					saveSubscriptions(user, newPages, repo, false);
					anyChanged = true;
				}
			}

			if (anyChanged) {
				PersonIdent ident = new PersonIdent(currentUser.getLoginName(), currentUser.getEmail());
				Git.wrap(repo.r()).commit()
					.setAuthor(ident)
					.setCommitter(ident)
					.setMessage(commitMessage)
					.call();
			}
		} finally {
			Util.closeQuietly(repo);
		}
	}

	@Subscribe
	public void deleteProjectBranch(ProjectBranchDeletedEvent event) {
		final String projectName = event.getProjectName();
		final String branchName = event.getBranchName();
		Predicate<Page> predicate = new Predicate<Page>() {
			@Override
			public boolean apply(Page page) {
				return !page.getProjectName().equals(projectName) || !page.getBranchName().equals(branchName);
			}
		};
		try {
			deleteFromAllSubscriptions(predicate, "delete branch " + projectName + "/" + branchName, //$NON-NLS-1$ //$NON-NLS-2$
					event.getCurrentUser());
		} catch (IOException e) {
			log.error(StringUtils.EMPTY, e);
		} catch (GitAPIException e) {
			log.error(StringUtils.EMPTY, e);
		}
	}

	@Subscribe
	public void renameProjectBranch(ProjectBranchRenamedEvent event) {
		final String projectName = event.getProjectName();
		final String branchName = event.getBranchName();
		final String newBranchName = event.getNewBranchName();
		Function<Page, Page> function = new Function<Page, Page>() {
			@Override
			public Page apply(Page page) {
				return page.getProjectName().equals(projectName) && page.getBranchName().equals(branchName) ?
						new Page(projectName, newBranchName, page.getPath()) :
						page;
			}
		};

		try {
			transformAllSubscriptions(function,
					"rename branch " + projectName + "/" + branchName + " to " + projectName + "/" + newBranchName, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					event.getCurrentUser());
		} catch (IOException e) {
			log.error(StringUtils.EMPTY, e);
		} catch (GitAPIException e) {
			log.error(StringUtils.EMPTY, e);
		}
	}
}
