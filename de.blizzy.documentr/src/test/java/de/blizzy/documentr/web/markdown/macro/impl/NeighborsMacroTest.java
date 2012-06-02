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
package de.blizzy.documentr.web.markdown.macro.impl;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import de.blizzy.documentr.pagestore.Page;
import de.blizzy.documentr.pagestore.PageStore;
import de.blizzy.documentr.web.markdown.HtmlSerializerContext;
import de.blizzy.documentr.web.markdown.macro.IMacroContext;

public class NeighborsMacroTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	@SuppressWarnings("nls")
	private static final String[] PAGES = {
		"home",
		"home/foo",
		"home/foo/aaa",
		"home/foo/aaa/a1",
		"home/foo/aaa/a2",
		"home/foo/bar", // <-- page under test
		"home/foo/bar/x",
		"home/foo/bar/x/x1",
		"home/foo/bar/x/x2",
		"home/foo/bar/y",
		"home/foo/bar/y/y1",
		"home/foo/bar/y/y2",
		"home/foo/bar/z",
		"home/foo/bar/z/z1",
		"home/foo/bar/z/z2",
		"home/foo/bbb",
		"home/foo/bbb/b1",
		"home/foo/bbb/b2"
	};
	
	private PageStore pageStore;
	private HtmlSerializerContext htmlSerializerContext;
	private IMacroContext macroContext;

	@Before
	public void setUp() throws IOException {
		htmlSerializerContext = mock(HtmlSerializerContext.class);
		when(htmlSerializerContext.getPageURI(anyString())).then(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return "/" + invocation.getArguments()[0]; //$NON-NLS-1$
			}
		});
		
		pageStore = mock(PageStore.class);
		setupPages();

		macroContext = mock(IMacroContext.class);
		when(macroContext.getPageStore()).thenReturn(pageStore);
	}
	
	private void setupPages() throws IOException {
		setupPages(PAGES);
		
		for (String page : PAGES) {
			List<String> childPages = new ArrayList<String>();
			String childPagePrefix = page + "/"; //$NON-NLS-1$
			for (String childPage : PAGES) {
				if (childPage.startsWith(childPagePrefix)) {
					String rest = StringUtils.substringAfter(childPage, childPagePrefix);
					if (!rest.contains("/")) { //$NON-NLS-1$
						childPages.add(childPage);
					}
				}
			}
			Collections.sort(childPages);
			when(pageStore.listChildPagePaths(PROJECT, BRANCH, page)).thenReturn(childPages);
		}
	}
	
	@Test
	public void getHtml() {
		when(htmlSerializerContext.getProjectName()).thenReturn(PROJECT);
		when(htmlSerializerContext.getBranchName()).thenReturn(BRANCH);
		when(htmlSerializerContext.getPagePath()).thenReturn("home/foo/bar"); //$NON-NLS-1$
		
		NeighborsMacro macro = new NeighborsMacro();
		macro.setHtmlSerializerContext(htmlSerializerContext);
		macro.setMacroContext(macroContext);
		
		// this is the HTML for home/foo/bar
		@SuppressWarnings("nls")
		String html =
				"<div class=\"neighbors-box\">" +
					"<ul class=\"neighbors\">" +
						"<li>" +
							"<a href=\"/home\">home</a>" +
							"<ul>" +
								"<li>" +
									"<a href=\"/home/foo\">home/foo</a>" +
									"<ul>" +
										"<li><a href=\"/home/foo/aaa\">home/foo/aaa</a></li>" +
										"<li class=\"active\">" +
											"home/foo/bar" +
											"<ul>" +
												"<li><a href=\"/home/foo/bar/x\">home/foo/bar/x</a></li>" +
												"<li><a href=\"/home/foo/bar/y\">home/foo/bar/y</a></li>" +
												"<li><a href=\"/home/foo/bar/z\">home/foo/bar/z</a></li>" +
											"</ul>" +
										"</li>" +
										"<li><a href=\"/home/foo/bbb\">home/foo/bbb</a></li>" +
									"</ul>" +
								"</li>" +
							"</ul>" +
						"</li>" +
					"</ul>" +
				"</div>";
		assertEquals(html, macro.getHtml());
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
		Page page = Page.fromText(parentPagePath, pagePath, "text"); //$NON-NLS-1$
		when(pageStore.getPage(PROJECT, BRANCH, pagePath, false)).thenReturn(page);
	}
}
