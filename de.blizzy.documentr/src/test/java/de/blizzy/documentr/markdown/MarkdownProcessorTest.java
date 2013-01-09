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

import static de.blizzy.documentr.TestUtil.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.security.core.Authentication;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.markdown.macro.IMacro;
import de.blizzy.documentr.markdown.macro.IMacroContext;
import de.blizzy.documentr.markdown.macro.IMacroDescriptor;
import de.blizzy.documentr.markdown.macro.IMacroRunnable;
import de.blizzy.documentr.markdown.macro.MacroFactory;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.system.SystemSettingsStore;

public class MarkdownProcessorTest extends AbstractDocumentrTest {
	private static final String MACRO = "macro"; //$NON-NLS-1$
	private static final String PARAMS = "params"; //$NON-NLS-1$
	private static final String CONTEXT = "/context"; //$NON-NLS-1$

	@Mock
	private MacroFactory macroFactory;
	@Mock
	private Authentication authentication;
	@Mock
	@SuppressWarnings("unused")
	private BeanFactory beanFactory;
	@Mock
	@SuppressWarnings("unused")
	private IPageStore pageStore;
	@Mock
	@SuppressWarnings("unused")
	private SystemSettingsStore systemSettingsStore;
	@Mock
	private IMacroDescriptor descriptor;
	@Mock
	private IMacroRunnable runnable;
	@Mock
	private IMacro macro;
	@InjectMocks
	private MarkdownProcessor markdownProcessor;

	@Test
	public void markdownToHtml() {
		when(descriptor.isCacheable()).thenReturn(true);

		when(macro.getDescriptor()).thenReturn(descriptor);
		when(macro.createRunnable()).thenReturn(runnable);

		String macroHtml = "<div>macroHtml</div>"; //$NON-NLS-1$
		when(runnable.getHtml(any(IMacroContext.class))).thenReturn(macroHtml);
		String cleanedMacroHtml = "<div>cleanedMacroHtml</div>"; //$NON-NLS-1$
		when(runnable.cleanupHtml(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) {
				String html = (String) invocation.getArguments()[0];
				return StringUtils.replace(html, "macroHtml", "cleanedMacroHtml"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		when(macroFactory.get(MACRO)).thenReturn(macro);

		String markdown = "{{:header:}}*header*{{:/header:}}**foo**\n\n{{" + MACRO + "/}}\n\nbar\n"; //$NON-NLS-1$ //$NON-NLS-2$
		String result = markdownProcessor.markdownToHtml(markdown, "project", "branch", //$NON-NLS-1$ //$NON-NLS-2$
				DocumentrConstants.HOME_PAGE_NAME + "/bar", authentication, CONTEXT); //$NON-NLS-1$

		String expectedHtml = "<p><strong>foo</strong></p>" + cleanedMacroHtml + "<p>bar</p>"; //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals(expectedHtml, removeTextRange(result));
	}

	@Test
	public void markdownToHtmlMustNotRenderNonCacheableMacros() {
		when(descriptor.isCacheable()).thenReturn(false);

		when(macro.getDescriptor()).thenReturn(descriptor);

		when(macroFactory.get(MACRO)).thenReturn(macro);

		String markdown = "**foo**\n\n{{" + MACRO + " " + PARAMS + "/}}\n\nbar\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String result = markdownProcessor.markdownToHtml(markdown, "project", "branch", //$NON-NLS-1$ //$NON-NLS-2$
				DocumentrConstants.HOME_PAGE_NAME + "/bar", authentication, CONTEXT); //$NON-NLS-1$

		String expectedHtml = "<p><strong>foo</strong></p><p>" + //$NON-NLS-1$
				"__" + MarkdownProcessor.NON_CACHEABLE_MACRO_MARKER + "_1__" + MACRO + " " + PARAMS + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"__" + MarkdownProcessor.NON_CACHEABLE_MACRO_BODY_MARKER + "__" + //$NON-NLS-1$ //$NON-NLS-2$
				"__/" + MarkdownProcessor.NON_CACHEABLE_MACRO_MARKER + "_1__" + "</p><p>bar</p>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals(expectedHtml, removeTextRange(result));
	}

	@Test
	public void processNonCacheableMacros() {
		when(descriptor.isCacheable()).thenReturn(false);

		when(macro.getDescriptor()).thenReturn(descriptor);
		when(macro.createRunnable()).thenReturn(runnable);

		String macroHtml = "<div>macroHtml</div>"; //$NON-NLS-1$
		when(runnable.getHtml(any(IMacroContext.class))).thenReturn(macroHtml);
		String cleanedMacroHtml = "<div>cleanedMacroHtml</div>"; //$NON-NLS-1$
		when(runnable.cleanupHtml(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) {
				String html = (String) invocation.getArguments()[0];
				return StringUtils.replace(html, "macroHtml", "cleanedMacroHtml"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		when(macroFactory.get(MACRO)).thenReturn(macro);

		String html = "<p>__" + MarkdownProcessor.NON_CACHEABLE_MACRO_MARKER + "_1__" + MACRO + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				PARAMS + "__/" + MarkdownProcessor.NON_CACHEABLE_MACRO_MARKER + "_1__</p>"; //$NON-NLS-1$ //$NON-NLS-2$
		String result = markdownProcessor.processNonCacheableMacros(html, "project", "branch", //$NON-NLS-1$ //$NON-NLS-2$
				DocumentrConstants.HOME_PAGE_NAME + "/bar", authentication, CONTEXT); //$NON-NLS-1$
		String expectedHtml = cleanedMacroHtml;
		assertEquals(expectedHtml, result);
	}

	@Test
	public void headerMarkdownToHtml() {
		String markdown = "{{:header:}}*header*{{:/header:}}**foo**"; //$NON-NLS-1$
		String result = markdownProcessor.headerMarkdownToHtml(markdown, "project", "branch", //$NON-NLS-1$ //$NON-NLS-2$
				DocumentrConstants.HOME_PAGE_NAME + "/bar", authentication, CONTEXT); //$NON-NLS-1$

		String expectedHtml = "<em>header</em>"; //$NON-NLS-1$
		assertEquals(expectedHtml, removeTextRange(result));
	}
}
