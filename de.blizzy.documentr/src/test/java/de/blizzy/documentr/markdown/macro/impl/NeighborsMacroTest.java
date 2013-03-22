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
package de.blizzy.documentr.markdown.macro.impl;

import static de.blizzy.documentr.DocumentrMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.security.core.Authentication;

import com.google.common.collect.Lists;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.Permission;
import de.blizzy.documentr.markdown.HtmlSerializerContext;
import de.blizzy.documentr.markdown.macro.IMacroContext;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.page.TestPageUtil;

public class NeighborsMacroTest extends AbstractDocumentrTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String INACCESSIBLE_PAGE_PATH = DocumentrConstants.HOME_PAGE_NAME + "/foo/inaccessible"; //$NON-NLS-1$
	@SuppressWarnings("nls")
	private static final String[] PAGES = {
		DocumentrConstants.HOME_PAGE_NAME,
		DocumentrConstants.HOME_PAGE_NAME + "/foo",
		DocumentrConstants.HOME_PAGE_NAME + "/foo/aaa",
		DocumentrConstants.HOME_PAGE_NAME + "/foo/aaa/a1",
		DocumentrConstants.HOME_PAGE_NAME + "/foo/aaa/a2",
		DocumentrConstants.HOME_PAGE_NAME + "/foo/bar", // <-- page under test
		DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/x",
		DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/x/x1",
		DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/x/x2",
		DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/y",
		DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/y/y1",
		DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/y/y2",
		DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/z",
		DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/z/z1",
		DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/z/z2",
		DocumentrConstants.HOME_PAGE_NAME + "/foo/bbb",
		DocumentrConstants.HOME_PAGE_NAME + "/foo/bbb/b1",
		INACCESSIBLE_PAGE_PATH
	};

	@Mock
	private IPageStore pageStore;
	@Mock
	private HtmlSerializerContext htmlSerializerContext;
	@Mock
	private DocumentrPermissionEvaluator permissionEvaluator;
	@Mock
	private IMacroContext context;
	private List<Page> pages = Lists.newArrayList();
	private NeighborsMacro runnable;

	@Before
	public void setUp() throws IOException {
		when(htmlSerializerContext.getPageUri(anyString())).then(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) {
				return "/" + invocation.getArguments()[0]; //$NON-NLS-1$
			}
		});

		setupPages();

		setupPagePermissions();

		when(context.getPageStore()).thenReturn(pageStore);
		when(context.getPermissionEvaluator()).thenReturn(permissionEvaluator);
		when(context.getLocale()).thenReturn(Locale.US);

		runnable = new NeighborsMacro();
	}

	@Test
	public void getHtmlWithDefaultChildren() {
		String childrenHtml =
				"<li><a href=\"/" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/x\">" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/x</a></li>" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"<li><a href=\"/" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/y\">" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/y</a></li>" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"<li><a href=\"/" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/z\">" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/z</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertHtml(childrenHtml, null);
	}

	@Test
	public void getHtmlWith1Child() {
		String childrenHtml =
				"<li><a href=\"/" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/x\">" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/x</a></li>" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"<li><a href=\"/" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/y\">" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/y</a></li>" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"<li><a href=\"/" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/z\">" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/z</a></li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertHtml(childrenHtml, "children=1"); //$NON-NLS-1$
	}

	@Test
	public void getHtmlWith2Children() {
		String childrenHtml =
				"<li><a href=\"/" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/x\">" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/x</a>" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"<ul class=\"nav nav-list\">" + //$NON-NLS-1$
						"<li><a href=\"/" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/x/x1\">" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/x/x1</a></li>" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						"<li><a href=\"/" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/x/x2\">" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/x/x2</a></li>" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"</ul>" + //$NON-NLS-1$
				"</li>" + //$NON-NLS-1$
				"<li><a href=\"/" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/y\">" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/y</a>" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"<ul class=\"nav nav-list\">" + //$NON-NLS-1$
						"<li><a href=\"/" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/y/y1\">" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/y/y1</a></li>" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						"<li><a href=\"/" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/y/y2\">" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/y/y2</a></li>" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"</ul>" + //$NON-NLS-1$
				"</li>" + //$NON-NLS-1$
				"<li><a href=\"/" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/z\">" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/z</a>" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"<ul class=\"nav nav-list\">" + //$NON-NLS-1$
						"<li><a href=\"/" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/z/z1\">" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/z/z1</a></li>" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						"<li><a href=\"/" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/z/z2\">" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar/z/z2</a></li>" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"</ul>" + //$NON-NLS-1$
				"</li>"; //$NON-NLS-1$
		assertHtml(childrenHtml, "children=2"); //$NON-NLS-1$
	}

	private void assertHtml(String childrenHtml, String childrenParam) {
		when(htmlSerializerContext.getProjectName()).thenReturn(PROJECT);
		when(htmlSerializerContext.getBranchName()).thenReturn(BRANCH);
		when(htmlSerializerContext.getPagePath()).thenReturn(DocumentrConstants.HOME_PAGE_NAME + "/foo/bar"); //$NON-NLS-1$

		when(context.getHtmlSerializerContext()).thenReturn(htmlSerializerContext);
		when(context.getParameters()).thenReturn(childrenParam);

		// this is the HTML for home/foo/bar
		@SuppressWarnings("nls")
		String html =
				"<span class=\"well well-small neighbors pull-right\"><ul class=\"nav nav-list\">" +
					"<li>" +
						"<a href=\"/" + DocumentrConstants.HOME_PAGE_NAME + "\">" + DocumentrConstants.HOME_PAGE_NAME + "</a>" +
						"<ul class=\"nav nav-list\">" +
							"<li>" +
								"<a href=\"/" + DocumentrConstants.HOME_PAGE_NAME + "/foo\">" + DocumentrConstants.HOME_PAGE_NAME + "/foo</a>" +
								"<ul class=\"nav nav-list\">" +
									"<li><a href=\"/" + DocumentrConstants.HOME_PAGE_NAME + "/foo/aaa\">" + DocumentrConstants.HOME_PAGE_NAME + "/foo/aaa</a></li>" +
									"<li class=\"active\">" +
										"<a href=\"/" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar\">" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bar</a>" +
										"<ul class=\"nav nav-list\">" +
											childrenHtml +
										"</ul>" +
									"</li>" +
									"<li><a href=\"/" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bbb\">" + DocumentrConstants.HOME_PAGE_NAME + "/foo/bbb</a></li>" +
								"</ul>" +
							"</li>" +
						"</ul>" +
					"</li>" +
				"</ul></span>";
		assertEquals(html, runnable.getHtml(context));
	}

	private void setupPages() throws IOException {
		setupPages(PAGES);

		for (Page page : pages) {
			List<Page> childPages = Lists.newArrayList();
			String childPagePrefix = page.getPath() + "/"; //$NON-NLS-1$
			for (Page childPage : pages) {
				if (childPage.getPath().startsWith(childPagePrefix)) {
					String rest = StringUtils.substringAfter(childPage.getPath(), childPagePrefix);
					if (!rest.contains("/")) { //$NON-NLS-1$
						childPages.add(childPage);
					}
				}
			}
			when(pageStore.listChildPagesOrdered(PROJECT, BRANCH, page.getPath(), Locale.US)).thenReturn(childPages);
		}
	}

	private void setupPagePermissions() {
		when(permissionEvaluator.hasPagePermission(Matchers.<Authentication>any(),
					eq(PROJECT), eq(BRANCH), notEq(INACCESSIBLE_PAGE_PATH), same(Permission.VIEW)))
				.thenReturn(true);
		when(permissionEvaluator.hasPagePermission(Matchers.<Authentication>any(),
					eq(PROJECT), eq(BRANCH), eq(INACCESSIBLE_PAGE_PATH), same(Permission.VIEW)))
				.thenReturn(false);
	}

	private void setupPages(String... pagePaths) throws IOException {
		for (String pagePath : pagePaths) {
			setupPage(pagePath);
		}
	}

	private void setupPage(String pagePath) throws IOException {
		String parentPagePath = pagePath.contains("/") ? //$NON-NLS-1$
				StringUtils.substringBeforeLast(pagePath, "/") : //$NON-NLS-1$
				null;
		Page page = Page.fromText(pagePath, "text"); //$NON-NLS-1$
		TestPageUtil.setParentPagePath(page, parentPagePath);
		TestPageUtil.setPath(page, pagePath);
		when(pageStore.getPage(PROJECT, BRANCH, pagePath, false)).thenReturn(page);
		pages.add(page);
	}
}
