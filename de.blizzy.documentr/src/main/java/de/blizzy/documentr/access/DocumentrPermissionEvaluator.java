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

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import de.blizzy.documentr.Util;
import de.blizzy.documentr.access.GrantedAuthorityTarget.Type;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.page.PageNotFoundException;

@Component
public class DocumentrPermissionEvaluator implements PermissionEvaluator {
	@Autowired
	private IPageStore pageStore;
	@Autowired
	private UserStore userStore;

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
		
		return hasPermission(authentication, targetId.toString(), Type.valueOf(targetType),
				Permission.valueOf(permission.toString()));
	}
	
	private boolean hasPermission(Authentication authentication, String targetId, Type targetType, Permission permission) {
		Assert.hasLength(targetId);
		
		switch (targetType) {
			case APPLICATION:
				return hasApplicationPermission(authentication, permission);
			case PROJECT:
				if (targetId.equals(GrantedAuthorityTarget.ANY)) {
					return hasAnyProjectPermission(authentication, permission);
				}
				return hasProjectPermission(authentication, targetId, permission);
			case BRANCH:
				{
					String projectName = StringUtils.substringBefore(targetId, "/"); //$NON-NLS-1$
					String branchName = StringUtils.substringAfter(targetId, "/"); //$NON-NLS-1$
					if (branchName.equals(GrantedAuthorityTarget.ANY)) {
						return hasAnyBranchPermission(authentication, projectName, permission);
					}
					return hasBranchPermission(authentication, projectName, branchName, permission);
				}
			case PAGE:
				{
					String[] parts = targetId.split("/"); //$NON-NLS-1$
					Assert.isTrue(parts.length == 3);
					String projectName = parts[0];
					String branchName = parts[1];
					String path = Util.toRealPagePath(parts[2]);
					return hasPagePermission(authentication, projectName, branchName, path, permission);
				}
			default:
				return false;
		}
	}

	public boolean hasApplicationPermission(Authentication authentication, Permission permission) {
		for (GrantedAuthority authority : authentication.getAuthorities()) {
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
				Page page = pageStore.getPage(projectName, branchName, path, false);
				String viewRestrictionRole = page.getViewRestrictionRole();
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
	
	private boolean hasRoleOnBranch(Authentication authentication, String projectName, String branchName,
			String roleName) throws IOException {
		
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
		return false;
	}
	
	private boolean hasPermission(PermissionGrantedAuthority authority, Permission permission) {
		Permission p = authority.getPermission();
		return (p == Permission.ADMIN) || (p == permission);
	}

	void setPageStore(IPageStore pageStore) {
		this.pageStore = pageStore;
	}

	void setUserStore(UserStore userStore) {
		this.userStore = userStore;
	}
}
