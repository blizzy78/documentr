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
package de.blizzy.documentr.subscription;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.gitective.core.BlobUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.blizzy.documentr.access.User;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.ILockedRepository;
import de.blizzy.documentr.repository.RepositoryNotFoundException;
import de.blizzy.documentr.repository.RepositoryUtil;

@Component
public class SubscriptionStore {
	private static final String REPOSITORY_NAME = "_subscriptions"; //$NON-NLS-1$
	private static final String SUBSCRIPTIONS_SUFFIX = ".subscriptions"; //$NON-NLS-1$
	
	@Autowired
	private GlobalRepositoryManager globalRepositoryManager;

	public void subscribe(String projectName, String branchName, String path, User user) throws IOException {
		ILockedRepository repo = null;
		try {
			repo = getOrCreateRepository(user);
			String json = BlobUtils.getHeadContent(repo.r(), user.getLoginName() + SUBSCRIPTIONS_SUFFIX);
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
				File file = new File(workingDir, user.getLoginName() + SUBSCRIPTIONS_SUFFIX);
				FileUtils.writeStringToFile(file, json, Charsets.UTF_8);
				
				Git git = Git.wrap(repo.r());
				git.add()
					.addFilepattern(user.getLoginName() + SUBSCRIPTIONS_SUFFIX)
					.call();
				PersonIdent ident = new PersonIdent(user.getLoginName(), user.getEmail());
				git.commit()
					.setAuthor(ident)
					.setCommitter(ident)
					.setMessage(user.getLoginName() + SUBSCRIPTIONS_SUFFIX)
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
			String json = BlobUtils.getHeadContent(repo.r(), user.getLoginName() + SUBSCRIPTIONS_SUFFIX);
			if (StringUtils.isNotBlank(json)) {
				Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
				List<Page> pagesList = gson.fromJson(json, new TypeToken<List<Page>>() {}.getType());
				Page page = new Page(projectName, branchName, path);
				return pagesList.contains(page);
			} else {
				return false;
			}
		} catch (GitAPIException e) {
			throw new IOException(e);
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
