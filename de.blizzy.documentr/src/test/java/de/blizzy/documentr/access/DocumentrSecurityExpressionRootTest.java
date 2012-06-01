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
import static org.mockito.Mockito.*;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import de.blizzy.documentr.access.GrantedAuthorityTarget.Type;
import de.blizzy.documentr.repository.GlobalRepositoryManager;

public class DocumentrSecurityExpressionRootTest {
	private Authentication authentication;
	private GlobalRepositoryManager repoManager;
	private DocumentrSecurityExpressionRoot root;
	private PermissionEvaluator permissionEvaluator;

	@Before
	public void setUp() {
		authentication = mock(Authentication.class);
		repoManager = mock(GlobalRepositoryManager.class);
		permissionEvaluator = mock(PermissionEvaluator.class);
		
		root = new DocumentrSecurityExpressionRoot(authentication, repoManager);
		root.setPermissionEvaluator(permissionEvaluator);
	}
	
	@Test
	@SuppressWarnings("boxing")
	public void hasApplicationPermission() {
		when(permissionEvaluator.hasPermission(authentication, GrantedAuthorityTarget.APPLICATION_TARGET_ID,
				Type.APPLICATION.name(), Permission.EDIT_PAGE.name())).thenReturn(true);
		assertTrue(root.hasApplicationPermission(Permission.EDIT_PAGE.name()));
	}
	
	@Test
	@SuppressWarnings("boxing")
	public void hasProjectPermission() {
		when(permissionEvaluator.hasPermission(authentication, "project", //$NON-NLS-1$
				Type.PROJECT.name(), Permission.EDIT_PAGE.name())).thenReturn(true);
		assertTrue(root.hasProjectPermission("project", Permission.EDIT_PAGE.name())); //$NON-NLS-1$
	}
	
	@Test
	@SuppressWarnings("boxing")
	public void hasAnyProjectPermission() {
		when(permissionEvaluator.hasPermission(authentication, GrantedAuthorityTarget.ANY,
				Type.PROJECT.name(), Permission.EDIT_PAGE.name())).thenReturn(true);
		assertTrue(root.hasAnyProjectPermission(Permission.EDIT_PAGE.name()));
	}
	
	@Test
	@SuppressWarnings("boxing")
	public void hasBranchPermission() {
		when(permissionEvaluator.hasPermission(authentication, "project/branch", //$NON-NLS-1$
				Type.BRANCH.name(), Permission.EDIT_PAGE.name())).thenReturn(true);
		assertTrue(root.hasBranchPermission("project", "branch", Permission.EDIT_PAGE.name())); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Test
	@SuppressWarnings("boxing")
	public void hasAnyBranchPermission() {
		when(permissionEvaluator.hasPermission(authentication, "project/" + GrantedAuthorityTarget.ANY, //$NON-NLS-1$
				Type.BRANCH.name(), Permission.EDIT_PAGE.name())).thenReturn(true);
		assertTrue(root.hasAnyBranchPermission("project", Permission.EDIT_PAGE.name())); //$NON-NLS-1$
	}
	
	@Test
	@SuppressWarnings("boxing")
	public void hasPagePermission() {
		when(permissionEvaluator.hasPermission(authentication, "project/branch/home,foo", //$NON-NLS-1$
				Type.PAGE.name(), Permission.EDIT_PAGE.name())).thenReturn(true);
		assertTrue(root.hasPagePermission("project", "branch", "home/foo", Permission.EDIT_PAGE.name())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Test
	public void projectExists() {
		when(repoManager.listProjects()).thenReturn(Arrays.asList("project1", "project2", "project3")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertTrue(root.projectExists("project1")); //$NON-NLS-1$
	}
	
	@Test
	public void setAndGetThis() {
		Object target = "this"; //$NON-NLS-1$
		root.setThis(target);
		assertSame(target, root.getThis());
	}
	
	@Test
	public void setAndGetRequest() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		root.setRequest(request);
		assertSame(request, root.getRequest());
	}
}
