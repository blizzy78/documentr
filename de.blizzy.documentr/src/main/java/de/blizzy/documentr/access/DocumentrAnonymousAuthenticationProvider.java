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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import de.blizzy.documentr.DocumentrConstants;

@Component //("anonymousAuthenticationProvider")
public class DocumentrAnonymousAuthenticationProvider implements AuthenticationProvider {
	private int keyHash = DocumentrConstants.ANONYMOUS_AUTH_KEY.hashCode();
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Assert.isTrue(supports(authentication.getClass()));
		
		if (((DocumentrAnonymousAuthentication) authentication).getKeyHash() != keyHash) {
			throw new BadCredentialsException("authentication does not contain the correct key"); //$NON-NLS-1$
		}
		
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>(authentication.getAuthorities());
		authorities.add(new PermissionGrantedAuthority(GrantedAuthorityTarget.APPLICATION, Permission.VIEW));
		return new AnonymousAuthenticationToken(UUID.randomUUID().toString(), authentication.getPrincipal(), authorities);
	}

	@Override
	public boolean supports(Class<?> authenticationClass) {
		return DocumentrAnonymousAuthentication.class.isAssignableFrom(authenticationClass);
	}
}
