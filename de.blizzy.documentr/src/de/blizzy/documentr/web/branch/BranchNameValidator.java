package de.blizzy.documentr.web.branch;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import de.blizzy.documentr.DocumentrConstants;

public class BranchNameValidator implements ConstraintValidator<ValidBranchName, String> {
	@Override
	public void initialize(ValidBranchName annotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (StringUtils.isBlank(value)) {
			return true;
		}
		
		return Pattern.matches("^" + DocumentrConstants.BRANCH_NAME_PATTERN + "$", value); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
