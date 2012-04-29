package de.blizzy.documentr.web.page;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import de.blizzy.documentr.web.branch.BranchNameNotBlacklisted;
import de.blizzy.documentr.web.branch.ValidBranchName;
import de.blizzy.documentr.web.project.ProjectExists;
import de.blizzy.documentr.web.project.ProjectNameNotBlacklisted;
import de.blizzy.documentr.web.project.ValidProjectName;

public class PageForm {
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
	private String branchName;
	@NotNull(message="{page.path.blank}")
	@NotEmpty(message="{page.path.blank}")
	@NotBlank(message="{page.path.blank}")
	@ValidPagePath
	@PagePathNotBlacklisted
	private String path;
	@NotNull(message="{page.title.blank}")
	@NotEmpty(message="{page.title.blank}")
	@NotBlank(message="{page.title.blank}")
	private String title;
	private String text;

	PageForm(String projectName, String branchName, String path, String title, String text) {
		this.projectName = projectName;
		this.branchName = branchName;
		this.path = path;
		this.title = title;
		this.text = text;
	}
	
	public String getProjectName() {
		return projectName;
	}
	
	public String getBranchName() {
		return branchName;
	}
	
	public String getPath() {
		return path;
	}

	public String getTitle() {
		return title;
	}
	
	public String getText() {
		return text;
	}
}
