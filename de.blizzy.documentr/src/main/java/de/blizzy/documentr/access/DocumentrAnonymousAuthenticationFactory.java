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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class DocumentrAnonymousAuthenticationFactory {
	@Autowired
	private UserStore userStore;
	
	public AbstractAuthenticationToken create(String key, Object principal) throws IOException {
		List<RoleGrantedAuthority> userAuthorities = userStore.getUserAuthorities(UserStore.ANONYMOUS_USER_LOGIN_NAME);
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		for (RoleGrantedAuthority rga : userAuthorities) {
			authorities.addAll(userStore.toPermissionGrantedAuthorities(rga));
		}
		// must have at least one authority
		authorities.add(new SimpleGrantedAuthority("ROLE_ANONYMOUS")); //$NON-NLS-1$
		
		return new DocumentrAnonymousAuthentication(key, principal, authorities);
	}
}
