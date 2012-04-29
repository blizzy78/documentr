package de.blizzy.documentr.web.page;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=PagePathBlacklistValidator.class)
public @interface PagePathNotBlacklisted {
	String message() default "{page.path.blacklisted}";
	Class<?>[] groups() default {};
	Class<?>[] payload() default {};
}
