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
package de.blizzy.documentr.page;

import static de.blizzy.documentr.TestUtil.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.gitective.core.CommitUtils;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.Settings;
import de.blizzy.documentr.TestSettingsUtil;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.ILockedRepository;
import de.blizzy.documentr.repository.LockManager;
import de.blizzy.documentr.repository.ProjectRepositoryManagerFactory;
import de.blizzy.documentr.repository.RepositoryUtil;
import de.blizzy.documentr.search.PageIndex;

public class PageStoreTest extends AbstractDocumentrTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH_1 = "branch_1"; //$NON-NLS-1$
	private static final String BRANCH_2 = "branch_2"; //$NON-NLS-1$
	private static final String BRANCH_3 = "branch_3"; //$NON-NLS-1$
	private static final String PAGE = "page"; //$NON-NLS-1$
	private static final User USER = new User("currentUser", "pw", "admin@example.com", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	private GlobalRepositoryManager globalRepoManager;
	private PageIndex pageIndex;
	private PageStore pageStore;

	@Before
	public void setUp() {
		File dataDir = createTempDir();
		Settings settings = new Settings();
		TestSettingsUtil.setDataDir(settings, dataDir);

		globalRepoManager = new GlobalRepositoryManager();
		globalRepoManager.setSettings(settings);
		ProjectRepositoryManagerFactory repoManagerFactory = new ProjectRepositoryManagerFactory();
		repoManagerFactory.setLockManager(mock(LockManager.class));
		globalRepoManager.setRepositoryManagerFactory(repoManagerFactory);
		globalRepoManager.init();
		
		pageIndex = mock(PageIndex.class);
		
		pageStore = new PageStore();
		pageStore.setGlobalRepositoryManager(globalRepoManager);
		pageStore.setPageIndex(pageIndex);
	}
	
	@Test
	public void saveAndGetPage() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		Page page = saveRandomPage(BRANCH_1, "home/foo"); //$NON-NLS-1$
		Page result = pageStore.getPage(PROJECT, BRANCH_1, "home/foo", true); //$NON-NLS-1$
		assertEquals(page.getTitle(), result.getTitle());
		assertEquals(((PageTextData) page.getData()).getText(), ((PageTextData) result.getData()).getText());
		assertEquals(page.getContentType(), result.getContentType());
		assertEquals("home", result.getParentPagePath()); //$NON-NLS-1$
		assertNull(result.getViewRestrictionRole());
		
		verify(pageIndex).addPage(PROJECT, BRANCH_1, "home/foo", page); //$NON-NLS-1$
	}
	
	@Test
	public void saveAndGetPageWithViewRestrictionRole() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		Page page = Page.fromText("title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		page.setViewRestrictionRole("viewRole"); //$NON-NLS-1$
		pageStore.savePage(PROJECT, BRANCH_1, "home/foo", page, USER); //$NON-NLS-1$
		Page result = pageStore.getPage(PROJECT, BRANCH_1, "home/foo", true); //$NON-NLS-1$
		assertEquals(page.getTitle(), result.getTitle());
		assertEquals(((PageTextData) page.getData()).getText(), ((PageTextData) result.getData()).getText());
		assertEquals(page.getContentType(), result.getContentType());
		assertEquals(page.getViewRestrictionRole(), result.getViewRestrictionRole());
		assertEquals("home", result.getParentPagePath()); //$NON-NLS-1$
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
		Page attachment = Page.fromData(new byte[] { 1, 2, 3 }, "application/octet-stream"); //$NON-NLS-1$
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
		Page attachment = Page.fromData(new byte[] { 1, 2, 3 }, "application/octet-stream"); //$NON-NLS-1$
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
		saveRandomPage(BRANCH_1, DocumentrConstants.HOME_PAGE_NAME + "/foo"); //$NON-NLS-1$
		saveRandomPage(BRANCH_1, DocumentrConstants.HOME_PAGE_NAME + "/foo/bar"); //$NON-NLS-1$
		saveRandomPage(BRANCH_1, DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/baz"); //$NON-NLS-1$
		saveRandomPage(BRANCH_1, DocumentrConstants.HOME_PAGE_NAME + "/foo/qux"); //$NON-NLS-1$
		
		Set<String> expected = Sets.newHashSet(DocumentrConstants.HOME_PAGE_NAME + "/foo/bar", //$NON-NLS-1$
				DocumentrConstants.HOME_PAGE_NAME + "/foo/qux"); //$NON-NLS-1$
		Set<String> result = Sets.newHashSet(pageStore.listChildPagePaths(
				PROJECT, BRANCH_1, DocumentrConstants.HOME_PAGE_NAME + "/foo")); //$NON-NLS-1$
		assertEquals(expected, result);
		expected = Collections.singleton(DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/baz"); //$NON-NLS-1$
		result = Sets.newHashSet(pageStore.listChildPagePaths(
				PROJECT, BRANCH_1, DocumentrConstants.HOME_PAGE_NAME + "/foo/bar")); //$NON-NLS-1$
		assertEquals(expected, result);
	}
	
	@Test
	public void deletePage() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		ILockedRepository repo = globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null);
		register(repo);
		saveRandomPage(BRANCH_1, "home"); //$NON-NLS-1$
		saveRandomPage(BRANCH_1, "home/foo"); //$NON-NLS-1$
		saveRandomAttachment(BRANCH_1, "home/foo", "test.txt"); //$NON-NLS-1$ //$NON-NLS-2$
		saveRandomPage(BRANCH_1, "home/foo/bar"); //$NON-NLS-1$
		File pageFile = new File(new File(new File(RepositoryUtil.getWorkingDir(repo.r()), "pages"), "home"), "foo.page"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		File metaFile = new File(new File(new File(RepositoryUtil.getWorkingDir(repo.r()), "pages"), "home"), "foo.meta"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		File subPagesDir = new File(new File(new File(RepositoryUtil.getWorkingDir(repo.r()), "pages"), "home"), "foo"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		File attachmentsDir = new File(new File(new File(RepositoryUtil.getWorkingDir(repo.r()), "attachments"), "home"), "foo"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertTrue(pageFile.isFile());
		assertTrue(metaFile.isFile());
		assertTrue(subPagesDir.isDirectory());
		assertTrue(attachmentsDir.isDirectory());
		
		pageStore.deletePage(PROJECT, BRANCH_1, "home/foo", USER); //$NON-NLS-1$
		List<String> result = pageStore.listChildPagePaths(PROJECT, BRANCH_1, "home"); //$NON-NLS-1$
		assertTrue(result.isEmpty());
		assertFalse(pageFile.exists());
		assertFalse(metaFile.exists());
		assertFalse(subPagesDir.exists());
		assertFalse(attachmentsDir.exists());
	}
	
	@Test
	public void deleteAttachment() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		saveRandomPage(BRANCH_1, PAGE);
		Page attachment = Page.fromData(new byte[] { 1, 2, 3 }, "application/octet-stream"); //$NON-NLS-1$
		pageStore.saveAttachment(PROJECT, BRANCH_1, PAGE, "test.dat", attachment, USER); //$NON-NLS-1$
		
		pageStore.deleteAttachment(PROJECT, BRANCH_1, PAGE, "test.dat", USER); //$NON-NLS-1$
		assertTrue(pageStore.listPageAttachments(PROJECT, BRANCH_1, PAGE).isEmpty());
	}
	
	@Test
	public void getPageMetadata() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		ILockedRepository repo = globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null);
		register(repo);
		saveRandomPage(BRANCH_1, PAGE);
		File file = new File(new File(RepositoryUtil.getWorkingDir(repo.r()), "pages"), PAGE + ".page"); //$NON-NLS-1$ //$NON-NLS-2$
		long size = file.length();
		PageMetadata metadata = pageStore.getPageMetadata(PROJECT, BRANCH_1, PAGE);
		assertEquals(USER.getLoginName(), metadata.getLastEditedBy());
		assertSecondsAgo(metadata.getLastEdited(), 5);
		assertEquals(size, metadata.getSize());
	}

	@Test
	public void getAttachmentMetadata() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		saveRandomPage(BRANCH_1, PAGE);
		Page attachment = Page.fromData(new byte[] { 1, 2, 3 }, "image/png"); //$NON-NLS-1$
		pageStore.saveAttachment(PROJECT, BRANCH_1, PAGE, "test.png", attachment, USER); //$NON-NLS-1$
		PageMetadata metadata = pageStore.getAttachmentMetadata(PROJECT, BRANCH_1, PAGE, "test.png"); //$NON-NLS-1$
		assertEquals(USER.getLoginName(), metadata.getLastEditedBy());
		assertSecondsAgo(metadata.getLastEdited(), 5);
	}
	
	@Test
	public void relocatePage() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		saveRandomPage(BRANCH_1, "home"); //$NON-NLS-1$
		saveRandomPage(BRANCH_1, "home/foo"); //$NON-NLS-1$
		Page page = saveRandomPage(BRANCH_1, "home/foo/bar"); //$NON-NLS-1$
		saveRandomPage(BRANCH_1, "home/foo/bar/quuux"); //$NON-NLS-1$
		saveRandomPage(BRANCH_1, "home/foo/quux"); //$NON-NLS-1$
		saveRandomPage(BRANCH_1, "home/baz"); //$NON-NLS-1$
		saveRandomPage(BRANCH_1, "home/baz/bar"); //$NON-NLS-1$
		saveRandomPage(BRANCH_1, "home/baz/qux"); //$NON-NLS-1$
		Page attachment = saveRandomAttachment(BRANCH_1, "home/foo/bar", "test.txt"); //$NON-NLS-1$ //$NON-NLS-2$
		saveRandomAttachment(BRANCH_1, "home/baz/bar", "test.txt"); //$NON-NLS-1$ //$NON-NLS-2$
		
		pageStore.relocatePage(PROJECT, BRANCH_1, "home/foo/bar", "home/baz", USER); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals(Sets.newHashSet("home/foo/quux"), //$NON-NLS-1$
				Sets.newHashSet(pageStore.listChildPagePaths(PROJECT, BRANCH_1, "home/foo"))); //$NON-NLS-1$
		assertEquals(Sets.newHashSet("home/baz/bar", "home/baz/qux"), //$NON-NLS-1$ //$NON-NLS-2$
				Sets.newHashSet(pageStore.listChildPagePaths(PROJECT, BRANCH_1, "home/baz"))); //$NON-NLS-1$
		assertEquals(Sets.newHashSet("home/baz/bar/quuux"), //$NON-NLS-1$
				Sets.newHashSet(pageStore.listChildPagePaths(PROJECT, BRANCH_1, "home/baz/bar"))); //$NON-NLS-1$
		assertEquals(page.getData(),
				pageStore.getPage(PROJECT, BRANCH_1, "home/baz/bar", true).getData()); //$NON-NLS-1$
		assertEquals(attachment.getData(),
				pageStore.getAttachment(PROJECT, BRANCH_1, "home/baz/bar", "test.txt").getData()); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Test
	public void getMarkdown() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		ILockedRepository repo = globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null);
		register(repo);
		
		Page page1 = Page.fromText("title", UUID.randomUUID().toString()); //$NON-NLS-1$
		pageStore.savePage(PROJECT, BRANCH_1, "home", page1, USER); //$NON-NLS-1$
		RevCommit commit1 = CommitUtils.getLastCommit(repo.r(), "pages/home.page"); //$NON-NLS-1$
		Page page2 = Page.fromText("title", UUID.randomUUID().toString()); //$NON-NLS-1$
		pageStore.savePage(PROJECT, BRANCH_1, "home", page2, USER); //$NON-NLS-1$
		RevCommit commit2 = CommitUtils.getLastCommit(repo.r(), "pages/home.page"); //$NON-NLS-1$
		Page page3 = Page.fromText("title", UUID.randomUUID().toString()); //$NON-NLS-1$
		pageStore.savePage(PROJECT, BRANCH_1, "home", page3, USER); //$NON-NLS-1$
		RevCommit commit3 = CommitUtils.getLastCommit(repo.r(), "pages/home.page"); //$NON-NLS-1$
		
		Map<String, String> result = pageStore.getMarkdown(PROJECT, BRANCH_1, "home", //$NON-NLS-1$
				Sets.newHashSet("latest", "previous", commit2.getName(), commit1.getName())); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals(commit3.getName(), result.get("latest")); //$NON-NLS-1$
		assertEquals(commit2.getName(), result.get("previous")); //$NON-NLS-1$
		assertEquals(((PageTextData) page2.getData()).getText(), result.get(commit2.getName()));
		assertEquals(((PageTextData) page1.getData()).getText(), result.get(commit1.getName()));
	}
	
	@Test
	public void listPageVersions() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		ILockedRepository repo = globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null);
		register(repo);
		
		saveRandomPage(BRANCH_1, "home"); //$NON-NLS-1$
		RevCommit commit1 = CommitUtils.getLastCommit(repo.r(), "pages/home.page"); //$NON-NLS-1$
		saveRandomPage(BRANCH_1, "home/foo"); //$NON-NLS-1$
		saveRandomPage(BRANCH_1, "home"); //$NON-NLS-1$
		RevCommit commit2 = CommitUtils.getLastCommit(repo.r(), "pages/home.page"); //$NON-NLS-1$
		saveRandomPage(BRANCH_1, "home/bar"); //$NON-NLS-1$
		saveRandomPage(BRANCH_1, "home"); //$NON-NLS-1$
		RevCommit commit3 = CommitUtils.getLastCommit(repo.r(), "pages/home.page"); //$NON-NLS-1$
		saveRandomPage(BRANCH_1, "home/baz"); //$NON-NLS-1$

		List<PageVersion> versions = pageStore.listPageVersions(PROJECT, BRANCH_1, "home"); //$NON-NLS-1$
		assertEquals(3, versions.size());
		assertPageVersion(commit3, versions.get(0));
		assertPageVersion(commit2, versions.get(1));
		assertPageVersion(commit1, versions.get(2));
	}
	
	@Test
	public void getPageViewRestrictionRole() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		Page page = Page.fromText("title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		page.setViewRestrictionRole("viewRole"); //$NON-NLS-1$
		pageStore.savePage(PROJECT, BRANCH_1, "home/page", page, USER); //$NON-NLS-1$
		
		String role = pageStore.getViewRestrictionRole(PROJECT, BRANCH_1, "home/page"); //$NON-NLS-1$
		assertEquals(page.getViewRestrictionRole(), role);
	}
	
	@Test
	public void restorePageVersion() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		Page page = Page.fromText("old", "old"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_1, PAGE, page, USER);
		page = Page.fromText("new", "new"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_1, PAGE, page, USER);
		List<PageVersion> versions = pageStore.listPageVersions(PROJECT, BRANCH_1, PAGE);
		
		pageStore.restorePageVersion(PROJECT, BRANCH_1, PAGE, versions.get(1).getCommitName(), USER);
		Page result = pageStore.getPage(PROJECT, BRANCH_1, PAGE, true);
		assertEquals("old", ((PageTextData) result.getData()).getText()); //$NON-NLS-1$
		versions = pageStore.listPageVersions(PROJECT, BRANCH_1, PAGE);
		assertEquals(3, versions.size());
	}
	
	private void assertPageVersion(RevCommit commit, PageVersion version) {
		assertEquals(commit.getName(), version.getCommitName());
		assertEquals(commit.getCommitterIdent().getName(), version.getLastEditedBy());
		assertEquals(new Date(commit.getCommitTime() * 1000L), version.getLastEdited());
	}

	private void assertBranchesPageIsSharedWith(String branchName, String... expectedBranches)
			throws IOException {
		
		List<String> branches = pageStore.getBranchesPageIsSharedWith(PROJECT, branchName, PAGE);
		assertEquals(expectedBranches.length, branches.size());
		assertEquals(Sets.newHashSet(expectedBranches), Sets.newHashSet(branches));
	}
	
	private Page saveRandomPage(String branchName, String path) throws IOException {
		Page page = createRandomPage();
		pageStore.savePage(PROJECT, branchName, path, page, USER);
		return page;
	}
	
	private Page saveRandomAttachment(String branchName, String pagePath, String fileName) throws IOException {
		try {
			byte[] data = UUID.randomUUID().toString().getBytes("UTF-8"); //$NON-NLS-1$
			Page attachment = Page.fromData(data, "application/octet-stream"); //$NON-NLS-1$
			pageStore.saveAttachment(PROJECT, branchName, pagePath, fileName, attachment, USER);
			return attachment;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
