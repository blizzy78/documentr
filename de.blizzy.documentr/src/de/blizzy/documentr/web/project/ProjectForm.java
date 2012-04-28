package de.blizzy.documentr.web.project;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;


public class ProjectForm {
	@NotNull(message="{project.name.blank}")
	@NotEmpty(message="{project.name.blank}")
	@NotBlank(message="{project.name.blank}")
	@ValidProjectName
	@ProjectNameNotBlacklisted
	@ProjectDoesNotExist
	private String name;

	ProjectForm(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
