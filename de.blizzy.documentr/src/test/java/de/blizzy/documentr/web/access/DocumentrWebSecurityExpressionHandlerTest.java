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

import static org.junit.Assert.*;

import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.DocumentrSecurityExpressionRoot;

public class DocumentrWebSecurityExpressionHandlerTest extends AbstractDocumentrTest {
	@Mock
	private DocumentrPermissionEvaluator permissionEvaluator;
	@Mock
	private Authentication authentication;
	@Mock
	private FilterInvocation filterInvocation;

	@Test
	public void createSecurityExpressionRootMustCreateDocumentrSecurityExpressionRoot() {
		DocumentrWebSecurityExpressionHandler expressionHandler = new DocumentrWebSecurityExpressionHandler();
		expressionHandler.setPermissionEvaluator(permissionEvaluator);

		SecurityExpressionOperations root = expressionHandler.createSecurityExpressionRoot(
				authentication, filterInvocation);
		assertTrue(root instanceof DocumentrSecurityExpressionRoot);
	}
}
