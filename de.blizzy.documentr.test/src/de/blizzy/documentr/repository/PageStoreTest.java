package de.blizzy.documentr.repository;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import de.blizzy.documentr.Settings;
import de.blizzy.documentr.TestUtil;

public class PageStoreTest {
	private GlobalRepositoryManager globalRepoManager;
	private PageStore pageStore;

	@Before
	public void setUp() {
		File dataDir = TestUtil.createTempDir();
		Settings settings = new Settings();
		settings.setDocumentrDataDir(dataDir);

		globalRepoManager = new GlobalRepositoryManager();
		globalRepoManager.setSettings(settings);
		globalRepoManager.setRepositoryManagerFactory(new ProjectRepositoryManagerFactory());
		globalRepoManager.init();
		
		pageStore = new PageStore();
		pageStore.setGlobalRepositoryManager(globalRepoManager);
	}
	
	@Test
	public void isPageSharedWithOtherBranches() throws IOException, GitAPIException {
		globalRepoManager.createProjectCentralRepository("project"); //$NON-NLS-1$
		globalRepoManager.createProjectBranchRepository("project", "branch1", null); //$NON-NLS-1$ //$NON-NLS-2$
		Page page = Page.fromText("title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage("project", "branch1", "page1", page); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		assertFalse(pageStore.isPageSharedWithOtherBranches("project", "branch1", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		globalRepoManager.createProjectBranchRepository("project", "branch2", "branch1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		globalRepoManager.createProjectBranchRepository("project", "branch3", "branch1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertTrue(pageStore.isPageSharedWithOtherBranches("project", "branch1", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertTrue(pageStore.isPageSharedWithOtherBranches("project", "branch2", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertTrue(pageStore.isPageSharedWithOtherBranches("project", "branch3", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		page = Page.fromText("title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage("project", "branch1", "page2", page); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		page = Page.fromText("title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage("project", "branch2", "page3", page); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		page = Page.fromText("title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage("project", "branch3", "page4", page); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertTrue(pageStore.isPageSharedWithOtherBranches("project", "branch1", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertTrue(pageStore.isPageSharedWithOtherBranches("project", "branch2", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertTrue(pageStore.isPageSharedWithOtherBranches("project", "branch3", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		page = Page.fromText("title", "text 2"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage("project", "branch1", "page1", page); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertFalse(pageStore.isPageSharedWithOtherBranches("project", "branch1", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertTrue(pageStore.isPageSharedWithOtherBranches("project", "branch2", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertTrue(pageStore.isPageSharedWithOtherBranches("project", "branch3", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		page = Page.fromText("title", "text 3"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage("project", "branch2", "page1", page); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertFalse(pageStore.isPageSharedWithOtherBranches("project", "branch1", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertFalse(pageStore.isPageSharedWithOtherBranches("project", "branch2", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertFalse(pageStore.isPageSharedWithOtherBranches("project", "branch3", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	@Test
	public void getBranchesPageIsSharedWith() throws IOException, GitAPIException {
		globalRepoManager.createProjectCentralRepository("project"); //$NON-NLS-1$
		globalRepoManager.createProjectBranchRepository("project", "branch1", null); //$NON-NLS-1$ //$NON-NLS-2$
		Page page = Page.fromText("title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage("project", "branch1", "page1", page); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		assertEquals(Sets.newHashSet("branch1"), //$NON-NLS-1$
				pageStore.getBranchesPageIsSharedWith("project", "branch1", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		globalRepoManager.createProjectBranchRepository("project", "branch2", "branch1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		globalRepoManager.createProjectBranchRepository("project", "branch3", "branch1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals(Sets.newHashSet("branch1", "branch2", "branch3"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				pageStore.getBranchesPageIsSharedWith("project", "branch1", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals(Sets.newHashSet("branch1", "branch2", "branch3"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				pageStore.getBranchesPageIsSharedWith("project", "branch2", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals(Sets.newHashSet("branch1", "branch2", "branch3"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				pageStore.getBranchesPageIsSharedWith("project", "branch3", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		page = Page.fromText("title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage("project", "branch1", "page2", page); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		page = Page.fromText("title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage("project", "branch2", "page3", page); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		page = Page.fromText("title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage("project", "branch3", "page4", page); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals(Sets.newHashSet("branch1", "branch2", "branch3"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				pageStore.getBranchesPageIsSharedWith("project", "branch1", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals(Sets.newHashSet("branch1", "branch2", "branch3"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				pageStore.getBranchesPageIsSharedWith("project", "branch2", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals(Sets.newHashSet("branch1", "branch2", "branch3"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				pageStore.getBranchesPageIsSharedWith("project", "branch3", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		page = Page.fromText("title", "text 2"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage("project", "branch1", "page1", page); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals(Sets.newHashSet("branch1"), //$NON-NLS-1$
				pageStore.getBranchesPageIsSharedWith("project", "branch1", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals(Sets.newHashSet("branch2", "branch3"), //$NON-NLS-1$ //$NON-NLS-2$
				pageStore.getBranchesPageIsSharedWith("project", "branch2", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals(Sets.newHashSet("branch2", "branch3"), //$NON-NLS-1$ //$NON-NLS-2$
				pageStore.getBranchesPageIsSharedWith("project", "branch3", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		page = Page.fromText("title", "text 3"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage("project", "branch2", "page1", page); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals(Sets.newHashSet("branch1"), //$NON-NLS-1$
				pageStore.getBranchesPageIsSharedWith("project", "branch1", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals(Sets.newHashSet("branch2"), //$NON-NLS-1$
				pageStore.getBranchesPageIsSharedWith("project", "branch2", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals(Sets.newHashSet("branch3"), //$NON-NLS-1$
				pageStore.getBranchesPageIsSharedWith("project", "branch3", "page1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
