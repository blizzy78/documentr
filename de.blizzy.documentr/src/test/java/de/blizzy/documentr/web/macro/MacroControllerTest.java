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
package de.blizzy.documentr.web.macro;

import static de.blizzy.documentr.DocumentrMatchers.*;
import static de.blizzy.documentr.TestUtil.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.google.common.collect.Lists;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.markdown.macro.CompilationMessage;
import de.blizzy.documentr.markdown.macro.MacroFactory;

public class MacroControllerTest extends AbstractDocumentrTest {
	private static final String MACRO = "macro"; //$NON-NLS-1$
	private static final String CODE = "code"; //$NON-NLS-1$

	@Mock
	private Model model;
	@Mock
	private MacroFactory macroFactory;
	@InjectMocks
	private MacroController controller;

	@Test
	public void createMacro() {
		String result = controller.createMacro(model);
		assertEquals("/macro/edit", result); //$NON-NLS-1$
		verify(model).addAttribute(eq("macroForm"), argMacroForm(StringUtils.EMPTY, StringUtils.EMPTY)); //$NON-NLS-1$
	}

	@Test
	public void editMacro() throws IOException {
		when(macroFactory.getGroovyMacroCode(MACRO)).thenReturn(CODE);

		String result = controller.editMacro(MACRO, model);
		assertEquals("/macro/edit", result); //$NON-NLS-1$
		verify(model).addAttribute(eq("macroForm"), argMacroForm(MACRO, CODE)); //$NON-NLS-1$
	}

	@Test
	public void verifyMacro() {
		List<CompilationMessage> errors = Lists.newArrayList(
				new CompilationMessage(CompilationMessage.Type.ERROR, 1, 2, 3, 4, "message1"), //$NON-NLS-1$
				new CompilationMessage(CompilationMessage.Type.WARNING, 5, 6, 7, 8, "message2")); //$NON-NLS-1$
		when(macroFactory.verifyGroovyMacro(CODE)).thenReturn(errors);

		Map<String, Object> result = controller.verifyMacro(CODE);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> messages = (List<Map<String, Object>>) result.get("messages"); //$NON-NLS-1$
		Map<String, Object> message = messages.get(0);
		assertEquals(CompilationMessage.Type.ERROR.name(), message.get("type")); //$NON-NLS-1$
		assertEquals(1, (int) ((Integer) message.get("startLine"))); //$NON-NLS-1$
		assertEquals(2, (int) ((Integer) message.get("startColumn"))); //$NON-NLS-1$
		assertEquals(3, (int) ((Integer) message.get("endLine"))); //$NON-NLS-1$
		assertEquals(4, (int) ((Integer) message.get("endColumn"))); //$NON-NLS-1$
		assertEquals("message1", message.get("message")); //$NON-NLS-1$ //$NON-NLS-2$
		message = messages.get(1);
		assertEquals(CompilationMessage.Type.WARNING.name(), message.get("type")); //$NON-NLS-1$
		assertEquals(5, (int) ((Integer) message.get("startLine"))); //$NON-NLS-1$
		assertEquals(6, (int) ((Integer) message.get("startColumn"))); //$NON-NLS-1$
		assertEquals(7, (int) ((Integer) message.get("endLine"))); //$NON-NLS-1$
		assertEquals(8, (int) ((Integer) message.get("endColumn"))); //$NON-NLS-1$
		assertEquals("message2", message.get("message")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void saveMacro() throws IOException {
		BindingResult bindingResult = mock(BindingResult.class);
		String result = controller.saveMacro(new MacroForm(MACRO, CODE), bindingResult);
		assertEquals("/macros", removeViewPrefix(result)); //$NON-NLS-1$
		assertRedirect(result);

		verify(macroFactory).saveGroovyMacro(MACRO, CODE);
	}

	@Test
	public void deleteMacro() throws IOException {
		String result = controller.deleteMacro(MACRO);
		assertEquals("/macros", removeViewPrefix(result)); //$NON-NLS-1$
		assertRedirect(result);

		verify(macroFactory).deleteGroovyMacro(MACRO);
	}

	@Test
	public void createMacroForm() {
		MacroForm result = controller.createMacroForm(MACRO, CODE);
		assertEquals(MACRO, result.getName());
		assertEquals(CODE, result.getCode());
	}
}
