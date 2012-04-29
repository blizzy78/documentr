package de.blizzy.documentr.web.branch;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import de.blizzy.documentr.DocumentrConstants;

public class BranchNameBlacklistValidator implements ConstraintValidator<BranchNameNotBlacklisted, String> {
	@Override
	public void initialize(BranchNameNotBlacklisted annotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (StringUtils.isBlank(value)) {
			return true;
		}
		
		return !Pattern.matches("^" + DocumentrConstants.BRANCH_NAMES_BLACKLIST_PATTERN + "$", value); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
