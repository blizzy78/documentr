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
package de.blizzy.documentr.validation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import de.blizzy.documentr.access.Role;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.validation.RoleExistsValidator;

public class RoleExistsValidatorTest {
	@Test
	public void isValid() throws IOException {
		UserStore userStore = mock(UserStore.class);
		Role role = mock(Role.class);
		when(userStore.getRole("role")).thenReturn(role); //$NON-NLS-1$

		RoleExistsValidator validator = new RoleExistsValidator();
		validator.setUserStore(userStore);
		
		assertTrue(validator.isValid(null, null));
		assertTrue(validator.isValid(StringUtils.EMPTY, null));
		assertTrue(validator.isValid("role", null)); //$NON-NLS-1$
		assertFalse(validator.isValid("role2", null)); //$NON-NLS-1$
	}
}
