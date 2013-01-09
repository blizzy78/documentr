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
package de.blizzy.documentr.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import de.blizzy.documentr.access.AuthenticationUtil;

@Component("authCreationTimeFilter")
public class AuthenticationCreationTimeFilter implements Filter {
	private static final String AUTHENTICATION_HASH_CODE = "authenticationHashCode"; //$NON-NLS-1$

	@Override
	public void init(FilterConfig filterConfig) {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		int hashCode = getHashCode(authentication);

		HttpSession session = ((HttpServletRequest) request).getSession();
		Integer lastAuthenticationHashCode = (Integer) session.getAttribute(AUTHENTICATION_HASH_CODE);
		if ((lastAuthenticationHashCode == null) || (lastAuthenticationHashCode != hashCode)) {
			session.setAttribute(AUTHENTICATION_HASH_CODE, hashCode);
			AuthenticationUtil.setAuthenticationCreationTime(session, System.currentTimeMillis());
		}

		chain.doFilter(request, response);
	}

	private int getHashCode(Authentication authentication) {
		int result = StringUtils.defaultString(authentication.getName()).hashCode();
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			result ^= authority.hashCode();
		}
		return result;
	}
}
