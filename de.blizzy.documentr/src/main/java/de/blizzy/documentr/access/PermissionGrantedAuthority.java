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

import lombok.Getter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import de.blizzy.documentr.access.GrantedAuthorityTarget.Type;

/** An authority that grants a permission on a target object. */
class PermissionGrantedAuthority implements GrantedAuthority {
	private static final long serialVersionUID = 4837778771482591248L;

	@Getter
	private GrantedAuthorityTarget target;
	@Getter
	private Permission permission;

	/**
	 * Constructs a new permission granted authority.
	 *
	 * @param target the target object
	 * @param permission the permission being granted
	 */
	PermissionGrantedAuthority(GrantedAuthorityTarget target, Permission permission) {
		Assert.notNull(target);
		Assert.isTrue(target.getType() != Type.PAGE);
		Assert.notNull(permission);

		this.target = target;
		this.permission = permission;
	}

	@Override
	public String getAuthority() {
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if ((o != null) && o.getClass().equals(getClass())) {
			PermissionGrantedAuthority other = (PermissionGrantedAuthority) o;
			return new EqualsBuilder()
				.append(other.target, target)
				.append(other.permission, permission)
				.isEquals();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(target)
			.append(permission)
			.toHashCode();
	}
}
