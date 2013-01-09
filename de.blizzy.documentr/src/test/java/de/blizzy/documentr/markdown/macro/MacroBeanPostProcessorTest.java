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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.BeanFactory;

import de.blizzy.documentr.AbstractDocumentrTest;

public class MacroBeanPostProcessorTest extends AbstractDocumentrTest {
	@Macro(name=MacroBeanPostProcessorTest.MACRO, insertText=MacroBeanPostProcessorTest.INSERT_TEXT)
	private static class TestMacro implements ISimpleMacro {
		private boolean createRunnableInvoked;

		@Override
		public IMacroRunnable createRunnable() {
			createRunnableInvoked = true;
			return null;
		}
	}

	static final String MACRO = "testMacro"; //$NON-NLS-1$
	static final String INSERT_TEXT = "insertText"; //$NON-NLS-1$

	@Mock
	private BeanFactory beanFactory;
	@InjectMocks
	private MacroBeanPostProcessor processor;
	private TestMacro macro;

	@Before
	public void setUp() {
		when(beanFactory.getBean(MessageSourceMacroDescriptor.ID, MACRO))
			.thenReturn(new MessageSourceMacroDescriptor(MACRO));

		macro = new TestMacro();
	}

	@Test
	public void foo() {
		SimpleMacroMacro result = (SimpleMacroMacro) processor.postProcessAfterInitialization(macro, "macroBean"); //$NON-NLS-1$
		IMacroDescriptor descriptor = result.getDescriptor();
		assertEquals(MACRO, descriptor.getMacroName());
		assertEquals(INSERT_TEXT, descriptor.getInsertText());
		assertTrue(descriptor.isCacheable());

		result.createRunnable();
		assertTrue(macro.createRunnableInvoked);
	}
}
