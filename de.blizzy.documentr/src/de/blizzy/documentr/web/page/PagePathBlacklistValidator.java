package de.blizzy.documentr.web.page;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import de.blizzy.documentr.DocumentrConstants;

public class PagePathBlacklistValidator implements ConstraintValidator<PagePathNotBlacklisted, String> {
	@Override
	public void initialize(PagePathNotBlacklisted annotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (StringUtils.isBlank(value)) {
			return true;
		}

		return !Pattern.matches("^" + DocumentrConstants.PAGE_PATHS_BLACKLIST_PATTERN + "$", value); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
