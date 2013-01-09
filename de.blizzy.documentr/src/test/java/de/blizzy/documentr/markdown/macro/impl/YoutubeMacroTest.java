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

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.markdown.macro.IMacroContext;
import de.blizzy.documentr.markdown.macro.impl.YoutubeMacro;

public class YoutubeMacroTest extends AbstractDocumentrTest {
	private YoutubeMacro runnable;
	@Mock
	private IMacroContext context;

	@Before
	public void setUp() {
		runnable = new YoutubeMacro();
	}

	@Test
	public void getHtml() {
		when(context.getParameters()).thenReturn("video123"); //$NON-NLS-1$
		assertEquals("<iframe width=\"560\" height=\"315\" src=\"http://www.youtube.com/embed/video123?" + //$NON-NLS-1$
				"rel=0\" frameborder=\"0\" allowfullscreen></iframe>", runnable.getHtml(context)); //$NON-NLS-1$
	}

	@Test
	public void getHtmlWithUrl() {
		when(context.getParameters()).thenReturn("http://www.youtube.com/watch?v=video123&feature=g-vrec"); //$NON-NLS-1$
		assertEquals("<iframe width=\"560\" height=\"315\" src=\"http://www.youtube.com/embed/video123?" + //$NON-NLS-1$
				"rel=0\" frameborder=\"0\" allowfullscreen></iframe>", runnable.getHtml(context)); //$NON-NLS-1$
	}
}
