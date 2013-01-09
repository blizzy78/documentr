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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

import com.google.common.collect.Sets;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.Settings;
import de.blizzy.documentr.access.GrantedAuthorityTarget.Type;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.LockManager;
import de.blizzy.documentr.repository.ProjectRepositoryManagerFactory;

public class UserStoreTest extends AbstractDocumentrTest {
	private static final User USER = new User("currentUser", "pw", "admin@example.com", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Mock
	private Settings settings;
	@Mock
	@SuppressWarnings("unused")
	private LockManager lockManager;
	private UserStore userStore;
	@InjectMocks
	private ShaPasswordEncoder passwordEncoder;
	@InjectMocks
	private ProjectRepositoryManagerFactory repoManagerFactory;

	@Before
	public void setUp() throws IOException, GitAPIException {
		File dataDir = tempDir.getRoot();

		when(settings.getDocumentrDataDir()).thenReturn(dataDir);

		GlobalRepositoryManager globalRepoManager = new GlobalRepositoryManager();
		Whitebox.setInternalState(globalRepoManager, settings, repoManagerFactory);
		globalRepoManager.init();

		userStore = new UserStore();
		Whitebox.setInternalState(userStore, globalRepoManager, passwordEncoder);
		userStore.init();
	}

	@Test
	public void createInitialAdmin() throws IOException {
		User user = userStore.getUser("admin"); //$NON-NLS-1$
		assertEquals("admin", user.getLoginName()); //$NON-NLS-1$
		String passwordHash = passwordEncoder.encodePassword("admin", "admin"); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals(passwordHash, user.getPassword());
		assertFalse(user.isDisabled());
	}

	@Test
	public void createInitialRoles() throws IOException {
		Role role = userStore.getRole("Administrator"); //$NON-NLS-1$
		assertEquals("Administrator", role.getName()); //$NON-NLS-1$
		assertEquals(EnumSet.of(Permission.ADMIN), role.getPermissions());

		role = userStore.getRole("Editor"); //$NON-NLS-1$
		assertEquals("Editor", role.getName()); //$NON-NLS-1$
		assertEquals(EnumSet.of(Permission.EDIT_BRANCH, Permission.EDIT_PAGE), role.getPermissions());

		role = userStore.getRole("Reader"); //$NON-NLS-1$
		assertEquals("Reader", role.getName()); //$NON-NLS-1$
		assertEquals(EnumSet.of(Permission.VIEW), role.getPermissions());

		List<RoleGrantedAuthority> authorities = userStore.getUserAuthorities("admin"); //$NON-NLS-1$
		assertEquals(Collections.singletonList(
				new RoleGrantedAuthority(GrantedAuthorityTarget.APPLICATION, "Administrator")), //$NON-NLS-1$
				authorities);

		authorities = userStore.getUserAuthorities(UserStore.ANONYMOUS_USER_LOGIN_NAME);
		assertEquals(Collections.singletonList(
				new RoleGrantedAuthority(GrantedAuthorityTarget.APPLICATION, "Reader")), //$NON-NLS-1$
				authorities);
	}

	@Test
	public void saveAndGetUser() throws IOException {
		User user = new User("user", "p", "email", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		user.addOpenId(new OpenId("openId1", "realOpenId1")); //$NON-NLS-1$ //$NON-NLS-2$
		user.addOpenId(new OpenId("openId2", "realOpenId2")); //$NON-NLS-1$ //$NON-NLS-2$
		userStore.saveUser(user, USER);
		User result = userStore.getUser("user"); //$NON-NLS-1$
		assertEquals(user.getLoginName(), result.getLoginName());
		assertEquals(user.getPassword(), result.getPassword());
		assertEquals(user.isDisabled(), result.isDisabled());
		assertEquals(user.getOpenIds(), result.getOpenIds());
	}

	@Test
	public void deleteUserMustDeleteUser() throws IOException {
		User user = new User("user", "p", "email", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		userStore.saveUser(user, USER);

		RoleGrantedAuthority rga = new RoleGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, "Reader"); //$NON-NLS-1$
		userStore.saveUserAuthorities("user", Sets.newHashSet(rga), USER); //$NON-NLS-1$

		userStore.deleteUser("user", USER); //$NON-NLS-1$

		expectedException.expect(UserNotFoundException.class);
		userStore.getUser("user"); //$NON-NLS-1$
	}

	@Test
	public void deleteUserMustDeleteUserAuthorities() throws IOException {
		User user = new User("user", "p", "email", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		userStore.saveUser(user, USER);

		RoleGrantedAuthority rga = new RoleGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, "Reader"); //$NON-NLS-1$
		userStore.saveUserAuthorities("user", Sets.newHashSet(rga), USER); //$NON-NLS-1$

		userStore.deleteUser("user", USER); //$NON-NLS-1$

		expectedException.expect(UserNotFoundException.class);
		userStore.getUserAuthorities("user"); //$NON-NLS-1$
	}

	@Test
	public void renameUserMustRenameUser() throws IOException {
		User user = new User("user", "p", "email", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		userStore.saveUser(user, USER);

		RoleGrantedAuthority rga = new RoleGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, "Reader"); //$NON-NLS-1$
		userStore.saveUserAuthorities("user", Sets.newHashSet(rga), USER); //$NON-NLS-1$

		userStore.renameUser("user", "user2", USER); //$NON-NLS-1$ //$NON-NLS-2$
		assertNotNull(userStore.getUser("user2")); //$NON-NLS-1$

		expectedException.expect(UserNotFoundException.class);
		userStore.getUser("user"); //$NON-NLS-1$
	}

	@Test
	public void renameUserMustRenameUserAuthorities() throws IOException {
		User user = new User("user", "p", "email", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		userStore.saveUser(user, USER);

		RoleGrantedAuthority rga = new RoleGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, "Reader"); //$NON-NLS-1$
		userStore.saveUserAuthorities("user", Sets.newHashSet(rga), USER); //$NON-NLS-1$

		userStore.renameUser("user", "user2", USER); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse(userStore.getUserAuthorities("user2").isEmpty()); //$NON-NLS-1$

		expectedException.expect(UserNotFoundException.class);
		userStore.getUserAuthorities("user"); //$NON-NLS-1$
	}

	@Test
	@Ignore
	public void renameRoleMustRenameRole() {
		// TODO: implement test
	}

	@Test
	@Ignore
	public void renameRoleMustRenameRoleInUserAuthorities() {
		// TODO: implement test
	}

	@Test
	public void getUserByOpenId() throws IOException {
		User user = new User("user", "p", "email", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		user.addOpenId(new OpenId("openId1", "realOpenId1")); //$NON-NLS-1$ //$NON-NLS-2$
		user.addOpenId(new OpenId("openId2", "realOpenId2")); //$NON-NLS-1$ //$NON-NLS-2$
		userStore.saveUser(user, USER);
		User user2 = new User("user2", "p", "email", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		user2.addOpenId(new OpenId("openId3", "realOpenId3")); //$NON-NLS-1$ //$NON-NLS-2$
		user2.addOpenId(new OpenId("openId4", "realOpenId4")); //$NON-NLS-1$ //$NON-NLS-2$
		userStore.saveUser(user2, USER);

		User result = userStore.getUserByOpenId("realOpenId1"); //$NON-NLS-1$
		assertEquals(user.getLoginName(), result.getLoginName());
		result = userStore.getUserByOpenId("realOpenId4"); //$NON-NLS-1$
		assertEquals(user2.getLoginName(), result.getLoginName());
	}

	@Test
	public void listUsers() throws IOException {
		userStore.saveUser(new User("u1", "pw", "email", false), USER); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		userStore.saveUser(new User("u2", "pw", "email", false), USER); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		userStore.saveUser(new User("u3", "pw", "email", false), USER); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		List<String> result = userStore.listUsers();
		assertTrue(result.contains("u1")); //$NON-NLS-1$
		assertTrue(result.contains("u2")); //$NON-NLS-1$
		assertTrue(result.contains("u3")); //$NON-NLS-1$
	}

	@Test
	public void listRoles() throws IOException {
		userStore.saveRole(new Role("r1", EnumSet.of(Permission.VIEW)), USER); //$NON-NLS-1$
		userStore.saveRole(new Role("r2", EnumSet.of(Permission.VIEW)), USER); //$NON-NLS-1$
		userStore.saveRole(new Role("r3", EnumSet.of(Permission.VIEW)), USER); //$NON-NLS-1$
		List<String> result = userStore.listRoles();
		assertTrue(result.contains("r1")); //$NON-NLS-1$
		assertTrue(result.contains("r2")); //$NON-NLS-1$
		assertTrue(result.contains("r3")); //$NON-NLS-1$
	}

	@Test
	public void saveAndGetRole() throws IOException {
		Role role = new Role("role", EnumSet.of(Permission.VIEW)); //$NON-NLS-1$
		userStore.saveRole(role, USER);
		Role result = userStore.getRole("role"); //$NON-NLS-1$
		assertEquals(result.getName(), role.getName());
		assertEquals(result.getPermissions(), role.getPermissions());
	}

	@Test
	public void deleteRoleMustDeleteRole() throws IOException {
		Role role = new Role("role", EnumSet.of(Permission.VIEW)); //$NON-NLS-1$
		userStore.saveRole(role, USER);

		userStore.deleteRole("role", USER); //$NON-NLS-1$

		expectedException.expect(RoleNotFoundException.class);
		userStore.getRole("role"); //$NON-NLS-1$
	}

	@Test
	public void deleteRoleMustRemoveRoleFromUsers() throws IOException {
		Role role = new Role("role", EnumSet.of(Permission.VIEW)); //$NON-NLS-1$
		userStore.saveRole(role, USER);
		User user = new User("user", "p", "email", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		userStore.saveUser(user, USER);
		RoleGrantedAuthority rga1 = new RoleGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, "Reader"); //$NON-NLS-1$
		RoleGrantedAuthority rga2 = new RoleGrantedAuthority(
				new GrantedAuthorityTarget("project", Type.PROJECT), "role"); //$NON-NLS-1$ //$NON-NLS-2$
		userStore.saveUserAuthorities("user", Sets.newHashSet(rga1, rga2), USER); //$NON-NLS-1$

		userStore.deleteRole("role", USER); //$NON-NLS-1$
		assertEquals(1, userStore.getUserAuthorities("user").size()); //$NON-NLS-1$
	}

	@Test
	public void saveAndGetUserAuthorities() throws IOException {
		User user = new User("user", "p", "email", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		userStore.saveUser(user, USER);

		RoleGrantedAuthority rga1 = new RoleGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, "Reader"); //$NON-NLS-1$
		RoleGrantedAuthority rga2 = new RoleGrantedAuthority(
				new GrantedAuthorityTarget("project", Type.PROJECT), "Administrator"); //$NON-NLS-1$ //$NON-NLS-2$
		userStore.saveUserAuthorities("user", Sets.newHashSet(rga1, rga2), USER); //$NON-NLS-1$
		List<RoleGrantedAuthority> result = userStore.getUserAuthorities("user"); //$NON-NLS-1$
		assertEquals(Sets.newHashSet(rga1, rga2), Sets.newHashSet(result));
	}

	@Test
	public void toPermissionGrantedAuthorities() throws IOException {
		Role role = new Role("role", EnumSet.of(Permission.EDIT_BRANCH, Permission.EDIT_PAGE)); //$NON-NLS-1$
		userStore.saveRole(role, USER);

		RoleGrantedAuthority rga = new RoleGrantedAuthority(
				new GrantedAuthorityTarget("project", Type.PROJECT), "role"); //$NON-NLS-1$ //$NON-NLS-2$
		Set<PermissionGrantedAuthority> result = userStore.toPermissionGrantedAuthorities(rga);
		Set<PermissionGrantedAuthority> expected = Sets.newHashSet(
				new PermissionGrantedAuthority(rga.getTarget(), Permission.EDIT_BRANCH),
				new PermissionGrantedAuthority(rga.getTarget(), Permission.EDIT_PAGE));
		assertEquals(expected, result);
	}
}
