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

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.GrantedAuthorityTarget.Type;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;

public class DocumentrPermissionEvaluatorTest {
	private static final String USER = "user"; //$NON-NLS-1$
	
	private IPageStore pageStore;
	private UserStore userStore;
	private PermissionEvaluator permissionEvaluator;

	@Before
	public void setUp() {
		pageStore = mock(IPageStore.class);
		userStore = mock(UserStore.class);
		
		DocumentrPermissionEvaluator dpe = new DocumentrPermissionEvaluator();
		dpe.setPageStore(pageStore);
		dpe.setUserStore(userStore);
		permissionEvaluator = dpe;
	}
	
	@Test
	public void hasApplicationPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, Permission.EDIT_PAGE));
		assertTrue(permissionEvaluator.hasPermission(
				authentication, GrantedAuthorityTarget.APPLICATION_TARGET_ID, Type.APPLICATION.name(),
				Permission.EDIT_PAGE.name()));
	}

	@Test
	public void hasApplicationPermissionMustHonorAdminPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, Permission.ADMIN));
		assertTrue(permissionEvaluator.hasPermission(
				authentication, GrantedAuthorityTarget.APPLICATION_TARGET_ID, Type.APPLICATION.name(),
				Permission.EDIT_PAGE.name()));
	}
	
	@Test
	public void hasProjectPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project", Type.PROJECT), Permission.EDIT_PAGE)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasPermission(
				authentication, "project", Type.PROJECT.name(), Permission.EDIT_PAGE.name())); //$NON-NLS-1$
	}
	
	@Test
	public void hasProjectPermissionMustCheckApplication() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, Permission.EDIT_PAGE));
		assertTrue(permissionEvaluator.hasPermission(
				authentication, "project", Type.PROJECT.name(), Permission.EDIT_PAGE.name())); //$NON-NLS-1$
	}
	
	@Test
	public void hasProjectPermissionMustHonorAdminPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project", Type.PROJECT), Permission.ADMIN)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasPermission(
				authentication, "project", Type.PROJECT.name(), Permission.EDIT_PAGE.name())); //$NON-NLS-1$
	}
	
	@Test
	public void hasAnyProjectPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project", Type.PROJECT), Permission.EDIT_PAGE)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasPermission(
				authentication, GrantedAuthorityTarget.ANY, Type.PROJECT.name(), Permission.EDIT_PAGE.name()));
	}
	
	@Test
	public void hasAnyProjectPermissionMustCheckApplication() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, Permission.EDIT_PAGE));
		assertTrue(permissionEvaluator.hasPermission(
				authentication, GrantedAuthorityTarget.ANY, Type.PROJECT.name(), Permission.EDIT_PAGE.name()));
	}
	
	@Test
	public void hasAnyProjectPermissionMustHonorAdminPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project", Type.PROJECT), Permission.ADMIN)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasPermission(
				authentication, GrantedAuthorityTarget.ANY, Type.PROJECT.name(), Permission.EDIT_PAGE.name()));
	}
	
	@Test
	public void hasBranchPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project/branch", Type.BRANCH), Permission.EDIT_PAGE)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasPermission(
				authentication, "project/branch", Type.BRANCH.name(), Permission.EDIT_PAGE.name())); //$NON-NLS-1$
	}

	@Test
	public void hasBranchPermissionMustCheckProject() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project", Type.PROJECT), Permission.EDIT_PAGE)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasPermission(
				authentication, "project/branch", Type.BRANCH.name(), Permission.EDIT_PAGE.name())); //$NON-NLS-1$
	}
	
	@Test
	public void hasBranchPermissionMustHonorAdminPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project/branch", Type.BRANCH), Permission.ADMIN)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasPermission(
				authentication, "project/branch", Type.BRANCH.name(), Permission.EDIT_PAGE.name())); //$NON-NLS-1$
	}
	
	@Test
	public void hasAnyBranchPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project/branch", Type.BRANCH), Permission.EDIT_PAGE)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasPermission(
				authentication, "project/*", Type.BRANCH.name(), Permission.EDIT_PAGE.name())); //$NON-NLS-1$
	}
	
	@Test
	public void hasAnyBranchPermissionMustCheckProject() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project", Type.PROJECT), Permission.EDIT_PAGE)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasPermission(
				authentication, "project/*", Type.BRANCH.name(), Permission.EDIT_PAGE.name())); //$NON-NLS-1$
	}
	
	@Test
	public void hasAnyBranchPermissionMustHonorAdminPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project/branch", Type.BRANCH), Permission.ADMIN)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasPermission(
				authentication, "project/*", Type.BRANCH.name(), Permission.EDIT_PAGE.name())); //$NON-NLS-1$
	}
	
	@Test
	public void hasPagePermissionMustCheckBranch() throws IOException {
		when(pageStore.getPage("project", "branch", DocumentrConstants.HOME_PAGE_NAME + "/foo", false)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			.thenReturn(Page.fromText("title", "text")); //$NON-NLS-1$ //$NON-NLS-2$
		
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project/branch", Type.BRANCH), Permission.EDIT_PAGE)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasPermission(
				authentication, "project/branch/" + DocumentrConstants.HOME_PAGE_NAME + ",foo", //$NON-NLS-1$ //$NON-NLS-2$
				Type.PAGE.name(), Permission.EDIT_PAGE.name()));
	}

	@Test
	public void hasPagePermissionWithPageRestrictingAccess() throws IOException {
		Page page = Page.fromText("title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		page.setViewRestrictionRole("viewRole"); //$NON-NLS-1$
		when(pageStore.getPage("project", "branch", "home/page", false)).thenReturn(page); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		when(userStore.getUserAuthorities(USER)).thenReturn(Lists.newArrayList(
				new RoleGrantedAuthority(GrantedAuthorityTarget.APPLICATION, "viewRole"))); //$NON-NLS-1$
		
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, Permission.VIEW));
		assertTrue(permissionEvaluator.hasPermission(
				authentication, "project/branch/home,page", Type.PAGE.name(), Permission.VIEW.name())); //$NON-NLS-1$
	}

	@Test
	public void hasPagePermissionWithPageRestrictingAccessMustHonorAdminPermission() throws IOException {
		Page page = Page.fromText("title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		page.setViewRestrictionRole("viewRole"); //$NON-NLS-1$
		when(pageStore.getPage("project", "branch", "home/page", false)).thenReturn(page); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, Permission.ADMIN));
		assertTrue(permissionEvaluator.hasPermission(
				authentication, "project/branch/home,page", Type.PAGE.name(), Permission.VIEW.name())); //$NON-NLS-1$
	}
	
	private Authentication mockAuthentication(GrantedAuthority... authorities) {
		return new AbstractAuthenticationToken(Sets.newHashSet(authorities)) {
			@Override
			public Object getPrincipal() {
				return USER;
			}
			
			@Override
			public Object getCredentials() {
				return null;
			}
		};
	}
}
