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
package de.blizzy.documentr.repository;

import static de.blizzy.documentr.DocumentrMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.Settings;
import de.blizzy.documentr.access.User;

public class GlobalRepositoryManagerTest extends AbstractDocumentrTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final User USER = new User("currentUser", "pw", "admin@example.com", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

	@Mock
	private Settings settings;
	@Mock
	private ProjectRepositoryManager repoManager;
	@Mock
	private ProjectRepositoryManagerFactory repoManagerFactory;
	@Mock
	private EventBus eventBus;
	@Mock
	private ILockedRepository repo;
	@Mock
	private LockManager lockManager;
	private File allReposDir;
	@InjectMocks
	private GlobalRepositoryManager globalRepoManager;

	@Before
	public void setUp() {
		File dataDir = new File("."); //$NON-NLS-1$
		allReposDir = new File(dataDir, "repositories"); //$NON-NLS-1$

		when(settings.getDocumentrDataDir()).thenReturn(dataDir);

		when(repoManagerFactory.getManager(allReposDir, PROJECT)).thenReturn(repoManager);

		globalRepoManager.init();
	}

	@Test
	public void createProjectCentralRepository() throws IOException, GitAPIException {
		when(repoManager.createCentralRepository(USER)).thenReturn(repo);
		assertSame(repo, globalRepoManager.createProjectCentralRepository(PROJECT, USER));
	}

	@Test
	public void getProjectCentralRepository() throws IOException {
		when(repoManager.getCentralRepository()).thenReturn(repo);
		assertSame(repo, globalRepoManager.getProjectCentralRepository(PROJECT));
	}

	@Test
	public void createProjectBranchRepositoryWithoutStartingBranch() throws IOException, GitAPIException {
		when(repoManager.createBranchRepository("branch", null)).thenReturn(repo); //$NON-NLS-1$
		assertSame(repo, globalRepoManager.createProjectBranchRepository(PROJECT, "branch", null)); //$NON-NLS-1$
	}

	@Test
	public void createProjectBranchRepository() throws IOException, GitAPIException {
		when(repoManager.createBranchRepository("branch", "startingBranch")).thenReturn(repo); //$NON-NLS-1$ //$NON-NLS-2$
		assertSame(repo, globalRepoManager.createProjectBranchRepository(PROJECT, "branch", "startingBranch")); //$NON-NLS-1$ //$NON-NLS-2$

		verify(eventBus).post(eqReflection(new BranchCreatedEvent(PROJECT, "branch"))); //$NON-NLS-1$
	}

	@Test
	public void listProjectBranches() throws IOException {
		List<String> branches = Lists.newArrayList("branch1", "branch2", "branch3"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(repoManager.listBranches()).thenReturn(branches);
		assertEquals(branches, globalRepoManager.listProjectBranches(PROJECT));
	}

	@Test
	public void listProjects() throws IOException, GitAPIException {
		File dataDir = tempDir.getRoot();
		when(settings.getDocumentrDataDir()).thenReturn(dataDir);
		ProjectRepositoryManagerFactory repoManagerFactory = new ProjectRepositoryManagerFactory();
		Whitebox.setInternalState(repoManagerFactory, lockManager);
		GlobalRepositoryManager globalRepoManager = new GlobalRepositoryManager();
		Whitebox.setInternalState(globalRepoManager, settings, repoManagerFactory);
		globalRepoManager.init();
		globalRepoManager.createProjectCentralRepository("project1", USER); //$NON-NLS-1$
		globalRepoManager.createProjectCentralRepository("project2", USER); //$NON-NLS-1$

		List<String> projects = globalRepoManager.listProjects();
		assertEquals(2, projects.size());
		assertTrue(projects.contains("project1")); //$NON-NLS-1$
		assertTrue(projects.contains("project2")); //$NON-NLS-1$
	}
}
