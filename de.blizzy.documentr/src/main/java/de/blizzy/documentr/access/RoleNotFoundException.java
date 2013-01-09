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
import de.blizzy.documentr.util.NotFoundException;

/** Thrown when no role is found that has a specific name. */
public class RoleNotFoundException extends NotFoundException {
	@Getter
	private String roleName;

	RoleNotFoundException(String roleName) {
		super("role not found: " + roleName); //$NON-NLS-1$

		this.roleName = roleName;
	}
}
