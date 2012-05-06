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
package de.blizzy.documentr.access;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gitective.core.BlobUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.ILockedRepository;
import de.blizzy.documentr.repository.RepositoryUtil;

@Component
public class UserStore {
	private static final String REPOSITORY_NAME = "_users"; //$NON-NLS-1$
	private static final String USER_SUFFIX = ".user"; //$NON-NLS-1$
	@Autowired
	private GlobalRepositoryManager repoManager;
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@PostConstruct
	public void init() throws IOException, GitAPIException {
		ILockedRepository repo = null;
		boolean created = false;
		try {
			repo = repoManager.createProjectCentralRepository(REPOSITORY_NAME, false);
			created = true;
		} catch (IllegalStateException e) {
			// okay
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
		
		if (created) {
			createInitialAdmin();
		}
	}

	private void createInitialAdmin() throws IOException {
		String passwordHash = passwordEncoder.encodePassword("admin", "admin"); //$NON-NLS-1$ //$NON-NLS-2$
		User user = new User("admin", passwordHash, false, true); //$NON-NLS-1$
		saveUser(user);
	}

	public void saveUser(User user) throws IOException {
		Assert.notNull(user);
		
		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			Map<String, Object> userMap = new HashMap<>();
			userMap.put("loginName", user.getLoginName()); //$NON-NLS-1$
			userMap.put("password", user.getPassword()); //$NON-NLS-1$
			userMap.put("disabled", Boolean.valueOf(user.isDisabled())); //$NON-NLS-1$
			userMap.put("admin", Boolean.valueOf(user.isAdmin())); //$NON-NLS-1$

			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
			String json = gson.toJson(userMap);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			File workingFile = new File(workingDir, user.getLoginName() + USER_SUFFIX);
			FileUtils.write(workingFile, json, "UTF-8"); //$NON-NLS-1$

			Git git = Git.wrap(repo.r());
			git.add().addFilepattern(user.getLoginName() + USER_SUFFIX).call();
			git.commit().setMessage(user.getLoginName()).call();
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}
	
	public User getUser(String loginName) throws IOException {
		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			String json = BlobUtils.getHeadContent(repo.r(), loginName + USER_SUFFIX);
			if (json == null) {
				throw new UserNotFoundException(loginName);
			}
			
			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
			Map<String, Object> userMap = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
			String password = (String) userMap.get("password"); //$NON-NLS-1$
			boolean disabled = ((Boolean) userMap.get("disabled")).booleanValue(); //$NON-NLS-1$
			boolean admin = ((Boolean) userMap.get("admin")).booleanValue(); //$NON-NLS-1$
			User user = new User(loginName, password, disabled, admin);
			return user;
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}

	public List<String> listUsers() throws IOException {
		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.isFile() && file.getName().endsWith(USER_SUFFIX);
				}
			};
			List<File> files = Arrays.asList(workingDir.listFiles(filter));
			Function<File, String> function = new Function<File, String>() {
				@Override
				public String apply(File file) {
					return StringUtils.substringBeforeLast(file.getName(), USER_SUFFIX);
				}
			};
			List<String> users = new ArrayList<>(Lists.transform(files, function));
			Collections.sort(users);
			return users;
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}
}
