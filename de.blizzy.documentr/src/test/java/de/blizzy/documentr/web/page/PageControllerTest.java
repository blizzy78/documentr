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
package de.blizzy.documentr.web.page;

import static de.blizzy.documentr.DocumentrMatchers.*;
import static de.blizzy.documentr.TestUtil.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.WebRequest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.Permission;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.markdown.IPageRenderer;
import de.blizzy.documentr.markdown.MarkdownProcessor;
import de.blizzy.documentr.page.CommitCherryPickConflictResolve;
import de.blizzy.documentr.page.CommitCherryPickResult;
import de.blizzy.documentr.page.ICherryPicker;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.page.PageMetadata;
import de.blizzy.documentr.page.PageNotFoundException;
import de.blizzy.documentr.page.PageVersion;
import de.blizzy.documentr.page.TestPageUtil;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.util.Util;

public class PageControllerTest extends AbstractDocumentrTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String PAGE_PATH = DocumentrConstants.HOME_PAGE_NAME + "/page"; //$NON-NLS-1$
	private static final String PAGE_PATH_URL = DocumentrConstants.HOME_PAGE_NAME + ",page"; //$NON-NLS-1$
	private static final String PAGE_NAME = "page"; //$NON-NLS-1$
	private static final String PARENT_PAGE = DocumentrConstants.HOME_PAGE_NAME;
	private static final String CONTEXT = "/context"; //$NON-NLS-1$
	private static final User USER = new User("currentUser", "pw", "admin@example.com", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final Locale LOCALE = Locale.ENGLISH;

	@Mock
	private IPageStore pageStore;
	@Mock
	private ICherryPicker cherryPicker;
	@Mock
	private GlobalRepositoryManager repoManager;
	@Mock
	private UserStore userStore;
	@Mock
	private IPageRenderer pageRenderer;
	@Mock
	private MarkdownProcessor markdownProcessor;
	@Mock
	private Authentication authenticatedAuthentication;
	@Mock
	private Authentication anonymousAuthentication;
	@Mock
	private HttpSession session;
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private Model model;
	@Mock
	private DocumentrPermissionEvaluator permissionEvaluator;
	@Mock
	private PageMetadata pageMetadata;
	@Mock
	private WebRequest webRequest;
	@InjectMocks
	private PageController pageController;

	@Before
	public void setUp() throws IOException {
		when(userStore.getUser(USER.getLoginName())).thenReturn(USER);

		when(authenticatedAuthentication.isAuthenticated()).thenReturn(true);
		when(authenticatedAuthentication.getName()).thenReturn(USER.getLoginName());

		when(authenticatedAuthentication.isAuthenticated()).thenReturn(false);
	}

	@Test
	public void getPage() throws IOException {
		when(session.getAttribute("authenticationCreationTime")).thenReturn(System.currentTimeMillis()); //$NON-NLS-1$

		when(request.getDateHeader(anyString())).thenReturn(-1L);
		when(request.getSession()).thenReturn(session);

		getPage(request);
	}

	@Test
	public void getPageMustReturnNormallyIfModified() throws IOException {
		when(session.getAttribute("authenticationCreationTime")).thenReturn(System.currentTimeMillis()); //$NON-NLS-1$

		when(request.getDateHeader("If-Modified-Since")).thenReturn( //$NON-NLS-1$
				new GregorianCalendar(2000, Calendar.JANUARY, 1).getTimeInMillis());
		when(request.getSession()).thenReturn(session);

		getPage(request);
	}

	private void getPage(HttpServletRequest request) throws IOException {
		Date lastModified = new Date();
		when(pageStore.getPageMetadata(PROJECT, BRANCH, PAGE_PATH)).thenReturn(
				new PageMetadata("user", lastModified, 123, "commit")); //$NON-NLS-1$ //$NON-NLS-2$

		Page page = Page.fromText("title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		page.setViewRestrictionRole("viewRole"); //$NON-NLS-1$
		TestPageUtil.setParentPagePath(page, PARENT_PAGE);
		when(pageStore.getPage(PROJECT, BRANCH, PAGE_PATH, false)).thenReturn(page);

		SecurityContextHolder.setContext(createSecurityContext(anonymousAuthentication));
		String view = pageController.getPage(PROJECT, BRANCH, PAGE_PATH_URL, model, request, response);
		SecurityContextHolder.clearContext();
		assertEquals("/project/branch/page/view", view); //$NON-NLS-1$

		verify(model).addAttribute("path", PAGE_PATH); //$NON-NLS-1$
		verify(model).addAttribute("pageName", PAGE_NAME); //$NON-NLS-1$
		verify(model).addAttribute("parentPagePath", PARENT_PAGE); //$NON-NLS-1$
		verify(model).addAttribute("title", page.getTitle()); //$NON-NLS-1$
		verify(model).addAttribute("viewRestrictionRole", page.getViewRestrictionRole()); //$NON-NLS-1$
		verify(response).setDateHeader("Last-Modified", lastModified.getTime()); //$NON-NLS-1$
	}

	@Test
	public void getPageMustReturn404IfNotFound() throws IOException {
		when(request.getDateHeader(anyString())).thenReturn(-1L);

		when(pageStore.getPageMetadata(eq(PROJECT), eq(BRANCH), eq("nonexistent"))) //$NON-NLS-1$
			.thenThrow(new PageNotFoundException(PROJECT, BRANCH, "nonexistent")); //$NON-NLS-1$

		SecurityContextHolder.setContext(createSecurityContext(authenticatedAuthentication));
		String view = pageController.getPage(PROJECT, BRANCH, "nonexistent", model, request, response); //$NON-NLS-1$
		SecurityContextHolder.clearContext();
		assertEquals("/error/" + HttpServletResponse.SC_NOT_FOUND + "/page.notFound", removeViewPrefix(view)); //$NON-NLS-1$ //$NON-NLS-2$
		assertForward(view);
	}

	@Test
	public void getPageMustReturn304IfNotModified() throws IOException {
		when(session.getAttribute("authenticationCreationTime")).thenReturn( //$NON-NLS-1$
				new GregorianCalendar(2012, Calendar.JUNE, 2).getTime().getTime());

		when(request.getDateHeader("If-Modified-Since")).thenReturn( //$NON-NLS-1$
				new GregorianCalendar(2012, Calendar.JUNE, 9).getTimeInMillis());
		when(request.getSession()).thenReturn(session);

		when(pageStore.getPageMetadata(eq(PROJECT), eq(BRANCH), eq("nonexistent"))) //$NON-NLS-1$
			.thenReturn(new PageMetadata("user", new GregorianCalendar(2012, Calendar.JUNE, 1).getTime(), 123, "commit")); //$NON-NLS-1$ //$NON-NLS-2$

		TestPageUtil.clearProjectEditTimes();

		SecurityContextHolder.setContext(createSecurityContext(anonymousAuthentication));
		String view = pageController.getPage(PROJECT, BRANCH, "nonexistent", model, request, response); //$NON-NLS-1$
		SecurityContextHolder.clearContext();
		assertTrue(removeViewPrefix(view).startsWith("/error/" + HttpServletResponse.SC_NOT_MODIFIED + "/")); //$NON-NLS-1$ //$NON-NLS-2$
		assertForward(view);
	}

	@Test
	public void createPage() {
		String view = pageController.createPage(PROJECT, BRANCH, PARENT_PAGE, model);
		assertEquals("/project/branch/page/edit", view); //$NON-NLS-1$

		verify(model).addAttribute(eq("pageForm"), //$NON-NLS-1$
				argPageForm(PROJECT, BRANCH, null, PARENT_PAGE, null, null, null));
	}

	@Test
	public void editPage() throws IOException {
		Page page = Page.fromText("title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		TestPageUtil.setParentPagePath(page, PARENT_PAGE);
		when(pageStore.getPage(PROJECT, BRANCH, PAGE_PATH, true)).thenReturn(page);
		when(pageStore.getPageMetadata(PROJECT, BRANCH, PAGE_PATH))
			.thenReturn(new PageMetadata("user", new Date(), 123, "commit")); //$NON-NLS-1$ //$NON-NLS-2$

		String view = pageController.editPage(PROJECT, BRANCH, PAGE_PATH_URL, model, session);
		assertEquals("/project/branch/page/edit", view); //$NON-NLS-1$

		verify(model).addAttribute(eq("pageForm"), //$NON-NLS-1$
				argPageForm(PROJECT, BRANCH, PAGE_PATH, PARENT_PAGE, "title", "text", "commit")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Test
	public void editPageButNonexistent() throws IOException {
		when(pageStore.getPage(eq(PROJECT), eq(BRANCH), eq("nonexistent"), anyBoolean())) //$NON-NLS-1$
			.thenThrow(new PageNotFoundException(PROJECT, BRANCH, "nonexistent")); //$NON-NLS-1$

		String view = pageController.editPage(PROJECT, BRANCH, "nonexistent", model, session); //$NON-NLS-1$
		assertEquals("/error/" + HttpServletResponse.SC_NOT_FOUND + "/page.notFound", removeViewPrefix(view)); //$NON-NLS-1$ //$NON-NLS-2$
		assertForward(view);
	}

	@Test
	public void savePage() throws IOException {
		when(repoManager.listProjectBranches(PROJECT)).thenReturn(Collections.singletonList(BRANCH));
		PageForm pageForm = new PageForm(PROJECT, BRANCH, PAGE_PATH, PARENT_PAGE, "title", "text", null, null, //$NON-NLS-1$ //$NON-NLS-2$
			ArrayUtils.EMPTY_STRING_ARRAY);
		BindingResult bindingResult = new BeanPropertyBindingResult(pageForm, "pageForm"); //$NON-NLS-1$

		String view = pageController.savePage(pageForm, bindingResult, model, authenticatedAuthentication);
		assertEquals("/page/" + PROJECT + "/" + BRANCH + "/" + PAGE_PATH_URL, removeViewPrefix(view)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertRedirect(view);
		assertFalse(bindingResult.hasErrors());

		verify(pageStore).savePage(eq(PROJECT), eq(BRANCH), eq(PAGE_PATH),
				argPage("title", "text"), isNull(String.class), same(USER)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void savePageWithViewRestrictionRole() throws IOException {
		when(repoManager.listProjectBranches(PROJECT)).thenReturn(Collections.singletonList(BRANCH));
		PageForm pageForm = new PageForm(PROJECT, BRANCH, PAGE_PATH, PARENT_PAGE, "title", "text", "viewRole", null, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			ArrayUtils.EMPTY_STRING_ARRAY);
		BindingResult bindingResult = new BeanPropertyBindingResult(pageForm, "pageForm"); //$NON-NLS-1$

		String view = pageController.savePage(pageForm, bindingResult, model, authenticatedAuthentication);
		assertEquals("/page/" + PROJECT + "/" + BRANCH + "/" + PAGE_PATH_URL, removeViewPrefix(view)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertRedirect(view);
		assertFalse(bindingResult.hasErrors());

		verify(pageStore).savePage(eq(PROJECT), eq(BRANCH), eq(PAGE_PATH),
				argPage(ANY, "title", "text", "viewRole"), isNull(String.class), same(USER)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Test
	public void savePageMustNotChangeExistingPath() throws IOException {
		when(repoManager.listProjectBranches(PROJECT)).thenReturn(Collections.singletonList(BRANCH));
		PageForm pageForm = new PageForm(PROJECT, BRANCH, PAGE_PATH, PARENT_PAGE, "title", "text", null, null, //$NON-NLS-1$ //$NON-NLS-2$
			ArrayUtils.EMPTY_STRING_ARRAY);
		BindingResult bindingResult = new BeanPropertyBindingResult(pageForm, "pageForm"); //$NON-NLS-1$
		pageController.savePage(pageForm, bindingResult, model, authenticatedAuthentication);

		Page page = Page.fromText("title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		when(pageStore.getPage(PROJECT, BRANCH, PAGE_PATH, true)).thenReturn(page);
		pageForm = new PageForm(PROJECT, BRANCH, PAGE_PATH, PARENT_PAGE, "title2", "text2", null, null, //$NON-NLS-1$ //$NON-NLS-2$
			ArrayUtils.EMPTY_STRING_ARRAY);
		bindingResult = new BeanPropertyBindingResult(pageForm, "pageForm"); //$NON-NLS-1$

		String view = pageController.savePage(pageForm, bindingResult, model, authenticatedAuthentication);
		assertEquals("/page/" + PROJECT + "/" + BRANCH + "/" + PAGE_PATH_URL, removeViewPrefix(view)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertRedirect(view);
		assertFalse(bindingResult.hasErrors());

		verify(pageStore).savePage(eq(PROJECT), eq(BRANCH), eq(PAGE_PATH),
				argPage("title2", "text2"), isNull(String.class), same(USER)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void savePageBlankPath() throws IOException {
		when(repoManager.listProjectBranches(PROJECT)).thenReturn(Collections.singletonList(BRANCH));
		String title = "title"; //$NON-NLS-1$
		PageForm pageForm = new PageForm(PROJECT, BRANCH, StringUtils.EMPTY, PARENT_PAGE, title, "text", null, null, //$NON-NLS-1$
			ArrayUtils.EMPTY_STRING_ARRAY);
		BindingResult bindingResult = new BeanPropertyBindingResult(pageForm, "pageForm"); //$NON-NLS-1$

		String view = pageController.savePage(pageForm, bindingResult, model, authenticatedAuthentication);
		String path = PARENT_PAGE + "/" + Util.simplifyForUrl(title); //$NON-NLS-1$
		String pathUrl = Util.toUrlPagePath(path);
		assertEquals("/page/" + PROJECT + "/" + BRANCH + "/" + pathUrl, removeViewPrefix(view)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertRedirect(view);
		assertFalse(bindingResult.hasErrors());

		verify(pageStore).savePage(eq(PROJECT), eq(BRANCH), eq(path),
				argPage(title, "text"), isNull(String.class), same(USER)); //$NON-NLS-1$
	}

	@Test
	public void savePageShouldDoNothingIfNoChanges() throws IOException {
		when(repoManager.listProjectBranches(PROJECT)).thenReturn(Collections.singletonList(BRANCH));
		Page page = Page.fromText("title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		when(pageStore.getPage(PROJECT, BRANCH, PAGE_PATH, true)).thenReturn(page);

		PageForm pageForm = new PageForm(PROJECT, BRANCH, PAGE_PATH, PARENT_PAGE, "title", "text", null, null, //$NON-NLS-1$ //$NON-NLS-2$
			ArrayUtils.EMPTY_STRING_ARRAY);
		BindingResult bindingResult = new BeanPropertyBindingResult(pageForm, "pageForm"); //$NON-NLS-1$
		pageController.savePage(pageForm, bindingResult, model, authenticatedAuthentication);

		verify(pageStore, never()).savePage(
				anyString(), anyString(), anyString(), Matchers.<Page>any(), anyString(), Matchers.<User>any());
	}

	@Test
	public void savePageButNonexistentBranch() throws IOException {
		when(repoManager.listProjectBranches(PROJECT)).thenReturn(Collections.singletonList(BRANCH));
		PageForm pageForm = new PageForm(PROJECT, "nonexistent", PAGE_PATH, PARENT_PAGE, "title", "text", null, null, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			ArrayUtils.EMPTY_STRING_ARRAY);
		BindingResult bindingResult = new BeanPropertyBindingResult(pageForm, "pageForm"); //$NON-NLS-1$

		String view = pageController.savePage(pageForm, bindingResult, model, authenticatedAuthentication);
		assertEquals("/project/branch/page/edit", view); //$NON-NLS-1$
		assertTrue(bindingResult.hasErrors());
		assertTrue(bindingResult.hasFieldErrors("branchName")); //$NON-NLS-1$
	}

	@Test
	public void generateName() throws IOException {
		String title = "simple as 1, 2, 3"; //$NON-NLS-1$
		String path = PARENT_PAGE + "/" + Util.simplifyForUrl(title); //$NON-NLS-1$
		when(pageStore.getPage(eq(PROJECT), eq(BRANCH), eq(path), anyBoolean()))
			.thenThrow(new PageNotFoundException(PROJECT, BRANCH, path));
		Map<String, Object> result = pageController.generateName(PROJECT, BRANCH, PARENT_PAGE, title);
		assertEquals(path, result.get("path")); //$NON-NLS-1$
		assertEquals(Boolean.FALSE, result.get("exists")); //$NON-NLS-1$

		title = "title"; //$NON-NLS-1$
		path = PARENT_PAGE + "/" + Util.simplifyForUrl(title); //$NON-NLS-1$
		Page page = Page.fromText(title, "text"); //$NON-NLS-1$
		when(pageStore.getPage(PROJECT, BRANCH, path, false)).thenReturn(page);
		result = pageController.generateName(PROJECT, BRANCH, PARENT_PAGE, title);
		assertEquals(path, result.get("path")); //$NON-NLS-1$
		assertEquals(Boolean.TRUE, result.get("exists")); //$NON-NLS-1$
	}

	@Test
	public void markdownToHtml() {
		when(markdownProcessor.markdownToHtml("markdown", PROJECT, BRANCH, PAGE_PATH, //$NON-NLS-1$
				authenticatedAuthentication, CONTEXT)).thenReturn("html"); //$NON-NLS-1$
		when(markdownProcessor.processNonCacheableMacros("html", PROJECT, BRANCH, PAGE_PATH, //$NON-NLS-1$
				authenticatedAuthentication, CONTEXT)).thenReturn("htmlWithMacros"); //$NON-NLS-1$

		when(request.getContextPath()).thenReturn(CONTEXT);

		Map<String, String> result = pageController.markdownToHtml(
				PROJECT, BRANCH, "markdown", PAGE_PATH, authenticatedAuthentication, request); //$NON-NLS-1$
		assertEquals("htmlWithMacros", result.get("html")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void copyToBranch() throws IOException {
		Page page = Page.fromText("title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		when(pageStore.getPage(PROJECT, BRANCH, PAGE_PATH, true)).thenReturn(page);

		String view = pageController.copyToBranch(PROJECT, BRANCH, PAGE_PATH_URL, "targetBranch", //$NON-NLS-1$
				authenticatedAuthentication);
		assertEquals("/page/edit/" + PROJECT + "/targetBranch/" + PAGE_PATH_URL, removeViewPrefix(view)); //$NON-NLS-1$ //$NON-NLS-2$
		assertRedirect(view);

		verify(pageStore).savePage(eq(PROJECT), eq("targetBranch"), eq(PAGE_PATH), //$NON-NLS-1$
				argPage("title", "text"), isNull(String.class), same(USER)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void deletePage() throws IOException {
		String view = pageController.deletePage(PROJECT, BRANCH, PAGE_PATH_URL, authenticatedAuthentication);
		assertEquals("/page/" + PROJECT + "/" + BRANCH + "/" + DocumentrConstants.HOME_PAGE_NAME, removeViewPrefix(view)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertRedirect(view);

		verify(pageStore).deletePage(PROJECT, BRANCH, PAGE_PATH, USER);
	}

	@Test
	public void relocatePage() throws IOException {
		String view = pageController.relocatePage(PROJECT, BRANCH, PAGE_PATH_URL, "home,newparent", //$NON-NLS-1$
				authenticatedAuthentication);
		assertEquals("/page/" + PROJECT + "/" + BRANCH + "/home,newparent,page", removeViewPrefix(view)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertRedirect(view);

		verify(pageStore).relocatePage(PROJECT, BRANCH, PAGE_PATH, "home/newparent", USER); //$NON-NLS-1$
	}

	@Test
	public void getPageMarkdown() throws IOException {
		Set<String> versions = Sets.newHashSet("commit1", "commit2", "nonexistent"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		Map<String, String> markdown = Maps.newHashMap();
		markdown.put("commit1", "md1"); //$NON-NLS-1$ //$NON-NLS-2$
		markdown.put("commit2", "md2"); //$NON-NLS-1$ //$NON-NLS-2$
		when(pageStore.getMarkdown(PROJECT, BRANCH, PAGE_PATH, versions)).thenReturn(markdown);

		Map<String, String> result = pageController.getPageMarkdown(PROJECT, BRANCH, PAGE_PATH_URL, versions);
		assertEquals(markdown, result);
	}

	@Test
	public void getPageMarkdownInRange() throws IOException {
		Page page = Page.fromText("title", "x\ny\nz\n"); //$NON-NLS-1$ //$NON-NLS-2$
		when(pageStore.getPage(PROJECT, BRANCH, PAGE_PATH, "commit", true)).thenReturn(page); //$NON-NLS-1$

		Map<String, String> result = pageController.getPageMarkdownInRange(
				PROJECT, BRANCH, PAGE_PATH_URL, 2, 4, "commit"); //$NON-NLS-1$
		assertEquals("y", result.get("markdown")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void savePageRange() throws IOException {
		Page page = Page.fromText("title", "x\ny\nz\n"); //$NON-NLS-1$ //$NON-NLS-2$
		when(pageStore.getPage(PROJECT, BRANCH, PAGE_PATH, "commit", true)).thenReturn(page); //$NON-NLS-1$

		when(pageRenderer.getHtml(PROJECT, BRANCH, PAGE_PATH, authenticatedAuthentication, CONTEXT))
			.thenReturn("html"); //$NON-NLS-1$

		when(markdownProcessor.processNonCacheableMacros("html", PROJECT, BRANCH, PAGE_PATH, //$NON-NLS-1$
				authenticatedAuthentication, CONTEXT)).thenReturn("htmlWithMacros"); //$NON-NLS-1$

		when(request.getContextPath()).thenReturn(CONTEXT);

		when(pageMetadata.getCommit()).thenReturn("newCommit"); //$NON-NLS-1$
		when(pageStore.getPageMetadata(PROJECT, BRANCH, PAGE_PATH)).thenReturn(pageMetadata);

		Map<String, Object> result = pageController.savePageRange(PROJECT, BRANCH, PAGE_PATH_URL,
				"a\nb\nc\n", "2,4", "commit", authenticatedAuthentication, request); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("htmlWithMacros", result.get("html")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("newCommit", result.get("commit")); //$NON-NLS-1$ //$NON-NLS-2$

		verify(pageStore).savePage(eq(PROJECT), eq(BRANCH), eq(PAGE_PATH),
				argPage("title", "x\na\nb\nc\nz\n"), eq("commit"), same(USER)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Test
	public void getPageChanges() {
		String view = pageController.getPageChanges(PROJECT, BRANCH, PAGE_PATH_URL, model);
		assertEquals("/project/branch/page/changes", view); //$NON-NLS-1$

		verify(model).addAttribute("projectName", PROJECT); //$NON-NLS-1$
		verify(model).addAttribute("branchName", BRANCH); //$NON-NLS-1$
		verify(model).addAttribute("path", PAGE_PATH); //$NON-NLS-1$
	}

	@Test
	public void restoreVersion() throws IOException {
		pageController.restoreVersion(PROJECT, BRANCH, PAGE_PATH_URL, "version", authenticatedAuthentication); //$NON-NLS-1$

		verify(pageStore).restorePageVersion(PROJECT, BRANCH, PAGE_PATH, "version", USER); //$NON-NLS-1$
	}

	@Test
	public void cherryPick() throws IOException {
		when(permissionEvaluator.hasPagePermission(
				authenticatedAuthentication, PROJECT, "targetBranch", PAGE_PATH, Permission.EDIT_PAGE)) //$NON-NLS-1$
				.thenReturn(true);

		when(cherryPicker.getCommitsList(PROJECT, BRANCH, PAGE_PATH, "version2", "version4")) //$NON-NLS-1$ //$NON-NLS-2$
			.thenReturn(Lists.newArrayList("version3", "version4")); //$NON-NLS-1$ //$NON-NLS-2$

		@SuppressWarnings("nls")
		List<CommitCherryPickResult> branchResults = Lists.newArrayList(
				new CommitCherryPickResult(new PageVersion("version3", "user", new Date()),
						CommitCherryPickResult.Status.OK),
				new CommitCherryPickResult(new PageVersion("version3", "user", new Date()),
						CommitCherryPickResult.Status.OK));
		SortedMap<String, List<CommitCherryPickResult>> results = Maps.newTreeMap();
		results.put("targetBranch", branchResults); //$NON-NLS-1$
		when(cherryPicker.cherryPick(PROJECT, BRANCH, PAGE_PATH, Lists.newArrayList("version3", "version4"), //$NON-NLS-1$ //$NON-NLS-2$
				Sets.newHashSet("targetBranch"), Collections.<CommitCherryPickConflictResolve>emptySet(), false, //$NON-NLS-1$
				USER, LOCALE))
				.thenReturn(results);

		String view = pageController.cherryPick(PROJECT, BRANCH, PAGE_PATH, "version2", "version4", //$NON-NLS-1$ //$NON-NLS-2$
				Sets.newHashSet("targetBranch"), false, webRequest, model, authenticatedAuthentication, LOCALE); //$NON-NLS-1$
		assertEquals("/page/" + PROJECT + "/" + BRANCH + "/" + PAGE_PATH_URL, removeViewPrefix(view)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertRedirect(view);
	}

	@Test
	public void cherryPickWithConflicts() throws IOException {
		when(permissionEvaluator.hasPagePermission(
				authenticatedAuthentication, PROJECT, "targetBranch", PAGE_PATH, Permission.EDIT_PAGE)) //$NON-NLS-1$
				.thenReturn(true);

		when(cherryPicker.getCommitsList(PROJECT, BRANCH, PAGE_PATH, "version2", "version4")) //$NON-NLS-1$ //$NON-NLS-2$
			.thenReturn(Lists.newArrayList("version3", "version4")); //$NON-NLS-1$ //$NON-NLS-2$

		@SuppressWarnings("nls")
		List<CommitCherryPickResult> branchResults = Lists.newArrayList(
				new CommitCherryPickResult(new PageVersion("version3", "user", new Date()),
						CommitCherryPickResult.Status.OK),
				new CommitCherryPickResult(new PageVersion("version3", "user", new Date()),
						"conflictText"));
		SortedMap<String, List<CommitCherryPickResult>> results = Maps.newTreeMap();
		results.put("targetBranch", branchResults); //$NON-NLS-1$
		when(cherryPicker.cherryPick(PROJECT, BRANCH, PAGE_PATH, Lists.newArrayList("version3", "version4"), //$NON-NLS-1$ //$NON-NLS-2$
				Sets.newHashSet("targetBranch"), Collections.<CommitCherryPickConflictResolve>emptySet(), false, //$NON-NLS-1$
				USER, LOCALE))
				.thenReturn(results);

		String view = pageController.cherryPick(PROJECT, BRANCH, PAGE_PATH, "version2", "version4", //$NON-NLS-1$ //$NON-NLS-2$
				Sets.newHashSet("targetBranch"), false, webRequest, model, authenticatedAuthentication, LOCALE); //$NON-NLS-1$
		assertEquals("/project/branch/page/cherryPick", view); //$NON-NLS-1$

		verify(model).addAttribute("cherryPickResults", results); //$NON-NLS-1$
		verify(model).addAttribute("version1", "version2"); //$NON-NLS-1$ //$NON-NLS-2$
		verify(model).addAttribute("version2", "version4"); //$NON-NLS-1$ //$NON-NLS-2$
		verify(model).addAttribute("resolves", Collections.emptySet()); //$NON-NLS-1$
	}

	@Test
	public void cherryPickWithConflictsAndResolveTexts() throws IOException {
		when(permissionEvaluator.hasPagePermission(
				authenticatedAuthentication, PROJECT, "targetBranch", PAGE_PATH, Permission.EDIT_PAGE)) //$NON-NLS-1$
				.thenReturn(true);

		when(cherryPicker.getCommitsList(PROJECT, BRANCH, PAGE_PATH, "version2", "version4")) //$NON-NLS-1$ //$NON-NLS-2$
			.thenReturn(Lists.newArrayList("version3", "version4")); //$NON-NLS-1$ //$NON-NLS-2$

		@SuppressWarnings("nls")
		Set<CommitCherryPickConflictResolve> resolves = Sets.newHashSet(
				new CommitCherryPickConflictResolve("targetBranch", "version3", "resolveText"));
		@SuppressWarnings("nls")
		List<CommitCherryPickResult> branchResults = Lists.newArrayList(
				new CommitCherryPickResult(new PageVersion("version3", "user", new Date()),
						CommitCherryPickResult.Status.OK),
				new CommitCherryPickResult(new PageVersion("version3", "user", new Date()),
						CommitCherryPickResult.Status.OK));
		SortedMap<String, List<CommitCherryPickResult>> results = Maps.newTreeMap();
		results.put("targetBranch", branchResults); //$NON-NLS-1$
		when(cherryPicker.cherryPick(PROJECT, BRANCH, PAGE_PATH, Lists.newArrayList("version3", "version4"), //$NON-NLS-1$ //$NON-NLS-2$
				Sets.newHashSet("targetBranch"), resolves, false, USER, LOCALE)) //$NON-NLS-1$
				.thenReturn(results);

		Map<String, String[]> params = Maps.newHashMap();
		params.put("resolveText_targetBranch/version3", new String[] { "resolveText" }); //$NON-NLS-1$ //$NON-NLS-2$
		when(webRequest.getParameterMap()).thenReturn(params);

		String view = pageController.cherryPick(PROJECT, BRANCH, PAGE_PATH, "version2", "version4", //$NON-NLS-1$ //$NON-NLS-2$
				Sets.newHashSet("targetBranch"), false, webRequest, model, authenticatedAuthentication, LOCALE); //$NON-NLS-1$
		assertEquals("/page/" + PROJECT + "/" + BRANCH + "/" + PAGE_PATH_URL, removeViewPrefix(view)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertRedirect(view);
	}
}
