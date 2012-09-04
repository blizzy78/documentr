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

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.Sets;

import de.blizzy.documentr.AbstractDocumentrTest;

public class MacroFactoryTest extends AbstractDocumentrTest {
	private static final String MACRO = "macro"; //$NON-NLS-1$
	
	private MacroFactory macroFactory;
	@Mock
	private IMacro macro;
	@Mock
	private IMacroDescriptor descriptor;

	@Before
	public void setUp() {
		when(descriptor.getMacroName()).thenReturn(MACRO);
		
		when(macro.getDescriptor()).thenReturn(descriptor);
		
		macroFactory = new MacroFactory();
		macroFactory.setContextMacros(Sets.newHashSet(macro));
		macroFactory.init();
	}
	
	@Test
	public void get() {
		IMacro result = macroFactory.get(MACRO);
		assertSame(macro, result);
	}
	
	@Test
	public void getDescriptors() {
		Set<IMacroDescriptor> descriptors = macroFactory.getDescriptors();
		assertEquals(Sets.newHashSet(descriptor), descriptors);
	}
}
