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

import static de.blizzy.documentr.TestUtil.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.Settings;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.ILockedRepository;
import de.blizzy.documentr.repository.LockManager;
import de.blizzy.documentr.repository.ProjectRepositoryManagerFactory;
import de.blizzy.documentr.repository.RepositoryUtil;

public class PageStoreTest extends AbstractDocumentrTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH_1 = "branch_1"; //$NON-NLS-1$
	private static final String BRANCH_2 = "branch_2"; //$NON-NLS-1$
	private static final String BRANCH_3 = "branch_3"; //$NON-NLS-1$
	private static final String PAGE = "page"; //$NON-NLS-1$
	private static final User USER = new User("currentUser", "pw", "admin@example.com", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	private GlobalRepositoryManager globalRepoManager;
	private PageStore pageStore;

	@Before
	public void setUp() {
		File dataDir = createTempDir();
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
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		Page page = saveRandomPage(BRANCH_1, "foo"); //$NON-NLS-1$
		Page result = pageStore.getPage(PROJECT, BRANCH_1, "foo", true); //$NON-NLS-1$
		assertEquals(page.getTitle(), result.getTitle());
		assertEquals(((PageTextData) page.getData()).getText(), ((PageTextData) result.getData()).getText());
		assertEquals(page.getContentType(), result.getContentType());
		assertEquals(page.getParentPagePath(), result.getParentPagePath());
	}
	
	@Test
	public void saveAndGetPageWithParentPagePath() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		Page page = saveRandomPage(BRANCH_1, "foo/bar", "parent"); //$NON-NLS-1$ //$NON-NLS-2$
		Page result = pageStore.getPage(PROJECT, BRANCH_1, "foo/bar", true); //$NON-NLS-1$
		assertEquals(page.getTitle(), result.getTitle());
		assertEquals(((PageTextData) page.getData()).getText(), ((PageTextData) result.getData()).getText());
		assertEquals(page.getContentType(), result.getContentType());
		assertEquals(page.getParentPagePath(), result.getParentPagePath());
	}
	
	@Test
	public void getPageWithoutData() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		saveRandomPage(BRANCH_1, "foo"); //$NON-NLS-1$
		Page result = pageStore.getPage(PROJECT, BRANCH_1, "foo", false); //$NON-NLS-1$
		assertNull(result.getData());
	}

	@Test
	public void listPagePaths() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		saveRandomPage(BRANCH_1, "test"); //$NON-NLS-1$
		saveRandomPage(BRANCH_1, "foo/bar/baz"); //$NON-NLS-1$
		List<String> paths = pageStore.listPagePaths(PROJECT, BRANCH_1);
		assertEquals(2, paths.size());
		assertTrue(paths.contains("test")); //$NON-NLS-1$
		assertTrue(paths.contains("foo/bar/baz")); //$NON-NLS-1$
	}
	
	@Test
	public void isPageSharedWithOtherBranches() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		saveRandomPage(BRANCH_1, PAGE);
		
		assertFalse(isPageSharedWithOtherBranches(BRANCH_1));

		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_2, BRANCH_1));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_3, BRANCH_1));
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
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		saveRandomPage(BRANCH_1, PAGE);

		assertBranchesPageIsSharedWith(BRANCH_1, BRANCH_1);
		
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_2, BRANCH_1));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_3, BRANCH_1));
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
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		saveRandomPage(BRANCH_1, "foo/bar/baz"); //$NON-NLS-1$
		Page attachment = Page.fromData(null, new byte[] { 1, 2, 3 }, "application/octet-stream"); //$NON-NLS-1$
		pageStore.saveAttachment(PROJECT, BRANCH_1, "foo/bar/baz", "test.dat", attachment, USER); //$NON-NLS-1$ //$NON-NLS-2$
		
		Page result = pageStore.getAttachment(PROJECT, BRANCH_1, "foo/bar/baz", "test.dat"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(ArrayUtils.isEquals(attachment.getData(), result.getData()));
		assertEquals(attachment.getContentType(), result.getContentType());
	}

	@Test
	public void listPageAttachments() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		saveRandomPage(BRANCH_1, "foo/bar/baz"); //$NON-NLS-1$
		saveRandomPage(BRANCH_1, "foo/bar/baz/qux"); //$NON-NLS-1$
		Page attachment = Page.fromData(null, new byte[] { 1, 2, 3 }, "application/octet-stream"); //$NON-NLS-1$
		pageStore.saveAttachment(PROJECT, BRANCH_1, "foo/bar/baz", "test.dat", attachment, USER); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.saveAttachment(PROJECT, BRANCH_1, "foo/bar/baz/qux", "test2.dat", attachment, USER); //$NON-NLS-1$ //$NON-NLS-2$

		List<String> attachments = pageStore.listPageAttachments(PROJECT, BRANCH_1, "foo/bar/baz"); //$NON-NLS-1$
		assertEquals(1, attachments.size());
		assertTrue(attachments.contains("test.dat")); //$NON-NLS-1$
		attachments = pageStore.listPageAttachments(PROJECT, BRANCH_1, "foo/bar/baz/qux"); //$NON-NLS-1$
		assertEquals(1, attachments.size());
		assertTrue(attachments.contains("test2.dat")); //$NON-NLS-1$
	}

	@Test
	public void listChildPagePaths() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		saveRandomPage(BRANCH_1, "home/foo"); //$NON-NLS-1$
		saveRandomPage(BRANCH_1, "home/foo/bar"); //$NON-NLS-1$
		saveRandomPage(BRANCH_1, "home/foo/bar/baz"); //$NON-NLS-1$
		saveRandomPage(BRANCH_1, "home/foo/qux"); //$NON-NLS-1$
		
		Set<String> expected = new HashSet<String>(
				Arrays.asList("home/foo/bar", "home/foo/qux")); //$NON-NLS-1$ //$NON-NLS-2$
		Set<String> result = new HashSet<String>(pageStore.listChildPagePaths(PROJECT, BRANCH_1, "home/foo")); //$NON-NLS-1$
		assertEquals(expected, result);
		expected = Collections.singleton("home/foo/bar/baz"); //$NON-NLS-1$
		result = new HashSet<String>(pageStore.listChildPagePaths(PROJECT, BRANCH_1, "home/foo/bar")); //$NON-NLS-1$
		assertEquals(expected, result);
	}
	
	@Test
	public void deletePage() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		ILockedRepository repo = globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null);
		register(repo);
		saveRandomPage(BRANCH_1, "foo"); //$NON-NLS-1$
		File pageFile = new File(new File(RepositoryUtil.getWorkingDir(repo.r()), "pages"), "foo.page"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(pageFile.isFile());
		File metaFile = new File(new File(RepositoryUtil.getWorkingDir(repo.r()), "pages"), "foo.meta"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(metaFile.isFile());
		
		pageStore.deletePage(PROJECT, BRANCH_1, "foo", USER); //$NON-NLS-1$
		List<String> result = pageStore.listPagePaths(PROJECT, BRANCH_1);
		assertEquals(Collections.emptySet(), new HashSet<String>(result));
		assertFalse(pageFile.isFile());
		assertFalse(metaFile.isFile());
	}
	
	@Test
	public void getPageMetadata() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		saveRandomPage(BRANCH_1, PAGE);
		PageMetadata metadata = pageStore.getPageMetadata(PROJECT, BRANCH_1, PAGE);
		assertEquals(USER.getLoginName(), metadata.getLastEditedBy());
		assertSecondsAgo(metadata.getLastEdited(), 5);
	}

	private void assertBranchesPageIsSharedWith(String branchName, String... expectedBranches)
			throws IOException {
		
		List<String> branches = pageStore.getBranchesPageIsSharedWith(PROJECT, branchName, PAGE);
		assertEquals(expectedBranches.length, branches.size());
		assertEquals(Sets.newHashSet(expectedBranches), new HashSet<String>(branches));
	}
	
	private Page saveRandomPage(String branchName, String path) throws IOException {
		return saveRandomPage(branchName, path, null);
	}
	
	private Page saveRandomPage(String branchName, String path, String parentPagePath) throws IOException {
		Page page = createRandomPage(parentPagePath);
		pageStore.savePage(PROJECT, branchName, path, page, USER);
		return page;
	}
}
