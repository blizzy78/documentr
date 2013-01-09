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
package de.blizzy.documentr.web;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.TestUtil;
import de.blizzy.documentr.access.GrantedAuthorityTarget;
import de.blizzy.documentr.access.GrantedAuthorityTarget.Type;
import de.blizzy.documentr.access.OpenId;
import de.blizzy.documentr.access.RoleGrantedAuthority;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.markdown.IPageRenderer;
import de.blizzy.documentr.markdown.MarkdownProcessor;
import de.blizzy.documentr.markdown.macro.IMacroDescriptor;
import de.blizzy.documentr.markdown.macro.MacroFactory;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.page.PageMetadata;
import de.blizzy.documentr.page.PageNotFoundException;
import de.blizzy.documentr.page.PageVersion;
import de.blizzy.documentr.repository.GlobalRepositoryManager;

public class FunctionsTest extends AbstractDocumentrTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String PAGE = "page"; //$NON-NLS-1$
	private static final String CONTEXT = "/context"; //$NON-NLS-1$
	private static final Locale LOCALE = Locale.US;

	@Mock
	private GlobalRepositoryManager repoManager;
	@Mock
	private IPageStore pageStore;
	@Mock
	private UserStore userStore;
	@Mock
	private IPageRenderer pageRenderer;
	@Mock
	private MarkdownProcessor markdownProcessor;
	@Mock
	private Authentication authentication;
	@Mock
	private MessageSource messageSource;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private MacroFactory macroFactory;
	@Mock
	private HttpServletRequest request;
	@Mock
	private IMacroDescriptor descriptor1;
	@Mock
	private IMacroDescriptor descriptor2;
	@Mock
	private PageMetadata pageMetadata;

	@Before
	public void setUp() {
		Functions.setGlobalRepositoryManager(repoManager);
		Functions.setPageStore(pageStore);
		Functions.setUserStore(userStore);
		Functions.setPageRenderer(pageRenderer);
		Functions.setMarkdownProcessor(markdownProcessor);
		Functions.setMessageSource(messageSource);
		Functions.setMacroFactory(macroFactory);

		when(securityContext.getAuthentication()).thenReturn(authentication);

		SecurityContextHolder.setContext(securityContext);

		LocaleContextHolder.setLocale(LOCALE);

		ServletRequestAttributes requestAttrs = new ServletRequestAttributes(request);
		RequestContextHolder.setRequestAttributes(requestAttrs);
	}

	@After
	public void tearDown() {
		Functions.setGlobalRepositoryManager(null);
		Functions.setPageStore(null);
		Functions.setUserStore(null);
		Functions.setPageRenderer(null);
		Functions.setMarkdownProcessor(null);
		Functions.setMessageSource(null);
		Functions.setMacroFactory(null);
		SecurityContextHolder.clearContext();
		LocaleContextHolder.resetLocaleContext();
		RequestContextHolder.resetRequestAttributes();
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
	public void getPageHtml() throws IOException {
		when(pageRenderer.getHtml(PROJECT, BRANCH, PAGE, authentication, CONTEXT)).thenReturn("html"); //$NON-NLS-1$
		when(markdownProcessor.processNonCacheableMacros("html", PROJECT, BRANCH, PAGE, //$NON-NLS-1$
				authentication, CONTEXT)).thenReturn("htmlWithMacros"); //$NON-NLS-1$
		when(request.getContextPath()).thenReturn(CONTEXT);
		assertEquals("htmlWithMacros", Functions.getPageHtml(PROJECT, BRANCH, PAGE)); //$NON-NLS-1$
	}

	@Test
	public void getPageHeaderHtml() throws IOException {
		when(pageRenderer.getHeaderHtml(PROJECT, BRANCH, PAGE, authentication, CONTEXT))
			.thenReturn("headerHtml"); //$NON-NLS-1$
		when(markdownProcessor.processNonCacheableMacros("headerHtml", PROJECT, BRANCH, PAGE, //$NON-NLS-1$
				authentication, CONTEXT)).thenReturn("headerHtmlWithMacros"); //$NON-NLS-1$
		when(request.getContextPath()).thenReturn(CONTEXT);
		assertEquals("headerHtmlWithMacros", Functions.getPageHeaderHtml(PROJECT, BRANCH, PAGE)); //$NON-NLS-1$
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
		PageMetadata metadata = new PageMetadata("user", date, 123, "commit"); //$NON-NLS-1$ //$NON-NLS-2$
		when(pageStore.getPageMetadata(PROJECT, BRANCH, PAGE)).thenReturn(metadata);

		PageMetadata result = Functions.getPageMetadata(PROJECT, BRANCH, PAGE);
		assertEquals(metadata.getLastEditedBy(), result.getLastEditedBy());
		assertEquals(metadata.getLastEdited(), result.getLastEdited());
		assertEquals(metadata.getSize(), result.getSize());
		assertEquals(metadata.getCommit(), result.getCommit());
	}

	@Test
	public void getAttachmentMetadata() throws IOException {
		Date date = new Date();
		PageMetadata metadata = new PageMetadata("user", date, 123, "commit"); //$NON-NLS-1$ //$NON-NLS-2$
		when(pageStore.getAttachmentMetadata(PROJECT, BRANCH, PAGE, "test.png")).thenReturn(metadata); //$NON-NLS-1$

		PageMetadata result = Functions.getAttachmentMetadata(PROJECT, BRANCH, PAGE, "test.png"); //$NON-NLS-1$
		assertEquals(metadata.getLastEditedBy(), result.getLastEditedBy());
		assertEquals(metadata.getLastEdited(), result.getLastEdited());
		assertEquals(metadata.getSize(), result.getSize());
		assertEquals(metadata.getCommit(), result.getCommit());
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

		String result = Functions.formatSize(1234);
		assertEquals("1.21 KB", result); //$NON-NLS-1$
	}

	@Test
	public void listPageVersions() throws IOException {
		List<PageVersion> versions = Lists.newArrayList(new PageVersion("commit", "user", new Date())); //$NON-NLS-1$ //$NON-NLS-2$
		when(pageStore.listPageVersions(PROJECT, BRANCH, PAGE)).thenReturn(versions);
		assertEquals(versions, Functions.listPageVersions(PROJECT, BRANCH, PAGE));
	}

	@Test
	public void listMyOpenIds() throws IOException {
		when(authentication.getName()).thenReturn("user"); //$NON-NLS-1$

		User user = new User("user", "p", "email", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		Set<OpenId> openIds = Sets.newHashSet();
		openIds.add(new OpenId("openId1", "realOpenId1")); //$NON-NLS-1$ //$NON-NLS-2$
		openIds.add(new OpenId("openId2", "realOpenId2")); //$NON-NLS-1$ //$NON-NLS-2$
		for (OpenId openId : openIds) {
			user.addOpenId(openId);
		}
		when(userStore.getUser("user")).thenReturn(user); //$NON-NLS-1$

		List<OpenId> result = Functions.listMyOpenIds();
		assertEquals(openIds, Sets.newHashSet(result));
	}

	@Test
	public void floor() {
		assertEquals(3, Functions.floor(3.141592654d));
	}

	@Test
	public void getMacros() {
		when(descriptor1.getTitle(LOCALE)).thenReturn("title1"); //$NON-NLS-1$
		when(descriptor1.getDescription(LOCALE)).thenReturn("description1"); //$NON-NLS-1$
		when(descriptor1.getInsertText()).thenReturn("insertText1"); //$NON-NLS-1$

		when(descriptor2.getTitle(LOCALE)).thenReturn("title2"); //$NON-NLS-1$
		when(descriptor2.getDescription(LOCALE)).thenReturn("description2"); //$NON-NLS-1$
		when(descriptor2.getInsertText()).thenReturn("insertText2"); //$NON-NLS-1$

		Set<IMacroDescriptor> descs = Sets.newHashSet(descriptor1, descriptor2);
		when(macroFactory.getDescriptors()).thenReturn(descs);

		List<JspMacroDescriptor> result = Functions.getMacros();
		assertEquals(2, result.size());
		assertEquals(descriptor1.getTitle(LOCALE), result.get(0).getTitle());
		assertEquals(descriptor1.getDescription(LOCALE), result.get(0).getDescription());
		assertEquals(descriptor1.getInsertText(), result.get(0).getInsertText());
		assertEquals(descriptor2.getTitle(LOCALE), result.get(1).getTitle());
		assertEquals(descriptor2.getDescription(LOCALE), result.get(1).getDescription());
		assertEquals(descriptor2.getInsertText(), result.get(1).getInsertText());
	}

	@Test
	public void getLanguage() {
		assertEquals(LOCALE.getLanguage(), Functions.getLanguage());
	}

	@Test
	public void escapeJavaScript() {
		assertEquals(StringEscapeUtils.escapeEcmaScript("\"'{}"), Functions.escapeJavaScript("\"'{}")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	@SuppressWarnings("unchecked")
	public void pageExists() throws IOException {
		when(pageStore.getPageMetadata(PROJECT, BRANCH, PAGE)).thenReturn(pageMetadata);
		assertTrue(Functions.pageExists(PROJECT, BRANCH, PAGE));

		when(pageStore.getPageMetadata(PROJECT, BRANCH, PAGE)).thenThrow(PageNotFoundException.class);
		assertFalse(Functions.pageExists(PROJECT, BRANCH, PAGE));
	}

	@Test
	public void getGroovyMacros() {
		List<String> macros = Lists.newArrayList("macro1", "macro2"); //$NON-NLS-1$ //$NON-NLS-2$
		when(macroFactory.listGroovyMacros()).thenReturn(macros);

		List<String> result = Functions.getGroovyMacros();
		assertEquals(macros, result);
	}
}
