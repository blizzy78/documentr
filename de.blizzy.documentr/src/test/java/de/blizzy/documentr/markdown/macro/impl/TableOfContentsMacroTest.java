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

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.Lists;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.markdown.Header;
import de.blizzy.documentr.markdown.HtmlSerializerContext;
import de.blizzy.documentr.markdown.macro.IMacroContext;
import de.blizzy.documentr.markdown.macro.impl.TableOfContentsMacro;

public class TableOfContentsMacroTest extends AbstractDocumentrTest {
	private TableOfContentsMacro runnable;
	@Mock
	private IMacroContext context;
	@Mock
	private HtmlSerializerContext htmlSerializerContext;

	@Before
	public void setUp() {
		runnable = new TableOfContentsMacro();

		when(context.getHtmlSerializerContext()).thenReturn(htmlSerializerContext);
	}

	@Test
	public void getMarkdown() {
		List<Header> headers = Lists.newArrayList(
				new Header("foo", 1), //$NON-NLS-1$
				new Header("bar", 2), //$NON-NLS-1$
				new Header("baz", 3), //$NON-NLS-1$
				new Header("qux", 1)); //$NON-NLS-1$
		when(htmlSerializerContext.getHeaders()).thenReturn(headers);

		assertEquals(
				"- [[#foo]]\n" + //$NON-NLS-1$
				"    - [[#bar]]\n" + //$NON-NLS-1$
				"        - [[#baz]]\n" + //$NON-NLS-1$
				"- [[#qux]]\n\n", //$NON-NLS-1$
				runnable.getMarkdown(context));
	}

	@Test
	public void getMarkdownButNoHeaders() {
		List<Header> headers = Collections.emptyList();
		when(htmlSerializerContext.getHeaders()).thenReturn(headers);

		assertNull(runnable.getMarkdown(context));
	}
}
