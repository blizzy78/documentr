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
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.gitective.core.CommitUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

import com.google.common.collect.Lists;

import de.blizzy.documentr.AbstractDocumentrTest;

public class PageUtilTest extends AbstractDocumentrTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$

	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

	@Mock
	private PageStore pageStore;

	@Test
	public void getPagePathHierarchy() throws IOException {
		Page page1 = createRandomPage(null);
		Page page2 = createRandomPage("page1"); //$NON-NLS-1$
		Page page3 = createRandomPage("page1/page2"); //$NON-NLS-1$
		when(pageStore.getPage(PROJECT, BRANCH, "page1", false)).thenReturn(page1); //$NON-NLS-1$
		when(pageStore.getPage(PROJECT, BRANCH, "page1/page2", false)).thenReturn(page2); //$NON-NLS-1$
		when(pageStore.getPage(PROJECT, BRANCH, "page1/page2/page3", false)).thenReturn(page3); //$NON-NLS-1$

		assertEquals(Lists.newArrayList("page1", "page1/page2", "page1/page2/page3"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				PageUtil.getPagePathHierarchy(PROJECT, BRANCH, "page1/page2/page3", pageStore)); //$NON-NLS-1$
	}

	@Test
	public void toPageVersion() throws GitAPIException, IOException {
		File repoDir = tempDir.getRoot();
		Git git = Git.init().setDirectory(repoDir).call();
		PersonIdent ident = new PersonIdent("user", "user@example.com"); //$NON-NLS-1$ //$NON-NLS-2$
		git.commit().setAuthor(ident).setCommitter(ident).setMessage("test").call(); //$NON-NLS-1$
		RevCommit commit = CommitUtils.getHead(git.getRepository());

		PageVersion pageVersion = PageUtil.toPageVersion(commit);
		assertEquals("user", pageVersion.getLastEditedBy()); //$NON-NLS-1$
		assertSecondsAgo(pageVersion.getLastEdited(), 5);
		assertEquals(commit.getName(), pageVersion.getCommitName());
	}
}
