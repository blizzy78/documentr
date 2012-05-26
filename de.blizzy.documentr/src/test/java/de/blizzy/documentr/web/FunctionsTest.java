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
package de.blizzy.documentr.web;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import de.blizzy.documentr.TestUtil;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.pagestore.IPageStore;
import de.blizzy.documentr.pagestore.Page;
import de.blizzy.documentr.pagestore.PageMetadata;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.web.markdown.IPageRenderer;

public class FunctionsTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String PAGE = "page"; //$NON-NLS-1$
	
	private GlobalRepositoryManager repoManager;
	private IPageStore pageStore;
	private UserStore userStore;
	private IPageRenderer pageRenderer;

	@Before
	public void setUp() {
		repoManager = mock(GlobalRepositoryManager.class);
		Functions.setGlobalRepositoryManager(repoManager);
		pageStore = mock(IPageStore.class);
		Functions.setPageStore(pageStore);
		userStore = mock(UserStore.class);
		Functions.setUserStore(userStore);
		pageRenderer = mock(IPageRenderer.class);
		Functions.setPageRenderer(pageRenderer);
	}

	@After
	public void tearDown() {
		Functions.setGlobalRepositoryManager(null);
		Functions.setPageStore(null);
	}
	
	@Test
	public void listProjects() {
		List<String> projects = Arrays.asList("p1", "p2", "p3"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(repoManager.listProjects()).thenReturn(projects);
		assertEquals(projects, Functions.listProjects());
	}

	@Test
	public void listProjectBranches() throws IOException {
		List<String> branches = Arrays.asList("b1", "b2", "b3"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(repoManager.listProjectBranches(PROJECT)).thenReturn(branches);
		assertEquals(branches, Functions.listProjectBranches(PROJECT));
	}

	@Test
	public void listPageAt() throws IOException {
		List<String> attachments = Arrays.asList("test.txt", "foo.png"); //$NON-NLS-1$ //$NON-NLS-2$
		when(pageStore.listPageAttachments(PROJECT, BRANCH, PAGE)).thenReturn(attachments);
		assertEquals(attachments, Functions.listPageAttachments(PROJECT, BRANCH, PAGE));
	}
	
	@Test
	public void getPageTitle() throws IOException {
		Page page = Page.fromText("parent", "title", "text"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(pageStore.getPage(PROJECT, BRANCH, PAGE, false)).thenReturn(page);
		assertEquals(page.getTitle(), Functions.getPageTitle(PROJECT, BRANCH, PAGE));
	}

	@Test
	public void getBranchesPageIsSharedWith() throws IOException {
		List<String> branches = Arrays.asList("b1", "b2", "b3"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(pageStore.getBranchesPageIsSharedWith(PROJECT, BRANCH, PAGE)).thenReturn(branches);
		assertEquals(branches, Functions.getBranchesPageIsSharedWith(PROJECT, BRANCH, PAGE));
	}

	@Test
	public void listUsers() throws IOException {
		List<String> users = Arrays.asList("u1", "u2", "u3"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(userStore.listUsers()).thenReturn(users);
		assertEquals(users, Functions.listUsers());
	}

	@Test
	public void getPageHTML() throws IOException {
		when(pageRenderer.getHTML(PROJECT, BRANCH, PAGE)).thenReturn("html"); //$NON-NLS-1$
		assertEquals("html", Functions.getPageHTML(PROJECT, BRANCH, PAGE)); //$NON-NLS-1$
	}
	
	@Test
	public void getPagePathHierarchy() throws IOException {
		Page page1 = TestUtil.createRandomPage(null);
		Page page2 = TestUtil.createRandomPage("page1"); //$NON-NLS-1$
		Page page3 = TestUtil.createRandomPage("page1/page2"); //$NON-NLS-1$
		when(pageStore.getPage(PROJECT, BRANCH, "page1", false)).thenReturn(page1); //$NON-NLS-1$
		when(pageStore.getPage(PROJECT, BRANCH, "page1/page2", false)).thenReturn(page2); //$NON-NLS-1$
		when(pageStore.getPage(PROJECT, BRANCH, "page1/page2/page3", false)).thenReturn(page3); //$NON-NLS-1$
		
		assertEquals(Lists.newArrayList("page1", "page1/page2", "page1/page2/page3"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				Functions.getPagePathHierarchy(PROJECT, BRANCH, "page1/page2/page3")); //$NON-NLS-1$
	}

	@Test
	public void getPageMetadata() throws IOException {
		Date date = new Date();
		PageMetadata metadata = new PageMetadata("user", date); //$NON-NLS-1$
		when(pageStore.getPageMetadata(PROJECT, BRANCH, PAGE)).thenReturn(metadata);
		assertEquals("user", metadata.getLastEditedBy()); //$NON-NLS-1$
		assertEquals(date, metadata.getLastEdited());
	}
}
