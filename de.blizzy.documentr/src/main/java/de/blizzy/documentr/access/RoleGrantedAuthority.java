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

/** An authority that grants a role on a target object. */
public class RoleGrantedAuthority implements GrantedAuthority {
	private static final long serialVersionUID = 4837778771482591248L;

	@Getter
	private GrantedAuthorityTarget target;
	@Getter
	private String roleName;

	/**
	 * Constructs a new role granted authority.
	 *
	 * @param target the target object
	 * @param roleName the name of the role being granted
	 */
	public RoleGrantedAuthority(GrantedAuthorityTarget target, String roleName) {
		Assert.notNull(target);
		Assert.hasLength(roleName);

		this.target = target;
		this.roleName = roleName;
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
			RoleGrantedAuthority other = (RoleGrantedAuthority) o;
			return new EqualsBuilder()
				.append(other.target, target)
				.append(other.roleName, roleName)
				.isEquals();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(target)
			.append(roleName)
			.toHashCode();
	}
}
