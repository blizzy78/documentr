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
package de.blizzy.documentr.web.page;

import static de.blizzy.documentr.TestUtil.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import de.blizzy.documentr.Util;
import de.blizzy.documentr.pagestore.Page;
import de.blizzy.documentr.pagestore.PageStore;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.web.markdown.macro.MacroFactory;

public class PageControllerTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String PAGE_PATH = "home/page"; //$NON-NLS-1$
	private static final String PAGE_PATH_URL = "home,page"; //$NON-NLS-1$
	private static final String PAGE_NAME = "page"; //$NON-NLS-1$
	private static final String PARENT_PAGE = "home"; //$NON-NLS-1$
	
	private PageStore pageStore;
	private GlobalRepositoryManager repoManager;
	private PageController pageController;

	@Before
	public void setUp() {
		pageStore = mock(PageStore.class);
		repoManager = mock(GlobalRepositoryManager.class);
		MacroFactory macroFactory = mock(MacroFactory.class);
		
		pageController = new PageController();
		pageController.setPageStore(pageStore);
		pageController.setGlobalRepositoryManager(repoManager);
		pageController.setMacroFactory(macroFactory);
	}
	
	@Test
	public void getPage() throws IOException {
		Page page = Page.fromText(PARENT_PAGE, "title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		when(pageStore.getPage(PROJECT, BRANCH, PAGE_PATH)).thenReturn(page);
		
		Model model = mock(Model.class);
		String view = pageController.getPage(PROJECT, BRANCH, PAGE_PATH_URL, model);
		assertEquals("/project/branch/page/view", view); //$NON-NLS-1$
		
		verify(model).addAttribute("path", PAGE_PATH); //$NON-NLS-1$
		verify(model).addAttribute("pageName", PAGE_NAME); //$NON-NLS-1$
		verify(model).addAttribute("parentPagePath", PARENT_PAGE); //$NON-NLS-1$
		verify(model).addAttribute("title", page.getTitle()); //$NON-NLS-1$
		verify(model).addAttribute("text", page.getText()); //$NON-NLS-1$
	}
	
	@Test
	public void createPage() {
		Model model = mock(Model.class);
		String view = pageController.createPage(PROJECT, BRANCH, PARENT_PAGE, model);
		assertEquals("/project/branch/page/edit", view); //$NON-NLS-1$
		
		verify(model).addAttribute(eq("pageForm"), //$NON-NLS-1$
				argPageForm(PROJECT, BRANCH, StringUtils.EMPTY, PARENT_PAGE, StringUtils.EMPTY,
						StringUtils.EMPTY));
	}
	
	@Test
	public void editPage() throws IOException {
		Page page = Page.fromText(PARENT_PAGE, "title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		when(pageStore.getPage(PROJECT, BRANCH, PAGE_PATH)).thenReturn(page);
		
		Model model = mock(Model.class);
		String view = pageController.editPage(PROJECT, BRANCH, PAGE_PATH_URL, model);
		assertEquals("/project/branch/page/edit", view); //$NON-NLS-1$
		
		verify(model).addAttribute(eq("pageForm"), //$NON-NLS-1$
				argPageForm(PROJECT, BRANCH, PAGE_PATH, PARENT_PAGE, "title", "text")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Test
	public void savePage() throws IOException {
		when(repoManager.listProjectBranches(PROJECT)).thenReturn(Collections.singletonList(BRANCH));
		PageForm pageForm = new PageForm(PROJECT, BRANCH, PAGE_PATH, PARENT_PAGE, "title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		BindingResult bindingResult = new BeanPropertyBindingResult(pageForm, "pageForm"); //$NON-NLS-1$
		
		String view = pageController.savePage(pageForm, bindingResult);
		assertEquals("/page/" + PROJECT + "/" + BRANCH + "/" + PAGE_PATH_URL, removeViewPrefix(view)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertRedirect(view);
		assertFalse(bindingResult.hasErrors());
		
		verify(pageStore).savePage(eq(PROJECT), eq(BRANCH), eq(PAGE_PATH),
				argPage(PARENT_PAGE, "title", "text")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Test
	public void savePageShouldDoNothingIfNoChanges() throws IOException {
		when(repoManager.listProjectBranches(PROJECT)).thenReturn(Collections.singletonList(BRANCH));
		Page page = Page.fromText(PARENT_PAGE, "title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		when(pageStore.getPage(PROJECT, BRANCH, PAGE_PATH)).thenReturn(page);
		
		PageForm pageForm = new PageForm(PROJECT, BRANCH, PAGE_PATH, PARENT_PAGE, "title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		BindingResult bindingResult = new BeanPropertyBindingResult(pageForm, "pageForm"); //$NON-NLS-1$
		pageController.savePage(pageForm, bindingResult);

		verify(pageStore, never()).savePage(anyString(), anyString(), anyString(), Matchers.<Page>any());
	}
	
	@Test
	public void generateName() throws IOException {
		String title = "simple as 1, 2, 3"; //$NON-NLS-1$
		Map<String, Object> result = pageController.generateName(PROJECT, BRANCH, PARENT_PAGE, title);
		assertEquals(PARENT_PAGE + "/" + Util.simplifyForURL(title), result.get("path")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals(Boolean.FALSE, result.get("exists")); //$NON-NLS-1$

		title = "title"; //$NON-NLS-1$
		Page page = Page.fromText(PARENT_PAGE, title, "text"); //$NON-NLS-1$
		when(pageStore.getPage(PROJECT, BRANCH, PARENT_PAGE + "/" + Util.simplifyForURL(title))).thenReturn(page); //$NON-NLS-1$
		result = pageController.generateName(PROJECT, BRANCH, PARENT_PAGE, title);
		assertEquals(PARENT_PAGE + "/" + Util.simplifyForURL(title), result.get("path")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals(Boolean.TRUE, result.get("exists")); //$NON-NLS-1$
	}
	
	@Test
	public void markdownToHTML() {
		Map<String, String> result = pageController.markdownToHTML(PROJECT, BRANCH, "**foo**", PAGE_PATH); //$NON-NLS-1$
		assertEquals("<p><strong>foo</strong></p>", result.get("html")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Test
	public void copyToBranch() throws IOException {
		Page page = Page.fromText(PARENT_PAGE, "title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		when(pageStore.getPage(PROJECT, BRANCH, PAGE_PATH)).thenReturn(page);
		
		String view = pageController.copyToBranch(PROJECT, BRANCH, PAGE_PATH_URL, "targetBranch"); //$NON-NLS-1$
		assertEquals("/page/edit/" + PROJECT + "/targetBranch/" + PAGE_PATH_URL, removeViewPrefix(view)); //$NON-NLS-1$ //$NON-NLS-2$
		assertRedirect(view);
		
		verify(pageStore).savePage(eq(PROJECT), eq("targetBranch"), eq(PAGE_PATH), //$NON-NLS-1$
				argPage(PARENT_PAGE, "title", "text")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Test
	public void deletePage() throws IOException {
		String view = pageController.deletePage(PROJECT, BRANCH, PAGE_PATH_URL);
		assertEquals("/page/" + PROJECT + "/" + BRANCH + "/home", removeViewPrefix(view)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertRedirect(view);
		
		verify(pageStore).deletePage(PROJECT, BRANCH, PAGE_PATH);
	}
	
	private PageForm argPageForm(final String projectName, final String branchName, final String path,
			final String parentPagePath, final String title, final String text) {
		
		Matcher<PageForm> matcher = new ArgumentMatcher<PageForm>() {
			@Override
			public boolean matches(Object argument) {
				PageForm form = (PageForm) argument;
				return StringUtils.equals(form.getProjectName(), projectName) &&
						StringUtils.equals(form.getBranchName(), branchName) &&
						StringUtils.equals(form.getPath(), path) &&
						StringUtils.equals(form.getParentPagePath(), parentPagePath) &&
						StringUtils.equals(form.getTitle(), title) &&
						StringUtils.equals(form.getText(), text);
			}
		};
		return argThat(matcher);
	}
	
	private Page argPage(final String parentPagePath, final String title, final String text) {
		Matcher<Page> matcher = new ArgumentMatcher<Page>() {
			@Override
			public boolean matches(Object argument) {
				Page page = (Page) argument;
				return StringUtils.equals(page.getParentPagePath(), parentPagePath) &&
						StringUtils.equals(page.getTitle(), title) &&
						StringUtils.equals(page.getText(), text);
			}
		};
		return argThat(matcher);
	}
}
