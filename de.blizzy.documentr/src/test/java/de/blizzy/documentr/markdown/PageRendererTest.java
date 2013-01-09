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
package de.blizzy.documentr.markdown;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;

public class PageRendererTest extends AbstractDocumentrTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String PAGE_PATH = "page"; //$NON-NLS-1$
	private static final String MARKDOWN = "md"; //$NON-NLS-1$
	private static final String HTML = "html"; //$NON-NLS-1$
	private static final String CONTEXT = "/context"; //$NON-NLS-1$
	private static final Page PAGE = Page.fromText("title", MARKDOWN); //$NON-NLS-1$

	@Mock
	private Authentication authentication;
	@Mock
	private IPageStore pageStore;
	@Mock
	private MarkdownProcessor markdownProcessor;
	@InjectMocks
	private PageRenderer pageRenderer;

	@Before
	public void setUp() throws IOException {
		when(pageStore.getPage(PROJECT, BRANCH, PAGE_PATH, true)).thenReturn(PAGE);
	}

	@Test
	public void getHtml() throws IOException {
		when(markdownProcessor.markdownToHtml(MARKDOWN, PROJECT, BRANCH, PAGE_PATH, authentication, CONTEXT))
			.thenReturn(HTML);

		String result = pageRenderer.getHtml(PROJECT, BRANCH, PAGE_PATH, authentication, CONTEXT);
		assertEquals(HTML, result);
	}

	@Test
	public void getHeaderHtml() throws IOException {
		when(markdownProcessor.headerMarkdownToHtml(MARKDOWN, PROJECT, BRANCH, PAGE_PATH, authentication, CONTEXT))
			.thenReturn(HTML);

		String result = pageRenderer.getHeaderHtml(PROJECT, BRANCH, PAGE_PATH, authentication, CONTEXT);
		assertEquals(HTML, result);
	}
}
