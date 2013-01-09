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
package de.blizzy.documentr.web.access;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.stereotype.Component;

import de.blizzy.documentr.access.DocumentrSecurityExpressionRoot;
import de.blizzy.documentr.repository.GlobalRepositoryManager;

@Component("webExpressionHandler")
public class DocumentrWebSecurityExpressionHandler extends DefaultWebSecurityExpressionHandler {
	@Autowired
	private GlobalRepositoryManager repoManager;
	@Autowired
	private PermissionEvaluator permissionEvaluator;

	@PostConstruct
	public void init() {
		setPermissionEvaluator(permissionEvaluator);
	}

	@Override
	protected SecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication, FilterInvocation fi) {
		DocumentrSecurityExpressionRoot root = new DocumentrSecurityExpressionRoot(authentication, repoManager);
		root.setRequest(fi.getRequest());
        root.setPermissionEvaluator(getPermissionEvaluator());
        return root;
	}
}
