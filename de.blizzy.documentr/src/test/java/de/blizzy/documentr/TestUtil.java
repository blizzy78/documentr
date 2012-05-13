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
import de.blizzy.documentr.pagestore.Page;

public final class TestUtil {
	private TestUtil() {}

	public static Page createRandomPage(String parentPagePath) {
		return Page.fromText(parentPagePath, String.valueOf(Math.random() * Long.MAX_VALUE),
				String.valueOf(Math.random() * Long.MAX_VALUE));
	}
	
	public static void assertEqualsContract(Object equal1, Object equal2, Object equal3, Object different) {
		assertNotSame(equal1, equal2);
		assertNotSame(equal1, equal3);
		assertNotSame(equal2, equal3);

		// same object
		assertTrue(equal1.equals(equal1));
		
		// reflexive
		assertTrue(equal1.equals(equal2));
		
		// symmetric
		if (equal1.equals(equal2)) {
			assertTrue(equal2.equals(equal1));
		}
		
		// transitive
		if (equal1.equals(equal2) && equal2.equals(equal3)) {
			assertTrue(equal1.equals(equal3));
		}
		
		// consistent
		if (equal1.equals(equal2)) {
			assertTrue(equal1.equals(equal2));
		} else {
			assertFalse(equal1.equals(equal2));
		}
		
		// null
		assertFalse(equal1.equals(null));
		
		// difference
		assertFalse(equal1.equals(different));
		assertFalse(different.equals(equal1));
	}

	public static void assertHashCodeContract(Object equal1, Object equal2) {
		assertNotSame(equal1, equal2);
		assertEquals(equal1, equal2);
		
		// consistent
		int hashCode = equal1.hashCode();
		assertEquals(hashCode, equal1.hashCode());
		
		// equal hash code for equal objects
		assertEquals(equal1.hashCode(), equal2.hashCode());
	}
}
