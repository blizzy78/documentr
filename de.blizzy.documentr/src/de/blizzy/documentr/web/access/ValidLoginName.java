package de.blizzy.documentr.web.access;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=LoginNameValidator.class)
public @interface ValidLoginName {
	String message() default "{user.loginName.invalid}";
	Class<?>[] groups() default {};
	Class<?>[] payload() default {};
}
