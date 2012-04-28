package de.blizzy.documentr.web.branch;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import de.blizzy.documentr.web.project.ProjectExists;
import de.blizzy.documentr.web.project.ProjectNameNotBlacklisted;
import de.blizzy.documentr.web.project.ValidProjectName;

public class BranchForm {
	@NotNull(message="{project.name.blank}")
	@NotEmpty(message="{project.name.blank}")
	@NotBlank(message="{project.name.blank}")
	@ValidProjectName
	@ProjectNameNotBlacklisted
	@ProjectExists
	private String projectName;
	@NotNull(message="{branch.name.blank}")
	@NotEmpty(message="{branch.name.blank}")
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
