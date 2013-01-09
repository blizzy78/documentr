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

import static de.blizzy.documentr.TestUtil.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.Settings;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.ILockedRepository;
import de.blizzy.documentr.repository.LockManager;
import de.blizzy.documentr.repository.ProjectRepositoryManagerFactory;

public class SubscriptionStoreTest extends AbstractDocumentrTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String PAGE = "page"; //$NON-NLS-1$
	private static final String USER = "user"; //$NON-NLS-1$
	private static final String USER_2 = "user2"; //$NON-NLS-1$
	private static final String EMAIL = "email"; //$NON-NLS-1$
	private static final String EMAIL_2 = "email2"; //$NON-NLS-1$

	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

	@Mock
	private UserStore userStore;
	@Mock
	private EventBus eventBus;
	@Mock
	private User user;
	@Mock
	private User user2;
	@Mock
	private Settings settings;
	@Mock
	@SuppressWarnings("unused")
	private LockManager lockManager;
	@InjectMocks
	private SubscriptionStore subscriptionStore;
	@InjectMocks
	private ProjectRepositoryManagerFactory repoManagerFactory;
	private GlobalRepositoryManager globalRepoManager;

	@Before
	public void setUp() throws IOException {
		File dataDir = tempDir.getRoot();

		when(settings.getDocumentrDataDir()).thenReturn(dataDir);

		when(user.getLoginName()).thenReturn(USER);
		when(user.getEmail()).thenReturn(EMAIL);
		when(user2.getLoginName()).thenReturn(USER_2);
		when(user2.getEmail()).thenReturn(EMAIL_2);

		when(userStore.getUser(USER)).thenReturn(user);
		when(userStore.getUser(USER_2)).thenReturn(user2);

		globalRepoManager = new GlobalRepositoryManager();
		Whitebox.setInternalState(globalRepoManager, settings, repoManagerFactory, eventBus);
		globalRepoManager.init();

		Whitebox.setInternalState(subscriptionStore, globalRepoManager);
	}

	@Test
	public void subscribeAndIsSubscribed() throws IOException, GitAPIException {
		ILockedRepository repo = globalRepoManager.createProjectCentralRepository("_subscriptions", false, user); //$NON-NLS-1$
		register(repo);

		subscriptionStore.subscribe(PROJECT, BRANCH, PAGE, user);
		assertTrue(subscriptionStore.isSubscribed(PROJECT, BRANCH, PAGE, user));
		assertClean(repo.r());
	}

	@Test
	public void unsubscribe() throws IOException, GitAPIException {
		ILockedRepository repo = globalRepoManager.createProjectCentralRepository("_subscriptions", false, user); //$NON-NLS-1$
		register(repo);
		subscriptionStore.subscribe(PROJECT, BRANCH, PAGE, user);
		assertTrue(subscriptionStore.isSubscribed(PROJECT, BRANCH, PAGE, user));

		subscriptionStore.unsubscribe(PROJECT, BRANCH, PAGE, user);
		assertFalse(subscriptionStore.isSubscribed(PROJECT, BRANCH, PAGE, user));

		assertClean(repo.r());
	}

	@Test
	public void getSubscriberEmails() throws IOException, GitAPIException {
		ILockedRepository repo = globalRepoManager.createProjectCentralRepository("_subscriptions", false, user); //$NON-NLS-1$
		register(repo);
		subscriptionStore.subscribe(PROJECT, BRANCH, PAGE, user);
		subscriptionStore.subscribe(PROJECT, BRANCH, PAGE, user2);

		assertEquals(Sets.newHashSet(EMAIL, EMAIL_2), subscriptionStore.getSubscriberEmails(PROJECT, BRANCH, PAGE));
	}
}
