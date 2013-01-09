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
package de.blizzy.documentr.repository;

import static de.blizzy.documentr.TestUtil.*;

import org.junit.Test;

public class LockKeyTest {
	@Test
	public void testEquals() {
		LockKey equal1 = new LockKey("p1", "b", true); //$NON-NLS-1$ //$NON-NLS-2$
		LockKey equal2 = new LockKey("p1", "b", true); //$NON-NLS-1$ //$NON-NLS-2$
		LockKey equal3 = new LockKey("p1", "b", true); //$NON-NLS-1$ //$NON-NLS-2$
		LockKey different1 = new LockKey("p2", "b", true); //$NON-NLS-1$ //$NON-NLS-2$
		LockKey different2 = new LockKey("p1", "b2", true); //$NON-NLS-1$ //$NON-NLS-2$
		LockKey different3 = new LockKey("p1", "b", false); //$NON-NLS-1$ //$NON-NLS-2$
		assertEqualsContract(equal1, equal2, equal3, different1);
		assertEqualsContract(equal1, equal2, equal3, different2);
		assertEqualsContract(equal1, equal2, equal3, different3);
	}

	@Test
	public void testHashCode() {
		LockKey equal1 = new LockKey("p1", "b", true); //$NON-NLS-1$ //$NON-NLS-2$
		LockKey equal2 = new LockKey("p1", "b", true); //$NON-NLS-1$ //$NON-NLS-2$
		assertHashCodeContract(equal1, equal2);
	}
}
