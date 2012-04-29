package de.blizzy.documentr.repository;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import de.blizzy.documentr.Settings;
import de.blizzy.documentr.TestUtil;

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

	private void assertBranchesPageIsSharedWith(String branchName, String... expectedBranches)
			throws IOException {
		
		Set<String> branches = pageStore.getBranchesPageIsSharedWith(PROJECT, branchName, PAGE);
		assertEquals(Sets.newHashSet(expectedBranches), branches);
	}
	
	private void saveRandomPage(String branchName, String path) throws IOException, GitAPIException {
		Page page = createRandomPage();
		pageStore.savePage(PROJECT, branchName, path, page);
	}
	
	private Page createRandomPage() {
		return Page.fromText(String.valueOf(Math.random() * Long.MAX_VALUE), String.valueOf(Math.random() * Long.MAX_VALUE));
	}
}
