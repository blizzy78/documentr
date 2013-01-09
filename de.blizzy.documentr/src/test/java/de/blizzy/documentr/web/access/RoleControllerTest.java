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
package de.blizzy.documentr.web.access;

import static de.blizzy.documentr.DocumentrMatchers.*;
import static de.blizzy.documentr.TestUtil.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import com.google.common.collect.Sets;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.DocumentrMatchers;
import de.blizzy.documentr.access.Permission;
import de.blizzy.documentr.access.Role;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;

public class RoleControllerTest extends AbstractDocumentrTest {
	private static final User USER = new User("currentUser", "pw", "admin@example.com", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	@Mock
	private UserStore userStore;
	@Mock
	private Authentication authentication;
	@Mock
	private Model model;
	@InjectMocks
	private RoleController roleController;

	@Before
	public void setUp() throws IOException {
		when(userStore.getUser(USER.getLoginName())).thenReturn(USER);

		when(authentication.getName()).thenReturn(USER.getLoginName());
	}

	@Test
	public void addRole() {
		String view = roleController.addRole(model);
		assertEquals("/user/role/edit", view); //$NON-NLS-1$

		verify(model).addAttribute(eq("roleForm"), DocumentrMatchers.argRoleForm(null, Sets.<String>newHashSet())); //$NON-NLS-1$
	}

	@Test
	public void editRole() throws IOException {
		when(userStore.getRole("role")).thenReturn( //$NON-NLS-1$
			new Role("role", EnumSet.of(Permission.EDIT_BRANCH, Permission.EDIT_PAGE))); //$NON-NLS-1$

		String view = roleController.editRole("role", model); //$NON-NLS-1$
		assertEquals("/user/role/edit", view); //$NON-NLS-1$

		verify(model).addAttribute(eq("roleForm"), argRoleForm( //$NON-NLS-1$
			"role", Sets.newHashSet(Permission.EDIT_BRANCH.name(), Permission.EDIT_PAGE.name()))); //$NON-NLS-1$
	}

	@Test
	public void saveRole() throws IOException {
		RoleForm form = new RoleForm("role", null, Sets.newHashSet( //$NON-NLS-1$
			Permission.EDIT_BRANCH.name(), Permission.EDIT_PAGE.name()));
		BindingResult bindingResult = new BeanPropertyBindingResult(form, "roleForm"); //$NON-NLS-1$
		String view = roleController.saveRole(form, bindingResult, authentication);
		assertEquals("/roles", removeViewPrefix(view)); //$NON-NLS-1$
		assertRedirect(view);
		assertFalse(bindingResult.hasErrors());

		verify(userStore).saveRole(
			argRole("role", EnumSet.of(Permission.EDIT_BRANCH, Permission.EDIT_PAGE)), //$NON-NLS-1$
			same(USER));
	}
}
