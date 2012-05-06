package de.blizzy.documentr.web.access;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import de.blizzy.documentr.DocumentrConstants;

public class LoginNameValidator implements ConstraintValidator<ValidLoginName, String> {
	@Override
	public void initialize(ValidLoginName annotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (StringUtils.isBlank(value)) {
			return true;
		}
		
		return Pattern.matches("^" + DocumentrConstants.USER_LOGIN_NAME_PATTERN + "$", value); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
