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
package de.blizzy.documentr.access;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.google.common.collect.Sets;

import de.blizzy.documentr.AbstractDocumentrTest;

public class DocumentrUserDetailsServiceTest extends AbstractDocumentrTest {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Mock
	private UserStore userStore;
	@InjectMocks
	private LoginNameUserDetailsService userDetailsService;

	@Test
	public void loadUserByUsername() throws IOException {
		User user = new User("user", "pw", "email", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(userStore.getUser("user")).thenReturn(user); //$NON-NLS-1$
		RoleGrantedAuthority roleAuthority = new RoleGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, "administrator"); //$NON-NLS-1$
		when(userStore.getUserAuthorities("user")).thenReturn(Collections.singletonList(roleAuthority)); //$NON-NLS-1$
		Role role = new Role("administrator", EnumSet.of(Permission.ADMIN)); //$NON-NLS-1$
		when(userStore.getRole("administrator")).thenReturn(role); //$NON-NLS-1$
		PermissionGrantedAuthority authority = new PermissionGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, Permission.ADMIN);
		when(userStore.toPermissionGrantedAuthorities(roleAuthority)).thenReturn(Collections.singleton(authority));

		UserDetails details = userDetailsService.loadUserByUsername("user"); //$NON-NLS-1$
		assertEquals("user", details.getUsername()); //$NON-NLS-1$
		assertTrue(details.isEnabled());
		assertTrue(Sets.newHashSet(details.getAuthorities()).contains(authority));
	}

	@Test
	public void loadUserByUsernameDisabled() throws IOException {
		User user = new User("user", "pw", "email", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(userStore.getUser("user")).thenReturn(user); //$NON-NLS-1$

		UserDetails details = userDetailsService.loadUserByUsername("user"); //$NON-NLS-1$
		assertFalse(details.isEnabled());
	}

	@Test
	public void loadUserByUsernameUnknown() throws IOException {
		when(userStore.getUser("nonexistent")).thenThrow(new UserNotFoundException("nonexistent")); //$NON-NLS-1$ //$NON-NLS-2$

		expectedException.expect(UsernameNotFoundException.class);
		userDetailsService.loadUserByUsername("nonexistent"); //$NON-NLS-1$
	}
}
