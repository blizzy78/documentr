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

import static de.blizzy.documentr.DocumentrMatchers.*;
import static de.blizzy.documentr.TestUtil.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;

import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserNotFoundException;
import de.blizzy.documentr.access.UserStore;

public class UserControllerTest {
	private static final User USER = new User("currentUser", "pw", "admin@example.com", false, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	private UserStore userStore;
	private ShaPasswordEncoder passwordEncoder;
	private UserController userController;
	private Authentication authentication;

	@Before
	public void setUp() throws IOException {
		userStore = mock(UserStore.class);
		when(userStore.getUser(USER.getLoginName())).thenReturn(USER);

		passwordEncoder = new ShaPasswordEncoder();
		
		userController = new UserController();
		userController.setUserStore(userStore);
		userController.setPasswordEncoder(passwordEncoder);
		
		authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn(USER.getLoginName());
	}
	
	@Test
	public void addUser() {
		Model model = mock(Model.class);
		String view = userController.addUser(model);
		assertEquals("/user/edit", view); //$NON-NLS-1$
		
		verify(model).addAttribute(eq("userForm"), //$NON-NLS-1$
				argUserForm(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, false, false));
	}
	
	@Test
	public void saveUser() throws IOException {
		UserForm user = new UserForm("user", "pw", "pw", "email", true, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(user, "userForm"); //$NON-NLS-1$
		when(userStore.getUser("user")).thenThrow(new UserNotFoundException("user")); //$NON-NLS-1$ //$NON-NLS-2$
		
		String view = userController.saveUser(user, bindingResult, authentication);
		assertEquals("/users", removeViewPrefix(view)); //$NON-NLS-1$
		assertRedirect(view);
		assertFalse(bindingResult.hasErrors());

		String passwordHash = passwordEncoder.encodePassword("pw", "user"); //$NON-NLS-1$ //$NON-NLS-2$
		verify(userStore).saveUser(argUser("user", passwordHash, "email", true, false), same(USER)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Test
	public void saveExistingUser() throws IOException {
		UserForm user = new UserForm("user", "newPW", "newPW", "email", true, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(user, "userForm"); //$NON-NLS-1$

		User oldUser = new User("user", "oldPW", "email", false, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(userStore.getUser("user")).thenReturn(oldUser); //$NON-NLS-1$
		
		String view = userController.saveUser(user, bindingResult, authentication);
		assertEquals("/users", removeViewPrefix(view)); //$NON-NLS-1$
		assertRedirect(view);
		assertFalse(bindingResult.hasErrors());
		
		String passwordHash = passwordEncoder.encodePassword("newPW", "user"); //$NON-NLS-1$ //$NON-NLS-2$
		verify(userStore).saveUser(argUser("user", passwordHash, "email", true, false), same(USER)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Test
	public void saveExistingUserButKeepPassword() throws IOException {
		UserForm user = new UserForm("user", StringUtils.EMPTY, StringUtils.EMPTY, "email", true, false); //$NON-NLS-1$ //$NON-NLS-2$
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(user, "userForm"); //$NON-NLS-1$
		
		User oldUser = new User("user", "oldPW", "email", false, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(userStore.getUser("user")).thenReturn(oldUser); //$NON-NLS-1$
		
		String view = userController.saveUser(user, bindingResult, authentication);
		assertEquals("/users", removeViewPrefix(view)); //$NON-NLS-1$
		assertRedirect(view);
		assertFalse(bindingResult.hasErrors());
		
		verify(userStore).saveUser(argUser("user", "oldPW", "email", true, false), same(USER)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	@Test
	public void saveUserPassword1Blank() throws IOException {
		UserForm user = new UserForm("user", StringUtils.EMPTY, "pw", "email", true, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(user, "userForm"); //$NON-NLS-1$
		
		String view = userController.saveUser(user, bindingResult, authentication);
		assertEquals("/user/edit", view); //$NON-NLS-1$
		assertTrue(bindingResult.hasErrors());
		assertTrue(bindingResult.hasFieldErrors("password1")); //$NON-NLS-1$
	}

	@Test
	public void saveUserPassword2Blank() throws IOException {
		UserForm user = new UserForm("user", "pw", StringUtils.EMPTY, "email", true, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(user, "userForm"); //$NON-NLS-1$
		
		String view = userController.saveUser(user, bindingResult, authentication);
		assertEquals("/user/edit", view); //$NON-NLS-1$
		assertTrue(bindingResult.hasErrors());
		assertTrue(bindingResult.hasFieldErrors("password2")); //$NON-NLS-1$
	}
	
	@Test
	public void saveUserPasswordsDiffer() throws IOException {
		UserForm user = new UserForm("user", "pw", "pw2", "email", true, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(user, "userForm"); //$NON-NLS-1$
		
		String view = userController.saveUser(user, bindingResult, authentication);
		assertEquals("/user/edit", view); //$NON-NLS-1$
		assertTrue(bindingResult.hasErrors());
		assertTrue(bindingResult.hasFieldErrors("password1")); //$NON-NLS-1$
		assertTrue(bindingResult.hasFieldErrors("password2")); //$NON-NLS-1$
	}
	
	@Test
	public void editUser() throws IOException {
		User user = new User("user", "pw", "email", false, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(userStore.getUser("user")).thenReturn(user); //$NON-NLS-1$

		Model model = mock(Model.class);
		String view = userController.editUser("user", model); //$NON-NLS-1$
		assertEquals("/user/edit", view); //$NON-NLS-1$
		
		verify(model).addAttribute(eq("userForm"), //$NON-NLS-1$
				argUserForm("user", StringUtils.EMPTY, StringUtils.EMPTY, false, false)); //$NON-NLS-1$
	}
}
