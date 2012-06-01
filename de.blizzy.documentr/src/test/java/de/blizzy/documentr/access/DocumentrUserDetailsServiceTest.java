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
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class DocumentrUserDetailsServiceTest {
	private UserStore userStore;
	private DocumentrUserDetailsService userDetailsService;

	@Before
	public void setUp() {
		userStore = mock(UserStore.class);
		
		userDetailsService = new DocumentrUserDetailsService();
		userDetailsService.setUserStore(userStore);
	}
	
	@Test
	public void loadUserByUsername() throws IOException {
		User user = new User("user", "pw", "email", false, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(userStore.getUser("user")).thenReturn(user); //$NON-NLS-1$
		
		UserDetails details = userDetailsService.loadUserByUsername("user"); //$NON-NLS-1$
		assertEquals("user", details.getUsername()); //$NON-NLS-1$
		assertTrue(details.isEnabled());
	}
	
	@Test
	public void loadUserByUsernameAdmin() throws IOException {
		User user = new User("user", "pw", "email", false, true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(userStore.getUser("user")).thenReturn(user); //$NON-NLS-1$
		
		UserDetails details = userDetailsService.loadUserByUsername("user"); //$NON-NLS-1$
		PermissionGrantedAuthority authority = new PermissionGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, Permission.ADMIN);
		assertTrue(new HashSet<GrantedAuthority>(details.getAuthorities()).contains(authority));
	}
	
	@Test
	public void loadUserByUsernameDisabled() throws IOException {
		User user = new User("user", "pw", "email", true, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(userStore.getUser("user")).thenReturn(user); //$NON-NLS-1$
		
		UserDetails details = userDetailsService.loadUserByUsername("user"); //$NON-NLS-1$
		assertFalse(details.isEnabled());
	}

	@Test(expected=UsernameNotFoundException.class)
	public void loadUserByUsernameUnknown() throws IOException {
		when(userStore.getUser("nonexistent")).thenThrow(new UserNotFoundException("nonexistent")); //$NON-NLS-1$ //$NON-NLS-2$
		
		userDetailsService.loadUserByUsername("nonexistent"); //$NON-NLS-1$
	}
}
