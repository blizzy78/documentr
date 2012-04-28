package de.blizzy.documentr.web.project;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import de.blizzy.documentr.repository.GlobalRepositoryManager;

public class ProjectDoesNotExistValidator implements ConstraintValidator<ProjectDoesNotExist, String> {
	@Autowired
	private GlobalRepositoryManager repoManager;
	
	@Override
	public void initialize(ProjectDoesNotExist annotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (StringUtils.isBlank(value)) {
			return true;
		}

		return !repoManager.listProjects().contains(value);
	}
}
