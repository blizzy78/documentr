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
package de.blizzy.documentr.web.markdown.macro;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.Test;

import de.blizzy.documentr.web.markdown.HtmlSerializerContext;

public class AbstractMacroTest {
	private static final class TestMacro extends AbstractMacro {
		@Override
		public String getHtml(String body) {
			return null;
		}
	}
	
	@Test
	public void setAndGetParameters() {
		AbstractMacro macro = new TestMacro();
		macro.setParameters("params"); //$NON-NLS-1$
		assertSame("params", macro.getParameters()); //$NON-NLS-1$
	}
	
	@Test
	public void setAndGetHtmlSerializerContext() {
		HtmlSerializerContext context = mock(HtmlSerializerContext.class);
		AbstractMacro macro = new TestMacro();
		macro.setHtmlSerializerContext(context);
		assertSame(context, macro.getHtmlSerializerContext());
	}

	@Test
	public void setAndGetMacroContext() {
		IMacroContext context = mock(IMacroContext.class);
		AbstractMacro macro = new TestMacro();
		macro.setMacroContext(context);
		assertSame(context, macro.getMacroContext());
	}
	
	@Test
	public void cleanupHTMLDefaultImplementationMustReturnHtmlUnchanged() {
		String randomHtml = UUID.randomUUID().toString();
		assertEquals(randomHtml, new TestMacro().cleanupHTML(randomHtml));
	}
	
	@Test
	public void isCacheableDefaultImplementation() {
		assertTrue(new TestMacro().isCacheable());
	}
}
