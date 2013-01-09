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

import java.io.Serializable;

import lombok.Getter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.util.Assert;

/** Represents a target object that permissions may be granted on. A target consists of a target ID and a type. */
public class GrantedAuthorityTarget implements Serializable {
	/** Types of target objects. */
	public static enum Type {
		/**
		 * The &quot;application object&quot;. Granting permissions on the application implies granting
		 * those permissions to everything.
		 */
		@SuppressWarnings("hiding")
		APPLICATION,

		/**
		 * A project. Granting permissions on a project implies granting those permissions to all
		 * branches and pages of that project.
		 */
		PROJECT,

		/**
		 * A branch. Granting permissions on a branch implies granting those permissions to all
		 * pages of that branch.
		 */
		BRANCH,

		/** A page. Permissions cannot be granted on pages directly. */
		PAGE;
	}

	/** The &quot;application object's&quot; target ID. */
	public static final String APPLICATION_TARGET_ID = "application"; //$NON-NLS-1$

	/** The &quot;application object&quot; target. */
	public static final GrantedAuthorityTarget APPLICATION =
			new GrantedAuthorityTarget(APPLICATION_TARGET_ID, Type.APPLICATION);

	/**
	 * Target ID component representing &quot;any&quot;. This cannot be used directly for target IDs.
	 *
	 * @see DocumentrPermissionEvaluator#hasAnyProjectPermission
	 * @see DocumentrPermissionEvaluator#hasAnyBranchPermission
	 */
	static final String ANY = "*"; //$NON-NLS-1$

	private static final long serialVersionUID = -8582662031330649292L;

	@Getter
	private String targetId;
	@Getter
	private Type type;

	/**
	 * Constructs a new target object.
	 *
	 * @param targetId the ID of the target object
	 * @param type the type of the target object
	 */
	public GrantedAuthorityTarget(String targetId, Type type) {
		Assert.hasLength(targetId);
		Assert.isTrue(!targetId.equals(ANY));
		Assert.isTrue(!targetId.endsWith("/" + ANY)); //$NON-NLS-1$
		Assert.notNull(type);

		this.targetId = targetId;
		this.type = type;
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
