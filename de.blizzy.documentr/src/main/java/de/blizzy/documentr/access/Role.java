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

import java.util.EnumSet;

/** A user role containing a set of {@link Permission}s. */
public class Role {
	private String name;
	private EnumSet<Permission> permissions;

	/**
	 * Constructs a new role.
	 * 
	 * @param name the role's name
	 * @param permissions the permissions this role contains
	 */
	public Role(String name, EnumSet<Permission> permissions) {
		this.name = name;
		this.permissions = permissions;
	}

	/** Returns the role name. */
	public String getName() {
		return name;
	}

	/** Returns the permissions this role contains. */
	public EnumSet<Permission> getPermissions() {
		return permissions;
	}
}
