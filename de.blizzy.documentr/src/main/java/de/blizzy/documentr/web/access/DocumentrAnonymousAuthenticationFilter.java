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

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.stereotype.Component;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.DocumentrAnonymousAuthentication;

@Component("anonymousAuthFilter")
public class DocumentrAnonymousAuthenticationFilter extends AnonymousAuthenticationFilter {
	public DocumentrAnonymousAuthenticationFilter() {
		super(DocumentrConstants.ANONYMOUS_AUTH_KEY);
	}
	
	@Override
	protected Authentication createAuthentication(HttpServletRequest request) {
		Authentication auth = super.createAuthentication(request);
		DocumentrAnonymousAuthentication authentication = new DocumentrAnonymousAuthentication(auth.getPrincipal());
		authentication.setDetails(auth.getDetails());
		return authentication;
	}
}
