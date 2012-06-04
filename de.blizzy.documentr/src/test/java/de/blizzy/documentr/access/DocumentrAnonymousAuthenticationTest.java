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

import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;

import com.google.common.collect.Sets;

public class DocumentrAnonymousAuthenticationTest {
	private static final String USER = "user"; //$NON-NLS-1$
	private static final String KEY = "key"; //$NON-NLS-1$
	
	private Set<GrantedAuthority> authorities;
	private DocumentrAnonymousAuthentication authentication;
	
	@Before
	public void setUp() {
		authorities = Collections.<GrantedAuthority>singleton(
				new PermissionGrantedAuthority(GrantedAuthorityTarget.APPLICATION, Permission.VIEW));
		
		authentication = new DocumentrAnonymousAuthentication(KEY, USER, authorities);
	}
	
	@Test
	public void getKeyHash() {
		assertEquals(KEY.hashCode(), authentication.getKeyHash());
	}

	@Test
	public void getPrincipal() {
		assertEquals(USER, authentication.getPrincipal());
	}
	
	@Test
	public void getAuthorities() {
		assertTrue(Sets.newHashSet(authentication.getAuthorities()).containsAll(authorities));
	}
}
