package de.blizzy.documentr.web.branch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=BranchNameBlacklistValidator.class)
public @interface BranchNameNotBlacklisted {
	String message() default "{branch.name.blacklisted}";
	Class<?>[] groups() default {};
	Class<?>[] payload() default {};
}
