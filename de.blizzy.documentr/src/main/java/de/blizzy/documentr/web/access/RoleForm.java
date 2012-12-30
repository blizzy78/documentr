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
package de.blizzy.documentr.web.access;

import java.util.Set;

import javax.validation.constraints.NotNull;

import lombok.Getter;

import org.hibernate.validator.constraints.NotBlank;

import de.blizzy.documentr.validation.annotation.ValidRoleName;

public class RoleForm {
	@NotNull(message="{role.name.blank}")
	@NotBlank(message="{role.name.blank}")
	@ValidRoleName
	@Getter
	private String name;
	@ValidRoleName
	@Getter
	private String originalName;
	@Getter
	private Set<String> permissions;

	public RoleForm(String name, String originalName, Set<String> permissions) {
		this.name = name;
		this.originalName = originalName;
		this.permissions = permissions;
	}
}
