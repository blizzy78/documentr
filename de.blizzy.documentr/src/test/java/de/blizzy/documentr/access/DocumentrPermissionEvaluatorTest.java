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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.google.common.collect.Sets;

import de.blizzy.documentr.access.GrantedAuthorityTarget.Type;

public class DocumentrPermissionEvaluatorTest {
	private DocumentrPermissionEvaluator documentrPermissionEvaluator;

	@Before
	public void setUp() {
		documentrPermissionEvaluator = new DocumentrPermissionEvaluator();
	}
	
	@Test
	public void hasApplicationPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, Permission.EDIT_PAGE));
		assertTrue(documentrPermissionEvaluator.hasPermission(
				authentication, GrantedAuthorityTarget.APPLICATION_TARGET_ID, Type.APPLICATION.name(),
				Permission.EDIT_PAGE.name()));
	}

	@Test
	public void hasApplicationPermissionMustHonorAdminPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, Permission.ADMIN));
		assertTrue(documentrPermissionEvaluator.hasPermission(
				authentication, GrantedAuthorityTarget.APPLICATION_TARGET_ID, Type.APPLICATION.name(),
				Permission.EDIT_PAGE.name()));
	}
	
	@Test
	public void hasProjectPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project", Type.PROJECT), Permission.EDIT_PAGE)); //$NON-NLS-1$
		assertTrue(documentrPermissionEvaluator.hasPermission(
				authentication, "project", Type.PROJECT.name(), Permission.EDIT_PAGE.name())); //$NON-NLS-1$
	}
	
	@Test
	public void hasProjectPermissionMustCheckApplication() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, Permission.EDIT_PAGE));
		assertTrue(documentrPermissionEvaluator.hasPermission(
				authentication, "project", Type.PROJECT.name(), Permission.EDIT_PAGE.name())); //$NON-NLS-1$
	}
	
	@Test
	public void hasProjectPermissionMustHonorAdminPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project", Type.PROJECT), Permission.ADMIN)); //$NON-NLS-1$
		assertTrue(documentrPermissionEvaluator.hasPermission(
				authentication, "project", Type.PROJECT.name(), Permission.EDIT_PAGE.name())); //$NON-NLS-1$
	}
	
	@Test
	public void hasAnyProjectPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project", Type.PROJECT), Permission.EDIT_PAGE)); //$NON-NLS-1$
		assertTrue(documentrPermissionEvaluator.hasPermission(
				authentication, GrantedAuthorityTarget.ANY, Type.PROJECT.name(), Permission.EDIT_PAGE.name()));
	}
	
	@Test
	public void hasAnyProjectPermissionMustCheckApplication() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, Permission.EDIT_PAGE));
		assertTrue(documentrPermissionEvaluator.hasPermission(
				authentication, GrantedAuthorityTarget.ANY, Type.PROJECT.name(), Permission.EDIT_PAGE.name()));
	}
	
	@Test
	public void hasAnyProjectPermissionMustHonorAdminPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project", Type.PROJECT), Permission.ADMIN)); //$NON-NLS-1$
		assertTrue(documentrPermissionEvaluator.hasPermission(
				authentication, GrantedAuthorityTarget.ANY, Type.PROJECT.name(), Permission.EDIT_PAGE.name()));
	}
	
	@Test
	public void hasBranchPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project/branch", Type.BRANCH), Permission.EDIT_PAGE)); //$NON-NLS-1$
		assertTrue(documentrPermissionEvaluator.hasPermission(
				authentication, "project/branch", Type.BRANCH.name(), Permission.EDIT_PAGE.name())); //$NON-NLS-1$
	}

	@Test
	public void hasBranchPermissionMustCheckProject() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project", Type.PROJECT), Permission.EDIT_PAGE)); //$NON-NLS-1$
		assertTrue(documentrPermissionEvaluator.hasPermission(
				authentication, "project/branch", Type.BRANCH.name(), Permission.EDIT_PAGE.name())); //$NON-NLS-1$
	}
	
	@Test
	public void hasBranchPermissionMustHonorAdminPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project/branch", Type.BRANCH), Permission.ADMIN)); //$NON-NLS-1$
		assertTrue(documentrPermissionEvaluator.hasPermission(
				authentication, "project/branch", Type.BRANCH.name(), Permission.EDIT_PAGE.name())); //$NON-NLS-1$
	}
	
	@Test
	public void hasAnyBranchPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project/branch", Type.BRANCH), Permission.EDIT_PAGE)); //$NON-NLS-1$
		assertTrue(documentrPermissionEvaluator.hasPermission(
				authentication, "project/*", Type.BRANCH.name(), Permission.EDIT_PAGE.name())); //$NON-NLS-1$
	}
	
	@Test
	public void hasAnyBranchPermissionMustCheckProject() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project", Type.PROJECT), Permission.EDIT_PAGE)); //$NON-NLS-1$
		assertTrue(documentrPermissionEvaluator.hasPermission(
				authentication, "project/*", Type.BRANCH.name(), Permission.EDIT_PAGE.name())); //$NON-NLS-1$
	}
	
	@Test
	public void hasAnyBranchPermissionMustHonorAdminPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project/branch", Type.BRANCH), Permission.ADMIN)); //$NON-NLS-1$
		assertTrue(documentrPermissionEvaluator.hasPermission(
				authentication, "project/*", Type.BRANCH.name(), Permission.EDIT_PAGE.name())); //$NON-NLS-1$
	}
	
	@Test
	@Ignore
	public void hasPagePermission() {
		// TODO
	}

	@Test
	public void hasPagePermissionMustCheckBranch() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project/branch", Type.BRANCH), Permission.EDIT_PAGE)); //$NON-NLS-1$
		assertTrue(documentrPermissionEvaluator.hasPermission(
				authentication, "project/branch/home,foo", Type.PAGE.name(), Permission.EDIT_PAGE.name())); //$NON-NLS-1$
	}
	
	private Authentication mockAuthentication(GrantedAuthority... authorities) {
		return new AbstractAuthenticationToken(Sets.newHashSet(authorities)) {
			@Override
			public Object getPrincipal() {
				return null;
			}
			
			@Override
			public Object getCredentials() {
				return null;
			}
		};
	}
}
