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

import org.junit.Before;
import org.junit.Test;

import de.blizzy.documentr.web.markdown.macro.impl.LabelMacro;

public class MacroDescriptorTest {
	private static final String NAME = "macro"; //$NON-NLS-1$
	private static final String TITLE_KEY = "titleKey"; //$NON-NLS-1$
	private static final String DESCRIPTION_KEY = "descriptionKey"; //$NON-NLS-1$
	private static final Class<? extends IMacro> CLASS = LabelMacro.class;
	private static final String INSERT_TEXT = "insertText"; //$NON-NLS-1$
	
	private MacroDescriptor desc;
	
	@Before
	public void setUp() {
		desc = new MacroDescriptor(NAME, TITLE_KEY, DESCRIPTION_KEY, CLASS, INSERT_TEXT);
	}
	
	@Test
	public void getName() {
		assertEquals(NAME, desc.getName());
	}
	
	@Test
	public void getTitleKey() {
		assertEquals(TITLE_KEY, desc.getTitleKey());
	}
	
	@Test
	public void getDescriptionKey() {
		assertEquals(DESCRIPTION_KEY, desc.getDescriptionKey());
	}
	
	@Test
	public void getMacroClass() {
		assertEquals(CLASS, desc.getMacroClass());
	}
	
	@Test
	public void getInsertText() {
		assertEquals(INSERT_TEXT, desc.getInsertText());
	}
}
