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

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import de.blizzy.documentr.access.GrantedAuthorityTarget.Type;

@Component
public class DocumentrPermissionEvaluator implements PermissionEvaluator {
	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		return false;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
			Object permission) {

		return hasPermission(authentication, targetId.toString(), Type.valueOf(targetType),
				Permission.valueOf(permission.toString()));
	}
	
	private boolean hasPermission(Authentication authentication, String targetId, Type targetType, Permission permission) {
		switch (targetType) {
			case PROJECT:
				return hasProjectPermission(authentication, targetId, permission);
			case APPLICATION:
				return hasApplicationPermission(authentication, permission);
			default:
				return false;
		}
	}

	private boolean hasProjectPermission(Authentication authentication, String projectName, Permission permission) {
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			if (authority instanceof PermissionGrantedAuthority) {
				PermissionGrantedAuthority pga = (PermissionGrantedAuthority) authority;
				GrantedAuthorityTarget target = pga.getTarget();
				if ((target.getType() == Type.PROJECT) && target.getTargetId().equals(projectName) &&
						(pga.getPermission() == permission)) {
					
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean hasApplicationPermission(Authentication authentication, Permission permission) {
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			if (authority instanceof PermissionGrantedAuthority) {
				PermissionGrantedAuthority pga = (PermissionGrantedAuthority) authority;
				if ((pga.getTarget().getType() == Type.APPLICATION) && (pga.getPermission() == permission)) {
					return true;
				}
			}
		}
		return false;
	}
}
