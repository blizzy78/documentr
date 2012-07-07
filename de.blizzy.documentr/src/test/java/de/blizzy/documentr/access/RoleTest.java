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
package de.blizzy.documentr.access;

import static org.junit.Assert.*;

import java.util.EnumSet;

import org.junit.Test;

public class RoleTest {
	@Test
	public void getName() {
		EnumSet<Permission> permissions = EnumSet.of(Permission.VIEW, Permission.EDIT_PAGE);
		Role role = new Role("role", permissions); //$NON-NLS-1$
		assertEquals("role", role.getName()); //$NON-NLS-1$
	}

	@Test
	public void getPermissions() {
		EnumSet<Permission> permissions = EnumSet.of(Permission.VIEW, Permission.EDIT_PAGE);
		Role role = new Role("role", permissions); //$NON-NLS-1$
		assertEquals(permissions, role.getPermissions());
	}
}
