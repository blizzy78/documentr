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

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;

import com.google.common.collect.Lists;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.repository.GlobalRepositoryManager;

public class DocumentrSecurityExpressionRootTest extends AbstractDocumentrTest {
	@Mock
	private Authentication authentication;
	@Mock
	private GlobalRepositoryManager repoManager;
	@Mock
	private DocumentrPermissionEvaluator permissionEvaluator;
	@InjectMocks
	private DocumentrSecurityExpressionRoot root;

	@Test
	public void hasApplicationPermission() {
		when(permissionEvaluator.hasApplicationPermission(authentication, Permission.EDIT_PAGE)).thenReturn(true);
		assertTrue(root.hasApplicationPermission(Permission.EDIT_PAGE));
	}

	@Test
	public void hasProjectPermission() {
		when(permissionEvaluator.hasProjectPermission(authentication, "project", Permission.EDIT_PAGE)).thenReturn(true); //$NON-NLS-1$
		assertTrue(root.hasProjectPermission("project", Permission.EDIT_PAGE)); //$NON-NLS-1$
	}

	@Test
	public void hasAnyProjectPermission() {
		when(permissionEvaluator.hasAnyProjectPermission(authentication, Permission.EDIT_PAGE)).thenReturn(true);
		assertTrue(root.hasAnyProjectPermission(Permission.EDIT_PAGE));
	}

	@Test
	public void hasBranchPermission() {
		when(permissionEvaluator.hasBranchPermission(authentication, "project", "branch", Permission.EDIT_PAGE)) //$NON-NLS-1$ //$NON-NLS-2$
			.thenReturn(true);
		assertTrue(root.hasBranchPermission("project", "branch", Permission.EDIT_PAGE)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void hasAnyBranchPermission() {
		when(permissionEvaluator.hasAnyBranchPermission(authentication, "project", Permission.EDIT_PAGE)) //$NON-NLS-1$
			.thenReturn(true);
		assertTrue(root.hasAnyBranchPermission("project", Permission.EDIT_PAGE)); //$NON-NLS-1$
	}

	@Test
	public void hasPagePermission() {
		when(permissionEvaluator.hasPagePermission(authentication, "project", "branch", //$NON-NLS-1$ //$NON-NLS-2$
				DocumentrConstants.HOME_PAGE_NAME + "/foo", Permission.EDIT_PAGE)).thenReturn(true); //$NON-NLS-1$
		assertTrue(root.hasPagePermission("project", "branch", //$NON-NLS-1$ //$NON-NLS-2$
				DocumentrConstants.HOME_PAGE_NAME + ",foo", Permission.EDIT_PAGE)); //$NON-NLS-1$
	}

	@Test
	public void projectExists() {
		when(repoManager.listProjects()).thenReturn(Lists.newArrayList("project1", "project2", "project3")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertTrue(root.projectExists("project1")); //$NON-NLS-1$
	}

	@Test
	public void setAndGetThis() {
		Object target = "this"; //$NON-NLS-1$
		root.setThis(target);
		assertSame(target, root.getThis());
	}
}
