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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.util.Assert;

class GrantedAuthorityTarget {
	static enum Type {
		APPLICATION, PROJECT, BRANCH, PAGE;
	}
	
	static final String ANY = "*"; //$NON-NLS-1$
	static final GrantedAuthorityTarget APPLICATION =
			new GrantedAuthorityTarget("application", Type.APPLICATION); //$NON-NLS-1$
	
	private String targetId;
	private Type type;

	GrantedAuthorityTarget(String targetId, Type type) {
		Assert.hasLength(targetId);
		Assert.isTrue(!targetId.equals(ANY));
		Assert.isTrue(!targetId.endsWith("/" + ANY)); //$NON-NLS-1$
		Assert.notNull(type);

		this.targetId = targetId;
		this.type = type;
	}
	
	String getTargetId() {
		return targetId;
	}
	
	Type getType() {
		return type;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if ((o != null) && o.getClass().equals(getClass())) {
			GrantedAuthorityTarget other = (GrantedAuthorityTarget) o;
			return new EqualsBuilder()
				.append(other.targetId, targetId)
				.append(other.type, type)
				.isEquals();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(targetId)
			.append(type)
			.toHashCode();
	}
}
