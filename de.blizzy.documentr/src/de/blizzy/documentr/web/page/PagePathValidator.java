package de.blizzy.documentr.web.page;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import de.blizzy.documentr.DocumentrConstants;

public class PagePathValidator implements ConstraintValidator<ValidPagePath, String> {
	@Override
	public void initialize(ValidPagePath annotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (StringUtils.isBlank(value)) {
			return true;
		}

		return Pattern.matches("^" + DocumentrConstants.PAGE_PATH_PATTERN + "$", value); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
