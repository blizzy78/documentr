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
package de.blizzy.documentr.web.markdown;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Test;

import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;

public class PageRendererTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String PAGE = "page"; //$NON-NLS-1$
	private static final String MARKDOWN = "md"; //$NON-NLS-1$
	private static final String HTML = "html"; //$NON-NLS-1$

	@Test
	public void getHtml() throws IOException {
		Page page = Page.fromText("title", MARKDOWN); //$NON-NLS-1$
		
		IPageStore pageStore = mock(IPageStore.class);
		when(pageStore.getPage(PROJECT, BRANCH, PAGE, true)).thenReturn(page); 

		MarkdownProcessor markdownProcessor = mock(MarkdownProcessor.class);
		when(markdownProcessor.markdownToHTML(MARKDOWN, PROJECT, BRANCH, PAGE, null)).thenReturn(HTML); 
		
		PageRenderer pageRenderer = new PageRenderer();
		pageRenderer.setPageStore(pageStore);
		pageRenderer.setMarkdownProcessor(markdownProcessor);
		
		String result = pageRenderer.getHtml(PROJECT, BRANCH, PAGE, null);
		assertEquals(HTML, result);
	}

	@Test
	public void getHeaderHtml() throws IOException {
		Page page = Page.fromText("title", MARKDOWN); //$NON-NLS-1$
		
		IPageStore pageStore = mock(IPageStore.class);
		when(pageStore.getPage(PROJECT, BRANCH, PAGE, true)).thenReturn(page); 

		MarkdownProcessor markdownProcessor = mock(MarkdownProcessor.class);
		when(markdownProcessor.headerMarkdownToHTML(MARKDOWN, PROJECT, BRANCH, PAGE, null)).thenReturn(HTML); 
		
		PageRenderer pageRenderer = new PageRenderer();
		pageRenderer.setPageStore(pageStore);
		pageRenderer.setMarkdownProcessor(markdownProcessor);
		
		String result = pageRenderer.getHeaderHtml(PROJECT, BRANCH, PAGE, null);
		assertEquals(HTML, result);
	}
}
