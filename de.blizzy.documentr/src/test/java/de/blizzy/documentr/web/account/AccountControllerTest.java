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
package de.blizzy.documentr.web.account;

import static de.blizzy.documentr.TestUtil.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.springframework.security.core.Authentication;

import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;

public class AccountControllerTest {
	private UserStore userStore;
	private AccountController accountController;

	@Before
	public void setUp() {
		userStore = mock(UserStore.class);

		accountController = new AccountController();
		accountController.setUserStore(userStore);
	}
	
	@Test
	public void getMyAccount() {
		assertEquals("/account/index", accountController.getMyAccount()); //$NON-NLS-1$
	}
	
	@Test
	public void getMyOpenIds() {
		assertEquals("/account/openId", accountController.getMyOpenIds()); //$NON-NLS-1$
	}
	
	@Test
	public void removeOpenId() throws IOException {
		User user = mock(User.class);
		when(userStore.getUser("user")).thenReturn(user); //$NON-NLS-1$

		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn("user"); //$NON-NLS-1$
		
		String view = accountController.removeOpenId("openId", authentication); //$NON-NLS-1$
		assertEquals("/account/openId", removeViewPrefix(view)); //$NON-NLS-1$
		assertRedirect(view);
		
		InOrder inOrder = inOrder(user, userStore);
		inOrder.verify(user).removeOpenId("openId"); //$NON-NLS-1$
		inOrder.verify(userStore).saveUser(same(user), same(user));
	}
}
