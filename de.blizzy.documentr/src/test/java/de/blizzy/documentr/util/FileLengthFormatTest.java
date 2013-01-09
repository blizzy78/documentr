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
package de.blizzy.documentr.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Locale;

import org.junit.Test;
import org.mockito.Mock;
import org.springframework.context.MessageSource;

import de.blizzy.documentr.AbstractDocumentrTest;

public class FileLengthFormatTest extends AbstractDocumentrTest {
	@Mock
	private MessageSource messageSource;

	@Test
	public void format() {
		when(messageSource.getMessage("sizeX.bytes", new Object[] { "123" }, Locale.US)) //$NON-NLS-1$ //$NON-NLS-2$
			.thenReturn("123 bytes"); //$NON-NLS-1$
		when(messageSource.getMessage("sizeX.kb", new Object[] { "1.21" }, Locale.US)) //$NON-NLS-1$ //$NON-NLS-2$
			.thenReturn("1.21 KB"); //$NON-NLS-1$
		when(messageSource.getMessage("sizeX.mb", new Object[] { "1.21" }, Locale.US)) //$NON-NLS-1$ //$NON-NLS-2$
			.thenReturn("1.21 MB"); //$NON-NLS-1$
		when(messageSource.getMessage("sizeX.gb", new Object[] { "1.21" }, Locale.US)) //$NON-NLS-1$ //$NON-NLS-2$
			.thenReturn("1.21 GB"); //$NON-NLS-1$

		FileLengthFormat format = new FileLengthFormat(messageSource, Locale.US);
		assertEquals("123 bytes", format.format(123)); //$NON-NLS-1$
		assertEquals("1.21 KB", format.format(1234)); //$NON-NLS-1$
		assertEquals("1.21 MB", format.format(1268777)); //$NON-NLS-1$
		assertEquals("1.21 GB", format.format(1299227648)); //$NON-NLS-1$
	}
}
