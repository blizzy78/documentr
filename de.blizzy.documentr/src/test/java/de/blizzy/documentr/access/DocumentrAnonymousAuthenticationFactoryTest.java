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

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import com.google.common.collect.Lists;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.access.GrantedAuthorityTarget.Type;

public class DocumentrAnonymousAuthenticationFactoryTest extends AbstractDocumentrTest {
	@Mock
	private UserStore userStore;
	@InjectMocks
	private DocumentrAnonymousAuthenticationFactory factory;

	@Test
	public void create() throws IOException {
		RoleGrantedAuthority applicationReaderRoleAuthority =
				new RoleGrantedAuthority(GrantedAuthorityTarget.APPLICATION, "reader"); //$NON-NLS-1$
		RoleGrantedAuthority projectAdminRoleAuthority =
				new RoleGrantedAuthority(new GrantedAuthorityTarget("project", Type.PROJECT), "admin"); //$NON-NLS-1$ //$NON-NLS-2$
		when(userStore.getUserAuthorities(UserStore.ANONYMOUS_USER_LOGIN_NAME))
			.thenReturn(Lists.newArrayList(applicationReaderRoleAuthority, projectAdminRoleAuthority));

		PermissionGrantedAuthority applicationViewAuthority =
				new PermissionGrantedAuthority(applicationReaderRoleAuthority.getTarget(), Permission.VIEW);
		PermissionGrantedAuthority projectAdminAuthority =
				new PermissionGrantedAuthority(projectAdminRoleAuthority.getTarget(), Permission.ADMIN);
		when(userStore.toPermissionGrantedAuthorities(applicationReaderRoleAuthority))
			.thenReturn(Collections.singleton(applicationViewAuthority));
		when(userStore.toPermissionGrantedAuthorities(projectAdminRoleAuthority))
			.thenReturn(Collections.singleton(projectAdminAuthority));

		String key = "key"; //$NON-NLS-1$
		AbstractAuthenticationToken authentication = factory.create(key);
		assertEquals(key.hashCode(), ((AnonymousAuthenticationToken) authentication).getKeyHash());
		assertSame(UserStore.ANONYMOUS_USER_LOGIN_NAME, authentication.getPrincipal());
		assertTrue(authentication.getAuthorities().contains(applicationViewAuthority));
		assertTrue(authentication.getAuthorities().contains(projectAdminAuthority));
	}
}
