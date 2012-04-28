package de.blizzy.documentr.web.project;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=ProjectDoesNotExistValidator.class)
public @interface ProjectDoesNotExist {
	String message() default "{project.name.exists}";
	Class<?>[] groups() default {};
	Class<?>[] payload() default {};
}
