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
package de.blizzy.documentr.web.branch;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import de.blizzy.documentr.web.project.ProjectExists;
import de.blizzy.documentr.web.project.ProjectNameNotBlacklisted;
import de.blizzy.documentr.web.project.ValidProjectName;

public class BranchForm {
	@NotNull(message="{project.name.blank}")
	@NotBlank(message="{project.name.blank}")
	@ValidProjectName
	@ProjectNameNotBlacklisted
	@ProjectExists
	private String projectName;
	@NotNull(message="{branch.name.blank}")
	@NotBlank(message="{branch.name.blank}")
	@ValidBranchName
	@BranchNameNotBlacklisted
	private String name;
	@ValidBranchName
	@BranchNameNotBlacklisted
	private String startingBranch;

	BranchForm(String projectName, String name, String startingBranch) {
		this.projectName = projectName;
		this.name = name;
		this.startingBranch = startingBranch;
	}
	
	public String getProjectName() {
		return projectName;
	}
	
	public String getName() {
		return name;
	}
	
	public String getStartingBranch() {
		return startingBranch;
	}
}
