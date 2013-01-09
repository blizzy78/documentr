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

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.GrantedAuthorityTarget.Type;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LoginNameUserDetailsService.class)
public class DocumentrPermissionEvaluatorTest extends AbstractDocumentrTest {
	private static final String USER = "user"; //$NON-NLS-1$

	@Mock
	private IPageStore pageStore;
	@Mock
	private UserStore userStore;
	@Mock
	private UserDetails userDetails;
	@InjectMocks
	private DocumentrPermissionEvaluator permissionEvaluator;
	private LoginNameUserDetailsService userDetailsService;

	@Before
	public void setUp() {
		userDetailsService = PowerMockito.mock(LoginNameUserDetailsService.class);
		Whitebox.setInternalState(permissionEvaluator, LoginNameUserDetailsService.class, userDetailsService);
	}

	@Test
	public void hasApplicationPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, Permission.EDIT_PAGE));
		assertTrue(permissionEvaluator.hasApplicationPermission(
				authentication, Permission.EDIT_PAGE));
	}

	@Test
	public void hasApplicationPermissionMustHonorAdminPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, Permission.ADMIN));
		assertTrue(permissionEvaluator.hasApplicationPermission(
				authentication, Permission.EDIT_PAGE));
	}

	@Test
	public void hasProjectPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project", Type.PROJECT), Permission.EDIT_PAGE)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasProjectPermission(
				authentication, "project", Permission.EDIT_PAGE)); //$NON-NLS-1$
	}

	@Test
	public void hasProjectPermissionMustCheckApplication() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, Permission.EDIT_PAGE));
		assertTrue(permissionEvaluator.hasProjectPermission(
				authentication, "project", Permission.EDIT_PAGE)); //$NON-NLS-1$
	}

	@Test
	public void hasProjectPermissionMustHonorAdminPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project", Type.PROJECT), Permission.ADMIN)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasProjectPermission(
				authentication, "project", Permission.EDIT_PAGE)); //$NON-NLS-1$
	}

	@Test
	public void hasAnyProjectPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project", Type.PROJECT), Permission.EDIT_PAGE)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasAnyProjectPermission(
				authentication, Permission.EDIT_PAGE));
	}

	@Test
	public void hasAnyProjectPermissionMustCheckApplication() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, Permission.EDIT_PAGE));
		assertTrue(permissionEvaluator.hasAnyProjectPermission(
				authentication, Permission.EDIT_PAGE));
	}

	@Test
	public void hasAnyProjectPermissionMustHonorAdminPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project", Type.PROJECT), Permission.ADMIN)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasAnyProjectPermission(
				authentication, Permission.EDIT_PAGE));
	}

	@Test
	public void hasBranchPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project/branch", Type.BRANCH), Permission.EDIT_PAGE)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasBranchPermission(
				authentication, "project", "branch", Permission.EDIT_PAGE)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void hasBranchPermissionMustCheckProject() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project", Type.PROJECT), Permission.EDIT_PAGE)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasBranchPermission(
				authentication, "project", "branch", Permission.EDIT_PAGE)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void hasBranchPermissionMustHonorAdminPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project/branch", Type.BRANCH), Permission.ADMIN)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasBranchPermission(
				authentication, "project", "branch", Permission.EDIT_PAGE)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void hasAnyBranchPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project/branch", Type.BRANCH), Permission.EDIT_PAGE)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasAnyBranchPermission(
				authentication, "project", Permission.EDIT_PAGE)); //$NON-NLS-1$
	}

	@Test
	public void hasAnyBranchPermissionMustCheckProject() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project", Type.PROJECT), Permission.EDIT_PAGE)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasAnyBranchPermission(
				authentication, "project", Permission.EDIT_PAGE)); //$NON-NLS-1$
	}

	@Test
	public void hasAnyBranchPermissionMustHonorAdminPermission() {
		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project/branch", Type.BRANCH), Permission.ADMIN)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasAnyBranchPermission(
				authentication, "project", Permission.EDIT_PAGE)); //$NON-NLS-1$
	}

	@Test
	public void hasPagePermissionMustCheckBranch() throws IOException {
		when(pageStore.getPage("project", "branch", DocumentrConstants.HOME_PAGE_NAME + "/foo", false)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			.thenReturn(Page.fromText("title", "text")); //$NON-NLS-1$ //$NON-NLS-2$

		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				new GrantedAuthorityTarget("project/branch", Type.BRANCH), Permission.EDIT_PAGE)); //$NON-NLS-1$
		assertTrue(permissionEvaluator.hasPagePermission(authentication,
				"project", "branch", DocumentrConstants.HOME_PAGE_NAME + "/foo", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				Permission.EDIT_PAGE));
	}

	@Test
	public void hasPagePermissionWithPageRestrictingAccess() throws IOException {
		Page page = Page.fromText("title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		page.setViewRestrictionRole("viewRole"); //$NON-NLS-1$
		when(pageStore.getPage("project", "branch", DocumentrConstants.HOME_PAGE_NAME + "/foo", false)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			.thenReturn(page);

		when(userStore.getUserAuthorities(USER)).thenReturn(Lists.newArrayList(
				new RoleGrantedAuthority(GrantedAuthorityTarget.APPLICATION, "viewRole"))); //$NON-NLS-1$

		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, Permission.VIEW));
		assertTrue(permissionEvaluator.hasPagePermission(authentication,
				"project", "branch", DocumentrConstants.HOME_PAGE_NAME + "/foo", Permission.VIEW)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Test
	public void hasPagePermissionWithPageRestrictingAccessMustHonorAdminPermission() throws IOException {
		Page page = Page.fromText("title", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		page.setViewRestrictionRole("viewRole"); //$NON-NLS-1$
		when(pageStore.getPage("project", "branch", DocumentrConstants.HOME_PAGE_NAME + "/foo", false)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			.thenReturn(page);

		Authentication authentication = mockAuthentication(new PermissionGrantedAuthority(
				GrantedAuthorityTarget.APPLICATION, Permission.ADMIN));
		assertTrue(permissionEvaluator.hasPagePermission(authentication,
				"project", "branch", DocumentrConstants.HOME_PAGE_NAME + "/foo", Permission.VIEW)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Test
	public void isAdmin() {
		when(userDetailsService.loadUserByUsername(USER)).thenReturn(userDetails);

		GrantedAuthority authority =
				new PermissionGrantedAuthority(GrantedAuthorityTarget.APPLICATION, Permission.ADMIN);
		doReturn(Sets.newHashSet(authority)).when(userDetails).getAuthorities();
		assertTrue(permissionEvaluator.isAdmin(USER));

		GrantedAuthorityTarget target = new GrantedAuthorityTarget("project", GrantedAuthorityTarget.Type.PROJECT); //$NON-NLS-1$
		authority = new PermissionGrantedAuthority(target, Permission.ADMIN);
		doReturn(Sets.newHashSet(authority)).when(userDetails).getAuthorities();
		assertFalse(permissionEvaluator.isAdmin(USER));

		target = new GrantedAuthorityTarget("project", GrantedAuthorityTarget.Type.PROJECT); //$NON-NLS-1$
		authority = new PermissionGrantedAuthority(target, Permission.VIEW);
		doReturn(Sets.newHashSet(authority)).when(userDetails).getAuthorities();
		assertFalse(permissionEvaluator.isAdmin(USER));
	}

	@Test
	public void isLastAdminRole() throws IOException {
		when(userStore.listUsers()).thenReturn(Lists.newArrayList(USER));
		List<RoleGrantedAuthority> authorities = Lists.newArrayList();
		when(userStore.getUserAuthorities(USER)).thenReturn(authorities);
		List<String> roleNames = Lists.newArrayList();
		when(userStore.listRoles()).thenReturn(roleNames);

		roleNames.add("viewer"); //$NON-NLS-1$
		RoleGrantedAuthority rga = new RoleGrantedAuthority(GrantedAuthorityTarget.APPLICATION, "viewer"); //$NON-NLS-1$
		authorities.add(rga);
		Role role = mock(Role.class);
		when(role.getPermissions()).thenReturn(EnumSet.of(Permission.VIEW));
		when(userStore.getRole("viewer")).thenReturn(role); //$NON-NLS-1$
		assertFalse(permissionEvaluator.isLastAdminRole("viewer")); //$NON-NLS-1$

		roleNames.add("admin"); //$NON-NLS-1$
		rga = new RoleGrantedAuthority(GrantedAuthorityTarget.APPLICATION, "admin"); //$NON-NLS-1$
		authorities.add(rga);
		role = mock(Role.class);
		when(role.getPermissions()).thenReturn(EnumSet.of(Permission.ADMIN));
		when(userStore.getRole("admin")).thenReturn(role); //$NON-NLS-1$
		assertTrue(permissionEvaluator.isLastAdminRole("admin")); //$NON-NLS-1$

		roleNames.add("projectAdmin"); //$NON-NLS-1$
		GrantedAuthorityTarget target = new GrantedAuthorityTarget("project", GrantedAuthorityTarget.Type.PROJECT); //$NON-NLS-1$
		rga = new RoleGrantedAuthority(target, "projectAdmin"); //$NON-NLS-1$
		authorities.add(rga);
		role = mock(Role.class);
		when(role.getPermissions()).thenReturn(EnumSet.of(Permission.ADMIN));
		when(userStore.getRole("projectAdmin")).thenReturn(role); //$NON-NLS-1$
		assertFalse(permissionEvaluator.isLastAdminRole("projectAdmin")); //$NON-NLS-1$

		roleNames.add("admin2"); //$NON-NLS-1$
		rga = new RoleGrantedAuthority(GrantedAuthorityTarget.APPLICATION, "admin2"); //$NON-NLS-1$
		authorities.add(rga);
		role = mock(Role.class);
		when(role.getPermissions()).thenReturn(EnumSet.of(Permission.ADMIN));
		when(userStore.getRole("admin2")).thenReturn(role); //$NON-NLS-1$
		assertFalse(permissionEvaluator.isLastAdminRole("admin2")); //$NON-NLS-1$
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
