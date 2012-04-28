package de.blizzy.documentr.web.project;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import de.blizzy.documentr.DocumentrConstants;

public class ProjectNameBlacklistValidator implements ConstraintValidator<ProjectNameNotBlacklisted, String> {
	@Override
	public void initialize(ProjectNameNotBlacklisted annotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (StringUtils.isBlank(value)) {
			return true;
		}
		
		return !ArrayUtils.contains(DocumentrConstants.PROJECT_NAMES_BLACKLIST, value);
	}
}
