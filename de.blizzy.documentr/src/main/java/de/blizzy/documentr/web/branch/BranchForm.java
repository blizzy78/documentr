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
package de.blizzy.documentr.web.branch;

import javax.validation.constraints.NotNull;

import lombok.Getter;

import org.hibernate.validator.constraints.NotBlank;

import de.blizzy.documentr.validation.annotation.BranchNameNotBlacklisted;
import de.blizzy.documentr.validation.annotation.ProjectExists;
import de.blizzy.documentr.validation.annotation.ProjectNameNotBlacklisted;
import de.blizzy.documentr.validation.annotation.ValidBranchName;
import de.blizzy.documentr.validation.annotation.ValidProjectName;

public class BranchForm {
	@NotNull(message="{project.name.blank}")
	@NotBlank(message="{project.name.blank}")
	@ValidProjectName
	@ProjectNameNotBlacklisted
	@ProjectExists
	@Getter
	private String projectName;
	@NotNull(message="{branch.name.blank}")
	@NotBlank(message="{branch.name.blank}")
	@ValidBranchName
	@BranchNameNotBlacklisted
	@Getter
	private String name;
	@ValidBranchName
	@BranchNameNotBlacklisted
	@Getter
	private String startingBranch;

	BranchForm(String projectName, String name, String startingBranch) {
		this.projectName = projectName;
		this.name = name;
		this.startingBranch = startingBranch;
	}
}
