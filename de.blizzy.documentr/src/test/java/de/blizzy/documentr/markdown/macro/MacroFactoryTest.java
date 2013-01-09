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
package de.blizzy.documentr.markdown.macro;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.ListableBeanFactory;

import com.google.common.collect.Lists;
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

	@Test
	public void listGroovyMacros() {
		List<String> macros = Lists.newArrayList("macro1", "macro2"); //$NON-NLS-1$ //$NON-NLS-2$
		when(groovyMacroScanner.listMacros()).thenReturn(macros);

		assertEquals(macros, macroFactory.listGroovyMacros());
	}

	@Test
	public void getGroovyMacroCode() throws IOException {
		when(groovyMacroScanner.getMacroCode(GROOVY_MACRO)).thenReturn("code"); //$NON-NLS-1$
		assertEquals("code", macroFactory.getGroovyMacroCode(GROOVY_MACRO)); //$NON-NLS-1$
	}

	@Test
	public void test() {
		List<CompilationMessage> errors = Lists.newArrayList(mock(CompilationMessage.class));
		when(groovyMacroScanner.verifyMacro("code")).thenReturn(errors); //$NON-NLS-1$

		List<CompilationMessage> result = macroFactory.verifyGroovyMacro("code"); //$NON-NLS-1$
		assertEquals(errors, result);
	}

	@Test
	public void saveGroovyMacro() throws IOException {
		macroFactory.saveGroovyMacro(GROOVY_MACRO, "code"); //$NON-NLS-1$

		InOrder inOrder = inOrder(groovyMacroScanner);
		inOrder.verify(groovyMacroScanner).saveMacro(GROOVY_MACRO, "code"); //$NON-NLS-1$
		inOrder.verify(groovyMacroScanner).findGroovyMacros();
	}

	@Test
	public void deleteGroovyMacro() throws IOException {
		macroFactory.deleteGroovyMacro(GROOVY_MACRO);

		InOrder inOrder = inOrder(groovyMacroScanner);
		inOrder.verify(groovyMacroScanner).deleteMacro(GROOVY_MACRO);
		inOrder.verify(groovyMacroScanner).findGroovyMacros();
	}
}
