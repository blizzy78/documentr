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
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;

import com.google.common.collect.Sets;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.access.OpenId;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserNotFoundException;
import de.blizzy.documentr.access.UserStore;

public class UserControllerTest extends AbstractDocumentrTest {
	private static final User USER = new User("currentUser", "pw", "admin@example.com", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	@Mock
	private UserStore userStore;
	@Mock
	private Authentication authentication;
	@Mock
	private Model model;
	private PasswordEncoder passwordEncoder;
	@InjectMocks
	private UserController userController;

	@Before
	public void setUp() throws IOException {
		when(userStore.getUser(USER.getLoginName())).thenReturn(USER);

		passwordEncoder = new ShaPasswordEncoder();

		Whitebox.setInternalState(userController, passwordEncoder);

		when(authentication.getName()).thenReturn(USER.getLoginName());
	}

	@Test
	public void addUser() {
		String view = userController.addUser(model);
		assertEquals("/user/edit", view); //$NON-NLS-1$

		verify(model).addAttribute(eq("userForm"), //$NON-NLS-1$
				argUserForm(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, false));
	}

	@Test
	public void saveUser() throws IOException {
		UserForm user = new UserForm("user", "user", "pw", "pw", "email", true, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(user, "userForm"); //$NON-NLS-1$
		when(userStore.getUser("user")).thenThrow(new UserNotFoundException("user")); //$NON-NLS-1$ //$NON-NLS-2$

		String view = userController.saveUser(user, bindingResult, authentication);
		assertEquals("/users", removeViewPrefix(view)); //$NON-NLS-1$
		assertRedirect(view);
		assertFalse(bindingResult.hasErrors());

		String passwordHash = passwordEncoder.encodePassword("pw", "user"); //$NON-NLS-1$ //$NON-NLS-2$
		verify(userStore).saveUser(argUser("user", passwordHash, "email", true), same(USER)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void saveExistingUser() throws IOException {
		UserForm user = new UserForm("user", "user", "newPW", "newPW", "email", true, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(user, "userForm"); //$NON-NLS-1$

		User oldUser = new User("user", "oldPW", "email", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		Set<OpenId> openIds = Sets.newHashSet(
				new OpenId("openId1", "realId1"), //$NON-NLS-1$ //$NON-NLS-2$
				new OpenId("openId2", "realId2")); //$NON-NLS-1$ //$NON-NLS-2$
		for (OpenId openId : openIds) {
			oldUser.addOpenId(openId);
		}
		when(userStore.getUser("user")).thenReturn(oldUser); //$NON-NLS-1$

		String view = userController.saveUser(user, bindingResult, authentication);
		assertEquals("/users", removeViewPrefix(view)); //$NON-NLS-1$
		assertRedirect(view);
		assertFalse(bindingResult.hasErrors());

		String passwordHash = passwordEncoder.encodePassword("newPW", "user"); //$NON-NLS-1$ //$NON-NLS-2$
		verify(userStore).saveUser(argUser("user", passwordHash, "email", true, openIds), same(USER)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void saveExistingUserButKeepPassword() throws IOException {
		UserForm user = new UserForm("user", "user", StringUtils.EMPTY, StringUtils.EMPTY, "email", true, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(user, "userForm"); //$NON-NLS-1$

		User oldUser = new User("user", "oldPW", "email", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(userStore.getUser("user")).thenReturn(oldUser); //$NON-NLS-1$

		String view = userController.saveUser(user, bindingResult, authentication);
		assertEquals("/users", removeViewPrefix(view)); //$NON-NLS-1$
		assertRedirect(view);
		assertFalse(bindingResult.hasErrors());

		verify(userStore).saveUser(argUser("user", "oldPW", "email", true), same(USER)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Test
	public void saveUserPassword1Blank() throws IOException {
		UserForm user = new UserForm("user", "user", StringUtils.EMPTY, "pw", "email", true, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(user, "userForm"); //$NON-NLS-1$

		String view = userController.saveUser(user, bindingResult, authentication);
		assertEquals("/user/edit", view); //$NON-NLS-1$
		assertTrue(bindingResult.hasErrors());
		assertTrue(bindingResult.hasFieldErrors("password1")); //$NON-NLS-1$
	}

	@Test
	public void saveUserPassword2Blank() throws IOException {
		UserForm user = new UserForm("user", "user", "pw", StringUtils.EMPTY, "email", true, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(user, "userForm"); //$NON-NLS-1$

		String view = userController.saveUser(user, bindingResult, authentication);
		assertEquals("/user/edit", view); //$NON-NLS-1$
		assertTrue(bindingResult.hasErrors());
		assertTrue(bindingResult.hasFieldErrors("password2")); //$NON-NLS-1$
	}

	@Test
	public void saveUserPasswordsDiffer() throws IOException {
		UserForm user = new UserForm("user", "user", "pw", "pw2", "email", true, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(user, "userForm"); //$NON-NLS-1$

		String view = userController.saveUser(user, bindingResult, authentication);
		assertEquals("/user/edit", view); //$NON-NLS-1$
		assertTrue(bindingResult.hasErrors());
		assertTrue(bindingResult.hasFieldErrors("password1")); //$NON-NLS-1$
		assertTrue(bindingResult.hasFieldErrors("password2")); //$NON-NLS-1$
	}

	@Test
	public void editUser() throws IOException {
		User user = new User("user", "pw", "email", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(userStore.getUser("user")).thenReturn(user); //$NON-NLS-1$

		String view = userController.editUser("user", model); //$NON-NLS-1$
		assertEquals("/user/edit", view); //$NON-NLS-1$

		verify(model).addAttribute(eq("userForm"), //$NON-NLS-1$
				argUserForm("user", StringUtils.EMPTY, StringUtils.EMPTY, false)); //$NON-NLS-1$
	}

	@Test
	public void deleteUser() throws IOException {
		String view = userController.deleteUser("user", authentication); //$NON-NLS-1$
		assertEquals("/users", removeViewPrefix(view)); //$NON-NLS-1$
		assertRedirect(view);

		verify(userStore).deleteUser("user", USER); //$NON-NLS-1$
	}
}
