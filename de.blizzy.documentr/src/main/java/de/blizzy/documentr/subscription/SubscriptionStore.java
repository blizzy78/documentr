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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.ILockedRepository;
import de.blizzy.documentr.repository.RepositoryNotFoundException;
import de.blizzy.documentr.repository.RepositoryUtil;
import de.blizzy.documentr.util.Util;

@Component
@Slf4j
public class SubscriptionStore {
	private static final String REPOSITORY_NAME = "_subscriptions"; //$NON-NLS-1$
	private static final String SUBSCRIPTIONS_SUFFIX = ".subscriptions"; //$NON-NLS-1$

	@Autowired
	private GlobalRepositoryManager globalRepositoryManager;
	@Autowired
	private UserStore userStore;

	public void subscribe(String projectName, String branchName, String path, User user) throws IOException {
		ILockedRepository repo = null;
		try {
			repo = getOrCreateRepository(user);
			String loginName = user.getLoginName();
			String json = BlobUtils.getHeadContent(repo.r(), loginName + SUBSCRIPTIONS_SUFFIX);
			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
			Set<Page> pages = Sets.newHashSet();
			if (StringUtils.isNotBlank(json)) {
				List<Page> pagesList = gson.fromJson(json, new TypeToken<List<Page>>() {}.getType());
				pages = Sets.newHashSet(pagesList);
			}

			Page page = new Page(projectName, branchName, path);
			if (pages.add(page)) {
				json = gson.toJson(pages);
				File workingDir = RepositoryUtil.getWorkingDir(repo.r());
				File file = new File(workingDir, loginName + SUBSCRIPTIONS_SUFFIX);
				FileUtils.writeStringToFile(file, json, Charsets.UTF_8);

				Git git = Git.wrap(repo.r());
				git.add()
					.addFilepattern(loginName + SUBSCRIPTIONS_SUFFIX)
					.call();
				PersonIdent ident = new PersonIdent(loginName, user.getEmail());
				git.commit()
					.setAuthor(ident)
					.setCommitter(ident)
					.setMessage(loginName + SUBSCRIPTIONS_SUFFIX)
					.call();
			}
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			Closeables.closeQuietly(repo);
		}
	}

	public void unsubscribe(String projectName, String branchName, String path, User user) throws IOException {
		ILockedRepository repo = null;
		try {
			repo = getOrCreateRepository(user);
			String json = BlobUtils.getHeadContent(repo.r(), user.getLoginName() + SUBSCRIPTIONS_SUFFIX);
			if (StringUtils.isNotBlank(json)) {
				Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
				List<Page> pagesList = gson.fromJson(json, new TypeToken<List<Page>>() {}.getType());
				Set<Page> pages = Sets.newHashSet(pagesList);
				Page page = new Page(projectName, branchName, path);
				if (pages.remove(page)) {
					Git git = Git.wrap(repo.r());
					if (!pages.isEmpty()) {
						json = gson.toJson(pages);
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

					PersonIdent ident = new PersonIdent(user.getLoginName(), user.getEmail());
					git.commit()
						.setAuthor(ident)
						.setCommitter(ident)
						.setMessage(user.getLoginName() + SUBSCRIPTIONS_SUFFIX)
						.call();
				}
			}
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			Closeables.closeQuietly(repo);
		}
	}

	public boolean isSubscribed(String projectName, String branchName, String path, User user) throws IOException {
		ILockedRepository repo = null;
		try {
			repo = getOrCreateRepository(user);
			return isSubscribed(projectName, branchName, path, user, repo);
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			Closeables.closeQuietly(repo);
		}
	}

	private boolean isSubscribed(String projectName, String branchName, String path, User user, ILockedRepository repo) {
		String json = BlobUtils.getHeadContent(repo.r(), user.getLoginName() + SUBSCRIPTIONS_SUFFIX);
		if (StringUtils.isNotBlank(json)) {
			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
			List<Page> pagesList = gson.fromJson(json, new TypeToken<List<Page>>() {}.getType());
			Page page = new Page(projectName, branchName, path);
			return pagesList.contains(page);
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
			Closeables.closeQuietly(repo);
		}
	}

	private ILockedRepository getOrCreateRepository(User user) throws IOException, GitAPIException {
		try {
			return globalRepositoryManager.getProjectCentralRepository(REPOSITORY_NAME, false);
		} catch (RepositoryNotFoundException e) {
			return globalRepositoryManager.createProjectCentralRepository(REPOSITORY_NAME, false, user);
		}
	}
}
