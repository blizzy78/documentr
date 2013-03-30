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

import javax.annotation.PostConstruct;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import de.blizzy.documentr.repository.IGlobalRepositoryManager;

/** documentr's security expression handler. */
@Component("expressionHandler")
public class DocumentrMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {
	@Autowired
	private IGlobalRepositoryManager repoManager;
	@Autowired
	private PermissionEvaluator permissionEvaluator;

	@PostConstruct
	public void init() {
		setPermissionEvaluator(permissionEvaluator);
	}

	@Override
	protected MethodSecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication,
			MethodInvocation invocation) {

		DocumentrSecurityExpressionRoot root =
				new DocumentrSecurityExpressionRoot(authentication, repoManager);
		root.setThis(invocation.getThis());
		root.setPermissionEvaluator(getPermissionEvaluator());

		return root;
	}
}
