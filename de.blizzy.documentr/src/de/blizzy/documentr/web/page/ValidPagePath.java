package de.blizzy.documentr.web.page;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=PagePathValidator.class)
public @interface ValidPagePath {
	String message() default "{page.path.invalid}";
	Class<?>[] groups() default {};
	Class<?>[] payload() default {};
}
