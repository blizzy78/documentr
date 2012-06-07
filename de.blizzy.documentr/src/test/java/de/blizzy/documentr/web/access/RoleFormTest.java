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
package de.blizzy.documentr.web.access;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

public class RoleFormTest {
	@Test
	public void getName() {
		Set<String> permissions = Sets.newHashSet("p1", "p2", "p3"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		RoleForm form = new RoleForm("role", permissions); //$NON-NLS-1$
		assertEquals("role", form.getName()); //$NON-NLS-1$
	}

	@Test
	public void getPermissions() {
		Set<String> permissions = Sets.newHashSet("p1", "p2", "p3"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		RoleForm form = new RoleForm("role", permissions); //$NON-NLS-1$
		assertEquals(permissions, form.getPermissions());
	}
}
