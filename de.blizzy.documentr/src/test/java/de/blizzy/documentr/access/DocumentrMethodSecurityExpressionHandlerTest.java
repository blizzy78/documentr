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

import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.springframework.security.core.Authentication;

import de.blizzy.documentr.AbstractDocumentrTest;

public class DocumentrMethodSecurityExpressionHandlerTest extends AbstractDocumentrTest {
	@Mock
	private MethodInvocation methodInvocation;
	@Mock
	private DocumentrPermissionEvaluator permissionEvaluator;
	@Mock
	private Authentication authentication;
	@InjectMocks
	private DocumentrMethodSecurityExpressionHandler expressionHandler;

	@Test
	public void createSecurityExpressionRoot() {
		Object target = "this"; //$NON-NLS-1$
		when(methodInvocation.getThis()).thenReturn(target);

		DocumentrSecurityExpressionRoot root =
				(DocumentrSecurityExpressionRoot) expressionHandler.createSecurityExpressionRoot(
						authentication, methodInvocation);
		assertSame(target, root.getThis());
		assertSame(permissionEvaluator, Whitebox.getInternalState(root, DocumentrPermissionEvaluator.class));
	}
}
