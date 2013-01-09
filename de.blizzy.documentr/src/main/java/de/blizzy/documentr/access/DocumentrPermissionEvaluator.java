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

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.Sets;

import de.blizzy.documentr.access.GrantedAuthorityTarget.Type;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.PageNotFoundException;
import de.blizzy.documentr.repository.GlobalRepositoryManager;

/**
 * <p>documentr's {@link PermissionEvaluator}.</p>
 *
 * <p>Permissions are handled recursively. For example, if asked if a user has a permission on a specific page,
 * and they don't, lookup continues on the page's branch. If the user does not have the permission on the branch,
 * lookup continues on the branch's project. If they don't have the permission on the project, lookup continues
 * on the &quot;application object.&quot;</p>
 *
 * <p>Granting the {@link Permission#ADMIN ADMIN} permission on an object implies granting all other permissions
 * on the same object.</p>
 *
 * <p>It is not possible to grant permissions on a higher-level object, then deny those permissions on any
 * of their children. For example, having granted the {@link Permission#VIEW VIEW} permission on a project
 * allows view access to all branches and pages of that project. In that case it is not possible to deny
 * viewing a particular branch of the project.</p>
 */
@Component
public class DocumentrPermissionEvaluator implements PermissionEvaluator {
	@Autowired
	private IPageStore pageStore;
	@Autowired
	private UserStore userStore;
	@Autowired
	private GlobalRepositoryManager repoManager;
	@Autowired
	private LoginNameUserDetailsService userDetailsService;

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		Assert.notNull(authentication);
		Assert.notNull(targetDomainObject);
		Assert.notNull(permission);

		// not used
		return false;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
			Object permission) {

		Assert.notNull(authentication);
		Assert.notNull(targetId);
		Assert.hasLength(targetType);
		Assert.notNull(permission);

		// not used
		return false;
	}

	public boolean hasApplicationPermission(Authentication authentication, Permission permission) {
		return hasApplicationPermission(authentication.getAuthorities(), permission);
	}

	private boolean hasApplicationPermission(Collection<? extends GrantedAuthority> authorities,
			Permission permission) {

		for (GrantedAuthority authority : authorities) {
			if (authority instanceof PermissionGrantedAuthority) {
				PermissionGrantedAuthority pga = (PermissionGrantedAuthority) authority;
				GrantedAuthorityTarget target = pga.getTarget();
				Type type = target.getType();
				if ((type == Type.APPLICATION) &&
					hasPermission(pga, permission)) {

					return true;
				}
			}
		}
		return false;
	}

	public boolean hasProjectPermission(Authentication authentication, String projectName, Permission permission) {
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			if (authority instanceof PermissionGrantedAuthority) {
				PermissionGrantedAuthority pga = (PermissionGrantedAuthority) authority;
				GrantedAuthorityTarget target = pga.getTarget();
				Type type = target.getType();
				String id = target.getTargetId();
				if ((type == Type.PROJECT) &&
					id.equals(projectName) &&
					hasPermission(pga, permission)) {

					return true;
				}
			}
		}
		return hasApplicationPermission(authentication, permission);
	}

	public boolean hasAnyProjectPermission(Authentication authentication, Permission permission) {
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			if (authority instanceof PermissionGrantedAuthority) {
				PermissionGrantedAuthority pga = (PermissionGrantedAuthority) authority;
				GrantedAuthorityTarget target = pga.getTarget();
				Type type = target.getType();
				if ((type == Type.PROJECT) &&
					hasPermission(pga, permission)) {

					return true;
				}
			}
		}
		return hasApplicationPermission(authentication, permission);
	}

	public boolean hasBranchPermission(Authentication authentication, String projectName, String branchName,
			Permission permission) {

		String targetId = projectName + "/" + branchName; //$NON-NLS-1$
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			if (authority instanceof PermissionGrantedAuthority) {
				PermissionGrantedAuthority pga = (PermissionGrantedAuthority) authority;
				GrantedAuthorityTarget target = pga.getTarget();
				Type type = target.getType();
				String id = target.getTargetId();
				if ((type == Type.BRANCH) &&
					id.equals(targetId) &&
					hasPermission(pga, permission)) {

					return true;
				}
			}
		}
		return hasProjectPermission(authentication, projectName, permission);
	}

	public boolean hasAnyBranchPermission(Authentication authentication, String projectName, Permission permission) {
		String targetIdPrefix = projectName + "/"; //$NON-NLS-1$
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			if (authority instanceof PermissionGrantedAuthority) {
				PermissionGrantedAuthority pga = (PermissionGrantedAuthority) authority;
				GrantedAuthorityTarget target = pga.getTarget();
				Type type = target.getType();
				String id = target.getTargetId();
				if ((type == Type.BRANCH) &&
					id.startsWith(targetIdPrefix) &&
					hasPermission(pga, permission)) {

					return true;
				}
			}
		}
		return hasProjectPermission(authentication, projectName, permission);
	}

	public boolean hasPagePermission(Authentication authentication, String projectName, String branchName,
			String path, Permission permission) {

		if (hasBranchPermission(authentication, projectName, branchName, Permission.ADMIN)) {
			return true;
		} else if (hasBranchPermission(authentication, projectName, branchName, permission)) {
			try {
				String viewRestrictionRole = pageStore.getViewRestrictionRole(projectName, branchName, path);
				return StringUtils.isBlank(viewRestrictionRole) ||
						hasRoleOnBranch(authentication, projectName, branchName, viewRestrictionRole);
			} catch (IOException e) {
				throw new AuthenticationServiceException(e.getMessage(), e);
			} catch (PageNotFoundException e) {
				throw new AuthenticationServiceException(e.getMessage(), e);
			}
		}
		return false;
	}

	public boolean hasPagePermissionInOtherBranches(Authentication authentication, String projectName,
			String branchName, String path, Permission permission) {

		try {
			List<String> branches = repoManager.listProjectBranches(projectName);
			for (String branch : branches) {
				if (!branch.equals(branchName)) {
					boolean result = hasPagePermission(authentication, projectName, branch, path, permission);
					if (result) {
						return true;
					}
				}
			}
		} catch (IOException e) {
			throw new AuthenticationServiceException(e.getMessage(), e);
		}
		return false;
	}

	public Set<String> getBranchesForPermission(Authentication authentication, Permission permission) {
		try {
			Set<String> branches = Sets.newHashSet();
			for (String project : repoManager.listProjects()) {
				for (String branch : repoManager.listProjectBranches(project)) {
					if (hasBranchPermission(authentication, project, branch, permission)) {
						branches.add(project + "/" + branch); //$NON-NLS-1$
					}
				}
			}
			return branches;
		} catch (IOException e) {
			throw new AuthenticationServiceException(e.getMessage(), e);
		}
	}

	public boolean isAdmin(String loginName) {
		try {
			UserDetails user = userDetailsService.loadUserByUsername(loginName);
			return hasApplicationPermission(user.getAuthorities(), Permission.ADMIN);
		} catch (UsernameNotFoundException e) {
			// okay
		}
		return false;
	}

	public boolean isLastAdminRole(String roleName) {
		try {
			if (userStore.getRole(roleName).getPermissions().contains(Permission.ADMIN)) {
				Set<String> roles = Sets.newHashSet(userStore.listRoles());
				roles.remove(roleName);

				// find all roles containing the ADMIN permission
				Set<String> adminRoles = Sets.newHashSet();
				for (String role : roles) {
					Role r = userStore.getRole(role);
					if (r.getPermissions().contains(Permission.ADMIN)) {
						adminRoles.add(role);
					}
				}

				// check whether any of the admin roles is granted to any user on the "application" object
				if (!adminRoles.isEmpty()) {
					List<String> users = userStore.listUsers();
					for (String user : users) {
						List<RoleGrantedAuthority> authorities = userStore.getUserAuthorities(user);
						for (RoleGrantedAuthority rga : authorities) {
							for (String role : adminRoles) {
								if (rga.getRoleName().equals(role) &&
									rga.getTarget().equals(GrantedAuthorityTarget.APPLICATION)) {

									return false;
								}
							}
						}
					}
				}

				return true;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return false;
	}

	private boolean hasRoleOnBranch(Authentication authentication, String projectName, String branchName,
			String roleName) throws IOException {

		if (authentication.isAuthenticated()) {
			List<RoleGrantedAuthority> authorities = userStore.getUserAuthorities(authentication.getName());
			for (RoleGrantedAuthority rga : authorities) {
				if (rga.getRoleName().equals(roleName)) {
					GrantedAuthorityTarget target = rga.getTarget();
					switch (target.getType()) {
						case APPLICATION:
							return true;
						case PROJECT:
							if (target.getTargetId().equals(projectName)) {
								return true;
							}
							break;
						case BRANCH:
							if (target.getTargetId().equals(projectName + "/" + branchName)) { //$NON-NLS-1$
								return true;
							}
							break;
					}
				}
			}
		}
		return false;
	}

	private boolean hasPermission(PermissionGrantedAuthority authority, Permission permission) {
		Permission p = authority.getPermission();
		return (p == Permission.ADMIN) || (p == permission);
	}
}
