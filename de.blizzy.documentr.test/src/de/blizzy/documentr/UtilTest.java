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
package de.blizzy.documentr;

import static org.junit.Assert.*;

import org.junit.Test;

public class UtilTest {
	@Test
	public void toRealPagePath() {
		assertEquals("x/y/z", Util.toRealPagePath("x,y,z")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("x/y/z", Util.toRealPagePath("x/y/z")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void toURLPagePath() {
		assertEquals("x,y,z", Util.toURLPagePath("x/y/z")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("x,y,z", Util.toURLPagePath("x,y,z")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Test
	public void generatePageName() {
		assertEquals("changes-for-1-0-x-code-name-wheatley-xl", //$NON-NLS-1$
				Util.generatePageName("Changes For 1.0.x: Code Name \"Wheatley\" (XL)")); //$NON-NLS-1$
	}
}
