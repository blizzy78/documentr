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
package de.blizzy.documentr.markdown.macro;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.ListableBeanFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.blizzy.documentr.AbstractDocumentrTest;

public class MacroFactoryTest extends AbstractDocumentrTest {
	private static final String MACRO = "macro"; //$NON-NLS-1$
	private static final String GROOVY_MACRO = "groovyMacro"; //$NON-NLS-1$
	
	@Mock
	private IMacro macro;
	@Mock
	private IMacro groovyMacro;
	@Mock
	private IMacroDescriptor descriptor;
	@Mock
	private IMacroDescriptor groovyDescriptor;
	@Mock
	private GroovyMacroScanner groovyMacroScanner;
	@Mock
	private ListableBeanFactory beanFactory;
	@InjectMocks
	private MacroFactory macroFactory;

	@Before
	public void setUp() {
		when(descriptor.getMacroName()).thenReturn(MACRO);

		when(groovyDescriptor.getMacroName()).thenReturn(GROOVY_MACRO);

		when(macro.getDescriptor()).thenReturn(descriptor);

		when(groovyMacro.getDescriptor()).thenReturn(groovyDescriptor);
		
		when(groovyMacroScanner.findGroovyMacros()).thenReturn(Sets.newHashSet(groovyMacro));

		Map<String, IMacro> macros = Maps.newHashMap();
		macros.put(MACRO, macro);
		when(beanFactory.getBeansOfType(IMacro.class)).thenReturn(macros);

		macroFactory.start();
	}
	
	@Test
	public void mustFindGroovyMacros() {
		assertSame(groovyMacro, macroFactory.get(GROOVY_MACRO));
	}
	
	@Test
	public void get() {
		assertSame(macro, macroFactory.get(MACRO));
	}
	
	@Test
	public void getDescriptors() {
		Set<IMacroDescriptor> descriptors = macroFactory.getDescriptors();
		assertEquals(Sets.newHashSet(descriptor, groovyDescriptor), descriptors);
	}
}
