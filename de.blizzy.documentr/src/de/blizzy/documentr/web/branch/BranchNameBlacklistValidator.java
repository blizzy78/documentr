package de.blizzy.documentr.web.branch;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.ArrayUtils;
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
		
		return !ArrayUtils.contains(DocumentrConstants.BRANCH_NAMES_BLACKLIST, value);
	}
}
