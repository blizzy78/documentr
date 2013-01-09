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

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.MessageSource;

import de.blizzy.documentr.AbstractDocumentrTest;

public class MessageSourceMacroDescriptorTest extends AbstractDocumentrTest {
	private static final String MACRO = "macro"; //$NON-NLS-1$
	private static final Locale LOCALE = Locale.US;
	private static final String TITLE = "title"; //$NON-NLS-1$
	private static final String DESCRIPTION = "description"; //$NON-NLS-1$

	@Mock
	private MessageSource messageSource;
	@Mock
	private BeanFactory beanFactory;
	private MessageSourceMacroDescriptor descriptor;

	@Before
	public void setUp() {
		MessageSourceMacroDescriptor desc = new MessageSourceMacroDescriptor(MACRO);
		Whitebox.setInternalState(desc, messageSource);

		when(beanFactory.getBean(MessageSourceMacroDescriptor.ID, MACRO)).thenReturn(desc);

		when(messageSource.getMessage("macro." + MACRO + ".title", null, LOCALE)) //$NON-NLS-1$ //$NON-NLS-2$
			.thenReturn(TITLE);
		when(messageSource.getMessage("macro." + MACRO + ".description", null, LOCALE)) //$NON-NLS-1$ //$NON-NLS-2$
			.thenReturn(DESCRIPTION);

		descriptor = MessageSourceMacroDescriptor.create(MACRO, beanFactory);
	}

	@Test
	public void getMacroName() {
		assertEquals(MACRO, descriptor.getMacroName());
	}

	@Test
	public void setAndGetInsertText() {
		MessageSourceMacroDescriptor desc = descriptor.insertText("insertText"); //$NON-NLS-1$
		assertSame(descriptor, desc);
		assertEquals("insertText", descriptor.getInsertText()); //$NON-NLS-1$
	}

	@Test
	public void getTitle() {
		assertEquals(TITLE, descriptor.getTitle(LOCALE));
	}

	@Test
	public void getDescription() {
		assertEquals(DESCRIPTION, descriptor.getDescription(LOCALE));
	}

	@Test
	public void setAndGetCacheable() {
		MessageSourceMacroDescriptor desc = descriptor.cacheable(true);
		assertSame(descriptor, desc);
		assertTrue(descriptor.isCacheable());

		descriptor.cacheable(false);
		assertFalse(descriptor.isCacheable());
	}
}
