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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.blizzy.documentr.TestUtil;
import de.blizzy.documentr.access.GrantedAuthorityTarget;
import de.blizzy.documentr.access.GrantedAuthorityTarget.Type;
import de.blizzy.documentr.access.RoleGrantedAuthority;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.page.PageMetadata;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.web.markdown.IPageRenderer;
import de.blizzy.documentr.web.markdown.MarkdownProcessor;

public class FunctionsTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String PAGE = "page"; //$NON-NLS-1$
	
	private GlobalRepositoryManager repoManager;
	private IPageStore pageStore;
	private UserStore userStore;
	private IPageRenderer pageRenderer;
	private MarkdownProcessor markdownProcessor;
	private Authentication authentication;
	private MessageSource messageSource;

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
		markdownProcessor = mock(MarkdownProcessor.class);
		Functions.setMarkdownProcessor(markdownProcessor);
		messageSource = mock(MessageSource.class);
		Functions.setMessageSource(messageSource);
		
		authentication = mock(Authentication.class);
		
		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		
		SecurityContextHolder.setContext(securityContext);
	}

	@After
	public void tearDown() {
		Functions.setGlobalRepositoryManager(null);
		Functions.setPageStore(null);
		Functions.setUserStore(null);
		Functions.setPageRenderer(null);
		Functions.setMarkdownProcessor(null);
		Functions.setMessageSource(null);
		SecurityContextHolder.clearContext();
	}
	
	@Test
	public void listProjects() {
		List<String> projects = Lists.newArrayList("p1", "p2", "p3"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(repoManager.listProjects()).thenReturn(projects);
		assertEquals(projects, Functions.listProjects());
	}

	@Test
	public void listProjectBranches() throws IOException {
		List<String> branches = Lists.newArrayList("b1", "b2", "b3"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(repoManager.listProjectBranches(PROJECT)).thenReturn(branches);
		assertEquals(branches, Functions.listProjectBranches(PROJECT));
	}

	@Test
	public void listPageAt() throws IOException {
		List<String> attachments = Lists.newArrayList("test.txt", "foo.png"); //$NON-NLS-1$ //$NON-NLS-2$
		when(pageStore.listPageAttachments(PROJECT, BRANCH, PAGE)).thenReturn(attachments);
		assertEquals(attachments, Functions.listPageAttachments(PROJECT, BRANCH, PAGE));
	}
	
	@Test
	public void getPageTitle() throws IOException {
		Page page = Page.fromText("title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		when(pageStore.getPage(PROJECT, BRANCH, PAGE, false)).thenReturn(page);
		assertEquals(page.getTitle(), Functions.getPageTitle(PROJECT, BRANCH, PAGE));
	}

	@Test
	public void getBranchesPageIsSharedWith() throws IOException {
		List<String> branches = Lists.newArrayList("b1", "b2", "b3"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(pageStore.getBranchesPageIsSharedWith(PROJECT, BRANCH, PAGE)).thenReturn(branches);
		assertEquals(branches, Functions.getBranchesPageIsSharedWith(PROJECT, BRANCH, PAGE));
	}

	@Test
	public void listUsers() throws IOException {
		List<String> users = Lists.newArrayList("u1", "u2", "u3"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(userStore.listUsers()).thenReturn(users);
		assertEquals(users, Functions.listUsers());
	}

	@Test
	public void getPageHTML() throws IOException {
		when(pageRenderer.getHtml(PROJECT, BRANCH, PAGE, authentication)).thenReturn("html"); //$NON-NLS-1$
		when(markdownProcessor.processNonCacheableMacros("html", PROJECT, BRANCH, PAGE, authentication)) //$NON-NLS-1$
			.thenReturn("htmlWithMacros"); //$NON-NLS-1$
		assertEquals("htmlWithMacros", Functions.getPageHTML(PROJECT, BRANCH, PAGE)); //$NON-NLS-1$
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
		PageMetadata metadata = new PageMetadata("user", date, 123); //$NON-NLS-1$
		when(pageStore.getPageMetadata(PROJECT, BRANCH, PAGE)).thenReturn(metadata);
		
		PageMetadata result = Functions.getPageMetadata(PROJECT, BRANCH, PAGE);
		assertEquals(metadata.getLastEditedBy(), result.getLastEditedBy());
		assertEquals(metadata.getLastEdited(), result.getLastEdited());
		assertEquals(metadata.getSize(), result.getSize());
	}
	
	@Test
	public void getAttachmentMetadata() throws IOException {
		Date date = new Date();
		PageMetadata metadata = new PageMetadata("user", date, 123); //$NON-NLS-1$
		when(pageStore.getAttachmentMetadata(PROJECT, BRANCH, PAGE, "test.png")).thenReturn(metadata); //$NON-NLS-1$
		
		PageMetadata result = Functions.getAttachmentMetadata(PROJECT, BRANCH, PAGE, "test.png"); //$NON-NLS-1$
		assertEquals(metadata.getLastEditedBy(), result.getLastEditedBy());
		assertEquals(metadata.getLastEdited(), result.getLastEdited());
		assertEquals(metadata.getSize(), result.getSize());
	}
	
	@Test
	public void listRoles() throws IOException {
		List<String> roles = Lists.newArrayList("role1", "role2", "role3"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(userStore.listRoles()).thenReturn(roles);
		
		List<String> result = Functions.listRoles();
		assertTrue(result.containsAll(roles));
	}
	
	@Test
	public void getUserAuthorities() throws IOException {
		List<RoleGrantedAuthority> authorities = Lists.newArrayList(
				new RoleGrantedAuthority(GrantedAuthorityTarget.APPLICATION, "role1"), //$NON-NLS-1$
				new RoleGrantedAuthority(new GrantedAuthorityTarget("project", Type.PROJECT), "role2")); //$NON-NLS-1$ //$NON-NLS-2$
		when(userStore.getUserAuthorities("user")).thenReturn(authorities); //$NON-NLS-1$
		
		List<RoleGrantedAuthority> result = Functions.getUserAuthorities("user"); //$NON-NLS-1$
		assertEquals(Sets.newHashSet(authorities), Sets.newHashSet(result));
	}
	
	@Test
	public void formatSize() {
		when(messageSource.getMessage("sizeX.kb", new Object[] { "1.21" }, Locale.US)) //$NON-NLS-1$ //$NON-NLS-2$
			.thenReturn("1.21 KB"); //$NON-NLS-1$
		
		LocaleContextHolder.setLocale(Locale.US);
		String result = Functions.formatSize(1234);
		LocaleContextHolder.resetLocaleContext();
		assertEquals("1.21 KB", result); //$NON-NLS-1$
	}
}
