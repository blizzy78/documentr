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
package de.blizzy.documentr.access;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.gitective.core.BlobUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import de.blizzy.documentr.access.GrantedAuthorityTarget.Type;
import de.blizzy.documentr.repository.IGlobalRepositoryManager;
import de.blizzy.documentr.repository.ILockedRepository;
import de.blizzy.documentr.repository.ProjectBranchDeletedEvent;
import de.blizzy.documentr.repository.ProjectBranchRenamedEvent;
import de.blizzy.documentr.repository.ProjectDeletedEvent;
import de.blizzy.documentr.repository.ProjectRenamedEvent;
import de.blizzy.documentr.repository.RepositoryUtil;
import de.blizzy.documentr.util.Util;

/** Manages storage of user account data. */
@Component
@Slf4j
public class UserStore {
	/** The login name of the anonymous user. */
	public static final String ANONYMOUS_USER_LOGIN_NAME = "_anonymous"; //$NON-NLS-1$

	private static final String REPOSITORY_NAME = "_users"; //$NON-NLS-1$
	private static final String USER_SUFFIX = ".user"; //$NON-NLS-1$
	private static final String ROLE_SUFFIX = ".role"; //$NON-NLS-1$
	private static final String AUTHORITIES_SUFFIX = ".authorities"; //$NON-NLS-1$

	@Autowired
	private IGlobalRepositoryManager globalRepositoryManager;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@PostConstruct
	public void init() throws IOException, GitAPIException {
		String passwordHash = passwordEncoder.encode("admin"); //$NON-NLS-1$
		User adminUser = new User("admin", passwordHash, "admin@example.com", false); //$NON-NLS-1$ //$NON-NLS-2$

		ILockedRepository repo = null;
		boolean created = false;
		try {
			repo = globalRepositoryManager.createProjectCentralRepository(REPOSITORY_NAME, false, adminUser);
			created = true;
		} catch (IllegalStateException e) {
			// okay
		} finally {
			Util.closeQuietly(repo);
		}

		if (created) {
			createInitialAdmin(adminUser);
			createInitialRoles(adminUser);
		}
	}

	private void createInitialAdmin(User adminUser) throws IOException {
		saveUser(adminUser, adminUser);
	}

	private void createInitialRoles(User adminUser) throws IOException {
		saveRole(new Role("Administrator", EnumSet.of(Permission.ADMIN)), adminUser); //$NON-NLS-1$
		saveRole(new Role("Editor", EnumSet.of(Permission.EDIT_BRANCH, Permission.EDIT_PAGE)), adminUser); //$NON-NLS-1$
		saveRole(new Role("Reader", EnumSet.of(Permission.VIEW)), adminUser); //$NON-NLS-1$

		Set<RoleGrantedAuthority> authorities = Collections.singleton(
				new RoleGrantedAuthority(GrantedAuthorityTarget.APPLICATION, "Administrator")); //$NON-NLS-1$
		saveUserAuthorities(adminUser.getLoginName(), authorities, adminUser);

		authorities = Collections.singleton(
				new RoleGrantedAuthority(GrantedAuthorityTarget.APPLICATION, "Reader")); //$NON-NLS-1$
		saveUserAuthorities(ANONYMOUS_USER_LOGIN_NAME, authorities, adminUser);
	}

	/**
	 * Saves a user.
	 *
	 * @param user the user to save
	 * @param currentUser the user performing the save operation
	 */
	public void saveUser(User user, User currentUser) throws IOException {
		Assert.notNull(user);
		Assert.notNull(currentUser);

		ILockedRepository repo = null;
		try {
			repo = globalRepositoryManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			Map<String, Object> userMap = new HashMap<String, Object>();
			userMap.put("loginName", user.getLoginName()); //$NON-NLS-1$
			userMap.put("password", user.getPassword()); //$NON-NLS-1$
			userMap.put("email", user.getEmail()); //$NON-NLS-1$
			userMap.put("disabled", Boolean.valueOf(user.isDisabled())); //$NON-NLS-1$
			if (!user.getOpenIds().isEmpty()) {
				userMap.put("openIds", user.getOpenIds()); //$NON-NLS-1$
			}

			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
			String json = gson.toJson(userMap);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			File workingFile = new File(workingDir, user.getLoginName() + USER_SUFFIX);
			FileUtils.write(workingFile, json, Charsets.UTF_8);

			Git git = Git.wrap(repo.r());
			git.add().addFilepattern(user.getLoginName() + USER_SUFFIX).call();
			PersonIdent ident = new PersonIdent(currentUser.getLoginName(), currentUser.getEmail());
			git.commit()
				.setAuthor(ident)
				.setCommitter(ident)
				.setMessage(user.getLoginName())
				.call();
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			Util.closeQuietly(repo);
		}
	}

	/**
	 * Returns the user that has the specified login name.
	 *
	 * @throws UserNotFoundException when the user could not be found
	 */
	public User getUser(String loginName) throws IOException {
		Assert.hasLength(loginName);

		ILockedRepository repo = null;
		try {
			repo = globalRepositoryManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			String json = BlobUtils.getHeadContent(repo.r(), loginName + USER_SUFFIX);
			if (json == null) {
				throw new UserNotFoundException(loginName);
			}

			return getUser(loginName, json);
		} finally {
			Util.closeQuietly(repo);
		}
	}

	private User getUser(String loginName, String json) {
		Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
		Map<String, Object> userMap = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
		String password = (String) userMap.get("password"); //$NON-NLS-1$
		String email = (String) userMap.get("email"); //$NON-NLS-1$
		boolean disabled = ((Boolean) userMap.get("disabled")).booleanValue(); //$NON-NLS-1$
		@SuppressWarnings("unchecked")
		List<Map<String, String>> openIds = (List<Map<String, String>>) userMap.get("openIds"); //$NON-NLS-1$
		if (openIds == null) {
			openIds = Lists.newArrayList();
		}
		User user = new User(loginName, password, email, disabled);
		for (Map<String, String> map : openIds) {
			OpenId openId = new OpenId(map.get("delegateId"), map.get("realId")); //$NON-NLS-1$ //$NON-NLS-2$
			user.addOpenId(openId);
		}
		return user;
	}

	/** Returns all known user login names. */
	public List<String> listUsers() throws IOException {
		ILockedRepository repo = null;
		try {
			repo = globalRepositoryManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			return listUsers(repo);
		} finally {
			Util.closeQuietly(repo);
		}
	}

	private List<String> listUsers(ILockedRepository repo) {
		File workingDir = RepositoryUtil.getWorkingDir(repo.r());
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(USER_SUFFIX);
			}
		};
		List<File> files = Lists.newArrayList(workingDir.listFiles(filter));
		Function<File, String> function = new Function<File, String>() {
			@Override
			public String apply(File file) {
				return StringUtils.substringBeforeLast(file.getName(), USER_SUFFIX);
			}
		};
		List<String> users = Lists.newArrayList(Lists.transform(files, function));
		Collections.sort(users);
		return users;
	}

	/**
	 * Saves a role.
	 *
	 * @param role the role to save
	 * @param currentUser the user performing the save operation
	 */
	public void saveRole(Role role, User currentUser) throws IOException {
		Assert.notNull(role);
		Assert.notNull(currentUser);

		ILockedRepository repo = null;
		try {
			repo = globalRepositoryManager.getProjectCentralRepository(REPOSITORY_NAME, false);

			Map<String, Object> roleMap = new HashMap<String, Object>();
			roleMap.put("name", role.getName()); //$NON-NLS-1$
			Set<String> permissions = Sets.newHashSet();
			for (Permission permission : role.getPermissions()) {
				permissions.add(permission.name());
			}
			roleMap.put("permissions", permissions); //$NON-NLS-1$

			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
			String json = gson.toJson(roleMap);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			File workingFile = new File(workingDir, role.getName() + ROLE_SUFFIX);
			FileUtils.write(workingFile, json, Charsets.UTF_8);

			Git git = Git.wrap(repo.r());
			git.add().addFilepattern(role.getName() + ROLE_SUFFIX).call();
			PersonIdent ident = new PersonIdent(currentUser.getLoginName(), currentUser.getEmail());
			git.commit()
				.setAuthor(ident)
				.setCommitter(ident)
				.setMessage(role.getName())
				.call();
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			Util.closeQuietly(repo);
		}
	}

	/** Returns all known role names. */
	public List<String> listRoles() throws IOException {
		ILockedRepository repo = null;
		try {
			repo = globalRepositoryManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.isFile() && file.getName().endsWith(ROLE_SUFFIX);
				}
			};
			List<File> files = Lists.newArrayList(workingDir.listFiles(filter));
			Function<File, String> function = new Function<File, String>() {
				@Override
				public String apply(File file) {
					return StringUtils.substringBeforeLast(file.getName(), ROLE_SUFFIX);
				}
			};
			List<String> users = Lists.newArrayList(Lists.transform(files, function));
			Collections.sort(users);
			return users;
		} finally {
			Util.closeQuietly(repo);
		}
	}

	/**
	 * Returns the role that has the specified name.
	 *
	 * @throws RoleNotFoundException when the role could not be found
	 */
	public Role getRole(String roleName) throws IOException {
		Assert.hasLength(roleName);

		ILockedRepository repo = null;
		try {
			repo = globalRepositoryManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			String json = BlobUtils.getHeadContent(repo.r(), roleName + ROLE_SUFFIX);
			if (json == null) {
				throw new RoleNotFoundException(roleName);
			}

			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
			Map<String, Object> roleMap = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
			@SuppressWarnings("unchecked")
			Collection<String> permissions = (Collection<String>) roleMap.get("permissions"); //$NON-NLS-1$
			EnumSet<Permission> rolePermissions = EnumSet.noneOf(Permission.class);
			for (String permission : permissions) {
				rolePermissions.add(Permission.valueOf(permission));
			}
			Role role = new Role(roleName, rolePermissions);
			return role;
		} finally {
			Util.closeQuietly(repo);
		}
	}

	/**
	 * Saves a user's authorities
	 *
	 * @param loginName the login name of the user whose authorities are to be saved
	 * @param authorities the user's authorities to be saved
	 * @param currentUser the user performing the save operation
	 *
	 * @throws UserNotFoundException when the user does not exist
	 */
	public void saveUserAuthorities(String loginName, Set<RoleGrantedAuthority> authorities, User currentUser)
			throws IOException {

		Assert.hasLength(loginName);
		Assert.notNull(authorities);
		Assert.notNull(currentUser);
		if (!loginName.equals(ANONYMOUS_USER_LOGIN_NAME)) {
			// check that user exists by trying to load it
			getUser(loginName);
		}

		ILockedRepository repo = null;
		try {
			repo = globalRepositoryManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			saveUserAuthorities(loginName, authorities, repo, currentUser, true);
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			Util.closeQuietly(repo);
		}
	}

	private void saveUserAuthorities(String loginName, Set<RoleGrantedAuthority> authorities,
			ILockedRepository repo, User currentUser, boolean commit) throws IOException, GitAPIException {

		Map<String, Set<String>> authoritiesMap = new HashMap<String, Set<String>>();
		for (RoleGrantedAuthority rga : authorities) {
			GrantedAuthorityTarget target = rga.getTarget();
			String targetStr = target.getType().name() + ":" + target.getTargetId(); //$NON-NLS-1$
			Set<String> roleNames = authoritiesMap.get(targetStr);
			if (roleNames == null) {
				roleNames = Sets.newHashSet();
				authoritiesMap.put(targetStr, roleNames);
			}
			roleNames.add(rga.getRoleName());
		}

		Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
		String json = gson.toJson(authoritiesMap);
		File workingDir = RepositoryUtil.getWorkingDir(repo.r());
		File workingFile = new File(workingDir, loginName + AUTHORITIES_SUFFIX);
		FileUtils.write(workingFile, json, Charsets.UTF_8);

		Git git = Git.wrap(repo.r());
		git.add().addFilepattern(loginName + AUTHORITIES_SUFFIX).call();
		if (commit) {
			PersonIdent ident = new PersonIdent(currentUser.getLoginName(), currentUser.getEmail());
			git.commit()
				.setAuthor(ident)
				.setCommitter(ident)
				.setMessage(loginName)
				.call();
		}
	}

	/**
	 * Returns a user's authorities.
	 *
	 * @param loginName the login name of the user
	 *
	 * @throws UserNotFoundException when the user does not exist
	 */
	public List<RoleGrantedAuthority> getUserAuthorities(String loginName) throws IOException {
		Assert.hasLength(loginName);

		ILockedRepository repo = null;
		try {
			repo = globalRepositoryManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			return getUserAuthorities(loginName, repo);
		} finally {
			Util.closeQuietly(repo);
		}
	}

	private List<RoleGrantedAuthority> getUserAuthorities(String loginName, ILockedRepository repo) {
		String json = BlobUtils.getHeadContent(repo.r(), loginName + AUTHORITIES_SUFFIX);
		if (json == null) {
			throw new UserNotFoundException(loginName);
		}

		Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
		Map<String, Set<String>> authoritiesMap = gson.fromJson(
				json, new TypeToken<Map<String, Set<String>>>(){}.getType());
		List<RoleGrantedAuthority> authorities = Lists.newArrayList();
		for (Map.Entry<String, Set<String>> entry : authoritiesMap.entrySet()) {
			String targetStr = entry.getKey();
			Type type = Type.valueOf(StringUtils.substringBefore(targetStr, ":")); //$NON-NLS-1$
			String targetId = StringUtils.substringAfter(targetStr, ":"); //$NON-NLS-1$
			for (String roleName : entry.getValue()) {
				authorities.add(new RoleGrantedAuthority(new GrantedAuthorityTarget(targetId, type), roleName));
			}
		}

		Collections.sort(authorities, new RoleGrantedAuthorityComparator());

		return authorities;
	}

	/**
	 * Returns the user that has an OpenID whose real ID is equal to the specified OpenID.
	 *
	 * @throws UserNotFoundException when the user could not be found
	 */
	public User getUserByOpenId(String openId) throws IOException {
		ILockedRepository repo = null;
		try {
			repo = globalRepositoryManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.isFile() && file.getName().endsWith(USER_SUFFIX);
				}
			};
			for (File file : workingDir.listFiles(filter)) {
				String loginName = StringUtils.substringBeforeLast(file.getName(), USER_SUFFIX);
				String json = FileUtils.readFileToString(file, Charsets.UTF_8);
				User user = getUser(loginName, json);
				for (OpenId id : user.getOpenIds()) {
					if (id.getRealId().equals(openId)) {
						return user;
					}
				}
			}

			throw new OpenIdNotFoundException(openId);
		} finally {
			Util.closeQuietly(repo);
		}
	}

	/**
	 * Converts a {@link RoleGrantedAuthority} to a set of {@link PermissionGrantedAuthority}.
	 * This method will return an empty set if the role specified by the RoleGrantedAuthority does not exist
	 * (rather than throwing a {@link RoleNotFoundException}.)
	 */
	Set<PermissionGrantedAuthority> toPermissionGrantedAuthorities(RoleGrantedAuthority rga) throws IOException {
		Set<PermissionGrantedAuthority> result = Sets.newHashSet();
		try {
			Role role = getRole(rga.getRoleName());
			GrantedAuthorityTarget target = rga.getTarget();
			for (Permission permission : role.getPermissions()) {
				result.add(new PermissionGrantedAuthority(target, permission));
			}
		} catch (RoleNotFoundException e) {
			// role might have been deleted
		}
		return result;
	}

	public void deleteUser(String loginName, User currentUser) throws IOException {
		Assert.hasLength(loginName);
		Assert.notNull(currentUser);

		ILockedRepository repo = null;
		try {
			repo = globalRepositoryManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			Git git = Git.wrap(repo.r());
			git.rm().addFilepattern(loginName + USER_SUFFIX).call();
			git.rm().addFilepattern(loginName + AUTHORITIES_SUFFIX).call();
			PersonIdent ident = new PersonIdent(currentUser.getLoginName(), currentUser.getEmail());
			git.commit()
				.setAuthor(ident)
				.setCommitter(ident)
				.setMessage("delete user " + loginName) //$NON-NLS-1$
				.call();
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			Util.closeQuietly(repo);
		}
	}

	public void renameUser(String loginName, String newLoginName, User currentUser) throws IOException {
		Assert.hasLength(loginName);
		Assert.hasLength(newLoginName);
		Assert.notNull(currentUser);
		// check that user exists by trying to load it
		getUser(loginName);
		// check that new user does not exist by trying to load it
		try {
			getUser(newLoginName);
			throw new IllegalArgumentException("user already exists: " + newLoginName); //$NON-NLS-1$
		} catch (UserNotFoundException e) {
			// okay
		}

		ILockedRepository repo = null;
		try {
			repo = globalRepositoryManager.getProjectCentralRepository(REPOSITORY_NAME, false);

			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			File file = new File(workingDir, loginName + USER_SUFFIX);
			File newFile = new File(workingDir, newLoginName + USER_SUFFIX);
			FileUtils.copyFile(file, newFile);
			file = new File(workingDir, loginName + AUTHORITIES_SUFFIX);
			newFile = new File(workingDir, newLoginName + AUTHORITIES_SUFFIX);
			FileUtils.copyFile(file, newFile);
			Git git = Git.wrap(repo.r());
			git.rm().addFilepattern(loginName + USER_SUFFIX).call();
			git.rm().addFilepattern(loginName + AUTHORITIES_SUFFIX).call();
			git.add().addFilepattern(newLoginName + USER_SUFFIX).call();
			git.add().addFilepattern(newLoginName + AUTHORITIES_SUFFIX).call();
			PersonIdent ident = new PersonIdent(currentUser.getLoginName(), currentUser.getEmail());
			git.commit()
				.setAuthor(ident)
				.setCommitter(ident)
				.setMessage("rename user " + loginName + " to " + newLoginName) //$NON-NLS-1$ //$NON-NLS-2$
				.call();
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			Util.closeQuietly(repo);
		}
	}

	public void renameRole(String roleName, String newRoleName, User currentUser) throws IOException {
		Assert.hasLength(roleName);
		Assert.hasLength(newRoleName);
		Assert.notNull(currentUser);
		// check that role exists by trying to load it
		getRole(roleName);
		// check that new role does not exist by trying to load it
		try {
			getRole(newRoleName);
			throw new IllegalArgumentException("role already exists: " + newRoleName); //$NON-NLS-1$
		} catch (RoleNotFoundException e) {
			// okay
		}

		log.info("renaming role: {} -> {}", roleName, newRoleName); //$NON-NLS-1$

		ILockedRepository repo = null;
		try {
			repo = globalRepositoryManager.getProjectCentralRepository(REPOSITORY_NAME, false);

			File workingDir = RepositoryUtil.getWorkingDir(repo.r());

			File file = new File(workingDir, roleName + ROLE_SUFFIX);
			File newFile = new File(workingDir, newRoleName + ROLE_SUFFIX);
			FileUtils.copyFile(file, newFile);
			Git git = Git.wrap(repo.r());
			git.rm().addFilepattern(roleName + ROLE_SUFFIX).call();
			git.add().addFilepattern(newRoleName + ROLE_SUFFIX).call();

			List<String> users = listUsers(repo);
			users.add(ANONYMOUS_USER_LOGIN_NAME);
			for (String user : users) {
				List<RoleGrantedAuthority> authorities = getUserAuthorities(user, repo);
				Set<RoleGrantedAuthority> newAuthorities = Sets.newHashSet();
				for (Iterator<RoleGrantedAuthority> iter = authorities.iterator(); iter.hasNext();) {
					RoleGrantedAuthority rga = iter.next();
					if (rga.getRoleName().equals(roleName)) {
						RoleGrantedAuthority newRga = new RoleGrantedAuthority(rga.getTarget(), newRoleName);
						newAuthorities.add(newRga);
						iter.remove();
					}
				}
				if (!newAuthorities.isEmpty()) {
					authorities.addAll(newAuthorities);
					saveUserAuthorities(user, Sets.newHashSet(authorities), repo, currentUser, false);
				}
			}

			PersonIdent ident = new PersonIdent(currentUser.getLoginName(), currentUser.getEmail());
			git.commit()
				.setAuthor(ident)
				.setCommitter(ident)
				.setMessage("rename role " + roleName + " to " + newRoleName) //$NON-NLS-1$ //$NON-NLS-2$
				.call();
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			Util.closeQuietly(repo);
		}
	}

	public void deleteRole(String roleName, User currentUser) throws IOException {
		Assert.hasLength(roleName);
		Assert.notNull(currentUser);
		// check that role exists by trying to load it
		getRole(roleName);

		ILockedRepository repo = null;
		try {
			repo = globalRepositoryManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			Git git = Git.wrap(repo.r());

			git.rm().addFilepattern(roleName + ROLE_SUFFIX).call();

			// remove role from all users
			List<String> users = listUsers(repo);
			users.add(ANONYMOUS_USER_LOGIN_NAME);
			for (String user : users) {
				List<RoleGrantedAuthority> authorities = getUserAuthorities(user, repo);
				boolean changed = false;
				for (Iterator<RoleGrantedAuthority> iter = authorities.iterator(); iter.hasNext();) {
					RoleGrantedAuthority rga = iter.next();
					if (rga.getRoleName().equals(roleName)) {
						iter.remove();
						changed = true;
					}
				}
				if (changed) {
					saveUserAuthorities(user, Sets.newHashSet(authorities), repo, currentUser, false);
				}
			}

			PersonIdent ident = new PersonIdent(currentUser.getLoginName(), currentUser.getEmail());
			git.commit()
				.setAuthor(ident)
				.setCommitter(ident)
				.setMessage("delete role " + roleName) //$NON-NLS-1$
				.call();
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			Util.closeQuietly(repo);
		}
	}

	@Subscribe
	public void renameProject(ProjectRenamedEvent event) {
		final String projectName = event.getProjectName();
		final String newProjectName = event.getNewProjectName();
		Function<RoleGrantedAuthority, RoleGrantedAuthority> function = new Function<RoleGrantedAuthority, RoleGrantedAuthority>() {
			@Override
			public RoleGrantedAuthority apply(RoleGrantedAuthority authority) {
				RoleGrantedAuthority newAuthority = renameProject(authority, projectName, newProjectName);
				return (newAuthority != null) ? newAuthority : authority;
			}
		};
		try {
			transformAllAuthorities(function, "rename project " + projectName + " to " + newProjectName, //$NON-NLS-1$ //$NON-NLS-2$
					event.getCurrentUser());
		} catch (IOException e) {
			log.error(StringUtils.EMPTY, e);
		} catch (GitAPIException e) {
			log.error(StringUtils.EMPTY, e);
		}
	}

	private void transformAllAuthorities(Function<RoleGrantedAuthority, RoleGrantedAuthority> function,
			String commitMessage, User currentUser) throws IOException, GitAPIException {

		ILockedRepository repo = null;
		try {
			List<String> users = listUsers();
			users.add(ANONYMOUS_USER_LOGIN_NAME);
			repo = globalRepositoryManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			boolean anyChanged = false;
			for (String loginName : users) {
				List<RoleGrantedAuthority> authorities = getUserAuthorities(loginName, repo);
				List<RoleGrantedAuthority> newAuthorities = Lists.newArrayList(Lists.transform(authorities, function));
				if (!newAuthorities.equals(authorities)) {
					saveUserAuthorities(loginName, Sets.newHashSet(newAuthorities), repo, currentUser, false);
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

	private RoleGrantedAuthority renameProject(RoleGrantedAuthority authority, String projectName, String newProjectName) {
		GrantedAuthorityTarget target = authority.getTarget();
		String targetId = target.getTargetId();
		GrantedAuthorityTarget newTarget = null;
		switch (target.getType()) {
			case PROJECT:
				{
					String projName = targetId;
					if (projName.equals(projectName)) {
						newTarget = new GrantedAuthorityTarget(newProjectName, GrantedAuthorityTarget.Type.PROJECT);
					}
				}
				break;

			case BRANCH:
				{
					String projName = StringUtils.substringBefore(targetId, "/"); //$NON-NLS-1$
					if (projName.equals(projectName)) {
						String branchName = StringUtils.substringAfter(targetId, "/"); //$NON-NLS-1$
						String newTargetId = newProjectName + "/" + branchName; //$NON-NLS-1$
						newTarget = new GrantedAuthorityTarget(newTargetId, GrantedAuthorityTarget.Type.BRANCH);
					}
				}
				break;
		}
		return (newTarget != null) ? new RoleGrantedAuthority(newTarget, authority.getRoleName()) : null;
	}

	@Subscribe
	public void deleteProject(ProjectDeletedEvent event) {
		final String projectName = event.getProjectName();
		Predicate<RoleGrantedAuthority> predicate = new Predicate<RoleGrantedAuthority>() {
			@Override
			public boolean apply(RoleGrantedAuthority authority) {
				GrantedAuthorityTarget target = authority.getTarget();
				String targetId = target.getTargetId();
				switch (target.getType()) {
					case PROJECT:
						{
							String projName = targetId;
							return !projName.equals(projectName);
						}

					case BRANCH:
						{
							String projName = StringUtils.substringBefore(targetId, "/"); //$NON-NLS-1$
							return !projName.equals(projectName);
						}
				}
				return true;
			}
		};
		try {
			deleteFromAllAuthorities(predicate, "delete project " + projectName, event.getCurrentUser()); //$NON-NLS-1$
		} catch (IOException e) {
			log.error(StringUtils.EMPTY, e);
		} catch (GitAPIException e) {
			log.error(StringUtils.EMPTY, e);
		}
	}

	private void deleteFromAllAuthorities(Predicate<RoleGrantedAuthority> predicate, String commitMessage, User currentUser)
			throws IOException, GitAPIException {

		ILockedRepository repo = null;
		try {
			List<String> users = listUsers();
			users.add(ANONYMOUS_USER_LOGIN_NAME);
			repo = globalRepositoryManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			boolean anyChanged = false;
			for (String loginName : users) {
				Set<RoleGrantedAuthority> authorities = Sets.newHashSet(getUserAuthorities(loginName, repo));
				Set<RoleGrantedAuthority> newAuthorities = Sets.newHashSet(Sets.filter(authorities, predicate));
				if (!newAuthorities.equals(authorities)) {
					saveUserAuthorities(loginName, newAuthorities, repo, currentUser, false);
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
		Predicate<RoleGrantedAuthority> predicate = new Predicate<RoleGrantedAuthority>() {
			@Override
			public boolean apply(RoleGrantedAuthority authority) {
				GrantedAuthorityTarget target = authority.getTarget();
				String targetId = target.getTargetId();
				switch (target.getType()) {
					case BRANCH:
						{
							String projName = StringUtils.substringBefore(targetId, "/"); //$NON-NLS-1$
							String braName = StringUtils.substringAfter(targetId, "/"); //$NON-NLS-1$
							return !projName.equals(projectName) || !braName.equals(branchName);
						}
				}
				return true;
			}
		};
		try {
			deleteFromAllAuthorities(predicate, "delete branch " + projectName + "/" + branchName, //$NON-NLS-1$ //$NON-NLS-2$
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
		Function<RoleGrantedAuthority, RoleGrantedAuthority> function = new Function<RoleGrantedAuthority, RoleGrantedAuthority>() {
			@Override
			public RoleGrantedAuthority apply(RoleGrantedAuthority authority) {
				RoleGrantedAuthority newAuthority = renameProjectBranch(authority, projectName, branchName, newBranchName);
				return (newAuthority != null) ? newAuthority : authority;
			}
		};
		try {
			transformAllAuthorities(function,
					"rename branch " + projectName + "/" + branchName + " to " + projectName + "/" + newBranchName, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					event.getCurrentUser());
		} catch (IOException e) {
			log.error(StringUtils.EMPTY, e);
		} catch (GitAPIException e) {
			log.error(StringUtils.EMPTY, e);
		}
	}

	private RoleGrantedAuthority renameProjectBranch(RoleGrantedAuthority authority, String projectName, String branchName,
			String newBranchName) {

		GrantedAuthorityTarget target = authority.getTarget();
		String targetId = target.getTargetId();
		GrantedAuthorityTarget newTarget = null;
		switch (target.getType()) {
			case BRANCH:
				{
					String projName = StringUtils.substringBefore(targetId, "/"); //$NON-NLS-1$
					String braName = StringUtils.substringAfter(targetId, "/"); //$NON-NLS-1$
					if (projName.equals(projectName) && braName.equals(branchName)) {
						String newTargetId = projectName + "/" + newBranchName; //$NON-NLS-1$
						newTarget = new GrantedAuthorityTarget(newTargetId, GrantedAuthorityTarget.Type.BRANCH);
					}
				}
				break;
		}
		return (newTarget != null) ? new RoleGrantedAuthority(newTarget, authority.getRoleName()) : null;
	}
}
