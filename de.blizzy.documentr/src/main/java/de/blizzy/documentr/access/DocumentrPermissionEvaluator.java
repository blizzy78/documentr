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

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import de.blizzy.documentr.Util;
import de.blizzy.documentr.access.GrantedAuthorityTarget.Type;

@Component
public class DocumentrPermissionEvaluator implements PermissionEvaluator {
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
					String projectName = StringUtils.substringBefore(targetId, "/"); //$NON-NLS-1$
					String branchName = StringUtils.substringAfter(targetId, "/"); //$NON-NLS-1$
					String path = Util.toRealPagePath(StringUtils.substringAfter(branchName, "/")); //$NON-NLS-1$
					branchName = StringUtils.substringBefore(branchName, "/"); //$NON-NLS-1$
					return hasPagePermission(authentication, projectName, branchName, path, permission);
				}
			default:
				return false;
		}
	}

	private boolean hasApplicationPermission(Authentication authentication, Permission permission) {
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

	private boolean hasProjectPermission(Authentication authentication, String projectName, Permission permission) {
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
	
	private boolean hasAnyProjectPermission(Authentication authentication, Permission permission) {
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
	
	private boolean hasBranchPermission(Authentication authentication, String projectName, String branchName,
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
	
	private boolean hasAnyBranchPermission(Authentication authentication, String projectName, Permission permission) {
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
	
	private boolean hasPagePermission(Authentication authentication, String projectName, String branchName,
			@SuppressWarnings("unused") String path, Permission permission) {

		// TODO: check page to see whom it is restricted to
		
		return hasBranchPermission(authentication, projectName, branchName, permission);
	}

	private boolean hasPermission(PermissionGrantedAuthority authority, Permission permission) {
		Permission p = authority.getPermission();
		return (p == Permission.ADMIN) || (p == permission);
	}
}
