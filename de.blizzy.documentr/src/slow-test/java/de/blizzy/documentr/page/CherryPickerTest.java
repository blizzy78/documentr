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
package de.blizzy.documentr.page;

import static de.blizzy.documentr.TestUtil.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.springframework.context.MessageSource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.Settings;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.LockManager;
import de.blizzy.documentr.repository.ProjectRepositoryManagerFactory;

public class CherryPickerTest extends AbstractDocumentrTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH_1 = "branch_1"; //$NON-NLS-1$
	private static final String BRANCH_2 = "branch_2"; //$NON-NLS-1$
	private static final String PAGE = "page"; //$NON-NLS-1$
	private static final User USER = new User("currentUser", "pw", "admin@example.com", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final Locale LOCALE = Locale.ENGLISH;

	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

	@Mock
	private EventBus eventBus;
	@Mock
	private Settings settings;
	@Mock
	@SuppressWarnings("unused")
	private LockManager lockManager;
	@Mock
	private MessageSource messageSource;
	private GlobalRepositoryManager globalRepoManager;
	private PageStore pageStore;
	private CherryPicker cherryPicker;
	@InjectMocks
	private ProjectRepositoryManagerFactory repoManagerFactory;

	@Before
	public void setUp() {
		File dataDir = tempDir.getRoot();

		when(settings.getDocumentrDataDir()).thenReturn(dataDir);

		globalRepoManager = new GlobalRepositoryManager();
		Whitebox.setInternalState(globalRepoManager, settings, repoManagerFactory, eventBus);
		globalRepoManager.init();

		pageStore = new PageStore();
		Whitebox.setInternalState(pageStore, globalRepoManager, eventBus);

		when(messageSource.getMessage(eq("sourceBranchX"), any(Object[].class), any(Locale.class))).thenReturn("THEIRS"); //$NON-NLS-1$ //$NON-NLS-2$
		when(messageSource.getMessage(eq("targetBranchX"), any(Object[].class), any(Locale.class))).thenReturn("OURS"); //$NON-NLS-1$ //$NON-NLS-2$

		cherryPicker = new CherryPicker();
		Whitebox.setInternalState(cherryPicker, globalRepoManager, eventBus, messageSource);
	}

	@Test
	public void cherryPickNonConflictingEdits() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		Page page = Page.fromText("title", "a\nb\nc\n"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_1, PAGE, page, null, USER);
		sleep(1000); // must wait because commit time is stored in seconds

		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_2, BRANCH_1));

		page = Page.fromText("title", "aaa\nb\nc\n"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_1, PAGE, page, null, USER);
		String commit1 = pageStore.getPageMetadata(PROJECT, BRANCH_1, PAGE).getCommit();
		sleep(1000); // must wait because commit time is stored in seconds
		page = Page.fromText("title", "aaa\nbbb\nc\n"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_1, PAGE, page, null, USER);
		String commit2 = pageStore.getPageMetadata(PROJECT, BRANCH_1, PAGE).getCommit();

		Map<String, List<CommitCherryPickResult>> results = cherryPicker.cherryPick(
				PROJECT, BRANCH_1, PAGE, Lists.newArrayList(commit1, commit2), Sets.newHashSet(BRANCH_2),
				Collections.<CommitCherryPickConflictResolve>emptySet(), false, USER, LOCALE);
		List<CommitCherryPickResult> branchResults = results.get(BRANCH_2);
		CommitCherryPickResult commit1Result = branchResults.get(0);
		assertEquals(commit1, commit1Result.getPageVersion().getCommitName());
		assertSame(CommitCherryPickResult.Status.OK, commit1Result.getStatus());
		assertNull(commit1Result.getConflictText());
		CommitCherryPickResult commit2Result = branchResults.get(1);
		assertEquals(commit2, commit2Result.getPageVersion().getCommitName());
		assertSame(CommitCherryPickResult.Status.OK, commit2Result.getStatus());
		assertNull(commit2Result.getConflictText());
		page = pageStore.getPage(PROJECT, BRANCH_2, PAGE, true);
		assertEquals("aaa\nbbb\nc\n", ((PageTextData) page.getData()).getText()); //$NON-NLS-1$
	}

	@Test
	public void cherryPickConflictingEdits() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		Page page = Page.fromText("title", "a\nb\nc\nd\ne\nf\ng\nh\ni\n"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_1, PAGE, page, null, USER);
		sleep(1000); // must wait because commit time is stored in seconds

		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_2, BRANCH_1));
		page = Page.fromText("title", "a\nb\nc\nd\ne\nf\ng\nxxx\ni\n"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_2, PAGE, page, null, USER);

		page = Page.fromText("title", "aaa\nb\nc\nd\ne\nf\ng\nh\ni\n"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_1, PAGE, page, null, USER);
		String commit1 = pageStore.getPageMetadata(PROJECT, BRANCH_1, PAGE).getCommit();
		sleep(1000); // must wait because commit time is stored in seconds
		page = Page.fromText("title", "aaa\nb\nc\nd\ne\nf\ng\nhhh\ni\n"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_1, PAGE, page, null, USER);
		String commit2 = pageStore.getPageMetadata(PROJECT, BRANCH_1, PAGE).getCommit();
		sleep(1000); // must wait because commit time is stored in seconds
		page = Page.fromText("title", "aaa\nbbb\nc\nd\ne\nf\ng\nhhh\ni\n"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_1, PAGE, page, null, USER);
		String commit3 = pageStore.getPageMetadata(PROJECT, BRANCH_1, PAGE).getCommit();

		Map<String, List<CommitCherryPickResult>> results = cherryPicker.cherryPick(
				PROJECT, BRANCH_1, PAGE, Lists.newArrayList(commit1, commit2, commit3), Sets.newHashSet(BRANCH_2),
				Collections.<CommitCherryPickConflictResolve>emptySet(), false, USER, LOCALE);
		List<CommitCherryPickResult> branchResults = results.get(BRANCH_2);
		CommitCherryPickResult commit1Result = branchResults.get(0);
		assertEquals(commit1, commit1Result.getPageVersion().getCommitName());
		assertSame(CommitCherryPickResult.Status.OK, commit1Result.getStatus());
		assertNull(commit1Result.getConflictText());
		CommitCherryPickResult commit2Result = branchResults.get(1);
		assertEquals(commit2, commit2Result.getPageVersion().getCommitName());
		assertSame(CommitCherryPickResult.Status.CONFLICT, commit2Result.getStatus());
		assertEquals("aaa\nb\nc\nd\ne\nf\ng\n<<<<<<< OURS\nxxx\n=======\nhhh\n>>>>>>> THEIRS\ni\n", //$NON-NLS-1$
				commit2Result.getConflictText());
		CommitCherryPickResult commit3Result = branchResults.get(2);
		assertEquals(commit3, commit3Result.getPageVersion().getCommitName());
		assertSame(CommitCherryPickResult.Status.UNKNOWN, commit3Result.getStatus());
		assertNull(commit3Result.getConflictText());
		page = pageStore.getPage(PROJECT, BRANCH_2, PAGE, true);
		assertEquals("a\nb\nc\nd\ne\nf\ng\nxxx\ni\n", ((PageTextData) page.getData()).getText()); //$NON-NLS-1$
	}

	@Test
	public void cherryPickConflictingEditsWithConflictResolves() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		Page page = Page.fromText("title", "a\nb\nc\nd\ne\nf\ng\nh\ni\n"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_1, PAGE, page, null, USER);
		sleep(1000); // must wait because commit time is stored in seconds

		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_2, BRANCH_1));
		page = Page.fromText("title", "a\nb\nc\nd\ne\nf\ng\nxxx\ni\n"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_2, PAGE, page, null, USER);

		page = Page.fromText("title", "aaa\nb\nc\nd\ne\nf\ng\nh\ni\n"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_1, PAGE, page, null, USER);
		String commit1 = pageStore.getPageMetadata(PROJECT, BRANCH_1, PAGE).getCommit();
		sleep(1000); // must wait because commit time is stored in seconds
		page = Page.fromText("title", "aaa\nb\nc\nd\ne\nf\ng\nhhh\ni\n"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_1, PAGE, page, null, USER);
		String commit2 = pageStore.getPageMetadata(PROJECT, BRANCH_1, PAGE).getCommit();
		sleep(1000); // must wait because commit time is stored in seconds
		page = Page.fromText("title", "aaa\nbbb\nc\nd\ne\nf\ng\nhhh\ni\n"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_1, PAGE, page, null, USER);
		String commit3 = pageStore.getPageMetadata(PROJECT, BRANCH_1, PAGE).getCommit();

		Set<CommitCherryPickConflictResolve> resolves = Sets.newHashSet(
				new CommitCherryPickConflictResolve(BRANCH_2, commit2,
						"<<<<<<< OURS\nxxx\n=======\nyyy\n>>>>>>> THEIRS\n")); //$NON-NLS-1$
		Map<String, List<CommitCherryPickResult>> results = cherryPicker.cherryPick(
				PROJECT, BRANCH_1, PAGE, Lists.newArrayList(commit1, commit2, commit3), Sets.newHashSet(BRANCH_2),
				resolves, false, USER, LOCALE);
		List<CommitCherryPickResult> branchResults = results.get(BRANCH_2);
		CommitCherryPickResult commit1Result = branchResults.get(0);
		assertEquals(commit1, commit1Result.getPageVersion().getCommitName());
		assertSame(CommitCherryPickResult.Status.OK, commit1Result.getStatus());
		assertNull(commit1Result.getConflictText());
		CommitCherryPickResult commit2Result = branchResults.get(1);
		assertEquals(commit2, commit2Result.getPageVersion().getCommitName());
		assertSame(CommitCherryPickResult.Status.CONFLICT, commit2Result.getStatus());
		assertEquals("<<<<<<< OURS\nxxx\n=======\nyyy\n>>>>>>> THEIRS\n", //$NON-NLS-1$
				commit2Result.getConflictText());
		CommitCherryPickResult commit3Result = branchResults.get(2);
		assertEquals(commit3, commit3Result.getPageVersion().getCommitName());
		assertSame(CommitCherryPickResult.Status.UNKNOWN, commit3Result.getStatus());
		assertNull(commit3Result.getConflictText());
		page = pageStore.getPage(PROJECT, BRANCH_2, PAGE, true);
		assertEquals("a\nb\nc\nd\ne\nf\ng\nxxx\ni\n", ((PageTextData) page.getData()).getText()); //$NON-NLS-1$
	}

	@Test
	public void cherryPickDryRunMustNotModifyPage() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		Page page = Page.fromText("title", "a\nb\nc\n"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_1, PAGE, page, null, USER);

		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_2, BRANCH_1));

		page = Page.fromText("title", "aaa\nb\nc\n"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_1, PAGE, page, null, USER);
		String commit = pageStore.getPageMetadata(PROJECT, BRANCH_1, PAGE).getCommit();

		cherryPicker.cherryPick(PROJECT, BRANCH_1, PAGE, Lists.newArrayList(commit), Sets.newHashSet(BRANCH_2),
				Collections.<CommitCherryPickConflictResolve>emptySet(), true, USER, LOCALE);
		page = pageStore.getPage(PROJECT, BRANCH_2, PAGE, true);
		assertEquals("a\nb\nc\n", ((PageTextData) page.getData()).getText()); //$NON-NLS-1$
	}

	@Test
	public void cherryPickConflictingEditsWithUnresolvedConflictResolves() throws IOException, GitAPIException {
		register(globalRepoManager.createProjectCentralRepository(PROJECT, USER));
		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_1, null));
		Page page = Page.fromText("title", "a\nb\nc\nd\ne\nf\ng\nh\ni\n"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_1, PAGE, page, null, USER);
		sleep(1000); // must wait because commit time is stored in seconds

		register(globalRepoManager.createProjectBranchRepository(PROJECT, BRANCH_2, BRANCH_1));
		page = Page.fromText("title", "a\nb\nc\nd\ne\nf\ng\nxxx\ni\n"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_2, PAGE, page, null, USER);

		page = Page.fromText("title", "aaa\nb\nc\nd\ne\nf\ng\nh\ni\n"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_1, PAGE, page, null, USER);
		String commit1 = pageStore.getPageMetadata(PROJECT, BRANCH_1, PAGE).getCommit();
		sleep(1000); // must wait because commit time is stored in seconds
		page = Page.fromText("title", "aaa\nb\nc\nd\ne\nf\ng\nhhh\ni\n"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_1, PAGE, page, null, USER);
		String commit2 = pageStore.getPageMetadata(PROJECT, BRANCH_1, PAGE).getCommit();
		sleep(1000); // must wait because commit time is stored in seconds
		page = Page.fromText("title", "aaa\nbbb\nc\nd\ne\nf\ng\nhhh\ni\n"); //$NON-NLS-1$ //$NON-NLS-2$
		pageStore.savePage(PROJECT, BRANCH_1, PAGE, page, null, USER);
		String commit3 = pageStore.getPageMetadata(PROJECT, BRANCH_1, PAGE).getCommit();

		Set<CommitCherryPickConflictResolve> resolves = Sets.newHashSet(
				new CommitCherryPickConflictResolve(BRANCH_2, commit2, "aaa\nb\nc\nd\ne\nf\ng\nhhh xxx\ni\n")); //$NON-NLS-1$
		Map<String, List<CommitCherryPickResult>> results = cherryPicker.cherryPick(
				PROJECT, BRANCH_1, PAGE, Lists.newArrayList(commit1, commit2, commit3), Sets.newHashSet(BRANCH_2),
				resolves, false, USER, LOCALE);
		List<CommitCherryPickResult> branchResults = results.get(BRANCH_2);
		CommitCherryPickResult commit1Result = branchResults.get(0);
		assertEquals(commit1, commit1Result.getPageVersion().getCommitName());
		assertSame(CommitCherryPickResult.Status.OK, commit1Result.getStatus());
		assertNull(commit1Result.getConflictText());
		CommitCherryPickResult commit2Result = branchResults.get(1);
		assertEquals(commit2, commit2Result.getPageVersion().getCommitName());
		assertSame(CommitCherryPickResult.Status.OK, commit2Result.getStatus());
		assertNull(commit2Result.getConflictText());
		CommitCherryPickResult commit3Result = branchResults.get(2);
		assertEquals(commit3, commit3Result.getPageVersion().getCommitName());
		assertSame(CommitCherryPickResult.Status.OK, commit3Result.getStatus());
		assertNull(commit3Result.getConflictText());
		page = pageStore.getPage(PROJECT, BRANCH_2, PAGE, true);
		assertEquals("aaa\nbbb\nc\nd\ne\nf\ng\nhhh xxx\ni\n", ((PageTextData) page.getData()).getText()); //$NON-NLS-1$
	}

	@Test
	public void getCommitsList() throws IOException {
		pageStore = mock(PageStore.class);
		Whitebox.setInternalState(cherryPicker, pageStore);

		@SuppressWarnings("nls")
		List<PageVersion> versions = Lists.newArrayList(
				new PageVersion("commit5", "user", new Date()),
				new PageVersion("commit4", "user", new Date()),
				new PageVersion("commit3", "user", new Date()),
				new PageVersion("commit2", "user", new Date()),
				new PageVersion("commit1", "user", new Date())
		);
		when(pageStore.listPageVersions(PROJECT, BRANCH_1, PAGE)).thenReturn(versions);

		List<String> result = cherryPicker.getCommitsList(PROJECT, BRANCH_1, PAGE, "commit2", "commit4"); //$NON-NLS-1$ //$NON-NLS-2$
		List<String> commits = Lists.newArrayList("commit3", "commit4"); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals(commits, result);
	}
}
