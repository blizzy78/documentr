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
package de.blizzy.documentr.web.markdown.macro.factory;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.web.markdown.HtmlSerializerContext;
import de.blizzy.documentr.web.markdown.macro.AbstractMacro;
import de.blizzy.documentr.web.markdown.macro.IMacro;
import de.blizzy.documentr.web.markdown.macro.factory.MacroFactory;
import de.blizzy.documentr.web.markdown.macro.impl.LabelMacro;
import de.blizzy.documentr.web.markdown.macro.impl.NeighborsMacro;
import de.blizzy.documentr.web.markdown.macro.impl.TableOfContentsMacro;
import de.blizzy.documentr.web.markdown.macro.impl.UnknownMacroMacro;
import de.blizzy.documentr.web.markdown.macro.impl.VimeoMacro;
import de.blizzy.documentr.web.markdown.macro.impl.YoutubeMacro;

public class MacroFactoryTest {
	private IPageStore pageStore;
	private MacroFactory macroFactory;
	private HtmlSerializerContext htmlSerializerContext;
	private DocumentrPermissionEvaluator permissionEvaluator;

	@Before
	public void setUp() {
		pageStore = mock(IPageStore.class);
		permissionEvaluator = mock(DocumentrPermissionEvaluator.class);
		
		macroFactory = new MacroFactory();
		macroFactory.setPageStore(pageStore);
		macroFactory.setPermissionEvaluator(permissionEvaluator);
		
		htmlSerializerContext = mock(HtmlSerializerContext.class);
	}
	
	@Test
	public void getMacro() {
		assertMacro("label", LabelMacro.class); //$NON-NLS-1$
		assertMacro("neighbors", NeighborsMacro.class); //$NON-NLS-1$
		assertMacro("toc", TableOfContentsMacro.class); //$NON-NLS-1$
		assertMacro("vimeo", VimeoMacro.class); //$NON-NLS-1$
		assertMacro("youtube", YoutubeMacro.class); //$NON-NLS-1$
		assertMacro("_unknown_", UnknownMacroMacro.class); //$NON-NLS-1$
	}

	private void assertMacro(String macroName, Class<? extends IMacro> macroClass) {
		IMacro macro = macroFactory.get(macroName, "params", htmlSerializerContext); //$NON-NLS-1$
		assertTrue(macroClass.isInstance(macro));
		if (macro instanceof AbstractMacro) {
			assertSame(htmlSerializerContext, ((AbstractMacro) macro).getHtmlSerializerContext());
			assertSame(pageStore, ((AbstractMacro) macro).getMacroContext().getPageStore());
			assertEquals("params", ((AbstractMacro) macro).getParameters()); //$NON-NLS-1$
		}
	}
}
