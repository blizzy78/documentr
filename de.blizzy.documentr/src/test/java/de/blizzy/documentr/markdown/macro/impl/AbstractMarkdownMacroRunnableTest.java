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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.markdown.HtmlSerializerContext;
import de.blizzy.documentr.markdown.macro.IMacroContext;
import de.blizzy.documentr.markdown.macro.impl.AbstractMarkdownMacroRunnable;

public class AbstractMarkdownMacroRunnableTest extends AbstractDocumentrTest {
	@Mock
	private IMacroContext context;
	@Mock
	private HtmlSerializerContext htmlSerializerContext;

	@Before
	public void setUp() {
		when(context.getHtmlSerializerContext()).thenReturn(htmlSerializerContext);
	}

	@Test
	public void getHtml() {
		when(htmlSerializerContext.markdownToHtml("markdown")).thenReturn("html"); //$NON-NLS-1$ //$NON-NLS-2$

		AbstractMarkdownMacroRunnable runnable = new AbstractMarkdownMacroRunnable() {
			@Override
			String getMarkdown(IMacroContext macroContext) {
				return "markdown"; //$NON-NLS-1$
			}
		};

		assertEquals("html", runnable.getHtml(context)); //$NON-NLS-1$
	}
}
