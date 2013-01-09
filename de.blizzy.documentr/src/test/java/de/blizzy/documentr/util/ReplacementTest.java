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

import java.util.regex.Pattern;

import org.junit.Test;

public class ReplacementTest {
	@Test
	public void replaceAll() {
		Replacement replacement = new Replacement("[0-9]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("x___y", replacement.replaceAll("x123y")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void replaceAllWithPatternInstance() {
		Pattern pattern = Pattern.compile("[a-z]", Pattern.DOTALL + Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
		Replacement replacement = new Replacement(pattern, "_"); //$NON-NLS-1$
		assertEquals("__", replacement.replaceAll("xY")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void dotAllNoCase() {
		Replacement replacement = Replacement.dotAllNoCase("[a-z]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("__", replacement.replaceAll("xY")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void replaceAllMustAcceptNull() {
		Replacement replacement = new Replacement("[0-9]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
		assertNull(replacement.replaceAll(null));
	}
}
