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
import de.blizzy.documentr.markdown.macro.IMacroContext;
import de.blizzy.documentr.markdown.macro.impl.ColorMacro;

public class ColorMacroTest extends AbstractDocumentrTest {
	private ColorMacro runnable;
	@Mock
	private IMacroContext context;

	@Before
	public void setUp() {
		runnable = new ColorMacro();
	}

	@Test
	public void getHtml() {
		when(context.getParameters()).thenReturn("#880000"); //$NON-NLS-1$
		when(context.getBody()).thenReturn("body"); //$NON-NLS-1$
		assertEquals("<span style=\"color: #880000;\">body</span>", runnable.getHtml(context)); //$NON-NLS-1$
	}
}
