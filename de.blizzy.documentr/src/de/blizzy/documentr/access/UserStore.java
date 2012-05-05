package de.blizzy.documentr.access;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gitective.core.BlobUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Component;

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
		ILockedRepository repo = null;
		try {
			repo = repoManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			Map<String, Object> userMap = new HashMap<>();
			userMap.put("loginName", user.getLoginName()); //$NON-NLS-1$
			userMap.put("password", user.getPassword()); //$NON-NLS-1$
			userMap.put("locked", Boolean.valueOf(user.isLocked())); //$NON-NLS-1$
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
			boolean locked = ((Boolean) userMap.get("locked")).booleanValue(); //$NON-NLS-1$
			boolean admin = ((Boolean) userMap.get("admin")).booleanValue(); //$NON-NLS-1$
			User user = new User(loginName, password, locked, admin);
			return user;
		} catch (IOException e) {
			throw new IOException(e);
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
	}
}
