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

import org.junit.Test;

public class UserFormTest {
	@Test
	public void getLoginName() {
		UserForm form = new UserForm("user", "pw", "pw2", false, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("user", form.getLoginName()); //$NON-NLS-1$
	}

	@Test
	public void getPassword1() {
		UserForm form = new UserForm("user", "pw", "pw2", false, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("pw", form.getPassword1()); //$NON-NLS-1$
	}
	
	@Test
	public void getPassword2() {
		UserForm form = new UserForm("user", "pw", "pw2", false, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("pw2", form.getPassword2()); //$NON-NLS-1$
	}
	
	@Test
	public void isDisabled() {
		UserForm form = new UserForm("user", "pw", "pw2", false, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertFalse(form.isDisabled());
		form = new UserForm("user", "pw", "pw2", true, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertTrue(form.isDisabled());
	}
	
	@Test
	public void isAdmin() {
		UserForm form = new UserForm("user", "pw", "pw2", false, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertFalse(form.isAdmin());
		form = new UserForm("user", "pw", "pw2", false, true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertTrue(form.isAdmin());
	}
}
