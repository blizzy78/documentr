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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.Settings;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.LockManager;
import de.blizzy.documentr.repository.ProjectRepositoryManagerFactory;

public class UserStoreTest extends AbstractDocumentrTest {
	private static final User USER = new User("currentUser", "pw", "admin@example.com", false, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	private UserStore userStore;
	private PasswordEncoder passwordEncoder;

	@Before
	public void setUp() throws IOException, GitAPIException {
		File dataDir = createTempDir();
		Settings settings = new Settings();
		settings.setDocumentrDataDir(dataDir);

		GlobalRepositoryManager globalRepoManager = new GlobalRepositoryManager();
		globalRepoManager.setSettings(settings);
		ProjectRepositoryManagerFactory repoManagerFactory = new ProjectRepositoryManagerFactory();
		repoManagerFactory.setLockManager(mock(LockManager.class));
		globalRepoManager.setRepositoryManagerFactory(repoManagerFactory);
		globalRepoManager.init();

		userStore = new UserStore();
		userStore.setGlobalRepositoryManager(globalRepoManager);
		passwordEncoder = new ShaPasswordEncoder();
		userStore.setPasswordEncoder(passwordEncoder);
		userStore.init();
	}
	
	@Test
	public void createInitialAdmin() throws IOException {
		User user = userStore.getUser("admin"); //$NON-NLS-1$
		assertEquals("admin", user.getLoginName()); //$NON-NLS-1$
		String passwordHash = passwordEncoder.encodePassword("admin", "admin"); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals(passwordHash, user.getPassword());
		assertFalse(user.isDisabled());
		assertTrue(user.isAdmin());
	}

	@Test
	@SuppressWarnings("boxing")
	public void saveAndGetUser() throws IOException {
		User user = new User("user", "p", "email", true, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		userStore.saveUser(user, USER);
		User result = userStore.getUser("user"); //$NON-NLS-1$
		assertEquals(user.getLoginName(), result.getLoginName());
		assertEquals(user.getPassword(), result.getPassword());
		assertEquals(user.isDisabled(), result.isDisabled());
		assertEquals(user.isAdmin(), result.isAdmin());
	}
	
	@Test
	public void listUsers() throws IOException {
		userStore.saveUser(new User("u1", "pw", "email", false, false), USER); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		userStore.saveUser(new User("u2", "pw", "email", false, false), USER); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		userStore.saveUser(new User("u3", "pw", "email", false, false), USER); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		Set<String> expected = new HashSet<String>(Arrays.asList("admin", "u1", "u2", "u3")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		Set<String> result = new HashSet<String>(userStore.listUsers());
		assertEquals(expected, result);
	}
}
