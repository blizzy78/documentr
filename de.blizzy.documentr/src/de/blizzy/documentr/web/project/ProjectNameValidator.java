package de.blizzy.documentr.web.project;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import de.blizzy.documentr.DocumentrConstants;

public class ProjectNameValidator implements ConstraintValidator<ValidProjectName, String> {
	@Override
	public void initialize(ValidProjectName annotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (StringUtils.isBlank(value)) {
			return true;
		}

		return Pattern.matches("^" + DocumentrConstants.PROJECT_NAME_PATTERN + "$", value); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
