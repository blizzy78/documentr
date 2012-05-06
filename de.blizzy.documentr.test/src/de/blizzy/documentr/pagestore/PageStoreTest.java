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
package de.blizzy.documentr.pagestore;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import de.blizzy.documentr.Settings;
import de.blizzy.documentr.TestUtil;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.LockManager;
import de.blizzy.documentr.repository.ProjectRepositoryManagerFactory;

public class PageStoreTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH_1 = "branch_1"; //$NON-NLS-1$
	private static final String BRANCH_2 = "branch_2"; //$NON-NLS-1$
	private static final String BRANCH_3 = "branch_3"; //$NON-NLS-1$
	private static final String PAGE = "page"; //$NON-NLS-1$
	
	private GlobalRepositoryManager globalRepoManager;
	private PageStore pageStore;

	@Before
	public void setUp() {
		File dataDir = TestUtil.createTempDir();
		Settings settings = new Settings();
		settings.setDocumentrDataDir(dataDir);

		globalRepoManager = new GlobalRepositoryManager();
		globalRepoManager.setSettings(settings);
		ProjectRepositoryManagerFactory repoManagerFactory = new ProjectRepositoryManagerFactory();
		repoManagerFactory.setLockManager(mock(LockManager.class));
		globalRepoManager.setRepositoryManagerFactory(repoManagerFactory);
		globalRepoManager.init();
		
		pageStore = new PageStore();
		pageStore.setGlobalRepositoryManager(globalRepoManager);
	}
	
	@Test
	public void saveAndGetPage() throws IOException, GitAPIException {
		globalRepoManager.createProjectCentralRepository(PROJECT);
		globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null);
		Page page = saveRandomPage(BRANCH_1, "foo/bar/baz"); //$NON-NLS-1$
		Page result = pageStore.getPage(PROJECT, BRANCH_1, "foo/bar/baz"); //$NON-NLS-1$
		assertEquals(page.getTitle(), result.getTitle());
		assertEquals(page.getText(), result.getText());
		assertEquals(page.getContentType(), result.getContentType());
	}
	
	@Test
	public void listPagePaths() throws IOException, GitAPIException {
		globalRepoManager.createProjectCentralRepository(PROJECT);
		globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null);
		saveRandomPage(BRANCH_1, "test"); //$NON-NLS-1$
		saveRandomPage(BRANCH_1, "foo/bar/baz"); //$NON-NLS-1$
		List<String> paths = pageStore.listPagePaths(PROJECT, BRANCH_1);
		assertEquals(2, paths.size());
		assertTrue(paths.contains("test")); //$NON-NLS-1$
		assertTrue(paths.contains("foo/bar/baz")); //$NON-NLS-1$
	}
	
	@Test
	public void isPageSharedWithOtherBranches() throws IOException, GitAPIException {
		globalRepoManager.createProjectCentralRepository(PROJECT);
		globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null);
		saveRandomPage(BRANCH_1, PAGE);
		
		assertFalse(isPageSharedWithOtherBranches(BRANCH_1));

		globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_2, BRANCH_1);
		globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_3, BRANCH_1);
		assertTrue(isPageSharedWithOtherBranches(BRANCH_1));
		assertTrue(isPageSharedWithOtherBranches(BRANCH_2));
		assertTrue(isPageSharedWithOtherBranches(BRANCH_3));
		
		saveRandomPage(BRANCH_1, "page_2"); //$NON-NLS-1$
		saveRandomPage(BRANCH_2, "page_3"); //$NON-NLS-1$
		saveRandomPage(BRANCH_3, "page_4"); //$NON-NLS-1$
		assertTrue(isPageSharedWithOtherBranches(BRANCH_1));
		assertTrue(isPageSharedWithOtherBranches(BRANCH_2));
		assertTrue(isPageSharedWithOtherBranches(BRANCH_3));

		saveRandomPage(BRANCH_1, PAGE);
		assertFalse(isPageSharedWithOtherBranches(BRANCH_1));
		assertTrue(isPageSharedWithOtherBranches(BRANCH_2));
		assertTrue(isPageSharedWithOtherBranches(BRANCH_3));
		
		saveRandomPage(BRANCH_2, PAGE);
		assertFalse(isPageSharedWithOtherBranches(BRANCH_1));
		assertFalse(isPageSharedWithOtherBranches(BRANCH_2));
		assertFalse(isPageSharedWithOtherBranches(BRANCH_3));
	}
	
	private boolean isPageSharedWithOtherBranches(String branchName) throws IOException {
		return pageStore.isPageSharedWithOtherBranches(PROJECT, branchName, PAGE);
	}
	
	@Test
	public void getBranchesPageIsSharedWith() throws IOException, GitAPIException {
		globalRepoManager.createProjectCentralRepository(PROJECT);
		globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null);
		saveRandomPage(BRANCH_1, PAGE);

		assertBranchesPageIsSharedWith(BRANCH_1, BRANCH_1);
		
		globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_2, BRANCH_1);
		globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_3, BRANCH_1);
		assertBranchesPageIsSharedWith(BRANCH_1, BRANCH_1, BRANCH_2, BRANCH_3);
		assertBranchesPageIsSharedWith(BRANCH_2, BRANCH_1, BRANCH_2, BRANCH_3);
		assertBranchesPageIsSharedWith(BRANCH_3, BRANCH_1, BRANCH_2, BRANCH_3);
		
		saveRandomPage(BRANCH_1, "page_2"); //$NON-NLS-1$
		saveRandomPage(BRANCH_2, "page_3"); //$NON-NLS-1$
		saveRandomPage(BRANCH_3, "page_4"); //$NON-NLS-1$
		assertBranchesPageIsSharedWith(BRANCH_1, BRANCH_1, BRANCH_2, BRANCH_3);
		assertBranchesPageIsSharedWith(BRANCH_2, BRANCH_1, BRANCH_2, BRANCH_3);
		assertBranchesPageIsSharedWith(BRANCH_3, BRANCH_1, BRANCH_2, BRANCH_3);
		
		saveRandomPage(BRANCH_1, PAGE);
		assertBranchesPageIsSharedWith(BRANCH_1, BRANCH_1);
		assertBranchesPageIsSharedWith(BRANCH_2, BRANCH_2, BRANCH_3);
		assertBranchesPageIsSharedWith(BRANCH_3, BRANCH_2, BRANCH_3);
		
		saveRandomPage(BRANCH_2, PAGE);
		assertBranchesPageIsSharedWith(BRANCH_1, BRANCH_1);
		assertBranchesPageIsSharedWith(BRANCH_2, BRANCH_2);
		assertBranchesPageIsSharedWith(BRANCH_3, BRANCH_3);
	}

	@Test
	public void saveAndGetAttachment() throws IOException, GitAPIException {
		globalRepoManager.createProjectCentralRepository(PROJECT);
		globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null);
		saveRandomPage(BRANCH_1, "foo/bar/baz"); //$NON-NLS-1$
		Page attachment = Page.fromData(new byte[] { 1, 2, 3 }, "application/octet-stream"); //$NON-NLS-1$
		pageStore.saveAttachment(PROJECT, BRANCH_1, "foo/bar/baz", "test.dat", attachment); //$NON-NLS-1$ //$NON-NLS-2$
		
		Page result = pageStore.getAttachment(PROJECT, BRANCH_1, "foo/bar/baz", "test.dat"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(ArrayUtils.isEquals(attachment.getData(), result.getData()));
		assertEquals(attachment.getContentType(), result.getContentType());
	}

	@Test
	public void listPageAttachments() throws IOException, GitAPIException {
		globalRepoManager.createProjectCentralRepository(PROJECT);
		globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null);
		saveRandomPage(BRANCH_1, "foo/bar/baz"); //$NON-NLS-1$
		Page attachment = Page.fromData(new byte[] { 1, 2, 3 }, "application/octet-stream"); //$NON-NLS-1$
		pageStore.saveAttachment(PROJECT, BRANCH_1, "foo/bar/baz", "test.dat", attachment); //$NON-NLS-1$ //$NON-NLS-2$
		List<String> attachments = pageStore.listPageAttachments(PROJECT, BRANCH_1, "foo/bar/baz"); //$NON-NLS-1$
		assertEquals(1, attachments.size());
		assertTrue(attachments.contains("test.dat")); //$NON-NLS-1$
	}
	
	private void assertBranchesPageIsSharedWith(String branchName, String... expectedBranches)
			throws IOException {
		
		List<String> branches = pageStore.getBranchesPageIsSharedWith(PROJECT, branchName, PAGE);
		assertEquals(expectedBranches.length, branches.size());
		assertEquals(Sets.newHashSet(expectedBranches), new HashSet<>(branches));
	}
	
	private Page saveRandomPage(String branchName, String path) throws IOException {
		Page page = createRandomPage();
		pageStore.savePage(PROJECT, branchName, path, page);
		return page;
	}
	
	private Page createRandomPage() {
		return Page.fromText(String.valueOf(Math.random() * Long.MAX_VALUE), String.valueOf(Math.random() * Long.MAX_VALUE));
	}
}
