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
package de.blizzy.documentr.system;

import static junit.framework.Assert.*;

import org.junit.Test;

public class VersionTest {
	@Test
	public void fromString() {
		Version version = Version.fromString("1.2.3-SNAPSHOT"); //$NON-NLS-1$
		assertEquals(1, version.getMajor());
		assertEquals(2, version.getMinor());
		assertEquals(3, version.getMaintenance());

		version = Version.fromString("2.3.4"); //$NON-NLS-1$
		assertEquals(2, version.getMajor());
		assertEquals(3, version.getMinor());
		assertEquals(4, version.getMaintenance());
	}

	@Test
	public void compareTo() {
		Version version = Version.fromString("1.2.3-SNAPSHOT"); //$NON-NLS-1$
		Version version2 = Version.fromString("2.3.4"); //$NON-NLS-1$
		assertTrue(version.compareTo(version2) < 0);
		assertTrue(version2.compareTo(version) > 0);
		assertEquals(0, version.compareTo(version));
	}
}
